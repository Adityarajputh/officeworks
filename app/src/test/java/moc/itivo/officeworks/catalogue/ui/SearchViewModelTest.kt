package moc.itivo.officeworks.catalogue.ui

import kotlinx.coroutines.CompletableDeferred
import moc.itivo.officeworks.catalogue.ui.search.SearchIntent
import moc.itivo.officeworks.catalogue.ui.search.SearchViewModel
import moc.itivo.officeworks.core.result.DataError
import moc.itivo.officeworks.core.result.DataResult
import moc.itivo.officeworks.navigation.Destination
import moc.itivo.officeworks.testutil.FakeProductRepository
import moc.itivo.officeworks.testutil.MainDispatcherRule
import moc.itivo.officeworks.testutil.product
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Rule
import org.junit.Test

class SearchViewModelTest {

    @get:Rule
    val mainDispatcherRule = MainDispatcherRule()

    private val repository = FakeProductRepository()
    private fun viewModel(initialQuery: String = "") =
        SearchViewModel(Destination.Search(initialQuery), repository)

    @Test
    fun `searches straight away for the query typed on the home page`() {
        repository.searchResult = { DataResult.Success(listOf(product(1, title = "iPhone"))) }

        val state = viewModel(initialQuery = "  phone ").state.value

        assertEquals(listOf("phone"), repository.searchQueries)
        assertEquals("phone", state.submittedQuery)
        assertEquals(listOf("iPhone"), state.results.map { it.title })
    }

    @Test
    fun `typing alone does not search, pressing Search does`() {
        val viewModel = viewModel()

        viewModel.onIntent(SearchIntent.QueryChanged("lamp"))
        assertTrue(repository.searchQueries.isEmpty())

        viewModel.onIntent(SearchIntent.Submit)
        assertEquals(listOf("lamp"), repository.searchQueries)
    }

    @Test
    fun `a blank query never reaches the repository`() {
        val viewModel = viewModel(initialQuery = "   ")

        viewModel.onIntent(SearchIntent.Submit)

        assertTrue(repository.searchQueries.isEmpty())
        assertNull(viewModel.state.value.submittedQuery)
    }

    @Test
    fun `no matches is an empty result, not an error`() {
        val state = viewModel(initialQuery = "zzz").state.value

        assertEquals("zzz", state.submittedQuery)
        assertTrue(state.results.isEmpty())
        assertNull(state.error)
        assertFalse(state.isLoading)
    }

    @Test
    fun `retry repeats the last submitted search, not what is in the box`() {
        repository.searchResult = { DataResult.Failure(DataError.NETWORK) }
        val viewModel = viewModel(initialQuery = "desk")
        assertEquals(DataError.NETWORK, viewModel.state.value.error)

        viewModel.onIntent(SearchIntent.QueryChanged("half typed"))
        repository.searchResult = { DataResult.Success(listOf(product(1))) }
        viewModel.onIntent(SearchIntent.Retry)

        assertEquals(listOf("desk", "desk"), repository.searchQueries)
        assertNull(viewModel.state.value.error)
    }

    @Test
    fun `a slow older search cannot overwrite a newer one`() {
        val slow = CompletableDeferred<Unit>()
        repository.searchGates["old"] = slow
        repository.searchResult = { query -> DataResult.Success(listOf(product(1, title = query))) }
        val viewModel = viewModel(initialQuery = "old")
        assertTrue(viewModel.state.value.isLoading)

        viewModel.onIntent(SearchIntent.QueryChanged("new"))
        viewModel.onIntent(SearchIntent.Submit)
        slow.complete(Unit)

        assertEquals("new", viewModel.state.value.submittedQuery)
        assertEquals(listOf("new"), viewModel.state.value.results.map { it.title })
    }
}
