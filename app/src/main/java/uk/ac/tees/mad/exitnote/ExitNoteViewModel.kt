package uk.ac.tees.mad.exitnote.viewmodel

import android.content.Context
import android.content.Intent
import android.os.Build
import android.util.Log
import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.booleanPreferencesKey
import androidx.datastore.preferences.core.doublePreferencesKey
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.floatPreferencesKey
import androidx.datastore.preferences.core.longPreferencesKey
import androidx.datastore.preferences.core.stringPreferencesKey
import androidx.datastore.preferences.preferencesDataStore
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.FirebaseAuthException
import com.google.firebase.firestore.FirebaseFirestore
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.launch
import kotlinx.coroutines.tasks.await
import uk.ac.tees.mad.exitnote.ExitNoteApplication
import uk.ac.tees.mad.exitnote.data.local.ExitNoteDatabase
import uk.ac.tees.mad.exitnote.data.local.exitNoteDataStore
import uk.ac.tees.mad.exitnote.data.remote.NominatimApi
import uk.ac.tees.mad.exitnote.service.LocationManager
import uk.ac.tees.mad.exitnote.service.LocationTrackingService
import uk.ac.tees.mad.exitnote.service.NotificationHelper

private val Context.dataStore: DataStore<Preferences> by preferencesDataStore(name = "exit_note_prefs")

class ExitNoteViewModel : ViewModel() {

    private val context: Context = ExitNoteApplication.instance.applicationContext
    private val auth: FirebaseAuth = FirebaseAuth.getInstance()
    private val firestore: FirebaseFirestore = FirebaseFirestore.getInstance()
    private val dataStore = context.exitNoteDataStore
    private val locationManager = LocationManager(context)
    private val notificationHelper = NotificationHelper(context)
    private val database = ExitNoteDatabase.getDatabase(context)
    private val nominatimApi = NominatimApi.create()

    private object PrefsKeys {
        val HOME_LATITUDE = doublePreferencesKey("home_latitude")
        val HOME_LONGITUDE = doublePreferencesKey("home_longitude")
        val HOME_RADIUS = floatPreferencesKey("home_radius")
        val HOME_LOCATION_NAME = stringPreferencesKey("home_location_name")
        val IS_HOME_SET = booleanPreferencesKey("is_home_set")
        val IS_TRACKING_ENABLED = booleanPreferencesKey("is_tracking_enabled")
        val LAST_EXIT_TIME = longPreferencesKey("last_exit_time")
    }

    private val _splashState = MutableStateFlow<SplashState>(SplashState.Loading)
    val splashState: StateFlow<SplashState> = _splashState.asStateFlow()

    private val _authState = MutableStateFlow<AuthState>(AuthState.Initial)
    val authState: StateFlow<AuthState> = _authState.asStateFlow()

    private val _homeLocationState = MutableStateFlow<HomeLocationState>(HomeLocationState.NotSet)
    val homeLocationState: StateFlow<HomeLocationState> = _homeLocationState.asStateFlow()

    private val _userState = MutableStateFlow<UserState>(UserState.NotAuthenticated)
    val userState: StateFlow<UserState> = _userState.asStateFlow()

    private val _isLoading = MutableStateFlow(false)
    val isLoading: StateFlow<Boolean> = _isLoading.asStateFlow()

    private val _errorMessage = MutableStateFlow<String?>(null)
    val errorMessage: StateFlow<String?> = _errorMessage.asStateFlow()

    private val _isTrackingEnabled = MutableStateFlow(false)
    val isTrackingEnabled: StateFlow<Boolean> = _isTrackingEnabled.asStateFlow()

    private val _lastExitTime = MutableStateFlow<Long?>(null)
    val lastExitTime: StateFlow<Long?> = _lastExitTime.asStateFlow()

    private val _currentLocation = MutableStateFlow<LocationData?>(null)
    val currentLocation: StateFlow<LocationData?> = _currentLocation.asStateFlow()

    init {
        checkAuthState()
        loadStoredData()
    }

    fun setError(msg: String) {
        _errorMessage.value = msg
    }

    private fun checkAuthState() {
        viewModelScope.launch {
            val currentUser = auth.currentUser
            if (currentUser != null) {
                _userState.value = UserState(
                    userId = currentUser.uid,
                    email = currentUser.email,
                    displayName = currentUser.displayName,
                    isAuthenticated = true
                )
            }
        }
    }

    private fun loadStoredData() {
        viewModelScope.launch {
            try {
                val prefs = dataStore.data.first()

                val latitude = prefs[PrefsKeys.HOME_LATITUDE]
                val longitude = prefs[PrefsKeys.HOME_LONGITUDE]
                val radius = prefs[PrefsKeys.HOME_RADIUS] ?: 250f
                val locationName = prefs[PrefsKeys.HOME_LOCATION_NAME]
                val isSet = prefs[PrefsKeys.IS_HOME_SET] ?: false

                if (latitude != null && longitude != null && isSet) {
                    _homeLocationState.value = HomeLocationState(
                        latitude = latitude,
                        longitude = longitude,
                        radius = radius,
                        locationName = locationName,
                        isSet = true
                    )
                }

                _isTrackingEnabled.value = prefs[PrefsKeys.IS_TRACKING_ENABLED] ?: false
                _lastExitTime.value = prefs[PrefsKeys.LAST_EXIT_TIME]

            } catch (e: Exception) {
                Log.e("ExitNoteVM", "Error loading stored data: ${e.message}")
            }
        }
    }

    fun checkInitialState() {
        viewModelScope.launch {
            _splashState.value = SplashState.Loading

            try {
                val currentUser = auth.currentUser

                if (currentUser == null) {
                    _splashState.value = SplashState(
                        isLoading = false,
                        shouldNavigateTo = NavigationDestination.AUTH
                    )
                } else {
                    val prefs = dataStore.data.first()
                    val isHomeSet = prefs[PrefsKeys.IS_HOME_SET] ?: false

                    if (isHomeSet) {
                        _splashState.value = SplashState(
                            isLoading = false,
                            shouldNavigateTo = NavigationDestination.HOME
                        )
                    } else {
                        _splashState.value = SplashState(
                            isLoading = false,
                            shouldNavigateTo = NavigationDestination.SETUP
                        )
                    }
                }
            } catch (e: Exception) {
                Log.e("ExitNoteVM", "Error checking initial state: ${e.message}")
                _splashState.value = SplashState(
                    isLoading = false,
                    shouldNavigateTo = NavigationDestination.AUTH
                )
            }
        }
    }

    fun signInWithEmail(email: String, password: String) {
        viewModelScope.launch {
            _isLoading.value = true
            _errorMessage.value = null
            _authState.value = AuthState(isSigningIn = true)

            try {
                val result = auth.signInWithEmailAndPassword(email, password).await()
                val user = result.user

                if (user != null) {
                    _userState.value = UserState(
                        userId = user.uid,
                        email = user.email,
                        displayName = user.displayName,
                        isAuthenticated = true
                    )

                    _authState.value = AuthState(isSuccess = true)

                    val prefs = dataStore.data.first()
                    val isHomeSet = prefs[PrefsKeys.IS_HOME_SET] ?: false

                    if (!isHomeSet) {
                        _splashState.value = SplashState(
                            isLoading = false,
                            shouldNavigateTo = NavigationDestination.SETUP
                        )
                    }
                } else {
                    _errorMessage.value = "Sign in failed. Please try again."
                    _authState.value = AuthState(error = "Sign in failed")
                }
            } catch (e: FirebaseAuthException) {
                val errorMsg = when (e.errorCode) {
                    "ERROR_INVALID_EMAIL" -> "Invalid email address"
                    "ERROR_WRONG_PASSWORD" -> "Incorrect password"
                    "ERROR_USER_NOT_FOUND" -> "No account found with this email"
                    "ERROR_USER_DISABLED" -> "This account has been disabled"
                    "ERROR_TOO_MANY_REQUESTS" -> "Too many attempts. Try again later"
                    else -> "Sign in failed: ${e.message}"
                }
                _errorMessage.value = errorMsg
                _authState.value = AuthState(error = errorMsg)
                Log.e("ExitNoteVM", "Sign in error: ${e.message}")
            } catch (e: Exception) {
                _errorMessage.value = "An error occurred: ${e.message}"
                _authState.value = AuthState(error = e.message)
                Log.e("ExitNoteVM", "Sign in error: ${e.message}")
            } finally {
                _isLoading.value = false
            }
        }
    }

    fun signUpWithEmail(email: String, password: String) {
        viewModelScope.launch {
            _isLoading.value = true
            _errorMessage.value = null
            _authState.value = AuthState(isSigningUp = true)

            try {
                val result = auth.createUserWithEmailAndPassword(email, password).await()
                val user = result.user

                if (user != null) {
                    val userDoc = hashMapOf(
                        "uid" to user.uid,
                        "email" to user.email,
                        "createdAt" to System.currentTimeMillis(),
                        "homeLocationSet" to false
                    )

                    firestore.collection("users")
                        .document(user.uid)
                        .set(userDoc)
                        .await()

                    _userState.value = UserState(
                        userId = user.uid,
                        email = user.email,
                        displayName = user.displayName,
                        isAuthenticated = true
                    )

                    _authState.value = AuthState(isSuccess = true)

                    _splashState.value = SplashState(
                        isLoading = false,
                        shouldNavigateTo = NavigationDestination.SETUP
                    )
                } else {
                    _errorMessage.value = "Sign up failed. Please try again."
                    _authState.value = AuthState(error = "Sign up failed")
                }
            } catch (e: FirebaseAuthException) {
                val errorMsg = when (e.errorCode) {
                    "ERROR_INVALID_EMAIL" -> "Invalid email address"
                    "ERROR_WEAK_PASSWORD" -> "Password is too weak. Use at least 6 characters"
                    "ERROR_EMAIL_ALREADY_IN_USE" -> "An account already exists with this email"
                    "ERROR_TOO_MANY_REQUESTS" -> "Too many attempts. Try again later"
                    else -> "Sign up failed: ${e.message}"
                }
                _errorMessage.value = errorMsg
                _authState.value = AuthState(error = errorMsg)
                Log.e("ExitNoteVM", "Sign up error: ${e.message}")
            } catch (e: Exception) {
                _errorMessage.value = "An error occurred: ${e.message}"
                _authState.value = AuthState(error = e.message)
                Log.e("ExitNoteVM", "Sign up error: ${e.message}")
            } finally {
                _isLoading.value = false
            }
        }
    }

    fun signOut() {
        viewModelScope.launch {
            try {
                stopLocationTracking()

                auth.signOut()
                _userState.value = UserState.NotAuthenticated
                _authState.value = AuthState.Initial
                _homeLocationState.value = HomeLocationState.NotSet

                dataStore.edit { prefs ->
                    prefs.clear()
                }

                database.locationHistoryDao().deleteAll()
                database.exitEventDao().deleteAll()

                Log.d("ExitNoteVM", "User signed out successfully")
            } catch (e: Exception) {
                Log.e("ExitNoteVM", "Sign out error: ${e.message}")
                _errorMessage.value = "Error signing out: ${e.message}"
            }
        }
    }

    fun captureCurrentLocation() {
        viewModelScope.launch {
            _isLoading.value = true
            _errorMessage.value = null
            _homeLocationState.value = _homeLocationState.value.copy(isCapturing = true)

            try {
                val location = locationManager.getCurrentLocation()

                if (location != null) {
                    _currentLocation.value = LocationData(
                        latitude = location.latitude,
                        longitude = location.longitude,
                        accuracy = location.accuracy,
                        timestamp = System.currentTimeMillis()
                    )

                    _homeLocationState.value = _homeLocationState.value.copy(
                        latitude = location.latitude,
                        longitude = location.longitude,
                        isCapturing = false
                    )

                    getLocationName(location.latitude, location.longitude)
                } else {
                    _errorMessage.value = "Could not get location. Please check permissions."
                    _homeLocationState.value = _homeLocationState.value.copy(isCapturing = false)
                }
            } catch (e: Exception) {
                Log.e("ExitNoteVM", "Error capturing location: ${e.message}")
                _errorMessage.value = "Error: ${e.message}"
                _homeLocationState.value = _homeLocationState.value.copy(isCapturing = false)
            } finally {
                _isLoading.value = false
            }
        }
    }

    fun setHomeLocation(latitude: Double, longitude: Double, radius: Float) {
        viewModelScope.launch {
            _isLoading.value = true

            try {
                dataStore.edit { prefs ->
                    prefs[PrefsKeys.HOME_LATITUDE] = latitude
                    prefs[PrefsKeys.HOME_LONGITUDE] = longitude
                    prefs[PrefsKeys.HOME_RADIUS] = radius
                    prefs[PrefsKeys.IS_HOME_SET] = true
                }

                val currentUser = auth.currentUser
                if (currentUser != null) {
                    val locationData = hashMapOf(
                        "latitude" to latitude,
                        "longitude" to longitude,
                        "radius" to radius,
                        "updatedAt" to System.currentTimeMillis()
                    )

                    firestore.collection("users")
                        .document(currentUser.uid)
                        .update("homeLocation", locationData, "homeLocationSet", true)
                        .await()
                }

                _homeLocationState.value = _homeLocationState.value.copy(
                    latitude = latitude,
                    longitude = longitude,
                    radius = radius,
                    isSet = true
                )

                Log.d("ExitNoteVM", "Home location saved successfully")
            } catch (e: Exception) {
                Log.e("ExitNoteVM", "Error saving home location: ${e.message}")
                _errorMessage.value = "Error saving location: ${e.message}"
            } finally {
                _isLoading.value = false
            }
        }
    }

    fun updateGeofenceRadius(radius: Float) {
        viewModelScope.launch {
            try {
                dataStore.edit { prefs ->
                    prefs[PrefsKeys.HOME_RADIUS] = radius
                }

                _homeLocationState.value = _homeLocationState.value.copy(radius = radius)

                val currentUser = auth.currentUser
                if (currentUser != null) {
                    firestore.collection("users")
                        .document(currentUser.uid)
                        .update("homeLocation.radius", radius)
                        .await()
                }
            } catch (e: Exception) {
                Log.e("ExitNoteVM", "Error updating radius: ${e.message}")
            }
        }
    }

    fun getLocationName(latitude: Double, longitude: Double) {
        viewModelScope.launch {
            try {
                val response = nominatimApi.reverseGeocode(latitude, longitude)
                val locationName = response.address?.let { address ->
                    listOfNotNull(
                        address.city ?: address.town ?: address.village,
                        address.state,
                        address.country
                    ).joinToString(", ")
                } ?: response.displayName?.take(50)

                if (locationName != null) {
                    _homeLocationState.value = _homeLocationState.value.copy(
                        locationName = locationName
                    )

                    dataStore.edit { prefs ->
                        prefs[PrefsKeys.HOME_LOCATION_NAME] = locationName
                    }
                }
            } catch (e: Exception) {
                Log.e("ExitNoteVM", "Error getting location name: ${e.message}")
            }
        }
    }

    fun toggleLocationTracking(enabled: Boolean) {
        viewModelScope.launch {
            try {
                dataStore.edit { prefs ->
                    prefs[PrefsKeys.IS_TRACKING_ENABLED] = enabled
                }

                _isTrackingEnabled.value = enabled

                val currentUser = auth.currentUser
                if (currentUser != null) {
                    firestore.collection("users")
                        .document(currentUser.uid)
                        .update("trackingEnabled", enabled)
                        .await()
                }

                if (enabled) {
                    startLocationTracking()
                } else {
                    stopLocationTracking()
                }

                Log.d("ExitNoteVM", "Tracking ${if (enabled) "enabled" else "disabled"}")
            } catch (e: Exception) {
                Log.e("ExitNoteVM", "Error toggling tracking: ${e.message}")
                _errorMessage.value = "Error updating tracking: ${e.message}"
            }
        }
    }

    private fun startLocationTracking() {
        try {
            Log.d("ExitNoteVM", "🚀 Starting location tracking service...")

            val serviceIntent = Intent(context, LocationTrackingService::class.java)
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                context.startForegroundService(serviceIntent)
            } else {
                context.startService(serviceIntent)
            }

            Log.d("ExitNoteVM", "✅ Foreground Service started - monitoring every 20 seconds")
        } catch (e: Exception) {
            Log.e("ExitNoteVM", "❌ Error starting tracking: ${e.message}", e)
        }
    }

    private fun stopLocationTracking() {
        try {
            Log.d("ExitNoteVM", "🛑 Stopping location tracking service...")

            val serviceIntent = Intent(context, LocationTrackingService::class.java)
            context.stopService(serviceIntent)

            Log.d("ExitNoteVM", "✅ Location tracking stopped")
        } catch (e: Exception) {
            Log.e("ExitNoteVM", "❌ Error stopping tracking: ${e.message}", e)
        }
    }

    fun checkIfOutsideGeofence() {
        viewModelScope.launch {
            try {
                val currentLoc = locationManager.getCurrentLocation() ?: return@launch
                val homeState = _homeLocationState.value

                if (!homeState.isSet || homeState.latitude == null || homeState.longitude == null) {
                    return@launch
                }

                val isOutside = locationManager.isOutsideGeofence(
                    currentLoc.latitude,
                    currentLoc.longitude,
                    homeState.latitude,
                    homeState.longitude,
                    homeState.radius
                )

                if (isOutside && _isTrackingEnabled.value) {
                    triggerExitNotification()
                }
            } catch (e: Exception) {
                Log.e("ExitNoteVM", "Error checking geofence: ${e.message}")
            }
        }
    }

    fun triggerExitNotification() {
        try {
            notificationHelper.showExitNotification()

            viewModelScope.launch {
                val currentTime = System.currentTimeMillis()
                dataStore.edit { prefs ->
                    prefs[PrefsKeys.LAST_EXIT_TIME] = currentTime
                }
                _lastExitTime.value = currentTime
            }
        } catch (e: Exception) {
            Log.e("ExitNoteVM", "Error triggering notification: ${e.message}")
        }
    }

    fun snoozeNotification(minutes: Int) {
        viewModelScope.launch {
            notificationHelper.cancelNotification()
        }
    }

    fun resetAppData() {
        viewModelScope.launch {
            try {
                stopLocationTracking()

                dataStore.edit { prefs ->
                    prefs.clear()
                }

                database.locationHistoryDao().deleteAll()
                database.exitEventDao().deleteAll()

                _homeLocationState.value = HomeLocationState.NotSet
                _isTrackingEnabled.value = false
                _lastExitTime.value = null

                Log.d("ExitNoteVM", "App data reset successfully")
            } catch (e: Exception) {
                Log.e("ExitNoteVM", "Error resetting data: ${e.message}")
                _errorMessage.value = "Error resetting data: ${e.message}"
            }
        }
    }

    fun clearError() {
        _errorMessage.value = null
    }
}

data class SplashState(
    val isLoading: Boolean = true,
    val shouldNavigateTo: NavigationDestination? = null
) {
    companion object {
        val Loading = SplashState(isLoading = true)
    }
}

enum class NavigationDestination {
    AUTH,
    SETUP,
    HOME
}

data class AuthState(
    val isSigningIn: Boolean = false,
    val isSigningUp: Boolean = false,
    val error: String? = null,
    val isSuccess: Boolean = false
) {
    companion object {
        val Initial = AuthState()
    }
}

data class UserState(
    val userId: String? = null,
    val email: String? = null,
    val displayName: String? = null,
    val isAuthenticated: Boolean = false
) {
    companion object {
        val NotAuthenticated = UserState(isAuthenticated = false)
    }
}

data class HomeLocationState(
    val latitude: Double? = null,
    val longitude: Double? = null,
    val radius: Float = 250f,
    val locationName: String? = null,
    val isSet: Boolean = false,
    val isCapturing: Boolean = false
) {
    companion object {
        val NotSet = HomeLocationState(isSet = false)
    }
}

data class LocationData(
    val latitude: Double,
    val longitude: Double,
    val accuracy: Float? = null,
    val timestamp: Long = System.currentTimeMillis()
)

data class GeofenceStatus(
    val isInside: Boolean = true,
    val distance: Double? = null,
    val lastChecked: Long = System.currentTimeMillis()
)