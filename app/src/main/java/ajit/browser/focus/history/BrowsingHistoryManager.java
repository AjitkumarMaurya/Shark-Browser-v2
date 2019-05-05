/* -*- Mode: Java; c-basic-offset: 4; tab-width: 4; indent-tabs-mode: nil; -*-
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/. */

package ajit.browser.focus.history;

import android.content.ContentResolver;
import android.content.ContentValues;
import android.content.Context;
import android.database.ContentObserver;
import android.net.Uri;
import android.os.Handler;

import ajit.browser.focus.history.model.Site;
import ajit.browser.focus.provider.HistoryContract;
import ajit.browser.focus.provider.QueryHandler;
import ajit.browser.icon.FavIconUtils;
import ajit.browser.focus.history.model.Site;
import ajit.browser.focus.provider.HistoryContract;
import ajit.browser.focus.provider.HistoryContract.BrowsingHistory;
import ajit.browser.focus.provider.QueryHandler;
import ajit.browser.focus.provider.QueryHandler.AsyncDeleteListener;
import ajit.browser.focus.provider.QueryHandler.AsyncDeleteWrapper;
import ajit.browser.focus.provider.QueryHandler.AsyncInsertListener;
import ajit.browser.focus.provider.QueryHandler.AsyncQueryListener;
import ajit.browser.focus.provider.QueryHandler.AsyncUpdateListener;
import ajit.browser.icon.FavIconUtils;

import java.lang.ref.WeakReference;
import java.util.ArrayList;

import ajit.browser.focus.history.model.Site;
import ajit.browser.focus.provider.HistoryContract;
import ajit.browser.focus.provider.QueryHandler;
import ajit.browser.icon.FavIconUtils;

/**
 * Created by hart on 07/08/2017.
 */

public class BrowsingHistoryManager {

    private static BrowsingHistoryManager sInstance;

    private WeakReference<ContentResolver> mResolver;
    private QueryHandler mQueryHandler;
    private BrowsingHistoryContentObserver mContentObserver;
    private ArrayList<ContentChangeListener> mListeners;

    private final class BrowsingHistoryContentObserver extends ContentObserver {

        public BrowsingHistoryContentObserver(Handler handler) {
            super(handler);
        }

        @Override
        public void onChange(boolean selfChange) {
            for (ContentChangeListener listener : mListeners) {
                listener.onContentChanged();
            }
        }
    }

    public interface ContentChangeListener {
        void onContentChanged();
    }

    public static BrowsingHistoryManager getInstance() {
        if (sInstance == null) {
            sInstance = new BrowsingHistoryManager();
        }
        return sInstance;
    }

    public void init(Context context) {
        ContentResolver resolver = context.getContentResolver();
        mResolver = new WeakReference<>(resolver);
        mQueryHandler = new QueryHandler(resolver);
        mContentObserver = new BrowsingHistoryContentObserver(null);
        mListeners = new ArrayList<>();
    }

    public void registerContentChangeListener(ContentChangeListener listener) {
        if (listener == null) {
            return;
        }

        mListeners.add(listener);
        if (mListeners.size() == 1) {
            ContentResolver resolver = mResolver.get();
            if (resolver != null) {
                resolver.registerContentObserver(HistoryContract.BrowsingHistory.CONTENT_URI, false, mContentObserver);
            }
        }
    }

    public void unregisterContentChangeListener(ContentChangeListener listener) {
        if (listener == null) {
            return;
        }

        mListeners.remove(listener);
        if (mListeners.size() == 0) {
            ContentResolver resolver = mResolver.get();
            if (resolver != null) {
                resolver.unregisterContentObserver(mContentObserver);
            }
        }
    }

    public static Site prepareSiteForFirstInsert(String url, String title, long timeStamp) {
        return new Site(QueryHandler.LONG_NO_VALUE, title, url, QueryHandler.LONG_NO_VALUE, timeStamp, (String) QueryHandler.OBJECT_NO_VALUE);
    }

    public void insert(final Site site, final QueryHandler.AsyncInsertListener listener) {
        mQueryHandler.postWorker(new Runnable() {
            @Override
            public void run() {
                final ContentValues contentValues = QueryHandler.getContentValuesFromSite(site);
                mQueryHandler.startInsert(QueryHandler.SITE_TOKEN, listener, HistoryContract.BrowsingHistory.CONTENT_URI, contentValues);
            }
        });
    }

    public void delete(long id, QueryHandler.AsyncDeleteListener listener) {
        mQueryHandler.startDelete(QueryHandler.SITE_TOKEN, new QueryHandler.AsyncDeleteWrapper(id, listener), HistoryContract.BrowsingHistory.CONTENT_URI, HistoryContract.BrowsingHistory._ID + " = ?", new String[]{Long.toString(id)});
    }

    public void deleteAll(QueryHandler.AsyncDeleteListener listener) {
        mQueryHandler.startDelete(QueryHandler.SITE_TOKEN, new QueryHandler.AsyncDeleteWrapper(-1, listener), HistoryContract.BrowsingHistory.CONTENT_URI, "1", null);
    }

    public void updateLastEntry(final Site site, final QueryHandler.AsyncUpdateListener listener) {
        mQueryHandler.postWorker(new Runnable() {
            @Override
            public void run() {
                final ContentValues contentValues = QueryHandler.getContentValuesFromSite(site);
                mQueryHandler.startUpdate(QueryHandler.SITE_TOKEN, listener, HistoryContract.BrowsingHistory.CONTENT_URI, contentValues, HistoryContract.BrowsingHistory._ID + " = ( SELECT " + HistoryContract.BrowsingHistory._ID + " FROM " + HistoryContract.TABLE_NAME + " WHERE " + HistoryContract.BrowsingHistory.URL + " = ? ORDER BY " + HistoryContract.BrowsingHistory.LAST_VIEW_TIMESTAMP + " DESC)", new String[]{site.getUrl()});
            }
        });
    }

    public void query(int offset, int limit, QueryHandler.AsyncQueryListener listener) {
        mQueryHandler.startQuery(QueryHandler.SITE_TOKEN, listener, Uri.parse(HistoryContract.BrowsingHistory.CONTENT_URI.toString() + "?offset=" + offset + "&limit=" + limit), null, null, null, HistoryContract.BrowsingHistory.LAST_VIEW_TIMESTAMP + " DESC");
    }

    public void queryTopSites(int limit, int minViewCount, QueryHandler.AsyncQueryListener listener) {
        mQueryHandler.startQuery(QueryHandler.SITE_TOKEN, listener, Uri.parse(HistoryContract.BrowsingHistory.CONTENT_URI.toString() + "?limit=" + limit), null, HistoryContract.BrowsingHistory.VIEW_COUNT + " >= ?", new String[]{Integer.toString(minViewCount)}, HistoryContract.BrowsingHistory.VIEW_COUNT + " DESC");
    }

    private static Site prepareSiteForUpdate(String title, String url, String fileUri) {
        return new Site(QueryHandler.LONG_NO_VALUE, title, url, QueryHandler.LONG_NO_VALUE, QueryHandler.LONG_NO_VALUE, fileUri);
    }

    public static void updateHistory(String title, String url, String fileUri) {
        updateHistory(title, url, fileUri, null);
    }

    public static void updateHistory(String title, String url, String fileUri, QueryHandler.AsyncUpdateListener callback) {
        BrowsingHistoryManager.getInstance().updateLastEntry(prepareSiteForUpdate(title, url, fileUri), callback);
    }

    public static class UpdateHistoryWrapper implements FavIconUtils.Consumer<String> {

        private String title;
        private String url;

        public UpdateHistoryWrapper(String title, String url) {
            this.title = title;
            this.url = url;
        }

        @Override
        public void accept(String fileUri) {
            BrowsingHistoryManager.updateHistory(title, url, fileUri);
        }
    }
}
