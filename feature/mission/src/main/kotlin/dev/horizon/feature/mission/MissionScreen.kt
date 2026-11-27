package dev.horizon.feature.mission

import androidx.compose.foundation.layout.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.unit.dp
import androidx.compose.ui.Modifier
import dev.horizon.core.design.*

@Composable
fun MissionScreen(science: Boolean, back: () -> Unit, tip: () -> Unit) {
    Page(if (science) "About" else "Horizon", if (science) "About the science" else "I build apps that ask for nothing.", back) {
        if (science) {
            Copy("Light exposure changes with intensity, timing and duration. A dimmer screen may feel more comfortable in a dark room. The surrounding room light matters too.")
            Copy("Evidence that changing a phone’s screen colour improves sleep is mixed. Choose the tint that feels comfortable.")
            Copy("Brown et al. (2022). Recommendations for daytime, evening, and nighttime indoor light exposure. PLOS Biology. doi:10.1371/journal.pbio.3001571")
            Copy("Duraccio et al. (2021). Does iPhone night shift mitigate negative effects of smartphone use on sleep outcomes in emerging adults? Sleep Health. doi:10.1016/j.sleh.2021.03.005")
        } else {
            Copy("No ads. No accounts. No tracking. No servers keeping your data, because there are no servers at all.")
            Copy("Your filter settings live on your phone. Google Play handles optional tips.")
            Copy("Works offline. Free for life. Every control is already unlocked.")
        }
        if (science) Action("Back to the app", Modifier.fillMaxWidth(), click = back)
        else Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
            Action("Back to the app", Modifier.weight(1f), click = back)
            Action("Leave a tip", Modifier.weight(1f), click = tip)
        }
    }
}
