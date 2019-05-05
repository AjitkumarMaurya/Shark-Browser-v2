/* -*- Mode: Java; c-basic-offset: 4; tab-width: 4; indent-tabs-mode: nil; -*-
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/. */

package ajit.browser.lightning.home.pinsite

import android.annotation.SuppressLint
import android.content.Context
import android.preference.PreferenceManager
import android.util.Log
import org.json.JSONArray
import org.json.JSONException
import org.json.JSONObject
import ajit.browser.focus.history.model.Site
import ajit.browser.focus.home.HomeFragment
import ajit.browser.focus.utils.TopSitesUtils
import ajit.browser.lightning.R

/**
 * TODO: The current implementation of SharedPreferenceSiteDelegate only relies on persistent data,
 * so its instance can be created whenever it's needed. However, I'm considering make it a single
 * instance, so we can preserve some states and reduce IO frequency
 */
fun getPinSiteManager(context: Context): PinSiteManager {
    return PinSiteManager(SharedPreferencePinSiteDelegate(context))
}

class PinSiteManager(
    private val pinSiteDelegate: PinSiteDelegate
) : PinSiteDelegate by pinSiteDelegate

interface PinSiteDelegate {
    fun isEnabled(): Boolean
    fun isPinned(site: ajit.browser.focus.history.model.Site): Boolean
    fun pin(site: ajit.browser.focus.history.model.Site)
    fun unpinned(site: ajit.browser.focus.history.model.Site)
    fun getPinSites(): List<ajit.browser.focus.history.model.Site>
    fun isFirstTimeEnable(): Boolean
}

class SharedPreferencePinSiteDelegate(private val context: Context) : PinSiteDelegate {

    companion object {
        private const val TAG = "PinSiteManager"

        private const val PREF_NAME = "pin_sites"
        private const val KEY_STRING_JSON = "json"
        private const val KEY_BOOLEAN_FIRST_INIT = "first_init"

        // The number of pinned sites the new user will see
        private const val DEFAULT_NEW_USER_PIN_COUNT = 2

        private const val PINNED_SITE_VIEW_COUNT_INTERVAL = 100L

        private const val JSON_KEY_BOOLEAN_IS_ENABLED = "isEnabled"
        private const val JSON_KEY_STRING_PARTNER = "partner"

        fun resetPinSiteData(context: Context) {
            context.getSharedPreferences(PREF_NAME, Context.MODE_PRIVATE).apply {
                edit().putBoolean(KEY_BOOLEAN_FIRST_INIT, true).putString(KEY_STRING_JSON, "").apply()
            }
        }
    }

    private val pref = context.getSharedPreferences(PREF_NAME, Context.MODE_PRIVATE)
    private val sites = mutableListOf<ajit.browser.focus.history.model.Site>()

    private val rootNode: JSONObject
    private var isEnabled = false

    private val partnerList = mutableListOf<ajit.browser.focus.history.model.Site>()

    init {
        val jsonString = ajit.browser.focus.utils.TopSitesUtils.loadDefaultSitesFromAssets(context, R.raw.pin_sites)
        this.rootNode = JSONObject(jsonString)
        this.isEnabled = isEnabled(rootNode)

        log("isEnable: $isEnabled")
        log("isFirstInit: ${isFirstInit()}")

        if (this.isEnabled) {
            val partnerSites = getPartnerList(rootNode)
            if (hasTopSiteRecord()) {
                log("init for update user")
                initForUpdateUser(partnerList, partnerSites)
            } else {
                log("init for new user")
                initForNewUser(partnerList, partnerSites)
            }
        } else {
            log("no initialization needed")
        }
    }

    override fun isEnabled(): Boolean {
        return isEnabled
    }

    override fun isPinned(site: ajit.browser.focus.history.model.Site): Boolean {
        return sites.any { it.id == site.id }
    }

    override fun pin(site: ajit.browser.focus.history.model.Site) {
        sites.add(0, ajit.browser.focus.history.model.Site(
                site.id,
                site.title,
                site.url,
                site.viewCount,
                site.lastViewTimestamp,
                site.favIconUri
        ))
        save(sites)
    }

    override fun unpinned(site: ajit.browser.focus.history.model.Site) {
        sites.removeAll { it.id == site.id }
        save(sites)
    }

    override fun getPinSites(): List<ajit.browser.focus.history.model.Site> {
        load(sites)
        return sites
    }

    override fun isFirstTimeEnable(): Boolean {
        return isFirstInit()
    }

    private fun getViewCountForPinSiteAt(index: Int): Long {
        return Long.MAX_VALUE - index * PINNED_SITE_VIEW_COUNT_INTERVAL
    }

    private fun save(sites: List<ajit.browser.focus.history.model.Site>) {
        sites.forEachIndexed { index, site ->
            site.viewCount = getViewCountForPinSiteAt(index)
        }
        val json = sitesToJson(sites)
        pref.edit().putString(KEY_STRING_JSON, json.toString()).apply()
        log("save")
    }

    private fun load(results: MutableList<ajit.browser.focus.history.model.Site>) {
        if (!this.isEnabled) {
            log("load - not enabled")
            return
        }

        log("load - enabled")
        results.clear()

        val isFirstInit = isFirstInit()
        if (isFirstInit && partnerList.isNotEmpty()) {
            results.addAll(0, partnerList)
            log("load partner list")
            save(results)
        } else {
            log("load saved pin site pref")
            loadSavedPinnedSite(results)
        }

        if (isFirstInit) {
            log("init finished")
            onFirstInitComplete()
        }
    }

    private fun initForUpdateUser(results: MutableList<ajit.browser.focus.history.model.Site>, partnerSites: List<ajit.browser.focus.history.model.Site>) {
        results.addAll(partnerSites)
    }

    private fun initForNewUser(results: MutableList<ajit.browser.focus.history.model.Site>, partnerSites: List<ajit.browser.focus.history.model.Site>) {
        results.addAll(partnerSites)

        val defaultTopSiteJson = ajit.browser.focus.utils.TopSitesUtils.loadDefaultSitesFromAssets(context, R.raw.topsites)
        val defaultTopSites = jsonToSites(JSONArray(defaultTopSiteJson), true).toMutableList()

        var remainPinCount = DEFAULT_NEW_USER_PIN_COUNT - partnerSites.size
        while (remainPinCount-- > 0 && defaultTopSites.isNotEmpty()) {
            results.add(defaultTopSites.removeAt(0))
        }
    }

    private fun loadSavedPinnedSite(results: MutableList<ajit.browser.focus.history.model.Site>) {
        val jsonString = pref.getString(KEY_STRING_JSON, "")
        try {
            results.addAll(jsonToSites(JSONArray(jsonString), false))
        } catch (ignored: JSONException) {
        }
    }

    private fun isFirstInit(): Boolean {
        return pref.getBoolean(KEY_BOOLEAN_FIRST_INIT, true)
    }

    private fun onFirstInitComplete() {
        pref.edit().putBoolean(KEY_BOOLEAN_FIRST_INIT, false).apply()
    }

    private fun sitesToJson(sites: List<ajit.browser.focus.history.model.Site>): JSONArray {
        val array = JSONArray()
        for (i in sites.indices) {
            val site = sites[i]
            val jsonSite = siteToJson(site)
            if (jsonSite != null) {
                array.put(jsonSite)
            }
        }
        return array
    }

    private fun jsonToSites(array: JSONArray, isDefaultTopSite: Boolean): List<ajit.browser.focus.history.model.Site> {
        val sites = ArrayList<ajit.browser.focus.history.model.Site>()
        val faviconPrefix = if (isDefaultTopSite) {
            ajit.browser.focus.utils.TopSitesUtils.TOP_SITE_ASSET_PREFIX
        } else {
            ""
        }
        try {
            for (i in 0 until array.length()) {
                val obj = array.getJSONObject(i)
                sites.add(ajit.browser.focus.history.model.Site(obj.getLong(TopSitesUtils.KEY_ID),
                        obj.getString(TopSitesUtils.KEY_TITLE),
                        obj.getString(TopSitesUtils.KEY_URL),
                        obj.getLong(TopSitesUtils.KEY_VIEW_COUNT),
                        0,
                        faviconPrefix + getFaviconUrl(obj)))
            }
        } catch (ignored: JSONException) {
        }

        return sites
    }

    private fun getFaviconUrl(json: JSONObject): String {
        return json.optString(ajit.browser.focus.utils.TopSitesUtils.KEY_FAVICON)
    }

    private fun siteToJson(site: ajit.browser.focus.history.model.Site): JSONObject? {
        return try {
            val node = JSONObject()
            node.put(ajit.browser.focus.utils.TopSitesUtils.KEY_ID, site.id)
            node.put(ajit.browser.focus.utils.TopSitesUtils.KEY_URL, site.url)
            node.put(ajit.browser.focus.utils.TopSitesUtils.KEY_TITLE, site.title)
            node.put(ajit.browser.focus.utils.TopSitesUtils.KEY_FAVICON, site.favIconUri)
            node.put(ajit.browser.focus.utils.TopSitesUtils.KEY_VIEW_COUNT, site.viewCount)
        } catch (e: JSONException) {
            null
        }
    }

    private fun hasTopSiteRecord(): Boolean {
        val defaultPref = PreferenceManager.getDefaultSharedPreferences(context)
        return defaultPref.getString(ajit.browser.focus.home.HomeFragment.TOPSITES_PREF, "")?.isNotEmpty() ?: false
    }

    private fun isEnabled(rootNode: JSONObject): Boolean {
        return rootNode.getBoolean(JSON_KEY_BOOLEAN_IS_ENABLED)
    }

    private fun getPartnerList(rootNode: JSONObject): List<ajit.browser.focus.history.model.Site> {
        return jsonToSites(rootNode.getJSONArray(JSON_KEY_STRING_PARTNER), true)
    }

    @SuppressLint("LogUsage")
    private fun log(msg: String) {

        Log.e("@@",msg)
    }
}
