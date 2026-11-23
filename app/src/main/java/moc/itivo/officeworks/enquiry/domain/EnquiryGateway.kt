package moc.itivo.officeworks.enquiry.domain

import moc.itivo.officeworks.core.result.DataResult

/**
 * Delivers an enquiry to the sales team.
 *
 * The app must never send email itself: that would mean shipping email-provider credentials
 * inside the APK, where anyone can extract them. In production this posts to our backend,
 * which owns the provider (SES, SendGrid, ...) and its secrets. Changing provider is then a
 * backend change only.
 */
fun interface EnquiryGateway {
    suspend fun submit(enquiry: Enquiry): DataResult<Unit>
}
