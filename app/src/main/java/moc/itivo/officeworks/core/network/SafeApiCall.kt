package moc.itivo.officeworks.core.network

import kotlinx.coroutines.CancellationException
import kotlinx.serialization.SerializationException
import moc.itivo.officeworks.core.result.DataError
import moc.itivo.officeworks.core.result.DataResult
import retrofit2.HttpException
import timber.log.Timber
import java.io.IOException

/**
 * Runs a network call and turns the exceptions Retrofit/OkHttp can throw into a [DataResult].
 * Cancellation is rethrown so structured concurrency keeps working.
 *
 * The UI only gets a [DataError], so the real cause is logged here. Timber writes nowhere unless a
 * tree is planted: debug builds plant one in OfficeworksApp, and production would plant a crash
 * reporting tree. Request bodies never reach these exceptions, so customer details from an
 * enquiry are not logged.
 */
suspend fun <T> safeApiCall(block: suspend () -> T): DataResult<T> = try {
    DataResult.Success(block())
} catch (e: CancellationException) {
    throw e
} catch (e: HttpException) {
    failure(
        when (e.code()) {
            404 -> DataError.NOT_FOUND
            in 500..599 -> DataError.SERVER
            else -> DataError.UNEXPECTED
        },
        e,
    )
} catch (e: IOException) {
    failure(DataError.NETWORK, e)
} catch (e: SerializationException) {
    failure(DataError.UNEXPECTED, e)
}

private fun failure(error: DataError, cause: Throwable): DataResult.Failure {
    Timber.w(cause, "API call failed: %s", error)
    return DataResult.Failure(error)
}
