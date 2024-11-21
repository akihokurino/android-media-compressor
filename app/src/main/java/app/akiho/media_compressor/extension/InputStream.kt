package app.akiho.media_compressor.extension

import android.graphics.BitmapFactory
import androidx.compose.ui.graphics.ImageBitmap
import androidx.compose.ui.graphics.asImageBitmap
import androidx.exifinterface.media.ExifInterface
import java.io.ByteArrayInputStream
import java.io.InputStream

fun InputStream.imageBitmap(): ImageBitmap {
  val byteArray = this.readBytes()
  val orientation =
      ExifInterface(ByteArrayInputStream(byteArray))
          .getAttributeInt(ExifInterface.TAG_ORIENTATION, ExifInterface.ORIENTATION_UNDEFINED)
  val bitmap = BitmapFactory.decodeStream(ByteArrayInputStream(byteArray))
  return bitmap.rotate(orientation).asImageBitmap()
}
