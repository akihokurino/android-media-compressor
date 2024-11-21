package app.akiho.media_compressor.ui.component

import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.rememberCoroutineScope
import androidx.hilt.navigation.compose.hiltViewModel
import app.akiho.media_compressor.model.AppError
import app.akiho.media_compressor.ui.CommonInjector
import kotlinx.coroutines.launch

const val ERROR_DIALOG_TITLE = "エラー"

@Composable
fun ErrorDialog(error: AppError?) {
  val injector: CommonInjector = hiltViewModel()
  val coroutineScope = rememberCoroutineScope()

  error?.let {
    AlertDialog(
        onDismissRequest = { coroutineScope.launch { injector.eventBus.notifyClearError() } },
        title = { Text(text = ERROR_DIALOG_TITLE) },
        text = { Text(text = it.message ?: AppError.DEFAULT) },
        confirmButton = {
          TextButton(onClick = { coroutineScope.launch { injector.eventBus.notifyClearError() } }) {
            Text("OK")
          }
        })
  }
}
