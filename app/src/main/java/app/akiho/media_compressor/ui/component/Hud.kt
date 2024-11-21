package app.akiho.media_compressor.ui.component

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.unit.dp

@Composable
fun Hud(isLoading: Boolean) {
  if (isLoading) {
    Box(
        contentAlignment = Alignment.Center,
        modifier = Modifier.fillMaxSize().clickable(false) {}) {
          Box(
              contentAlignment = Alignment.Center,
              modifier =
                  Modifier.size(80.dp)
                      .clip(RoundedCornerShape(8))
                      .background(MaterialTheme.colorScheme.primaryContainer)) {
                CircularProgressIndicator()
              }
        }
  }
}
