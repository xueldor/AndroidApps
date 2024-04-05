package com.xue.position;

import android.app.Service;
import android.content.Context;
import android.content.Intent;
import android.content.pm.ApplicationInfo;
import android.content.pm.PackageManager;
import android.os.IBinder;
import android.util.Log;

import com.xue.position.baidu.BaiduLocationProvider;

public class XueLocationService extends Service {
    private static final String TAG = "XueLocationService";
    private BaiduLocationProvider mBaiduLocationProvider;

    @Override
    public IBinder onBind(Intent intent) {
        if(mBaiduLocationProvider == null) {
            mBaiduLocationProvider = BaiduLocationProvider.getInstance(this);
        }
        IBinder binder = mBaiduLocationProvider.getBinder();
        if (binder != null) {
            Log.i(TAG, "BaiduLocationProvider getBinder");
            return binder;
        }
        Log.e(TAG, "BaiduLocationProvider error");
        return null;
    }

    public static int getUid(Context context){
        try {
            String packageName = context.getPackageName(); // 指定包名
            PackageManager pm = context.getPackageManager();
            ApplicationInfo ai = pm.getApplicationInfo(packageName, PackageManager.GET_ACTIVITIES);
            Log.d("UID", "getUid:" + ai.uid);
            return ai.uid;
        } catch (PackageManager.NameNotFoundException e) {
            e.printStackTrace();
        }
        return -1;
    }
}