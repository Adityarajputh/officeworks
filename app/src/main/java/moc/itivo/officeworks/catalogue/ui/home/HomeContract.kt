package moc.itivo.officeworks.catalogue.ui.home

import moc.itivo.officeworks.catalogue.domain.model.Category
import moc.itivo.officeworks.catalogue.domain.model.Product
import moc.itivo.officeworks.core.result.DataError

data class HomeState(
    val isLoading: Boolean = true,
    val featured: List<Product> = emptyList(),
    val categories: List<Category> = emptyList(),
    val error: DataError? = null,
)

sealed interface HomeIntent {
    data object Retry : HomeIntent
}
