package ajit.browser.lightning.content

import ajit.browser.lite.partner.NewsItem
import android.arch.lifecycle.MutableLiveData
import android.arch.lifecycle.ViewModel
import ajit.browser.lite.partner.Repository

class NewsViewModel : ViewModel(), ajit.browser.lite.partner.Repository.OnDataChangedListener<NewsItem> {
    var repository: ajit.browser.lite.partner.Repository<out NewsItem>? = null
        set(value) {
            if (field != value) {
                items.value = null
            }
            field = value
        }
    val items = MutableLiveData<List<NewsItem>>()

    override fun onDataChanged(newsItemList: List<NewsItem>?) {
        // return the new list, so diff utils will think this is something to diff
        items.value = newsItemList
    }

    fun loadMore() {
        repository?.loadMore()
        // now wait for OnDataChangedListener.onDataChanged to return the result
    }
}