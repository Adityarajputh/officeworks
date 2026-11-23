package moc.itivo.officeworks.core.result

/**
 * Outcome of a call that crosses a boundary (network today, maybe a database or our own
 * backend later). Callers must handle [Failure]; nothing below the domain layer throws.
 */
sealed interface DataResult<out T> {
    data class Success<out T>(val data: T) : DataResult<T>
    data class Failure(val error: DataError) : DataResult<Nothing>
}

/** What went wrong, described in terms the UI can act on rather than HTTP details. */
enum class DataError {
    /** No connection, timeout, DNS failure. Usually worth a retry. */
    NETWORK,

    /** The thing asked for does not exist (HTTP 404). Retrying will not help. */
    NOT_FOUND,

    /** The server failed (HTTP 5xx). */
    SERVER,

    /** Anything else: unexpected status, malformed response. */
    UNEXPECTED,
}

inline fun <T, R> DataResult<T>.map(transform: (T) -> R): DataResult<R> = when (this) {
    is DataResult.Success -> DataResult.Success(transform(data))
    is DataResult.Failure -> this
}

fun DataResult<*>.errorOrNull(): DataError? = (this as? DataResult.Failure)?.error
