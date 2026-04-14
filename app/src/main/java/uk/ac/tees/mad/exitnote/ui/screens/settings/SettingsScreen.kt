package uk.ac.tees.mad.exitnote.ui.screens.settings

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.BugReport
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material.icons.filled.ExitToApp
import androidx.compose.material.icons.filled.Info
import androidx.compose.material.icons.filled.Notifications
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Divider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import uk.ac.tees.mad.exitnote.viewmodel.ExitNoteViewModel

@Composable
fun SettingsScreen(
    viewModel: ExitNoteViewModel,
    onNavigateBack: () -> Unit,
    onNavigateToAuth: () -> Unit
) {
    val userState by viewModel.userState.collectAsStateWithLifecycle()
    val homeLocationState by viewModel.homeLocationState.collectAsStateWithLifecycle()
    val isTrackingEnabled by viewModel.isTrackingEnabled.collectAsStateWithLifecycle()

    var showSignOutDialog by remember { mutableStateOf(false) }
    var showResetDialog by remember { mutableStateOf(false) }
    var showTestDialog by remember { mutableStateOf(false) }

    SettingsContent(
        userEmail = userState.email,
        homeLocationSet = homeLocationState.isSet,
        isTrackingEnabled = isTrackingEnabled,
        onNavigateBack = onNavigateBack,
        onEditLocation = onNavigateBack,
        onTestNotification = {
            viewModel.triggerExitNotification()
            showTestDialog = true
        },
        onManualCheck = {
            viewModel.checkIfOutsideGeofence()
        },
        onSignOut = { showSignOutDialog = true },
        onResetData = { showResetDialog = true }
    )

    if (showSignOutDialog) {
        AlertDialog(
            onDismissRequest = { showSignOutDialog = false },
            title = { Text("Sign Out") },
            text = { Text("Are you sure you want to sign out?") },
            confirmButton = {
                TextButton(
                    onClick = {
                        showSignOutDialog = false
                        viewModel.signOut()
                        onNavigateToAuth()
                    }
                ) {
                    Text("Sign Out", color = Color(0xFFE74C3C))
                }
            },
            dismissButton = {
                TextButton(onClick = { showSignOutDialog = false }) {
                    Text("Cancel")
                }
            }
        )
    }

    if (showResetDialog) {
        AlertDialog(
            onDismissRequest = { showResetDialog = false },
            title = { Text("Reset App Data") },
            text = {
                Text("This will clear all your settings and location data. This action cannot be undone.")
            },
            confirmButton = {
                TextButton(
                    onClick = {
                        showResetDialog = false
                        viewModel.resetAppData()
                    }
                ) {
                    Text("Reset", color = Color(0xFFE74C3C))
                }
            },
            dismissButton = {
                TextButton(onClick = { showResetDialog = false }) {
                    Text("Cancel")
                }
            }
        )
    }

    if (showTestDialog) {
        AlertDialog(
            onDismissRequest = { showTestDialog = false },
            title = { Text("Test Notification Sent") },
            text = { Text("Check your notification tray for the exit reminder notification!") },
            confirmButton = {
                TextButton(onClick = { showTestDialog = false }) {
                    Text("OK")
                }
            }
        )
    }
}

@Composable
private fun SettingsContent(
    userEmail: String?,
    homeLocationSet: Boolean,
    isTrackingEnabled: Boolean,
    onNavigateBack: () -> Unit,
    onEditLocation: () -> Unit,
    onTestNotification: () -> Unit,
    onManualCheck: () -> Unit,
    onSignOut: () -> Unit,
    onResetData: () -> Unit
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
                verticalAlignment = Alignment.CenterVertically
            ) {
                IconButton(
                    onClick = onNavigateBack,
                    modifier = Modifier
                        .size(48.dp)
                        .clip(CircleShape)
                        .background(Color.White)
                ) {
                    Icon(
                        imageVector = Icons.Default.ArrowBack,
                        contentDescription = "Back",
                        tint = Color(0xFF2C3E50)
                    )
                }

                Spacer(modifier = Modifier.size(16.dp))

                Text(
                    text = "Settings",
                    fontSize = 28.sp,
                    fontWeight = FontWeight.Bold,
                    color = Color(0xFF2C3E50)
                )
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
                        .padding(20.dp)
                ) {
                    Text(
                        text = "Account",
                        fontSize = 14.sp,
                        fontWeight = FontWeight.SemiBold,
                        color = Color(0xFF7F8C8D)
                    )

                    Spacer(modifier = Modifier.height(16.dp))

                    if (userEmail != null) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Box(
                                modifier = Modifier
                                    .size(48.dp)
                                    .clip(CircleShape)
                                    .background(Color(0xFFE8ECFF)),
                                contentAlignment = Alignment.Center
                            ) {
                                Text(
                                    text = userEmail.first().uppercase(),
                                    fontSize = 20.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = Color(0xFF6B7FD7)
                                )
                            }

                            Spacer(modifier = Modifier.size(12.dp))

                            Column {
                                Text(
                                    text = "Email",
                                    fontSize = 12.sp,
                                    color = Color(0xFF7F8C8D)
                                )
                                Text(
                                    text = userEmail,
                                    fontSize = 14.sp,
                                    fontWeight = FontWeight.Medium,
                                    color = Color(0xFF2C3E50)
                                )
                            }
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
                Column(
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Text(
                        text = "Location",
                        fontSize = 14.sp,
                        fontWeight = FontWeight.SemiBold,
                        color = Color(0xFF7F8C8D),
                        modifier = Modifier.padding(20.dp, 20.dp, 20.dp, 12.dp)
                    )

                    SettingsItem(
                        icon = Icons.Default.Edit,
                        title = "Edit Home Location",
                        subtitle = if (homeLocationSet) "Update your home location" else "Not set",
                        onClick = onEditLocation
                    )
                }
            }

            Spacer(modifier = Modifier.height(20.dp))

            Card(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(16.dp),
                colors = CardDefaults.cardColors(
                    containerColor = Color(0xFFFFF3E0)
                ),
                elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
            ) {
                Column(
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Text(
                        text = "Testing & Debug",
                        fontSize = 14.sp,
                        fontWeight = FontWeight.SemiBold,
                        color = Color(0xFFE67E22),
                        modifier = Modifier.padding(20.dp, 20.dp, 20.dp, 12.dp)
                    )

                    SettingsItem(
                        icon = Icons.Default.Notifications,
                        title = "Test Notification",
                        subtitle = "Trigger exit reminder notification now",
                        onClick = onTestNotification,
                        textColor = Color(0xFFE67E22)
                    )

                    Divider(
                        color = Color(0xFFFBE9E7),
                        modifier = Modifier.padding(horizontal = 20.dp)
                    )

                    SettingsItem(
                        icon = Icons.Default.BugReport,
                        title = "Check Location Now",
                        subtitle = "Manually check if outside geofence (${if (isTrackingEnabled) "Tracking ON" else "Tracking OFF"})",
                        onClick = onManualCheck,
                        textColor = Color(0xFFE67E22)
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
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Text(
                        text = "App Info",
                        fontSize = 14.sp,
                        fontWeight = FontWeight.SemiBold,
                        color = Color(0xFF7F8C8D),
                        modifier = Modifier.padding(20.dp, 20.dp, 20.dp, 12.dp)
                    )

                    SettingsItem(
                        icon = Icons.Default.Info,
                        title = "Version",
                        subtitle = "1.0.0",
                        onClick = {}
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
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Text(
                        text = "Danger Zone",
                        fontSize = 14.sp,
                        fontWeight = FontWeight.SemiBold,
                        color = Color(0xFFE74C3C),
                        modifier = Modifier.padding(20.dp, 20.dp, 20.dp, 12.dp)
                    )

                    SettingsItem(
                        icon = Icons.Default.Delete,
                        title = "Reset App Data",
                        subtitle = "Clear all settings and location data",
                        onClick = onResetData,
                        textColor = Color(0xFFE74C3C)
                    )

                    Divider(
                        color = Color(0xFFF0F0F0),
                        modifier = Modifier.padding(horizontal = 20.dp)
                    )

                    SettingsItem(
                        icon = Icons.Default.ExitToApp,
                        title = "Sign Out",
                        subtitle = "Sign out of your account",
                        onClick = onSignOut,
                        textColor = Color(0xFFE74C3C)
                    )
                }
            }

            Spacer(modifier = Modifier.height(32.dp))

            Text(
                text = "Exit Note v1.0.0\nMade with ❤️",
                fontSize = 13.sp,
                color = Color(0xFF95A5A6),
                modifier = Modifier.fillMaxWidth(),
                textAlign = androidx.compose.ui.text.style.TextAlign.Center
            )

            Spacer(modifier = Modifier.height(32.dp))
        }
    }
}

@Composable
private fun SettingsItem(
    icon: ImageVector,
    title: String,
    subtitle: String,
    onClick: () -> Unit,
    textColor: Color = Color(0xFF2C3E50)
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clickable(onClick = onClick)
            .padding(20.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Icon(
            imageVector = icon,
            contentDescription = title,
            tint = textColor,
            modifier = Modifier.size(24.dp)
        )

        Spacer(modifier = Modifier.size(16.dp))

        Column(
            modifier = Modifier.weight(1f)
        ) {
            Text(
                text = title,
                fontSize = 16.sp,
                fontWeight = FontWeight.Medium,
                color = textColor
            )
            Text(
                text = subtitle,
                fontSize = 13.sp,
                color = Color(0xFF7F8C8D)
            )
        }
    }
}

@Preview(showBackground = true, name = "Exit Note – Settings")
@Composable
fun SettingsScreenPreview() {
    SettingsContent(
        userEmail = "user@example.com",
        homeLocationSet = true,
        isTrackingEnabled = true,
        onNavigateBack = {},
        onEditLocation = {},
        onTestNotification = {},
        onManualCheck = {},
        onSignOut = {},
        onResetData = {}
    )
}