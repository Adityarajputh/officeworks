package moc.itivo.officeworks.catalogue.domain

import moc.itivo.officeworks.catalogue.domain.model.CategoryProducts
import moc.itivo.officeworks.catalogue.domain.model.Product
import moc.itivo.officeworks.core.result.DataResult
import moc.itivo.officeworks.core.result.map
import javax.inject.Inject

/**
 * Loads a category and splits it into "top rated" and "the rest".
 *
 * Sorting happens here rather than in the API query, so the rule still holds if the data
 * source changes to one that cannot sort by rating.
 */
class GetCategoryProductsUseCase @Inject constructor(
    private val repository: ProductRepository,
) {
    suspend operator fun invoke(categorySlug: String): DataResult<CategoryProducts> =
        repository.getProductsInCategory(categorySlug).map { splitByRating(it) }

    companion object {
        const val TOP_RATED_COUNT = 3

        fun splitByRating(products: List<Product>, topCount: Int = TOP_RATED_COUNT): CategoryProducts {
            val byRating = products.sortedByDescending { it.rating }
            return CategoryProducts(
                topRated = byRating.take(topCount),
                others = byRating.drop(topCount),
            )
        }
    }
}
