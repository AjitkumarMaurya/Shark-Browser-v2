package ajit.browser.lightning.download

import android.app.DownloadManager
import ajit.browser.focus.download.DownloadInfo
import ajit.browser.focus.download.DownloadInfoManager
import ajit.browser.threadutils.ThreadUtils

class DownloadInfoRepository {

    interface OnQueryListCompleteListener {
        fun onComplete(list: List<ajit.browser.focus.download.DownloadInfo>)
    }

    interface OnQueryItemCompleteListener {
        fun onComplete(download: ajit.browser.focus.download.DownloadInfo)
    }

    fun queryIndicatorStatus(listenerList: DownloadInfoRepository.OnQueryListCompleteListener) {
        ajit.browser.focus.download.DownloadInfoManager.getInstance().queryDownloadingAndUnreadIds { downloadInfoList ->
            listenerList.onComplete(downloadInfoList)
        }
    }

    fun queryByRowId(rowId: Long, listenerItem: OnQueryItemCompleteListener) {
        ajit.browser.focus.download.DownloadInfoManager.getInstance().queryByRowId(rowId) { downloadInfoList ->
            if (downloadInfoList.size > 0) {
                val downloadInfo = downloadInfoList[0]
                listenerItem.onComplete(downloadInfo)
            }
        }
    }

    fun queryByDownloadId(rowId: Long, listenerItem: OnQueryItemCompleteListener) {
        ajit.browser.focus.download.DownloadInfoManager.getInstance().queryByDownloadId(rowId) { downloadInfoList ->
            if (downloadInfoList.size > 0) {
                val downloadInfo = downloadInfoList[0]
                listenerItem.onComplete(downloadInfo)
            }
        }
    }

    fun queryDownloadingItems(runningIds: LongArray, listenerList: OnQueryListCompleteListener) {
        ajit.browser.threadutils.ThreadUtils.postToBackgroundThread {
            val query = DownloadManager.Query()
            query.setFilterById(*runningIds)
            query.setFilterByStatus(DownloadManager.STATUS_RUNNING)
            ajit.browser.focus.download.DownloadInfoManager.getInstance().downloadManager.query(query).use {
                if (it != null) {
                    val list = ArrayList<ajit.browser.focus.download.DownloadInfo>()
                    while (it.moveToNext()) {
                        val id = it.getLong(it.getColumnIndex(DownloadManager.COLUMN_ID))
                        val totalSize = it.getDouble(it.getColumnIndex(DownloadManager.COLUMN_TOTAL_SIZE_BYTES))
                        val currentSize = it.getDouble(it.getColumnIndex(DownloadManager.COLUMN_BYTES_DOWNLOADED_SO_FAR))
                        val info = ajit.browser.focus.download.DownloadInfo()
                        info.downloadId = id
                        info.sizeTotal = totalSize
                        info.sizeSoFar = currentSize
                        list.add(info)
                    }
                    listenerList.onComplete(list)
                }
            }
        }
    }

    fun markAllItemsAreRead() {
        ajit.browser.focus.download.DownloadInfoManager.getInstance().markAllItemsAreRead(null)
    }

    fun loadData(offset: Int, pageSize: Int, listenerList: OnQueryListCompleteListener) {
        ajit.browser.focus.download.DownloadInfoManager.getInstance().query(offset, pageSize) { downloadInfoList ->
            listenerList.onComplete(downloadInfoList)
        }
    }

    fun remove(rowId: Long) {
        ajit.browser.focus.download.DownloadInfoManager.getInstance().delete(rowId, null)
    }

    fun deleteFromDownloadManager(downloadId: Long) {
        ajit.browser.focus.download.DownloadInfoManager.getInstance().downloadManager.remove(downloadId)
    }

    companion object {

        @Volatile private var INSTANCE: DownloadInfoRepository? = null

        @JvmStatic
        fun getInstance(): DownloadInfoRepository? =
                INSTANCE ?: synchronized(this) {
                    INSTANCE ?: DownloadInfoRepository().also {
                        INSTANCE = it
                    }
                }
    }
}
