/* -*- Mode: Java; c-basic-offset: 4; tab-width: 4; indent-tabs-mode: nil; -*-
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/. */

package ajit.browser.focus.tabs.tabtray

import android.graphics.Bitmap
import android.net.Uri
import android.webkit.ValueCallback
import android.webkit.WebChromeClient

import ajit.browser.rocket.tabs.Session
import ajit.browser.rocket.tabs.SessionManager
import ajit.browser.rocket.tabs.TabViewClient
import ajit.browser.rocket.tabs.TabViewEngineSession

import java.util.ArrayList

internal class TabsSessionModel(private val sessionManager: SessionManager) : ajit.browser.focus.tabs.tabtray.TabTrayContract.Model {
    private var modelObserver: SessionModelObserver? = null

    private val tabs = ArrayList<Session>()

    override fun loadTabs(listener: ajit.browser.focus.tabs.tabtray.TabTrayContract.Model.OnLoadCompleteListener?) {
        tabs.clear()
        tabs.addAll(sessionManager.getTabs())

        listener?.onLoadComplete()
    }

    override fun getTabs(): List<Session> {
        return tabs
    }

    override fun getFocusedTab(): Session? {
        return sessionManager.focusSession
    }

    override fun switchTab(tabPosition: Int) {
        if (tabPosition >= 0 && tabPosition < tabs.size) {
            val target = tabs[tabPosition]
            val latestTabs = sessionManager.getTabs()
            val exist = latestTabs.indexOf(target) != -1
            if (exist) {
                sessionManager.switchToTab(target.id)
            }
        } else {

        }
    }

    override fun removeTab(tabPosition: Int) {
        if (tabPosition >= 0 && tabPosition < tabs.size) {
            sessionManager.dropTab(tabs[tabPosition].id)
        } else {

        }
    }

    override fun clearTabs() {
        val tabs = sessionManager.getTabs()
        for (tab in tabs) {
            sessionManager.dropTab(tab.id)
        }
    }

    override fun subscribe(observer: ajit.browser.focus.tabs.tabtray.TabTrayContract.Model.Observer) {
        if (this.modelObserver == null) {
            this.modelObserver = object : SessionModelObserver() {
                override fun updateFailingUrl(url: String?, updateFromError: Boolean) {
                    // do nothing
                }

                override fun handleExternalUrl(url: String?): Boolean {
                    // do nothing
                    return false
                }

                override fun onShowFileChooser(es: TabViewEngineSession, filePathCallback: ValueCallback<Array<Uri>>?, fileChooserParams: WebChromeClient.FileChooserParams?): Boolean {
                    // do nothing
                    return false
                }

                override fun onTabModelChanged(session: Session) {
                    observer.onTabUpdate(session)
                }

                override fun onSessionCountChanged(count: Int) {
                    observer.onUpdate(sessionManager.getTabs())
                }

                override fun onHttpAuthRequest(callback: ajit.browser.rocket.tabs.TabViewClient.HttpAuthCallback, host: String?, realm: String?) {
                    // do nothing
                }
            }
        }
        sessionManager.register(this.modelObserver as SessionManager.Observer)
    }

    override fun unsubscribe() {
        if (modelObserver != null) {
            sessionManager.unregister(modelObserver as SessionManager.Observer)
            modelObserver = null
        }
    }

    private abstract class SessionModelObserver : SessionManager.Observer, Session.Observer {
        var session: Session? = null

        override fun onUrlChanged(session: Session, url: String?) {
            session.let { onTabModelChanged(it) }
        }

        override fun onTitleChanged(session: Session, title: String?) {
            session.let { onTabModelChanged(it) }
        }

        override fun onReceivedIcon(icon: Bitmap?) {
            session?.let { onTabModelChanged(it) }
        }

        override fun onFocusChanged(session: Session?, factor: SessionManager.Factor) {
            this.session?.let { it.unregister(this) }
            this.session = session
            this.session?.let { it.register(this) }
        }

        internal abstract fun onTabModelChanged(session: Session)
    }
}
