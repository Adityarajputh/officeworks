package moc.itivo.officeworks.catalogue.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.size
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import coil3.compose.AsyncImage
import moc.itivo.officeworks.R

/**
 * A product image with a neutral background while it loads, and a "broken image" icon if there
 * is no URL or the image fails to load, instead of an empty box.
 *
 * Pass a null [contentDescription] when nearby text already describes the product (tiles).
 */
@Composable
fun ProductImage(url: String?, contentDescription: String?, modifier: Modifier = Modifier) {
    var failed by remember(url) { mutableStateOf(url.isNullOrBlank()) }
    Box(
        modifier = modifier.background(MaterialTheme.colorScheme.surfaceVariant),
        contentAlignment = Alignment.Center,
    ) {
        if (failed) {
            Icon(
                painter = painterResource(R.drawable.ic_image_unavailable),
                contentDescription = contentDescription?.let { stringResource(R.string.image_unavailable) },
                tint = MaterialTheme.colorScheme.onSurfaceVariant,
                modifier = Modifier.size(40.dp),
            )
        } else {
            AsyncImage(
                model = url,
                contentDescription = contentDescription,
                contentScale = ContentScale.Fit,
                onError = { failed = true },
                modifier = Modifier.matchParentSize(),
            )
        }
    }
}
