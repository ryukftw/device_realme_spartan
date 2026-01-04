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
import android.content.Intent;

/**
 * Utility class for thermal profile management
 * Simple static wrapper around ThermalController
 */
public class ThermalUtils {

    /**
     * Check if thermal profiles are supported on this device
     */
    public static boolean isSupported() {
        return ThermalController.isSupported();
    }

    /**
     * Set thermal profile
     */
    public static boolean setThermalProfile(Context context, ThermalProfile profile) {
        return ThermalController.getInstance(context).setThermalProfile(profile);
    }

    /**
     * Get current thermal profile
     */
    public static ThermalProfile getCurrentProfile(Context context) {
        return ThermalController.getInstance(context).getCurrentProfile();
    }

    /**
     * Set app-specific thermal profile
     */
    public static void setAppProfile(Context context, String packageName, ThermalProfile profile) {
        ThermalController.getInstance(context).setAppProfile(packageName, profile);
    }

    /**
     * Get thermal profile for specific app
     */
    public static ThermalProfile getAppProfile(Context context, String packageName) {
        return ThermalController.getInstance(context).getAppProfile(packageName);
    }

    /**
     * Enable/disable automatic profile switching
     */
    public static void setAutoSwitchEnabled(Context context, boolean enabled) {
        ThermalController controller = ThermalController.getInstance(context);
        controller.setAutoSwitchEnabled(enabled);

        // Start or stop monitoring service based on state
        Intent serviceIntent = new Intent(context, ThermalMonitorService.class);
        if (enabled) {
            context.startService(serviceIntent);
        } else {
            context.stopService(serviceIntent);
        }
    }

    /**
     * Check if automatic switching is enabled
     */
    public static boolean isAutoSwitchEnabled(Context context) {
        return ThermalController.getInstance(context).isAutoSwitchEnabled();
    }

    /**
     * Restore thermal profile state (for boot)
     */
    public static void restore(Context context) {
        ThermalController controller = ThermalController.getInstance(context);
        controller.restore();

        // Enable auto-switch and start monitoring service
        controller.setAutoSwitchEnabled(true);
        context.startService(new Intent(context, ThermalMonitorService.class));
    }
}
