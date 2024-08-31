package com.xue.peace;

import android.app.Dialog;
import android.content.Context;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;

import com.xue.peace.ui.main.MainViewModel;


public class DetailDialog extends Dialog implements View.OnClickListener {

	private Context mContext;
	private View rootView;

	MainViewModel mvm;
	AppInfoBean mAppInfoBean;


	private TextView mAppNameTV;
	private TextView mPackageNameTV;
	private TextView mIsHideTV;
	private TextView mIsDisableTV;
	private TextView mIsSuspendTV;
	private TextView mUid;
	private Button mRefresh;

	private boolean canTouchOutside = true;

	public DetailDialog(Context context) {
		this(context,0);
	}

	public DetailDialog(Context context, int themeResId) {
		super(context, themeResId);
		this.mContext = context;
	}

	public void setMvm(MainViewModel mvm) {
		this.mvm = mvm;
	}

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
        rootView = LayoutInflater.from(mContext).inflate(R.layout.detail_dialog,null,false);
		setContentView(rootView);
		setCanceledOnTouchOutside(false);
		initView();
	}

	private void initView() {
		if (canTouchOutside) {
            setCanceledOnTouchOutside(true);
        }
		mAppNameTV = rootView.findViewById(R.id.app_name_dialog);
        mPackageNameTV = rootView.findViewById(R.id.package_name_dialog);
        mIsHideTV = rootView.findViewById(R.id.is_hide);
        mIsDisableTV = rootView.findViewById(R.id.is_disable);
		mIsSuspendTV = rootView.findViewById(R.id.is_suspend);
        mUid = rootView.findViewById(R.id.app_uid);
		mRefresh = rootView.findViewById(R.id.bt_refresh);
		mRefresh.setOnClickListener(this);
	}

	public void loadDetailData(AppInfoBean bean){
		mAppInfoBean = bean;
		mAppNameTV.setText(bean.getAppName());
        mPackageNameTV.setText(bean.getPackageInfo().packageName);
		Boolean hide = bean.isHide();
        mIsHideTV.setText(hide==null?"- (-表示当前应用没有权限)":hide? "是":"否");
        mIsDisableTV.setText(bean.isDisable()?"是":"否");
		mIsSuspendTV.setText(bean.isSuspend()?"是":"否");
        mUid.setText(String.valueOf(bean.getAppUid()));
    }

	@Override
	public void onClick(View v) {
		switch (v.getId()) {
			case R.id.bt_refresh:
				mvm.updateAppInfoBean(mContext, mAppInfoBean);
				loadDetailData(mAppInfoBean);
				break;
		}
	}
}
