package com.xue.autoshutdown;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.os.IBinder;
import android.os.SystemClock;
import android.os.UserHandle;
import android.util.Log;
import android.widget.Toast;

import java.lang.reflect.Method;

public class AlarmBroadcastReciver extends BroadcastReceiver {
    private static final String TAG = "XueShutdownAlarmBroadcastReciver";
    @Override
    public void onReceive(Context context, Intent intent) {
        Log.i(TAG, "onReceive, will shutdown after 10s");
        Toast.makeText(context,"10s后关机", Toast.LENGTH_LONG).show();
        //通知其它应用即将关机
        Intent notifyIntent = new Intent(Intent.ACTION_SHUTDOWN);
        notifyIntent.addFlags(Intent.FLAG_RECEIVER_FOREGROUND);
        context.sendBroadcast(notifyIntent);
        SystemClock.sleep(7000);
        Toast.makeText(context,"即将关机", Toast.LENGTH_LONG).show();
        SystemClock.sleep(3000);
        //反射调用PowerManager
         try {
            Class<?> serviceManager = Class.forName("android.os.ServiceManager");
            Method getService = serviceManager.getMethod("getService", String.class);
            Object remoteService = getService.invoke(null, Context.POWER_SERVICE);
            Class<?> stub = Class.forName("android.os.IPowerManager$Stub");
            Method asInterface = stub.getMethod("asInterface", IBinder.class);
            Object powerManager = asInterface.invoke(null, remoteService);
            Method shutdown = powerManager.getClass().getDeclaredMethod("shutdown",
                    boolean.class, String.class, boolean.class);
            shutdown.invoke(powerManager, false, "", true);
        } catch (Exception e) {
            //nothing to do
        }
    }
}