package uk.ac.tees.mad.exitnote.service

import android.app.Notification
import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.app.Service
import android.content.Intent
import android.location.Location
import android.os.Build
import android.os.IBinder
import android.os.Looper
import android.util.Log
import androidx.core.app.NotificationCompat
import androidx.datastore.preferences.core.booleanPreferencesKey
import androidx.datastore.preferences.core.doublePreferencesKey
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.floatPreferencesKey
import androidx.datastore.preferences.core.longPreferencesKey
import androidx.datastore.preferences.preferencesDataStore
import com.google.android.gms.location.LocationCallback
import com.google.android.gms.location.LocationRequest
import com.google.android.gms.location.LocationResult
import com.google.android.gms.location.LocationServices
import com.google.android.gms.location.Priority
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.cancel
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.launch
import uk.ac.tees.mad.exitnote.MainActivity
import uk.ac.tees.mad.exitnote.data.local.ExitEventEntity
import uk.ac.tees.mad.exitnote.data.local.ExitNoteDatabase
import uk.ac.tees.mad.exitnote.data.local.LocationHistoryEntity

private val android.content.Context.dataStore by preferencesDataStore(name = "exit_note_prefs")

class LocationTrackingService : Service() {

    private val serviceScope = CoroutineScope(SupervisorJob() + Dispatchers.IO)
    private val fusedLocationClient by lazy { LocationServices.getFusedLocationProviderClient(this) }
    private val locationManager by lazy { LocationManager(this) }
    private val notificationHelper by lazy { NotificationHelper(this) }
    private val database by lazy { ExitNoteDatabase.getDatabase(this) }

    private var lastNotificationTime = 0L
    private val cooldownPeriod = 600_000L // 10 minutes

    companion object {
        const val CHANNEL_ID = "location_tracking_channel"
        const val NOTIFICATION_ID = 2001
        const val ACTION_STOP_TRACKING = "uk.ac.tees.mad.exitnote.STOP_TRACKING"
    }

    private val locationCallback = object : LocationCallback() {
        override fun onLocationResult(result: LocationResult) {
            result.lastLocation?.let { location ->
                serviceScope.launch {
                    checkLocationAndNotify(location)
                }
            }
        }
    }

    override fun onCreate() {
        super.onCreate()
        createNotificationChannel()
    }

    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {
        if (intent?.action == ACTION_STOP_TRACKING) {
            stopSelf()
            return START_NOT_STICKY
        }

        startForeground(NOTIFICATION_ID, createForegroundNotification())
        startLocationUpdates()

        return START_STICKY
    }

    private fun startLocationUpdates() {
        try {
            val locationRequest = LocationRequest.Builder(
                Priority.PRIORITY_BALANCED_POWER_ACCURACY,
                120_000L
            ).apply {
                setMinUpdateIntervalMillis(60_000L)
                setWaitForAccurateLocation(false)
            }.build()

            fusedLocationClient.requestLocationUpdates(
                locationRequest,
                locationCallback,
                Looper.getMainLooper()
            )

            Log.d("LocationService", "Location updates started (every ~2 minutes)")
        } catch (e: SecurityException) {
            Log.e("LocationService", "Location permission not granted", e)
            stopSelf()
        } catch (e: Exception) {
            Log.e("LocationService", "Failed to request location updates", e)
            stopSelf()
        }
    }

    private suspend fun checkLocationAndNotify(location: Location) {
        try {
            val prefs = dataStore.data.first()

            val isTrackingEnabled = prefs[booleanPreferencesKey("is_tracking_enabled")] ?: false
            val isHomeSet = prefs[booleanPreferencesKey("is_home_set")] ?: false

            if (!isTrackingEnabled || !isHomeSet) {
                stopSelf()
                return
            }

            val homeLat = prefs[doublePreferencesKey("home_latitude")] ?: return
            val homeLon = prefs[doublePreferencesKey("home_longitude")] ?: return
            val radius = prefs[floatPreferencesKey("home_radius")] ?: 250f

            val distance = locationManager.calculateDistance(
                location.latitude,
                location.longitude,
                homeLat,
                homeLon
            )

            database.locationHistoryDao().insertLocation(
                LocationHistoryEntity(
                    latitude = location.latitude,
                    longitude = location.longitude,
                    timestamp = System.currentTimeMillis(),
                    isExitEvent = distance > radius,
                    distanceFromHome = distance
                )
            )

            Log.d("LocationService", "Distance from home: $distance meters (radius: $radius)")

            if (distance > radius) {
                val currentTime = System.currentTimeMillis()
                val timeSinceLastNotification = currentTime - lastNotificationTime

                if (timeSinceLastNotification > cooldownPeriod) {
                    Log.d("LocationService", "Outside geofence! Showing notification")

                    notificationHelper.showExitNotification()
                    lastNotificationTime = currentTime

                    database.exitEventDao().insertExitEvent(
                        ExitEventEntity(
                            exitTime = currentTime,
                            latitude = location.latitude,
                            longitude = location.longitude,
                            notificationShown = true,
                            userResponse = null
                        )
                    )

                    dataStore.edit { prefs ->
                        prefs[longPreferencesKey("last_exit_time")] = currentTime
                    }
                }
            }
        } catch (e: Exception) {
            Log.e("LocationService", "Error checking location: ${e.message}", e)
        }
    }

    private fun createNotificationChannel() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val channel = NotificationChannel(
                CHANNEL_ID,
                "Location Tracking",
                NotificationManager.IMPORTANCE_LOW
            ).apply {
                description = "Monitoring your location for exit reminders"
                setShowBadge(false)
            }

            val notificationManager = getSystemService(NotificationManager::class.java)
            notificationManager.createNotificationChannel(channel)
        }
    }

    private fun createForegroundNotification(): Notification {
        val intent = Intent(this, MainActivity::class.java)
        val pendingIntent = PendingIntent.getActivity(
            this,
            0,
            intent,
            PendingIntent.FLAG_IMMUTABLE or PendingIntent.FLAG_UPDATE_CURRENT
        )

        val stopIntent = Intent(this, LocationTrackingService::class.java).apply {
            action = ACTION_STOP_TRACKING
        }
        val stopPendingIntent = PendingIntent.getService(
            this,
            1,
            stopIntent,
            PendingIntent.FLAG_IMMUTABLE or PendingIntent.FLAG_UPDATE_CURRENT
        )

        return NotificationCompat.Builder(this, CHANNEL_ID)
            .setContentTitle("Exit Note")
            .setContentText("Monitoring your location")
            .setSmallIcon(android.R.drawable.ic_menu_mylocation)
            .setContentIntent(pendingIntent)
            .setOngoing(true)
            .addAction(
                android.R.drawable.ic_menu_close_clear_cancel,
                "Stop",
                stopPendingIntent
            )
            .build()
    }

    override fun onDestroy() {
        super.onDestroy()
        fusedLocationClient.removeLocationUpdates(locationCallback)
        serviceScope.cancel()
        Log.d("LocationService", "Service destroyed")
    }

    override fun onBind(intent: Intent?): IBinder? = null
}