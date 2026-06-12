package com.ada.mudra

import android.app.Application
import android.net.Uri
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.ada.mudra.data.model.AppSettings
import com.ada.mudra.data.model.GalleryPhoto
import com.ada.mudra.data.model.TrustedContact
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

    val phoneCallProvider: PhoneCallProvider = AndroidPhoneCallProvider(application)
    val whatsAppProvider: WhatsAppProvider = AndroidWhatsAppProvider(application)

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
        viewModelScope.launch { repository.upsertContact(contact) }
    }

    fun deleteContact(id: String) {
        viewModelScope.launch { repository.deleteContact(id) }
    }

    fun saveContactAvatar(uri: Uri, onSaved: (String) -> Unit) {
        viewModelScope.launch { onSaved(repository.saveContactAvatar(uri)) }
    }

    fun addPhoto(uri: Uri) {
        viewModelScope.launch { repository.addPhoto(uri) }
    }

    fun updatePhotoCaption(id: String, caption: String) {
        viewModelScope.launch { repository.updatePhotoCaption(id, caption) }
    }

    fun deletePhoto(id: String) {
        viewModelScope.launch { repository.deletePhoto(id) }
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
