package dev.horizon.feature.mission

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.layout.*
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Text
import androidx.compose.material3.rememberModalBottomSheetState
import androidx.compose.runtime.*
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.unit.*
import dev.horizon.core.design.*
import kotlinx.coroutines.delay

/** Prices come directly from Play so checkout and the sheet always agree. */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun TipSheet(prices: Map<String, String>, message: String, busy: Boolean, received: Boolean,
             purchase: (String) -> Unit, retry: () -> Unit, close: () -> Unit) {
    var selected by rememberSaveable { mutableStateOf<String?>(null) }
    val dismiss by rememberUpdatedState(close)
    LaunchedEffect(received) { if (received) { delay(6000); dismiss() } }
    ModalBottomSheet(onDismissRequest = close, sheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true),
        containerColor = Ground, contentColor = Ink) {
        Column(Modifier.fillMaxWidth().navigationBarsPadding().padding(horizontal = 22.dp, vertical = 12.dp),
            verticalArrangement = Arrangement.spacedBy(14.dp)) {
            if (received) {
                val color = Ember
                Canvas(Modifier.size(64.dp, 34.dp)) {
                    drawArc(color, 180f, 180f, false, Offset(2.dp.toPx(), 2.dp.toPx()),
                        Size(size.width - 4.dp.toPx(), (size.height - 3.dp.toPx()) * 2), style = Stroke(3.dp.toPx(), cap = StrokeCap.Round))
                }
                Text("Thank you.", fontFamily = Outfit, fontSize = 28.sp)
                Copy("That keeps this one alive, and the next one free.")
                Action("Back to the app", Modifier.fillMaxWidth(), click = close)
            } else {
                Text("Keep the lamp on", fontFamily = Outfit, fontSize = 28.sp)
                Copy("You already have every feature. A one-time tip helps me keep building Horizon.")
                if (prices.isNotEmpty()) Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    prices.forEach { (id, price) ->
                        Action(price, Modifier.weight(1f), chosen = selected == id, enabled = !busy) { selected = id }
                    }
                }
                if (message.isNotBlank()) Copy(message)
                if (prices.isEmpty() && !busy && message.startsWith("Tips are unavailable")) {
                    Action("Check again", click = retry)
                }
                Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    Action("I’m good, thanks", Modifier.weight(1f), click = close)
                    Action("Continue", Modifier.weight(1f), enabled = !busy && selected in prices.keys) { selected?.let(purchase) }
                }
                Copy("One time. Handled by Google Play.")
            }
        }
    }
}
