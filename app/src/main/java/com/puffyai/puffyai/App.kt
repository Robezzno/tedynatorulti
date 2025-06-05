package com.puffyai.puffyai

import android.app.Application
import dagger.hilt.android.HiltAndroidApp
import timber.log.Timber // For logging, if you decide to integrate a logging library

@HiltAndroidApp
class App : Application() {
    override fun onCreate() {
        super.onCreate()

        // Global exception handler for uncaught exceptions
        Thread.setDefaultUncaughtExceptionHandler { thread, throwable ->
            // Log the exception. In a real app, you'd send this to Crashlytics or another crash reporting tool.
            // For now, we'll just print to Logcat.
            // Timber.e(throwable, "Uncaught exception on thread: %s", thread.name)
            android.util.Log.e("App", "Uncaught exception on thread: ${thread.name}", throwable)

            // Optionally, you can restart the app or show a custom error dialog
            // For simplicity, we'll let the system handle the crash after logging.
        }

        // Initialize Timber for logging in debug builds
        if (BuildConfig.DEBUG) {
            Timber.plant(Timber.DebugTree())
        }
    }
}