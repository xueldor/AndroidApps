package com.xue.peace;

import android.app.Application;
import android.app.admin.DevicePolicyManager;
import android.content.Context;
import android.content.pm.PackageManager;

public class App extends Application {
    public static App single;

    public DevicePolicyManager mDPM;
    public PackageManager pm;

    @Override
    public void onCreate() {
        super.onCreate();
        single = this;
        mDPM = (DevicePolicyManager) getSystemService(Context.DEVICE_POLICY_SERVICE);
        pm = getPackageManager();
    }
}
