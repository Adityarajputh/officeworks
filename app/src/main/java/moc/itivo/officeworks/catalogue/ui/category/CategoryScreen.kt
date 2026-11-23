package moc.itivo.officeworks.catalogue.ui.category

import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Scaffold
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.hilt.lifecycle.viewmodel.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import moc.itivo.officeworks.R
import moc.itivo.officeworks.catalogue.ui.components.ProductGrid
import moc.itivo.officeworks.catalogue.ui.components.gridHeader
import moc.itivo.officeworks.catalogue.ui.components.productTiles
import moc.itivo.officeworks.core.ui.BackTopAppBar
import moc.itivo.officeworks.core.ui.ErrorView
import moc.itivo.officeworks.core.ui.LoadingView
import moc.itivo.officeworks.core.ui.MessageView
import moc.itivo.officeworks.navigation.Destination

@Composable
fun CategoryRoute(
    route: Destination.Category,
    onBack: () -> Unit,
    onProductClick: (Int) -> Unit,
    viewModel: CategoryViewModel = hiltViewModel<CategoryViewModel, CategoryViewModel.Factory> { it.create(route) },
) {
    val state by viewModel.state.collectAsStateWithLifecycle()
    CategoryScreen(state, viewModel::onIntent, onBack, onProductClick)
}

@Composable
fun CategoryScreen(
    state: CategoryState,
    onIntent: (CategoryIntent) -> Unit,
    onBack: () -> Unit,
    onProductClick: (Int) -> Unit,
) {
    val topRatedTitle = stringResource(R.string.category_top_rated_title, state.categoryName)
    val moreTitle = stringResource(R.string.category_more_title, state.categoryName)
    Scaffold(topBar = { BackTopAppBar(title = state.categoryName, onBack = onBack) }) { padding ->
        val modifier = Modifier.padding(padding)
        when {
            state.isLoading -> LoadingView(modifier)
            state.error != null -> ErrorView(state.error, onRetry = { onIntent(CategoryIntent.Retry) }, modifier)
            state.topRated.isEmpty() -> MessageView(stringResource(R.string.category_empty), modifier)
            else -> ProductGrid(modifier) {
                gridHeader(topRatedTitle, key = "top-header")
                productTiles(state.topRated, keyPrefix = "top", onProductClick = onProductClick)
                if (state.others.isNotEmpty()) {
                    gridHeader(moreTitle, key = "more-header")
                    productTiles(state.others, keyPrefix = "more", onProductClick = onProductClick)
                }
            }
        }
    }
}
