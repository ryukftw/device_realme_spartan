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

import android.os.Bundle;
import androidx.preference.Preference;
import androidx.preference.Preference.OnPreferenceChangeListener;
import androidx.preference.SwitchPreferenceCompat;

import com.android.settingslib.widget.SettingsBasePreferenceFragment;

import org.lineageos.settings.device.display.AntiFlikerUtils;

public class DeviceSettingsFragment extends SettingsBasePreferenceFragment
        implements OnPreferenceChangeListener {

    private static final String KEY_ANTI_FLICKER = "anti_flicker";

    private SwitchPreferenceCompat mAntiFlikerPreference;

    @Override
    public void onCreatePreferences(Bundle savedInstanceState, String rootKey) {
        addPreferencesFromResource(R.xml.device_settings);

        mAntiFlikerPreference = findPreference(KEY_ANTI_FLICKER);
        if (mAntiFlikerPreference != null) {
            if (AntiFlikerUtils.isSupported()) {
                mAntiFlikerPreference.setEnabled(true);
                mAntiFlikerPreference.setOnPreferenceChangeListener(this);
            } else {
                getPreferenceScreen().removePreference(mAntiFlikerPreference);
            }
        }
    }

    @Override
    public boolean onPreferenceChange(Preference preference, Object newValue) {
        if (KEY_ANTI_FLICKER.equals(preference.getKey())) {
            boolean enabled = (Boolean) newValue;
            return AntiFlikerUtils.setEnabled(enabled);
        }
        return false;
    }
}
