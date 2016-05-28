package com.king.app.fileencryption.guide;

import android.widget.ImageView;

public abstract class AutoScrollAdapter {

	private AutoScrollView autoScrollView;
	
	public void setAutoScrollView(AutoScrollView view) {
		autoScrollView = view;
	}
	
	public void notifyDataSetChanged() {
		autoScrollView.notifyDataSetChanged();
	}

	public abstract void loadNextImage(ImageView imageView);
}
