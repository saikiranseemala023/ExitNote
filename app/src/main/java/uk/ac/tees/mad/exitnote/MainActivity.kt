package uk.ac.tees.mad.exitnote

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.tooling.preview.Preview
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.compose.rememberNavController
import uk.ac.tees.mad.exitnote.navigation.ExitNoteNavGraph
import uk.ac.tees.mad.exitnote.ui.screens.splash.SplashScreen
import uk.ac.tees.mad.exitnote.ui.theme.ExitNoteTheme
import uk.ac.tees.mad.exitnote.viewmodel.ExitNoteViewModel

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            ExitNoteTheme {
                val navController = rememberNavController()
                ExitNoteNavGraph(
                    navController
                )
            }
        }
    }
}
