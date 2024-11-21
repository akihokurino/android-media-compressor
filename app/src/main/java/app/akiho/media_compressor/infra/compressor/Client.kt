package app.akiho.media_compressor.infra.compressor

import android.content.Context
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.net.Uri
import android.provider.MediaStore
import android.util.Log
import androidx.compose.ui.graphics.ImageBitmap
import androidx.compose.ui.graphics.asAndroidBitmap
import app.akiho.media_compressor.model.AppError
import com.arthenica.ffmpegkit.FFmpegKit
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.suspendCancellableCoroutine
import kotlinx.coroutines.withContext
import java.io.ByteArrayOutputStream
import java.io.File
import java.io.FileOutputStream
import java.io.InputStream
import javax.inject.Inject
import kotlin.coroutines.resume

class CompressorClient
@Inject
constructor(
    @ApplicationContext private val context: Context,
) {
  suspend fun compress(image: ImageBitmap): Result<CompressedResult> =
      withContext(Dispatchers.IO) {
        suspendCancellableCoroutine { continuation ->
          val androidBitmap = image.asAndroidBitmap()
          val originalSize = androidBitmap.allocationByteCount.toLong()
          val data = compressBitmap(resizeBitmap(androidBitmap))
          if (data.size > 2 * 1024 * 1024) {
            continuation.resume(Result.failure(AppError.Plain("Unable to compress image below2MB")))
            return@suspendCancellableCoroutine
          }

          val tempFile = File.createTempFile("compressed_image", ".tmp", context.cacheDir)
          FileOutputStream(tempFile).use { fos -> fos.write(data) }
          val compressedUri = Uri.fromFile(tempFile)
          val compressedSize = getUriDataSize(context, compressedUri)

          continuation.resume(
              Result.success(
                  CompressedResult(
                      uri = compressedUri,
                      originalSize = originalSize,
                      compressedSize = compressedSize,
                      isVideo = false)))
        }
      }

  suspend fun compress(localUrl: Uri, videoDuration: Long? = null): Result<CompressedResult> =
      withContext(Dispatchers.IO) {
        suspendCancellableCoroutine { continuation ->
          if (videoDuration != null) {
            val originalSize = getUriDataSize(context, localUrl)
            val targetSizeInBytes = 2 * 1024 * 1024
            val targetBitrate = (targetSizeInBytes * 8 / videoDuration).toInt()
            val tempFile = File(context.cacheDir, "compressed_video.mp4")
            val filePath = getFilePathFromUri(context, localUrl)
            val command =
                "-y -i ${filePath} -vf scale=640:-2 -b:v ${targetBitrate} -maxrate ${targetBitrate} -bufsize ${targetBitrate / 2} -vcodec libx264 -preset medium ${tempFile.absolutePath}"

            FFmpegKit.executeAsync(
                command,
                { session ->
                  val returnCode = session.returnCode
                  if (returnCode.isValueSuccess) {
                    val compressedUri = Uri.fromFile(tempFile)
                    val compressedSize = getUriDataSize(context, compressedUri)
                    continuation.resume(
                        Result.success(
                            CompressedResult(
                                uri = compressedUri,
                                originalSize = originalSize,
                                compressedSize = compressedSize,
                                isVideo = true)))
                  } else {
                    continuation.resume(Result.failure(AppError.Plain("Video compression failed")))
                  }
                },
                { log -> Log.d("ffmpegLog", log.message) }) {}
          } else {
            try {
              val originalSize = getUriDataSize(context, localUrl)
              val inputStream =
                  context.contentResolver.openInputStream(localUrl)
                      ?: throw AppError.Plain("Failed to open input stream for URI: $localUrl")
              val bitmap = BitmapFactory.decodeStream(inputStream)
              val data = compressBitmap(resizeBitmap(bitmap))
              if (data.size > 2 * 1024 * 1024) {
                continuation.resume(
                    Result.failure(AppError.Plain("Unable to compress image below 2MB")))
                return@suspendCancellableCoroutine
              }

              val tempFile = File.createTempFile("compressed_image", ".tmp", context.cacheDir)
              FileOutputStream(tempFile).use { fos -> fos.write(data) }
              val compressedUri = Uri.fromFile(tempFile)
              val compressedSize = getUriDataSize(context, compressedUri)

              continuation.resume(
                  Result.success(
                      CompressedResult(
                          uri = compressedUri,
                          originalSize = originalSize,
                          compressedSize = compressedSize,
                          isVideo = false)))
            } catch (e: Exception) {
              continuation.resume(Result.failure(e))
            }
          }
        }
      }

  private fun resizeBitmap(bitmap: Bitmap): Bitmap {
    return if (bitmap.width >= 2000 || bitmap.height >= 2000) {
      val maxDimension = maxOf(bitmap.width, bitmap.height)
      val scale = maxDimension / 1000
      val newWidth = bitmap.width / scale
      val newHeight = bitmap.height / scale
      Bitmap.createScaledBitmap(bitmap, newWidth, newHeight, true)
    } else {
      bitmap
    }
  }

  private fun compressBitmap(bitmap: Bitmap): ByteArray {
    val outputStream = ByteArrayOutputStream()
    var quality = 100
    var data: ByteArray
    do {
      outputStream.reset()
      bitmap.compress(Bitmap.CompressFormat.JPEG, quality, outputStream)
      data = outputStream.toByteArray()
      quality -= 5
    } while (data.size > 2 * 1024 * 1024 && quality > 0)
    return data
  }

  private fun getFilePathFromUri(context: Context, uri: Uri): String? {
    val cursor = context.contentResolver.query(uri, null, null, null, null) ?: return null
    return cursor.use {
      if (it.moveToFirst()) {
        val index = it.getColumnIndex(MediaStore.MediaColumns.DATA)
        if (index != -1) it.getString(index) else null
      } else null
    }
  }

  private fun getUriDataSize(context: Context, uri: Uri): Long {
    return try {
      val inputStream: InputStream? = context.contentResolver.openInputStream(uri)
      inputStream?.use { it.available().toLong() } ?: 0L
    } catch (e: Exception) {
      0L
    }
  }
}

data class CompressedResult(
  val uri: Uri,
  val originalSize: Long,
  val compressedSize: Long,
  val isVideo: Boolean
)
