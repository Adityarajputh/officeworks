package moc.itivo.officeworks.enquiry

import kotlinx.coroutines.CompletableDeferred
import moc.itivo.officeworks.core.result.DataResult
import moc.itivo.officeworks.enquiry.domain.Enquiry
import moc.itivo.officeworks.enquiry.domain.EnquiryGateway

class FakeEnquiryGateway : EnquiryGateway {
    var result: DataResult<Unit> = DataResult.Success(Unit)
    var gate: CompletableDeferred<Unit>? = null
    val sent = mutableListOf<Enquiry>()

    override suspend fun submit(enquiry: Enquiry): DataResult<Unit> {
        sent += enquiry
        gate?.await()
        return result
    }
}
