package moc.itivo.officeworks.catalogue.domain

import kotlinx.coroutines.test.runTest
import moc.itivo.officeworks.core.result.DataError
import moc.itivo.officeworks.core.result.DataResult
import moc.itivo.officeworks.testutil.FakeProductRepository
import moc.itivo.officeworks.testutil.product
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test

class GetCategoryProductsUseCaseTest {

    private val repository = FakeProductRepository()
    private val useCase = GetCategoryProductsUseCase(repository)

    @Test
    fun `top rated are the three highest rated, the rest follow without duplicates`() = runTest {
        repository.categoryProductsResult = DataResult.Success(
            listOf(
                product(1, rating = 3.0),
                product(2, rating = 4.9),
                product(3, rating = 2.0),
                product(4, rating = 4.5),
                product(5, rating = 4.7),
            ),
        )

        val result = (useCase("smartphones") as DataResult.Success).data

        assertEquals(listOf(2, 5, 4), result.topRated.map { it.id })
        assertEquals(listOf(1, 3), result.others.map { it.id })
        assertTrue(result.topRated.none { it in result.others })
        assertEquals(listOf("smartphones"), repository.requestedCategories)
    }

    @Test
    fun `a category with fewer than three products is all top rated`() = runTest {
        repository.categoryProductsResult = DataResult.Success(listOf(product(1, rating = 3.0), product(2, rating = 4.0)))

        val result = (useCase("tiny") as DataResult.Success).data

        assertEquals(listOf(2, 1), result.topRated.map { it.id })
        assertTrue(result.others.isEmpty())
    }

    @Test
    fun `failures pass through unchanged`() = runTest {
        repository.categoryProductsResult = DataResult.Failure(DataError.NETWORK)

        assertEquals(DataResult.Failure(DataError.NETWORK), useCase("smartphones"))
    }
}
