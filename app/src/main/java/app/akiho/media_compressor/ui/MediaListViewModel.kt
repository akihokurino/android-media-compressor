package app.akiho.media_compressor.ui

import androidx.lifecycle.viewModelScope
import app.akiho.media_compressor.infra.compressor.CompressedResult
import app.akiho.media_compressor.infra.compressor.CompressorClient
import app.akiho.media_compressor.infra.media.MediaStoreClient
import app.akiho.media_compressor.model.AppError
import app.akiho.media_compressor.model.DateGroup
import app.akiho.media_compressor.model.LocalAsset
import dagger.hilt.android.lifecycle.HiltViewModel
import javax.inject.Inject
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch

@HiltViewModel
open class MediaListViewModel
@Inject
constructor(
    private val mediaStoreClient: MediaStoreClient,
    private val compressorClient: CompressorClient,
    eventBus: EventBus,
) : BaseViewModel(eventBus) {
  private val _assets = MutableStateFlow<List<DateGroup<LocalAsset>>>(emptyList())
  val assets: StateFlow<List<DateGroup<LocalAsset>>>
    get() = _assets

  private val _selected = MutableStateFlow<LocalAsset?>(null)
  val selected: StateFlow<LocalAsset?>
    get() = _selected

  private val _compressed = MutableStateFlow<CompressedResult?>(null)
  val compressed: StateFlow<CompressedResult?>
    get() = _compressed

  fun initialize() {
    showHud.value = true
    viewModelScope.launch { showHud.value = false }
  }

  fun load() {
    viewModelScope.launch {
      val assets = mediaStoreClient.getAssets(true)
      _assets.value = DateGroup.from(assets)
    }
  }

  fun select(asset: LocalAsset?) {
    _selected.value = asset
  }

  fun compress() {
    viewModelScope.launch {
      val asset = _selected.value ?: return@launch
      showHud.value = true
      try {
        _compressed.value =
            compressorClient.compress(asset.url, asset.videoDurationSecond).getOrThrow()
      } catch (e: Exception) {
        error.value = AppError.from(e as? AppError)
      } finally {
        showHud.value = false
      }
    }
  }

  fun clear() {
    _compressed.value = null
  }
}
