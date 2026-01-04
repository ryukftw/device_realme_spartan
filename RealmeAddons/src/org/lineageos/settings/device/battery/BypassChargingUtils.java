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

package org.lineageos.settings.device.battery;

import android.content.Context;

/**
 * Helper class for bypass charging
 * Delegates to BypassChargingController
 */
public class BypassChargingUtils {

    /**
     * Check if bypass charging is supported on this device
     */
    public static boolean isSupported() {
        return BypassChargingController.isSupported();
    }

    /**
     * Enable or disable bypass charging
     */
    public static boolean setEnabled(Context context, boolean enabled) {
        BypassChargingController controller = BypassChargingController.getInstance(context);
        return enabled ? controller.enableBypassCharging() : controller.disableBypassCharging();
    }

    /**
     * Get current bypass charging state
     */
    public static boolean isCurrentlyEnabled(Context context) {
        return BypassChargingController.getInstance(context).isBypassEnabled();
    }

    /**
     * Check if power is currently connected
     */
    public static boolean isPowerConnected(Context context) {
        return BypassChargingController.getInstance(context).isPowerConnected();
    }

    /**
     * Restore bypass charging state from shared preferences (for boot)
     */
    public static void restore(Context context) {
        if (!isSupported()) {
            return;
        }
        BypassChargingController.getInstance(context).restore();
    }
}
