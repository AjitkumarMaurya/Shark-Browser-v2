package ajit.browser.focus.autobot

import android.app.Activity
import android.content.Intent
import android.support.test.InstrumentationRegistry
import android.support.test.espresso.Espresso.onView
import android.support.test.espresso.action.Tap
import android.support.test.espresso.action.ViewActions.click
import android.support.test.espresso.action.ViewActions.pressImeActionButton
import android.support.test.espresso.action.ViewActions.replaceText

import android.support.test.espresso.assertion.ViewAssertions.doesNotExist
import android.support.test.espresso.assertion.ViewAssertions.matches
import android.support.test.espresso.matcher.RootMatchers
import android.support.test.espresso.matcher.ViewMatchers.isDisplayed
import android.support.test.espresso.matcher.ViewMatchers.withClassName
import android.support.test.espresso.matcher.ViewMatchers.withId
import android.support.test.espresso.matcher.ViewMatchers.withText
import android.support.test.uiautomator.UiDevice
import android.support.test.uiautomator.UiSelector
import android.view.View
import org.hamcrest.Matchers
import org.mozilla.focus.R
import ajit.browser.focus.activity.MainActivity
import ajit.browser.focus.helper.ScreenshotIdlingResource
import ajit.browser.focus.helper.SessionLoadedIdlingResource
import ajit.browser.focus.utils.AndroidTestUtils

inline fun session(func: SessionRobot.() -> Unit) = SessionRobot().apply(func)
class SessionRobot : MenuRobot() {

    private lateinit var sessionLoadedIdlingResource: ajit.browser.focus.helper.SessionLoadedIdlingResource
    private lateinit var screenshotIdlingResource: ajit.browser.focus.helper.ScreenshotIdlingResource

    private fun loadLocalPage(activity: ajit.browser.focus.activity.MainActivity, url: String) {
        // TODO find a way to remove the activity reference
        sessionLoadedIdlingResource = ajit.browser.focus.helper.SessionLoadedIdlingResource(activity)

        // Enter test site url
        onView(Matchers.allOf<View>(withId(R.id.url_edit), isDisplayed())).perform(replaceText(url), pressImeActionButton())
        onView(Matchers.allOf(withId(R.id.display_url), isDisplayed())).check(matches(withText(url)))
    }
    // Load and check if the test site is loaded
    private fun loadPage(activity: ajit.browser.focus.activity.MainActivity, url: String) {
        // TODO find a way to remove the activity reference
        sessionLoadedIdlingResource = ajit.browser.focus.helper.SessionLoadedIdlingResource(activity)

        // Enter test site url
        onView(Matchers.allOf<View>(withId(R.id.url_edit), isDisplayed())).perform(replaceText(url), pressImeActionButton())

        runWithIdleRes(sessionLoadedIdlingResource) {
            onView(Matchers.allOf(withId(R.id.display_url), isDisplayed())).check(matches(withText(url)))
        }
    }

    fun loadPageFromHomeSearchField(activity: ajit.browser.focus.activity.MainActivity, url: String) {
        // Click search field
        onView(Matchers.allOf<View>(withId(R.id.home_fragment_fake_input), isDisplayed())).perform(click())
        loadPage(activity, url)
    }

    fun loadPageFromUrlBar(activity: ajit.browser.focus.activity.MainActivity, url: String) {
        onView(Matchers.allOf<View>(withId(R.id.display_url), isDisplayed())).perform(click())
        loadPage(activity, url)
    }

    fun loadLocalPageFromUrlBar(activity: ajit.browser.focus.activity.MainActivity, url: String) {
        onView(Matchers.allOf<View>(withId(R.id.display_url), isDisplayed())).perform(click())
        loadLocalPage(activity, url)
    }

    /** Bookmark related */
    fun toggleBookmark() {
        onView(withId(R.id.action_bookmark)).perform(click())
    }

    fun checkAddBookmarkSnackbarIsDisplayed() {
        onView(Matchers.allOf(withId(android.support.design.R.id.snackbar_action), withText(R.string.bookmark_saved_edit)))
                .check(matches(isDisplayed()))
    }

    fun clickBookmarkSnackbarEdit() {
        onView(Matchers.allOf(withId(android.support.design.R.id.snackbar_action), withText(R.string.bookmark_saved_edit))).perform(click())
    }

    fun checkRemoveBookmarkToastIsDisplayed(activity: Activity) {
        onView(withText(R.string.bookmark_removed))
                .inRoot(RootMatchers.withDecorView(Matchers.not<View>(Matchers.`is`<View>(activity.window.decorView))))
                .check(matches(isDisplayed()))
    }

    /** Screenshot related */
    fun clickCaptureScreen(activity: ajit.browser.focus.activity.MainActivity) {
        // TODO find a way to remove the activity reference
        screenshotIdlingResource = ajit.browser.focus.helper.ScreenshotIdlingResource(activity)

        // Click screen capture button
        onView(Matchers.allOf(withId(R.id.btn_capture), isDisplayed())).perform(click())

        ajit.browser.focus.utils.AndroidTestUtils.toastContainsText(activity, R.string.screenshot_saved)
        checkNoScreenshotOnBoarding()
    }

    private var SCREENSHOT_ONBOARDING_EVENT_DELAY: Long = 0

    fun checkNoScreenshotOnBoarding() {
        onView(withId(R.id.my_shot_img_hand)).check(doesNotExist())
        onView(withId(R.id.my_shot_category_learn_more)).check(doesNotExist())
    }

    fun firstTimeClickCaptureScreen() {
        // TODO find a way to remove the activity reference
        // Click screen capture button
        onView(Matchers.allOf(withId(R.id.btn_capture), isDisplayed())).perform(click())
        checkScreenshotOnBoarding()
    }

    fun checkScreenshotOnBoarding() {
        // As Espresso failed on CI owing to intermittent, uiautomator is applied
        val uiDevice = UiDevice.getInstance(InstrumentationRegistry.getInstrumentation())
        uiDevice.findObject(UiSelector().resourceId("my_shot_category_learn_more"))
        uiDevice.findObject(UiSelector().resourceId("my_shot_img_hand"))
    }

    fun longClickOnWebViewContent(activity: ajit.browser.focus.activity.MainActivity) {
        val displayMetrics = activity.resources.displayMetrics
        val displayWidth = displayMetrics.widthPixels
        val displayHeight = displayMetrics.heightPixels
        onView(withId(R.id.main_content)).check(matches(isDisplayed())).perform(ajit.browser.focus.utils.AndroidTestUtils.clickXY(displayWidth / 2, displayHeight / 2, Tap.LONG))
    }

    fun clickOpenLinkInNewTab() {
        onView(withText(R.string.contextmenu_open_in_new_tab)).perform(click())
    }

    fun checkNewTabOpenedSnackbarIsDisplayed() {
        onView(Matchers.allOf(withId(android.support.design.R.id.snackbar_text), withText(R.string.new_background_tab_hint))).check(matches(isDisplayed()))
    }

    fun checkNoLocationPermissionSnackbarIsDisplayed() {
        onView(Matchers.allOf(withId(android.support.design.R.id.snackbar_text), withText(R.string.permission_toast_location))).check(matches(isDisplayed()))
    }

    fun checkGeoPermissionDialogIsDisplayed() {
        onView(withText(R.string.geolocation_dialog_allow)).inRoot(RootMatchers.isDialog()).check(matches(isDisplayed()))
    }

    fun clickAllowGeoPermission() {
        onView(withText(R.string.geolocation_dialog_allow)).inRoot(RootMatchers.isDialog()).perform(click())
    }

    fun clickTabTray() {
        onView(withId(R.id.btn_tab_tray)).perform(click())
    }

    fun clickCloseAllTabs() {
        onView(withId(R.id.close_all_tabs_btn)).perform(click())
    }

    fun loadErrorPage(url: String) {
        onView(withId(R.id.display_url)).perform(click())
        onView(withId(R.id.url_edit)).perform(replaceText(url), pressImeActionButton())
        onView(withId(R.id.main_content)).check(matches(isDisplayed()))
    }

    fun clickTextActionMore() {
        // Since text action popup menu is control by framework and we need to click overflow button to show the target string,
        // it's not easy to find a good way to let espresso track that button. So use ImageButton class name to find the target for now.
        onView(withClassName(Matchers.containsString("ImageButton")))
                .inRoot(RootMatchers.isPlatformPopup())
                .perform(click())
    }

    fun checkSearchInRocketIsDisplayed() {
        onView(withText(R.string.text_selection_search_action))
                .inRoot(RootMatchers.isPlatformPopup())
                .check(matches(isDisplayed()))
    }

    fun goHome(activity: Activity) {
        val intent = Intent()
        intent.action = Intent.ACTION_MAIN
        intent.addCategory(Intent.CATEGORY_HOME)
        activity.startActivity(intent)
    }

    fun bringToForeground(activity: Activity) {
        val intent = Intent(activity, ajit.browser.focus.activity.MainActivity::class.java)
        intent.flags = Intent.FLAG_ACTIVITY_REORDER_TO_FRONT
        activity.startActivity(intent)
    }
}