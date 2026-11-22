package moc.itivo.officeworks.enquiry.domain

import moc.itivo.officeworks.core.result.DataError
import moc.itivo.officeworks.core.result.DataResult
import javax.inject.Inject

sealed interface SubmitEnquiryResult {
    data object Sent : SubmitEnquiryResult
    data class Invalid(val validation: EnquiryValidation) : SubmitEnquiryResult
    data class Failed(val error: DataError) : SubmitEnquiryResult
}

/**
 * Validates the form and only then sends it, so no caller can send an invalid enquiry.
 * Also trims input and turns blank optional fields into null.
 */
class SubmitEnquiryUseCase @Inject constructor(
    private val validator: EnquiryValidator,
    private val gateway: EnquiryGateway,
) {
    suspend operator fun invoke(productId: Int, productTitle: String, form: EnquiryForm): SubmitEnquiryResult {
        val validation = validator.validate(form)
        if (!validation.isValid) return SubmitEnquiryResult.Invalid(validation)

        val enquiry = Enquiry(
            productId = productId,
            productTitle = productTitle,
            customerName = form.name.trim(),
            customerEmail = form.email.trim(),
            customerPhone = form.phone.trim().ifEmpty { null },
            message = form.message.trim().ifEmpty { null },
        )
        return when (val result = gateway.submit(enquiry)) {
            is DataResult.Success -> SubmitEnquiryResult.Sent
            is DataResult.Failure -> SubmitEnquiryResult.Failed(result.error)
        }
    }
}
