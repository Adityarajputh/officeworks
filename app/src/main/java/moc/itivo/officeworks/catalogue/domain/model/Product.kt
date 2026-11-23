package moc.itivo.officeworks.catalogue.domain.model

import java.math.BigDecimal

/** A product as the app understands it, independent of any API's JSON shape. */
data class Product(
    val id: Int,
    val title: String,
    val description: String,
    val categorySlug: String,
    val brand: String?,
    val price: BigDecimal,
    val rating: Double,
    val thumbnailUrl: String?,
    val imageUrls: List<String>,
    val availability: String?,
)

data class Category(
    val slug: String,
    val name: String,
)

/** A category page: the best-rated products first, then everything else in the category. */
data class CategoryProducts(
    val topRated: List<Product>,
    val others: List<Product>,
)
