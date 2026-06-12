package com.ada.mudra.data.model

import kotlinx.serialization.Serializable

@Serializable
data class TrustedContact(
    val id: String,
    val displayName: String,
    val relationLabel: String,
    val phoneE164: String,
    val whatsappE164: String? = null,
    val photoFileName: String? = null,
    val canPhoneCall: Boolean = true,
    val canWhatsApp: Boolean = true,
    val sortOrder: Int = 0,
    val isActive: Boolean = true,
) {
    val whatsappNumber: String get() = whatsappE164 ?: phoneE164
}

@Serializable
data class GalleryPhoto(
    val id: String,
    val fileName: String,
    val caption: String = "",
    val addedAtEpochMs: Long,
)

@Serializable
data class AppSettings(
    val caregiverPin: String = DEFAULT_PIN,
    val hapticsEnabled: Boolean = true,
    val launcherModeEnabled: Boolean = false,
) {
    companion object {
        const val DEFAULT_PIN = "1234"
    }
}
