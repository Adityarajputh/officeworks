package moc.itivo.officeworks.catalogue.ui.components

import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.grid.GridItemSpan
import androidx.compose.foundation.lazy.grid.LazyGridScope
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.ui.Modifier
import androidx.compose.ui.semantics.heading
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.unit.dp
import moc.itivo.officeworks.catalogue.domain.model.Product

val GridContentPadding = PaddingValues(16.dp)
val GridMinCellWidth = 160.dp

/** A full-width section title inside a product grid. */
fun LazyGridScope.gridHeader(text: String, key: String) {
    item(key = key, span = { GridItemSpan(maxLineSpan) }) {
        Text(
            text = text,
            style = MaterialTheme.typography.titleLarge,
            modifier = Modifier
                .padding(top = 8.dp, bottom = 4.dp)
                .semantics { heading() },
        )
    }
}

fun LazyGridScope.productTiles(products: List<Product>, keyPrefix: String, onProductClick: (Int) -> Unit) {
    items(products, key = { "$keyPrefix-${it.id}" }) { product ->
        ProductTile(product = product, onClick = { onProductClick(product.id) })
    }
}
