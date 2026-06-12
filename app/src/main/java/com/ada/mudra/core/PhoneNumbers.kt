package com.ada.mudra.core

/**
 * Pure helpers for phone numbers. Numbers are stored in E.164 format (+ then
 * country code and digits) so they work for dialing and WhatsApp links alike.
 */
object PhoneNumbers {

    private val e164Regex = Regex("""^\+[1-9]\d{6,14}$""")

    /** Drops spaces, dashes and brackets that people naturally type. */
    fun normalize(raw: String): String =
        raw.filter { it.isDigit() || it == '+' }

    fun isValidE164(raw: String): Boolean =
        e164Regex.matches(normalize(raw))

    /**
     * Official WhatsApp click-to-chat link: https://wa.me/<digits without + >.
     * See https://faq.whatsapp.com/5913398998672934
     */
    fun waLink(e164: String): String =
        "https://wa.me/" + e164.filter { it.isDigit() }
}
