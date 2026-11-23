package moc.itivo.officeworks.enquiry.ui

import androidx.annotation.StringRes
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.imePadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Button
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberUpdatedState
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.semantics.heading
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.input.KeyboardCapitalization
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.hilt.lifecycle.viewmodel.compose.hiltViewModel
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.compose.LocalLifecycleOwner
import androidx.lifecycle.repeatOnLifecycle
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import moc.itivo.officeworks.R
import moc.itivo.officeworks.core.result.DataError
import moc.itivo.officeworks.core.ui.BackTopAppBar
import moc.itivo.officeworks.core.ui.messageRes
import moc.itivo.officeworks.enquiry.domain.FieldError
import moc.itivo.officeworks.navigation.Destination

@Composable
fun EnquiryRoute(
    route: Destination.Enquiry,
    onBack: () -> Unit,
    onContinueBrowsing: () -> Unit,
    viewModel: EnquiryViewModel = hiltViewModel<EnquiryViewModel, EnquiryViewModel.Factory> { it.create(route) },
) {
    val state by viewModel.state.collectAsStateWithLifecycle()
    EnquiryScreen(state, viewModel::onIntent, onBack, onContinueBrowsing)
}

@Composable
fun EnquiryScreen(
    state: EnquiryState,
    onIntent: (EnquiryIntent) -> Unit,
    onBack: () -> Unit,
    onContinueBrowsing: () -> Unit,
) {
    val snackbarHostState = remember { SnackbarHostState() }
    SubmitErrorSnackbar(state.submitError, snackbarHostState, onShown = { onIntent(EnquiryIntent.SubmitErrorShown) })

    Scaffold(
        topBar = { BackTopAppBar(title = stringResource(R.string.enquiry_title), onBack = onBack) },
        snackbarHost = { SnackbarHost(snackbarHostState) },
    ) { padding ->
        val modifier = Modifier
            .padding(padding)
            .fillMaxSize()
        if (state.isSent) {
            ThankYou(state.productTitle, onContinueBrowsing, modifier)
        } else {
            EnquiryFormContent(state, onIntent, modifier)
        }
    }
}

/**
 * Shows [error] once, then reports it as shown. Showing only runs while the screen is at least
 * STARTED: if the app goes to the background or rotates mid-snackbar, the error is still in the
 * state and is shown again when the customer comes back, instead of being lost.
 */
@Composable
private fun SubmitErrorSnackbar(error: DataError?, snackbarHostState: SnackbarHostState, onShown: () -> Unit) {
    if (error == null) return
    val message = stringResource(error.messageRes())
    val lifecycle = LocalLifecycleOwner.current.lifecycle
    val currentOnShown by rememberUpdatedState(onShown)
    LaunchedEffect(error, message, lifecycle) {
        lifecycle.repeatOnLifecycle(Lifecycle.State.STARTED) {
            snackbarHostState.showSnackbar(message)
            currentOnShown()
        }
    }
}

@Composable
private fun EnquiryFormContent(state: EnquiryState, onIntent: (EnquiryIntent) -> Unit, modifier: Modifier) {
    val enabled = !state.isSubmitting
    Column(
        modifier = modifier
            .verticalScroll(rememberScrollState())
            .imePadding()
            .padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp),
    ) {
        Text(stringResource(R.string.enquiry_about, state.productTitle), style = MaterialTheme.typography.titleMedium)
        Text(stringResource(R.string.enquiry_intro), style = MaterialTheme.typography.bodyMedium)

        FormField(
            value = state.form.name,
            onValueChange = { onIntent(EnquiryIntent.NameChanged(it)) },
            label = stringResource(R.string.enquiry_name),
            error = state.errors.name,
            enabled = enabled,
            keyboardOptions = KeyboardOptions(capitalization = KeyboardCapitalization.Words, imeAction = ImeAction.Next),
            testTag = "enquiry_name",
        )
        FormField(
            value = state.form.email,
            onValueChange = { onIntent(EnquiryIntent.EmailChanged(it)) },
            label = stringResource(R.string.enquiry_email),
            error = state.errors.email,
            enabled = enabled,
            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Email, imeAction = ImeAction.Next),
            testTag = "enquiry_email",
        )
        FormField(
            value = state.form.phone,
            onValueChange = { onIntent(EnquiryIntent.PhoneChanged(it)) },
            label = stringResource(R.string.enquiry_phone),
            error = state.errors.phone,
            enabled = enabled,
            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Phone, imeAction = ImeAction.Next),
            testTag = "enquiry_phone",
        )
        FormField(
            value = state.form.message,
            onValueChange = { onIntent(EnquiryIntent.MessageChanged(it)) },
            label = stringResource(R.string.enquiry_message),
            error = state.errors.message,
            enabled = enabled,
            keyboardOptions = KeyboardOptions(capitalization = KeyboardCapitalization.Sentences),
            singleLine = false,
            testTag = "enquiry_message",
        )
        Text(stringResource(R.string.enquiry_required_note), style = MaterialTheme.typography.bodySmall)

        Button(
            onClick = { onIntent(EnquiryIntent.Submit) },
            enabled = enabled,
            modifier = Modifier
                .fillMaxWidth()
                .testTag("enquiry_submit"),
        ) {
            if (state.isSubmitting) {
                CircularProgressIndicator(Modifier.size(20.dp), strokeWidth = 2.dp)
            } else {
                Text(stringResource(R.string.enquiry_submit))
            }
        }
    }
}

@Composable
private fun FormField(
    value: String,
    onValueChange: (String) -> Unit,
    label: String,
    error: FieldError?,
    enabled: Boolean,
    keyboardOptions: KeyboardOptions,
    testTag: String,
    singleLine: Boolean = true,
) {
    OutlinedTextField(
        value = value,
        onValueChange = onValueChange,
        label = { Text(label) },
        isError = error != null,
        supportingText = error?.let { { Text(stringResource(it.messageRes())) } },
        enabled = enabled,
        singleLine = singleLine,
        minLines = if (singleLine) 1 else 4,
        keyboardOptions = keyboardOptions,
        modifier = Modifier
            .fillMaxWidth()
            .testTag(testTag),
    )
}

@StringRes
private fun FieldError.messageRes(): Int = when (this) {
    FieldError.REQUIRED -> R.string.enquiry_error_required
    FieldError.INVALID_EMAIL -> R.string.enquiry_error_email
    FieldError.INVALID_PHONE -> R.string.enquiry_error_phone
    FieldError.TOO_LONG -> R.string.enquiry_error_too_long
}

@Composable
private fun ThankYou(productTitle: String, onContinueBrowsing: () -> Unit, modifier: Modifier) {
    Column(
        modifier = modifier.padding(24.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp, Alignment.CenterVertically),
        horizontalAlignment = Alignment.CenterHorizontally,
    ) {
        Text(
            stringResource(R.string.enquiry_thank_you_title),
            style = MaterialTheme.typography.headlineMedium,
            modifier = Modifier.semantics { heading() },
        )
        Text(
            stringResource(R.string.enquiry_thank_you_body, productTitle),
            style = MaterialTheme.typography.bodyLarge,
            textAlign = TextAlign.Center,
        )
        Button(onClick = onContinueBrowsing) { Text(stringResource(R.string.enquiry_continue)) }
    }
}
