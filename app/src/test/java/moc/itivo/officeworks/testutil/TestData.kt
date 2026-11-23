package moc.itivo.officeworks.testutil

import moc.itivo.officeworks.catalogue.domain.model.Category
import moc.itivo.officeworks.catalogue.domain.model.Product
import java.math.BigDecimal

fun product(
    id: Int,
    rating: Double = 4.0,
    title: String = "Product $id",
    category: String = "smartphones",
) = Product(
    id = id,
    title = title,
    description = "Description of $title",
    categorySlug = category,
    brand = "Brand",
    price = BigDecimal("9.99"),
    rating = rating,
    thumbnailUrl = "https://example.com/$id.png",
    imageUrls = listOf("https://example.com/$id.png"),
    availability = "In Stock",
)

fun category(slug: String, name: String = slug.replaceFirstChar(Char::uppercase)) = Category(slug, name)
