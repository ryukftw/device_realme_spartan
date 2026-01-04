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
import android.content.SharedPreferences;
import android.util.Log;

import androidx.preference.PreferenceManager;

import org.lineageos.settings.device.utils.FileUtils;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

/**
 * Controller for thermal profile functionality
 * Manages thermal states and per-app thermal profile mappings
 */
public class ThermalController {
    private static final String TAG = "ThermalController";
    private static final boolean DEBUG = false;

    private static final String THERMAL_NODE = "/sys/class/thermal/thermal_message/sconfig";
    private static final String THERMAL_PROFILE_KEY = "thermal_current_profile";
    private static final String THERMAL_AUTO_SWITCH_KEY = "thermal_auto_switch";
    private static final String THERMAL_APP_PROFILES_KEY = "thermal_app_profiles";

    private static final int MAX_WRITE_RETRIES = 3;
    private static final long DEBOUNCE_DELAY_MS = 1000; // 1 second cooldown

    private static ThermalController sInstance;
    private static final Object sLock = new Object();

    private final Context mContext;
    private final SharedPreferences mPrefs;
    private final List<StateChangeListener> mListeners = new ArrayList<>();
    private final Map<String, ThermalProfile> mAppProfiles = new HashMap<>();

    private ThermalProfile mCurrentProfile = ThermalProfile.DEFAULT;
    private boolean mAutoSwitchEnabled = false;
    private long mLastProfileChangeTime = 0;

    public interface StateChangeListener {
        void onProfileChanged(ThermalProfile profile);
        void onAutoSwitchChanged(boolean enabled);
    }

    private ThermalController(Context context) {
        mContext = context.getApplicationContext();
        mPrefs = PreferenceManager.getDefaultSharedPreferences(mContext);
        loadState();
    }

    public static ThermalController getInstance(Context context) {
        if (sInstance == null) {
            synchronized (sLock) {
                if (sInstance == null) {
                    sInstance = new ThermalController(context);
                }
            }
        }
        return sInstance;
    }

    /**
     * Check if thermal profile hardware is supported
     */
    public static boolean isSupported() {
        return FileUtils.fileExists(THERMAL_NODE);
    }

    /**
     * Register a state change listener
     */
    public void registerListener(StateChangeListener listener) {
        synchronized (sLock) {
            if (!mListeners.contains(listener)) {
                mListeners.add(listener);
            }
        }
    }

    /**
     * Unregister a state change listener
     */
    public void unregisterListener(StateChangeListener listener) {
        synchronized (sLock) {
            mListeners.remove(listener);
        }
    }

    /**
     * Set thermal profile
     */
    public boolean setThermalProfile(ThermalProfile profile) {
        synchronized (sLock) {
            if (!isSupported()) {
                Log.e(TAG, "Thermal profiles not supported");
                return false;
            }

            if (profile == null) {
                profile = ThermalProfile.DEFAULT;
            }

            // Apply debouncing to prevent rapid changes
            long currentTime = System.currentTimeMillis();
            if (currentTime - mLastProfileChangeTime < DEBOUNCE_DELAY_MS) {
                if (DEBUG) Log.d(TAG, "Debouncing profile change, ignoring");
                return true; // Not an error, just debounced
            }

            mCurrentProfile = profile;
            mLastProfileChangeTime = currentTime;
            saveCurrentProfile();

            if (!applyThermalProfile(profile)) {
                return false;
            }

            notifyProfileChanged();
            return true;
        }
    }

    /**
     * Get current thermal profile
     */
    public ThermalProfile getCurrentProfile() {
        synchronized (sLock) {
            return mCurrentProfile;
        }
    }

    /**
     * Set app-specific thermal profile
     */
    public void setAppProfile(String packageName, ThermalProfile profile) {
        synchronized (sLock) {
            if (packageName == null || packageName.isEmpty()) {
                return;
            }

            if (profile == null) {
                mAppProfiles.remove(packageName);
            } else {
                mAppProfiles.put(packageName, profile);
            }

            saveAppProfiles();
        }
    }

    /**
     * Get thermal profile for specific app
     */
    public ThermalProfile getAppProfile(String packageName) {
        synchronized (sLock) {
            return mAppProfiles.get(packageName);
        }
    }

    /**
     * Remove app-specific thermal profile
     */
    public void removeAppProfile(String packageName) {
        setAppProfile(packageName, null);
    }

    /**
     * Get all app-profile mappings
     */
    public Map<String, ThermalProfile> getAllAppProfiles() {
        synchronized (sLock) {
            return new HashMap<>(mAppProfiles);
        }
    }

    /**
     * Handle foreground app change (for automatic switching)
     */
    public void handleAppForeground(String packageName) {
        synchronized (sLock) {
            if (!mAutoSwitchEnabled) {
                return;
            }

            if (packageName == null || packageName.isEmpty() || "Unknown".equals(packageName)) {
                return;
            }

            ThermalProfile profile = mAppProfiles.get(packageName);
            if (profile != null && profile != mCurrentProfile) {
                if (DEBUG) Log.d(TAG, "Auto-switching to " + profile + " for " + packageName);
                setThermalProfile(profile);
            }
        }
    }

    /**
     * Enable/disable automatic profile switching
     */
    public void setAutoSwitchEnabled(boolean enabled) {
        synchronized (sLock) {
            if (mAutoSwitchEnabled != enabled) {
                mAutoSwitchEnabled = enabled;
                saveAutoSwitch();
                notifyAutoSwitchChanged();
            }
        }
    }

    /**
     * Check if automatic switching is enabled
     */
    public boolean isAutoSwitchEnabled() {
        synchronized (sLock) {
            return mAutoSwitchEnabled;
        }
    }

    /**
     * Apply thermal profile to hardware (write to sysfs with verification)
     */
    private boolean applyThermalProfile(ThermalProfile profile) {
        String value = String.valueOf(profile.getValue());

        for (int retry = 0; retry < MAX_WRITE_RETRIES; retry++) {
            try {
                FileUtils.writeLine(THERMAL_NODE, value);
                String verify = FileUtils.readOneLine(THERMAL_NODE);

                if (value.equals(verify)) {
                    if (DEBUG) Log.d(TAG, "Applied thermal profile: " + profile);
                    return true;
                } else {
                    Log.w(TAG, "Thermal profile verification failed (attempt " + (retry + 1) + "): expected=" + value + " actual=" + verify);
                }
            } catch (Exception e) {
                Log.e(TAG, "Failed to apply thermal profile (attempt " + (retry + 1) + ")", e);
            }

            // Wait before retry
            if (retry < MAX_WRITE_RETRIES - 1) {
                try {
                    Thread.sleep(50);
                } catch (InterruptedException e) {
                    Thread.currentThread().interrupt();
                    return false;
                }
            }
        }

        Log.e(TAG, "Failed to apply thermal profile after " + MAX_WRITE_RETRIES + " retries");
        return false;
    }

    /**
     * Load state from SharedPreferences
     */
    private void loadState() {
        synchronized (sLock) {
            // Load current profile
            String profileValue = mPrefs.getString(THERMAL_PROFILE_KEY, "0");
            mCurrentProfile = ThermalProfile.fromString(profileValue);

            // Load auto-switch state
            mAutoSwitchEnabled = mPrefs.getBoolean(THERMAL_AUTO_SWITCH_KEY, true);

            // Load app profiles
            loadAppProfiles();

            if (DEBUG) {
                Log.d(TAG, "Loaded state: profile=" + mCurrentProfile +
                      " autoSwitch=" + mAutoSwitchEnabled +
                      " appProfiles=" + mAppProfiles.size());
            }
        }
    }

    /**
     * Load app profiles from JSON
     */
    private void loadAppProfiles() {
        mAppProfiles.clear();

        try {
            String json = mPrefs.getString(THERMAL_APP_PROFILES_KEY, "{}");
            JSONObject obj = new JSONObject(json);
            Iterator<String> keys = obj.keys();

            while (keys.hasNext()) {
                String packageName = keys.next();
                int profileValue = obj.getInt(packageName);
                ThermalProfile profile = ThermalProfile.fromValue(profileValue);
                mAppProfiles.put(packageName, profile);
            }
        } catch (JSONException e) {
            Log.e(TAG, "Failed to load app profiles from JSON", e);
            mAppProfiles.clear();
        }
    }

    /**
     * Save current profile to SharedPreferences
     */
    private void saveCurrentProfile() {
        mPrefs.edit().putString(THERMAL_PROFILE_KEY, String.valueOf(mCurrentProfile.getValue())).apply();
        if (DEBUG) Log.d(TAG, "Saved current profile: " + mCurrentProfile);
    }

    /**
     * Save auto-switch state to SharedPreferences
     */
    private void saveAutoSwitch() {
        mPrefs.edit().putBoolean(THERMAL_AUTO_SWITCH_KEY, mAutoSwitchEnabled).apply();
        if (DEBUG) Log.d(TAG, "Saved auto-switch: " + mAutoSwitchEnabled);
    }

    /**
     * Save app profiles to JSON
     */
    private void saveAppProfiles() {
        try {
            JSONObject obj = new JSONObject();
            for (Map.Entry<String, ThermalProfile> entry : mAppProfiles.entrySet()) {
                obj.put(entry.getKey(), entry.getValue().getValue());
            }
            mPrefs.edit().putString(THERMAL_APP_PROFILES_KEY, obj.toString()).apply();
            if (DEBUG) Log.d(TAG, "Saved app profiles: " + mAppProfiles.size());
        } catch (JSONException e) {
            Log.e(TAG, "Failed to save app profiles to JSON", e);
        }
    }

    /**
     * Notify listeners of profile change
     */
    private void notifyProfileChanged() {
        for (StateChangeListener listener : mListeners) {
            listener.onProfileChanged(mCurrentProfile);
        }
    }

    /**
     * Notify listeners of auto-switch change
     */
    private void notifyAutoSwitchChanged() {
        for (StateChangeListener listener : mListeners) {
            listener.onAutoSwitchChanged(mAutoSwitchEnabled);
        }
    }

    /**
     * Restore thermal profile state (for boot)
     */
    public void restore() {
        synchronized (sLock) {
            if (!isSupported()) {
                Log.w(TAG, "Thermal profiles not supported on this device");
                return;
            }

            loadState();
            applyThermalProfile(mCurrentProfile);

            if (DEBUG) {
                Log.d(TAG, "Restored state: profile=" + mCurrentProfile +
                      " autoSwitch=" + mAutoSwitchEnabled +
                      " appProfiles=" + mAppProfiles.size());
            }
        }
    }
}
