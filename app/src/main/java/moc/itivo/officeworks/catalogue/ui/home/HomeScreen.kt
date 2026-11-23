package moc.itivo.officeworks.catalogue.ui.home

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.grid.GridItemSpan
import androidx.compose.material3.AssistChip
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import androidx.hilt.lifecycle.viewmodel.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import moc.itivo.officeworks.R
import moc.itivo.officeworks.catalogue.domain.model.Category
import moc.itivo.officeworks.catalogue.ui.components.ProductGrid
import moc.itivo.officeworks.catalogue.ui.components.ProductSearchBar
import moc.itivo.officeworks.catalogue.ui.components.gridHeader
import moc.itivo.officeworks.catalogue.ui.components.productTiles
import moc.itivo.officeworks.core.ui.ErrorView
import moc.itivo.officeworks.core.ui.LoadingView

@Composable
fun HomeRoute(
    onProductClick: (Int) -> Unit,
    onCategoryClick: (Category) -> Unit,
    onSearch: (String) -> Unit,
    viewModel: HomeViewModel = hiltViewModel(),
) {
    val state by viewModel.state.collectAsStateWithLifecycle()
    HomeScreen(
        state = state,
        onIntent = viewModel::onIntent,
        onProductClick = onProductClick,
        onCategoryClick = onCategoryClick,
        onSearch = onSearch,
    )
}

@OptIn(ExperimentalMaterial3Api::class, ExperimentalLayoutApi::class)
@Composable
fun HomeScreen(
    state: HomeState,
    onIntent: (HomeIntent) -> Unit,
    onProductClick: (Int) -> Unit,
    onCategoryClick: (Category) -> Unit,
    onSearch: (String) -> Unit,
) {
    // The search text only matters until the user submits, so it stays in the UI.
    var query by rememberSaveable { mutableStateOf("") }
    val featuredTitle = stringResource(R.string.home_featured_title)
    val categoriesTitle = stringResource(R.string.home_categories_title)

    Scaffold(topBar = { TopAppBar(title = { Text(stringResource(R.string.app_name)) }) }) { padding ->
        Column(Modifier.padding(padding).fillMaxSize()) {
            ProductSearchBar(
                query = query,
                onQueryChange = { query = it },
                onSearch = { onSearch(query.trim()) },
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp, vertical = 8.dp),
            )
            when {
                state.isLoading -> LoadingView()
                state.error != null -> ErrorView(state.error, onRetry = { onIntent(HomeIntent.Retry) })
                else -> ProductGrid {
                    gridHeader(featuredTitle, key = "featured-header")
                    productTiles(state.featured, keyPrefix = "featured", onProductClick = onProductClick)

                    gridHeader(categoriesTitle, key = "categories-header")
                    item(key = "categories", span = { GridItemSpan(maxLineSpan) }) {
                        FlowRow(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                            state.categories.forEach { category ->
                                AssistChip(
                                    onClick = { onCategoryClick(category) },
                                    label = { Text(category.name) },
                                )
                            }
                        }
                    }
                }
            }
        }
    }
}
