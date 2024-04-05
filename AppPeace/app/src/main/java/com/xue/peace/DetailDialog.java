package com.xue.peace;

import android.app.Dialog;
import android.content.Context;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.TextView;


public class DetailDialog extends Dialog implements View.OnClickListener {

	private Context mContext;
	private View rootView;

	private TextView mPackageNameTV;
	private TextView mIsHideTV;
	private TextView mIsDisableTV;
	private TextView mUid;

	private boolean canTouchOutside = true;

	public DetailDialog(Context context) {
		this(context,0);
	}

	public DetailDialog(Context context, int themeResId) {
		super(context, themeResId);
		this.mContext = context;
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
        mPackageNameTV = rootView.findViewById(R.id.package_name_dialog);
        mIsHideTV = rootView.findViewById(R.id.is_hide);
        mIsDisableTV = rootView.findViewById(R.id.is_disable);
        mUid = rootView.findViewById(R.id.app_uid);
	}

	public void loadDetailData(AppInfoBean bean){
        mPackageNameTV.setText(bean.getPackageInfo().packageName);
        mIsHideTV.setText(bean.isHide() ? "是":"否");
        mIsDisableTV.setText(bean.isDisable()?"是":"否");
        mUid.setText(String.valueOf(bean.getAppUid()));
    }

	@Override
	public void onClick(View v) {
		switch (v.getId()) {

		}
	}
}
