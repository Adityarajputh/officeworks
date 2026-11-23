package moc.itivo.officeworks.catalogue.domain

import kotlinx.coroutines.test.runTest
import moc.itivo.officeworks.core.result.DataResult
import moc.itivo.officeworks.testutil.FakeProductRepository
import moc.itivo.officeworks.testutil.product
import org.junit.Assert.assertEquals
import org.junit.Test

class TopRatedFeaturedProductsProviderTest {

    @Test
    fun `features the five top rated products`() = runTest {
        val repository = FakeProductRepository()
        val topFive = (1..5).map { product(it) }
        repository.topRatedResult = DataResult.Success(topFive)

        val result = TopRatedFeaturedProductsProvider(repository).getFeaturedProducts()

        assertEquals(listOf(5), repository.requestedTopRatedLimits)
        assertEquals(DataResult.Success(topFive), result)
    }
}
