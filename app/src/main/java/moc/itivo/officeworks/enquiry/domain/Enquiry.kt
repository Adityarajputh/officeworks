package moc.itivo.officeworks.enquiry.domain

/** What the customer typed into the enquiry form, before validation. */
data class EnquiryForm(
    val name: String = "",
    val email: String = "",
    val phone: String = "",
    val message: String = "",
)

/** A validated, normalised enquiry, ready to hand to an [EnquiryGateway]. */
data class Enquiry(
    val productId: Int,
    val productTitle: String,
    val customerName: String,
    val customerEmail: String,
    val customerPhone: String?,
    val message: String?,
)
