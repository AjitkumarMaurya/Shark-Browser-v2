package ajit.browser.focus.screengrab

import android.content.Intent
import android.support.test.rule.ActivityTestRule
import android.support.test.runner.AndroidJUnit4
import okhttp3.mockwebserver.MockResponse
import okhttp3.mockwebserver.MockWebServer
import org.junit.Before
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith
import ajit.browser.focus.activity.MainActivity
import ajit.browser.focus.annotation.ScreengrabOnly
import org.mozilla.focus.autobot.bookmark
import org.mozilla.focus.autobot.session
import ajit.browser.focus.helper.BeforeTestTask
import ajit.browser.focus.utils.AndroidTestUtils
import tools.fastlane.screengrab.FalconScreenshotStrategy
import tools.fastlane.screengrab.Screengrab
import java.io.IOException

@ajit.browser.focus.annotation.ScreengrabOnly
@RunWith(AndroidJUnit4::class)
class BookmarksScreenshot : BaseScreenshot() {

    private lateinit var webServer: MockWebServer

    @JvmField
    @Rule
    var activityTestRule: ActivityTestRule<ajit.browser.focus.activity.MainActivity> = object : ActivityTestRule<ajit.browser.focus.activity.MainActivity>(ajit.browser.focus.activity.MainActivity::class.java, true, false) {
        override fun beforeActivityLaunched() {
            super.beforeActivityLaunched()

            webServer = MockWebServer()
            try {
                webServer.enqueue(MockResponse()
                        .setBody(ajit.browser.focus.utils.AndroidTestUtils.readTestAsset(HTML_FILE_GET_LOCATION))
                        .addHeader("Set-Cookie", "sphere=battery; Expires=Wed, 21 Oct 2035 07:28:00 GMT;"))
                webServer.enqueue(MockResponse()
                        .setBody(ajit.browser.focus.utils.AndroidTestUtils.readTestAsset(HTML_FILE_GET_LOCATION))
                        .addHeader("Set-Cookie", "sphere=battery; Expires=Wed, 21 Oct 2035 07:28:00 GMT;"))
                webServer.start()
            } catch (e: IOException) {
                throw AssertionError("Could not start web server", e)
            }
        }

        override fun afterActivityFinished() {
            super.afterActivityFinished()

            try {
                webServer.close()
                webServer.shutdown()
            } catch (e: IOException) {
                throw AssertionError("Could not stop web server", e)
            }
        }
    }

    @Before
    fun setUp() {
        ajit.browser.focus.helper.BeforeTestTask.Builder()
                .build()
                .execute()
        activityTestRule.launchActivity(Intent())
        Screengrab.setDefaultScreenshotStrategy(FalconScreenshotStrategy(activityTestRule.activity))
    }

    @Test
    fun screenshotBookmarks() {

        session {
            loadPageFromHomeSearchField(activityTestRule.activity, webServer.url(TEST_SITE_1).toString())
            clickBrowserMenu()
            toggleBookmark()
            checkAddBookmarkSnackbarIsDisplayed()
            takeScreenshotViaFastlane(ajit.browser.focus.screengrab.ScreenshotNamingUtils.BOOKMARK_ADD_SNACKBAR)
            clickBookmarkSnackbarEdit()
            takeScreenshotViaFastlane(ajit.browser.focus.screengrab.ScreenshotNamingUtils.BOOKMARK_EDIT)
        }

        bookmark {
            updateBookmarkName(MOCK_BOOKMARK_CONTENT)
            clickSave()
            checkBookmarkUpdatedToastIsDisplayed(activityTestRule.activity)
            takeScreenshotViaFastlane(ajit.browser.focus.screengrab.ScreenshotNamingUtils.BOOKMARK_UPDATED)
            clickBrowserMenu()
            clickMenuBookmarks()
            takeScreenshotViaFastlane(ajit.browser.focus.screengrab.ScreenshotNamingUtils.BOOKMARK_LIST)
            clickListItemActionMenu(0)
            checkItemMenuEditIsDisplayed()
            takeScreenshotViaFastlane(ajit.browser.focus.screengrab.ScreenshotNamingUtils.BOOKMARK_LIST_MENU)
            clickItemMenuEdit()
            pressBack()
            pressBack()
        }

        session {
            clickBrowserMenu()
            toggleBookmark()
            checkRemoveBookmarkToastIsDisplayed(activityTestRule.activity)
            takeScreenshotViaFastlane(ajit.browser.focus.screengrab.ScreenshotNamingUtils.BOOKMARK_REMOVED)
        }
    }

    companion object {
        private const val TEST_SITE_1 = "/site1/"
        private val HTML_FILE_GET_LOCATION = "get_location.html"
        private val MOCK_BOOKMARK_CONTENT = "mock_bookmark_content"
    }
}