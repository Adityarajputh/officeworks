package moc.itivo.officeworks.enquiry.data

import kotlinx.coroutines.test.runTest
import kotlinx.serialization.json.jsonObject
import kotlinx.serialization.json.jsonPrimitive
import mockwebserver3.MockResponse
import mockwebserver3.MockWebServer
import moc.itivo.officeworks.core.network.NetworkModule
import moc.itivo.officeworks.core.result.DataError
import moc.itivo.officeworks.core.result.DataResult
import moc.itivo.officeworks.enquiry.domain.Enquiry
import moc.itivo.officeworks.testutil.createApi
import moc.itivo.officeworks.testutil.jsonResponse
import org.junit.After
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Before
import org.junit.Test

class DummyJsonEnquiryGatewayTest {

    private val server = MockWebServer()
    private val json = NetworkModule.provideJson()
    private lateinit var gateway: DummyJsonEnquiryGateway

    private val enquiry = Enquiry(
        productId = 7,
        productTitle = "Desk Lamp",
        customerName = "Sam Lee",
        customerEmail = "sam@example.com",
        customerPhone = null,
        message = "Do you have it in blue?",
    )

    @Before
    fun setUp() {
        server.start()
        gateway = DummyJsonEnquiryGateway(server.createApi(json))
    }

    @After
    fun tearDown() {
        server.close()
    }

    @Test
    fun `posts the enquiry to the simulated endpoint`() = runTest {
        server.enqueue(jsonResponse("""{"id":252}""", code = 201))

        val result = gateway.submit(enquiry)

        val request = server.takeRequest()
        assertEquals("POST", request.method)
        assertEquals("/posts/add", request.url.encodedPath)
        val body = json.parseToJsonElement(request.body!!.utf8()).jsonObject
        assertEquals("Product enquiry: Desk Lamp", body["title"]!!.jsonPrimitive.content)
        assertEquals(enquiry.toEmailBody(), body["body"]!!.jsonPrimitive.content)
        assertEquals(DataResult.Success(Unit), result)
    }

    @Test
    fun `server errors are reported, not swallowed`() = runTest {
        server.enqueue(MockResponse.Builder().code(500).build())

        assertEquals(DataResult.Failure(DataError.SERVER), gateway.submit(enquiry))
    }

    @Test
    fun `email body has the product and contact details and skips empty optional fields`() {
        val body = enquiry.toEmailBody()

        assertEquals(
            """
            Product: Desk Lamp (id 7)
            Name: Sam Lee
            Email: sam@example.com

            Do you have it in blue?
            """.trimIndent(),
            body,
        )
        assertFalse(body.contains("Phone"))
    }
}
