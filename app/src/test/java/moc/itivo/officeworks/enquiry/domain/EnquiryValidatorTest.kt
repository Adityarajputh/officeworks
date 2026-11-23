package moc.itivo.officeworks.enquiry.domain

import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Test

class EnquiryValidatorTest {

    private val validator = EnquiryValidator()
    private val valid = EnquiryForm(name = "Sam Lee", email = "sam@example.com")

    @Test
    fun `name and email are enough`() {
        assertTrue(validator.validate(valid).isValid)
    }

    @Test
    fun `name and email are required, whitespace does not count`() {
        val result = validator.validate(EnquiryForm(name = "   ", email = ""))

        assertFalse(result.isValid)
        assertEquals(FieldError.REQUIRED, result.name)
        assertEquals(FieldError.REQUIRED, result.email)
    }

    @Test
    fun `rejects malformed emails`() {
        listOf("sam", "sam@", "@example.com", "sam@example", "sam example@x.com", "sam@exa mple.com").forEach {
            assertEquals("for '$it'", FieldError.INVALID_EMAIL, validator.validate(valid.copy(email = it)).email)
        }
    }

    @Test
    fun `accepts common real emails`() {
        listOf("sam.lee+quotes@example.com.au", "SAM@EXAMPLE.CO", " sam@example.com ").forEach {
            assertNull("for '$it'", validator.validate(valid.copy(email = it)).email)
        }
    }

    @Test
    fun `phone is optional but must look like a phone number when given`() {
        assertNull(validator.validate(valid.copy(phone = "")).phone)
        assertNull(validator.validate(valid.copy(phone = "+61 (02) 9876-5432")).phone)
        assertEquals(FieldError.INVALID_PHONE, validator.validate(valid.copy(phone = "call me")).phone)
        assertEquals(FieldError.INVALID_PHONE, validator.validate(valid.copy(phone = "123")).phone)
    }

    @Test
    fun `limits lengths`() {
        assertEquals(FieldError.TOO_LONG, validator.validate(valid.copy(name = "a".repeat(101))).name)
        assertEquals(FieldError.TOO_LONG, validator.validate(valid.copy(message = "a".repeat(1001))).message)
        assertNull(validator.validate(valid.copy(message = "a".repeat(1000))).message)
    }
}
