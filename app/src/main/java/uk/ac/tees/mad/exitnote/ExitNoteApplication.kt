package uk.ac.tees.mad.exitnote

import android.app.Application
import android.content.Context

/**
 * Custom Application class for ExitNote
 *
 * Central place for:
 * - Global singletons / app-wide instances
 * - Application lifecycle management
 */
class ExitNoteApplication : Application() {

    // Simple singleton pattern - accessible from anywhere via ExitNoteApplication.instance
    companion object {
        lateinit var instance: ExitNoteApplication
            private set

        // Convenience getter for Context
        val context: Context
            get() = instance.applicationContext
    }

    override fun onCreate() {
        super.onCreate()
        instance = this
    }
}