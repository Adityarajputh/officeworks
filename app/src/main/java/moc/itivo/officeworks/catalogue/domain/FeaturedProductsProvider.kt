package moc.itivo.officeworks.catalogue.domain

import moc.itivo.officeworks.catalogue.domain.model.Product
import moc.itivo.officeworks.core.result.DataResult
import javax.inject.Inject

/**
 * Decides which products are featured on the home page.
 *
 * The brief says the owner has "chosen 5 products". There is no admin tool yet, so
 * [TopRatedFeaturedProductsProvider] stands in by taking the 5 highest rated. When the owner
 * can hand-pick products, add a provider that reads their list and change the binding;
 * the home screen does not change.
 */
fun interface FeaturedProductsProvider {
    suspend fun getFeaturedProducts(): DataResult<List<Product>>
}

class TopRatedFeaturedProductsProvider @Inject constructor(
    private val repository: ProductRepository,
) : FeaturedProductsProvider {

    override suspend fun getFeaturedProducts(): DataResult<List<Product>> =
        repository.getTopRatedProducts(limit = FEATURED_COUNT)

    companion object {
        const val FEATURED_COUNT = 5
    }
}
