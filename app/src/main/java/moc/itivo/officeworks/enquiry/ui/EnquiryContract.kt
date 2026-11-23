package moc.itivo.officeworks.enquiry.ui

import moc.itivo.officeworks.core.result.DataError
import moc.itivo.officeworks.enquiry.domain.EnquiryForm
import moc.itivo.officeworks.enquiry.domain.EnquiryValidation

data class EnquiryState(
    val productTitle: String,
    val form: EnquiryForm = EnquiryForm(),
    val errors: EnquiryValidation = EnquiryValidation(),
    val isSubmitting: Boolean = false,
    val isSent: Boolean = false,
)

sealed interface EnquiryIntent {
    data class NameChanged(val value: String) : EnquiryIntent
    data class EmailChanged(val value: String) : EnquiryIntent
    data class PhoneChanged(val value: String) : EnquiryIntent
    data class MessageChanged(val value: String) : EnquiryIntent
    data object Submit : EnquiryIntent
}

sealed interface EnquiryEffect {
    /** Sending failed; the form keeps what the customer typed so they can retry. */
    data class SubmitFailed(val error: DataError) : EnquiryEffect
}
