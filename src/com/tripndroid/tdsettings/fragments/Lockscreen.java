/*
 * Copyright (C) 2013 TripNDroid Mobile Engineering
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.tripndroid.tdsettings.fragments;

import android.content.Context;
import android.os.Bundle;
import android.preference.CheckBoxPreference;
import android.preference.Preference;
import android.preference.PreferenceScreen;
import android.preference.Preference.OnPreferenceChangeListener;
import android.provider.Settings;

import com.tripndroid.tdsettings.TRIPNDROIDPreferenceFragment;
import com.tripndroid.tdsettings.R;

public class Lockscreen extends TRIPNDROIDPreferenceFragment
        implements OnPreferenceChangeListener {

    public static final String KEY_SEE_TRHOUGH = "see_through";
    public static final String PREF_LOCKSCREEN_USE_CAROUSEL = "lockscreen_use_widget_container_carousel";

    private CheckBoxPreference mSeeThrough;
    private CheckBoxPreference mLockscreenUseCarousel;

    private Context mContext;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        addPreferencesFromResource(R.xml.lock_screen_settings);
        PreferenceScreen prefSet = getPreferenceScreen();
        mContext = getActivity();

        mSeeThrough = (CheckBoxPreference) prefSet.findPreference(KEY_SEE_TRHOUGH);

        mLockscreenUseCarousel = (CheckBoxPreference)findPreference(PREF_LOCKSCREEN_USE_CAROUSEL);
        mLockscreenUseCarousel.setChecked(Settings.System.getBoolean(getActivity().getContentResolver(),
                Settings.System.LOCKSCREEN_USE_WIDGET_CONTAINER_CAROUSEL, false));

    }

    @Override
    public boolean onPreferenceTreeClick(PreferenceScreen preferenceScreen, Preference preference) {
        if (preference == mSeeThrough) {
            Settings.System.putInt(mContext.getContentResolver(),
                    Settings.System.LOCKSCREEN_SEE_THROUGH, mSeeThrough.isChecked()
                    ? 1 : 0);
            return true;
        } else if (preference == mLockscreenUseCarousel) {
            Settings.System.putInt(getActivity().getContentResolver(),
                    Settings.System.LOCKSCREEN_USE_WIDGET_CONTAINER_CAROUSEL,
                    ((CheckBoxPreference)preference).isChecked() ? 1 : 0);
            return true;
        } 
        return super.onPreferenceTreeClick(preferenceScreen, preference);
    }

    public boolean onPreferenceChange(Preference preference, Object newValue) {
        return true;
    }
}
