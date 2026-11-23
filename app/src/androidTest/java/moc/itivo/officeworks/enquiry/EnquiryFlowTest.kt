package moc.itivo.officeworks.enquiry

import androidx.compose.ui.test.assertIsDisplayed
import androidx.compose.ui.test.junit4.createComposeRule
import androidx.compose.ui.test.onNodeWithTag
import androidx.compose.ui.test.onNodeWithText
import androidx.compose.ui.test.performClick
import androidx.compose.ui.test.performScrollTo
import androidx.compose.ui.test.performTextInput
import androidx.test.ext.junit.runners.AndroidJUnit4
import moc.itivo.officeworks.core.result.DataError
import moc.itivo.officeworks.core.result.DataResult
import moc.itivo.officeworks.enquiry.domain.Enquiry
import moc.itivo.officeworks.enquiry.domain.EnquiryGateway
import moc.itivo.officeworks.enquiry.domain.EnquiryValidator
import moc.itivo.officeworks.enquiry.domain.SubmitEnquiryUseCase
import moc.itivo.officeworks.enquiry.ui.EnquiryRoute
import moc.itivo.officeworks.enquiry.ui.EnquiryViewModel
import moc.itivo.officeworks.navigation.Destination
import moc.itivo.officeworks.ui.theme.OfficeworksTheme
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith

/**
 * The enquiry acceptance criterion end to end on a device: real screen, real ViewModel and
 * validation, with only the delivery gateway faked.
 */
@RunWith(AndroidJUnit4::class)
class EnquiryFlowTest {

    @get:Rule
    val composeRule = createComposeRule()

    private class RecordingGateway(private val result: DataResult<Unit>) : EnquiryGateway {
        val sent = mutableListOf<Enquiry>()
        override suspend fun submit(enquiry: Enquiry): DataResult<Unit> {
            sent += enquiry
            return result
        }
    }

    private fun launch(gateway: EnquiryGateway) {
        val route = Destination.Enquiry(productId = 7, productTitle = "Desk Lamp")
        val viewModel = EnquiryViewModel(route, SubmitEnquiryUseCase(EnquiryValidator(), gateway))
        composeRule.setContent {
            OfficeworksTheme {
                EnquiryRoute(
                    route = route,
                    onBack = {},
                    onContinueBrowsing = {},
                    viewModel = viewModel,
                )
            }
        }
    }

    @Test
    fun completingTheFormSendsTheEnquiryAndThanksTheCustomer() {
        val gateway = RecordingGateway(DataResult.Success(Unit))
        launch(gateway)

        composeRule.onNodeWithTag("enquiry_name").performTextInput("Sam Lee")
        composeRule.onNodeWithTag("enquiry_email").performTextInput("sam@example.com")
        composeRule.onNodeWithTag("enquiry_message").performTextInput("Is it available in blue?")
        composeRule.onNodeWithTag("enquiry_submit").performScrollTo().performClick()

        composeRule.onNodeWithText("Thank you").assertIsDisplayed()
        val sent = gateway.sent.single()
        assertEquals(7, sent.productId)
        assertEquals("sam@example.com", sent.customerEmail)
    }

    @Test
    fun anInvalidEmailIsFlaggedAndNothingIsSent() {
        val gateway = RecordingGateway(DataResult.Success(Unit))
        launch(gateway)

        composeRule.onNodeWithTag("enquiry_name").performTextInput("Sam Lee")
        composeRule.onNodeWithTag("enquiry_email").performTextInput("sam@")
        composeRule.onNodeWithTag("enquiry_submit").performScrollTo().performClick()

        composeRule.onNodeWithText("Enter a valid email address, like name@example.com.").assertIsDisplayed()
        assertTrue(gateway.sent.isEmpty())
    }

    @Test
    fun aDeliveryFailureKeepsTheFormAndExplainsWhy() {
        launch(RecordingGateway(DataResult.Failure(DataError.NETWORK)))

        composeRule.onNodeWithTag("enquiry_name").performTextInput("Sam Lee")
        composeRule.onNodeWithTag("enquiry_email").performTextInput("sam@example.com")
        composeRule.onNodeWithTag("enquiry_submit").performScrollTo().performClick()

        composeRule.onNodeWithText("Can't reach the catalogue. Check your connection and try again.").assertIsDisplayed()
        composeRule.onNodeWithText("Sam Lee").assertIsDisplayed()
    }
}
