package app.akiho.media_compressor.ui

import javax.inject.Inject
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.collectLatest

class EventBus @Inject constructor() {
  private val clearError = MutableSharedFlow<Unit>()

  suspend fun notifyClearError() {
    clearError.emit(Unit)
  }

  suspend fun observeClearError(fn: () -> Unit) {
    clearError.collectLatest { fn() }
  }
}
