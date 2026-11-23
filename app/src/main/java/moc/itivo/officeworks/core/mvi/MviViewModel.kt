package moc.itivo.officeworks.core.mvi

import androidx.lifecycle.ViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update

/**
 * Small MVI base: the screen renders [state] and sends user actions through [onIntent].
 *
 * Data only flows one way: Intent -> ViewModel -> new State -> UI.
 *
 * There is deliberately no separate event channel. Something the UI must show once (e.g. a
 * snackbar) is a field in the state, and the screen sends an intent once it has been shown.
 * This follows the Android guidance on ViewModel events: a state survives rotation and
 * backgrounding, whereas an event sent through a Channel can be consumed and then lost.
 */
abstract class MviViewModel<S, I>(initialState: S) : ViewModel() {

    private val _state = MutableStateFlow(initialState)
    val state: StateFlow<S> = _state.asStateFlow()

    protected val currentState: S get() = _state.value

    abstract fun onIntent(intent: I)

    protected fun setState(reduce: S.() -> S) = _state.update(reduce)
}
