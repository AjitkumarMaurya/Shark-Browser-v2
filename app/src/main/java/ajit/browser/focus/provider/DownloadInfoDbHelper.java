/* -*- Mode: Java; c-basic-offset: 4; tab-width: 4; indent-tabs-mode: nil; -*-
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/. */

package ajit.browser.focus.provider;

import android.app.DownloadManager;
import android.content.Context;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;

import static ajit.browser.focus.provider.DownloadContract.Download;

/**
 * Created by anlin on 17/08/2017.
 */

public class DownloadInfoDbHelper {
    // Database Info
    private static final String DATABASE_NAME = "DownloadInfo.db";
    private static final int VERSION_INIT = 1;
    private static final int VERSION_ADD_STATUS_AND_UNREAD = VERSION_INIT + 1;

    private static final String CREATE_TABLE_IF_NOT_EXISTS = "CREATE TABLE ";

    private static DownloadInfoDbHelper sInstance;
    private final OpenHelper mOpenHelper;


    private static final class OpenHelper extends SQLiteOpenHelper {

        public OpenHelper(Context context, String name, SQLiteDatabase.CursorFactory factory, int version) {
            super(context, name, factory, version);
        }

        @Override
        public void onCreate(SQLiteDatabase sqLiteDatabase) {

            String CREATE_TABLE = CREATE_TABLE_IF_NOT_EXISTS + DownloadContract.Download.TABLE_DOWNLOAD + "("
                    + DownloadContract.Download._ID + " INTEGER PRIMARY KEY AUTOINCREMENT,"
                    + DownloadContract.Download.DOWNLOAD_ID + " INTEGER,"
                    + DownloadContract.Download.FILE_PATH + " TEXT,"
                    + DownloadContract.Download.STATUS + " INTEGER,"
                    + DownloadContract.Download.IS_READ + " INTEGER DEFAULT 0" // Download item default is unread
                    + ")";

            sqLiteDatabase.execSQL(CREATE_TABLE);
        }

        @Override
        public void onUpgrade(SQLiteDatabase sqLiteDatabase, int oldVersion, int newVersion) {
            if (oldVersion < VERSION_ADD_STATUS_AND_UNREAD) {
                // add new column status and update all legacy data to 'STATUS_SUCCESSFUL'
                sqLiteDatabase.execSQL("ALTER TABLE " +  DownloadContract.Download.TABLE_DOWNLOAD + " ADD " + DownloadContract.Download.STATUS + " INTEGER;");
                sqLiteDatabase.execSQL("UPDATE " + DownloadContract.Download.TABLE_DOWNLOAD + " SET " + DownloadContract.Download.STATUS + " = " + String.valueOf(DownloadManager.STATUS_SUCCESSFUL) + ";");

                // add new column unread and mark all legacy data 'IS_READ' = 1
                sqLiteDatabase.execSQL("ALTER TABLE " +  DownloadContract.Download.TABLE_DOWNLOAD + " ADD " + DownloadContract.Download.IS_READ + " INTEGER DEFAULT 0;");
                sqLiteDatabase.execSQL("UPDATE " + DownloadContract.Download.TABLE_DOWNLOAD + " SET " + DownloadContract.Download.IS_READ + " = 1;");
            }
        }
    }

    private DownloadInfoDbHelper(Context context) {

        mOpenHelper = new OpenHelper(context, DATABASE_NAME, null, getDatabaseVersion());
    }

    private int getDatabaseVersion() {
        return VERSION_ADD_STATUS_AND_UNREAD;
    }

    public static synchronized DownloadInfoDbHelper getsInstance(Context context) {
        if (sInstance == null) {
            sInstance = new DownloadInfoDbHelper(context);
        }

        return sInstance;
    }

    public SQLiteDatabase getReadableDB() {
        return mOpenHelper.getReadableDatabase();
    }

    public SQLiteDatabase getWritableDB() {
        return mOpenHelper.getWritableDatabase();
    }
}
