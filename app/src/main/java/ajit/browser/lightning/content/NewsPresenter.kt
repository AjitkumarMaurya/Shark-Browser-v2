package ajit.browser.lightning.content

import android.arch.lifecycle.LifecycleOwner
import android.arch.lifecycle.Observer
import android.arch.lifecycle.ViewModelProviders
import android.content.Context
import android.support.annotation.VisibleForTesting
import android.support.v4.app.FragmentActivity
import ajit.browser.focus.utils.Settings
import ajit.browser.lightning.widget.NewsSourcePreference.PREF_INT_NEWS_PRIORITY
import ajit.browser.lite.partner.NewsItem
import ajit.browser.threadutils.ThreadUtils

interface NewsViewContract {
    fun getViewLifecycleOwner(): LifecycleOwner
    fun updateNews(items: List<NewsItem>?)
}

class NewsPresenter(private val newsViewContract: NewsViewContract) : ContentPortalView.NewsListListener {

    @VisibleForTesting
    var newsViewModel: NewsViewModel? = null

    companion object {
        private val LOADMORE_THRESHOLD = 3000L
    }
    private var isLoading = false

    fun setupNewsViewModel(fragmentActivity: FragmentActivity?) {
        if (fragmentActivity == null) {
            return
        }
        newsViewModel = ViewModelProviders.of(fragmentActivity).get(NewsViewModel::class.java)
        val repository = NewsRepository.getInstance(fragmentActivity)
        repository.setOnDataChangedListener(newsViewModel)
        newsViewModel?.repository = repository
        newsViewModel?.items?.observe(newsViewContract.getViewLifecycleOwner(),
                Observer { items ->
                    newsViewContract.updateNews(items)
                    isLoading = false
                })
        // creating a repository will also create a new subscription.
        // we deliberately create a new subscription again to load data aggressively.
        newsViewModel?.loadMore()
    }

    override fun loadMore() {
        if (!isLoading) {
            newsViewModel?.loadMore()
            isLoading = true
            ajit.browser.threadutils.ThreadUtils.postToMainThreadDelayed({ isLoading = false }, LOADMORE_THRESHOLD)
        }
    }

    override fun onShow(context: Context) {
        updateSourcePriority(context)
        checkNewsRepositoryReset(context)
    }

    fun checkNewsRepositoryReset(context: Context) {
        // News Repository is reset and empty when the user changes the news source.
        // We create a new Repository and inject to newsViewModel here.
        // TODO: similar code happens in setupNewsViewModel(), need to refine them
        if (NewsRepository.isEmpty() && newsViewModel != null) {
            val repository = NewsRepository.getInstance(context)
            repository.setOnDataChangedListener(newsViewModel)
            newsViewModel?.repository = repository
            newsViewModel?.items?.value = null
            newsViewModel?.loadMore()
        }
    }

    private fun updateSourcePriority(context: Context) {
        // the user had seen the news. Treat it as an user selection so no on can change it
        ajit.browser.focus.utils.Settings.getInstance(context).setPriority(PREF_INT_NEWS_PRIORITY, ajit.browser.focus.utils.Settings.PRIORITY_USER)
    }
}