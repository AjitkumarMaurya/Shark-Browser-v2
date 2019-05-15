/* -*- Mode: Java; c-basic-offset: 4; tab-width: 4; indent-tabs-mode: nil; -*-
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/. */

package ajit.browser.focus.widget

import android.os.Handler
import android.os.Looper
import android.text.Editable
import android.text.TextWatcher
import android.view.View
import android.view.inputmethod.EditorInfo
import android.webkit.WebView
import android.widget.TextView
import mozilla.components.browser.session.Session.FindResult
import ajit.browser.lightning.R
import ajit.browser.focus.telemetry.TelemetryWrapper
import ajit.browser.focus.utils.ViewUtils
import ajit.browser.rocket.tabs.Session

class FindInPage : ajit.browser.focus.widget.BackKeyHandleable {
    private val container: View
    private val queryText: TextView
    private val resultText: TextView
    private val prevBtn: View
    private val nextBtn: View
    private val closeBtn: View

    private val resultFormat: String
    private val accessibilityFormat: String

    private var session: Session? = null

    constructor(rootView: View) {
        container = rootView.findViewById(R.id.find_in_page)
        queryText = container.findViewById(R.id.find_in_page_query_text)
        resultText = container.findViewById(R.id.find_in_page_result_text)
        nextBtn = container.findViewById(R.id.find_in_page_next_btn)
        prevBtn = container.findViewById(R.id.find_in_page_prev_btn)
        closeBtn = container.findViewById(R.id.find_in_page_close_btn)

        resultFormat = container.context.getString(R.string.find_in_page_result)
        accessibilityFormat = container.context.getString(R.string.accessibility_find_in_page_result)

        initViews()
    }

    override fun onBackPressed(): Boolean {
        return if (container.visibility == View.VISIBLE) {
            hide()
            true
        } else {
            false
        }
    }

    fun onFindResultReceived(result: FindResult) {
        val activeMatchOrdinal = result.activeMatchOrdinal
        val numOfMatches = result.numberOfMatches
        if (numOfMatches > 0) {
            // We don't want the presentation of the activeMatchOrdinal to be zero indexed. So let's
            // increment it by one.
            val ordinal = activeMatchOrdinal + 1
            resultText.text = String.format(resultFormat, ordinal, numOfMatches)
            resultText.contentDescription = String.format(accessibilityFormat, ordinal, numOfMatches)
        } else {
            resultText.text = ""
            resultText.contentDescription = ""
        }
    }

    fun show(current: Session?) {
        if (container.visibility == View.VISIBLE) {
            return
        }

        if (current != null) {
            session = current
            container.visibility = View.VISIBLE
            // FIXME: post to another round to increase possibility of showing keyboard
            // Find-in-page button of menu is in another window, meanwhile the find-in-page view
            // is in another window. Showing-keyboard might fail if the focused view is invisible.
            Handler(Looper.getMainLooper()).post {
                queryText.requestFocus()
                ajit.browser.focus.utils.ViewUtils.showKeyboard(queryText)
            }
        }
    }

    fun hide() {
        if (container.visibility != View.VISIBLE) {
            return
        }

        ajit.browser.focus.utils.ViewUtils.hideKeyboard(queryText)
        queryText.text = null
        queryText.clearFocus()
        container.visibility = View.GONE
    }

    private fun initViews() {
        fun obtainWebView(): WebView? {
            val tabView = session?.engineSession?.tabView ?: return null
            return tabView as WebView
        }

        closeBtn.setOnClickListener {
            hide()
        }
        queryText.setOnClickListener { queryText.isCursorVisible = true }
        prevBtn.setOnClickListener {
            obtainWebView()?.findNext(false)
            TelemetryWrapper.findInPage(TelemetryWrapper.FIND_IN_PAGE.CLICK_PREVIOUS)
        }
        nextBtn.setOnClickListener {
            obtainWebView()?.findNext(true)
            TelemetryWrapper.findInPage(TelemetryWrapper.FIND_IN_PAGE.CLICK_NEXT)
        }

        queryText.addTextChangedListener(object : TextWatcher {
            override fun afterTextChanged(s: Editable?) {
            }

            override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {
            }

            override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {
                s?.let { obtainWebView()?.findAllAsync(s.toString()) }
            }
        })

        queryText.setOnEditorActionListener { _, actionId, _ ->
            if (actionId == EditorInfo.IME_ACTION_DONE) {
                ajit.browser.focus.utils.ViewUtils.hideKeyboard(queryText)
                queryText.isCursorVisible = false
            }

            return@setOnEditorActionListener false
        }
    }
}