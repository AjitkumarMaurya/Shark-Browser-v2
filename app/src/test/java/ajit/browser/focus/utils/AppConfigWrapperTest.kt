package ajit.browser.focus.utils

import org.junit.Assert.assertEquals
import org.junit.Test
import ajit.browser.focus.utils.FirebaseHelper.RATE_APP_DIALOG_THRESHOLD
import ajit.browser.focus.utils.FirebaseHelper.RATE_APP_NOTIFICATION_THRESHOLD
import ajit.browser.focus.utils.FirebaseHelper.SHARE_APP_DIALOG_THRESHOLD

/**
 * Make sure default value will be used.
 */
class AppConfigWrapperTest {

    @Test
    fun `customize default value`() {

        val rateDialog = 3
        val rateNotification = 4
        val shareDialog = 5

        val map = HashMap<String, Any>().apply {
            this[RATE_APP_DIALOG_THRESHOLD] = rateDialog
            this[RATE_APP_NOTIFICATION_THRESHOLD] = rateNotification
            this[SHARE_APP_DIALOG_THRESHOLD] = shareDialog
        }

        ajit.browser.focus.utils.FirebaseHelper.replaceContract(FirebaseNoOpImp(map))

        assertEquals(rateDialog, ajit.browser.focus.utils.AppConfigWrapper.getRateDialogLaunchTimeThreshold().toInt())

        assertEquals(
                rateNotification,
                ajit.browser.focus.utils.AppConfigWrapper.getRateAppNotificationLaunchTimeThreshold().toInt()
        )

        assertEquals(
                shareDialog,
                ajit.browser.focus.utils.AppConfigWrapper.getShareDialogLaunchTimeThreshold(false).toInt()
        )

        assertEquals(
                shareDialog + rateNotification - rateDialog,
                ajit.browser.focus.utils.AppConfigWrapper.getShareDialogLaunchTimeThreshold(true).toInt()
        )
    }
}