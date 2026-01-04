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

import android.graphics.drawable.Drawable;

/**
 * Model class representing an application with thermal profile assignment
 */
public class AppInfo implements Comparable<AppInfo> {
    private final String mPackageName;
    private final String mAppName;
    private final Drawable mIcon;
    private ThermalProfile mThermalProfile;

    public AppInfo(String packageName, String appName, Drawable icon, ThermalProfile thermalProfile) {
        mPackageName = packageName;
        mAppName = appName;
        mIcon = icon;
        mThermalProfile = thermalProfile;
    }

    public String getPackageName() {
        return mPackageName;
    }

    public String getAppName() {
        return mAppName;
    }

    public Drawable getIcon() {
        return mIcon;
    }

    public ThermalProfile getThermalProfile() {
        return mThermalProfile;
    }

    public void setThermalProfile(ThermalProfile profile) {
        mThermalProfile = profile;
    }

    @Override
    public int compareTo(AppInfo other) {
        return mAppName.compareToIgnoreCase(other.mAppName);
    }
}
