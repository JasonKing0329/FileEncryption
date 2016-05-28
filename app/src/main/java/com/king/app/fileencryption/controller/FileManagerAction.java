package com.king.app.fileencryption.controller;

import com.king.app.fileencryption.TabActionBar;
import com.king.app.fileencryption.slidingmenu.SlidingMenuCreator;

import android.content.res.Configuration;
import android.graphics.Bitmap;
import android.view.Menu;
import android.view.MenuItem;
import android.widget.LinearLayout;

public interface FileManagerAction {
	public boolean onOptionsItemSelected(MenuItem item);
	public boolean onPrepareOptionsMenu(Menu menu);
	public void onBackPressed();
	public void saveState();
	public void reInit();
	public void changeBackground(Bitmap bitmap);
	/**
	 * onRestart 过程验证身份
	 * @param needUnlock
	 */
	public void onConfigurationChanged(Configuration newConfig);
	public void onResume();
	public void onCreateOptionsMenu(Menu menu);
	public void setActionBar(TabActionBar actionBar);
	public SlidingMenuCreator loadMenu(LinearLayout menuLayout);
	public SlidingMenuCreator loadTwoWayMenu(LinearLayout menuLayout, LinearLayout menuLayoutRight);
}
