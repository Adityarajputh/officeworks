package moc.itivo.officeworks.testutil

import kotlinx.serialization.json.Json
import mockwebserver3.MockResponse
import mockwebserver3.MockWebServer
import moc.itivo.officeworks.core.network.NetworkModule
import okhttp3.MediaType.Companion.toMediaType
import retrofit2.Retrofit
import retrofit2.converter.kotlinx.serialization.asConverterFactory
import retrofit2.create

/**
 * A Retrofit API pointed at this local server, using the app's real [Json] config, so tests pin
 * down how the app actually parses responses.
 */
inline fun <reified T : Any> MockWebServer.createApi(json: Json = NetworkModule.provideJson()): T =
    Retrofit.Builder()
        .baseUrl(url("/"))
        .addConverterFactory(json.asConverterFactory("application/json".toMediaType()))
        .build()
        .create()

fun jsonResponse(body: String, code: Int = 200): MockResponse = MockResponse.Builder()
    .code(code)
    .addHeader("Content-Type", "application/json")
    .body(body)
    .build()
