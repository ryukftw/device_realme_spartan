/*
 * Copyright (C) 2025 The LineageOS Project
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package org.lineageos.settings.device.thermal;

import android.content.Intent;
import android.content.pm.ApplicationInfo;
import android.content.pm.PackageManager;
import android.content.pm.ResolveInfo;
import android.graphics.drawable.Drawable;
import android.os.AsyncTask;
import android.os.Bundle;

import androidx.preference.ListPreference;
import androidx.preference.Preference;
import androidx.preference.PreferenceCategory;
import androidx.preference.SwitchPreferenceCompat;

import com.android.settingslib.widget.SettingsBasePreferenceFragment;

import org.lineageos.settings.device.R;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

/**
 * Fragment displaying thermal profile preferences with per-app configuration
 */
public class ThermalProfileFragment extends SettingsBasePreferenceFragment
        implements ThermalController.StateChangeListener {

    private ThermalController mController;
    private PreferenceCategory mAppProfilesCategory;
    private SwitchPreferenceCompat mAutoSwitchPref;

    @Override
    public void onCreatePreferences(Bundle savedInstanceState, String rootKey) {
        addPreferencesFromResource(R.xml.thermal_profile_preferences);

        mController = ThermalController.getInstance(getContext());

        // Get preference references
        mAutoSwitchPref = findPreference("thermal_auto_switch");
        mAppProfilesCategory = findPreference("thermal_app_profiles_category");

        // Set up auto-switch preference
        if (mAutoSwitchPref != null) {
            mAutoSwitchPref.setChecked(mController.isAutoSwitchEnabled());
            mAutoSwitchPref.setOnPreferenceChangeListener((preference, newValue) -> {
                boolean enabled = (Boolean) newValue;
                mController.setAutoSwitchEnabled(enabled);
                return true;
            });
        }

        // Load apps asynchronously
        new LoadAppsTask().execute();
    }

    @Override
    public void onResume() {
        super.onResume();
        mController.registerListener(this);
    }

    @Override
    public void onPause() {
        super.onPause();
        mController.unregisterListener(this);
    }

    @Override
    public void onProfileChanged(ThermalProfile profile) {
        // Update UI if needed
    }

    @Override
    public void onAutoSwitchChanged(boolean enabled) {
        if (mAutoSwitchPref != null) {
            mAutoSwitchPref.setChecked(enabled);
        }
    }

    /**
     * AsyncTask to load installed apps in background
     */
    private class LoadAppsTask extends AsyncTask<Void, Void, List<AppInfo>> {

        @Override
        protected List<AppInfo> doInBackground(Void... voids) {
            return loadInstalledApps();
        }

        @Override
        protected void onPostExecute(List<AppInfo> appList) {
            if (mAppProfilesCategory != null && getContext() != null) {
                populateAppPreferences(appList);
            }
        }
    }

    /**
     * Load all installed apps with launcher activities
     */
    private List<AppInfo> loadInstalledApps() {
        List<AppInfo> appList = new ArrayList<>();
        PackageManager pm = getContext().getPackageManager();

        // Get all apps with launcher activities
        Intent mainIntent = new Intent(Intent.ACTION_MAIN, null);
        mainIntent.addCategory(Intent.CATEGORY_LAUNCHER);
        List<ResolveInfo> resolveInfos = pm.queryIntentActivities(mainIntent, 0);

        for (ResolveInfo resolveInfo : resolveInfos) {
            ApplicationInfo appInfo = resolveInfo.activityInfo.applicationInfo;
            String packageName = appInfo.packageName;

            // Skip system package
            if (packageName.equals("org.lineageos.settings.device")) {
                continue;
            }

            try {
                String appName = appInfo.loadLabel(pm).toString();
                Drawable icon = appInfo.loadIcon(pm);

                // Get thermal profile for this app (default if not set)
                ThermalProfile profile = mController.getAppProfile(packageName);
                if (profile == null) {
                    profile = ThermalProfile.DEFAULT;
                }

                appList.add(new AppInfo(packageName, appName, icon, profile));
            } catch (Exception e) {
                // Skip apps that fail to load
            }
        }

        // Sort alphabetically
        Collections.sort(appList);

        return appList;
    }

    /**
     * Populate the preference screen with app preferences
     */
    private void populateAppPreferences(List<AppInfo> appList) {
        mAppProfilesCategory.removeAll();

        for (AppInfo appInfo : appList) {
            ListPreference pref = new ListPreference(getContext());
            pref.setKey("thermal_app_" + appInfo.getPackageName());
            pref.setTitle(appInfo.getAppName());
            pref.setIcon(appInfo.getIcon());

            // Set up entries and values
            CharSequence[] entries = new CharSequence[ThermalProfile.values().length];
            CharSequence[] entryValues = new CharSequence[ThermalProfile.values().length];

            for (int i = 0; i < ThermalProfile.values().length; i++) {
                ThermalProfile profile = ThermalProfile.values()[i];
                entries[i] = profile.getName(getContext());
                entryValues[i] = String.valueOf(profile.getValue());
            }

            pref.setEntries(entries);
            pref.setEntryValues(entryValues);

            // Set current value
            ThermalProfile currentProfile = appInfo.getThermalProfile();
            pref.setValue(String.valueOf(currentProfile.getValue()));
            pref.setSummary(currentProfile.getName(getContext()));

            // Handle preference changes
            pref.setOnPreferenceChangeListener((preference, newValue) -> {
                int profileValue = Integer.parseInt((String) newValue);
                ThermalProfile newProfile = ThermalProfile.fromValue(profileValue);

                mController.setAppProfile(appInfo.getPackageName(), newProfile);
                appInfo.setThermalProfile(newProfile);

                pref.setSummary(newProfile.getName(getContext()));
                return true;
            });

            mAppProfilesCategory.addPreference(pref);
        }
    }
}
