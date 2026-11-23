package moc.itivo.officeworks

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import dagger.hilt.android.AndroidEntryPoint
import moc.itivo.officeworks.navigation.AppNavHost
import moc.itivo.officeworks.ui.theme.OfficeworksTheme

@AndroidEntryPoint
class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            OfficeworksTheme {
                AppNavHost()
            }
        }
    }
}
