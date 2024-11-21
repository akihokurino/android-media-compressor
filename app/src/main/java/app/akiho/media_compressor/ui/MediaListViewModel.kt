package app.akiho.media_compressor.ui

import androidx.lifecycle.viewModelScope
import app.akiho.media_compressor.infra.media.MediaStoreClient
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
) : BaseViewModel() {
  private val _localAssets = MutableStateFlow<List<DateGroup<LocalAsset>>>(emptyList())
  val localAssets: StateFlow<List<DateGroup<LocalAsset>>>
    get() = _localAssets

  fun initialize() {
    showHud.value = true

    viewModelScope.launch { showHud.value = false }
  }

  fun loadLocalAssets() {
    viewModelScope.launch {
      val assets = mediaStoreClient.getAssets(true)
      _localAssets.value = DateGroup.from(assets)
    }
  }
}
