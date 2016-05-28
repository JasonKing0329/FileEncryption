package com.king.app.fileencryption.guide;

import android.widget.ImageView;

public abstract class AutoSwitchAdapter {

	private AutoSwitchView autoSwitchView;
	
	public abstract int getCount();
	
	public abstract void loadNextImage(ImageView view);

	public abstract void recycleAll();
	
	public void setAutoSwitchView(AutoSwitchView view) {
		autoSwitchView = view;
	}
	
	public void notifyDataSetChanged() {
		autoSwitchView.restart();
	}

}
