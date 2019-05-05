package ajit.browser.lightning.download

import android.arch.lifecycle.ViewModel
import android.arch.lifecycle.ViewModelProvider
import ajit.browser.focus.Inject

class DownloadViewModelFactory private constructor(private val repository: DownloadInfoRepository) : ViewModelProvider.NewInstanceFactory() {

    @Suppress("UNCHECKED_CAST")
    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        if (modelClass.isAssignableFrom(DownloadIndicatorViewModel::class.java)) {
            return DownloadIndicatorViewModel(repository) as T
        } else if (modelClass.isAssignableFrom(DownloadInfoViewModel::class.java)) {
            return DownloadInfoViewModel(repository) as T
        }
        throw IllegalArgumentException("Unknown ViewModel class: " + modelClass.name)
    }

    companion object {

        @Volatile private var INSTANCE: DownloadViewModelFactory? = null

        @JvmStatic
        fun getInstance(): DownloadViewModelFactory? =
                INSTANCE ?: synchronized(this) {
                    INSTANCE ?: DownloadViewModelFactory(ajit.browser.focus.Inject.provideDownloadInfoRepository()).also { INSTANCE = it }
                }
    }
}
