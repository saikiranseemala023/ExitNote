package uk.ac.tees.mad.exitnote.data.local

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "location_history")
data class LocationHistoryEntity(
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0,
    val latitude: Double,
    val longitude: Double,
    val timestamp: Long,
    val isExitEvent: Boolean,
    val distanceFromHome: Double?
)

@Entity(tableName = "exit_events")
data class ExitEventEntity(
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0,
    val exitTime: Long,
    val latitude: Double,
    val longitude: Double,
    val notificationShown: Boolean,
    val userResponse: String?
)
