package moc.itivo.officeworks.catalogue.ui

import moc.itivo.officeworks.catalogue.domain.TopRatedFeaturedProductsProvider
import moc.itivo.officeworks.catalogue.ui.home.HomeIntent
import moc.itivo.officeworks.catalogue.ui.home.HomeViewModel
import moc.itivo.officeworks.core.result.DataError
import moc.itivo.officeworks.core.result.DataResult
import moc.itivo.officeworks.testutil.FakeProductRepository
import moc.itivo.officeworks.testutil.MainDispatcherRule
import moc.itivo.officeworks.testutil.category
import moc.itivo.officeworks.testutil.product
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertNull
import org.junit.Rule
import org.junit.Test

class HomeViewModelTest {

    @get:Rule
    val mainDispatcherRule = MainDispatcherRule()

    private val repository = FakeProductRepository()
    private fun viewModel() = HomeViewModel(TopRatedFeaturedProductsProvider(repository), repository)

    @Test
    fun `shows the five featured products and the categories`() {
        val featured = (1..5).map { product(it) }
        repository.topRatedResult = DataResult.Success(featured)
        repository.categoriesResult = DataResult.Success(listOf(category("beauty"), category("laptops")))

        val state = viewModel().state.value

        assertFalse(state.isLoading)
        assertNull(state.error)
        assertEquals(featured, state.featured)
        assertEquals(listOf("beauty", "laptops"), state.categories.map { it.slug })
    }

    @Test
    fun `shows an error when featured products fail, and retry recovers`() {
        repository.topRatedResult = DataResult.Failure(DataError.NETWORK)
        val viewModel = viewModel()
        assertEquals(DataError.NETWORK, viewModel.state.value.error)

        repository.topRatedResult = DataResult.Success(listOf(product(1)))
        viewModel.onIntent(HomeIntent.Retry)

        assertNull(viewModel.state.value.error)
        assertEquals(1, viewModel.state.value.featured.size)
    }

    @Test
    fun `shows an error when categories fail`() {
        repository.categoriesResult = DataResult.Failure(DataError.SERVER)

        assertEquals(DataError.SERVER, viewModel().state.value.error)
    }
}
