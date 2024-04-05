package com.xue.peace;

import android.app.admin.DeviceAdminReceiver;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.os.Process;
import android.util.Log;

public class MyPolicyReceiver extends DeviceAdminReceiver {
    private static final String TAG = "MyPolicyReceiver";

    @Override
    public void onEnabled(Context context, Intent intent) {
        //设备管理可用
        Log.i(TAG,"onEnabled");
    }

    @Override
    public void onDisabled(Context context, Intent intent) {
        //设备管理不可用
        Log.i(TAG,"onDisabled");
        System.exit(0);
    }

}
