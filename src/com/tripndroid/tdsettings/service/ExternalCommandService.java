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

package com.tripndroid.tdsettings.service;

import com.tripndroid.tdsettings.util.CMDProcessor;

import android.app.IntentService;
import android.content.Intent;

public class ExternalCommandService extends IntentService {

    public ExternalCommandService() {
        super(ExternalCommandService.class.getSimpleName());
    }

    @Override
    protected void onHandleIntent(Intent intent) {
        if (intent.hasExtra("cmd")) {
            new CMDProcessor().su.runWaitFor(intent.getStringExtra("cmd"));
        }
    }
}
