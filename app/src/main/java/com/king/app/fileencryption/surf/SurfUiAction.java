package com.king.app.fileencryption.surf;

import java.util.List;

import android.content.Intent;
import android.widget.RelativeLayout;

/**
 * @author JingYang
 * @version create time：2016-1-26 下午5:49:52
 *
 */
public interface SurfUiAction {

	/**
	 * inflate tool bar into container
	 * define too bar action
	 * @param container
	 */
	public void loadToolbar(RelativeLayout container);
	/**
	 * load images to show
	 * @param intent
	 * @return
	 */
	public void loadImages(Intent intent);
	/**
	 * get image list
	 * @return
	 */
	public List<String> getImageList();
	/**
	 * page switched by switch ViewPager or click item of HorizontalGallery
	 * @param position
	 */
	public void onSwitchPage(int position);
	/**
	 * stop playing list when activity status is onPause/onStop/onDestroy
	 * @return
	 */
	public boolean stopAutoPlay();
}
