package moc.itivo.officeworks.enquiry.domain

import kotlinx.coroutines.test.runTest
import moc.itivo.officeworks.core.result.DataError
import moc.itivo.officeworks.core.result.DataResult
import moc.itivo.officeworks.enquiry.FakeEnquiryGateway
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test

class SubmitEnquiryUseCaseTest {

    private val gateway = FakeEnquiryGateway()
    private val submit = SubmitEnquiryUseCase(EnquiryValidator(), gateway)

    @Test
    fun `an invalid form is never sent`() = runTest {
        val result = submit(1, "Phone", EnquiryForm(name = "", email = "nope"))

        assertTrue(result is SubmitEnquiryResult.Invalid)
        assertTrue(gateway.sent.isEmpty())
    }

    @Test
    fun `sends a trimmed enquiry with blank optional fields as null`() = runTest {
        val result = submit(7, "Desk Lamp", EnquiryForm(name = "  Sam ", email = " sam@example.com ", phone = "  ", message = " "))

        assertEquals(SubmitEnquiryResult.Sent, result)
        assertEquals(
            Enquiry(
                productId = 7,
                productTitle = "Desk Lamp",
                customerName = "Sam",
                customerEmail = "sam@example.com",
                customerPhone = null,
                message = null,
            ),
            gateway.sent.single(),
        )
    }

    @Test
    fun `reports delivery failures`() = runTest {
        gateway.result = DataResult.Failure(DataError.NETWORK)

        val result = submit(1, "Phone", EnquiryForm(name = "Sam", email = "sam@example.com"))

        assertEquals(SubmitEnquiryResult.Failed(DataError.NETWORK), result)
    }
}
