package app.akiho.media_compressor.model

import android.content.Context
import android.net.Uri
import android.provider.MediaStore
import androidx.exifinterface.media.ExifInterface
import java.util.Calendar
import java.util.Date

data class LocalAsset(
    val id: String,
    val url: Uri,
    val date: Date,
    val size: Size,
    val isVideo: Boolean,
    val videoDurationSecond: Long?,
) : HasDate, HasSize {
  override fun mustDate(): Date = date

  override fun mustSize(): Size = size

  override fun equals(other: Any?): Boolean {
    if (this === other) return true
    if (other !is LocalAsset) return false
    return id == other.id
  }

  override fun hashCode(): Int {
    return id.hashCode()
  }

  fun latLng(context: Context): LatLng {
    return context.contentResolver.openInputStream(MediaStore.setRequireOriginal(url))?.let {
        inputStream ->
      val result = ExifInterface(inputStream).getLatLong()
      result?.let { LatLng(it[0], it[1]) }
    } ?: LatLng(0.0, 0.0)
  }
}

interface HasSize {
  fun mustSize(): Size
}

interface HasDate {
  fun mustDate(): Date
}

interface HasLatLng {
  fun getLatLng(): LatLng
}

data class LatLng(val lat: Double, val lng: Double) {
  fun isEmpty(): Boolean {
    return lat == 0.0 && lng == 0.0
  }
}

data class Size(val width: Double, val height: Double)

data class DateGroup<T : HasDate>(val date: Date, val items: List<T>) {
  companion object {
    fun <T : HasDate> from(items: List<T>): List<DateGroup<T>> {
      return items
          .groupBy { item ->
            val calendar =
                Calendar.getInstance().apply {
                  time = item.mustDate()
                  set(Calendar.HOUR_OF_DAY, 0)
                  set(Calendar.MINUTE, 0)
                  set(Calendar.SECOND, 0)
                  set(Calendar.MILLISECOND, 0)
                }
            calendar.time
          }
          .map { (date, items) -> DateGroup(date, items) }
          .sortedByDescending { it.date }
    }
  }
}
