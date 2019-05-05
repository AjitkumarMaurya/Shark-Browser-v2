/* -*- Mode: Java; c-basic-offset: 4; tab-width: 4; indent-tabs-mode: nil; -*-
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/. */

package ajit.browser.lightning.privately

import android.arch.lifecycle.ViewModelProviders
import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.content.IntentFilter
import android.os.Bundle
import android.support.annotation.CheckResult
import android.support.v4.app.Fragment
import android.support.v4.content.LocalBroadcastManager
import android.view.View
import android.widget.Toast
import ajit.browser.lightning.BuildConfig
import ajit.browser.lightning.R
import ajit.browser.focus.activity.BaseActivity
import ajit.browser.focus.activity.MainActivity
import ajit.browser.focus.download.DownloadInfoManager
import ajit.browser.focus.fragment.BrowserFragment
import ajit.browser.focus.navigation.ScreenNavigator
import ajit.browser.focus.navigation.ScreenNavigator.BrowserScreen
import ajit.browser.focus.navigation.ScreenNavigator.HomeScreen
import ajit.browser.focus.navigation.ScreenNavigator.Screen
import ajit.browser.focus.navigation.ScreenNavigator.UrlInputScreen
import ajit.browser.focus.telemetry.TelemetryWrapper
import ajit.browser.focus.urlinput.UrlInputFragment
import ajit.browser.focus.utils.Constants
import ajit.browser.focus.widget.FragmentListener
import ajit.browser.focus.widget.FragmentListener.TYPE
import ajit.browser.lightning.component.PrivateSessionNotificationService
import ajit.browser.lightning.privately.home.PrivateHomeFragment
import ajit.browser.rocket.tabs.SessionManager
import ajit.browser.rocket.tabs.TabViewProvider
import ajit.browser.rocket.tabs.TabsSessionProvider

class PrivateModeActivity : ajit.browser.focus.activity.BaseActivity(),
        ajit.browser.focus.widget.FragmentListener,
        ajit.browser.focus.navigation.ScreenNavigator.Provider,
        ajit.browser.focus.navigation.ScreenNavigator.HostActivity,
        ajit.browser.rocket.tabs.TabsSessionProvider.SessionHost {

    private val LOG_TAG = "PrivateModeActivity"
    private var sessionManager: SessionManager? = null
    private lateinit var tabViewProvider: PrivateTabViewProvider
    private lateinit var sharedViewModel: SharedViewModel
    private lateinit var screenNavigator: ajit.browser.focus.navigation.ScreenNavigator
    private lateinit var uiMessageReceiver: BroadcastReceiver
    private lateinit var snackBarContainer: View

    override fun onCreate(savedInstanceState: Bundle?) {
        // we don't keep any state if user leave Private-mode
        super.onCreate(null)

        tabViewProvider = PrivateTabViewProvider(this)
        screenNavigator = ajit.browser.focus.navigation.ScreenNavigator(this)

        val exitEarly = handleIntent(intent)
        if (exitEarly) {
            pushToBack()
            return
        }

        setContentView(R.layout.activity_private_mode)

        snackBarContainer = findViewById(R.id.container)
        makeStatusBarTransparent()

        initViewModel()
        initBroadcastReceivers()

        screenNavigator.popToHomeScreen(false)
    }

    private fun initViewModel() {
        sharedViewModel = ViewModelProviders.of(this).get(SharedViewModel::class.java)
        sharedViewModel.urlInputState().value = false
    }

    override fun onResume() {
        super.onResume()
        val uiActionFilter = IntentFilter()
        uiActionFilter.addCategory(ajit.browser.focus.utils.Constants.CATEGORY_FILE_OPERATION)
        uiActionFilter.addAction(ajit.browser.focus.utils.Constants.ACTION_NOTIFY_RELOCATE_FINISH)
        LocalBroadcastManager.getInstance(this).registerReceiver(uiMessageReceiver, uiActionFilter)
    }

    override fun onPause() {
        super.onPause()
        LocalBroadcastManager.getInstance(this).unregisterReceiver(uiMessageReceiver)
    }

    override fun onDestroy() {
        super.onDestroy()
        stopPrivateMode()
        sessionManager?.destroy()
    }

    override fun applyLocale() {}

    override fun onNotified(from: Fragment, type: ajit.browser.focus.widget.FragmentListener.TYPE, payload: Any?) {
        when (type) {
            TYPE.TOGGLE_PRIVATE_MODE -> pushToBack()
            TYPE.SHOW_URL_INPUT -> showUrlInput(payload)
            TYPE.DISMISS_URL_INPUT -> dismissUrlInput()
            TYPE.OPEN_URL_IN_CURRENT_TAB -> openUrl(payload)
            TYPE.OPEN_URL_IN_NEW_TAB -> openUrl(payload)
            TYPE.DROP_BROWSING_PAGES -> dropBrowserFragment()
            else -> {
            }
        }
    }

    override fun onBackPressed() {
        if (supportFragmentManager.isStateSaved) {
            return
        }

        val handled = screenNavigator.visibleBrowserScreen?.onBackPressed() ?: false
        if (handled) {
            return
        }

        if (!this.screenNavigator.canGoBack()) {
            finish()
            return
        }

        super.onBackPressed()
    }

    override fun getSessionManager(): SessionManager {
        if (sessionManager == null) {
            sessionManager = SessionManager(tabViewProvider)
        }

        // we just created it, it definitely not null
        return sessionManager!!
    }

    override fun onNewIntent(intent: Intent?) {
        super.onNewIntent(intent)

        val exitEarly = handleIntent(intent)
        if (exitEarly) {
            return
        }
    }

    private fun dropBrowserFragment() {
        stopPrivateMode()
        Toast.makeText(this, R.string.private_browsing_erase_done, Toast.LENGTH_LONG).show()
    }

    override fun getScreenNavigator(): ajit.browser.focus.navigation.ScreenNavigator = screenNavigator

    override fun getBrowserScreen(): BrowserScreen {
        return supportFragmentManager.findFragmentById(R.id.browser) as BrowserFragment
    }

    override fun createFirstRunScreen(): Screen {
        if (ajit.browser.lightning.BuildConfig.DEBUG) {
            throw RuntimeException("PrivateModeActivity should never show first-run")
        }
        TODO("PrivateModeActivity should never show first-run")
    }

    override fun createHomeScreen(): HomeScreen {
        return PrivateHomeFragment.create()
    }

    override fun createUrlInputScreen(url: String?, parentFragmentTag: String?): UrlInputScreen {
        return UrlInputFragment.create(url, null, false)
    }

    private fun pushToBack() {
        val intent = Intent(this, ajit.browser.focus.activity.MainActivity::class.java)
        intent.addFlags(Intent.FLAG_ACTIVITY_TASK_ON_HOME)
        startActivity(intent)
        overridePendingTransition(0, R.anim.pb_exit)
    }

    private fun showUrlInput(payload: Any?) {
        val url = payload?.toString() ?: ""
        screenNavigator.addUrlScreen(url)
        sharedViewModel.urlInputState().value = true
    }

    private fun dismissUrlInput() {
        screenNavigator.popUrlScreen()
        sharedViewModel.urlInputState().value = false
    }

    private fun openUrl(payload: Any?) {
        val url = payload?.toString() ?: ""

        ViewModelProviders.of(this)
                .get(SharedViewModel::class.java)
                .setUrl(url)

        dismissUrlInput()
        startPrivateMode()
        ajit.browser.focus.navigation.ScreenNavigator.get(this).showBrowserScreen(url, false, false)
    }

    private fun makeStatusBarTransparent() {
        var visibility = window.decorView.systemUiVisibility
        // do not overwrite existing value
        visibility = visibility or (View.SYSTEM_UI_FLAG_LAYOUT_STABLE or View.SYSTEM_UI_FLAG_LAYOUT_FULLSCREEN)
        window.decorView.systemUiVisibility = visibility
    }

    private fun startPrivateMode() {
        PrivateSessionNotificationService.start(this)
    }

    private fun stopPrivateMode() {
        PrivateSessionNotificationService.stop(this)
        PrivateMode.sanitize(this.applicationContext)
        ajit.browser.rocket.tabs.TabViewProvider.purify(this)
    }

    @CheckResult
    private fun handleIntent(intent: Intent?): Boolean {

        if (intent?.action == PrivateMode.INTENT_EXTRA_SANITIZE) {
            TelemetryWrapper.erasePrivateModeNotification()
            stopPrivateMode()
            Toast.makeText(this, R.string.private_browsing_erase_done, Toast.LENGTH_LONG).show()
            finishAndRemoveTask()
            return true
        }
        return false
    }

    private fun initBroadcastReceivers() {
        uiMessageReceiver = object : BroadcastReceiver() {
            override fun onReceive(context: Context, intent: Intent) {
                if (intent.action == ajit.browser.focus.utils.Constants.ACTION_NOTIFY_RELOCATE_FINISH) {
                    ajit.browser.focus.download.DownloadInfoManager.getInstance().showOpenDownloadSnackBar(intent.getLongExtra(ajit.browser.focus.utils.Constants.EXTRA_ROW_ID, -1), snackBarContainer, LOG_TAG)
                }
            }
        }
    }
}
