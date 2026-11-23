package moc.itivo.officeworks.catalogue.ui

import moc.itivo.officeworks.catalogue.ui.product.ProductDetailViewModel
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

class ProductDetailViewModelTest {

    @get:Rule
    val mainDispatcherRule = MainDispatcherRule()

    private val repository = FakeProductRepository()
    private fun viewModel() = ProductDetailViewModel(Destination.Product(productId = 42), repository)

    @Test
    fun `loads the product named in the route`() {
        repository.productResult = DataResult.Success(product(42))

        val state = viewModel().state.value

        assertEquals(listOf(42), repository.requestedProductIds)
        assertEquals(42, state.product?.id)
        assertNull(state.error)
    }

    @Test
    fun `an unknown product shows not found`() {
        repository.productResult = DataResult.Failure(DataError.NOT_FOUND)

        val state = viewModel().state.value

        assertEquals(DataError.NOT_FOUND, state.error)
        assertNull(state.product)
    }
}
