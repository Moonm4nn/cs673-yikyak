package edu.cs673.yikyak_clone

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import edu.cs673.yikyak_clone.ui.navigation.AppNavigation
import edu.cs673.yikyak_clone.ui.theme.YikyakcloneTheme

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            YikyakcloneTheme {
                AppNavigation()
            }
        }
    }
}
