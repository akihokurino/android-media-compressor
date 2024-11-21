package app.akiho.media_compressor.infra.media

import android.content.ContentValues
import android.content.Context
import android.graphics.Bitmap
import android.net.Uri
import android.os.Environment
import android.provider.MediaStore
import androidx.compose.ui.graphics.asAndroidBitmap
import app.akiho.media_compressor.infra.http.HttpClient
import app.akiho.media_compressor.model.LocalAsset
import app.akiho.media_compressor.model.Size
import dagger.hilt.android.qualifiers.ApplicationContext
import java.io.OutputStream
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale
import javax.inject.Inject
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext

class MediaStoreClient
@Inject
constructor(@ApplicationContext private val context: Context, private val httpClient: HttpClient) {
  suspend fun getAssets(withVideo: Boolean): List<LocalAsset> =
      withContext(Dispatchers.IO) {
        val assets = mutableListOf<LocalAsset>()

        context.contentResolver
            .query(
                MediaStore.Images.Media.EXTERNAL_CONTENT_URI,
                arrayOf(
                    MediaStore.Images.Media._ID,
                    MediaStore.Images.Media.DATE_TAKEN,
                    MediaStore.Images.Media.WIDTH,
                    MediaStore.Images.Media.HEIGHT,
                ),
                null,
                null,
                null)
            ?.use { cursor ->
              val idColumn = cursor.getColumnIndexOrThrow(MediaStore.Images.Media._ID)
              val dateColumn = cursor.getColumnIndexOrThrow(MediaStore.Images.Media.DATE_TAKEN)
              val widthColumn = cursor.getColumnIndexOrThrow(MediaStore.Images.Media.WIDTH)
              val heightColumn = cursor.getColumnIndexOrThrow(MediaStore.Images.Media.HEIGHT)

              while (cursor.moveToNext()) {
                val id = cursor.getLong(idColumn).toString()
                val dateMillis = cursor.getLong(dateColumn)
                val date = Date(dateMillis)
                val url = Uri.withAppendedPath(MediaStore.Images.Media.EXTERNAL_CONTENT_URI, id)

                val asset =
                    LocalAsset(
                        id,
                        url,
                        date,
                        size =
                            Size(
                                width = cursor.getDouble(widthColumn),
                                height = cursor.getDouble(heightColumn)),
                        isVideo = false,
                        videoDurationSecond = null)
                assets.add(asset)
              }
            }

        if (withVideo) {
          context.contentResolver
              .query(
                  MediaStore.Video.Media.EXTERNAL_CONTENT_URI,
                  arrayOf(
                      MediaStore.Video.Media._ID,
                      MediaStore.Video.Media.DATE_TAKEN,
                      MediaStore.Video.Media.WIDTH,
                      MediaStore.Video.Media.HEIGHT,
                      MediaStore.Video.Media.DURATION),
                  null,
                  null,
                  null)
              ?.use { cursor ->
                val idColumn = cursor.getColumnIndexOrThrow(MediaStore.Video.Media._ID)
                val dateColumn = cursor.getColumnIndexOrThrow(MediaStore.Video.Media.DATE_TAKEN)
                val widthColumn = cursor.getColumnIndexOrThrow(MediaStore.Video.Media.WIDTH)
                val heightColumn = cursor.getColumnIndexOrThrow(MediaStore.Video.Media.HEIGHT)
                val durationColumn = cursor.getColumnIndexOrThrow(MediaStore.Video.Media.DURATION)

                while (cursor.moveToNext()) {
                  val id = cursor.getLong(idColumn).toString()
                  val dateMillis = cursor.getLong(dateColumn)
                  val date = Date(dateMillis)
                  val url = Uri.withAppendedPath(MediaStore.Video.Media.EXTERNAL_CONTENT_URI, id)
                  val duration = cursor.getLong(durationColumn)

                  val asset =
                      LocalAsset(
                          id,
                          url,
                          date,
                          size =
                              Size(
                                  width = cursor.getDouble(widthColumn),
                                  height = cursor.getDouble(heightColumn)),
                          isVideo = true,
                          videoDurationSecond = duration / 1000)
                  assets.add(asset)
                }
              }
        }

        assets.sortedByDescending { it.date }
      }

  suspend fun getImage(assetId: String): LocalAsset? =
      withContext(Dispatchers.IO) {
        val uri = Uri.withAppendedPath(MediaStore.Images.Media.EXTERNAL_CONTENT_URI, assetId)
        context.contentResolver
            .query(
                uri,
                arrayOf(
                    MediaStore.Images.Media._ID,
                    MediaStore.Images.Media.DATE_TAKEN,
                    MediaStore.Images.Media.WIDTH,
                    MediaStore.Images.Media.HEIGHT),
                null,
                null,
                null)
            ?.use { cursor ->
              if (cursor.moveToFirst()) {
                val idColumn = cursor.getColumnIndexOrThrow(MediaStore.Images.Media._ID)
                val dateColumn = cursor.getColumnIndexOrThrow(MediaStore.Images.Media.DATE_TAKEN)
                val widthColumn = cursor.getColumnIndexOrThrow(MediaStore.Images.Media.WIDTH)
                val heightColumn = cursor.getColumnIndexOrThrow(MediaStore.Images.Media.HEIGHT)

                val id = cursor.getLong(idColumn).toString()
                val dateMillis = cursor.getLong(dateColumn)
                val date = Date(dateMillis)
                val url = Uri.withAppendedPath(MediaStore.Images.Media.EXTERNAL_CONTENT_URI, id)

                return@withContext LocalAsset(
                    id,
                    url,
                    date,
                    size =
                        Size(
                            width = cursor.getDouble(widthColumn),
                            height = cursor.getDouble(heightColumn)),
                    isVideo = false,
                    videoDurationSecond = null)
              }
            }
        return@withContext null
      }

  suspend fun getVideo(assetId: String): LocalAsset? =
      withContext(Dispatchers.IO) {
        val uri = Uri.withAppendedPath(MediaStore.Video.Media.EXTERNAL_CONTENT_URI, assetId)
        context.contentResolver
            .query(
                uri,
                arrayOf(
                    MediaStore.Video.Media._ID,
                    MediaStore.Video.Media.DATE_TAKEN,
                    MediaStore.Video.Media.WIDTH,
                    MediaStore.Video.Media.HEIGHT,
                    MediaStore.Video.Media.DURATION),
                null,
                null,
                null)
            ?.use { cursor ->
              if (cursor.moveToFirst()) {
                val idColumn = cursor.getColumnIndexOrThrow(MediaStore.Video.Media._ID)
                val dateColumn = cursor.getColumnIndexOrThrow(MediaStore.Video.Media.DATE_TAKEN)
                val widthColumn = cursor.getColumnIndexOrThrow(MediaStore.Video.Media.WIDTH)
                val heightColumn = cursor.getColumnIndexOrThrow(MediaStore.Video.Media.HEIGHT)
                val durationColumn = cursor.getColumnIndexOrThrow(MediaStore.Video.Media.DURATION)

                val id = cursor.getLong(idColumn).toString()
                val dateMillis = cursor.getLong(dateColumn)
                val date = Date(dateMillis)
                val url = Uri.withAppendedPath(MediaStore.Video.Media.EXTERNAL_CONTENT_URI, id)
                val duration = cursor.getLong(durationColumn)

                return@withContext LocalAsset(
                    id,
                    url,
                    date,
                    size =
                        Size(
                            width = cursor.getDouble(widthColumn),
                            height = cursor.getDouble(heightColumn)),
                    isVideo = true,
                    videoDurationSecond = duration / 1000)
              }
            }
        return@withContext null
      }

  suspend fun saveImage(url: Uri): Boolean {
    var outputStream: OutputStream? = null
    try {
      val bitmap = httpClient.downloadImage(url).getOrThrow().asAndroidBitmap()

      val timeStamp: String =
          SimpleDateFormat("yyyyMMdd_HHmmss", Locale.getDefault()).format(Date())
      val fileName = "IMG_$timeStamp.jpg"

      val contentValues =
          ContentValues().apply {
            put(MediaStore.MediaColumns.DISPLAY_NAME, fileName)
            put(MediaStore.MediaColumns.MIME_TYPE, "image/jpeg")
            put(MediaStore.MediaColumns.RELATIVE_PATH, Environment.DIRECTORY_PICTURES)
          }

      val uri =
          context.contentResolver.insert(
              MediaStore.Images.Media.EXTERNAL_CONTENT_URI, contentValues)

      outputStream = uri?.let { context.contentResolver.openOutputStream(it) }
      outputStream?.let { bitmap.compress(Bitmap.CompressFormat.JPEG, 100, it) }

      return true
    } catch (e: Exception) {
      return false
    } finally {
      outputStream?.close()
    }
  }

  suspend fun saveVideo(url: Uri): Boolean {
    var outputStream: OutputStream? = null
    try {
      val file = httpClient.downloadVideo(url).getOrThrow()

      val timeStamp: String =
          SimpleDateFormat("yyyyMMdd_HHmmss", Locale.getDefault()).format(Date())
      val fileName = "VID_$timeStamp.mp4"

      val contentValues =
          ContentValues().apply {
            put(MediaStore.MediaColumns.DISPLAY_NAME, fileName)
            put(MediaStore.MediaColumns.MIME_TYPE, "video/mp4")
            put(MediaStore.MediaColumns.RELATIVE_PATH, Environment.DIRECTORY_MOVIES)
          }

      val uri =
          context.contentResolver.insert(MediaStore.Video.Media.EXTERNAL_CONTENT_URI, contentValues)

      outputStream = uri?.let { context.contentResolver.openOutputStream(it) }
      file.inputStream().use { input -> outputStream?.use { output -> input.copyTo(output) } }

      return true
    } catch (e: Exception) {
      e.printStackTrace()
      return false
    } finally {
      outputStream?.close()
    }
  }
}
