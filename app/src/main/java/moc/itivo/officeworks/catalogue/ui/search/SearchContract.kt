package moc.itivo.officeworks.catalogue.ui.search

import moc.itivo.officeworks.catalogue.domain.model.Product
import moc.itivo.officeworks.core.result.DataError

data class SearchState(
    /** What is in the text field right now. */
    val query: String = "",
    /** The term the results belong to; null until the first search. */
    val submittedQuery: String? = null,
    val isLoading: Boolean = false,
    val results: List<Product> = emptyList(),
    val error: DataError? = null,
)

sealed interface SearchIntent {
    data class QueryChanged(val query: String) : SearchIntent
    data object Submit : SearchIntent
    data object Retry : SearchIntent
}
