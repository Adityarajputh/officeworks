package moc.itivo.officeworks.catalogue.ui.search

import androidx.lifecycle.viewModelScope
import dagger.assisted.Assisted
import dagger.assisted.AssistedFactory
import dagger.assisted.AssistedInject
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.Job
import kotlinx.coroutines.launch
import moc.itivo.officeworks.catalogue.domain.ProductRepository
import moc.itivo.officeworks.core.mvi.MviViewModel
import moc.itivo.officeworks.core.result.DataResult
import moc.itivo.officeworks.navigation.Destination

/**
 * Searches only when the user presses Search (per the acceptance criteria), not on every
 * keystroke. Matching is done by the data source; DummyJSON matches title and description.
 */
@HiltViewModel(assistedFactory = SearchViewModel.Factory::class)
class SearchViewModel @AssistedInject constructor(
    @Assisted route: Destination.Search,
    private val repository: ProductRepository,
) : MviViewModel<SearchState, SearchIntent>(SearchState(query = route.query)) {

    @AssistedFactory
    interface Factory {
        fun create(route: Destination.Search): SearchViewModel
    }

    private var searchJob: Job? = null

    init {
        search(currentState.query)
    }

    override fun onIntent(intent: SearchIntent) {
        when (intent) {
            is SearchIntent.QueryChanged -> setState { copy(query = intent.query) }
            SearchIntent.Submit -> search(currentState.query)
            SearchIntent.Retry -> currentState.submittedQuery?.let(::search)
        }
    }

    private fun search(rawQuery: String) {
        val term = rawQuery.trim()
        if (term.isEmpty()) return

        // A newer search always wins; results from an older one must not overwrite it.
        searchJob?.cancel()
        setState { copy(submittedQuery = term, isLoading = true, error = null) }
        searchJob = viewModelScope.launch {
            when (val result = repository.searchProducts(term)) {
                is DataResult.Success -> setState { copy(isLoading = false, results = result.data) }
                is DataResult.Failure -> setState { copy(isLoading = false, results = emptyList(), error = result.error) }
            }
        }
    }
}
