package com.xue.autoshutdown;

import android.app.AlarmManager;
import android.app.PendingIntent;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.IBinder;
import android.os.SystemClock;
import android.util.Log;
import android.widget.Toast;

import java.lang.reflect.Method;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;

public class BootBroadcastReciver extends BroadcastReceiver {
    private static final String TAG = "XueShutdownBootBroadcastReciver";
    @Override
    public void onReceive(Context context, Intent intent) {
        Log.i(TAG, "onReceive");
        Toast.makeText(context, "Boot Complete", Toast.LENGTH_SHORT).show();
        Date date = Utils.getSmartShutTime(context, Utils.getPrefTime(context));
        if (date != null) {
            Utils.setShutdownAlarm(context, date);
        }
    }
}