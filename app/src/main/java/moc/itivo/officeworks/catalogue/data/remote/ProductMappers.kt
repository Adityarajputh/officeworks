package moc.itivo.officeworks.catalogue.data.remote

import moc.itivo.officeworks.catalogue.domain.model.Category
import moc.itivo.officeworks.catalogue.domain.model.Product
import java.math.BigDecimal

fun ProductDto.toDomain(): Product = Product(
    id = id,
    title = title.trim(),
    description = description.trim(),
    categorySlug = category,
    brand = brand?.trim()?.takeIf { it.isNotEmpty() },
    // BigDecimal.valueOf keeps 9.99 as 9.99 instead of 9.9900000000000002131628...
    price = BigDecimal.valueOf(price),
    rating = rating,
    thumbnailUrl = thumbnail?.takeIf { it.isNotBlank() } ?: images.firstOrNull(),
    imageUrls = images.filter { it.isNotBlank() },
    availability = availabilityStatus?.takeIf { it.isNotBlank() },
)

fun CategoryDto.toDomain(): Category = Category(slug = slug, name = name)
