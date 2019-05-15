/* -*- Mode: Java; c-basic-offset: 4; tab-width: 20; indent-tabs-mode: nil; -*-
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/. */

package ajit.browser.focus;

import android.app.Application;
import android.arch.lifecycle.ViewModelProviders;
import android.content.Context;
import android.content.SharedPreferences;
import android.content.res.Resources;
import android.os.StrictMode;
import android.preference.PreferenceManager;
import android.support.v4.app.FragmentActivity;
import android.view.View;
import android.view.animation.Animation;

import ajit.browser.focus.home.HomeFragment;
import ajit.browser.focus.persistence.TabsDatabase;
import ajit.browser.focus.utils.AppConstants;
import ajit.browser.focus.utils.RemoteConfigConstants;
import ajit.browser.lightning.R;
import ajit.browser.focus.home.HomeFragment;
import ajit.browser.focus.persistence.TabsDatabase;
import ajit.browser.focus.utils.AppConstants;
import ajit.browser.focus.utils.RemoteConfigConstants;
import ajit.browser.lightning.download.DownloadIndicatorViewModel;
import ajit.browser.lightning.download.DownloadInfoRepository;
import ajit.browser.lightning.download.DownloadInfoViewModel;
import ajit.browser.lightning.download.DownloadViewModelFactory;
import ajit.browser.lightning.urlinput.GlobalDataSource;
import ajit.browser.lightning.urlinput.LocaleDataSource;
import ajit.browser.lightning.urlinput.QuickSearchRepository;
import ajit.browser.lightning.urlinput.QuickSearchViewModel;
import ajit.browser.lightning.urlinput.QuickSearchViewModelFactory;

import javax.annotation.Nullable;

import ajit.browser.focus.home.HomeFragment;
import ajit.browser.focus.persistence.TabsDatabase;
import ajit.browser.focus.utils.AppConstants;
import ajit.browser.focus.utils.RemoteConfigConstants;

public class Inject {

    private static boolean sIsNewCreated = true;

    public static String getDefaultTopSites(Context context) {

        return PreferenceManager.getDefaultSharedPreferences(context)
                .getString(HomeFragment.TOPSITES_PREF, null);

    }

    public static TabsDatabase getTabsDatabase(Context context) {
        return TabsDatabase.getInstance(context);
    }


    public static boolean isTelemetryEnabled(Context context) {
        // The first access to shared preferences will require a disk read.
        final StrictMode.ThreadPolicy threadPolicy = StrictMode.allowThreadDiskReads();
        try {
            final Resources resources = context.getResources();
            final SharedPreferences preferences = PreferenceManager.getDefaultSharedPreferences(context);
            final boolean isEnabledByDefault = AppConstants.isBuiltWithFirebase();
            // Telemetry is not enable by default in debug build. But the user / developer can choose to turn it on
            // in AndroidTest, this is enabled by default
            return preferences.getBoolean(resources.getString(R.string.pref_key_telemetry), isEnabledByDefault);
        } finally {
            StrictMode.setThreadPolicy(threadPolicy);
        }

    }

    public static void enableStrictMode() {
        if (AppConstants.isReleaseBuild()) {
            return;
        }

        final StrictMode.ThreadPolicy.Builder threadPolicyBuilder = new StrictMode.ThreadPolicy.Builder().detectAll();
        final StrictMode.VmPolicy.Builder vmPolicyBuilder = new StrictMode.VmPolicy.Builder().detectAll();

        threadPolicyBuilder.penaltyLog().penaltyDialog();
        // Previously we have penaltyDeath() for debug build, but in order to add crashlytics, we can't use it here.
        // ( crashlytics has untagged Network violation so it always crashes
        vmPolicyBuilder.penaltyLog();

        StrictMode.setThreadPolicy(threadPolicyBuilder.build());
        StrictMode.setVmPolicy(vmPolicyBuilder.build());
    }

    public static final boolean getActivityNewlyCreatedFlag() {
        return sIsNewCreated;
    }

    public static void setActivityNewlyCreatedFlag() {
        sIsNewCreated = false;
    }

    public static boolean isUnderEspressoTest() {
        return false;
    }

    public static RemoteConfigConstants.SURVEY getDefaultFeatureSurvey() {
        return RemoteConfigConstants.SURVEY.NONE;
    }

    public static DownloadInfoRepository provideDownloadInfoRepository() {
        //TODO inject data source, ex production DB or mock DB here
        return DownloadInfoRepository.getInstance();
    }

    public static DownloadIndicatorViewModel obtainDownloadIndicatorViewModel(FragmentActivity activity) {
        DownloadViewModelFactory factory = DownloadViewModelFactory.getInstance();
        return ViewModelProviders.of(activity, factory).get(DownloadIndicatorViewModel.class);
    }

    public static DownloadInfoViewModel obtainDownloadInfoViewModel(FragmentActivity activity) {
        DownloadViewModelFactory factory = DownloadViewModelFactory.getInstance();
        return ViewModelProviders.of(activity, factory).get(DownloadInfoViewModel.class);
    }

    private static QuickSearchRepository provideQuickSearchRepository(Application application) {
        return QuickSearchRepository.getInstance(GlobalDataSource.getInstance(application), LocaleDataSource.getInstance(application));
    }

    public static QuickSearchViewModel obtainQuickSearchViewModel(FragmentActivity activity) {
        QuickSearchViewModelFactory factory = new QuickSearchViewModelFactory(provideQuickSearchRepository(activity.getApplication()));
        return ViewModelProviders.of(activity, factory).get(QuickSearchViewModel.class);
    }

    public static void startAnimation(@Nullable View view, Animation animation) {
        if (view == null) {
            return;
        }
        view.startAnimation(animation);
    }
}