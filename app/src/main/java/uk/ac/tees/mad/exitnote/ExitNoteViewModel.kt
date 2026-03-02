package uk.ac.tees.mad.exitnote.viewmodel

import android.content.Context
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import uk.ac.tees.mad.exitnote.ExitNoteApplication


class ExitNoteViewModel : ViewModel() {

    private val context: Context = ExitNoteApplication.instance.applicationContext

    // UI State flows
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

    // Location tracking state
    private val _isTrackingEnabled = MutableStateFlow(false)
    val isTrackingEnabled: StateFlow<Boolean> = _isTrackingEnabled.asStateFlow()

    private val _lastExitTime = MutableStateFlow<Long?>(null)
    val lastExitTime: StateFlow<Long?> = _lastExitTime.asStateFlow()

    // Current location data
    private val _currentLocation = MutableStateFlow<LocationData?>(null)
    val currentLocation: StateFlow<LocationData?> = _currentLocation.asStateFlow()

    init {
        // Initialization logic will be added in next sprint
    }

    // ========== Splash Screen Methods ==========

    fun checkInitialState() {
        viewModelScope.launch {
            _splashState.value = SplashState.Loading
            // TODO: Implementation in next sprint
            // Will check:
            // 1. If user is authenticated
            // 2. If home location is set
            // 3. Navigate accordingly
        }
    }

    // ========== Authentication Methods ==========

    fun signInWithEmail(email: String, password: String) {
        viewModelScope.launch {
            _isLoading.value = true
            _errorMessage.value = null
            // TODO: Implementation in next sprint
            _isLoading.value = false
        }
    }

    fun signUpWithEmail(email: String, password: String) {
        viewModelScope.launch {
            _isLoading.value = true
            _errorMessage.value = null
            // TODO: Implementation in next sprint
            _isLoading.value = false
        }
    }

    fun signInWithGoogle() {
        viewModelScope.launch {
            _isLoading.value = true
            _errorMessage.value = null
            // TODO: Implementation in next sprint
            _isLoading.value = false
        }
    }

    fun signOut() {
        viewModelScope.launch {
            // TODO: Implementation in next sprint
            _userState.value = UserState.NotAuthenticated
            _authState.value = AuthState.Initial
        }
    }

    // ========== Home Location Setup Methods ==========

    fun captureCurrentLocation() {
        viewModelScope.launch {
            _isLoading.value = true
            _errorMessage.value = null
            // TODO: Implementation in next sprint
            // Will use GPS to get current location
            _isLoading.value = false
        }
    }

    fun setHomeLocation(latitude: Double, longitude: Double, radius: Float) {
        viewModelScope.launch {
            _isLoading.value = true
            // TODO: Implementation in next sprint
            // Will save to SharedPreferences/DataStore
            _isLoading.value = false
        }
    }

    fun updateGeofenceRadius(radius: Float) {
        viewModelScope.launch {
            // TODO: Implementation in next sprint
        }
    }

    fun getLocationName(latitude: Double, longitude: Double) {
        viewModelScope.launch {
            // TODO: Implementation in next sprint
            // Will use OpenStreetMap Nominatim API
        }
    }

    // ========== Location Tracking Methods ==========

    fun toggleLocationTracking(enabled: Boolean) {
        viewModelScope.launch {
            _isTrackingEnabled.value = enabled
            // TODO: Implementation in next sprint
            // Will start/stop geofence monitoring
        }
    }

    fun checkIfOutsideGeofence() {
        viewModelScope.launch {
            // TODO: Implementation in next sprint
            // Will use Haversine formula to calculate distance
        }
    }

    // ========== Notification Methods ==========

    fun triggerExitNotification() {
        // TODO: Implementation in next sprint
    }

    fun snoozeNotification(minutes: Int) {
        viewModelScope.launch {
            // TODO: Implementation in next sprint
        }
    }

    // ========== Settings Methods ==========

    fun resetAppData() {
        viewModelScope.launch {
            // TODO: Implementation in next sprint
            // Will clear all local data
        }
    }

    fun clearError() {
        _errorMessage.value = null
    }
}

// ========== State Classes ==========

/**
 * Represents the state of the splash screen
 */
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

/**
 * Represents authentication state
 */
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

/**
 * Represents user authentication status
 */
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

/**
 * Represents home location setup state
 */
data class HomeLocationState(
    val latitude: Double? = null,
    val longitude: Double? = null,
    val radius: Float = 250f, // Default 250 meters
    val locationName: String? = null,
    val isSet: Boolean = false,
    val isCapturing: Boolean = false
) {
    companion object {
        val NotSet = HomeLocationState(isSet = false)
    }
}

/**
 * Represents location data
 */
data class LocationData(
    val latitude: Double,
    val longitude: Double,
    val accuracy: Float? = null,
    val timestamp: Long = System.currentTimeMillis()
)

/**
 * Represents geofence status
 */
data class GeofenceStatus(
    val isInside: Boolean = true,
    val distance: Double? = null,
    val lastChecked: Long = System.currentTimeMillis()
)