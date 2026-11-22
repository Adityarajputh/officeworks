package moc.itivo.officeworks.catalogue.data

import moc.itivo.officeworks.catalogue.data.remote.DummyJsonProductApi
import moc.itivo.officeworks.catalogue.data.remote.toDomain
import moc.itivo.officeworks.catalogue.domain.ProductRepository
import moc.itivo.officeworks.catalogue.domain.model.Category
import moc.itivo.officeworks.catalogue.domain.model.Product
import moc.itivo.officeworks.core.network.safeApiCall
import moc.itivo.officeworks.core.result.DataResult
import javax.inject.Inject

/**
 * [ProductRepository] backed by DummyJSON. Retrofit's suspend functions are main-safe, so no
 * dispatcher switching is needed here.
 */
class DummyJsonProductRepository @Inject constructor(
    private val api: DummyJsonProductApi,
) : ProductRepository {

    override suspend fun getTopRatedProducts(limit: Int): DataResult<List<Product>> = safeApiCall {
        api.getProducts(sortBy = "rating", order = "desc", limit = limit).products.map { it.toDomain() }
    }

    override suspend fun getCategories(): DataResult<List<Category>> = safeApiCall {
        api.getCategories().map { it.toDomain() }
    }

    override suspend fun getProductsInCategory(categorySlug: String): DataResult<List<Product>> = safeApiCall {
        api.getProductsInCategory(categorySlug).products.map { it.toDomain() }
    }

    override suspend fun searchProducts(query: String): DataResult<List<Product>> = safeApiCall {
        api.searchProducts(query).products.map { it.toDomain() }
    }

    override suspend fun getProduct(id: Int): DataResult<Product> = safeApiCall {
        api.getProduct(id).toDomain()
    }
}
