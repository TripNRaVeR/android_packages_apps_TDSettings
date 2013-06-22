package com.tripndroid.tdsettings.service;

import android.app.Notification;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.app.Service;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.AsyncTask;
import android.os.IBinder;
import android.preference.PreferenceManager;
import android.provider.Settings;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.util.List;

import com.tripndroid.tdsettings.TRIPNDROIDPreferenceFragment;
import com.tripndroid.tdsettings.R;
import com.tripndroid.tdsettings.service.CodeReceiver;
import com.tripndroid.tdsettings.util.AbstractAsyncSuCMDProcessor;
import com.tripndroid.tdsettings.util.CMDProcessor;
import com.tripndroid.tdsettings.util.Helpers;
import com.tripndroid.tdsettings.service.CodeReceiver;

public class BootService extends Service {

    public static boolean servicesStarted = false;

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        if (intent == null) {
            stopSelf();
        }
        new BootWorker(this).execute();
        return START_STICKY;
    }

    @Override
    public IBinder onBind(Intent intent) {
        return null;
    }

    class BootWorker extends AsyncTask<Void, Void, Void> {

        Context c;

        public BootWorker(Context c) {
            this.c = c;
        }

        @Override
        protected Void doInBackground(Void... args) {
            SharedPreferences preferences = PreferenceManager.getDefaultSharedPreferences(c);

            if (HeadphoneService.getUserHeadphoneAudioMode(c) != -1
                    || HeadphoneService.getUserBTAudioMode(c) != -1) {
                c.startService(new Intent(c, HeadphoneService.class));
            }

            if (FlipService.getUserFlipAudioMode(c) != -1
                    || FlipService.getUserCallSilent(c) != 0)
                c.startService(new Intent(c, FlipService.class));

            return null;
        }

        @Override
        protected void onPostExecute(Void result) {
            super.onPostExecute(result);
            servicesStarted = true;
            stopSelf();
        }
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
    }
}
