package dev.horizon.feature.mission

import androidx.compose.foundation.background
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Text
import androidx.compose.ui.unit.sp
import androidx.compose.foundation.layout.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.unit.dp
import androidx.compose.ui.Modifier
import dev.horizon.core.design.*

@Composable
fun ScienceScreen(back: () -> Unit) {
    Page("About", "About the science", back) {
            Copy("Light exposure changes with intensity, timing and duration. A dimmer screen may feel more comfortable in a dark room. The surrounding room light matters too.")
            Copy("Evidence that changing a phone’s screen colour improves sleep is mixed. Choose the tint that feels comfortable.")
            Copy("Brown et al. (2022). Recommendations for daytime, evening, and nighttime indoor light exposure. PLOS Biology. doi:10.1371/journal.pbio.3001571")
            Copy("Duraccio et al. (2021). Does iPhone night shift mitigate negative effects of smartphone use on sleep outcomes in emerging adults? Sleep Health. doi:10.1016/j.sleh.2021.03.005")
        Action("Back to the app", Modifier.fillMaxWidth(), click = back)
    }
}
