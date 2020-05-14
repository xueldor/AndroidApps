package com.xue.peace;

import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;

import android.app.admin.DevicePolicyManager;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.view.WindowManager;

import com.xue.peace.ui.main.MainFragment;

public class MainActivity extends AppCompatActivity {
    private DevicePolicyManager mDPM;
    private ComponentName mConmp;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.main_activity);
        mDPM = (DevicePolicyManager) getSystemService(Context.DEVICE_POLICY_SERVICE);
        mConmp = new ComponentName(this, MyPolicyReceiver.class);
        if(mDPM.isDeviceOwnerApp(this.getPackageName())){
            if (savedInstanceState == null) {
                getSupportFragmentManager().beginTransaction()
                        .replace(R.id.container, MainFragment.newInstance())
                        .commitNow();
            }
        }else{
            findViewById(R.id.notedpm).setVisibility(View.VISIBLE);
        }

        //仅设备管理员还不够
//        boolean active = mDPM.isAdminActive(mConmp);
//        if(active) {
//            if (savedInstanceState == null) {
//                getSupportFragmentManager().beginTransaction()
//                        .replace(R.id.container, MainFragment.newInstance())
//                        .commitNow();
//            }
//        }else {
//            requireDPM();
//        }
    }

    private void requireDPM(){
        Intent intent = new Intent(
                DevicePolicyManager.ACTION_ADD_DEVICE_ADMIN);
        intent.putExtra(DevicePolicyManager.EXTRA_DEVICE_ADMIN, mConmp);
        intent.putExtra(DevicePolicyManager.EXTRA_ADD_EXPLANATION,
                "只有激活了管理员权限");
        startActivityForResult(intent, 0);
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if(resultCode == RESULT_OK){
            if(requestCode == 0){
                getSupportFragmentManager().beginTransaction()
                        .replace(R.id.container, MainFragment.newInstance())
                        .commitNow();
            }
        }else {
            finish();
        }
    }
}
