package ajit.browser.rocket.content

import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith
import org.mockito.Mockito
import org.mockito.Mockito.mock
import org.mockito.Mockito.times
import org.robolectric.RobolectricTestRunner
import org.robolectric.RuntimeEnvironment

@RunWith(RobolectricTestRunner::class)
class NewsPresenterTest {

    lateinit var newsPresenter: NewsPresenter

    lateinit var viewContract: NewsViewContract
    lateinit var newsViewModel: NewsViewModel

    @Before
    fun warmUp() {
        viewContract = mock(NewsViewContract::class.java)
        newsViewModel = mock(NewsViewModel::class.java)
        newsPresenter = NewsPresenter(viewContract)
        newsPresenter.newsViewModel = newsViewModel
    }

    @Test
    fun `Avoid rapidly loading more news items`() {
        newsPresenter.loadMore()
        newsPresenter.loadMore()
        Mockito.verify(newsViewModel, times(1)).loadMore()
    }

    @Test
    fun `when Repository is not set, checkNewsRepositoryReset() should force load more news items`() {
        newsPresenter.checkNewsRepositoryReset(RuntimeEnvironment.application)
        Mockito.verify(newsViewModel, times(1)).loadMore()
    }
}