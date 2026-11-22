package moc.itivo.officeworks.enquiry.ui

import androidx.lifecycle.viewModelScope
import dagger.assisted.Assisted
import dagger.assisted.AssistedFactory
import dagger.assisted.AssistedInject
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.launch
import moc.itivo.officeworks.core.mvi.MviViewModel
import moc.itivo.officeworks.enquiry.domain.SubmitEnquiryResult
import moc.itivo.officeworks.enquiry.domain.SubmitEnquiryUseCase
import moc.itivo.officeworks.navigation.Destination

@HiltViewModel(assistedFactory = EnquiryViewModel.Factory::class)
class EnquiryViewModel @AssistedInject constructor(
    @Assisted private val route: Destination.Enquiry,
    private val submitEnquiry: SubmitEnquiryUseCase,
) : MviViewModel<EnquiryState, EnquiryIntent, EnquiryEffect>(EnquiryState(productTitle = route.productTitle)) {

    @AssistedFactory
    interface Factory {
        fun create(route: Destination.Enquiry): EnquiryViewModel
    }

    override fun onIntent(intent: EnquiryIntent) {
        when (intent) {
            // Editing a field clears that field's error, so stale messages do not linger.
            is EnquiryIntent.NameChanged -> setState {
                copy(form = form.copy(name = intent.value), errors = errors.copy(name = null))
            }
            is EnquiryIntent.EmailChanged -> setState {
                copy(form = form.copy(email = intent.value), errors = errors.copy(email = null))
            }
            is EnquiryIntent.PhoneChanged -> setState {
                copy(form = form.copy(phone = intent.value), errors = errors.copy(phone = null))
            }
            is EnquiryIntent.MessageChanged -> setState {
                copy(form = form.copy(message = intent.value), errors = errors.copy(message = null))
            }
            EnquiryIntent.Submit -> submit()
        }
    }

    private fun submit() {
        if (currentState.isSubmitting || currentState.isSent) return // No double sends.
        setState { copy(isSubmitting = true) }
        viewModelScope.launch {
            when (val result = submitEnquiry(route.productId, route.productTitle, currentState.form)) {
                SubmitEnquiryResult.Sent -> setState { copy(isSubmitting = false, isSent = true) }
                is SubmitEnquiryResult.Invalid -> setState { copy(isSubmitting = false, errors = result.validation) }
                is SubmitEnquiryResult.Failed -> {
                    setState { copy(isSubmitting = false) }
                    sendEffect(EnquiryEffect.SubmitFailed(result.error))
                }
            }
        }
    }
}
