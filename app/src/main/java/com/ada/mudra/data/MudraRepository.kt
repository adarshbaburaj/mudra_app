package com.ada.mudra.data

import android.content.Context
import android.net.Uri
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.stringPreferencesKey
import androidx.datastore.preferences.preferencesDataStore
import com.ada.mudra.data.model.AppSettings
import com.ada.mudra.data.model.GalleryPhoto
import com.ada.mudra.data.model.TrustedContact
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.withContext
import kotlinx.serialization.builtins.ListSerializer
import kotlinx.serialization.decodeFromString
import kotlinx.serialization.json.Json
import java.io.File
import java.util.UUID

private val Context.dataStore by preferencesDataStore(name = "mudra_store")

/**
 * Local-first storage for the MVP: contacts, photos and settings live only on
 * this phone (DataStore + app-private files). The same interface will back the
 * Supabase-synced version in the next milestone, so screens never need to know
 * where the data comes from.
 */
class MudraRepository(private val context: Context) {

    private val json = Json { ignoreUnknownKeys = true }

    private val contactsKey = stringPreferencesKey("contacts_json")
    private val photosKey = stringPreferencesKey("photos_json")
    private val settingsKey = stringPreferencesKey("settings_json")

    val contacts: Flow<List<TrustedContact>> = context.dataStore.data.map { prefs ->
        decodeList<TrustedContact>(prefs[contactsKey])
            .sortedWith(compareBy({ it.sortOrder }, { it.displayName }))
    }

    val photos: Flow<List<GalleryPhoto>> = context.dataStore.data.map { prefs ->
        decodeList<GalleryPhoto>(prefs[photosKey]).sortedByDescending { it.addedAtEpochMs }
    }

    val settings: Flow<AppSettings> = context.dataStore.data.map { prefs ->
        prefs[settingsKey]?.let { raw ->
            runCatching { json.decodeFromString(AppSettings.serializer(), raw) }.getOrNull()
        } ?: AppSettings()
    }

    // ---- Contacts ----

    suspend fun upsertContact(contact: TrustedContact) {
        context.dataStore.edit { prefs ->
            val current = decodeList<TrustedContact>(prefs[contactsKey])
            val updated = current.filterNot { it.id == contact.id } + contact
            prefs[contactsKey] =
                json.encodeToString(ListSerializer(TrustedContact.serializer()), updated)
        }
    }

    suspend fun deleteContact(id: String) {
        context.dataStore.edit { prefs ->
            val current = decodeList<TrustedContact>(prefs[contactsKey])
            current.firstOrNull { it.id == id }?.photoFileName?.let { avatarFile(it).delete() }
            prefs[contactsKey] = json.encodeToString(
                ListSerializer(TrustedContact.serializer()),
                current.filterNot { it.id == id },
            )
        }
    }

    suspend fun saveContactAvatar(uri: Uri): String = withContext(Dispatchers.IO) {
        copyIntoDir(uri, avatarsDir())
    }

    // ---- Gallery ----

    suspend fun addPhoto(uri: Uri, caption: String = "") {
        val fileName = withContext(Dispatchers.IO) { copyIntoDir(uri, galleryDir()) }
        val photo = GalleryPhoto(
            id = UUID.randomUUID().toString(),
            fileName = fileName,
            caption = caption,
            addedAtEpochMs = System.currentTimeMillis(),
        )
        context.dataStore.edit { prefs ->
            val current = decodeList<GalleryPhoto>(prefs[photosKey])
            prefs[photosKey] =
                json.encodeToString(ListSerializer(GalleryPhoto.serializer()), current + photo)
        }
    }

    suspend fun updatePhotoCaption(id: String, caption: String) {
        context.dataStore.edit { prefs ->
            val current = decodeList<GalleryPhoto>(prefs[photosKey])
            prefs[photosKey] = json.encodeToString(
                ListSerializer(GalleryPhoto.serializer()),
                current.map { if (it.id == id) it.copy(caption = caption) else it },
            )
        }
    }

    suspend fun deletePhoto(id: String) {
        context.dataStore.edit { prefs ->
            val current = decodeList<GalleryPhoto>(prefs[photosKey])
            current.firstOrNull { it.id == id }?.let { photoFile(it.fileName).delete() }
            prefs[photosKey] = json.encodeToString(
                ListSerializer(GalleryPhoto.serializer()),
                current.filterNot { it.id == id },
            )
        }
    }

    // ---- Settings ----

    suspend fun updateSettings(transform: (AppSettings) -> AppSettings) {
        context.dataStore.edit { prefs ->
            val current = prefs[settingsKey]?.let { raw ->
                runCatching { json.decodeFromString(AppSettings.serializer(), raw) }.getOrNull()
            } ?: AppSettings()
            prefs[settingsKey] = json.encodeToString(AppSettings.serializer(), transform(current))
        }
    }

    // ---- Files ----

    fun photoFile(fileName: String): File = File(galleryDir(), fileName)

    fun avatarFile(fileName: String): File = File(avatarsDir(), fileName)

    private fun galleryDir(): File = File(context.filesDir, "gallery").apply { mkdirs() }

    private fun avatarsDir(): File = File(context.filesDir, "avatars").apply { mkdirs() }

    private fun copyIntoDir(uri: Uri, dir: File): String {
        val fileName = UUID.randomUUID().toString() + ".img"
        val target = File(dir, fileName)
        context.contentResolver.openInputStream(uri).use { input ->
            requireNotNull(input) { "Could not open selected image" }
            target.outputStream().use { output -> input.copyTo(output) }
        }
        return fileName
    }

    private inline fun <reified T> decodeList(raw: String?): List<T> =
        raw?.let { runCatching { json.decodeFromString<List<T>>(it) }.getOrNull() } ?: emptyList()
}
