/* -*- Mode: Java; c-basic-offset: 4; tab-width: 4; indent-tabs-mode: nil; -*-
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/. */

package ajit.browser.focus.tabs.tabtray

import android.graphics.Bitmap
import android.os.Bundle
import android.os.Message
import android.view.View
import org.junit.Assert
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith
import org.mockito.ArgumentCaptor
import org.mockito.ArgumentMatchers.any
import org.mockito.ArgumentMatchers.anyInt
import org.mockito.Captor
import org.mockito.Mock
import org.mockito.Mockito
import org.mockito.Mockito.never
import org.mockito.Mockito.verify
import org.mockito.MockitoAnnotations
import org.mozilla.rocket.tabs.Session
import org.mozilla.rocket.tabs.SessionManager
import ajit.browser.rocket.tabs.SiteIdentity
import ajit.browser.rocket.tabs.TabChromeClient
import ajit.browser.rocket.tabs.TabView
import ajit.browser.rocket.tabs.TabViewClient
import ajit.browser.rocket.tabs.TabViewProvider
import ajit.browser.rocket.tabs.utils.TabUtil
import ajit.browser.rocket.tabs.web.DownloadCallback
import org.robolectric.RobolectricTestRunner

@RunWith(RobolectricTestRunner::class)
class TabTrayPresenterTest {

    private lateinit var tabTrayPresenter: ajit.browser.focus.tabs.tabtray.TabTrayPresenter

    @Mock
    private val tabTrayContractView: ajit.browser.focus.tabs.tabtray.TabTrayContract.View? = null

    @Mock
    private lateinit var tabsSessionModel: TabsSessionModel

    @Captor
    private lateinit var tabListCaptor: ArgumentCaptor<List<Session>>

    @Before
    fun setUp() {
        MockitoAnnotations.initMocks(this)
        tabTrayPresenter = ajit.browser.focus.tabs.tabtray.TabTrayPresenter(tabTrayContractView, tabsSessionModel)
    }

    @Test
    fun viewReady_showFocusedTab() {
        Mockito.`when`(tabsSessionModel.tabs).thenReturn(listOf())
        this.tabTrayPresenter.viewReady()
        verify<ajit.browser.focus.tabs.tabtray.TabTrayContract.View>(this.tabTrayContractView).closeTabTray()

        Mockito.`when`(tabsSessionModel.tabs).thenReturn(listOf(Session(), Session(), Session()))
        this.tabTrayPresenter.viewReady()
        verify<ajit.browser.focus.tabs.tabtray.TabTrayContract.View>(this.tabTrayContractView).showFocusedTab(anyInt())
    }

    @Test
    fun viewReady_tabCountChanged_viewRefreshDataCalled() {
        val session = SessionManager(DefaultTabViewProvider())

        // Prepare some tabs
        session.addTab("url", ajit.browser.rocket.tabs.utils.TabUtil.argument(null, false, true))
        session.addTab("url", ajit.browser.rocket.tabs.utils.TabUtil.argument(null, false, true))
        session.addTab("url", ajit.browser.rocket.tabs.utils.TabUtil.argument(null, false, true))
        Assert.assertEquals(3, session.getTabs().size)

        val presenter = ajit.browser.focus.tabs.tabtray.TabTrayPresenter(tabTrayContractView, TabsSessionModel(session))

        // view is not ready yet, add tab should not trigger refreshData
        verify<ajit.browser.focus.tabs.tabtray.TabTrayContract.View>(this.tabTrayContractView, never()).refreshData(tabListCaptor.capture(), any())

        // OK we are ready
        presenter.viewReady()

        // Session count changed
        session.addTab("url", ajit.browser.rocket.tabs.utils.TabUtil.argument(null, false, true))
        Assert.assertEquals(4, session.getTabs().size)

        // Assert refresh data is called, with new tab list of size=4
        verify<ajit.browser.focus.tabs.tabtray.TabTrayContract.View>(this.tabTrayContractView).refreshData(tabListCaptor.capture(), any())
        Assert.assertEquals(4, tabListCaptor.value.size)
    }

    private class DefaultTabViewProvider : ajit.browser.rocket.tabs.TabViewProvider() {

        override fun create(): ajit.browser.rocket.tabs.TabView {
            return DefaultTabView()
        }
    }

    private class DefaultTabView : ajit.browser.rocket.tabs.TabView {

        private var url: String? = null

        override fun setContentBlockingEnabled(enabled: Boolean) {
        }

        override fun setImageBlockingEnabled(enabled: Boolean) {
        }

        override fun isBlockingEnabled(): Boolean {
            return false
        }

        override fun performExitFullScreen() {
        }

        override fun setViewClient(viewClient: ajit.browser.rocket.tabs.TabViewClient?) {
        }

        override fun setChromeClient(chromeClient: ajit.browser.rocket.tabs.TabChromeClient?) {
        }

        override fun setDownloadCallback(callback: ajit.browser.rocket.tabs.web.DownloadCallback?) {
        }

        override fun setFindListener(callback: ajit.browser.rocket.tabs.TabView.FindListener?) {
        }

        override fun onPause() {
        }

        override fun onResume() {
        }

        override fun destroy() {
        }

        override fun reload() {
        }

        override fun stopLoading() {
        }

        override fun getUrl(): String? {
            return this.url
        }

        override fun getTitle(): String? {
            return null
        }

        @ajit.browser.rocket.tabs.SiteIdentity.SecurityState
        override fun getSecurityState(): Int {
            return ajit.browser.rocket.tabs.SiteIdentity.UNKNOWN
        }

        override fun loadUrl(url: String) {
            this.url = url
        }

        override fun cleanup() {
        }

        override fun goForward() {
        }

        override fun goBack() {
        }

        override fun canGoForward(): Boolean {
            return false
        }

        override fun canGoBack(): Boolean {
            return false
        }

        override fun restoreViewState(inState: Bundle) {
        }

        override fun saveViewState(outState: Bundle) {
        }

        override fun insertBrowsingHistory() {
        }

        override fun getView(): View? {
            return null
        }

        override fun buildDrawingCache(autoScale: Boolean) {
        }

        override fun getDrawingCache(autoScale: Boolean): Bitmap? {
            return null
        }

        override fun bindOnNewWindowCreation(msg: Message) {
        }
    }
}
