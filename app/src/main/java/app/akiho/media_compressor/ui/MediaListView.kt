package app.akiho.media_compressor.ui

import android.Manifest
import android.content.pm.PackageManager
import android.os.Build
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.annotation.RequiresApi
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.material3.AssistChip
import androidx.compose.material3.AssistChipDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import androidx.core.content.ContextCompat
import androidx.hilt.navigation.compose.hiltViewModel
import app.akiho.media_compressor.extension.displayDate
import app.akiho.media_compressor.model.LocalAsset
import app.akiho.media_compressor.ui.component.AssetViewer
import app.akiho.media_compressor.ui.component.AssetViewerResource
import app.akiho.media_compressor.ui.component.CustomModalBottomSheet
import app.akiho.media_compressor.ui.component.CustomSpacer
import app.akiho.media_compressor.ui.component.ImageView
import app.akiho.media_compressor.ui.component.Spacer12
import app.akiho.media_compressor.ui.component.Spacer20
import kotlinx.coroutines.launch

@OptIn(ExperimentalMaterial3Api::class)
@RequiresApi(Build.VERSION_CODES.TIRAMISU)
@Composable
fun MediaListView(viewModel: MediaListViewModel = hiltViewModel()) {
  val context = LocalContext.current
  val displayMetrics = context.resources.displayMetrics
  val density = displayMetrics.density
  val windowWidth = displayMetrics.widthPixels / density
  val assets by viewModel.localAssets.collectAsState()
  var hasImagePermission by remember { mutableStateOf(false) }
  var hasVideoPermission by remember { mutableStateOf(false) }
  val showAssetViewer = remember { mutableStateOf(false) }
  var selectAsset by remember { mutableStateOf<LocalAsset?>(null) }
  val listState = rememberLazyListState()
  val coroutineScope = rememberCoroutineScope()

  val requestPermissionLauncher =
      rememberLauncherForActivityResult(
          contract = ActivityResultContracts.RequestMultiplePermissions()) { permissions ->
            hasImagePermission = permissions[Manifest.permission.READ_MEDIA_IMAGES] ?: false
            hasVideoPermission = permissions[Manifest.permission.READ_MEDIA_VIDEO] ?: false

            if (hasImagePermission || hasVideoPermission) {
              viewModel.loadLocalAssets()
            }
          }

  LaunchedEffect(Unit) {
    hasImagePermission =
        ContextCompat.checkSelfPermission(context, Manifest.permission.READ_MEDIA_IMAGES) ==
            PackageManager.PERMISSION_GRANTED

    hasVideoPermission =
        ContextCompat.checkSelfPermission(context, Manifest.permission.READ_MEDIA_VIDEO) ==
            PackageManager.PERMISSION_GRANTED

    if (hasImagePermission || hasVideoPermission) {
      viewModel.loadLocalAssets()
    }

    val permissionsToRequest = mutableListOf<String>()
    if (!hasImagePermission) {
      permissionsToRequest.add(Manifest.permission.READ_MEDIA_IMAGES)
    }
    if (!hasVideoPermission) {
      permissionsToRequest.add(Manifest.permission.READ_MEDIA_VIDEO)
    }
    permissionsToRequest.add(Manifest.permission.ACCESS_MEDIA_LOCATION)

    if (permissionsToRequest.isNotEmpty()) {
      requestPermissionLauncher.launch(permissionsToRequest.toTypedArray())
    }
  }

  Scaffold(
      topBar = {
        TopAppBar(
            title = { Text(text = "メディア") },
        )
      },
      content = { innerPadding ->
        Box(modifier = Modifier.padding(innerPadding)) {
          LazyColumn(state = listState) {
            items(assets) { group ->
              Column {
                Spacer20()
                Row(
                    modifier = Modifier.padding(horizontal = 16.dp),
                    verticalAlignment = Alignment.CenterVertically) {
                      Text(group.date.displayDate())
                      Spacer(modifier = Modifier.weight(1f))
                    }
                Spacer12()
                group.items.chunked(3).forEach { rowAssets ->
                  Row(
                      modifier = Modifier.fillMaxWidth(),
                      horizontalArrangement = Arrangement.spacedBy(1.dp)) {
                        rowAssets.forEach { asset ->
                          Box(modifier = Modifier.size((windowWidth / 3).dp)) {
                            ImageView(
                                url = asset.url,
                                modifier =
                                    Modifier.size((windowWidth / 3).dp).clickable {
                                      selectAsset = asset
                                      showAssetViewer.value = true
                                    },
                                contentScale = ContentScale.Crop)

                            asset.videoDurationSecond?.let { second ->
                              AssistChip(
                                  modifier =
                                      Modifier.align(Alignment.BottomEnd)
                                          .padding(horizontal = 16.dp),
                                  colors =
                                      AssistChipDefaults.assistChipColors(
                                          containerColor = MaterialTheme.colorScheme.background),
                                  onClick = {},
                                  label = { Text("${second}S") })
                            }
                          }
                        }
                        repeat(3 - rowAssets.size) {
                          Spacer(modifier = Modifier.size((windowWidth / 3).dp))
                        }
                      }
                  CustomSpacer(size = 1)
                }
              }
            }
          }

          CustomModalBottomSheet(showBottomSheet = showAssetViewer, withHeader = false) {
            AssetViewer(
                items = assets.fold(listOf()) { acc, bundle -> acc + bundle.items },
                current = selectAsset!!,
                getResource = { v ->
                  AssetViewerResource(
                      url = v.url,
                      isVideo = v.isVideo,
                      videoStartSecond = 0,
                      videoEndSecond = 15,
                      isSelected = false)
                },
                onSelect = {},
                onChange = { v ->
                  val index = assets.indexOfFirst { it.items.contains(v) }
                  coroutineScope.launch { listState.animateScrollToItem(index) }
                }) {
                  selectAsset = null
                  showAssetViewer.value = false
                }
          }
        }
      })
}
