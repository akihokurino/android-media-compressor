package app.akiho.media_compressor.ui.component

import android.net.Uri
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.wrapContentSize
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material3.AssistChip
import androidx.compose.material3.AssistChipDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableLongStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.unit.dp
import androidx.compose.ui.viewinterop.AndroidView
import androidx.media3.common.MediaItem
import androidx.media3.exoplayer.ExoPlayer
import androidx.media3.ui.PlayerView
import app.akiho.media_compressor.R
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale
import kotlinx.coroutines.delay

@Composable
fun VideoView(
    url: Uri,
    modifier: Modifier = Modifier,
    isMute: Boolean = true,
    startSeconds: Long = 0,
    endSeconds: Long = 15,
    isShowDuration: Boolean = true,
    isEnablePause: Boolean = false,
) {
  val context = LocalContext.current
  var currentTime by remember { mutableLongStateOf(0L) }
  var isPlaying by remember { mutableStateOf(false) }
  val exoPlayer = remember {
    ExoPlayer.Builder(context).build().apply {
      setMediaItem(MediaItem.fromUri(url))
      repeatMode = ExoPlayer.REPEAT_MODE_ALL
      volume = if (isMute) 0f else 0.5f
    }
  }

  DisposableEffect(Unit) {
    exoPlayer.apply {
      prepare()
      playWhenReady = true
    }
    isPlaying = true

    onDispose {
      exoPlayer.apply {
        playWhenReady = false
        clearVideoSurface()
        release()
      }
      isPlaying = false
    }
  }

  LaunchedEffect(startSeconds, endSeconds) {
    exoPlayer.seekTo(startSeconds * 1000)

    while (true) {
      val currentPosition = exoPlayer.currentPosition
      if (currentPosition >= endSeconds * 1000) {
        exoPlayer.seekTo(startSeconds * 1000)
      }

      currentTime = currentPosition

      delay(500L)
    }
  }

  Box(modifier = modifier, contentAlignment = Alignment.Center) {
    AndroidView(
        factory = {
          PlayerView(it).apply {
            useController = false
            player = exoPlayer
          }
        },
        modifier =
            Modifier.fillMaxWidth().wrapContentSize().clickable {
              if (isEnablePause) {
                if (isPlaying) {
                  exoPlayer.playWhenReady = false
                  exoPlayer.pause()
                  isPlaying = false
                } else {
                  exoPlayer.playWhenReady = true
                  exoPlayer.play()
                  isPlaying = true
                }
              }
            })

    if (isShowDuration) {
      AssistChip(
          modifier = Modifier.align(Alignment.TopStart).padding(horizontal = 16.dp),
          colors =
              AssistChipDefaults.assistChipColors(
                  containerColor = MaterialTheme.colorScheme.background),
          onClick = {},
          label = { Text(SimpleDateFormat("mm:ss", Locale.JAPAN).format(Date(currentTime))) },
          leadingIcon = {
            if (isMute) {
              Icon(
                  painter = painterResource(id = R.drawable.ic_volume_off),
                  contentDescription = null)
            }
          })
    }

    if (isEnablePause && !isPlaying) {
      IconButton(
          onClick = {
            exoPlayer.playWhenReady = true
            exoPlayer.play()
            isPlaying = true
          },
          modifier =
              Modifier.align(Alignment.Center)
                  .size(50.dp)
                  .clip(CircleShape)
                  .background(MaterialTheme.colorScheme.primaryContainer)) {
            Icon(imageVector = Icons.Default.PlayArrow, contentDescription = null)
          }
    }
  }
}
