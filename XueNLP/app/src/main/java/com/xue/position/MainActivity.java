package com.xue.position;

import android.Manifest;
import android.app.Activity;
import android.content.Context;
import android.content.pm.PackageManager;
import android.os.Bundle;
import android.os.Handler;

import com.xue.position.baidu.BaiduLocationProvider;

public class MainActivity extends Activity {

    private BaiduLocationProvider mBaiduLocationProvider;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        if(!hasPermissions(this)){
            requestPermissions(getPermissions(),0);
        }
        if(mBaiduLocationProvider == null) {
            mBaiduLocationProvider = BaiduLocationProvider.getInstance(this.getApplicationContext());
        }
        Handler handler = new Handler();
        handler.post(new Runnable() {
            @Override
            public void run() {
                mBaiduLocationProvider.requestLocation();
//                handler.postDelayed(this, 2000);
            }
        });
    }

    public String[] getPermissions() {
        return new String[] {
                Manifest.permission.ACCESS_COARSE_LOCATION,
                Manifest.permission.ACCESS_FINE_LOCATION,
                Manifest.permission.READ_EXTERNAL_STORAGE,
                Manifest.permission.WRITE_EXTERNAL_STORAGE
        };
    }
    boolean hasPermissions(Context context) {
        String[] permissions = getPermissions();
        for (String p : permissions) {
            if (p == Manifest.permission.ACCESS_COARSE_LOCATION
                    || p == Manifest.permission.ACCESS_FINE_LOCATION) {
                continue;
            }
            if(checkSelfPermission(p) != PackageManager.PERMISSION_GRANTED) {
                return false;
            }
        }

        boolean coarseLocation = checkSelfPermission(Manifest.permission.ACCESS_COARSE_LOCATION) == PackageManager.PERMISSION_GRANTED;
        boolean fineLocation = checkSelfPermission(Manifest.permission.ACCESS_FINE_LOCATION) == PackageManager.PERMISSION_GRANTED;
        return coarseLocation || fineLocation;
    }
}