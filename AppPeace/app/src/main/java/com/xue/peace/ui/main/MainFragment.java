package com.xue.peace.ui.main;

import androidx.lifecycle.ViewModelProviders;

import android.app.admin.DevicePolicyManager;
import android.content.ComponentName;
import android.content.Context;
import android.content.pm.ApplicationInfo;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;
import android.graphics.Color;
import android.os.Bundle;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.DefaultItemAnimator;
import androidx.recyclerview.widget.DividerItemDecoration;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.RadioGroup;
import android.widget.TextView;
import android.widget.Toast;

import com.xue.peace.AppInfoBean;
import com.xue.peace.DetailDialog;
import com.xue.peace.MainActivity;
import com.xue.peace.MyPolicyReceiver;
import com.xue.peace.R;
import com.xue.peace.SwipeView;

import java.util.ArrayList;
import java.util.List;

public class MainFragment extends Fragment implements RadioGroup.OnCheckedChangeListener {
    private static final String TAG = "MainFragment";

    private RadioGroup rgSwipCard;

    private MainViewModel mViewModel;
    private RecyclerView mUserApkListView;
    private RecyclerView mSystemApkListView;
    private DetailDialog mDetailDialog;

    DevicePolicyManager dpm;
    ComponentName dpmCompName;

    public final static int HIDE = 1;
    public final static int DISABLE = 2;
    public final static int SUSPEND = 3;

    private int mMode;

    private List<AppInfoBean> userApps = new ArrayList<>();
    private List<AppInfoBean> systemApps = new ArrayList<>();

    public static MainFragment newInstance() {
        return new MainFragment();
    }

    public void setMode(int mode) {
        mMode = mode;
        if (mViewModel != null) {
            mViewModel.setMode(mode);
        }
        RecyclerView.Adapter adapterUser = mUserApkListView.getAdapter();
        if (adapterUser != null) adapterUser.notifyDataSetChanged();
        RecyclerView.Adapter adapterSystem = mSystemApkListView.getAdapter();
        if (adapterSystem != null) adapterSystem.notifyDataSetChanged();
    }

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container,
                             @Nullable Bundle savedInstanceState) {
        return inflater.inflate(R.layout.main_fragment, container, false);
    }

    @Override
    public void onActivityCreated(@Nullable Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);
        rgSwipCard = getView().findViewById(R.id.rg_card);
        rgSwipCard.setOnCheckedChangeListener(this);

        mUserApkListView = getView().findViewById(R.id.user_apk_list);
        mSystemApkListView = getView().findViewById(R.id.system_apk_list);
        //分隔线
        mUserApkListView.addItemDecoration(new DividerItemDecoration(getActivity(), DividerItemDecoration.VERTICAL));
        mSystemApkListView.addItemDecoration(new DividerItemDecoration(getActivity(),DividerItemDecoration.VERTICAL));
        //item动画，删除一项后下面的列表项滑动到被删除的位置
        mSystemApkListView.setItemAnimator(new DefaultItemAnimator());

        LinearLayoutManager userManager = new LinearLayoutManager(getActivity(), LinearLayoutManager.VERTICAL, false);
        mUserApkListView.setLayoutManager(userManager);
        LinearLayoutManager sysManager = new LinearLayoutManager(getActivity(), LinearLayoutManager.VERTICAL, false);
        mSystemApkListView.setLayoutManager(sysManager);

        mViewModel = ViewModelProviders.of(this).get(MainViewModel.class);
        mViewModel.setMode(mMode);
        mViewModel.getAllApps(getActivity().getApplicationContext(),userApps,systemApps);

        AppInfoAdapter userAdapter = new AppInfoAdapter();
        userAdapter.setData(userApps);
        mUserApkListView.setAdapter(userAdapter);

        AppInfoAdapter systemAdapter = new AppInfoAdapter();
        systemAdapter.setData(systemApps);
        mSystemApkListView.setAdapter(systemAdapter);

        dpm = (DevicePolicyManager) getActivity().getSystemService(Context.DEVICE_POLICY_SERVICE);
        dpmCompName = new ComponentName(getActivity(), MyPolicyReceiver.class);

    }

    @Override
    public void onCheckedChanged(RadioGroup group, int checkedId) {
        if(checkedId == R.id.rb_chose_system_app){
            mUserApkListView.setVisibility(View.GONE);
            mSystemApkListView.setVisibility(View.VISIBLE);
        }else {
            mUserApkListView.setVisibility(View.VISIBLE);
            mSystemApkListView.setVisibility(View.GONE);
        }
    }

    class AppInfoHolder extends RecyclerView.ViewHolder implements View.OnClickListener {
        SwipeView mSwipeView;
        AppInfoBean mInfo;
        TextView nameTV;
        TextView packTV;
        ImageView iconIV;
        ImageButton button;

        // 应用是否冻结住。包括hide、disable、suspend等。
        boolean isAppFreeze = false;

        public AppInfoHolder(View itemView) {
            super(itemView);
            mSwipeView = (SwipeView)itemView;
            nameTV = itemView.findViewById(R.id.appName);
            iconIV = itemView.findViewById(R.id.appIcon);
            packTV = itemView.findViewById(R.id.appPackage);
            button = itemView.findViewById(R.id.bt_use_or_disuse);
            mSwipeView.setOnClickListener(this);
            mSwipeView.setOnClickRightView(this);
        }
        public void bind(AppInfoBean bean) {
            mInfo = bean;
            nameTV.setText(bean.getAppName());
            packTV.setText(bean.getAppPackageName());
            iconIV.setImageDrawable(bean.getAppIcon());
            updateDisplay();
        }

        @Override
        public void onClick(View v) {
            if(v.getId() == R.id.bt_use_or_disuse){
                if(mInfo.getAppPackageName().equals(getActivity().getPackageName())){
                    //自身
                    Toast.makeText(getActivity(),"My self,disable this action",Toast.LENGTH_SHORT).show();
                    return;
                }

                if (mMode == HIDE) {
                    //hide此应用
                    if (mViewModel.isDeviceOwner()) {
                        boolean result = dpm.setApplicationHidden(dpmCompName, mInfo.getAppPackageName(), !isAppFreeze);
                        if (result) {
                            updateDisplay();
                        } else {
                            Toast.makeText(getActivity(), "This app can't be hide", Toast.LENGTH_SHORT).show();
                        }
                    }else {
                        Toast.makeText(getActivity(), R.string.dpm_permission_warning, Toast.LENGTH_SHORT).show();
                    }
                } else if (mMode == DISABLE) {
                    //设置disable状态,需要system权限
                    int flag = isAppFreeze? PackageManager.COMPONENT_ENABLED_STATE_ENABLED:PackageManager.COMPONENT_ENABLED_STATE_DISABLED_USER;
                    PackageManager pm = getActivity().getPackageManager();
                    try {
                        pm.setApplicationEnabledSetting(mInfo.getAppPackageName(), flag,0);
                        updateDisplay();
                    }catch (Exception e){
                        e.printStackTrace();
                        Toast.makeText(getActivity(), "没有disable权限", Toast.LENGTH_SHORT).show();
                    }
                } else if (mMode == SUSPEND) {
                    PackageManager pm = getActivity().getPackageManager();
                    try {
                        String[] pkgs = new String[] {mInfo.getAppPackageName()};
                        pm.setPackagesSuspended(pkgs, !isAppFreeze, null,null,null);
                        updateDisplay();
                    }catch (Exception e){
                        e.printStackTrace();
                        Toast.makeText(getActivity(), "没有suspend权限", Toast.LENGTH_SHORT).show();
                    }
                }
                mViewModel.updateAppInfoBean(getActivity(), mInfo);
            }else if(v == mSwipeView){
                if(mDetailDialog == null){
                    mDetailDialog = new DetailDialog(getActivity());
                }
                mDetailDialog.setMvm(mViewModel);
                mDetailDialog.show();
                mDetailDialog.loadDetailData(mInfo);
            }
        }

        void updateDisplay(){
            mViewModel.updateAppInfoBean(getActivity(), mInfo);
            if (mMode == HIDE) {
                if (mInfo.isHide() != null) {
                    if (mInfo.isHide()) {
                        isAppFreeze = true;
                    } else {
                        itemView.setBackgroundColor(Color.TRANSPARENT);
                        button.setBackgroundResource(R.drawable.iv_use);
                        isAppFreeze = false;
                    }
                }
            } else if (mMode == DISABLE) {
                if (mInfo.isDisable()) {
                    isAppFreeze = true;
                } else{
                    isAppFreeze = false;
                }
            } else if (mMode == SUSPEND) {
                if (mInfo.isSuspend()) {
                    isAppFreeze = true;
                } else{
                    isAppFreeze = false;
                }
            }
            if (isAppFreeze) {
                itemView.setBackgroundColor(Color.GRAY);
                button.setBackgroundResource(R.drawable.iv_dis_use);

            } else {
                itemView.setBackgroundColor(Color.TRANSPARENT);
                button.setBackgroundResource(R.drawable.iv_use);
            }
        }
    }

    class AppInfoAdapter extends RecyclerView.Adapter{
        private List<AppInfoBean> mData;

        public void setData(List<AppInfoBean> data) {
            mData = data;
        }

        @NonNull
        @Override
        public RecyclerView.ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
            View item = LayoutInflater.from(getActivity()).inflate(R.layout.app_info_item , null ,false);
            return new AppInfoHolder(item);
        }

        @Override
        public void onBindViewHolder(@NonNull RecyclerView.ViewHolder holder, int position) {
            AppInfoHolder appHolder = (AppInfoHolder)holder;
            appHolder.bind(mData.get(position));
        }

        @Override
        public int getItemCount() {
            return mData == null ? 0 : mData.size();
        }
    }

}
