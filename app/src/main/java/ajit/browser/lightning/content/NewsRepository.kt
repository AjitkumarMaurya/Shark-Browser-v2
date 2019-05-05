package ajit.browser.lightning.content

import android.annotation.SuppressLint
import android.content.Context
import ajit.browser.lite.newspoint.RepositoryNewsPoint
import ajit.browser.lite.partner.Repository
import ajit.browser.rocket.bhaskar.RepositoryBhaskar
import ajit.browser.lightning.widget.NewsSourcePreference.NEWS_DB
import ajit.browser.lite.partner.NewsItem

class NewsRepository {
    companion object {

        @SuppressLint("StaticFieldLeak")
        @Volatile
        private var INSTANCE: ajit.browser.lite.partner.Repository<out NewsItem>? = null

        @JvmStatic
        fun getInstance(
            context: Context?
        ): ajit.browser.lite.partner.Repository<out NewsItem> = INSTANCE ?: synchronized(this) {
            if (context == null) {
                throw IllegalStateException("can't create Content Repository with null context")
            }
            INSTANCE ?: buildRepository(context.applicationContext).also { INSTANCE = it }
        }

        @JvmStatic
        fun reset() {
            INSTANCE?.reset()
            INSTANCE = null
        }

        @JvmStatic
        fun resetSubscriptionUrl(subscriptionUrl: String) {
            INSTANCE?.setSubscriptionUrl(subscriptionUrl)
        }

        @JvmStatic
        fun isEmpty() = INSTANCE == null

        private fun buildRepository(context: Context): ajit.browser.lite.partner.Repository<out NewsItem> {
            return if (ajit.browser.lightning.content.NewsSourceManager.getInstance().newsSource == NEWS_DB) {
                ajit.browser.rocket.bhaskar.RepositoryBhaskar(context, ajit.browser.lightning.content.NewsSourceManager.getInstance().newsSourceUrl)
            } else {
                ajit.browser.lite.newspoint.RepositoryNewsPoint(context, ajit.browser.lightning.content.NewsSourceManager.getInstance().newsSourceUrl)
            }
        }
    }
}