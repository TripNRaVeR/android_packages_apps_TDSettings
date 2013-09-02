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

package com.tripndroid.tdsettings.util;

import android.os.AsyncTask;

import com.tripndroid.tdsettings.objects.EasyPair;
import com.tripndroid.tdsettings.util.CMDProcessor;
import com.tripndroid.tdsettings.util.Helpers;

public abstract class AbstractAsyncSuCMDProcessor extends AsyncTask<String, Void, String> {
    // if /system needs to be mounted before command
    private boolean mMountSystem;
    // su terminal we execute on
    private CMDProcessor mTerm;
    // return if we recieve a null command or empty command
    public final String FAILURE = "failed_no_command";

    /**
     * Constructor that allows mounting/dismounting
     * of /system partition while in background thread
     */
    public AbstractAsyncSuCMDProcessor(boolean mountSystem) {
         this.mMountSystem = mountSystem;
    }

    /**
     * Constructor that assumes /system should not be mounted
     */
    public AbstractAsyncSuCMDProcessor() {
         this.mMountSystem = false;
    }

    /**
     * DO NOT override this method you should simply send your commands off
     * as params and expect to handle results in {@link #onPostExecute}
     *
     * if you find a need to @Override this method then you should
     * consider using a new AsyncTask implentation instead
     *
     * @param params The parameters of the task.
     *
     * @return A result, defined by the subclass of this task.
     */
    @Override
    protected String doInBackground(String... params) {
        // don't bother if we don't get a command
        if (params[0] == null || params[0].trim().equals(""))
            return FAILURE;

        mTerm = new CMDProcessor();
        EasyPair<String, String> pairedOutput = new EasyPair<String, String>(FAILURE, FAILURE);

        // conditionally enforce mounting
        if (mMountSystem) {
            Helpers.getMount("rw");
        }
        try {
            // process all commands ***DO NOT SEND null OR ""; you have been warned***
            for (int i = 0; params.length > i; i++) {
                // always watch for null and empty strings, lazy devs :/
                if (params[i] != null && !params[i].trim().equals(""))
                    pairedOutput = mTerm.su.runWaitFor(params[i]).getOutput();
                else
                    // bail because of careless devs
                    return FAILURE;
            }
        // always unmount
        } finally {
            if (mMountSystem)
                Helpers.getMount("ro");
        }
        // return the last commmand result output EasyPair<stdout, stderr>
        return pairedOutput.getFirst();
    }

    @Override
    protected abstract void onPostExecute(String result);
}

