package moc.itivo.officeworks.catalogue.ui.components

import java.math.BigDecimal
import java.math.RoundingMode

/**
 * DummyJSON does not say which currency its prices are in, so we show a plain "$" amount.
 * When the real catalogue arrives with a currency code, format with NumberFormat instead.
 */
fun formatPrice(price: BigDecimal): String = "$" + price.setScale(2, RoundingMode.HALF_UP).toPlainString()

fun formatRating(rating: Double): String = String.format(java.util.Locale.ROOT, "%.1f", rating)
