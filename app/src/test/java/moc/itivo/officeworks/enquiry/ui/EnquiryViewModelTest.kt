package moc.itivo.officeworks.enquiry.ui

import kotlinx.coroutines.CompletableDeferred
import moc.itivo.officeworks.core.result.DataError
import moc.itivo.officeworks.core.result.DataResult
import moc.itivo.officeworks.enquiry.FakeEnquiryGateway
import moc.itivo.officeworks.enquiry.domain.EnquiryValidator
import moc.itivo.officeworks.enquiry.domain.FieldError
import moc.itivo.officeworks.enquiry.domain.SubmitEnquiryUseCase
import moc.itivo.officeworks.navigation.Destination
import moc.itivo.officeworks.testutil.MainDispatcherRule
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Rule
import org.junit.Test

class EnquiryViewModelTest {

    @get:Rule
    val mainDispatcherRule = MainDispatcherRule()

    private val gateway = FakeEnquiryGateway()
    private val viewModel = EnquiryViewModel(
        Destination.Enquiry(productId = 7, productTitle = "Desk Lamp"),
        SubmitEnquiryUseCase(EnquiryValidator(), gateway),
    )

    private fun fillValidForm() {
        viewModel.onIntent(EnquiryIntent.NameChanged("Sam Lee"))
        viewModel.onIntent(EnquiryIntent.EmailChanged("sam@example.com"))
    }

    @Test
    fun `shows the product being enquired about`() {
        assertEquals("Desk Lamp", viewModel.state.value.productTitle)
    }

    @Test
    fun `submitting an empty form shows field errors and sends nothing`() {
        viewModel.onIntent(EnquiryIntent.Submit)

        val state = viewModel.state.value
        assertEquals(FieldError.REQUIRED, state.errors.name)
        assertEquals(FieldError.REQUIRED, state.errors.email)
        assertFalse(state.isSent)
        assertTrue(gateway.sent.isEmpty())
    }

    @Test
    fun `editing a field clears only that field's error`() {
        viewModel.onIntent(EnquiryIntent.Submit)

        viewModel.onIntent(EnquiryIntent.NameChanged("S"))

        assertNull(viewModel.state.value.errors.name)
        assertEquals(FieldError.REQUIRED, viewModel.state.value.errors.email)
    }

    @Test
    fun `a valid enquiry is sent for this product and the customer sees thank you`() {
        fillValidForm()

        viewModel.onIntent(EnquiryIntent.Submit)

        assertTrue(viewModel.state.value.isSent)
        val sent = gateway.sent.single()
        assertEquals(7, sent.productId)
        assertEquals("Desk Lamp", sent.productTitle)
        assertEquals("sam@example.com", sent.customerEmail)
    }

    @Test
    fun `a failed send keeps the form and holds the error until it has been shown`() {
        gateway.result = DataResult.Failure(DataError.NETWORK)
        fillValidForm()

        viewModel.onIntent(EnquiryIntent.Submit)

        val state = viewModel.state.value
        assertEquals(DataError.NETWORK, state.submitError)
        assertFalse(state.isSent)
        assertFalse(state.isSubmitting)
        assertEquals("Sam Lee", state.form.name)

        viewModel.onIntent(EnquiryIntent.SubmitErrorShown)
        assertNull(viewModel.state.value.submitError)
    }

    @Test
    fun `trying again clears the old error and can succeed`() {
        gateway.result = DataResult.Failure(DataError.SERVER)
        fillValidForm()
        viewModel.onIntent(EnquiryIntent.Submit)
        assertEquals(DataError.SERVER, viewModel.state.value.submitError)

        gateway.result = DataResult.Success(Unit)
        viewModel.onIntent(EnquiryIntent.Submit)

        assertNull(viewModel.state.value.submitError)
        assertTrue(viewModel.state.value.isSent)
        assertEquals(2, gateway.sent.size)
    }

    @Test
    fun `tapping submit twice while sending sends once`() {
        val gate = CompletableDeferred<Unit>()
        gateway.gate = gate
        fillValidForm()

        viewModel.onIntent(EnquiryIntent.Submit)
        assertTrue(viewModel.state.value.isSubmitting)
        viewModel.onIntent(EnquiryIntent.Submit)
        gate.complete(Unit)

        assertEquals(1, gateway.sent.size)
        assertTrue(viewModel.state.value.isSent)
    }
}
