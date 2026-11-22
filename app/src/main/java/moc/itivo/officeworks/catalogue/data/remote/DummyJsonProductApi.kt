package moc.itivo.officeworks.catalogue.data.remote

import retrofit2.http.GET
import retrofit2.http.Path
import retrofit2.http.Query

/**
 * DummyJSON product endpoints (https://dummyjson.com/docs/products).
 * `limit=0` asks DummyJSON for every match instead of its default page of 30.
 */
interface DummyJsonProductApi {

    @GET("products")
    suspend fun getProducts(
        @Query("sortBy") sortBy: String,
        @Query("order") order: String,
        @Query("limit") limit: Int,
        @Query("select") select: String = LIST_FIELDS,
    ): ProductListDto

    @GET("products/categories")
    suspend fun getCategories(): List<CategoryDto>

    @GET("products/category/{slug}")
    suspend fun getProductsInCategory(
        @Path("slug") slug: String,
        @Query("limit") limit: Int = 0,
        @Query("select") select: String = LIST_FIELDS,
    ): ProductListDto

    @GET("products/search")
    suspend fun searchProducts(
        @Query("q") query: String,
        @Query("limit") limit: Int = 0,
        @Query("select") select: String = LIST_FIELDS,
    ): ProductListDto

    @GET("products/{id}")
    suspend fun getProduct(@Path("id") id: Int): ProductDto

    companion object {
        /** Only the fields list screens need; skips reviews, dimensions, etc. */
        const val LIST_FIELDS = "title,price,description,category,brand,rating,thumbnail,images,availabilityStatus"
    }
}
