package moc.itivo.officeworks.testutil

import kotlinx.coroutines.CompletableDeferred
import moc.itivo.officeworks.catalogue.domain.ProductRepository
import moc.itivo.officeworks.catalogue.domain.model.Category
import moc.itivo.officeworks.catalogue.domain.model.Product
import moc.itivo.officeworks.core.result.DataError
import moc.itivo.officeworks.core.result.DataResult

/** In-memory [ProductRepository] that records what it was asked for. */
class FakeProductRepository : ProductRepository {
    var topRatedResult: DataResult<List<Product>> = DataResult.Success(emptyList())
    var categoriesResult: DataResult<List<Category>> = DataResult.Success(emptyList())
    var categoryProductsResult: DataResult<List<Product>> = DataResult.Success(emptyList())
    var productResult: DataResult<Product> = DataResult.Failure(DataError.NOT_FOUND)
    var searchResult: (String) -> DataResult<List<Product>> = { DataResult.Success(emptyList()) }

    /** When set, search calls for that query suspend until the deferred completes. */
    val searchGates = mutableMapOf<String, CompletableDeferred<Unit>>()

    val requestedTopRatedLimits = mutableListOf<Int>()
    val requestedCategories = mutableListOf<String>()
    val requestedProductIds = mutableListOf<Int>()
    val searchQueries = mutableListOf<String>()
    var categoriesCalls = 0

    override suspend fun getTopRatedProducts(limit: Int): DataResult<List<Product>> {
        requestedTopRatedLimits += limit
        return topRatedResult
    }

    override suspend fun getCategories(): DataResult<List<Category>> {
        categoriesCalls++
        return categoriesResult
    }

    override suspend fun getProductsInCategory(categorySlug: String): DataResult<List<Product>> {
        requestedCategories += categorySlug
        return categoryProductsResult
    }

    override suspend fun searchProducts(query: String): DataResult<List<Product>> {
        searchQueries += query
        searchGates[query]?.await()
        return searchResult(query)
    }

    override suspend fun getProduct(id: Int): DataResult<Product> {
        requestedProductIds += id
        return productResult
    }
}
