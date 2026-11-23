package moc.itivo.officeworks.catalogue.ui.search

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Scaffold
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.pluralStringResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import androidx.hilt.lifecycle.viewmodel.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import moc.itivo.officeworks.R
import moc.itivo.officeworks.catalogue.ui.components.ProductGrid
import moc.itivo.officeworks.catalogue.ui.components.ProductSearchBar
import moc.itivo.officeworks.catalogue.ui.components.gridHeader
import moc.itivo.officeworks.catalogue.ui.components.productTiles
import moc.itivo.officeworks.core.ui.BackTopAppBar
import moc.itivo.officeworks.core.ui.ErrorView
import moc.itivo.officeworks.core.ui.LoadingView
import moc.itivo.officeworks.core.ui.MessageView
import moc.itivo.officeworks.navigation.Destination

@Composable
fun SearchRoute(
    route: Destination.Search,
    onBack: () -> Unit,
    onProductClick: (Int) -> Unit,
    viewModel: SearchViewModel = hiltViewModel<SearchViewModel, SearchViewModel.Factory> { it.create(route) },
) {
    val state by viewModel.state.collectAsStateWithLifecycle()
    SearchScreen(state, viewModel::onIntent, onBack, onProductClick)
}

@Composable
fun SearchScreen(
    state: SearchState,
    onIntent: (SearchIntent) -> Unit,
    onBack: () -> Unit,
    onProductClick: (Int) -> Unit,
) {
    Scaffold(topBar = { BackTopAppBar(title = stringResource(R.string.search_title), onBack = onBack) }) { padding ->
        Column(Modifier.padding(padding).fillMaxSize()) {
            ProductSearchBar(
                query = state.query,
                onQueryChange = { onIntent(SearchIntent.QueryChanged(it)) },
                onSearch = { onIntent(SearchIntent.Submit) },
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp, vertical = 8.dp),
            )
            val submitted = state.submittedQuery
            when {
                submitted == null -> MessageView(stringResource(R.string.search_prompt))
                state.isLoading -> LoadingView()
                state.error != null -> ErrorView(state.error, onRetry = { onIntent(SearchIntent.Retry) })
                state.results.isEmpty() -> MessageView(stringResource(R.string.search_no_results, submitted))
                else -> {
                    val count = state.results.size
                    val resultsTitle = pluralStringResource(R.plurals.search_results_count, count, count, submitted)
                    ProductGrid {
                        gridHeader(resultsTitle, key = "results-header")
                        productTiles(state.results, keyPrefix = "result", onProductClick = onProductClick)
                    }
                }
            }
        }
    }
}
