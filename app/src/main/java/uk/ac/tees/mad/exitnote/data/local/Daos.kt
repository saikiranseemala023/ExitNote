package uk.ac.tees.mad.exitnote.data.local

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.Query
import kotlinx.coroutines.flow.Flow

@Dao
interface LocationHistoryDao {
    @Insert
    suspend fun insertLocation(location: LocationHistoryEntity)

    @Query("SELECT * FROM location_history ORDER BY timestamp DESC LIMIT 100")
    fun getAllLocations(): Flow<List<LocationHistoryEntity>>

    @Query("SELECT * FROM location_history WHERE isExitEvent = 1 ORDER BY timestamp DESC LIMIT 10")
    fun getExitEvents(): Flow<List<LocationHistoryEntity>>

    @Query("DELETE FROM location_history WHERE timestamp < :cutoffTime")
    suspend fun deleteOldLocations(cutoffTime: Long)

    @Query("DELETE FROM location_history")
    suspend fun deleteAll()
}

@Dao
interface ExitEventDao {
    @Insert
    suspend fun insertExitEvent(event: ExitEventEntity)

    @Query("SELECT * FROM exit_events ORDER BY exitTime DESC LIMIT 20")
    fun getAllExitEvents(): Flow<List<ExitEventEntity>>

    @Query("SELECT * FROM exit_events ORDER BY exitTime DESC LIMIT 1")
    suspend fun getLastExitEvent(): ExitEventEntity?

    @Query("DELETE FROM exit_events WHERE exitTime < :cutoffTime")
    suspend fun deleteOldEvents(cutoffTime: Long)

    @Query("DELETE FROM exit_events")
    suspend fun deleteAll()
}