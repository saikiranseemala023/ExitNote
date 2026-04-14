package uk.ac.tees.mad.exitnote.data.local

import android.content.Context
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.preferencesDataStore

val Context.exitNoteDataStore by preferencesDataStore(
    name = "exit_note_prefs"
)
