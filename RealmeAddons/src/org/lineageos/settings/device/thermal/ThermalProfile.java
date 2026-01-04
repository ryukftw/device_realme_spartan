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

import android.content.Context;

import org.lineageos.settings.device.R;

/**
 * Enum representing thermal profile states for device thermal management.
 * Each profile maps to a specific thermal configuration value written to sysfs.
 */
public enum ThermalProfile {
    DEFAULT(0, R.string.thermal_profile_default, R.string.thermal_profile_default_desc),
    BENCHMARK(1, R.string.thermal_profile_benchmark, R.string.thermal_profile_benchmark_desc),
    BROWSER(2, R.string.thermal_profile_browser, R.string.thermal_profile_browser_desc),
    CAMERA(3, R.string.thermal_profile_camera, R.string.thermal_profile_camera_desc),
    DIALER(4, R.string.thermal_profile_dialer, R.string.thermal_profile_dialer_desc),
    GAMING(5, R.string.thermal_profile_gaming, R.string.thermal_profile_gaming_desc),
    STREAMING(6, R.string.thermal_profile_streaming, R.string.thermal_profile_streaming_desc);

    private final int mValue;
    private final int mNameResId;
    private final int mDescResId;

    ThermalProfile(int value, int nameResId, int descResId) {
        mValue = value;
        mNameResId = nameResId;
        mDescResId = descResId;
    }

    public int getValue() {
        return mValue;
    }

    public String getName(Context context) {
        return context.getString(mNameResId);
    }

    public String getDescription(Context context) {
        return context.getString(mDescResId);
    }

    /**
     * Get ThermalProfile from integer value
     */
    public static ThermalProfile fromValue(int value) {
        for (ThermalProfile profile : values()) {
            if (profile.mValue == value) {
                return profile;
            }
        }
        return DEFAULT;
    }

    /**
     * Get ThermalProfile from string value
     */
    public static ThermalProfile fromString(String value) {
        try {
            return fromValue(Integer.parseInt(value));
        } catch (NumberFormatException e) {
            return DEFAULT;
        }
    }
}
