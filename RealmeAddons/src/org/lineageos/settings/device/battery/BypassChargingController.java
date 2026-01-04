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
import android.content.SharedPreferences;
import android.util.Log;

import androidx.preference.PreferenceManager;

import org.lineageos.settings.device.utils.FileUtils;

import java.util.ArrayList;
import java.util.List;

/**
 * Controller for bypass charging functionality
 * Manages state and handles power events
 */
public class BypassChargingController {
    private static final String TAG = "BypassChargingController";
    private static final boolean DEBUG = false;

    private static final String BYPASS_CHARGING_NODE = "/sys/devices/virtual/oplus_chg/battery/mmi_charging_enable";
    private static final String BYPASS_CHARGING_KEY = "bypass_charging";

    // Hardware control values (inverted logic)
    private static final String BYPASS_ENABLED = "0";  // Bypass active
    private static final String BYPASS_DISABLED = "1"; // Normal charging

    private static BypassChargingController sInstance;
    private static final Object sLock = new Object();

    private final Context mContext;
    private final SharedPreferences mPrefs;
    private final List<StateChangeListener> mListeners = new ArrayList<>();

    private boolean mIsPowerConnected = false;
    private boolean mBypassEnabled = false;

    public interface StateChangeListener {
        void onStateChanged(boolean bypassEnabled, boolean powerConnected);
    }

    private BypassChargingController(Context context) {
        mContext = context.getApplicationContext();
        mPrefs = PreferenceManager.getDefaultSharedPreferences(mContext);
        loadState();
    }

    public static BypassChargingController getInstance(Context context) {
        if (sInstance == null) {
            synchronized (sLock) {
                if (sInstance == null) {
                    sInstance = new BypassChargingController(context);
                }
            }
        }
        return sInstance;
    }

    /**
     * Check if bypass charging hardware is supported
     */
    public static boolean isSupported() {
        return FileUtils.fileExists(BYPASS_CHARGING_NODE);
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
     * Handle power connected event
     */
    public void handlePowerConnected() {
        synchronized (sLock) {
            if (DEBUG) Log.d(TAG, "Power connected");
            mIsPowerConnected = true;

            // If bypass was enabled, re-enable hardware
            if (mBypassEnabled) {
                enableHardwareBypass();
            }

            notifyStateChanged();
        }
    }

    /**
     * Handle power disconnected event
     */
    public void handlePowerDisconnected() {
        synchronized (sLock) {
            if (DEBUG) Log.d(TAG, "Power disconnected");
            mIsPowerConnected = false;

            // Disable hardware bypass when unplugged
            disableHardwareBypass();

            notifyStateChanged();
        }
    }

    /**
     * Enable bypass charging
     */
    public boolean enableBypassCharging() {
        synchronized (sLock) {
            if (!isSupported()) {
                Log.e(TAG, "Bypass charging not supported");
                return false;
            }

            mBypassEnabled = true;
            saveState();

            // Only enable hardware if power is connected
            if (mIsPowerConnected) {
                if (!enableHardwareBypass()) {
                    mBypassEnabled = false;
                    saveState();
                    return false;
                }
            }

            notifyStateChanged();
            return true;
        }
    }

    /**
     * Disable bypass charging
     */
    public boolean disableBypassCharging() {
        synchronized (sLock) {
            if (!isSupported()) {
                return false;
            }

            mBypassEnabled = false;
            saveState();

            disableHardwareBypass();

            notifyStateChanged();
            return true;
        }
    }

    /**
     * Get current bypass charging enabled state
     */
    public boolean isBypassEnabled() {
        synchronized (sLock) {
            return mBypassEnabled;
        }
    }

    /**
     * Get current power connected state
     */
    public boolean isPowerConnected() {
        synchronized (sLock) {
            return mIsPowerConnected;
        }
    }

    /**
     * Enable hardware bypass (write to sysfs with verification)
     */
    private boolean enableHardwareBypass() {
        try {
            FileUtils.writeLine(BYPASS_CHARGING_NODE, BYPASS_ENABLED);
            String verify = FileUtils.readOneLine(BYPASS_CHARGING_NODE);
            if (!BYPASS_ENABLED.equals(verify)) {
                Log.e(TAG, "Hardware bypass enable verification failed: expected=" + BYPASS_ENABLED + " actual=" + verify);
                return false;
            }
            if (DEBUG) Log.d(TAG, "Hardware bypass enabled");
            return true;
        } catch (Exception e) {
            Log.e(TAG, "Failed to enable hardware bypass", e);
            return false;
        }
    }

    /**
     * Disable hardware bypass (write to sysfs with verification)
     */
    private boolean disableHardwareBypass() {
        try {
            FileUtils.writeLine(BYPASS_CHARGING_NODE, BYPASS_DISABLED);
            String verify = FileUtils.readOneLine(BYPASS_CHARGING_NODE);
            if (!BYPASS_DISABLED.equals(verify)) {
                Log.e(TAG, "Hardware bypass disable verification failed: expected=" + BYPASS_DISABLED + " actual=" + verify);
                return false;
            }
            if (DEBUG) Log.d(TAG, "Hardware bypass disabled");
            return true;
        } catch (Exception e) {
            Log.e(TAG, "Failed to disable hardware bypass", e);
            return false;
        }
    }

    /**
     * Load state from SharedPreferences
     */
    private void loadState() {
        synchronized (sLock) {
            mBypassEnabled = mPrefs.getBoolean(BYPASS_CHARGING_KEY, false);
            if (DEBUG) Log.d(TAG, "Loaded state: bypassEnabled=" + mBypassEnabled);
        }
    }

    /**
     * Save state to SharedPreferences
     */
    private void saveState() {
        mPrefs.edit().putBoolean(BYPASS_CHARGING_KEY, mBypassEnabled).apply();
        if (DEBUG) Log.d(TAG, "Saved state: bypassEnabled=" + mBypassEnabled);
    }

    /**
     * Notify all listeners of state change
     */
    private void notifyStateChanged() {
        for (StateChangeListener listener : mListeners) {
            listener.onStateChanged(mBypassEnabled, mIsPowerConnected);
        }
    }

    /**
     * Restore bypass charging state (for boot)
     */
    public void restore() {
        synchronized (sLock) {
            if (!isSupported()) {
                return;
            }

            loadState();

            // Only restore hardware state if power is connected
            if (mBypassEnabled && mIsPowerConnected) {
                enableHardwareBypass();
            }

            if (DEBUG) Log.d(TAG, "Restored state: bypassEnabled=" + mBypassEnabled + " powerConnected=" + mIsPowerConnected);
        }
    }
}
