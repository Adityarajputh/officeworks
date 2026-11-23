package moc.itivo.officeworks.catalogue.ui.product

import moc.itivo.officeworks.catalogue.domain.model.Product
import moc.itivo.officeworks.core.result.DataError

data class ProductDetailState(
    val isLoading: Boolean = true,
    val product: Product? = null,
    val error: DataError? = null,
)

sealed interface ProductDetailIntent {
    data object Retry : ProductDetailIntent
}
