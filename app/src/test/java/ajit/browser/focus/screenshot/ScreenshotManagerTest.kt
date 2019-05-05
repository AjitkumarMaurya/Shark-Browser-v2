package ajit.browser.focus.screenshot

import org.junit.Test
import org.junit.runner.RunWith
import ajit.browser.focus.utils.FirebaseHelper
import org.mozilla.focus.utils.FirebaseNoOpImp
import org.robolectric.RobolectricTestRunner
import org.robolectric.RuntimeEnvironment

@RunWith(RobolectricTestRunner::class)
class ScreenshotManagerTest {

    @Test
    fun testCategories() {
        val sm = ajit.browser.focus.screenshot.ScreenshotManager()
        val context = RuntimeEnvironment.application

        ajit.browser.focus.utils.FirebaseHelper.replaceContract(FirebaseNoOpImp())
        assert(sm.getCategory(context, "https://alipay.com/").equals("Banking"))
        assert(sm.getCategory(context, "https://m.alipay.com/").equals("Banking"))

        assert(sm.getCategory(context, "https://blogspot.com/").equals("Weblogs"))
        assert(sm.getCategory(context, "https://m.blogspot.com/").equals("Weblogs"))
    }
}
