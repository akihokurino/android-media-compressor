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
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.AssistChip
import androidx.compose.material3.AssistChipDefaults
import androidx.compose.material3.Checkbox
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
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
import app.akiho.media_compressor.infra.compressor.CompressedResult
import app.akiho.media_compressor.model.LocalAsset
import app.akiho.media_compressor.ui.component.AssetViewer
import app.akiho.media_compressor.ui.component.AssetViewerResource
import app.akiho.media_compressor.ui.component.CustomModalBottomSheet
import app.akiho.media_compressor.ui.component.CustomSpacer
import app.akiho.media_compressor.ui.component.ErrorDialog
import app.akiho.media_compressor.ui.component.Hud
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
  val assets by viewModel.assets.collectAsState()
  val selected by viewModel.selected.collectAsState()
  val showHud by viewModel.observeShowHud.collectAsState()
  val error by viewModel.observeError.collectAsState()
  val compressed by viewModel.compressed.collectAsState()
  var hasImagePermission by remember { mutableStateOf(false) }
  var hasVideoPermission by remember { mutableStateOf(false) }
  val showAssetViewer = remember { mutableStateOf(false) }
  var detailAsset by remember { mutableStateOf<LocalAsset?>(null) }
  val listState = rememberLazyListState()
  val coroutineScope = rememberCoroutineScope()

  val requestPermissionLauncher =
      rememberLauncherForActivityResult(
          contract = ActivityResultContracts.RequestMultiplePermissions()) { permissions ->
            hasImagePermission = permissions[Manifest.permission.READ_MEDIA_IMAGES] ?: false
            hasVideoPermission = permissions[Manifest.permission.READ_MEDIA_VIDEO] ?: false

            if (hasImagePermission || hasVideoPermission) {
              viewModel.load()
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
      viewModel.load()
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

    viewModel.initialize()
  }

  Scaffold(
      topBar = {
        TopAppBar(
            title = { Text(text = "メディア") },
            actions = {
              TextButton(enabled = selected != null, onClick = { viewModel.compress() }) {
                Text(text = "圧縮")
              }
            },
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
                                      detailAsset = asset
                                      showAssetViewer.value = true
                                    },
                                contentScale = ContentScale.Crop)

                            Checkbox(
                                checked = selected == asset,
                                onCheckedChange = { v ->
                                  if (v) {
                                    viewModel.select(asset)
                                  } else {
                                    viewModel.select(null)
                                  }
                                },
                                modifier = Modifier.align(Alignment.TopEnd))

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
                current = detailAsset!!,
                getResource = { v ->
                  AssetViewerResource(
                      url = v.url,
                      isVideo = v.isVideo,
                      videoStartSecond = 0,
                      videoEndSecond = 15,
                      isSelected = selected == v)
                },
                onSelect = { viewModel.select(it) },
                onChange = { v ->
                  val index = assets.indexOfFirst { it.items.contains(v) }
                  coroutineScope.launch { listState.animateScrollToItem(index) }
                }) {
                  detailAsset = null
                  showAssetViewer.value = false
                }
          }

          Hud(isLoading = showHud)
          ErrorDialog(error = error)

          compressed?.let { CompressedDialog(it) { viewModel.clear() } }
        }
      })
}

@Composable
fun CompressedDialog(v: CompressedResult, onClose: () -> Unit) {
  AlertDialog(
      onDismissRequest = { onClose() },
      title = { Text(text = "圧縮が完了しました") },
      text = {
        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            modifier = Modifier.padding(vertical = 32.dp)) {
              Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                ImageView(
                    url = v.uri,
                    modifier = Modifier.align(Alignment.Center),
                    contentScale = ContentScale.FillWidth)
              }
            }
      },
      confirmButton = { TextButton(onClick = { onClose() }) { Text("OK") } })
}
