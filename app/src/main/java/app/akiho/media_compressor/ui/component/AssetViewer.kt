package app.akiho.media_compressor.ui.component

import android.net.Uri
import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.pager.HorizontalPager
import androidx.compose.foundation.pager.rememberPagerState
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material3.Checkbox
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.unit.dp
import app.akiho.media_compressor.R

@OptIn(ExperimentalFoundationApi::class, ExperimentalMaterial3Api::class)
@Composable
fun <T> AssetViewer(
    items: List<T>,
    current: T,
    getResource: (T) -> AssetViewerResource,
    titleView: @Composable ((T) -> Unit)? = null,
    menu: List<AssetViewerMenu> = emptyList(),
    onSelect: ((T) -> Unit)? = null,
    onChange: (T) -> Unit,
    onClose: (T) -> Unit
) {
  val pagerState =
      rememberPagerState(initialPage = items.indexOf(current), pageCount = { items.count() })
  var expanded by remember { mutableStateOf(false) }

  LaunchedEffect(pagerState.currentPage) { onChange(items[pagerState.currentPage]) }

  Scaffold(
      topBar = {
        TopAppBar(
            title = { titleView?.let { it(items[pagerState.currentPage]) } },
            navigationIcon = {
              IconButton(onClick = { onClose(items[pagerState.currentPage]) }) {
                Icon(imageVector = Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Back")
              }
            },
            actions = {
              onSelect?.let {
                Row(verticalAlignment = Alignment.CenterVertically) {
                  Text("選択")
                  Spacer4()
                  Checkbox(
                      checked = getResource(items[pagerState.currentPage]).isSelected,
                      onCheckedChange = { it(items[pagerState.currentPage]) })
                }
              }

              if (menu.isNotEmpty()) {
                Box(modifier = Modifier.padding(horizontal = 16.dp)) {
                  IconButton(onClick = { expanded = true }, modifier = Modifier.size(20.dp)) {
                    Icon(
                        painter = painterResource(id = R.drawable.ic_more_horiz),
                        contentDescription = null)
                  }

                  DropdownMenu(expanded = expanded, onDismissRequest = { expanded = false }) {
                    menu.map {
                      DropdownMenuItem(
                          text = {
                            Row {
                              Text(
                                  it.title,
                                  color =
                                      if (it.isDestructive) MaterialTheme.colorScheme.error
                                      else Color.Unspecified)
                              Spacer16()
                              Spacer(modifier = Modifier.weight(1f))
                              if (it.isDestructive) {
                                Icon(
                                    painter = painterResource(id = it.resourceId),
                                    contentDescription = null,
                                    tint = MaterialTheme.colorScheme.error)
                              } else {
                                Icon(
                                    painter = painterResource(id = it.resourceId),
                                    contentDescription = null)
                              }
                            }
                          },
                          onClick = {
                            expanded = false
                            it.onClick()
                          })
                    }
                  }
                }
              }
            })
      },
      content = { innerPadding ->
        Box(modifier = Modifier.padding(innerPadding)) {
          HorizontalPager(
              state = pagerState,
              verticalAlignment = Alignment.CenterVertically,
              modifier = Modifier.fillMaxSize()) { index ->
                val resource = getResource(items[index])
                if (resource.isVideo) {
                  VideoView(
                      url = resource.url,
                      modifier = Modifier.fillMaxWidth(),
                      isShowDuration = false)
                } else {
                  val isVisible = pagerState.currentPage == index
                  ZoomableImageView(
                      url = resource.url,
                      isVisible = isVisible,
                      modifier = Modifier.fillMaxWidth(),
                      contentScale = ContentScale.FillWidth)
                }
              }
        }
      })
}

data class AssetViewerResource(val url: Uri, val isVideo: Boolean, val isSelected: Boolean = false)

data class AssetViewerMenu(
    val title: String,
    val resourceId: Int,
    val isDestructive: Boolean = false,
    val onClick: () -> Unit
)
