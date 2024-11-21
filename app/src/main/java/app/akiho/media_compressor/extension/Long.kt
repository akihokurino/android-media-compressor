package app.akiho.media_compressor.extension

import java.util.Locale

fun Long.megaBytes(): String {
  val mb = this.toDouble() / 1_048_576
  return String.format(Locale.JAPAN, "%.2f", mb)
}
