package moc.itivo.officeworks.catalogue.ui.category

import moc.itivo.officeworks.catalogue.domain.model.Product
import moc.itivo.officeworks.core.result.DataError

data class CategoryState(
    val categoryName: String,
    val isLoading: Boolean = true,
    val topRated: List<Product> = emptyList(),
    val others: List<Product> = emptyList(),
    val error: DataError? = null,
)

sealed interface CategoryIntent {
    data object Retry : CategoryIntent
}
