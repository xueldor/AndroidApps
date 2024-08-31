package com.xue.peace;

import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;

import android.app.admin.DevicePolicyManager;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.os.Bundle;
import android.os.SystemClock;
import android.util.Log;
import android.view.View;
import android.view.WindowManager;
import android.widget.RadioButton;
import android.widget.RadioGroup;
import android.widget.Toast;

import com.xue.peace.ui.main.MainFragment;
import com.xue.peace.ui.main.SignatureChecker;

public class MainActivity extends AppCompatActivity {

    private RadioGroup mRadioGroup;
    private RadioButton mRadioButtonHide;
    private RadioButton mRadioButtonDisable;
    private RadioButton mRadioButtonSuspend;

    MainFragment mMainFragment;

    private DevicePolicyManager mDPM;
    private ComponentName mConmp;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.main_activity);
        mRadioGroup = findViewById(R.id.radio_group);
        mRadioButtonHide = findViewById(R.id.radio_button_hide);
        mRadioButtonDisable = findViewById(R.id.radio_button_disable);
        mRadioButtonSuspend = findViewById(R.id.radio_button_suspend);

        mMainFragment = MainFragment.newInstance();
        getSupportFragmentManager().beginTransaction()
                .replace(R.id.container, mMainFragment)
                .commitNow();

        mDPM = (DevicePolicyManager) getSystemService(Context.DEVICE_POLICY_SERVICE);
        if (!mDPM.isDeviceOwnerApp(this.getPackageName())) {
            mRadioButtonHide.setEnabled(false);
            Toast.makeText(this, "缺少相应权限因此禁用hide",Toast.LENGTH_SHORT).show();
        }
        mRadioGroup.setOnCheckedChangeListener(new RadioGroup.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(RadioGroup group, int checkedId) {
                if(mRadioButtonHide.isChecked()) {
                    mMainFragment.setMode(MainFragment.HIDE);
                    mConmp = new ComponentName(MainActivity.this, MyPolicyReceiver.class);
                    if(!mDPM.isDeviceOwnerApp(MainActivity.this.getPackageName())){
                        Toast.makeText(MainActivity.this, R.string.dpm_permission_warning, Toast.LENGTH_SHORT).show();
                    }
                }else if (mRadioButtonDisable.isChecked()){
                    mMainFragment.setMode(MainFragment.DISABLE);
                } else if (mRadioButtonSuspend.isChecked()) {
                    mMainFragment.setMode(MainFragment.SUSPEND);
                }
            }
        });


        //仅设备管理员还不够。deviceOwner包含了deviceAdmin, 只能有一个应用是deviceOwner， deviceAdmin可以多个。
        // 我们需要用adb激活deviceOwner，因此下面代码无需执行。
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
