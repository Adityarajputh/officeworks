package moc.itivo.officeworks.enquiry.data

import kotlinx.serialization.Serializable
import moc.itivo.officeworks.core.network.safeApiCall
import moc.itivo.officeworks.core.result.DataResult
import moc.itivo.officeworks.enquiry.domain.Enquiry
import moc.itivo.officeworks.enquiry.domain.EnquiryGateway
import retrofit2.http.Body
import retrofit2.http.POST
import javax.inject.Inject

/**
 * PROOF-OF-CONCEPT STAND-IN. No email is sent.
 *
 * DummyJSON has no email endpoint; `POST /posts/add` accepts a payload, answers like a real
 * create call and stores nothing. Using it means the enquiry flow makes a real network call
 * with real failure modes (offline, timeouts, 5xx) that the UI has to handle.
 *
 * Replace with a gateway that posts the [Enquiry] to our backend, which emails the sales
 * inbox. Note that this stand-in sends the form contents to a public third-party test API,
 * so demos should use made-up contact details.
 */
class DummyJsonEnquiryGateway @Inject constructor(
    private val api: DummyJsonEnquiryApi,
) : EnquiryGateway {

    override suspend fun submit(enquiry: Enquiry): DataResult<Unit> = safeApiCall {
        api.addPost(
            SimulatedPostDto(
                title = "Product enquiry: ${enquiry.productTitle}",
                body = enquiry.toEmailBody(),
                userId = SIMULATED_USER_ID,
            ),
        )
        Unit
    }

    private companion object {
        // DummyJSON requires a userId that exists in its fixtures; it has no meaning here.
        const val SIMULATED_USER_ID = 1
    }
}

/** The email body the sales inbox would receive. Kept plain text on purpose. */
internal fun Enquiry.toEmailBody(): String = buildString {
    appendLine("Product: $productTitle (id $productId)")
    appendLine("Name: $customerName")
    appendLine("Email: $customerEmail")
    customerPhone?.let { appendLine("Phone: $it") }
    message?.let {
        appendLine()
        append(it)
    }
}.trimEnd()

interface DummyJsonEnquiryApi {
    @POST("posts/add")
    suspend fun addPost(@Body post: SimulatedPostDto): SimulatedPostResponseDto
}

@Serializable
data class SimulatedPostDto(val title: String, val body: String, val userId: Int)

@Serializable
data class SimulatedPostResponseDto(val id: Int)
