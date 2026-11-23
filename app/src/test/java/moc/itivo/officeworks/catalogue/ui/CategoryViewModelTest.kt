package moc.itivo.officeworks.catalogue.ui

import moc.itivo.officeworks.catalogue.domain.GetCategoryProductsUseCase
import moc.itivo.officeworks.catalogue.ui.category.CategoryIntent
import moc.itivo.officeworks.catalogue.ui.category.CategoryViewModel
import moc.itivo.officeworks.core.result.DataError
import moc.itivo.officeworks.core.result.DataResult
import moc.itivo.officeworks.navigation.Destination
import moc.itivo.officeworks.testutil.FakeProductRepository
import moc.itivo.officeworks.testutil.MainDispatcherRule
import moc.itivo.officeworks.testutil.product
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNull
import org.junit.Rule
import org.junit.Test

class CategoryViewModelTest {

    @get:Rule
    val mainDispatcherRule = MainDispatcherRule()

    private val repository = FakeProductRepository()
    private fun viewModel() = CategoryViewModel(
        Destination.Category(slug = "smartphones", name = "Smartphones"),
        GetCategoryProductsUseCase(repository),
    )

    @Test
    fun `loads the category from the route and splits top rated from the rest`() {
        repository.categoryProductsResult = DataResult.Success(
            listOf(product(1, rating = 1.0), product(2, rating = 5.0), product(3, rating = 4.0), product(4, rating = 3.0)),
        )

        val state = viewModel().state.value

        assertEquals(listOf("smartphones"), repository.requestedCategories)
        assertEquals("Smartphones", state.categoryName)
        assertEquals(listOf(2, 3, 4), state.topRated.map { it.id })
        assertEquals(listOf(1), state.others.map { it.id })
    }

    @Test
    fun `shows an error and retries`() {
        repository.categoryProductsResult = DataResult.Failure(DataError.NETWORK)
        val viewModel = viewModel()
        assertEquals(DataError.NETWORK, viewModel.state.value.error)

        repository.categoryProductsResult = DataResult.Success(listOf(product(1)))
        viewModel.onIntent(CategoryIntent.Retry)

        assertNull(viewModel.state.value.error)
        assertEquals(2, repository.requestedCategories.size)
    }
}
