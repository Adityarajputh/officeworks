package moc.itivo.officeworks.catalogue.domain

import moc.itivo.officeworks.catalogue.domain.model.Category
import moc.itivo.officeworks.catalogue.domain.model.Product
import moc.itivo.officeworks.core.result.DataResult

/**
 * The only way the app reads product data. Today it is backed by DummyJSON
 * ([moc.itivo.officeworks.catalogue.data.DummyJsonProductRepository]); pointing the app at our
 * own backend means writing one new implementation and changing one binding.
 */
interface ProductRepository {
    suspend fun getTopRatedProducts(limit: Int): DataResult<List<Product>>
    suspend fun getCategories(): DataResult<List<Category>>
    suspend fun getProductsInCategory(categorySlug: String): DataResult<List<Product>>
    suspend fun searchProducts(query: String): DataResult<List<Product>>
    suspend fun getProduct(id: Int): DataResult<Product>
}
