package uk.ac.tees.mad.exitnote.service

import android.Manifest
import android.app.Notification
import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.app.Service
import android.content.Intent
import android.content.pm.PackageManager
import android.os.Build
import android.os.IBinder
import android.os.Looper
import android.util.Log
import androidx.core.app.NotificationCompat
import androidx.core.content.ContextCompat
import androidx.datastore.preferences.core.booleanPreferencesKey
import androidx.datastore.preferences.core.doublePreferencesKey
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.floatPreferencesKey
import androidx.datastore.preferences.core.longPreferencesKey
import androidx.datastore.preferences.preferencesDataStore
import com.google.android.gms.location.FusedLocationProviderClient
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
import uk.ac.tees.mad.exitnote.data.local.exitNoteDataStore
import kotlin.math.atan2
import kotlin.math.cos
import kotlin.math.pow
import kotlin.math.sin
import kotlin.math.sqrt


class LocationTrackingService : Service() {

    private val serviceScope = CoroutineScope(SupervisorJob() + Dispatchers.IO)
    private lateinit var fusedLocationClient: FusedLocationProviderClient
    private val notificationHelper by lazy { NotificationHelper(this) }
    private val database by lazy { ExitNoteDatabase.getDatabase(this) }

    private val dataStore by lazy { applicationContext.exitNoteDataStore }

    private var lastNotificationTime = 0L
    private val cooldownPeriod = 300000L
    private var wasInsideGeofence = true
    private var homeLat: Double? = null
    private var homeLon: Double? = null
    private var homeRadius: Float = 250f

    companion object {
        const val CHANNEL_ID = "location_tracking_channel"
        const val NOTIFICATION_ID = 2001
        const val ACTION_STOP_TRACKING = "uk.ac.tees.mad.exitnote.STOP_TRACKING"
    }

    private val locationCallback = object : LocationCallback() {
        override fun onLocationResult(result: LocationResult) {
            super.onLocationResult(result)
            val location = result.lastLocation ?: return

            Log.d("LocationService", "📍 LOCATION UPDATE: ${location.latitude}, ${location.longitude}, accuracy: ${location.accuracy}m")

            serviceScope.launch {
                processLocation(location.latitude, location.longitude)
            }
        }
    }

    override fun onCreate() {
        super.onCreate()
        fusedLocationClient = LocationServices.getFusedLocationProviderClient(this)
        createNotificationChannel()
        Log.d("LocationService", "✅ Service created")
    }

    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {
        Log.d("LocationService", "🚀 Service onStartCommand called")

        if (intent?.action == ACTION_STOP_TRACKING) {
            Log.d("LocationService", "🛑 Stop action received")
            stopSelf()
            return START_NOT_STICKY
        }

        startForeground(NOTIFICATION_ID, createForegroundNotification())

        serviceScope.launch {
            loadHomeLocation()
            startLocationUpdates()
        }

        return START_STICKY
    }

    private suspend fun loadHomeLocation() {
        try {
            val prefs = dataStore.data.first()
            homeLat = prefs[doublePreferencesKey("home_latitude")]
            homeLon = prefs[doublePreferencesKey("home_longitude")]
            homeRadius = prefs[floatPreferencesKey("home_radius")] ?: 250f

            Log.d("LocationService", "🏠 Home location loaded: ($homeLat, $homeLon), radius: ${homeRadius}m")
        } catch (e: Exception) {
            Log.e("LocationService", "❌ Error loading home location", e)
        }
    }

    private fun startLocationUpdates() {
        if (!hasLocationPermission()) {
            Log.e("LocationService", "❌ NO LOCATION PERMISSION!")
            stopSelf()
            return
        }

        try {
            val locationRequest = LocationRequest.Builder(
                Priority.PRIORITY_HIGH_ACCURACY,
                20000
            ).apply {
                setMinUpdateIntervalMillis(10000)
                setMaxUpdateDelayMillis(30000)
                setWaitForAccurateLocation(false)
                setMinUpdateDistanceMeters(10f)
            }.build()

            fusedLocationClient.requestLocationUpdates(
                locationRequest,
                locationCallback,
                Looper.getMainLooper()
            )

            Log.d("LocationService", "✅ Location updates started - HIGH ACCURACY, every 20 seconds")

            fusedLocationClient.lastLocation.addOnSuccessListener { location ->
                location?.let {
                    Log.d("LocationService", "📍 Got last known location immediately")
                    serviceScope.launch {
                        processLocation(it.latitude, it.longitude)
                    }
                }
            }

        } catch (e: SecurityException) {
            Log.e("LocationService", "❌ Security exception", e)
            stopSelf()
        } catch (e: Exception) {
            Log.e("LocationService", "❌ Error starting location updates", e)
            stopSelf()
        }
    }

    private suspend fun processLocation(latitude: Double, longitude: Double) {
        try {
            if (homeLat == null || homeLon == null) {
                Log.w("LocationService", "⚠️ Home location not set, skipping check")
                return
            }

            val distance = calculateDistance(latitude, longitude, homeLat!!, homeLon!!)
            val isInside = distance <= homeRadius

            Log.d("LocationService", "📏 Distance from home: ${distance.toInt()}m (limit: ${homeRadius.toInt()}m)")
            Log.d("LocationService", "🏠 Was inside: $wasInsideGeofence → Now inside: $isInside")

            database.locationHistoryDao().insertLocation(
                LocationHistoryEntity(
                    latitude = latitude,
                    longitude = longitude,
                    timestamp = System.currentTimeMillis(),
                    isExitEvent = !isInside,
                    distanceFromHome = distance
                )
            )

            if (wasInsideGeofence && !isInside) {
                Log.d("LocationService", "🚨 EXIT DETECTED! Crossing from inside to outside!")

                val currentTime = System.currentTimeMillis()
                val timeSinceLastNotification = currentTime - lastNotificationTime

                Log.d("LocationService", "⏱️ Time since last notification: ${timeSinceLastNotification/1000}s (cooldown: ${cooldownPeriod/1000}s)")

                if (timeSinceLastNotification > cooldownPeriod) {
                    Log.d("LocationService", "🔔 TRIGGERING NOTIFICATION NOW!")

                    notificationHelper.showExitNotification()
                    lastNotificationTime = currentTime

                    database.exitEventDao().insertExitEvent(
                        ExitEventEntity(
                            exitTime = currentTime,
                            latitude = latitude,
                            longitude = longitude,
                            notificationShown = true,
                            userResponse = null
                        )
                    )

                    dataStore.edit { prefs ->
                        prefs[longPreferencesKey("last_exit_time")] = currentTime
                    }

                    Log.d("LocationService", "✅ EXIT NOTIFICATION SENT SUCCESSFULLY!")
                } else {
                    val waitTime = (cooldownPeriod - timeSinceLastNotification) / 1000
                    Log.d("LocationService", "⏳ Cooldown active, wait ${waitTime}s more")
                }
            } else if (!wasInsideGeofence && isInside) {
                Log.d("LocationService", "🏠 RETURNED HOME!")
            } else if (!isInside) {
                Log.d("LocationService", "🚶 Still outside geofence")
            } else {
                Log.d("LocationService", "✅ Inside geofence, all good")
            }

            wasInsideGeofence = isInside

        } catch (e: Exception) {
            Log.e("LocationService", "❌ Error processing location", e)
        }
    }

    private fun calculateDistance(lat1: Double, lon1: Double, lat2: Double, lon2: Double): Double {
        val earthRadius = 6371000.0

        val dLat = Math.toRadians(lat2 - lat1)
        val dLon = Math.toRadians(lon2 - lon1)

        val a = sin(dLat / 2).pow(2) +
                cos(Math.toRadians(lat1)) * cos(Math.toRadians(lat2)) *
                sin(dLon / 2).pow(2)

        val c = 2 * atan2(sqrt(a), sqrt(1 - a))

        return earthRadius * c
    }

    private fun hasLocationPermission(): Boolean {
        return ContextCompat.checkSelfPermission(
            this,
            Manifest.permission.ACCESS_FINE_LOCATION
        ) == PackageManager.PERMISSION_GRANTED
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
            .setContentTitle("Exit Note Active")
            .setContentText("Monitoring location every 20s")
            .setSmallIcon(android.R.drawable.ic_menu_mylocation)
            .setContentIntent(pendingIntent)
            .setOngoing(true)
            .setPriority(NotificationCompat.PRIORITY_LOW)
            .setCategory(NotificationCompat.CATEGORY_SERVICE)
            .addAction(
                android.R.drawable.ic_menu_close_clear_cancel,
                "Stop",
                stopPendingIntent
            )
            .build()
    }

    override fun onDestroy() {
        super.onDestroy()
        try {
            fusedLocationClient.removeLocationUpdates(locationCallback)
            Log.d("LocationService", "🛑 Service destroyed, location updates stopped")
        } catch (e: Exception) {
            Log.e("LocationService", "❌ Error removing location updates", e)
        }
        serviceScope.cancel()
    }

    override fun onBind(intent: Intent?): IBinder? = null
}