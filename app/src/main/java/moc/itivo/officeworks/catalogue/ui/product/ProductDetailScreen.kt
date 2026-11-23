package moc.itivo.officeworks.catalogue.ui.product

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Button
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import androidx.hilt.lifecycle.viewmodel.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import moc.itivo.officeworks.R
import moc.itivo.officeworks.catalogue.domain.model.Product
import moc.itivo.officeworks.catalogue.ui.components.ProductImage
import moc.itivo.officeworks.catalogue.ui.components.RatingRow
import moc.itivo.officeworks.catalogue.ui.components.formatPrice
import moc.itivo.officeworks.core.ui.BackTopAppBar
import moc.itivo.officeworks.core.ui.ErrorView
import moc.itivo.officeworks.core.ui.LoadingView
import moc.itivo.officeworks.navigation.Destination

@Composable
fun ProductDetailRoute(
    route: Destination.Product,
    onBack: () -> Unit,
    onEnquire: (Product) -> Unit,
    viewModel: ProductDetailViewModel = hiltViewModel<ProductDetailViewModel, ProductDetailViewModel.Factory> {
        it.create(route)
    },
) {
    val state by viewModel.state.collectAsStateWithLifecycle()
    ProductDetailScreen(state, viewModel::onIntent, onBack, onEnquire)
}

@Composable
fun ProductDetailScreen(
    state: ProductDetailState,
    onIntent: (ProductDetailIntent) -> Unit,
    onBack: () -> Unit,
    onEnquire: (Product) -> Unit,
) {
    val product = state.product
    Scaffold(
        topBar = { BackTopAppBar(title = stringResource(R.string.product_title), onBack = onBack) },
        bottomBar = {
            if (product != null) {
                Surface(tonalElevation = 3.dp) {
                    Button(
                        onClick = { onEnquire(product) },
                        modifier = Modifier
                            .fillMaxWidth()
                            .navigationBarsPadding()
                            .padding(16.dp)
                            .testTag("enquire_button"),
                    ) {
                        Text(stringResource(R.string.product_enquire))
                    }
                }
            }
        },
    ) { padding ->
        val modifier = Modifier.padding(padding)
        when {
            state.isLoading -> LoadingView(modifier)
            state.error != null -> ErrorView(state.error, onRetry = { onIntent(ProductDetailIntent.Retry) }, modifier)
            product != null -> ProductDetails(product, modifier)
        }
    }
}

@Composable
private fun ProductDetails(product: Product, modifier: Modifier = Modifier) {
    LazyColumn(
        modifier = modifier,
        contentPadding = PaddingValues(vertical = 16.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp),
    ) {
        item(key = "images") {
            // No images at all still shows one "image unavailable" placeholder.
            val images: List<String?> = product.imageUrls.ifEmpty { listOf(product.thumbnailUrl) }
            LazyRow(
                contentPadding = PaddingValues(horizontal = 16.dp),
                horizontalArrangement = Arrangement.spacedBy(12.dp),
            ) {
                itemsIndexed(images) { index, url ->
                    ProductImage(
                        url = url,
                        contentDescription = stringResource(
                            R.string.product_image_description,
                            index + 1,
                            product.title,
                        ),
                        modifier = Modifier
                            .size(280.dp)
                            .clip(RoundedCornerShape(12.dp)),
                    )
                }
            }
        }
        item(key = "summary") {
            Column(Modifier.padding(horizontal = 16.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
                Text(product.title, style = MaterialTheme.typography.headlineSmall)
                product.brand?.let {
                    Text(
                        stringResource(R.string.product_brand, it),
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                    )
                }
                Text(formatPrice(product.price), style = MaterialTheme.typography.titleLarge)
                RatingRow(product.rating)
                product.availability?.let {
                    Text(it, style = MaterialTheme.typography.labelLarge, color = MaterialTheme.colorScheme.primary)
                }
            }
        }
        item(key = "description") {
            Text(
                product.description,
                style = MaterialTheme.typography.bodyLarge,
                modifier = Modifier.padding(horizontal = 16.dp),
            )
        }
    }
}
