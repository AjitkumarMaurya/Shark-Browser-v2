package ajit.browser.lightning.component

import android.content.Context
import android.content.Intent
import android.net.Uri
import android.support.annotation.CheckResult
import ajit.browser.focus.activity.MainActivity
import ajit.browser.focus.activity.SettingsActivity
import ajit.browser.focus.notification.RocketMessagingService
import ajit.browser.focus.telemetry.TelemetryWrapper
import ajit.browser.focus.utils.IntentUtils
import ajit.browser.focus.utils.SupportUtils
import ajit.browser.focus.widget.DefaultBrowserPreference

class LaunchIntentDispatcher {

    enum class LaunchMethod(val value: String) {
        EXTRA_BOOL_WEB_SEARCH("web_search"),
        EXTRA_BOOL_TEXT_SELECTION("text_selection"),
        EXTRA_BOOL_HOME_SCREEN_SHORTCUT("shortcut")
    }

    enum class Command(val value: String) {
        SET_DEFAULT("SET_DEFAULT")
    }

    enum class Action {
        NORMAL, HANDLED
    }

    companion object {
        /**
         * Now [LaunchIntentDispatcher.dispatch] is the universal entry to our app. All starting method should be analyzed here.
         * */
        @JvmStatic
        @CheckResult
        fun dispatch(context: Context, intent: Intent): Action? {

            /**
             * This extra is passed when we click our icon in mobile launcher
             * */
            if (intent.getBooleanExtra(LaunchMethod.EXTRA_BOOL_HOME_SCREEN_SHORTCUT.value, false)) {
                TelemetryWrapper.launchByHomeScreenShortcutEvent()
                return Action.NORMAL
            }

            /**
             * This extra is passed when we long click on some text and click "Search in Firefox Rocket"
             * */
            if (intent.getBooleanExtra(LaunchMethod.EXTRA_BOOL_TEXT_SELECTION.value, false)) {
                TelemetryWrapper.launchByExternalAppEvent(TelemetryWrapper.Extra_Value.TEXT_SELECTION)
                return Action.NORMAL
            }

            /**
             * This extra is passed when we long click on some text and click "Web Search"
             * */
            if (intent.getBooleanExtra(LaunchMethod.EXTRA_BOOL_WEB_SEARCH.value, false)) {
                TelemetryWrapper.launchByExternalAppEvent(TelemetryWrapper.Extra_Value.WEB_SEARCH)
                return Action.NORMAL
            }

            /**
             *  This intent is used when we want to set default browser on Android L, see [DefaultBrowserPreference]
             *  Called by the internal app, doesn't count as a launch event
             * */
            if (intent.getBooleanExtra(ajit.browser.focus.widget.DefaultBrowserPreference.EXTRA_RESOLVE_BROWSER, false)) {
                context.startActivity(Intent(context, ajit.browser.focus.activity.SettingsActivity::class.java))
                // called by internal app, doesn't count as a launch event
                return Action.HANDLED
            }
            /**
             * This extra is passed by the Notification (either [RocketMessagingService.onRemoteMessage] or System tray
             * if we have this extra, we want to show this url in a new tab
             */
            intent.getStringExtra(ajit.browser.focus.notification.RocketMessagingService.PUSH_OPEN_URL)?.run {

                intent.data = Uri.parse(this)
                intent.action = Intent.ACTION_VIEW
                intent.setClass(context, ajit.browser.focus.activity.MainActivity::class.java)
                intent.putExtra(ajit.browser.focus.utils.IntentUtils.EXTRA_OPEN_NEW_TAB, true)
            }

            /**
             * This extra is passed by the Notification (either [RocketMessagingService.onRemoteMessage] or System tray
             *  Called by the internal app, doesn't count as a launch event
             * */
            intent.getStringExtra(ajit.browser.focus.notification.RocketMessagingService.PUSH_COMMAND)?.apply {
                when (this) {
                    Command.SET_DEFAULT.value -> {
                        if (!ajit.browser.focus.utils.IntentUtils.openDefaultAppsSettings(context)) {
                            intent.action = Intent.ACTION_VIEW
                            intent.data = Uri.parse(ajit.browser.focus.utils.SupportUtils.getSumoURLForTopic(context, "rocket-default"))
                            return Action.NORMAL
                        } else {
                            return Action.HANDLED
                        }
                    }
                }
            }

            /**
             *  When notification received in bg, the intent 's action is also ACTION_MAIN.(https://goo.gl/sMLZhZ )
             * We've return early in previous check so they won't reach this line.
             * Their actions are recorded in [org.mozilla.focus.notification.NotificationActionBroadcastReceiver]
             * */
            if (Intent.ACTION_MAIN == intent.action) {
                TelemetryWrapper.launchByAppLauncherEvent()
                return Action.NORMAL
            }

            if (Intent.ACTION_VIEW == intent.action) {
                TelemetryWrapper.launchByExternalAppEvent(null)
                return Action.NORMAL
            }

            return Action.NORMAL
        }
    }
}
