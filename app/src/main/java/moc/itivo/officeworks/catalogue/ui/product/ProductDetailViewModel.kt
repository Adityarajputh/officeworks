package moc.itivo.officeworks.catalogue.ui.product

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
 * Always fetches the product by id instead of reusing the list item, so the details page
 * works the same from home, category, search or (later) a deep link.
 */
@HiltViewModel(assistedFactory = ProductDetailViewModel.Factory::class)
class ProductDetailViewModel @AssistedInject constructor(
    @Assisted private val route: Destination.Product,
    private val repository: ProductRepository,
) : MviViewModel<ProductDetailState, ProductDetailIntent>(ProductDetailState()) {

    @AssistedFactory
    interface Factory {
        fun create(route: Destination.Product): ProductDetailViewModel
    }

    private var loadJob: Job? = null

    init {
        load()
    }

    override fun onIntent(intent: ProductDetailIntent) {
        when (intent) {
            ProductDetailIntent.Retry -> load()
        }
    }

    private fun load() {
        loadJob?.cancel()
        setState { copy(isLoading = true, error = null) }
        loadJob = viewModelScope.launch {
            when (val result = repository.getProduct(route.productId)) {
                is DataResult.Success -> setState { copy(isLoading = false, product = result.data) }
                is DataResult.Failure -> setState { copy(isLoading = false, error = result.error) }
            }
        }
    }
}
