/*
 * Copyright (C) 2012 The CyanogenMod Project
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

package com.tripndroid.tdsettings.fragments;

import android.app.Activity;
import android.app.AlertDialog;
import android.app.Dialog;
import android.app.DialogFragment;
import android.content.ContentResolver;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.content.pm.ResolveInfo;
import android.content.res.Configuration;
import android.content.res.Resources;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Point;
import android.graphics.drawable.AnimationDrawable;
import android.graphics.drawable.BitmapDrawable;
import android.net.Uri;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.preference.CheckBoxPreference;
import android.preference.ListPreference;
import android.preference.Preference;
import android.preference.PreferenceGroup;
import android.preference.PreferenceScreen;
import android.preference.Preference.OnPreferenceChangeListener;
import android.preference.TwoStatePreference;
import android.provider.MediaStore;
import android.provider.Settings;
import android.text.TextUtils;
import android.util.Log;
import android.view.Display;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.CheckBox;
import android.widget.CompoundButton;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.SeekBar;
import android.widget.SeekBar.OnSeekBarChangeListener;
import android.widget.TextView;
import android.widget.Toast;

import com.tripndroid.tdsettings.TRIPNDROIDPreferenceFragment;
import com.tripndroid.tdsettings.R;
import com.tripndroid.tdsettings.service.CodeReceiver;
import com.tripndroid.tdsettings.util.AbstractAsyncSuCMDProcessor;
import com.tripndroid.tdsettings.util.CMDProcessor;
import com.tripndroid.tdsettings.util.Helpers;
import com.tripndroid.tdsettings.util.ShortcutPickerHelper;
import com.tripndroid.tdsettings.widgets.AlphaSeekBar;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.URISyntaxException;
import java.nio.channels.FileChannel;
import java.security.SecureRandom;
import java.util.Enumeration;
import java.util.List;
import java.util.Random;
import java.util.zip.ZipEntry;
import java.util.zip.ZipFile;

/**
 * Lockscreen Buttons Settings
 */
public class LockscreenButtons extends TRIPNDROIDPreferenceFragment
        implements Preference.OnPreferenceChangeListener {

    private static final String TAG = "LockscreenButtons";

    private static final String LONG_PRESS_BACK = "lockscreen_long_press_back";
    private static final String LONG_PRESS_HOME = "lockscreen_long_press_home";
    private static final String LONG_PRESS_MENU = "lockscreen_long_press_menu";

    // Masks for checking presence of hardware keys.
    // Must match values in frameworks/base/core/res/res/values/config.xml
    private static final int KEY_MASK_HOME = 0x01;
    private static final int KEY_MASK_BACK = 0x02;
    private static final int KEY_MASK_MENU = 0x04;

    private ListPreference mLongBackAction;
    private ListPreference mLongHomeAction;
    private ListPreference mLongMenuAction;
    private ListPreference[] mActions;

    private boolean torchSupported() {
        return getResources().getBoolean(R.bool.has_led_flash);
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        final int deviceKeys = getResources().getInteger(
                com.android.internal.R.integer.config_deviceHardwareKeys);
        final boolean hasHomeKey = (deviceKeys & KEY_MASK_HOME) != 0;
        final boolean hasBackKey = (deviceKeys & KEY_MASK_BACK) != 0;
        final boolean hasMenuKey = (deviceKeys & KEY_MASK_MENU) != 0;

        addPreferencesFromResource(R.xml.lockscreen_buttons_settings);

        PreferenceScreen prefSet = getPreferenceScreen();

        mLongBackAction = (ListPreference) prefSet.findPreference(LONG_PRESS_BACK);
        if (hasBackKey) {
            mLongBackAction.setKey(Settings.System.LOCKSCREEN_LONG_BACK_ACTION);
        } else {
            getPreferenceScreen().removePreference(mLongBackAction);
        }

        mLongHomeAction = (ListPreference) prefSet.findPreference(LONG_PRESS_HOME);
        if (hasHomeKey) {
            mLongHomeAction.setKey(Settings.System.LOCKSCREEN_LONG_HOME_ACTION);
        } else {
            getPreferenceScreen().removePreference(mLongHomeAction);
        }

        mLongMenuAction = (ListPreference) prefSet.findPreference(LONG_PRESS_MENU);
        if (hasMenuKey) {
            mLongMenuAction.setKey(Settings.System.LOCKSCREEN_LONG_MENU_ACTION);
        } else {
            getPreferenceScreen().removePreference(mLongMenuAction);
        }

        mActions = new ListPreference[] {
            mLongBackAction, mLongHomeAction, mLongMenuAction
        };
        for (ListPreference pref : mActions) {
            if (torchSupported()) {
                final CharSequence[] oldEntries = pref.getEntries();
                final CharSequence[] oldValues = pref.getEntryValues();
                ArrayList<CharSequence> newEntries = new ArrayList<CharSequence>();
                ArrayList<CharSequence> newValues = new ArrayList<CharSequence>();
                for (int i = 0; i < oldEntries.length; i++) {
                    newEntries.add(oldEntries[i].toString());
                    newValues.add(oldValues[i].toString());
                }
                newEntries.add(getString(R.string.lockscreen_buttons_flashlight));
                newValues.add("FLASHLIGHT");
                pref.setEntries(
                        newEntries.toArray(new CharSequence[newEntries.size()]));
                pref.setEntryValues(
                        newValues.toArray(new CharSequence[newValues.size()]));
            }
            pref.setOnPreferenceChangeListener(this);
        }
    }


    @Override
    public void onResume() {
        super.onResume();

        for (ListPreference pref : mActions) {
            updateEntry(pref);
        }
    }

    private void updateEntry(ListPreference pref) {
        String value = Settings.System.getString(getContentResolver(), pref.getKey());
        if (value == null) {
            value = "";
        }

        CharSequence entry = findEntryForValue(pref, value);
        if (entry != null) {
            pref.setValue(value);
            pref.setSummary(entry);
            return;
        }
    }

    private CharSequence findEntryForValue(ListPreference pref, CharSequence value) {
        CharSequence[] entries = pref.getEntryValues();
        for (int i = 0; i < entries.length; i++) {
            if (TextUtils.equals(entries[i], value)) {
                return pref.getEntries()[i];
            }
        }
        return null;
    }

    @Override
    public boolean onPreferenceChange(Preference pref, Object newValue) {
        /* we only have ListPreferences, so know newValue is a string */
        ListPreference list = (ListPreference) pref;
        String value = (String) newValue;

        if (Settings.System.putString(getContentResolver(), list.getKey(), value)) {
            pref.setSummary(findEntryForValue(list, value));
        }

        return true;
    }

}
