package moc.itivo.officeworks

import android.app.Application
import dagger.hilt.android.HiltAndroidApp
import timber.log.Timber

@HiltAndroidApp
class OfficeworksApp : Application() {
    override fun onCreate() {
        super.onCreate()
        // Logs go to Logcat in debug builds only. A release build would plant a crash-reporting
        // tree here instead (e.g. one that records non-fatal errors).
        if (BuildConfig.DEBUG) Timber.plant(Timber.DebugTree())
    }
}
