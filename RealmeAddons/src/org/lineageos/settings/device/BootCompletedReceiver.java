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

package org.lineageos.settings.device;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.util.Log;

import org.lineageos.settings.device.battery.BypassChargingUtils;
import org.lineageos.settings.device.battery.PowerStateMonitorService;
import org.lineageos.settings.device.display.AntiFlikerUtils;
import org.lineageos.settings.device.thermal.ThermalUtils;

public class BootCompletedReceiver extends BroadcastReceiver {

    private static final String TAG = "RealmeAddons";

    @Override
    public void onReceive(final Context context, Intent intent) {
        Log.d(TAG, "Received boot completed intent");

        // Restore anti-flicker state
        AntiFlikerUtils.restore(context);

        // Restore bypass charging state
        BypassChargingUtils.restore(context);

        // Restore thermal profile state
        ThermalUtils.restore(context);

        // Start power state monitor service
        Intent serviceIntent = new Intent(context, PowerStateMonitorService.class);
        context.startService(serviceIntent);
    }
}
