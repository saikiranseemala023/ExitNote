package uk.ac.tees.mad.exitnote.service

import android.content.Context
import android.util.Log
import androidx.datastore.preferences.core.booleanPreferencesKey
import androidx.datastore.preferences.core.doublePreferencesKey
import androidx.datastore.preferences.core.floatPreferencesKey
import androidx.datastore.preferences.core.longPreferencesKey
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.preferencesDataStore
import androidx.work.CoroutineWorker
import androidx.work.WorkerParameters
import kotlinx.coroutines.flow.first
import uk.ac.tees.mad.exitnote.data.local.ExitEventEntity
import uk.ac.tees.mad.exitnote.data.local.ExitNoteDatabase
import uk.ac.tees.mad.exitnote.data.local.LocationHistoryEntity

private val Context.dataStore by preferencesDataStore(name = "exit_note_prefs")

class LocationMonitorWorker(
    context: Context,
    params: WorkerParameters
) : CoroutineWorker(context, params) {

    private val locationManager = LocationManager(context)
    private val notificationHelper = NotificationHelper(context)
    private val database = ExitNoteDatabase.getDatabase(context)

    override suspend fun doWork(): Result {
        return try {
            val prefs = applicationContext.dataStore.data.first()

            val isTrackingEnabled = prefs[booleanPreferencesKey("is_tracking_enabled")] ?: false
            val isHomeSet = prefs[booleanPreferencesKey("is_home_set")] ?: false

            if (!isTrackingEnabled || !isHomeSet) {
                return Result.success()
            }

            val homeLat = prefs[doublePreferencesKey("home_latitude")] ?: return Result.success()
            val homeLon = prefs[doublePreferencesKey("home_longitude")] ?: return Result.success()
            val radius = prefs[floatPreferencesKey("home_radius")] ?: 250f
            val lastExitTime = prefs[longPreferencesKey("last_exit_time")]

            val currentLocation = locationManager.getCurrentLocation()

            if (currentLocation != null) {
                val distance = locationManager.calculateDistance(
                    currentLocation.latitude,
                    currentLocation.longitude,
                    homeLat,
                    homeLon
                )

                val isOutside = distance > radius

                database.locationHistoryDao().insertLocation(
                    LocationHistoryEntity(
                        latitude = currentLocation.latitude,
                        longitude = currentLocation.longitude,
                        timestamp = System.currentTimeMillis(),
                        isExitEvent = isOutside,
                        distanceFromHome = distance
                    )
                )

                if (isOutside) {
                    val currentTime = System.currentTimeMillis()
                    val timeSinceLastExit = lastExitTime?.let { currentTime - it } ?: Long.MAX_VALUE

                    if (timeSinceLastExit > 3600000) {
                        notificationHelper.showExitNotification()

                        database.exitEventDao().insertExitEvent(
                            ExitEventEntity(
                                exitTime = currentTime,
                                latitude = currentLocation.latitude,
                                longitude = currentLocation.longitude,
                                notificationShown = true,
                                userResponse = null
                            )
                        )

                        applicationContext.dataStore.edit { prefs ->
                            prefs[longPreferencesKey("last_exit_time")] = currentTime
                        }

                        Log.d("LocationWorker", "Exit detected, notification sent")
                    }
                }
            }

            Result.success()
        } catch (e: Exception) {
            Log.e("LocationWorker", "Error: ${e.message}")
            Result.retry()
        }
    }
}