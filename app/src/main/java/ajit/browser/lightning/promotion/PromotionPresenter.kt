package ajit.browser.lightning.promotion

import android.content.Context
import android.support.annotation.VisibleForTesting
import ajit.browser.focus.utils.AppConfigWrapper
import ajit.browser.focus.utils.IntentUtils
import ajit.browser.focus.utils.NewFeatureNotice
import ajit.browser.focus.utils.SafeIntent
import ajit.browser.focus.utils.Settings
import kotlin.properties.Delegates

interface PromotionViewContract {
    fun postSurveyNotification()
    fun showRateAppDialog()
    fun showRateAppNotification()
    fun showShareAppDialog()
    fun showPrivacyPolicyUpdateNotification()
    fun showRateAppDialogFromIntent()
}

class PromotionModel {

    // using a notnull delegate will make sure if the value is not set, it'll throw exception
    var didShowRateDialog by Delegates.notNull<Boolean>()
    var didShowShareDialog by Delegates.notNull<Boolean>()
    var isSurveyEnabled by Delegates.notNull<Boolean>()
    var didShowRateAppNotification by Delegates.notNull<Boolean>()
    var didDismissRateDialog by Delegates.notNull<Boolean>()
    var appCreateCount by Delegates.notNull<Int>()

    var rateAppDialogThreshold by Delegates.notNull<Long>()
    var rateAppNotificationThreshold by Delegates.notNull<Long>()
    var shareAppDialogThreshold by Delegates.notNull<Long>()

    var shouldShowPrivacyPolicyUpdate by Delegates.notNull<Boolean>()

    var showRateAppDialogFromIntent by Delegates.notNull<Boolean>()

    constructor(context: Context, safeIntent: ajit.browser.focus.utils.SafeIntent) : this(ajit.browser.focus.utils.Settings.getInstance(context).eventHistory, ajit.browser.focus.utils.NewFeatureNotice.getInstance(context), safeIntent)
    @VisibleForTesting
    constructor(history: ajit.browser.focus.utils.Settings.EventHistory, newFeatureNotice: ajit.browser.focus.utils.NewFeatureNotice, safeIntent: ajit.browser.focus.utils.SafeIntent) {

        parseIntent(safeIntent)

        didShowRateDialog = history.contains(ajit.browser.focus.utils.Settings.Event.ShowRateAppDialog)
        didShowShareDialog = history.contains(ajit.browser.focus.utils.Settings.Event.ShowShareAppDialog)
        didDismissRateDialog = history.contains(ajit.browser.focus.utils.Settings.Event.DismissRateAppDialog)
        didShowRateAppNotification = history.contains(ajit.browser.focus.utils.Settings.Event.ShowRateAppNotification)
        isSurveyEnabled = ajit.browser.focus.utils.AppConfigWrapper.isSurveyNotificationEnabled() && !history.contains(ajit.browser.focus.utils.Settings.Event.PostSurveyNotification)
        if (accumulateAppCreateCount()) {
            history.add(ajit.browser.focus.utils.Settings.Event.AppCreate)
        }
        appCreateCount = history.getCount(ajit.browser.focus.utils.Settings.Event.AppCreate)
        rateAppDialogThreshold = ajit.browser.focus.utils.AppConfigWrapper.getRateDialogLaunchTimeThreshold()
        rateAppNotificationThreshold = ajit.browser.focus.utils.AppConfigWrapper.getRateAppNotificationLaunchTimeThreshold()
        shareAppDialogThreshold = ajit.browser.focus.utils.AppConfigWrapper.getShareDialogLaunchTimeThreshold(didDismissRateDialog)

        shouldShowPrivacyPolicyUpdate = newFeatureNotice.shouldShowPrivacyPolicyUpdate()
    }

    fun parseIntent(safeIntent: ajit.browser.focus.utils.SafeIntent?) {
        showRateAppDialogFromIntent = safeIntent?.getBooleanExtra(ajit.browser.focus.utils.IntentUtils.EXTRA_SHOW_RATE_DIALOG, false) == true
    }

    private fun accumulateAppCreateCount() = !didShowRateDialog || !didShowShareDialog || isSurveyEnabled || !didShowRateAppNotification
}

class PromotionPresenter {
    companion object {

        @JvmStatic
        fun runPromotion(promotionViewContract: PromotionViewContract, promotionModel: PromotionModel) {
            if (runPromotionFromIntent(promotionViewContract, promotionModel)) {
                // Don't run other promotion if we already displayed above promotion
                return
            }

            if (!promotionModel.didShowRateDialog && promotionModel.appCreateCount >= promotionModel.rateAppDialogThreshold) {
                promotionViewContract.showRateAppDialog()
            } else if (promotionModel.didDismissRateDialog && !promotionModel.didShowRateAppNotification && promotionModel.appCreateCount >= promotionModel.rateAppNotificationThreshold) {
                promotionViewContract.showRateAppNotification()
            } else if (!promotionModel.didShowShareDialog && promotionModel.appCreateCount >= promotionModel.shareAppDialogThreshold) {
                promotionViewContract.showShareAppDialog()
            }

            if (promotionModel.isSurveyEnabled && promotionModel.appCreateCount >= ajit.browser.focus.utils.AppConfigWrapper.getSurveyNotificationLaunchTimeThreshold()) {
                promotionViewContract.postSurveyNotification()
            }

            if (promotionModel.shouldShowPrivacyPolicyUpdate) {
                promotionViewContract.showPrivacyPolicyUpdateNotification()
            }
        }

        @JvmStatic
        // return true if promotion is already handled
        fun runPromotionFromIntent(promotionViewContract: PromotionViewContract, promotionModel: PromotionModel): Boolean {
            // When we receive this action, it means we need to show "Love Rocket" dialog
            if (promotionModel.showRateAppDialogFromIntent) {
                promotionViewContract.showRateAppDialogFromIntent()
                return true
            }
            return false
        }
    }
}