package app.akiho.media_compressor.ui.component

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Close
import androidx.compose.material3.Button
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.Text
import androidx.compose.material3.rememberModalBottomSheetState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.MutableState
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun CustomModalBottomSheet(
    showBottomSheet: MutableState<Boolean>,
    title: String = "",
    withHeader: Boolean = true,
    skipPartiallyExpanded: Boolean = true,
    content: @Composable () -> Unit
) {
  val state = rememberModalBottomSheetState(skipPartiallyExpanded = skipPartiallyExpanded)

  if (showBottomSheet.value) {
    ModalBottomSheet(onDismissRequest = { showBottomSheet.value = false }, sheetState = state) {
      Column {
        if (withHeader) {
          Row(
              modifier = Modifier.padding(horizontal = 16.dp),
              horizontalArrangement = Arrangement.SpaceBetween,
              verticalAlignment = Alignment.CenterVertically) {
                Text(title, fontWeight = FontWeight.Bold)
                Spacer(modifier = Modifier.weight(1f))
                Button(
                    onClick = { showBottomSheet.value = false },
                    modifier = Modifier.size(28.dp).clip(CircleShape),
                    contentPadding = PaddingValues(0.dp)) {
                      Icon(
                          imageVector = Icons.Default.Close,
                          contentDescription = null,
                          modifier = Modifier.size(24.dp))
                    }
              }
          Spacer16()
        }
        content()
      }
    }
  }
}
