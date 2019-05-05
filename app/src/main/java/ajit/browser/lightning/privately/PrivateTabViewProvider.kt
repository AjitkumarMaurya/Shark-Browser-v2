package ajit.browser.lightning.privately

import android.app.Activity
import android.webkit.WebSettings
import ajit.browser.focus.web.WebViewProvider
import ajit.browser.rocket.tabs.TabView
import ajit.browser.rocket.tabs.TabViewProvider

class PrivateTabViewProvider(private val host: Activity) : ajit.browser.rocket.tabs.TabViewProvider() {

    override fun create(): ajit.browser.rocket.tabs.TabView {
        return ajit.browser.focus.web.WebViewProvider.create(host, null, WebViewSettingsHook) as ajit.browser.rocket.tabs.TabView
    }

    object WebViewSettingsHook : ajit.browser.focus.web.WebViewProvider.WebSettingsHook {
        override fun modify(settings: WebSettings?) {
            if (settings == null) {
                return
            }

            settings.setSupportMultipleWindows(false)
        }
    }
}