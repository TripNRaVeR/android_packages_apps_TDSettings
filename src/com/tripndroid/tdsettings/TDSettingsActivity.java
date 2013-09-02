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

package com.tripndroid.tdsettings;

import android.app.ActionBar;
import android.app.Activity;
import android.app.Fragment;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.res.Configuration;
import android.os.Bundle;
import android.preference.Preference;
import android.preference.PreferenceActivity;
import android.preference.PreferenceFragment;
import android.provider.Settings;
import android.text.TextUtils;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView.AdapterContextMenuInfo;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.ListAdapter;
import android.widget.Switch;
import android.widget.TextView;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

public class TDSettingsActivity extends PreferenceActivity implements ButtonBarHandler {

    private static final String TAG = "TDSettings";

    protected HashMap<Integer, Integer> mHeaderIndexMap = new HashMap<Integer, Integer>();
    private List<Header> mHeaders;

    private String mFragmentClass;
    private int mTopLevelHeaderId;
    private Header mFirstHeader;
    private Header mCurrentHeader;
    boolean mInLocalHeaderSwitch;

    protected boolean isShortcut;

    @Override
    public void onCreate(Bundle savedInstanceState) {

        mInLocalHeaderSwitch = true;
        super.onCreate(savedInstanceState);
        mInLocalHeaderSwitch = false;

        if (!onIsHidingHeaders() && onIsMultiPane()) {
            highlightHeader();
            setTitle(R.string.app_name);
        }

        if ("com.tripndroid.tdsettings.START_NEW_FRAGMENT".equals(getIntent().getAction())) {
            String className = getIntent().getStringExtra("tripndroid_fragment_name").toString();
            Class<?> cls = null;
            try {
                cls = Class.forName(className);
            } catch (ClassNotFoundException e1) {
                // can't find the class at all, die
                return;
            }

            try {
                cls.asSubclass(TDSettingsActivity.class);
                return;
            } catch (ClassCastException e) {
                // fall through
            }

            try {
                cls.asSubclass(Fragment.class);
                Bundle b = new Bundle();
                b.putBoolean("started_from_shortcut", true);
                isShortcut = true;
                startWithFragment(className, b, null, 0);
                finish(); // close current activity
                return;
            } catch (ClassCastException e) {
            }

            try {
                cls.asSubclass(Activity.class);
                isShortcut = true;
                Intent activity = new Intent(getApplicationContext(), cls);
                activity.putExtra("started_from_shortcut", true);
                startActivity(activity);
                finish(); // close current activity
                return;
            } catch (ClassCastException e) {
            }
        }
        ActionBar actionBar = getActionBar();
        actionBar.setDisplayHomeAsUpEnabled(true);

    }

    @Override
    public void onBackPressed() {
        if (isShortcut) {
            finish();
        } else {
            super.onBackPressed();
        }
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {

            Configuration config = getBaseContext().getResources().getConfiguration();

        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        AdapterContextMenuInfo info = (AdapterContextMenuInfo) item.getMenuInfo();
        switch (item.getItemId()) {
            case android.R.id.home:
                onBackPressed();
                return true;
            default:
                return super.onContextItemSelected(item);
        }
    }

    /**
     * Populate the activity with the top-level headers.
     */
    @Override
    public void onBuildHeaders(List<Header> target) {
        loadHeadersFromResource(R.xml.preference_headers, target);
        ArrayList<Header> toRemove = new ArrayList<Header>();
        for (int i=0; i<target.size(); i++) {
            Header header = target.get(i);
        }
        for (int i=0; i<toRemove.size(); i++) {
            target.remove(toRemove.get(i));
        }
        updateHeaderList(target);
        mHeaders = target;
    }

    /**
     * Override initial header when an activity-alias is causing Settings to be
     * launched for a specific fragment encoded in the android:name parameter.
     */
    @Override
    public Header onGetInitialHeader() {
        String fragmentClass = getStartingFragmentClass(super.getIntent());
        if (fragmentClass != null) {
            Header header = new Header();
            header.fragment = fragmentClass;
            header.title = getTitle();
            header.fragmentArguments = getIntent().getExtras();
            mCurrentHeader = header;
            return header;
        }

        return mFirstHeader;
    }

    @Override
    public boolean hasNextButton() {
        return super.hasNextButton();
    }

    @Override
    public Button getNextButton() {
        return super.getNextButton();
    }

    private void highlightHeader() {
        if (mTopLevelHeaderId != 0) {
            Integer index = mHeaderIndexMap.get(mTopLevelHeaderId);
            if (index != null) {
                getListView().setItemChecked(index, true);
                getListView().smoothScrollToPosition(index);
            }
        }
    }

    private void updateHeaderList(List<Header> target) {
        int i = 0;
        while (i < target.size()) {
            Header header = target.get(i);

            int id = (int) header.id;

            if (target.get(i) == header) {
                if (mFirstHeader == null &&
                        HeaderAdapter.getHeaderType(header) != HeaderAdapter.HEADER_TYPE_CATEGORY) {
                    mFirstHeader = header;
                }
                mHeaderIndexMap.put(id, i);
                i++;
            }
        }
    }

    @Override
    public void onResume() {
        super.onResume();

        ListAdapter listAdapter = getListAdapter();
        if (listAdapter instanceof HeaderAdapter) {
            ((HeaderAdapter) listAdapter).resume();
        }
    }

    @Override
    public void onPause() {
        super.onPause();

        ListAdapter listAdapter = getListAdapter();
        if (listAdapter instanceof HeaderAdapter) {
            ((HeaderAdapter) listAdapter).pause();
        }
    }

    protected String getStartingFragmentClass(Intent intent) {
        if (mFragmentClass != null)
            return mFragmentClass;

        String intentClass = intent.getComponent().getClassName();
        if (intentClass.equals(getClass().getName()))
            return null;

        return intentClass;
    }

    @Override
    public void setListAdapter(ListAdapter adapter) {
        if (mHeaders == null) {
            mHeaders = new ArrayList<Header>();
            for (int i = 0; i < adapter.getCount(); i++) {
                mHeaders.add((Header) adapter.getItem(i));
            }
        }

        super.setListAdapter(new HeaderAdapter(this, mHeaders));
    }

    private static class HeaderAdapter extends ArrayAdapter<Header> {
        static final int HEADER_TYPE_CATEGORY = 0;
        static final int HEADER_TYPE_NORMAL = 1;
        static final int HEADER_TYPE_SWITCH = 2;
        private static final int HEADER_TYPE_COUNT = HEADER_TYPE_SWITCH + 1;

        private static class HeaderViewHolder {
            ImageView icon;
            TextView title;
            TextView summary;
            Switch switch_;
        }

        private LayoutInflater mInflater;

        static int getHeaderType(Header header) {
            if (header.fragment == null && header.intent == null) {
                return HEADER_TYPE_CATEGORY;
            } else {
                return HEADER_TYPE_NORMAL;
            }
        }

        @Override
        public int getItemViewType(int position) {
            Header header = getItem(position);
            return getHeaderType(header);
        }

        @Override
        public boolean areAllItemsEnabled() {
            return false;
        }

        @Override
        public boolean isEnabled(int position) {
            return getItemViewType(position) != HEADER_TYPE_CATEGORY;
        }

        @Override
        public int getViewTypeCount() {
            return HEADER_TYPE_COUNT;
        }

        @Override
        public boolean hasStableIds() {
            return true;
        }

        public HeaderAdapter(Context context, List<Header> objects) {
            super(context, 0, objects);
            mInflater = (LayoutInflater) context.getSystemService(Context.LAYOUT_INFLATER_SERVICE);

        }

        @Override
        public View getView(int position, View convertView, ViewGroup parent) {
            HeaderViewHolder holder;
            Header header = getItem(position);
            int headerType = getHeaderType(header);
            View view = null;

            if (convertView == null) {
                holder = new HeaderViewHolder();
                switch (headerType) {
                    case HEADER_TYPE_CATEGORY:
                        view = new TextView(getContext(), null,
                                android.R.attr.listSeparatorTextViewStyle);
                        holder.title = (TextView) view;
                        break;

                    case HEADER_TYPE_NORMAL:
                        view = mInflater.inflate(
                                com.android.internal.R.layout.preference_header_item, parent,
                                false);
                        holder.icon = (ImageView) view.findViewById(com.android.internal.R.id.icon);
                        holder.title = (TextView)
                                view.findViewById(com.android.internal.R.id.title);
                        holder.summary = (TextView)
                                view.findViewById(com.android.internal.R.id.summary);
                        break;
                }
                view.setTag(holder);
            } else {
                view = convertView;
                holder = (HeaderViewHolder) view.getTag();
            }

            switch (headerType) {
                case HEADER_TYPE_CATEGORY:
                    holder.title.setText(header.getTitle(getContext().getResources()));
                    break;
                case HEADER_TYPE_NORMAL:
                    holder.icon.setImageResource(header.iconRes);
                    holder.title.setText(header.getTitle(getContext().getResources()));
                    CharSequence summary = header.getSummary(getContext().getResources());
                    if (!TextUtils.isEmpty(summary)) {
                        holder.summary.setVisibility(View.VISIBLE);
                        holder.summary.setText(summary);
                    } else {
                        holder.summary.setVisibility(View.GONE);
                    }
                    break;
            }

            return view;
        }

        public void resume() {
        }

        public void pause() {
        }
    }

    @Override
    public boolean onPreferenceStartFragment(PreferenceFragment caller, Preference pref) {

        int titleRes = pref.getTitleRes();

        startPreferencePanel(pref.getFragment(), pref.getExtras(), titleRes, null, null, 0);
        return true;
    }

}
