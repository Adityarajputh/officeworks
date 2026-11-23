package moc.itivo.officeworks.core.network

import kotlinx.coroutines.CancellationException
import kotlinx.serialization.SerializationException
import moc.itivo.officeworks.core.result.DataError
import moc.itivo.officeworks.core.result.DataResult
import retrofit2.HttpException
import java.io.IOException

/**
 * Runs a network call and turns the exceptions Retrofit/OkHttp can throw into a [DataResult].
 * Cancellation is rethrown so structured concurrency keeps working.
 */
suspend fun <T> safeApiCall(block: suspend () -> T): DataResult<T> = try {
    DataResult.Success(block())
} catch (e: CancellationException) {
    throw e
} catch (e: HttpException) {
    DataResult.Failure(
        when (e.code()) {
            404 -> DataError.NOT_FOUND
            in 500..599 -> DataError.SERVER
            else -> DataError.UNEXPECTED
        },
    )
} catch (e: IOException) {
    DataResult.Failure(DataError.NETWORK)
} catch (e: SerializationException) {
    DataResult.Failure(DataError.UNEXPECTED)
}
