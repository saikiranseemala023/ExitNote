package uk.ac.tees.mad.exitnote.data.local

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase

@Database(
    entities = [LocationHistoryEntity::class, ExitEventEntity::class],
    version = 1,
    exportSchema = false
)
abstract class ExitNoteDatabase : RoomDatabase() {
    abstract fun locationHistoryDao(): LocationHistoryDao
    abstract fun exitEventDao(): ExitEventDao

    companion object {
        @Volatile
        private var INSTANCE: ExitNoteDatabase? = null

        fun getDatabase(context: Context): ExitNoteDatabase {
            return INSTANCE ?: synchronized(this) {
                val instance = Room.databaseBuilder(
                    context.applicationContext,
                    ExitNoteDatabase::class.java,
                    "exit_note_database"
                ).build()
                INSTANCE = instance
                instance
            }
        }
    }
}