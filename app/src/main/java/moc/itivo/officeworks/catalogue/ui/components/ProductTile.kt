package moc.itivo.officeworks.catalogue.ui.components

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Star
import androidx.compose.material3.Card
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import moc.itivo.officeworks.R
import moc.itivo.officeworks.catalogue.domain.model.Product

@Composable
fun ProductTile(product: Product, onClick: () -> Unit, modifier: Modifier = Modifier) {
    Card(onClick = onClick, modifier = modifier.testTag("product_tile_${product.id}")) {
        ProductImage(
            url = product.thumbnailUrl,
            contentDescription = null, // The title below already describes the tile.
            modifier = Modifier
                .fillMaxWidth()
                .aspectRatio(1f),
        )
        Column(Modifier.padding(12.dp), verticalArrangement = Arrangement.spacedBy(4.dp)) {
            Text(
                text = product.title,
                style = MaterialTheme.typography.titleSmall,
                maxLines = 2,
                overflow = TextOverflow.Ellipsis,
            )
            Text(text = formatPrice(product.price), style = MaterialTheme.typography.bodyMedium)
            RatingRow(product.rating)
        }
    }
}

@Composable
fun RatingRow(rating: Double, modifier: Modifier = Modifier) {
    val ratingText = formatRating(rating)
    val description = stringResource(R.string.product_rating, ratingText)
    Row(
        modifier = modifier.semantics(mergeDescendants = true) { contentDescription = description },
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(4.dp),
    ) {
        Icon(
            Icons.Filled.Star,
            contentDescription = null,
            tint = MaterialTheme.colorScheme.secondary,
            modifier = Modifier.size(16.dp),
        )
        Text(text = ratingText, style = MaterialTheme.typography.labelMedium)
    }
}
