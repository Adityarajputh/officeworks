package moc.itivo.officeworks.catalogue.data.remote

import kotlinx.serialization.Serializable

/** DummyJSON's response shapes. These never leave the data layer; see ProductMappers.kt. */
@Serializable
data class ProductListDto(
    val products: List<ProductDto>,
    val total: Int = 0,
)

@Serializable
data class ProductDto(
    val id: Int,
    val title: String,
    val price: Double,
    val description: String = "",
    val category: String = "",
    val brand: String? = null,
    val rating: Double = 0.0,
    val thumbnail: String? = null,
    val images: List<String> = emptyList(),
    val availabilityStatus: String? = null,
)

@Serializable
data class CategoryDto(
    val slug: String,
    val name: String,
)
