package app.akiho.media_compressor.extension

import android.net.Uri

fun String.removeQueryFromUrl(): String {
  val uri = Uri.parse(this)
  val newUri = uri.buildUpon().clearQuery().build()
  return newUri.toString()
}

fun String.url(): Uri {
  return Uri.parse(this)
}
