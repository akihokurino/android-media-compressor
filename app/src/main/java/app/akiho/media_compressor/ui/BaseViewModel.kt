package app.akiho.media_compressor.ui

import androidx.lifecycle.ViewModel
import app.akiho.media_compressor.model.AppError
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow

open class BaseViewModel : ViewModel() {
  protected val error = MutableStateFlow<AppError?>(null)
  protected val showHud = MutableStateFlow(false)

  init {}

  val observeError: StateFlow<AppError?>
    get() = error

  val observeShowHud: StateFlow<Boolean>
    get() = showHud
}
