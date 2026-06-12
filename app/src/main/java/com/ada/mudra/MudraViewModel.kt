package com.ada.mudra

import android.app.Application
import android.net.Uri
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.ada.mudra.data.model.AppSettings
import com.ada.mudra.data.model.GalleryPhoto
import com.ada.mudra.data.model.TrustedContact
import com.ada.mudra.data.remote.SyncManager
import com.ada.mudra.domain.provider.AndroidPhoneCallProvider
import com.ada.mudra.domain.provider.AndroidWhatsAppProvider
import com.ada.mudra.domain.provider.PhoneCallProvider
import com.ada.mudra.domain.provider.WhatsAppProvider
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import java.io.File

class MudraViewModel(application: Application) : AndroidViewModel(application) {

    private val repository = (application as MudraApp).repository
    private val sync = (application as MudraApp).syncManager

    val phoneCallProvider: PhoneCallProvider = AndroidPhoneCallProvider(application)
    val whatsAppProvider: WhatsAppProvider = AndroidWhatsAppProvider(application)

    val syncState: StateFlow<SyncManager.State> = sync.state
    val syncError: StateFlow<String?> = sync.lastError
    val syncing: StateFlow<Boolean> = sync.syncing

    val contacts: StateFlow<List<TrustedContact>> = repository.contacts
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000), emptyList())

    val photos: StateFlow<List<GalleryPhoto>> = repository.photos
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000), emptyList())

    val settings: StateFlow<AppSettings> = repository.settings
        .stateIn(viewModelScope, SharingStarted.Eagerly, AppSettings())

    fun contactById(id: String?): TrustedContact? = contacts.value.firstOrNull { it.id == id }

    fun avatarFile(contact: TrustedContact): File? =
        contact.photoFileName?.let { repository.avatarFile(it) }

    fun avatarFileByName(fileName: String): File = repository.avatarFile(fileName)

    fun photoFile(photo: GalleryPhoto): File = repository.photoFile(photo.fileName)

    fun upsertContact(contact: TrustedContact) {
        viewModelScope.launch {
            repository.upsertContact(contact)
            sync.onContactSaved(contact)
        }
    }

    fun deleteContact(id: String) {
        viewModelScope.launch {
            repository.deleteContact(id)
            sync.onContactDeleted(id)
        }
    }

    fun saveContactAvatar(uri: Uri, onSaved: (String) -> Unit) {
        viewModelScope.launch { onSaved(repository.saveContactAvatar(uri)) }
    }

    fun addPhoto(uri: Uri) {
        viewModelScope.launch {
            val photo = repository.addPhoto(uri)
            sync.onPhotoAdded(photo)
        }
    }

    fun updatePhotoCaption(id: String, caption: String) {
        viewModelScope.launch {
            repository.updatePhotoCaption(id, caption)
            sync.onCaptionChanged(id, caption)
        }
    }

    fun deletePhoto(id: String) {
        viewModelScope.launch {
            repository.deletePhoto(id)
            sync.onPhotoDeleted(id)
        }
    }

    // ---- Account & sync ----

    fun signIn(email: String, password: String) {
        viewModelScope.launch { sync.signIn(email, password) }
    }

    fun signUp(email: String, password: String, onPendingConfirmation: () -> Unit) {
        viewModelScope.launch {
            sync.signUp(email, password)
            if (sync.lastError.value == null && sync.awaitingEmailConfirmation()) {
                onPendingConfirmation()
            }
        }
    }

    fun signOut() {
        viewModelScope.launch { sync.signOut() }
    }

    fun createFamily(familyName: String, seniorName: String) {
        viewModelScope.launch { sync.createFamily(familyName, seniorName) }
    }

    fun syncNow() {
        viewModelScope.launch { sync.pullAll() }
    }

    fun setHapticsEnabled(enabled: Boolean) {
        viewModelScope.launch { repository.updateSettings { it.copy(hapticsEnabled = enabled) } }
    }

    fun setLauncherModeEnabled(enabled: Boolean) {
        viewModelScope.launch { repository.updateSettings { it.copy(launcherModeEnabled = enabled) } }
    }

    fun changePin(newPin: String) {
        viewModelScope.launch { repository.updateSettings { it.copy(caregiverPin = newPin) } }
    }
}
