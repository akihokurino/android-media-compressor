package app.akiho.media_compressor.infra.http

import android.content.Context
import android.graphics.Bitmap
import android.net.Uri
import androidx.compose.ui.graphics.ImageBitmap
import androidx.compose.ui.graphics.asAndroidBitmap
import app.akiho.media_compressor.extension.imageBitmap
import app.akiho.media_compressor.model.AppError
import dagger.hilt.android.qualifiers.ApplicationContext
import java.io.File
import java.io.FileOutputStream
import java.io.InputStream
import javax.inject.Inject
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import okhttp3.OkHttpClient
import okhttp3.Request

class HttpClient
@Inject
constructor(
    @ApplicationContext private val context: Context,
    private val httpClient: OkHttpClient
) {
  suspend fun downloadImage(url: Uri): Result<ImageBitmap> {
    return withContext(Dispatchers.IO) {
      val request = Request.Builder().url(url.toString()).build()
      val response = httpClient.newCall(request).execute()
      if (response.isSuccessful) {
        val inputStream: InputStream? = response.body?.byteStream()
        inputStream?.let { Result.success(it.imageBitmap()) } ?: Result.failure(AppError.from(null))
      } else {
        Result.failure(AppError.from(null))
      }
    }
  }

  suspend fun downloadImageToFile(
      url: Uri,
  ): Result<File> {
    return withContext(Dispatchers.IO) {
      val imageBitmap =
          downloadImage(url).getOrNull() ?: return@withContext Result.failure(AppError.from(null))
      val tempFile = File.createTempFile("temp", ".jpeg", context.cacheDir)
      val outputStream = FileOutputStream(tempFile)
      outputStream.use {
        val bitmap = imageBitmap.asAndroidBitmap()
        bitmap.compress(Bitmap.CompressFormat.JPEG, 100, it)
      }
      Result.success(tempFile)
    }
  }

  suspend fun downloadVideo(url: Uri): Result<File> {
    return withContext(Dispatchers.IO) {
      val request = Request.Builder().url(url.toString()).build()
      val response = httpClient.newCall(request).execute()
      if (response.isSuccessful) {
        val inputStream: InputStream? = response.body?.byteStream()
        inputStream?.use {
          val tempFile = File.createTempFile("temp", ".mp4", context.cacheDir)
          val outputStream = FileOutputStream(tempFile)
          val buffer = ByteArray(1024)
          var length: Int
          while (inputStream.read(buffer).also { length = it } != -1) {
            outputStream.write(buffer, 0, length)
          }
          outputStream.close()
          Result.success(tempFile)
        } ?: Result.failure(AppError.from(null))
      } else {
        Result.failure(AppError.from(null))
      }
    }
  }
}
