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

package org.lineageos.settings.device.display;

import android.content.SharedPreferences;
import android.service.quicksettings.Tile;
import android.service.quicksettings.TileService;
import androidx.preference.PreferenceManager;

import org.lineageos.settings.device.R;

public class AntiFlikerTileService extends TileService {

    private static final String ANTI_FLICKER_KEY = "anti_flicker";

    @Override
    public void onStartListening() {
        super.onStartListening();
        updateTile();
    }

    @Override
    public void onClick() {
        super.onClick();

        if (!AntiFlikerUtils.isSupported()) {
            getQsTile().setState(Tile.STATE_UNAVAILABLE);
            getQsTile().updateTile();
            return;
        }

        boolean currentState = AntiFlikerUtils.isCurrentlyEnabled();
        boolean newState = !currentState;

        if (AntiFlikerUtils.setEnabled(newState)) {
            // Save to preferences
            SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(this);
            prefs.edit().putBoolean(ANTI_FLICKER_KEY, newState).apply();

            updateTile();
        }
    }

    private void updateTile() {
        Tile tile = getQsTile();
        if (tile == null) {
            return;
        }

        if (!AntiFlikerUtils.isSupported()) {
            tile.setState(Tile.STATE_UNAVAILABLE);
        } else {
            boolean enabled = AntiFlikerUtils.isCurrentlyEnabled();
            tile.setState(enabled ? Tile.STATE_ACTIVE : Tile.STATE_INACTIVE);
        }

        tile.updateTile();
    }
}
