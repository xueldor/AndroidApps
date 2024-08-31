package com.xue.peace.ui.main;

import android.content.Context;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;
import android.content.pm.Signature;

public class SignatureChecker {

    public static boolean isPlatformSigned(Context context) {
        try {
            // 获取应用自身的签名
            PackageInfo packageInfo = context.getPackageManager().getPackageInfo(
                    context.getPackageName(), PackageManager.GET_SIGNATURES);
            Signature[] appSignatures = packageInfo.signatures;

            // 获取系统 platform 签名
            packageInfo = context.getPackageManager().getPackageInfo(
                    "android", PackageManager.GET_SIGNATURES);
            Signature[] platformSignatures = packageInfo.signatures;

            // 比较签名
            if (appSignatures != null && platformSignatures != null) {
                for (Signature appSignature : appSignatures) {
                    for (Signature platformSignature : platformSignatures) {
                        if (appSignature.equals(platformSignature)) {
                            return true;
                        }
                    }
                }
            }
        } catch (PackageManager.NameNotFoundException e) {
            e.printStackTrace();
        }

        return false;
    }
}