package com.king.app.fileencryption.guide;

import android.app.Activity;
import android.content.Context;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.TextView;

import com.king.app.fileencryption.Application;
import com.king.app.fileencryption.R;
import com.king.app.fileencryption.publicview.DragSideBar;

public class SideViewManager implements OnClickListener {

	public interface OnSideListener {
		public void onMenuMainView();
		public void onMenuTimeLine();
		public void onMenuSetting();
		public void onMenuClose();
	}
	
	private Context mContext;
	private DragSideBar dragSideBar;
	
	private OnSideListener onSideListener;

	private View bkView;
	private TextView mainViewButton;
	private TextView timeLineButton;
	private TextView settingButton;
	private TextView closeButton;
	
	public SideViewManager(Context context, DragSideBar sideBar) {

		mContext = context;
		dragSideBar = sideBar;
		dragSideBar.setLayoutRes(R.layout.guide_sidebar);

		Activity view = (Activity) context;
		bkView = view.findViewById(R.id.dragsidebar_bk);
		mainViewButton = (TextView) view.findViewById(R.id.guide_menu_mainview);
		timeLineButton = (TextView) view.findViewById(R.id.guide_menu_timeline);
		settingButton = (TextView) view.findViewById(R.id.guide_menu_setting);
		closeButton = (TextView) view.findViewById(R.id.guide_menu_close);
		bkView.setOnClickListener(this);
		mainViewButton.setOnClickListener(this);
		timeLineButton.setOnClickListener(this);
		settingButton.setOnClickListener(this);
		closeButton.setOnClickListener(this);
		
		if (Application.isLollipop()) {
			mainViewButton.setBackgroundResource(R.drawable.ripple_rect_white);
			timeLineButton.setBackgroundResource(R.drawable.ripple_rect_white);
			settingButton.setBackgroundResource(R.drawable.ripple_rect_white);
			closeButton.setBackgroundResource(R.drawable.ripple_rect_white);
		}
			
	}

	public void setOnSideListener(OnSideListener listener) {
		onSideListener = listener;
	}

	@Override
	public void onClick(View v) {
		if (onSideListener != null) {
			if (v == mainViewButton) {
				onSideListener.onMenuMainView();
			}
			else if (v == timeLineButton) {
				onSideListener.onMenuTimeLine();
			}
			else if (v == settingButton) {
				onSideListener.onMenuSetting();
			}
			else if (v == closeButton) {
				onSideListener.onMenuClose();
			}
		}
	}

}
