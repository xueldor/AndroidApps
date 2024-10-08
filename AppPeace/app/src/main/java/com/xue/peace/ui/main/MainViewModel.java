package com.xue.peace.ui.main;

import android.app.admin.DevicePolicyManager;
import android.content.ComponentName;
import android.content.Context;
import android.content.pm.ApplicationInfo;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;
import android.graphics.drawable.BitmapDrawable;
import android.os.SystemClock;
import android.util.Log;

import androidx.lifecycle.ViewModel;

import com.xue.peace.App;
import com.xue.peace.AppInfoBean;
import com.xue.peace.MyPolicyReceiver;

import java.util.ArrayList;
import java.util.List;

public class MainViewModel extends ViewModel {

    private static final String TAG = "MainViewModel";

    private int mMode;

    private DevicePolicyManager mDpm;
    private PackageManager pm;

    public void setMode(int mode) {
        mMode = mode;
    }
    public boolean isDeviceOwner() {
        return App.single.mDPM.isDeviceOwnerApp(App.single.getPackageName());
    }



    public boolean hasHidePermission() {
        return isDeviceOwner();
    }
    public boolean hasSystemPermission() {
        try {
            Context ctx = App.single;
            PackageInfo packageInfo = App.single.pm.getPackageInfo(ctx.getPackageName(), PackageManager.GET_SIGNING_CERTIFICATES);
            boolean systemapp = isSystemApp(ctx, packageInfo);
            return systemapp && SignatureChecker.isPlatformSigned(ctx);
        } catch (PackageManager.NameNotFoundException e) {
            e.printStackTrace();
        }
        return false;
    }

    private boolean isSystemApp(Context ctx,PackageInfo pi) {
        boolean isSystemApp = false;
        // 是系统中已安装的应用
        if (pi != null) {
            boolean isSysApp = (pi.applicationInfo.flags & ApplicationInfo.FLAG_SYSTEM) == 1;
            boolean isSysUpd = (pi.applicationInfo.flags & ApplicationInfo.FLAG_UPDATED_SYSTEM_APP) == 1;
            isSystemApp = isSysApp || isSysUpd;
        }
        return isSystemApp;
    }
    public List<AppInfoBean> getAllApps(Context ctx, List<AppInfoBean> outUserApps, List<AppInfoBean> outSystemApps){
        if(outUserApps != null) outUserApps.clear();
        if(outSystemApps != null) outSystemApps.clear();

        long start = SystemClock.elapsedRealtime();
        PackageManager pm = ctx.getPackageManager();
        DevicePolicyManager dpm = (DevicePolicyManager) ctx.getSystemService(Context.DEVICE_POLICY_SERVICE);
        ComponentName dpmCompName = new ComponentName(ctx, MyPolicyReceiver.class);

//        List<ApplicationInfo> appInfos = pm.getInstalledApplications(PackageManager.MATCH_UNINSTALLED_PACKAGES);
        List<PackageInfo>  pkgInfos = pm.getInstalledPackages(PackageManager.MATCH_UNINSTALLED_PACKAGES);

        List<AppInfoBean> retList = new ArrayList<>(pkgInfos.size());
        for (PackageInfo info : pkgInfos) {
            AppInfoBean appInfoBean = createAppInfoBean(info, pm, dpm, dpmCompName);
            if(isSystemApp(ctx,info)){
                if(outSystemApps != null) {
                    outSystemApps.add(appInfoBean);
                }
            }else {
                if(outUserApps != null){
                    outUserApps.add(appInfoBean);
                }
            }
            retList.add(appInfoBean);
        }
        long end = SystemClock.elapsedRealtime();
        Log.i(TAG,"getInstalledPackages cost " + (end - start) + "ms");
        return retList;
    }
    private AppInfoBean createAppInfoBean(PackageInfo info,PackageManager pm,
                                          DevicePolicyManager dpm,ComponentName dpmCompName) {
        AppInfoBean tmp = new AppInfoBean();
        tmp.setAppName(info.applicationInfo.loadLabel(pm).toString());
        tmp.setAppIcon(info.applicationInfo.loadIcon(pm));
        tmp.setApplicationInfo(info.applicationInfo);
        tmp.setPackageInfo(info);
        String tmpPkgName = info.applicationInfo.packageName;
        tmp.setAppPackageName(tmpPkgName);
        if (isDeviceOwner()) {
            tmp.setHide(dpm.isApplicationHidden(dpmCompName, tmpPkgName));
        } else {
            tmp.setHide(null); // 没有权限判断是否hide
        }
        tmp.setDisable(!info.applicationInfo.enabled);
        tmp.setSuspend(!((info.applicationInfo.flags & ApplicationInfo.FLAG_SUSPENDED) == 0));
        tmp.setAppUid(info.applicationInfo.uid);
//        int enableState = pm.getApplicationEnabledSetting(tmpPkgName);
//        if(enableState == PackageManager.COMPONENT_ENABLED_STATE_DEFAULT){
//            tmp.setDisable(!info.applicationInfo.enabled);
//        }else if(enableState == )
        return tmp;
    }

    public void updateAppInfoBean(Context ctx, AppInfoBean bean){
        String pkgName = bean.getAppPackageName();
        DevicePolicyManager dpm = (DevicePolicyManager)ctx.getSystemService(Context.DEVICE_POLICY_SERVICE);
        try {
            if (isDeviceOwner()) {
                ComponentName dpmCompName = new ComponentName(ctx, MyPolicyReceiver.class);
                bean.setHide(dpm.isApplicationHidden(dpmCompName, pkgName));
            } else {
                bean.setHide(null); // 没有权限判断是否hide
            }
            PackageInfo info = ctx.getPackageManager().getPackageInfo(pkgName, PackageManager.GET_SIGNING_CERTIFICATES);
            bean.setDisable(!info.applicationInfo.enabled);
            bean.setSuspend(!((info.applicationInfo.flags & ApplicationInfo.FLAG_SUSPENDED) == 0));
            bean.setPackageInfo(info);
        } catch (PackageManager.NameNotFoundException e) {
            e.printStackTrace();
        }
    }

    @Override
    protected void onCleared() {

    }
}
