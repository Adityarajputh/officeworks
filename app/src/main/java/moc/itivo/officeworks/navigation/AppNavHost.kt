package moc.itivo.officeworks.navigation

import androidx.compose.runtime.Composable
import androidx.navigation.NavHostController
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import androidx.navigation.toRoute
import moc.itivo.officeworks.catalogue.ui.category.CategoryRoute
import moc.itivo.officeworks.catalogue.ui.home.HomeRoute
import moc.itivo.officeworks.catalogue.ui.product.ProductDetailRoute
import moc.itivo.officeworks.catalogue.ui.search.SearchRoute
import moc.itivo.officeworks.enquiry.ui.EnquiryRoute

/**
 * All navigation lives here. Screens only expose callbacks (onProductClick, onBack, ...), so they
 * can be reused or tested without a NavController.
 *
 * Arguments are decoded here with toRoute() and handed to each ViewModel through Hilt assisted
 * injection, so ViewModels take a plain typed route object and never touch SavedStateHandle.
 */
@Composable
fun AppNavHost(navController: NavHostController = rememberNavController()) {
    // launchSingleTop stops a double tap from pushing the same screen twice.
    fun go(destination: Destination) = navController.navigate(destination) { launchSingleTop = true }
    val back: () -> Unit = { navController.navigateUp() }
    val openProduct: (Int) -> Unit = { id -> go(Destination.Product(id)) }

    NavHost(navController = navController, startDestination = Destination.Home) {
        composable<Destination.Home> {
            HomeRoute(
                onProductClick = openProduct,
                onCategoryClick = { category -> go(Destination.Category(category.slug, category.name)) },
                onSearch = { query -> go(Destination.Search(query)) },
            )
        }
        composable<Destination.Category> { entry ->
            CategoryRoute(route = entry.toRoute(), onBack = back, onProductClick = openProduct)
        }
        composable<Destination.Search> { entry ->
            SearchRoute(route = entry.toRoute(), onBack = back, onProductClick = openProduct)
        }
        composable<Destination.Product> { entry ->
            ProductDetailRoute(
                route = entry.toRoute(),
                onBack = back,
                onEnquire = { product -> go(Destination.Enquiry(product.id, product.title)) },
            )
        }
        composable<Destination.Enquiry> { entry ->
            EnquiryRoute(
                route = entry.toRoute(),
                onBack = back,
                onContinueBrowsing = { navController.popBackStack(Destination.Home, inclusive = false) },
            )
        }
    }
}
