package uk.ac.tees.mad.exitnote.ui.screens.setup

import android.Manifest
import android.os.Build
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
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.MyLocation
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Icon
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Slider
import androidx.compose.material3.SliderDefaults
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableFloatStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.google.accompanist.permissions.ExperimentalPermissionsApi
import com.google.accompanist.permissions.isGranted
import com.google.accompanist.permissions.rememberMultiplePermissionsState
import com.google.accompanist.permissions.rememberPermissionState
import com.google.accompanist.permissions.shouldShowRationale
import uk.ac.tees.mad.exitnote.viewmodel.ExitNoteViewModel

@OptIn(ExperimentalPermissionsApi::class)
@Composable
fun SetupLocationScreen(
    viewModel: ExitNoteViewModel,
    userEmail: String?,
    onSetupComplete: () -> Unit
) {
    val homeLocationState by viewModel.homeLocationState.collectAsStateWithLifecycle()
    val isLoading by viewModel.isLoading.collectAsStateWithLifecycle()
    val errorMessage by viewModel.errorMessage.collectAsStateWithLifecycle()

    val locationPermissions = rememberMultiplePermissionsState(
        permissions = listOf(
            Manifest.permission.ACCESS_FINE_LOCATION,
            Manifest.permission.ACCESS_COARSE_LOCATION
        )
    )

    val notificationPermission = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
        rememberPermissionState(Manifest.permission.POST_NOTIFICATIONS)
    } else {
        null
    }

    var showPermissionDialog by remember { mutableStateOf(false) }

    LaunchedEffect(Unit) {
        if (!locationPermissions.allPermissionsGranted) {
            locationPermissions.launchMultiplePermissionRequest()
        }
    }

    SetupLocationContent(
        userEmail = userEmail,
        isLoading = isLoading,
        isCapturing = homeLocationState.isCapturing,
        capturedLatitude = homeLocationState.latitude,
        capturedLongitude = homeLocationState.longitude,
        currentRadius = homeLocationState.radius,
        locationName = homeLocationState.locationName,
        errorMessage = errorMessage,
        hasLocationPermission = locationPermissions.allPermissionsGranted,
        onCaptureLocation = {
            if (locationPermissions.allPermissionsGranted) {
                viewModel.captureCurrentLocation()

                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
                    notificationPermission?.launchPermissionRequest()
                }
            } else {
                showPermissionDialog = true
            }
        },
        onRadiusChange = { viewModel.updateGeofenceRadius(it) },
        onSaveLocation = { lat, lon, radius ->
            viewModel.setHomeLocation(lat, lon, radius)
            onSetupComplete()
        },
        onRequestPermission = {
            locationPermissions.launchMultiplePermissionRequest()
        }
    )

    if (showPermissionDialog) {
        AlertDialog(
            onDismissRequest = { showPermissionDialog = false },
            title = { Text("Location Permission Required") },
            text = {
                Text("This app needs location permission to set your home location and detect when you leave.")
            },
            confirmButton = {
                TextButton(
                    onClick = {
                        showPermissionDialog = false
                        locationPermissions.launchMultiplePermissionRequest()
                    }
                ) {
                    Text("Grant Permission")
                }
            },
            dismissButton = {
                TextButton(onClick = { showPermissionDialog = false }) {
                    Text("Cancel")
                }
            }
        )
    }
}

@Composable
private fun SetupLocationContent(
    userEmail: String?,
    isLoading: Boolean,
    isCapturing: Boolean,
    capturedLatitude: Double?,
    capturedLongitude: Double?,
    currentRadius: Float,
    locationName: String?,
    errorMessage: String?,
    hasLocationPermission: Boolean,
    onCaptureLocation: () -> Unit,
    onRadiusChange: (Float) -> Unit,
    onSaveLocation: (Double, Double, Float) -> Unit,
    onRequestPermission: () -> Unit
) {
    var radius by remember { mutableFloatStateOf(currentRadius) }

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(Color(0xFFF5F7FA))
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .verticalScroll(rememberScrollState())
                .padding(24.dp)
        ) {
            Spacer(modifier = Modifier.height(40.dp))

            Text(
                text = "Set Your Home Location",
                fontSize = 28.sp,
                fontWeight = FontWeight.Bold,
                color = Color(0xFF2C3E50),
                textAlign = TextAlign.Center,
                modifier = Modifier.fillMaxWidth()
            )

            Spacer(modifier = Modifier.height(12.dp))

            Text(
                text = "We'll remind you when you leave this area",
                fontSize = 15.sp,
                color = Color(0xFF7F8C8D),
                textAlign = TextAlign.Center,
                modifier = Modifier.fillMaxWidth()
            )

            if (userEmail != null) {
                Spacer(modifier = Modifier.height(8.dp))
                Text(
                    text = "Signed in as: $userEmail",
                    fontSize = 13.sp,
                    color = Color(0xFF95A5A6),
                    textAlign = TextAlign.Center,
                    modifier = Modifier.fillMaxWidth()
                )
            }

            Spacer(modifier = Modifier.height(32.dp))

            if (!hasLocationPermission) {
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(16.dp),
                    colors = CardDefaults.cardColors(
                        containerColor = Color(0xFFFFEBEE)
                    )
                ) {
                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(20.dp)
                    ) {
                        Text(
                            text = "⚠️ Permission Required",
                            fontSize = 16.sp,
                            fontWeight = FontWeight.SemiBold,
                            color = Color(0xFFE74C3C)
                        )

                        Spacer(modifier = Modifier.height(8.dp))

                        Text(
                            text = "Location permission is required to set your home location.",
                            fontSize = 14.sp,
                            color = Color(0xFF8B1A1A)
                        )

                        Spacer(modifier = Modifier.height(12.dp))

                        Button(
                            onClick = onRequestPermission,
                            colors = ButtonDefaults.buttonColors(
                                containerColor = Color(0xFFE74C3C)
                            ),
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Text("Grant Permission")
                        }
                    }
                }

                Spacer(modifier = Modifier.height(20.dp))
            }

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
                        text = "Step 1: Capture Location",
                        fontSize = 18.sp,
                        fontWeight = FontWeight.SemiBold,
                        color = Color(0xFF2C3E50)
                    )

                    Spacer(modifier = Modifier.height(12.dp))

                    Text(
                        text = "Make sure you're at home, then tap the button below to capture your current location.",
                        fontSize = 14.sp,
                        color = Color(0xFF7F8C8D),
                        lineHeight = 20.sp
                    )

                    Spacer(modifier = Modifier.height(20.dp))

                    OutlinedButton(
                        onClick = onCaptureLocation,
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(56.dp),
                        shape = RoundedCornerShape(12.dp),
                        enabled = !isLoading && !isCapturing && hasLocationPermission
                    ) {
                        if (isCapturing) {
                            CircularProgressIndicator(
                                modifier = Modifier.size(24.dp),
                                color = Color(0xFF6B7FD7)
                            )
                        } else {
                            Icon(
                                imageVector = Icons.Default.MyLocation,
                                contentDescription = "Capture Location",
                                tint = if (hasLocationPermission) Color(0xFF6B7FD7) else Color(0xFFBDC3C7)
                            )
                            Spacer(modifier = Modifier.size(8.dp))
                            Text(
                                text = "Capture Current Location",
                                fontSize = 16.sp,
                                fontWeight = FontWeight.SemiBold,
                                color = if (hasLocationPermission) Color(0xFF6B7FD7) else Color(0xFFBDC3C7)
                            )
                        }
                    }

                    if (errorMessage != null) {
                        Spacer(modifier = Modifier.height(12.dp))

                        Card(
                            colors = CardDefaults.cardColors(
                                containerColor = Color(0xFFFFEBEE)
                            ),
                            shape = RoundedCornerShape(8.dp)
                        ) {
                            Text(
                                text = errorMessage,
                                fontSize = 13.sp,
                                color = Color(0xFFE74C3C),
                                modifier = Modifier.padding(12.dp)
                            )
                        }
                    }

                    if (capturedLatitude != null && capturedLongitude != null) {
                        Spacer(modifier = Modifier.height(16.dp))

                        Card(
                            modifier = Modifier.fillMaxWidth(),
                            shape = RoundedCornerShape(12.dp),
                            colors = CardDefaults.cardColors(
                                containerColor = Color(0xFFE8F5E9)
                            )
                        ) {
                            Column(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(16.dp)
                            ) {
                                Row(
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Text(
                                        text = "✓",
                                        fontSize = 20.sp,
                                        color = Color(0xFF27AE60),
                                        fontWeight = FontWeight.Bold
                                    )
                                    Spacer(modifier = Modifier.size(8.dp))
                                    Text(
                                        text = "Location Captured",
                                        fontSize = 15.sp,
                                        fontWeight = FontWeight.SemiBold,
                                        color = Color(0xFF27AE60)
                                    )
                                }

                                Spacer(modifier = Modifier.height(8.dp))

                                Text(
                                    text = "Lat: ${String.format("%.6f", capturedLatitude)}",
                                    fontSize = 13.sp,
                                    color = Color(0xFF2C7A4E)
                                )

                                Text(
                                    text = "Lon: ${String.format("%.6f", capturedLongitude)}",
                                    fontSize = 13.sp,
                                    color = Color(0xFF2C7A4E)
                                )

                                if (locationName != null) {
                                    Spacer(modifier = Modifier.height(4.dp))
                                    Text(
                                        text = "Area: $locationName",
                                        fontSize = 13.sp,
                                        color = Color(0xFF2C7A4E)
                                    )
                                }
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
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(20.dp)
                ) {
                    Text(
                        text = "Step 2: Set Geofence Radius",
                        fontSize = 18.sp,
                        fontWeight = FontWeight.SemiBold,
                        color = Color(0xFF2C3E50)
                    )

                    Spacer(modifier = Modifier.height(12.dp))

                    Text(
                        text = "Choose how far from home you need to be before getting a reminder.",
                        fontSize = 14.sp,
                        color = Color(0xFF7F8C8D),
                        lineHeight = 20.sp
                    )

                    Spacer(modifier = Modifier.height(20.dp))

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            text = "Radius",
                            fontSize = 16.sp,
                            fontWeight = FontWeight.Medium,
                            color = Color(0xFF2C3E50)
                        )

                        Text(
                            text = "${radius.toInt()} meters",
                            fontSize = 16.sp,
                            fontWeight = FontWeight.Bold,
                            color = Color(0xFF6B7FD7)
                        )
                    }

                    Spacer(modifier = Modifier.height(12.dp))

                    Slider(
                        value = radius,
                        onValueChange = { radius = it },
                        valueRange = 100f..1000f,
                        steps = 17,
                        colors = SliderDefaults.colors(
                            thumbColor = Color(0xFF6B7FD7),
                            activeTrackColor = Color(0xFF6B7FD7),
                            inactiveTrackColor = Color(0xFFBDC3C7)
                        ),
                        modifier = Modifier.fillMaxWidth()
                    )

                    Spacer(modifier = Modifier.height(8.dp))

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Text(
                            text = "100m",
                            fontSize = 12.sp,
                            color = Color(0xFF95A5A6)
                        )
                        Text(
                            text = "1000m",
                            fontSize = 12.sp,
                            color = Color(0xFF95A5A6)
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(32.dp))

            Button(
                onClick = {
                    if (capturedLatitude != null && capturedLongitude != null) {
                        onRadiusChange(radius)
                        onSaveLocation(capturedLatitude, capturedLongitude, radius)
                    }
                },
                modifier = Modifier
                    .fillMaxWidth()
                    .height(56.dp),
                colors = ButtonDefaults.buttonColors(
                    containerColor = Color(0xFF6B7FD7),
                    disabledContainerColor = Color(0xFFBDC3C7)
                ),
                shape = RoundedCornerShape(12.dp),
                enabled = capturedLatitude != null && capturedLongitude != null && !isLoading
            ) {
                if (isLoading) {
                    CircularProgressIndicator(
                        color = Color.White,
                        modifier = Modifier.size(24.dp)
                    )
                } else {
                    Text(
                        text = "Save and Continue",
                        fontSize = 16.sp,
                        fontWeight = FontWeight.SemiBold
                    )
                }
            }

            Spacer(modifier = Modifier.height(16.dp))

            Card(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(12.dp),
                colors = CardDefaults.cardColors(
                    containerColor = Color(0xFFFFF3E0)
                ),
                elevation = CardDefaults.cardElevation(defaultElevation = 0.dp)
            ) {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(16.dp)
                ) {
                    Text(
                        text = "💡 Tip",
                        fontSize = 14.sp,
                        fontWeight = FontWeight.SemiBold,
                        color = Color(0xFFE67E22)
                    )

                    Spacer(modifier = Modifier.height(8.dp))

                    Text(
                        text = "Start with 200-300 meters for most homes. You can always adjust this later in settings.",
                        fontSize = 13.sp,
                        color = Color(0xFF8B5E34),
                        lineHeight = 18.sp
                    )
                }
            }

            Spacer(modifier = Modifier.height(32.dp))
        }
    }
}

@Preview(showBackground = true, name = "Exit Note – Setup (Initial)")
@Composable
fun SetupLocationInitialPreview() {
    SetupLocationContent(
        userEmail = "user@example.com",
        isLoading = false,
        isCapturing = false,
        capturedLatitude = null,
        capturedLongitude = null,
        currentRadius = 250f,
        locationName = null,
        errorMessage = null,
        hasLocationPermission = true,
        onCaptureLocation = {},
        onRadiusChange = {},
        onSaveLocation = { _, _, _ -> },
        onRequestPermission = {}
    )
}

@Preview(showBackground = true, name = "Exit Note – Setup (Captured)")
@Composable
fun SetupLocationCapturedPreview() {
    SetupLocationContent(
        userEmail = "user@example.com",
        isLoading = false,
        isCapturing = false,
        capturedLatitude = 40.712776,
        capturedLongitude = -74.005974,
        currentRadius = 300f,
        locationName = "New York, NY",
        errorMessage = null,
        hasLocationPermission = true,
        onCaptureLocation = {},
        onRadiusChange = {},
        onSaveLocation = { _, _, _ -> },
        onRequestPermission = {}
    )
}