package com.example.surveillancecamera.data

import android.util.Base64
import com.example.surveillancecamera.BuildConfig
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import okhttp3.OkHttpClient
import okhttp3.Request
import org.json.JSONArray

class CameraRepository {

    private val client = OkHttpClient()

    private fun authorizationHeader(): String {
        val credentials =
            "${BuildConfig.CAMERA_USERNAME}:${BuildConfig.CAMERA_PASSWORD}"

        val encodedCredentials =
            Base64.encodeToString(
                credentials.toByteArray(),
                Base64.NO_WRAP
            )

        return "Basic $encodedCredentials"
    }

    suspend fun fetchImageList(): List<String> = withContext(Dispatchers.IO) {
        val request = Request.Builder()
            .url("${BuildConfig.CAMERA_API_URL.substringBeforeLast("/latest")}/images")
            .header("Authorization", authorizationHeader())
            .get()
            .build()

        client.newCall(request).execute().use { response ->
            if (!response.isSuccessful) {
                throw RuntimeException("HTTP error: ${response.code}")
            }

            val body = response.body.string()

            val jsonArray = JSONArray(body)

            List(jsonArray.length()) { index ->
                jsonArray.getString(index)
            }
        }
    }

    suspend fun fetchImage(fileName: String): CameraImage =
        withContext(Dispatchers.IO) {

            val baseUrl =
                BuildConfig.CAMERA_API_URL.substringBeforeLast("/latest")

            val request = Request.Builder()
                .url("$baseUrl/image?filename=$fileName")
                .header("Authorization", authorizationHeader())
                .get()
                .build()

            client.newCall(request).execute().use { response ->
                if (!response.isSuccessful) {
                    throw RuntimeException("HTTP error: ${response.code}")
                }

                val image = response.body.bytes()

                val responseFileName =
                    response.header("X-Camera-Filename")
                        ?: throw RuntimeException(
                            "X-Camera-Filename header is missing"
                        )

                CameraImage(
                    image = image,
                    fileName = responseFileName
                )
            }
        }
}

data class CameraImage(
    val image: ByteArray,
    val fileName: String
)
