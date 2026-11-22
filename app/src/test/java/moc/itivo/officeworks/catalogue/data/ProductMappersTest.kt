package moc.itivo.officeworks.catalogue.data

import moc.itivo.officeworks.catalogue.data.remote.CategoryDto
import moc.itivo.officeworks.catalogue.data.remote.ProductDto
import moc.itivo.officeworks.catalogue.data.remote.toDomain
import moc.itivo.officeworks.catalogue.domain.model.Category
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNull
import org.junit.Test
import java.math.BigDecimal

class ProductMappersTest {

    private val dto = ProductDto(
        id = 1,
        title = "  Phone  ",
        price = 9.99,
        description = "A phone",
        category = "smartphones",
        brand = "Acme",
        rating = 4.5,
        thumbnail = "https://example.com/thumb.png",
        images = listOf("https://example.com/1.png", ""),
        availabilityStatus = "In Stock",
    )

    @Test
    fun `maps every field and trims text`() {
        val product = dto.toDomain()

        assertEquals(1, product.id)
        assertEquals("Phone", product.title)
        assertEquals("A phone", product.description)
        assertEquals("smartphones", product.categorySlug)
        assertEquals("Acme", product.brand)
        assertEquals(4.5, product.rating, 0.0)
        assertEquals("https://example.com/thumb.png", product.thumbnailUrl)
        assertEquals("In Stock", product.availability)
    }

    @Test
    fun `price keeps its exact decimal value`() {
        assertEquals(BigDecimal("9.99"), dto.toDomain().price)
    }

    @Test
    fun `blank image urls are dropped`() {
        assertEquals(listOf("https://example.com/1.png"), dto.toDomain().imageUrls)
    }

    @Test
    fun `blank brand becomes null`() {
        assertNull(dto.copy(brand = "  ").toDomain().brand)
    }

    @Test
    fun `missing thumbnail falls back to the first image`() {
        assertEquals("https://example.com/1.png", dto.copy(thumbnail = null).toDomain().thumbnailUrl)
    }

    @Test
    fun `maps a category`() {
        assertEquals(Category("home-decoration", "Home Decoration"), CategoryDto("home-decoration", "Home Decoration").toDomain())
    }
}
