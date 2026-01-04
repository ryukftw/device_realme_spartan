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

package org.lineageos.settings.device.display;

import android.content.Context;
import android.content.SharedPreferences;
import androidx.preference.PreferenceManager;

import org.lineageos.settings.device.utils.FileUtils;

public class AntiFlikerUtils {

    private static final String ANTI_FLICKER_NODE = "/sys/kernel/oplus_display/dimlayer_bl_en";

    private static final String ANTI_FLICKER_KEY = "anti_flicker";

    /**
     * Check if anti-flicker is supported on this device
     */
    public static boolean isSupported() {
        return FileUtils.fileExists(ANTI_FLICKER_NODE);
    }

    /**
     * Enable or disable anti-flicker
     */
    public static boolean setEnabled(boolean enabled) {
        return FileUtils.writeLine(ANTI_FLICKER_NODE, enabled ? "1" : "0");
    }

    /**
     * Get current anti-flicker state from sysfs
     */
    public static boolean isCurrentlyEnabled() {
        String value = FileUtils.readOneLine(ANTI_FLICKER_NODE);
        return "1".equals(value);
    }

    /**
     * Restore anti-flicker state from shared preferences (for boot)
     */
    public static void restore(Context context) {
        if (!isSupported()) {
            return;
        }

        SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(context);
        boolean enabled = prefs.getBoolean(ANTI_FLICKER_KEY, false);
        setEnabled(enabled);
    }
}
