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

import android.app.Activity;
import android.content.ClipData;
import android.content.ClipboardManager;
import android.content.Intent;
import android.os.Bundle;
import android.widget.Toast;

import com.tripndroid.tdsettings.R;

public class ShareToClipboard extends Activity {
    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        ClipboardManager mClipboardManager = (ClipboardManager) this
                .getSystemService(CLIPBOARD_SERVICE);

        Intent intent = getIntent();

        CharSequence text = intent.getCharSequenceExtra(Intent.EXTRA_TEXT);

        mClipboardManager.setPrimaryClip(ClipData.newPlainText("Shared to RC", text));

        Toast.makeText(this, R.string.clipboard_notification, Toast.LENGTH_SHORT).show();
        finish();
    }
}
