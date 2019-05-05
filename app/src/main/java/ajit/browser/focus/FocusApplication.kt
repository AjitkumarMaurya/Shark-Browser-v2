/* -*- Mode: Java; c-basic-offset: 4; tab-width: 20; indent-tabs-mode: nil; -*-
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/. */

package ajit.browser.focus

import ajit.browser.focus.telemetry.TelemetryWrapper
import ajit.browser.lightning.R
import ajit.browser.lightning.privately.PrivateMode.Companion.PRIVATE_PROCESS_NAME
import ajit.browser.lightning.privately.PrivateMode.Companion.WEBVIEW_FOLDER_NAME
import ajit.browser.lightning.privately.PrivateModeActivity
import android.app.Activity
import android.os.Bundle
import android.preference.PreferenceManager
import com.squareup.leakcanary.LeakCanary
import org.mozilla.focus.utils.AdjustHelper
import java.io.File

class FocusApplication : ajit.browser.focus.locale.LocaleAwareApplication() {

    lateinit var partnerActivator: ajit.browser.lightning.partner.PartnerActivator
    var isInPrivateProcess = false

    // Override getCacheDir cause when we create a WebView, it'll asked the application's
    // getCacheDir() method and create WebView specific cache.
    override fun getCacheDir(): File {
        if (isInPrivateProcess) {
            return File(super.getCacheDir().absolutePath + "-" + PRIVATE_PROCESS_NAME)
        }
        return super.getCacheDir()
    }

    // Override getCacheDir cause when we create a WebView, it'll asked the application's
    // getDir() method and create WebView specific files.
    override fun getDir(name: String?, mode: Int): File {
        if (name == WEBVIEW_FOLDER_NAME && isInPrivateProcess) {
            return super.getDir("$name-$PRIVATE_PROCESS_NAME", mode)
        }
        return super.getDir(name, mode)
    }

    override fun onCreate() {
        super.onCreate()
        if (LeakCanary.isInAnalyzerProcess(this)) {
            // This process is dedicated to LeakCanary for heap analysis.
            // You should not init your app in this process.
            return
        }
        LeakCanary.install(this)

        PreferenceManager.setDefaultValues(this, R.xml.settings, false)
//ajit
        // Provide different strict mode penalty for ui testing and production code
      //  Inject.enableStrictMode()

        ajit.browser.focus.search.SearchEngineManager.getInstance().init(this)
        ajit.browser.lightning.content.NewsSourceManager.getInstance().init(this)

        TelemetryWrapper.init(this)
        AdjustHelper.setupAdjustIfNeeded(this)

        ajit.browser.focus.history.BrowsingHistoryManager.getInstance().init(this)
        ajit.browser.focus.screenshot.ScreenshotManager.getInstance().init(this)
        ajit.browser.focus.download.DownloadInfoManager.getInstance()
        ajit.browser.focus.download.DownloadInfoManager.init(this)
        // initialize the NotificationUtil to configure the default notification channel. This is required for API 26+
        ajit.browser.focus.notification.NotificationUtil.init(this)

        partnerActivator = ajit.browser.lightning.partner.PartnerActivator(this)
        partnerActivator.launch()

        monitorPrivateProcess()
    }

    /**
     *   We use PrivateModeActivity's existence to determine if we are in private mode (process)  or not. We don't use
     *   ActivityManager.getRunningAppProcesses() cause it sometimes return null.
     *
     *   The Application class should also rely on this flag to determine if it want to override getDir() and getCacheDir().
     *
     *  Note: we can be in private mode process but don't have any private session yet. ( e.g. We launched
     *  PrivateModeActivity but haven't create any tab yet)
     *
     */
    private fun monitorPrivateProcess() {
        registerActivityLifecycleCallbacks(object : ActivityLifecycleCallbacks {
            override fun onActivityPaused(activity: Activity?) {
            }

            override fun onActivityResumed(activity: Activity?) {
            }

            override fun onActivityStarted(activity: Activity?) {
            }

            override fun onActivityDestroyed(activity: Activity?) {
            }

            override fun onActivitySaveInstanceState(activity: Activity?, outState: Bundle?) {
            }

            override fun onActivityStopped(activity: Activity?) {
            }

            override fun onActivityCreated(activity: Activity?, savedInstanceState: Bundle?) {
                // once PrivateModeActivity exist, this process is for private mode
                if (activity is PrivateModeActivity) {
                    isInPrivateProcess = true
                }
            }
        })
    }
}
