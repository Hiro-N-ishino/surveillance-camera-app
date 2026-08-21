package com.example.surveillancecamera.data

import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import okhttp3.OkHttpClient
import okhttp3.Request
import com.example.surveillancecamera.BuildConfig

class CameraRepository {

    private val client = OkHttpClient()

    suspend fun fetchLatestImage(): CameraImage = withContext(Dispatchers.IO){
        val request = Request.Builder()
            .url(BuildConfig.CAMERA_API_URL)
            .get()
            .build()

        client.newCall(request).execute().use { response ->
            if (!response.isSuccessful) {
                throw RuntimeException("HTTP error: ${response.code}")
            }

            //response.body.bytes()
            val image = response.body.bytes()
            val fileName = response.header("X-Camera-Filename")
                ?: throw RuntimeException("X-Camera-Filename header is missing")
            
            CameraImage(
                image = image,
                fileName = fileName
            )
        }
    }
}

data class CameraImage(
    val image: ByteArray,
    val fileName: String
)
