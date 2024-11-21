package app.akiho.media_compressor.ui.component

import android.net.Uri
import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import app.akiho.media_compressor.extension.removeQueryFromUrl
import coil.compose.rememberAsyncImagePainter
import coil.imageLoader
import coil.request.ImageRequest

@Composable
fun ImageView(
    url: Uri,
    modifier: Modifier = Modifier,
    contentScale: ContentScale = ContentScale.FillWidth,
    cornerRadius: Int = 0
) {
  val context = LocalContext.current
  val painter =
      rememberAsyncImagePainter(
          model =
              ImageRequest.Builder(context)
                  .data(url)
                  .memoryCacheKey(url.toString().removeQueryFromUrl())
                  .diskCacheKey(url.toString().removeQueryFromUrl())
                  .build(),
          imageLoader = context.imageLoader)

  Box(
      modifier = modifier.clip(RoundedCornerShape(cornerRadius)),
      contentAlignment = Alignment.Center) {
        Image(
            painter = painter,
            contentDescription = null,
            modifier = modifier,
            contentScale = contentScale)
      }
}
