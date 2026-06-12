package com.ada.mudra.data.remote

import com.ada.mudra.data.MudraRepository
import com.ada.mudra.data.model.GalleryPhoto
import com.ada.mudra.data.model.TrustedContact
import io.github.jan.supabase.gotrue.auth
import io.github.jan.supabase.gotrue.providers.builtin.Email
import io.github.jan.supabase.gotrue.SessionStatus
import io.github.jan.supabase.postgrest.from
import io.github.jan.supabase.postgrest.postgrest
import io.github.jan.supabase.postgrest.rpc
import io.github.jan.supabase.realtime.PostgresAction
import io.github.jan.supabase.realtime.RealtimeChannel
import io.github.jan.supabase.realtime.channel
import io.github.jan.supabase.realtime.postgresChangeFlow
import io.github.jan.supabase.storage.storage
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.launchIn
import kotlinx.coroutines.flow.onEach
import kotlinx.coroutines.launch
import kotlinx.serialization.json.buildJsonObject
import kotlinx.serialization.json.put
import java.time.OffsetDateTime

/**
 * Keeps the phone's local data (DataStore + files) in sync with Supabase.
 *
 * Philosophy: local-first. The senior screens only ever read local data, so
 * the app works fully offline. When signed in, caregiver edits are pushed
 * immediately, and any change on another family device arrives via Realtime
 * and triggers a pull that replaces the local cache (server wins).
 */
class SyncManager(
    private val repository: MudraRepository,
    private val scope: CoroutineScope,
) {

    sealed interface State {
        /** No Supabase keys in this build: app is local-only, sync UI hidden. */
        data object NotConfigured : State
        data object Loading : State
        data object SignedOut : State
        /** Signed in but no family yet — show the create-family step. */
        data object NeedsFamily : State
        data class Ready(val familyId: String, val seniorProfileId: String) : State
    }

    private val client = SupabaseClientProvider.client

    private val _state = MutableStateFlow<State>(
        if (client == null) State.NotConfigured else State.Loading
    )
    val state: StateFlow<State> = _state

    private val _lastError = MutableStateFlow<String?>(null)
    val lastError: StateFlow<String?> = _lastError

    private val _syncing = MutableStateFlow(false)
    val syncing: StateFlow<Boolean> = _syncing

    private var realtimeChannel: RealtimeChannel? = null
    private var pendingPull: Job? = null

    private companion object {
        const val BUCKET = "family-gallery"
    }

    init {
        client?.let { c ->
            scope.launch {
                c.auth.sessionStatus.collect { status ->
                    when (status) {
                        is SessionStatus.Authenticated -> refreshFamilyState()
                        is SessionStatus.NotAuthenticated -> _state.value = State.SignedOut
                        is SessionStatus.LoadingFromStorage -> _state.value = State.Loading
                        is SessionStatus.NetworkError -> {
                            // Offline: stay quiet, local cache keeps the app working.
                            if (_state.value is State.Loading) _state.value = State.SignedOut
                        }
                    }
                }
            }
        }
    }

    // ---------- Auth ----------

    suspend fun signUp(email: String, password: String) {
        val c = client ?: return
        runReporting {
            c.auth.signUpWith(Email) {
                this.email = email.trim()
                this.password = password
            }
        }
    }

    /** True when sign-up finished but email confirmation is still pending. */
    fun awaitingEmailConfirmation(): Boolean =
        client?.auth?.currentSessionOrNull() == null

    suspend fun signIn(email: String, password: String) {
        val c = client ?: return
        runReporting {
            c.auth.signInWith(Email) {
                this.email = email.trim()
                this.password = password
            }
        }
    }

    suspend fun signOut() {
        val c = client ?: return
        stopRealtime()
        runReporting { c.auth.signOut() }
    }

    // ---------- Family ----------

    suspend fun createFamily(familyName: String, seniorName: String) {
        val c = client ?: return
        runReporting {
            val familyId = c.postgrest.rpc(
                "create_family",
                buildJsonObject { put("family_name", familyName.trim()) },
            ).decodeAs<String>()
            c.from("senior_profiles").insert(
                SeniorProfileWrite(familyId = familyId, displayName = seniorName.trim())
            )
            refreshFamilyState()
        }
    }

    private suspend fun refreshFamilyState() {
        val c = client ?: return
        val userId = c.auth.currentUserOrNull()?.id ?: run {
            _state.value = State.SignedOut
            return
        }
        runReporting {
            val membership = c.from("family_members").select {
                filter { eq("user_id", userId) }
                limit(1)
            }.decodeList<RemoteFamilyMember>().firstOrNull()

            val senior = membership?.let { m ->
                c.from("senior_profiles").select {
                    filter { eq("family_id", m.familyId) }
                    limit(1)
                }.decodeList<RemoteSeniorProfile>().firstOrNull()
            }

            if (membership == null || senior == null) {
                _state.value = State.NeedsFamily
            } else {
                _state.value = State.Ready(membership.familyId, senior.id)
                bootstrapSync()
                startRealtime(membership.familyId)
            }
        }
    }

    // ---------- Sync ----------

    /**
     * First sync after connecting: if the server is empty but this phone
     * already has contacts/photos (pre-sync usage), upload them; otherwise
     * the server is the source of truth and we pull.
     */
    private suspend fun bootstrapSync() {
        val ready = _state.value as? State.Ready ?: return
        val c = client ?: return
        val remoteContacts = c.from("trusted_contacts").select {
            filter { eq("family_id", ready.familyId) }
        }.decodeList<RemoteContact>()

        if (remoteContacts.isEmpty()) {
            repository.contacts.first().forEach { pushContact(it) }
            repository.photos.first().forEach { photo -> uploadPhoto(photo) }
        }
        pullAll()
    }

    suspend fun pullAll() {
        val ready = _state.value as? State.Ready ?: return
        val c = client ?: return
        _syncing.value = true
        try {
            runReporting {
                // Contacts: download avatars we don't have, then swap the local list.
                val remoteContacts = c.from("trusted_contacts").select {
                    filter { eq("family_id", ready.familyId) }
                }.decodeList<RemoteContact>()

                val localContacts = remoteContacts.map { remote ->
                    val avatarName = remote.avatarPath?.let { path ->
                        val fileName = "${remote.id}.img"
                        if (!repository.avatarFile(fileName).exists()) {
                            val bytes = c.storage.from(BUCKET).downloadAuthenticated(path)
                            repository.writeAvatarBytes(fileName, bytes)
                        }
                        fileName
                    }
                    TrustedContact(
                        id = remote.id,
                        displayName = remote.displayName,
                        relationLabel = remote.relationLabel,
                        phoneE164 = remote.phoneE164 ?: remote.whatsappE164.orEmpty(),
                        whatsappE164 = remote.whatsappE164,
                        photoFileName = avatarName,
                        canPhoneCall = remote.canPhoneCall && remote.phoneE164 != null,
                        canWhatsApp = remote.canWhatsApp,
                        sortOrder = remote.sortOrder,
                        isActive = remote.isActive,
                    )
                }
                repository.replaceAllContacts(localContacts)

                // Gallery: download missing images, then swap the local list.
                val remoteItems = c.from("gallery_items").select {
                    filter { eq("family_id", ready.familyId) }
                }.decodeList<RemoteGalleryItem>()

                val localPhotos = remoteItems.map { remote ->
                    val fileName = "${remote.id}.img"
                    if (!repository.photoFile(fileName).exists()) {
                        val bytes = c.storage.from(BUCKET).downloadAuthenticated(remote.storagePath)
                        repository.writeGalleryBytes(fileName, bytes)
                    }
                    GalleryPhoto(
                        id = remote.id,
                        fileName = fileName,
                        caption = remote.caption.orEmpty(),
                        addedAtEpochMs = remote.createdAt?.let { raw ->
                            runCatching {
                                OffsetDateTime.parse(raw).toInstant().toEpochMilli()
                            }.getOrNull()
                        } ?: System.currentTimeMillis(),
                    )
                }
                repository.replaceAllPhotos(localPhotos)
            }
        } finally {
            _syncing.value = false
        }
    }

    // ---------- Pushes (called after local writes; no-ops when not Ready) ----------

    fun onContactSaved(contact: TrustedContact) {
        scope.launch { runReporting { pushContact(contact) } }
    }

    fun onContactDeleted(contactId: String) {
        val ready = _state.value as? State.Ready ?: return
        val c = client ?: return
        scope.launch {
            runReporting {
                c.from("trusted_contacts").delete { filter { eq("id", contactId) } }
                runCatching {
                    c.storage.from(BUCKET).delete(listOf("${ready.familyId}/avatars/$contactId.img"))
                }
            }
        }
    }

    fun onPhotoAdded(photo: GalleryPhoto) {
        scope.launch { runReporting { uploadPhoto(photo) } }
    }

    fun onCaptionChanged(photoId: String, caption: String) {
        val c = client ?: return
        if (_state.value !is State.Ready) return
        scope.launch {
            runReporting {
                c.from("gallery_items").update({ set("caption", caption) }) {
                    filter { eq("id", photoId) }
                }
            }
        }
    }

    fun onPhotoDeleted(photoId: String) {
        val ready = _state.value as? State.Ready ?: return
        val c = client ?: return
        scope.launch {
            runReporting {
                c.from("gallery_items").delete { filter { eq("id", photoId) } }
                runCatching {
                    c.storage.from(BUCKET)
                        .delete(listOf("${ready.familyId}/${ready.seniorProfileId}/$photoId.img"))
                }
            }
        }
    }

    private suspend fun pushContact(contact: TrustedContact) {
        val ready = _state.value as? State.Ready ?: return
        val c = client ?: return
        val avatarPath = contact.photoFileName?.let { fileName ->
            val file = repository.avatarFile(fileName)
            if (file.exists()) {
                val path = "${ready.familyId}/avatars/${contact.id}.img"
                c.storage.from(BUCKET).upload(path, file.readBytes(), upsert = true)
                path
            } else null
        }
        c.from("trusted_contacts").upsert(
            RemoteContact(
                id = contact.id,
                familyId = ready.familyId,
                seniorProfileId = ready.seniorProfileId,
                displayName = contact.displayName,
                relationLabel = contact.relationLabel,
                phoneE164 = contact.phoneE164.ifBlank { null },
                whatsappE164 = contact.whatsappE164,
                avatarPath = avatarPath,
                canPhoneCall = contact.canPhoneCall,
                canWhatsApp = contact.canWhatsApp,
                sortOrder = contact.sortOrder,
                isActive = contact.isActive,
            )
        )
    }

    private suspend fun uploadPhoto(photo: GalleryPhoto) {
        val ready = _state.value as? State.Ready ?: return
        val c = client ?: return
        val file = repository.photoFile(photo.fileName)
        if (!file.exists()) return
        val path = "${ready.familyId}/${ready.seniorProfileId}/${photo.id}.img"
        c.storage.from(BUCKET).upload(path, file.readBytes(), upsert = true)
        c.from("gallery_items").upsert(
            GalleryItemWrite(
                id = photo.id,
                familyId = ready.familyId,
                seniorProfileId = ready.seniorProfileId,
                storagePath = path,
                caption = photo.caption.ifBlank { null },
            )
        )
    }

    // ---------- Realtime ----------

    private suspend fun startRealtime(familyId: String) {
        val c = client ?: return
        if (realtimeChannel != null) return
        val channel = c.channel("family-$familyId")
        channel.postgresChangeFlow<PostgresAction>(schema = "public") {
            table = "trusted_contacts"
            filter = "family_id=eq.$familyId"
        }.onEach { schedulePull() }.launchIn(scope)
        channel.postgresChangeFlow<PostgresAction>(schema = "public") {
            table = "gallery_items"
            filter = "family_id=eq.$familyId"
        }.onEach { schedulePull() }.launchIn(scope)
        channel.subscribe()
        realtimeChannel = channel
    }

    private suspend fun stopRealtime() {
        realtimeChannel?.let { runCatching { it.unsubscribe() } }
        realtimeChannel = null
    }

    /** Coalesces bursts of change events into a single pull. */
    private fun schedulePull() {
        pendingPull?.cancel()
        pendingPull = scope.launch {
            delay(700)
            pullAll()
        }
    }

    private suspend fun runReporting(block: suspend () -> Unit) {
        try {
            _lastError.value = null
            block()
        } catch (e: Exception) {
            _lastError.value = e.message ?: e.toString()
        }
    }
}
