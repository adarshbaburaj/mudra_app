package com.ada.mudra.data.remote

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

/**
 * Wire formats matching the Supabase tables (snake_case columns).
 * Write DTOs contain exactly the columns we set, so server-side
 * defaults (created_at, updated_at, ...) are never overwritten.
 */

@Serializable
data class RemoteFamilyMember(
    @SerialName("family_id") val familyId: String,
    @SerialName("user_id") val userId: String,
    val role: String = "caregiver",
)

@Serializable
data class RemoteSeniorProfile(
    val id: String,
    @SerialName("family_id") val familyId: String,
    @SerialName("display_name") val displayName: String,
)

@Serializable
data class SeniorProfileWrite(
    @SerialName("family_id") val familyId: String,
    @SerialName("display_name") val displayName: String,
)

@Serializable
data class RemoteContact(
    val id: String,
    @SerialName("family_id") val familyId: String,
    @SerialName("senior_profile_id") val seniorProfileId: String,
    @SerialName("display_name") val displayName: String,
    @SerialName("relation_label") val relationLabel: String,
    @SerialName("phone_e164") val phoneE164: String? = null,
    @SerialName("whatsapp_e164") val whatsappE164: String? = null,
    @SerialName("avatar_path") val avatarPath: String? = null,
    @SerialName("can_phone_call") val canPhoneCall: Boolean = true,
    @SerialName("can_whatsapp") val canWhatsApp: Boolean = false,
    @SerialName("sort_order") val sortOrder: Int = 0,
    @SerialName("is_active") val isActive: Boolean = true,
)

@Serializable
data class RemoteGalleryItem(
    val id: String,
    @SerialName("family_id") val familyId: String,
    @SerialName("senior_profile_id") val seniorProfileId: String,
    @SerialName("storage_path") val storagePath: String,
    val caption: String? = null,
    @SerialName("created_at") val createdAt: String? = null,
)

@Serializable
data class GalleryItemWrite(
    val id: String,
    @SerialName("family_id") val familyId: String,
    @SerialName("senior_profile_id") val seniorProfileId: String,
    @SerialName("storage_path") val storagePath: String,
    val caption: String? = null,
)
