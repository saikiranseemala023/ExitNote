package uk.ac.tees.mad.exitnote.ui.screens.home

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material.icons.filled.LocationOn
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material.icons.outlined.Circle
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.Switch
import androidx.compose.material3.SwitchDefaults
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import uk.ac.tees.mad.exitnote.viewmodel.ExitNoteViewModel
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

@Composable
fun HomeScreen(
    viewModel: ExitNoteViewModel,
    onNavigateToSettings: () -> Unit,
    onNavigateToSetup: () -> Unit
) {
    val homeLocationState by viewModel.homeLocationState.collectAsStateWithLifecycle()
    val userState by viewModel.userState.collectAsStateWithLifecycle()
    val isTrackingEnabled by viewModel.isTrackingEnabled.collectAsStateWithLifecycle()
    val lastExitTime by viewModel.lastExitTime.collectAsStateWithLifecycle()

    HomeScreenContent(
        userEmail = userState.email,
        isTrackingEnabled = isTrackingEnabled,
        isHomeLocationSet = homeLocationState.isSet,
        latitude = homeLocationState.latitude,
        longitude = homeLocationState.longitude,
        radius = homeLocationState.radius,
        locationName = homeLocationState.locationName,
        lastExitTime = lastExitTime,
        onToggleTracking = { viewModel.toggleLocationTracking(it) },
        onNavigateToSettings = onNavigateToSettings,
        onNavigateToSetup = onNavigateToSetup
    )
}

@Composable
private fun HomeScreenContent(
    userEmail: String?,
    isTrackingEnabled: Boolean,
    isHomeLocationSet: Boolean,
    latitude: Double?,
    longitude: Double?,
    radius: Float,
    locationName: String?,
    lastExitTime: Long?,
    onToggleTracking: (Boolean) -> Unit,
    onNavigateToSettings: () -> Unit,
    onNavigateToSetup: () -> Unit
) {
    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(Color(0xFFF5F7FA))
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .verticalScroll(rememberScrollState())
                .padding(20.dp)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column {
                    Text(
                        text = "Home",
                        fontSize = 28.sp,
                        fontWeight = FontWeight.Bold,
                        color = Color(0xFF2C3E50)
                    )

                    if (userEmail != null) {
                        Text(
                            text = userEmail,
                            fontSize = 14.sp,
                            color = Color(0xFF7F8C8D)
                        )
                    }
                }

                IconButton(
                    onClick = onNavigateToSettings,
                    modifier = Modifier
                        .size(48.dp)
                        .clip(CircleShape)
                        .background(Color.White)
                ) {
                    Icon(
                        imageVector = Icons.Default.Settings,
                        contentDescription = "Settings",
                        tint = Color(0xFF6B7FD7)
                    )
                }
            }

            Spacer(modifier = Modifier.height(24.dp))

            Card(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(16.dp),
                colors = CardDefaults.cardColors(
                    containerColor = Color.White
                ),
                elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
            ) {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(20.dp),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    Box(
                        modifier = Modifier
                            .size(80.dp)
                            .clip(CircleShape)
                            .background(
                                if (isTrackingEnabled)
                                    Color(0xFF27AE60).copy(alpha = 0.1f)
                                else
                                    Color(0xFFE74C3C).copy(alpha = 0.1f)
                            ),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(
                            imageVector = if (isTrackingEnabled)
                                Icons.Filled.CheckCircle
                            else
                                Icons.Outlined.Circle,
                            contentDescription = null,
                            modifier = Modifier.size(40.dp),
                            tint = if (isTrackingEnabled)
                                Color(0xFF27AE60)
                            else
                                Color(0xFFE74C3C)
                        )
                    }

                    Spacer(modifier = Modifier.height(16.dp))

                    Text(
                        text = if (isTrackingEnabled)
                            "Exit Reminder Active"
                        else
                            "Exit Reminder Inactive",
                        fontSize = 20.sp,
                        fontWeight = FontWeight.Bold,
                        color = Color(0xFF2C3E50),
                        textAlign = TextAlign.Center
                    )

                    Spacer(modifier = Modifier.height(8.dp))

                    Text(
                        text = if (isTrackingEnabled)
                            "You'll be reminded when you leave home"
                        else
                            "Turn on to receive exit reminders",
                        fontSize = 14.sp,
                        color = Color(0xFF7F8C8D),
                        textAlign = TextAlign.Center
                    )
                }
            }

            Spacer(modifier = Modifier.height(20.dp))

            Card(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(16.dp),
                colors = CardDefaults.cardColors(
                    containerColor = Color.White
                ),
                elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
            ) {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(20.dp)
                ) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            text = "Home Location",
                            fontSize = 18.sp,
                            fontWeight = FontWeight.SemiBold,
                            color = Color(0xFF2C3E50)
                        )

                        IconButton(
                            onClick = onNavigateToSetup,
                            modifier = Modifier.size(36.dp)
                        ) {
                            Icon(
                                imageVector = Icons.Default.Edit,
                                contentDescription = "Edit Location",
                                tint = Color(0xFF6B7FD7)
                            )
                        }
                    }

                    Spacer(modifier = Modifier.height(12.dp))

                    if (isHomeLocationSet && latitude != null && longitude != null) {
                        LocationDetailRow(
                            label = "Latitude",
                            value = String.format("%.6f", latitude)
                        )

                        Spacer(modifier = Modifier.height(8.dp))

                        LocationDetailRow(
                            label = "Longitude",
                            value = String.format("%.6f", longitude)
                        )

                        Spacer(modifier = Modifier.height(8.dp))

                        LocationDetailRow(
                            label = "Radius",
                            value = "${radius.toInt()} meters"
                        )

                        if (locationName != null) {
                            Spacer(modifier = Modifier.height(8.dp))

                            LocationDetailRow(
                                label = "Area",
                                value = locationName
                            )
                        }
                    } else {
                        Row(
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Icon(
                                imageVector = Icons.Default.LocationOn,
                                contentDescription = null,
                                tint = Color(0xFF7F8C8D),
                                modifier = Modifier.size(20.dp)
                            )
                            Spacer(modifier = Modifier.width(8.dp))
                            Text(
                                text = "No home location set",
                                fontSize = 14.sp,
                                color = Color(0xFF7F8C8D)
                            )
                        }

                        Spacer(modifier = Modifier.height(12.dp))

                        TextButton(onClick = onNavigateToSetup) {
                            Text(
                                text = "Set Home Location",
                                color = Color(0xFF6B7FD7),
                                fontWeight = FontWeight.SemiBold
                            )
                        }
                    }
                }
            }

            Spacer(modifier = Modifier.height(20.dp))

            Card(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(16.dp),
                colors = CardDefaults.cardColors(
                    containerColor = Color.White
                ),
                elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
            ) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(20.dp),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Column(modifier = Modifier.weight(1f)) {
                        Text(
                            text = "Enable Exit Reminders",
                            fontSize = 16.sp,
                            fontWeight = FontWeight.SemiBold,
                            color = Color(0xFF2C3E50)
                        )
                        Spacer(modifier = Modifier.height(4.dp))
                        Text(
                            text = "Get notified when you leave home",
                            fontSize = 13.sp,
                            color = Color(0xFF7F8C8D)
                        )
                    }

                    Spacer(modifier = Modifier.width(12.dp))

                    Switch(
                        checked = isTrackingEnabled,
                        onCheckedChange = onToggleTracking,
                        colors = SwitchDefaults.colors(
                            checkedThumbColor = Color.White,
                            checkedTrackColor = Color(0xFF6B7FD7),
                            uncheckedThumbColor = Color.White,
                            uncheckedTrackColor = Color(0xFFBDC3C7)
                        ),
                        enabled = isHomeLocationSet
                    )
                }
            }

            AnimatedVisibility(
                visible = lastExitTime != null,
                enter = fadeIn(),
                exit = fadeOut()
            ) {
                Column {
                    Spacer(modifier = Modifier.height(20.dp))

                    Card(
                        modifier = Modifier.fillMaxWidth(),
                        shape = RoundedCornerShape(16.dp),
                        colors = CardDefaults.cardColors(
                            containerColor = Color.White
                        ),
                        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
                    ) {
                        Column(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(20.dp)
                        ) {
                            Text(
                                text = "Last Exit",
                                fontSize = 16.sp,
                                fontWeight = FontWeight.SemiBold,
                                color = Color(0xFF2C3E50)
                            )

                            Spacer(modifier = Modifier.height(8.dp))

                            Text(
                                text = formatTimestamp(lastExitTime ?: 0L),
                                fontSize = 14.sp,
                                color = Color(0xFF7F8C8D)
                            )
                        }
                    }
                }
            }

            Spacer(modifier = Modifier.height(20.dp))

            Card(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(16.dp),
                colors = CardDefaults.cardColors(
                    containerColor = Color(0xFFE8ECFF)
                ),
                elevation = CardDefaults.cardElevation(defaultElevation = 0.dp)
            ) {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(16.dp)
                ) {
                    Text(
                        text = "💡 How it works",
                        fontSize = 15.sp,
                        fontWeight = FontWeight.SemiBold,
                        color = Color(0xFF2C3E50)
                    )

                    Spacer(modifier = Modifier.height(8.dp))

                    Text(
                        text = "When you leave your home area, you'll receive a notification asking \"Anything you forgot?\" This helps you remember important items before you go.",
                        fontSize = 13.sp,
                        color = Color(0xFF5D6D7E),
                        lineHeight = 18.sp
                    )
                }
            }

            Spacer(modifier = Modifier.height(32.dp))
        }
    }
}

@Composable
private fun LocationDetailRow(
    label: String,
    value: String
) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Text(
            text = label,
            fontSize = 14.sp,
            color = Color(0xFF7F8C8D)
        )

        Text(
            text = value,
            fontSize = 14.sp,
            fontWeight = FontWeight.Medium,
            color = Color(0xFF2C3E50)
        )
    }
}

private fun formatTimestamp(timestamp: Long): String {
    val sdf = SimpleDateFormat("MMM dd, yyyy 'at' hh:mm a", Locale.getDefault())
    return sdf.format(Date(timestamp))
}

@Preview(showBackground = true, name = "Exit Note – Home Screen (Active)")
@Composable
fun HomeScreenActivePreview() {
    HomeScreenContent(
        userEmail = "user@example.com",
        isTrackingEnabled = true,
        isHomeLocationSet = true,
        latitude = 40.712776,
        longitude = -74.005974,
        radius = 250f,
        locationName = "New York, NY",
        lastExitTime = System.currentTimeMillis() - 3600000,
        onToggleTracking = {},
        onNavigateToSettings = {},
        onNavigateToSetup = {}
    )
}

@Preview(showBackground = true, name = "Exit Note – Home Screen (Inactive)")
@Composable
fun HomeScreenInactivePreview() {
    HomeScreenContent(
        userEmail = "user@example.com",
        isTrackingEnabled = false,
        isHomeLocationSet = true,
        latitude = 28.6139,
        longitude = 77.2090,
        radius = 300f,
        locationName = "Delhi, India",
        lastExitTime = null,
        onToggleTracking = {},
        onNavigateToSettings = {},
        onNavigateToSetup = {}
    )
}

@Preview(showBackground = true, name = "Exit Note – Home Screen (No Location)")
@Composable
fun HomeScreenNoLocationPreview() {
    HomeScreenContent(
        userEmail = "newuser@example.com",
        isTrackingEnabled = false,
        isHomeLocationSet = false,
        latitude = null,
        longitude = null,
        radius = 250f,
        locationName = null,
        lastExitTime = null,
        onToggleTracking = {},
        onNavigateToSettings = {},
        onNavigateToSetup = {}
    )
}