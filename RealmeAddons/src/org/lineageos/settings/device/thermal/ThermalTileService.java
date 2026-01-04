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

import android.service.quicksettings.Tile;
import android.service.quicksettings.TileService;

/**
 * Quick Settings tile for thermal profile switching
 * Cycles through: Default -> Gaming -> Benchmark
 */
public class ThermalTileService extends TileService
        implements ThermalController.StateChangeListener {

    private ThermalController mController;

    @Override
    public void onCreate() {
        super.onCreate();
        mController = ThermalController.getInstance(this);
    }

    @Override
    public void onStartListening() {
        super.onStartListening();
        mController.registerListener(this);
        updateTile();
    }

    @Override
    public void onStopListening() {
        super.onStopListening();
        mController.unregisterListener(this);
    }

    @Override
    public void onClick() {
        super.onClick();

        if (!ThermalUtils.isSupported()) {
            getQsTile().setState(Tile.STATE_UNAVAILABLE);
            getQsTile().updateTile();
            return;
        }

        // Cycle through: Default -> Gaming -> Benchmark -> Default
        ThermalProfile currentProfile = mController.getCurrentProfile();
        ThermalProfile nextProfile;

        switch (currentProfile) {
            case DEFAULT:
                nextProfile = ThermalProfile.GAMING;
                break;
            case GAMING:
                nextProfile = ThermalProfile.BENCHMARK;
                break;
            default:
                nextProfile = ThermalProfile.DEFAULT;
                break;
        }

        mController.setThermalProfile(nextProfile);
        updateTile();
    }

    @Override
    public void onProfileChanged(ThermalProfile profile) {
        updateTile();
    }

    @Override
    public void onAutoSwitchChanged(boolean enabled) {
        // Not relevant for tile
    }

    private void updateTile() {
        Tile tile = getQsTile();
        if (tile == null) {
            return;
        }

        if (!ThermalUtils.isSupported()) {
            tile.setState(Tile.STATE_UNAVAILABLE);
        } else {
            ThermalProfile currentProfile = mController.getCurrentProfile();
            tile.setLabel(currentProfile.getName(this));
            tile.setSubtitle(currentProfile.getDescription(this));

            // Different state based on profile
            if (currentProfile == ThermalProfile.DEFAULT) {
                tile.setState(Tile.STATE_INACTIVE);
            } else {
                tile.setState(Tile.STATE_ACTIVE);
            }
        }

        tile.updateTile();
    }
}
