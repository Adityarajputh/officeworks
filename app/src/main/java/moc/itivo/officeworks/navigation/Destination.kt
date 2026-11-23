package moc.itivo.officeworks.navigation

import kotlinx.serialization.Serializable

/** Type-safe navigation destinations. AppNavHost decodes them and passes them to ViewModels. */
sealed interface Destination {
    @Serializable
    data object Home : Destination

    @Serializable
    data class Category(val slug: String, val name: String) : Destination

    @Serializable
    data class Search(val query: String) : Destination

    @Serializable
    data class Product(val productId: Int) : Destination

    @Serializable
    data class Enquiry(val productId: Int, val productTitle: String) : Destination
}
