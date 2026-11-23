package moc.itivo.officeworks.catalogue.ui.category

import androidx.lifecycle.viewModelScope
import dagger.assisted.Assisted
import dagger.assisted.AssistedFactory
import dagger.assisted.AssistedInject
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.Job
import kotlinx.coroutines.launch
import moc.itivo.officeworks.catalogue.domain.GetCategoryProductsUseCase
import moc.itivo.officeworks.core.mvi.MviViewModel
import moc.itivo.officeworks.core.result.DataResult
import moc.itivo.officeworks.navigation.Destination

@HiltViewModel(assistedFactory = CategoryViewModel.Factory::class)
class CategoryViewModel @AssistedInject constructor(
    @Assisted private val route: Destination.Category,
    private val getCategoryProducts: GetCategoryProductsUseCase,
) : MviViewModel<CategoryState, CategoryIntent>(CategoryState(categoryName = route.name)) {

    @AssistedFactory
    interface Factory {
        fun create(route: Destination.Category): CategoryViewModel
    }

    private var loadJob: Job? = null

    init {
        load()
    }

    override fun onIntent(intent: CategoryIntent) {
        when (intent) {
            CategoryIntent.Retry -> load()
        }
    }

    private fun load() {
        loadJob?.cancel()
        setState { copy(isLoading = true, error = null) }
        loadJob = viewModelScope.launch {
            when (val result = getCategoryProducts(route.slug)) {
                is DataResult.Success -> setState {
                    copy(isLoading = false, topRated = result.data.topRated, others = result.data.others)
                }
                is DataResult.Failure -> setState { copy(isLoading = false, error = result.error) }
            }
        }
    }
}
