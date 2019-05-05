package ajit.browser.lightning.periodic

import android.content.Context
import android.content.SharedPreferences
import android.preference.PreferenceManager
import androidx.work.Worker
import androidx.work.WorkerParameters
import ajit.browser.focus.telemetry.TelemetryWrapper
import ajit.browser.focus.utils.AppConfigWrapper
import ajit.browser.focus.utils.DialogUtils

class FirstLaunchWorker(context: Context, workerParams: WorkerParameters) : Worker(context, workerParams) {

    companion object {
        val TAG: String = FirstLaunchWorker::class.java.simpleName
        val ACTION: String = ajit.browser.lightning.BuildConfig.APPLICATION_ID + ".action." + TAG

        const val TIMER_DISABLED = 0
        const val TIMER_SUSPEND = -1

        private const val PREF_KEY_BOOLEAN_NOTIFICATION_FIRED: String = "pref-key-boolean-notification-fired"

        fun isNotificationFired(context: Context, default: Boolean = false): Boolean {
            return getSharedPreference(context).getBoolean(PREF_KEY_BOOLEAN_NOTIFICATION_FIRED, default)
        }

        fun setNotificationFired(context: Context, value: Boolean) {
            val edit = getSharedPreference(context).edit()
            edit.putBoolean(PREF_KEY_BOOLEAN_NOTIFICATION_FIRED, value)
            edit.apply()
        }

        private fun getSharedPreference(context: Context): SharedPreferences {
            return PreferenceManager.getDefaultSharedPreferences(context)
        }
    }

    override fun doWork(): Result {
        val message = ajit.browser.focus.utils.AppConfigWrapper.getFirstLaunchNotificationMessage()
        ajit.browser.focus.utils.DialogUtils.showDefaultSettingNotification(applicationContext, message)
        TelemetryWrapper.showFirstrunNotification(ajit.browser.focus.utils.AppConfigWrapper.getFirstLaunchWorkerTimer(), message)

        setNotificationFired(applicationContext, true)
        return Result.success()
    }
}