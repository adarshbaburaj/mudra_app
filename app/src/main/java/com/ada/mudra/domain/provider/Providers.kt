package com.ada.mudra.domain.provider

import android.content.Context
import android.content.Intent
import android.net.Uri
import com.ada.mudra.core.PhoneNumbers

interface PhoneCallProvider {
    /** Safe Dial Mode: opens the system dialer with the number pre-filled (ACTION_DIAL). */
    fun openDialer(phoneE164: String): Result<Unit>
}

interface WhatsAppProvider {
    fun isWhatsAppInstalled(): Boolean

    /** Opens the chat of one trusted contact via the official wa.me link. Never sends anything. */
    fun openTrustedChat(whatsappE164: String): Result<Unit>
}

/**
 * Video calling is intentionally stubbed in this MVP. The senior UI never shows
 * it until a provider (Jitsi/Daily/...) is chosen and implemented behind this
 * interface in a later milestone.
 */
interface VideoCallProvider {
    suspend fun startOutgoingCall(contactId: String): Result<Unit>
}

class AndroidPhoneCallProvider(private val context: Context) : PhoneCallProvider {
    override fun openDialer(phoneE164: String): Result<Unit> = runCatching {
        val intent = Intent(Intent.ACTION_DIAL, Uri.parse("tel:$phoneE164"))
            .addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
        context.startActivity(intent)
    }
}

class AndroidWhatsAppProvider(private val context: Context) : WhatsAppProvider {

    private val whatsAppPackages = listOf("com.whatsapp", "com.whatsapp.w4b")

    override fun isWhatsAppInstalled(): Boolean = installedPackage() != null

    override fun openTrustedChat(whatsappE164: String): Result<Unit> = runCatching {
        val intent = Intent(Intent.ACTION_VIEW, Uri.parse(PhoneNumbers.waLink(whatsappE164)))
            .addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
        installedPackage()?.let { intent.setPackage(it) }
        context.startActivity(intent)
    }

    private fun installedPackage(): String? = whatsAppPackages.firstOrNull { pkg ->
        runCatching { context.packageManager.getPackageInfo(pkg, 0) }.isSuccess
    }
}
