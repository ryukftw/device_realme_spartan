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

import android.app.Service;
import android.content.Intent;
import android.os.Handler;
import android.os.IBinder;
import android.os.Looper;
import android.util.Log;

import org.lineageos.settings.device.gamebar.ForegroundAppDetector;

/**
 * Background service that monitors foreground app changes and
 * automatically switches thermal profiles when enabled
 */
public class ThermalMonitorService extends Service {

    private static final String TAG = "ThermalMonitorService";
    private static final boolean DEBUG = false;

    private static final long MONITOR_INTERVAL = 500; // 500ms polling interval

    private Handler mHandler;
    private Runnable mMonitorRunnable;
    private ThermalController mController;
    private String mLastPackageName = "";

    @Override
    public void onCreate() {
        super.onCreate();

        mController = ThermalController.getInstance(this);
        mHandler = new Handler(Looper.getMainLooper());

        mMonitorRunnable = new Runnable() {
            @Override
            public void run() {
                monitorForegroundApp();
                mHandler.postDelayed(this, MONITOR_INTERVAL);
            }
        };

        if (DEBUG) Log.d(TAG, "Service created");
    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        if (!mController.isAutoSwitchEnabled()) {
            if (DEBUG) Log.d(TAG, "Auto-switch disabled, stopping service");
            stopSelf();
            return START_NOT_STICKY;
        }

        mHandler.post(mMonitorRunnable);
        if (DEBUG) Log.d(TAG, "Service started, monitoring enabled");

        return START_STICKY;
    }

    @Override
    public IBinder onBind(Intent intent) {
        return null;
    }

    @Override
    public void onDestroy() {
        super.onDestroy();

        if (mHandler != null && mMonitorRunnable != null) {
            mHandler.removeCallbacks(mMonitorRunnable);
        }

        if (DEBUG) Log.d(TAG, "Service destroyed");
    }

    /**
     * Monitor foreground app and switch thermal profile if needed
     */
    private void monitorForegroundApp() {
        if (!mController.isAutoSwitchEnabled()) {
            stopSelf();
            return;
        }

        String packageName = ForegroundAppDetector.getForegroundPackageName(this);

        if (packageName != null && !packageName.equals(mLastPackageName)) {
            if (DEBUG) Log.d(TAG, "Foreground app changed: " + mLastPackageName + " -> " + packageName);

            mController.handleAppForeground(packageName);
            mLastPackageName = packageName;
        }
    }
}
