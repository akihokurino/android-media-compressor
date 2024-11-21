package app.akiho.media_compressor.ui

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import app.akiho.media_compressor.model.AppError
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch

open class BaseViewModel(eventBus: EventBus) : ViewModel() {
  protected val error = MutableStateFlow<AppError?>(null)
  protected val showHud = MutableStateFlow(false)

  init {
    viewModelScope.launch { eventBus.observeClearError { error.value = null } }
  }

  val observeError: StateFlow<AppError?>
    get() = error

  val observeShowHud: StateFlow<Boolean>
    get() = showHud
}
