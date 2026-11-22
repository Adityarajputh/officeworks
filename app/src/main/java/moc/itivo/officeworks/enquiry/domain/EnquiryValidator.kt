package moc.itivo.officeworks.enquiry.domain

import javax.inject.Inject

enum class FieldError { REQUIRED, INVALID_EMAIL, INVALID_PHONE, TOO_LONG }

data class EnquiryValidation(
    val name: FieldError? = null,
    val email: FieldError? = null,
    val phone: FieldError? = null,
    val message: FieldError? = null,
) {
    val isValid: Boolean get() = name == null && email == null && phone == null && message == null
}

/**
 * Client-side checks so the customer gets instant feedback. The backend must validate again;
 * client checks are for user experience, not security.
 *
 * Name and email are required (sales needs a way to reply). Phone and message are optional.
 */
class EnquiryValidator @Inject constructor() {

    fun validate(form: EnquiryForm): EnquiryValidation {
        val name = form.name.trim()
        val email = form.email.trim()
        val phone = form.phone.trim()
        val message = form.message.trim()

        return EnquiryValidation(
            name = when {
                name.isEmpty() -> FieldError.REQUIRED
                name.length > MAX_NAME -> FieldError.TOO_LONG
                else -> null
            },
            email = when {
                email.isEmpty() -> FieldError.REQUIRED
                email.length > MAX_EMAIL -> FieldError.TOO_LONG
                !EMAIL.matches(email) -> FieldError.INVALID_EMAIL
                else -> null
            },
            phone = when {
                phone.isEmpty() -> null
                !PHONE.matches(phone) || phone.count(Char::isDigit) !in MIN_PHONE_DIGITS..MAX_PHONE_DIGITS ->
                    FieldError.INVALID_PHONE
                else -> null
            },
            message = if (message.length > MAX_MESSAGE) FieldError.TOO_LONG else null,
        )
    }

    companion object {
        const val MAX_NAME = 100
        const val MAX_EMAIL = 254
        const val MAX_MESSAGE = 1000
        private const val MIN_PHONE_DIGITS = 6
        private const val MAX_PHONE_DIGITS = 15

        // Deliberately simple. android.util.Patterns is not available in JVM unit tests, and
        // a perfect RFC 5322 regex is not worth it: the real check is whether sales can reply.
        private val EMAIL = Regex("^[A-Za-z0-9._%+-]+@[A-Za-z0-9-]+(\\.[A-Za-z0-9-]+)*\\.[A-Za-z]{2,}$")
        private val PHONE = Regex("^\\+?[0-9 ()-]+$")
    }
}
