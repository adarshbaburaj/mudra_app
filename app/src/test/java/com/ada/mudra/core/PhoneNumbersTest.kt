package com.ada.mudra.core

import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test

class PhoneNumbersTest {

    @Test
    fun `valid E164 numbers are accepted`() {
        assertTrue(PhoneNumbers.isValidE164("+919812345678"))
        assertTrue(PhoneNumbers.isValidE164("+971501234567"))
        assertTrue(PhoneNumbers.isValidE164("+14155552671"))
    }

    @Test
    fun `numbers typed with spaces and dashes are normalized then accepted`() {
        assertTrue(PhoneNumbers.isValidE164("+91 98123 45678"))
        assertTrue(PhoneNumbers.isValidE164("+1 (415) 555-2671"))
    }

    @Test
    fun `numbers without plus or country code are rejected`() {
        assertFalse(PhoneNumbers.isValidE164("9812345678"))
        assertFalse(PhoneNumbers.isValidE164("0501234567"))
        assertFalse(PhoneNumbers.isValidE164(""))
        assertFalse(PhoneNumbers.isValidE164("+0123456789"))
        assertFalse(PhoneNumbers.isValidE164("hello"))
    }

    @Test
    fun `normalize keeps only digits and plus`() {
        assertEquals("+919812345678", PhoneNumbers.normalize("+91 98123-45678"))
        assertEquals("+14155552671", PhoneNumbers.normalize("+1 (415) 555 2671"))
    }

    @Test
    fun `wa link uses digits only without plus`() {
        assertEquals("https://wa.me/919812345678", PhoneNumbers.waLink("+919812345678"))
        assertEquals("https://wa.me/971501234567", PhoneNumbers.waLink("+971501234567"))
    }
}
