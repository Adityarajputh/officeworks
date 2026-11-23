package moc.itivo.officeworks.catalogue.ui.home

import androidx.lifecycle.viewModelScope
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.Job
import kotlinx.coroutines.async
import kotlinx.coroutines.launch
import moc.itivo.officeworks.catalogue.domain.FeaturedProductsProvider
import moc.itivo.officeworks.catalogue.domain.ProductRepository
import moc.itivo.officeworks.core.mvi.MviViewModel
import moc.itivo.officeworks.core.result.DataResult
import moc.itivo.officeworks.core.result.errorOrNull
import javax.inject.Inject

@HiltViewModel
class HomeViewModel @Inject constructor(
    private val featuredProducts: FeaturedProductsProvider,
    private val repository: ProductRepository,
) : MviViewModel<HomeState, HomeIntent>(HomeState()) {

    private var loadJob: Job? = null

    init {
        load()
    }

    override fun onIntent(intent: HomeIntent) {
        when (intent) {
            HomeIntent.Retry -> load()
        }
    }

    private fun load() {
        loadJob?.cancel()
        setState { copy(isLoading = true, error = null) }
        loadJob = viewModelScope.launch {
            // Both requests run at the same time; the page needs both to be useful.
            val featuredCall = async { featuredProducts.getFeaturedProducts() }
            val categoriesCall = async { repository.getCategories() }
            val featured = featuredCall.await()
            val categories = categoriesCall.await()

            setState {
                if (featured is DataResult.Success && categories is DataResult.Success) {
                    copy(isLoading = false, featured = featured.data, categories = categories.data)
                } else {
                    copy(isLoading = false, error = featured.errorOrNull() ?: categories.errorOrNull())
                }
            }
        }
    }
}
