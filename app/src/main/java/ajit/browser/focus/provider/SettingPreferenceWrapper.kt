package ajit.browser.focus.provider

import android.content.ContentResolver
import android.database.Cursor
import android.net.Uri
import ajit.browser.focus.provider.SettingContract.KEY
import ajit.browser.focus.provider.SettingContract.PATH_GET_BOOLEAN
import ajit.browser.focus.provider.SettingContract.PATH_GET_FLOAT

/**
 * A ContentProvider wrapper for SharePreference in Settings that allows other process components e.g. PrivateModeActivity can access SharePreference data
 */
class SettingPreferenceWrapper(private val resolver: ContentResolver) {

    fun getFloat(key: String, defValue: Float): Float {
        return getValue(PATH_GET_FLOAT, key, defValue) as Float
    }

    fun getBoolean(key: String, defValue: Boolean): Boolean {
        return getValue(PATH_GET_BOOLEAN, key, defValue) as Boolean
    }

    private fun getValue(pathSegment: String, key: String, defValue: Any?): Any? {
        var v: Any? = null

        val uri = Uri.withAppendedPath(SettingContract.AUTHORITY_URI, pathSegment)
        val selectionArgs = arrayOf(key, defValue.toString())
        var cursor: Cursor? = null
        try {
            cursor = resolver.query(uri, null, null, selectionArgs, null)
            if (cursor != null) {
                val bundle = cursor.extras
                if (bundle != null) {
                    v = bundle.get(KEY)
                    bundle.clear()
                }
            }
        } finally {
            cursor?.close()
        }
        return v ?: defValue
    }
}
