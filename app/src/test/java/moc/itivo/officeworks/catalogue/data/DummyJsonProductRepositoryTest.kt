package moc.itivo.officeworks.catalogue.data

import kotlinx.coroutines.test.runTest
import mockwebserver3.MockResponse
import mockwebserver3.MockWebServer
import moc.itivo.officeworks.core.result.DataError
import moc.itivo.officeworks.core.result.DataResult
import moc.itivo.officeworks.testutil.createApi
import moc.itivo.officeworks.testutil.jsonResponse
import org.junit.After
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Test

/**
 * Exercises the real Retrofit + kotlinx.serialization stack against a local server, so the
 * contract with DummyJSON (paths, query parameters, JSON shape, error codes) is pinned down.
 */
class DummyJsonProductRepositoryTest {

    private val server = MockWebServer()
    private lateinit var repository: DummyJsonProductRepository

    @Before
    fun setUp() {
        server.start()
        repository = DummyJsonProductRepository(server.createApi())
    }

    @After
    fun tearDown() {
        server.close()
    }

    @Test
    fun `top rated asks the API to sort by rating, highest first`() = runTest {
        server.enqueue(jsonResponse(fixture("products_list.json")))

        val result = repository.getTopRatedProducts(limit = 5)

        val url = server.takeRequest().url
        assertEquals("/products", url.encodedPath)
        assertEquals("rating", url.queryParameter("sortBy"))
        assertEquals("desc", url.queryParameter("order"))
        assertEquals("5", url.queryParameter("limit"))
        assertEquals(2, (result as DataResult.Success).data.size)
    }

    @Test
    fun `search sends the term and asks for every match`() = runTest {
        server.enqueue(jsonResponse(fixture("products_list.json")))

        val result = repository.searchProducts("phone case")

        val url = server.takeRequest().url
        assertEquals("/products/search", url.encodedPath)
        assertEquals("phone case", url.queryParameter("q"))
        assertEquals("0", url.queryParameter("limit"))
        assertTrue(result is DataResult.Success)
    }

    @Test
    fun `category slug goes in the path`() = runTest {
        server.enqueue(jsonResponse(fixture("products_list.json")))

        repository.getProductsInCategory("home-decoration")

        assertEquals("/products/category/home-decoration", server.takeRequest().url.encodedPath)
    }

    @Test
    fun `parses products, ignores unknown fields and fills defaults for missing ones`() = runTest {
        server.enqueue(jsonResponse(fixture("products_list.json")))

        val products = (repository.searchProducts("x") as DataResult.Success).data

        assertEquals("Realme XT", products[0].title)
        assertEquals("Realme", products[0].brand)
        assertEquals(2, products[0].imageUrls.size)
        // Second product only has id, title and price.
        assertEquals("Oppo Reno", products[1].title)
        assertEquals(null, products[1].brand)
        assertEquals(emptyList<String>(), products[1].imageUrls)
    }

    @Test
    fun `parses categories`() = runTest {
        server.enqueue(jsonResponse("""[{"slug":"beauty","name":"Beauty","url":"https://dummyjson.com/products/category/beauty"}]"""))

        val categories = (repository.getCategories() as DataResult.Success).data

        assertEquals("/products/categories", server.takeRequest().url.encodedPath)
        assertEquals("beauty", categories.single().slug)
    }

    @Test
    fun `404 becomes NOT_FOUND`() = runTest {
        server.enqueue(MockResponse.Builder().code(404).body("""{"message":"Product with id '999' not found"}""").build())

        assertEquals(DataResult.Failure(DataError.NOT_FOUND), repository.getProduct(999))
    }

    @Test
    fun `5xx becomes SERVER`() = runTest {
        server.enqueue(MockResponse.Builder().code(503).build())

        assertEquals(DataResult.Failure(DataError.SERVER), repository.getCategories())
    }

    @Test
    fun `malformed JSON becomes UNEXPECTED instead of crashing`() = runTest {
        server.enqueue(jsonResponse("""{"products": "not a list"}"""))

        assertEquals(DataResult.Failure(DataError.UNEXPECTED), repository.searchProducts("x"))
    }

    @Test
    fun `no connection becomes NETWORK`() = runTest {
        server.close()

        assertEquals(DataResult.Failure(DataError.NETWORK), repository.getTopRatedProducts(5))
    }

    private fun fixture(name: String): String =
        checkNotNull(javaClass.classLoader?.getResource("dummyjson/$name")) { "Missing fixture $name" }.readText()
}
