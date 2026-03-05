package uk.ac.tees.mad.exitnote.ui.screens.splash

import androidx.compose.animation.core.Animatable
import androidx.compose.animation.core.tween
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.LocationOn
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import kotlinx.coroutines.delay
import uk.ac.tees.mad.exitnote.viewmodel.ExitNoteViewModel

/**
 * Splash Screen - First screen shown when app launches
 * Displays app logo and tagline, then navigates based on app state
 */
@Composable
fun SplashScreen(
    viewModel: ExitNoteViewModel,
    onNavigateToAuth: () -> Unit,
    onNavigateToHome: () -> Unit,
    onNavigateToSetup: (String?) -> Unit
) {
    val alphaAnimation = remember { Animatable(0f) }

    LaunchedEffect(Unit) {
        // Fade in animation
        alphaAnimation.animateTo(
            targetValue = 1f,
            animationSpec = tween(durationMillis = 1000)
        )

        // Show splash for 2 seconds
        delay(2000)

        // TODO: In next sprint, check actual state from ViewModel
        // For now, navigate to Auth screen
        onNavigateToAuth()
    }

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(Color(0xFFF5F7FA)),
        contentAlignment = Alignment.Center
    ) {
        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            modifier = Modifier
                .alpha(alphaAnimation.value)
                .padding(32.dp)
        ) {
            // App Icon/Logo
            Icon(
                imageVector = Icons.Outlined.LocationOn,
                contentDescription = "Exit Note Logo",
                modifier = Modifier.size(100.dp),
                tint = Color(0xFF6B7FD7)
            )

            Spacer(modifier = Modifier.height(24.dp))

            // App Name
            Text(
                text = "Exit Note",
                fontSize = 32.sp,
                fontWeight = FontWeight.Bold,
                color = Color(0xFF2C3E50),
                textAlign = TextAlign.Center
            )

            Spacer(modifier = Modifier.height(12.dp))

            // Tagline
            Text(
                text = "One last reminder before you go.",
                fontSize = 16.sp,
                fontWeight = FontWeight.Normal,
                color = Color(0xFF7F8C8D),
                textAlign = TextAlign.Center,
                modifier = Modifier.padding(horizontal = 32.dp)
            )
        }
    }
}

@Preview(showBackground = true, name = "Exit Note – Splash Screen")
@Composable
fun ExitNoteSplashExactPreview() {
    val alphaAnimation = remember { Animatable(0f) }

    LaunchedEffect(Unit) {
        alphaAnimation.animateTo(
            targetValue = 1f,
            animationSpec = tween(durationMillis = 1000)
        )
    }

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(Color(0xFFF5F7FA)),
        contentAlignment = Alignment.Center
    ) {
        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            modifier = Modifier
                .alpha(alphaAnimation.value)
                .padding(32.dp)
        ) {
            Icon(
                imageVector = Icons.Outlined.LocationOn,
                contentDescription = "Exit Note Logo",
                modifier = Modifier.size(100.dp),
                tint = Color(0xFF6B7FD7)
            )

            Spacer(modifier = Modifier.height(24.dp))

            Text(
                text = "Exit Note",
                fontSize = 32.sp,
                fontWeight = FontWeight.Bold,
                color = Color(0xFF2C3E50),
                textAlign = TextAlign.Center
            )

            Spacer(modifier = Modifier.height(12.dp))

            Text(
                text = "One last reminder before you go.",
                fontSize = 16.sp,
                fontWeight = FontWeight.Normal,
                color = Color(0xFF7F8C8D),
                textAlign = TextAlign.Center,
                modifier = Modifier.padding(horizontal = 32.dp)
            )
        }
    }
}