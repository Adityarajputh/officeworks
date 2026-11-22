package moc.itivo.officeworks.catalogue.ui.category

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.material3.Scaffold
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import androidx.hilt.lifecycle.viewmodel.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import moc.itivo.officeworks.R
import moc.itivo.officeworks.catalogue.ui.components.GridContentPadding
import moc.itivo.officeworks.catalogue.ui.components.GridMinCellWidth
import moc.itivo.officeworks.catalogue.ui.components.gridHeader
import moc.itivo.officeworks.catalogue.ui.components.productTiles
import moc.itivo.officeworks.core.result.DataError
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
            state.error != null -> ErrorView(
                error = state.error,
                onRetry = { onIntent(CategoryIntent.Retry) }.takeIf { state.error != DataError.NOT_FOUND },
                modifier = modifier,
            )
            state.topRated.isEmpty() -> MessageView(stringResource(R.string.category_empty), modifier)
            else -> LazyVerticalGrid(
                columns = GridCells.Adaptive(GridMinCellWidth),
                contentPadding = GridContentPadding,
                horizontalArrangement = Arrangement.spacedBy(12.dp),
                verticalArrangement = Arrangement.spacedBy(12.dp),
                modifier = modifier,
            ) {
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
