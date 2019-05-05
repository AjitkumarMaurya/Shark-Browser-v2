package ajit.browser.lightning.download

import ajit.browser.focus.download.DownloadInfo

class DownloadInfoPack(var list: ArrayList<ajit.browser.focus.download.DownloadInfo>, var notifyType: Int, var index: Long) {
    object Constants {
        const val NOTIFY_DATASET_CHANGED = 1
        const val NOTIFY_ITEM_INSERTED = 2
        const val NOTIFY_ITEM_REMOVED = 3
        const val NOTIFY_ITEM_CHANGED = 4
    }
}
