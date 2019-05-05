package ajit.browser.rocket.promotion

import org.junit.Assert.assertEquals
import org.junit.Test
import org.mockito.Mockito.`when`
import org.mockito.Mockito.mock
import ajit.browser.focus.utils.FirebaseHelper
import org.mozilla.focus.utils.FirebaseNoOpImp
import ajit.browser.focus.utils.IntentUtils
import ajit.browser.focus.utils.NewFeatureNotice
import ajit.browser.focus.utils.SafeIntent
import ajit.browser.focus.utils.Settings

class PromotionModelTest {

    @Test
    fun intentHasValidExtraShouldShouldRateAppDialog() {

        val safeIntent = mock(ajit.browser.focus.utils.SafeIntent::class.java)
        val eventHistory = mock(ajit.browser.focus.utils.Settings.EventHistory::class.java)
        val newFeatureNotice = mock(ajit.browser.focus.utils.NewFeatureNotice::class.java)

        ajit.browser.focus.utils.FirebaseHelper.replaceContract(FirebaseNoOpImp())

        `when`(safeIntent.getBooleanExtra(ajit.browser.focus.utils.IntentUtils.EXTRA_SHOW_RATE_DIALOG, false)).thenReturn(true)
        `when`(newFeatureNotice.shouldShowPrivacyPolicyUpdate()).thenReturn(false)

        val promotionModel = PromotionModel(eventHistory, newFeatureNotice, safeIntent)

        promotionModel.parseIntent(safeIntent)
        assertEquals(true, promotionModel.showRateAppDialogFromIntent)
    }
}
