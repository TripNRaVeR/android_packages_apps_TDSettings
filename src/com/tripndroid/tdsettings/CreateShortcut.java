/*
 * Copyright (C) 2010 The Android Open Source Project
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

package com.tripndroid.tdsettings;

import android.app.LauncherActivity;
import android.content.Intent;
import android.view.View;
import android.widget.ListView;

public class CreateShortcut extends LauncherActivity {

    @Override
    protected Intent getTargetIntent() {
        Intent targetIntent = new Intent(Intent.ACTION_MAIN, null);
        targetIntent.addCategory("com.tripndroid.tdsettings.SHORTCUT");
        targetIntent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
        return targetIntent;
    }

    @Override
    protected void onListItemClick(ListView l, View v, int position, long id) {
        Intent shortcutIntent = intentForPosition(position);

        String intentClass = shortcutIntent.getComponent().getClassName();

        shortcutIntent = new Intent();
        shortcutIntent.setClass(getApplicationContext(), TDSettingsActivity.class);
        shortcutIntent.setAction("com.tripndroid.tdsettings.START_NEW_FRAGMENT");
        shortcutIntent.putExtra("tripndroid_fragment_name", intentClass);
        shortcutIntent.addFlags(Intent.FLAG_ACTIVITY_SINGLE_TOP);
        shortcutIntent.setFlags(Intent.FLAG_ACTIVITY_RESET_TASK_IF_NEEDED);

        Intent intent = new Intent();
        intent.putExtra(Intent.EXTRA_SHORTCUT_ICON_RESOURCE,
                Intent.ShortcutIconResource.fromContext(this, getProperShortcutIcon(intentClass)));
        intent.putExtra(Intent.EXTRA_SHORTCUT_INTENT, shortcutIntent);
        intent.putExtra(Intent.EXTRA_SHORTCUT_NAME, itemForPosition(position).label);
        setResult(RESULT_OK, intent);
        finish();
    }

    private int getProperShortcutIcon(String className) {
        String c = className.substring(className.lastIndexOf(".") + 1);

        if (c.equals("LEDControl"))
            return R.drawable.ic_tdsettings_led;
        else if (c.equals("Lockscreens"))
            return R.drawable.ic_tdsettings_lockscreens;
        else if (c.equals("Sound"))
            return R.drawable.ic_tdsettings_sound;
        else if (c.equals("Navbar"))
            return R.drawable.ic_tdsettings_navigation_bar;
        else if (c.equals("StatusBarBattery"))
            return R.drawable.ic_tdsettings_battery;
        else if (c.equals("StatusBarClock"))
            return R.drawable.ic_tdsettings_clock;
        else if (c.equals("StatusBarGeneral"))
            return R.drawable.ic_tdsettings_general;
        else if (c.equals("StatusBarToggles"))
            return R.drawable.ic_tdsettings_toggles;
        else if (c.equals("UserInterface"))
            return R.drawable.ic_tdsettings_general_ui;
        else if (c.equals("Weather"))
            return R.drawable.ic_tdsettings_weather;
        else
            return R.mipmap.ic_launcher;
    }

    @Override
    protected boolean onEvaluateShowIcons() {
        return false;
    }
}
