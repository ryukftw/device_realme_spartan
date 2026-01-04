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

package org.lineageos.settings.device.gameoptimizer;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.os.Bundle;

import androidx.preference.Preference;
import androidx.preference.Preference.OnPreferenceChangeListener;
import androidx.preference.SwitchPreferenceCompat;

import com.android.settingslib.widget.SettingsBasePreferenceFragment;

import org.lineageos.settings.device.R;
import org.lineageos.settings.device.battery.BypassChargingController;
import org.lineageos.settings.device.battery.BypassChargingUtils;

public class GameOptimizerFragment extends SettingsBasePreferenceFragment
        implements OnPreferenceChangeListener, BypassChargingController.StateChangeListener {

    private static final String KEY_BYPASS_CHARGING = "bypass_charging";

    private SwitchPreferenceCompat mBypassChargingPreference;
    private BypassChargingController mController;

    private final BroadcastReceiver mPowerReceiver = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            String action = intent.getAction();
            if (Intent.ACTION_POWER_CONNECTED.equals(action)) {
                mController.handlePowerConnected();
            } else if (Intent.ACTION_POWER_DISCONNECTED.equals(action)) {
                mController.handlePowerDisconnected();
            }
        }
    };

    @Override
    public void onCreatePreferences(Bundle savedInstanceState, String rootKey) {
        addPreferencesFromResource(R.xml.game_optimizer_preferences);

        mController = BypassChargingController.getInstance(getContext());

        mBypassChargingPreference = findPreference(KEY_BYPASS_CHARGING);
        if (mBypassChargingPreference != null) {
            if (BypassChargingUtils.isSupported()) {
                mBypassChargingPreference.setOnPreferenceChangeListener(this);
                updateBypassChargingState();
            } else {
                getPreferenceScreen().removePreference(mBypassChargingPreference);
            }
        }
    }

    @Override
    public void onResume() {
        super.onResume();
        if (mBypassChargingPreference != null && BypassChargingUtils.isSupported()) {
            // Register power state receiver
            IntentFilter filter = new IntentFilter();
            filter.addAction(Intent.ACTION_POWER_CONNECTED);
            filter.addAction(Intent.ACTION_POWER_DISCONNECTED);
            getContext().registerReceiver(mPowerReceiver, filter);

            // Register controller listener
            mController.registerListener(this);

            updateBypassChargingState();
        }
    }

    @Override
    public void onPause() {
        super.onPause();
        if (mBypassChargingPreference != null && BypassChargingUtils.isSupported()) {
            try {
                getContext().unregisterReceiver(mPowerReceiver);
            } catch (IllegalArgumentException e) {
                // Receiver not registered, ignore
            }

            mController.unregisterListener(this);
        }
    }

    @Override
    public void onStateChanged(boolean bypassEnabled, boolean powerConnected) {
        if (mBypassChargingPreference == null) {
            return;
        }

        // Update checked state
        mBypassChargingPreference.setChecked(bypassEnabled);

        // Update enabled state and summary based on power connection
        mBypassChargingPreference.setEnabled(powerConnected);
        if (!powerConnected) {
            mBypassChargingPreference.setSummary(R.string.bypass_charging_unavailable_summary);
        } else {
            mBypassChargingPreference.setSummary(R.string.bypass_charging_summary);
        }
    }

    private void updateBypassChargingState() {
        if (mBypassChargingPreference == null) {
            return;
        }
        boolean isPowerConnected = BypassChargingUtils.isPowerConnected(getContext());
        boolean isBypassEnabled = BypassChargingUtils.isCurrentlyEnabled(getContext());

        mBypassChargingPreference.setChecked(isBypassEnabled);
        mBypassChargingPreference.setEnabled(isPowerConnected);
        if (!isPowerConnected) {
            mBypassChargingPreference.setSummary(R.string.bypass_charging_unavailable_summary);
        } else {
            mBypassChargingPreference.setSummary(R.string.bypass_charging_summary);
        }
    }

    @Override
    public boolean onPreferenceChange(Preference preference, Object newValue) {
        if (KEY_BYPASS_CHARGING.equals(preference.getKey())) {
            boolean enabled = (Boolean) newValue;
            return BypassChargingUtils.setEnabled(getContext(), enabled);
        }
        return false;
    }
}
