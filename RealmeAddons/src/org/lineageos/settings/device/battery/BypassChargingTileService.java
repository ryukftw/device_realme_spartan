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

import android.service.quicksettings.Tile;
import android.service.quicksettings.TileService;

public class BypassChargingTileService extends TileService
        implements BypassChargingController.StateChangeListener {

    private BypassChargingController mController;

    @Override
    public void onStartListening() {
        super.onStartListening();
        mController = BypassChargingController.getInstance(this);
        mController.registerListener(this);
        updateTile();
    }

    @Override
    public void onStopListening() {
        super.onStopListening();
        if (mController != null) {
            mController.unregisterListener(this);
        }
    }

    @Override
    public void onStateChanged(boolean bypassEnabled, boolean powerConnected) {
        updateTile();
    }

    @Override
    public void onClick() {
        super.onClick();

        if (!BypassChargingUtils.isSupported()) {
            getQsTile().setState(Tile.STATE_UNAVAILABLE);
            getQsTile().updateTile();
            return;
        }

        if (!mController.isPowerConnected()) {
            // Don't allow toggling when not charging
            updateTile();
            return;
        }

        boolean currentState = mController.isBypassEnabled();
        boolean newState = !currentState;

        BypassChargingUtils.setEnabled(this, newState);
        updateTile();
    }

    private void updateTile() {
        Tile tile = getQsTile();
        if (tile == null) {
            return;
        }

        if (!BypassChargingUtils.isSupported()) {
            tile.setState(Tile.STATE_UNAVAILABLE);
        } else if (!mController.isPowerConnected()) {
            // Disable tile when not charging
            tile.setState(Tile.STATE_UNAVAILABLE);
        } else {
            boolean enabled = mController.isBypassEnabled();
            tile.setState(enabled ? Tile.STATE_ACTIVE : Tile.STATE_INACTIVE);
        }

        tile.updateTile();
    }
}
