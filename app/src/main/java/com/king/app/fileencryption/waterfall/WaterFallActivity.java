package com.king.app.fileencryption.waterfall;

import java.io.File;
import java.io.FilenameFilter;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import com.king.app.fileencryption.R;
import com.king.app.fileencryption.controller.PictureManagerUpdate;
import com.king.app.fileencryption.controller.ThemeManager;
import com.king.app.fileencryption.controller.WholeRandomManager;
import com.king.app.fileencryption.data.Constants;
import com.king.app.fileencryption.setting.SettingActivity;
import com.king.app.fileencryption.setting.SettingProperties;
import com.king.app.fileencryption.slidingmenu.SlidingMenuAbstract;
import com.king.app.fileencryption.slidingmenu.SlidingMenuCreator;
import com.king.app.fileencryption.slidingmenu.SlidingMenuLeft;
import com.king.app.fileencryption.slidingmenu.SlidingMenuRight;
import com.king.app.fileencryption.slidingmenu.SlidingMenuTwoWay;
import com.king.app.fileencryption.slidingmenu.SlidingMenuTwoWay.OnSlideChagedListener;
import com.king.app.fileencryption.sorder.controller.SOrderPictureBridge;
import com.king.app.fileencryption.sorder.entity.SOrder;
import com.king.app.fileencryption.thumbfolder.ShowImageDialog;
import com.king.app.fileencryption.tool.SimpleEncrypter;
import com.king.app.fileencryption.util.DisplayHelper;
import com.king.app.fileencryption.waterfall.LazyScrollView.OnPageListener;

import android.app.Activity;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.drawable.BitmapDrawable;
import android.graphics.drawable.Drawable;
import android.os.Bundle;
import android.os.Handler;
import android.util.Log;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.animation.AlphaAnimation;
import android.view.animation.Animation;
import android.widget.LinearLayout;
import android.widget.Toast;

public class WaterFallActivity extends Activity
		implements LazyScrollView.OnItemClickListener, OnClickListener, OnSlideChagedListener
		, OnPageListener, ImageProvider {

	private LazyScrollView waterfallView;
	private ShowImageDialog imageDialog;
	private List<String> imageList;
	private Bitmap slidingBackground;

	private LinearLayout menuLayout;
	private LinearLayout menuLayoutRight;
	private SlidingMenuAbstract slidingMenu;
	private View backgroundView;
	private SlidingMenuCreator slidingMenuCreator;
	private int imageItemSize;
	private boolean isTwoWayMenu;
	private boolean isEndlessMode;
	private WholeRandomManager wholeRandomManager;

	private int curOrientation;
	private int verColumn, horColumn;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		setTheme(new ThemeManager(this).getDefaultTheme());
		super.onCreate(savedInstanceState);
		DisplayHelper.enableFullScreen(this);
		DisplayHelper.disableScreenshot(this);

		curOrientation = getResources().getConfiguration().orientation;

		int leftOrRight = SettingProperties.getSlidingMenuMode(this);
		if (leftOrRight == SettingProperties.SLIDINGMENU_RIGHT) {
			setContentView(R.layout.activity_waterfall_menu_right);
			slidingMenu = (SlidingMenuAbstract) findViewById(R.id.waterfall_slidingmenu);
			backgroundView = slidingMenu;
		}
		else if (leftOrRight == SettingProperties.SLIDINGMENU_LEFT) {
			setContentView(R.layout.activity_waterfall);
			slidingMenu = (SlidingMenuAbstract) findViewById(R.id.waterfall_slidingmenu);
			backgroundView = slidingMenu;
		}
		else if (leftOrRight == SettingProperties.SLIDINGMENU_TWOWAY) {
			setContentView(R.layout.activity_waterfall_menu_twoway);
			slidingMenu = (SlidingMenuAbstract) findViewById(R.id.waterfall_slidingmenu);
			backgroundView = findViewById(R.id.waterfall_bk_layout);
			isTwoWayMenu = true;
		}
		waterfallView = (LazyScrollView) findViewById(R.id.waterfallview);
		menuLayout = (LinearLayout) findViewById(R.id.waterfall_menu_layout);
		if (isTwoWayMenu) {
			menuLayoutRight = (LinearLayout) findViewById(R.id.waterfall_menu_layout_right);
			((SlidingMenuTwoWay) slidingMenu).addOnSlideChangedListener(this);
		}


		verColumn = SettingProperties.getWaterfallColNum(this);
		horColumn = SettingProperties.getWaterfallHorColNum(this);

		loadMenu(leftOrRight);

		if (getIntent().getBooleanExtra("isEndless", false)) {
			startEndlessWaterfall();
			return;
		}

		loadImageList();

		int col = curOrientation == android.content.res.Configuration.ORIENTATION_PORTRAIT
				? verColumn:horColumn;
		waterfallView.prepare(imageList.size(), Constants.WATERFALL_NUM_PER_LOAD, col);
		waterfallView.setImagePathList(imageList);
		createImageItemSize();
		waterfallView.setImageProvider(this);

		waterfallView.setOnItemClickListener(this);
		waterfallView.setup();
	}

	@Override
	public Bitmap loadImage(String filePath) {

		return PictureManagerUpdate.getInstance().createImage(filePath
				, imageItemSize*imageItemSize, WaterFallActivity.this, 1);
	}

	private void createImageItemSize() {
		int column = SettingProperties.getWaterfallColNum(WaterFallActivity.this);
		if (column  == 2) {
			imageItemSize = getResources().getInteger(R.integer.waterfall_column2_image_size);
		}
		else {//column = 3
			imageItemSize = getResources().getInteger(R.integer.waterfall_column3_image_size);
		}
	}

	private void loadMenu(int leftOrRight) {

		if (leftOrRight == SettingProperties.SLIDINGMENU_TWOWAY) {
			slidingMenuCreator = new SlidingMenuCreator(this, this);
			slidingMenuCreator.loadMenu(Constants.WaterfallMenu, menuLayout, SettingProperties.SLIDINGMENU_LEFT);
			slidingMenuCreator.loadMenu(Constants.WaterfallMenu, menuLayoutRight, SettingProperties.SLIDINGMENU_RIGHT);
			if (getResources().getConfiguration().orientation == android.content.res.Configuration.ORIENTATION_PORTRAIT) {
				slidingBackground = slidingMenuCreator.loadBackground(SlidingMenuLeft.BK_KEY);
			}
			else {
				slidingBackground = slidingMenuCreator.loadBackground(SlidingMenuLeft.BK_KEY + "_landscape");
			}
			updateBackground(slidingBackground);
		}
		else {
			slidingMenuCreator = new SlidingMenuCreator(this, this);
			slidingMenuCreator.loadMenu(Constants.WaterfallMenu, menuLayout, leftOrRight);
			if (getResources().getConfiguration().orientation == android.content.res.Configuration.ORIENTATION_PORTRAIT) {
				slidingBackground = slidingMenuCreator.loadBackground(SlidingMenuAbstract.MENU_BK_KEY);
			}
			else {
				slidingBackground = slidingMenuCreator.loadBackground(SlidingMenuAbstract.MENU_BK_KEY + "_landscape");
			}
			updateBackground(slidingBackground);
		}

	}

	private void loadImageList() {
		Intent intent = getIntent();
		String filePath = intent.getStringExtra("filePath");
		if (filePath != null) {
			loadFromFile(filePath);
		}
		else {
			int orderId = intent.getIntExtra("order", -1);
			if (orderId != -1) {
				SOrderPictureBridge bridge = SOrderPictureBridge.getInstance(this);
				SOrder order = bridge.queryOrder(orderId);
				bridge.getOrderItemList(order);
				imageList = order.getImgPathList();
			}
		}
		if (imageList != null && imageList.size() > 0) {
			Collections.shuffle(imageList);
		}
	}

	private void loadFromFile(String filePath) {
		imageList = new ArrayList<String>();
		File[] files = new File(filePath).listFiles(new FilenameFilter() {

			@Override
			public boolean accept(File dir, String filename) {
				return filename.endsWith(SimpleEncrypter.FILE_EXTRA);
			}
		});
		for (File f:files) {
			imageList.add(f.getPath());
		}
	}

	@Override
	public void onItemClick(View view, int position) {
		showImage(position);
	}

	private void showImage(int position) {
		if (imageDialog == null) {
			imageDialog = new ShowImageDialog(this, null, 0);
		}
		imageDialog.setImagePath(imageList.get(position));
		imageDialog.fitImageView();
		imageDialog.show();
	}

	@Override
	public void onConfigurationChanged(android.content.res.Configuration newConfig) {

		if (imageDialog != null) {
			imageDialog.setOrientationChanged();
			if (imageDialog.isShowing()) {
				imageDialog.onConfigChange();
			}
		}
		if (SettingProperties.getSlidingMenuMode(this) == SettingProperties.SLIDINGMENU_LEFT) {
			slidingMenuCreator.onConfigurationChanged(menuLayout);
			if (newConfig.orientation == android.content.res.Configuration.ORIENTATION_PORTRAIT) {
				updateBackground(slidingMenuCreator.loadBackground(SlidingMenuLeft.BK_KEY));
			}
			else {
				updateBackground(slidingMenuCreator.loadBackground(SlidingMenuLeft.BK_KEY + "_landscape"));
			}
		}
		else if (SettingProperties.getSlidingMenuMode(this) == SettingProperties.SLIDINGMENU_RIGHT) {
			slidingMenuCreator.onConfigurationChanged(menuLayout);
			if (newConfig.orientation == android.content.res.Configuration.ORIENTATION_PORTRAIT) {
				updateBackground(slidingMenuCreator.loadBackground(SlidingMenuRight.BK_KEY));
			}
			else {
				updateBackground(slidingMenuCreator.loadBackground(SlidingMenuRight.BK_KEY + "_landscape"));
			}
		}
		else if (SettingProperties.getSlidingMenuMode(this) == SettingProperties.SLIDINGMENU_TWOWAY) {
			slidingMenuCreator.onConfigurationChanged(menuLayout);
			slidingMenuCreator.onConfigurationChanged(menuLayoutRight);
			if (newConfig.orientation == android.content.res.Configuration.ORIENTATION_PORTRAIT) {
				updateBackground(slidingMenuCreator.loadBackground(SlidingMenuLeft.BK_KEY));
			}
			else {
				updateBackground(slidingMenuCreator.loadBackground(SlidingMenuLeft.BK_KEY + "_landscape"));
			}
		}

		if (newConfig.orientation != curOrientation) {

			curOrientation = newConfig.orientation;
			//updateColumn的方法有很多问题，很难处理，还是使用reset lazyscrollview的方法
			refreshWaterfall();
			/*
			if (isEndlessMode) {
				//updateColumn的方法有很多问题，很难处理，还是使用reset lazyscrollview的方法
				refreshEndlessWaterfall();
			}
			else {
				waterfallView.updateColumn();
			}
			*/

		}
		super.onConfigurationChanged(newConfig);
	}

	@Override
	public void onClick(View view) {
		switch (view.getId()) {
			case R.string.menu_view_all:
				slidingMenu.closeMenu();
				startEndlessWaterfall();
				break;
			case R.string.menu_exit:
				finish();
				break;
			case R.string.action_settings:
				startActivityForResult(new Intent().setClass(this, SettingActivity.class), 0);
				break;
			default:
				break;
		}
	}

	/**
	 * 用在发生转屏的时候
	 */
	private void refreshWaterfall() {
		waterfallView.reset();

		int col = curOrientation == android.content.res.Configuration.ORIENTATION_PORTRAIT
				? verColumn:horColumn;
		waterfallView.prepare(imageList.size(), Constants.WATERFALL_NUM_PER_LOAD, col);
		waterfallView.setImagePathList(imageList);
		createImageItemSize();
		waterfallView.setImageProvider(this);

		waterfallView.setOnItemClickListener(this);
		waterfallView.addOnPageListener(this);
		waterfallView.setEndlessMode(isEndlessMode);
		waterfallView.setup();
	}

	private void startEndlessWaterfall() {
		isEndlessMode = true;
		if (imageList == null) {
			imageList = new ArrayList<String>();
		}
		else {
			imageList.clear();
			waterfallView.reset();
		}
		int col = curOrientation == android.content.res.Configuration.ORIENTATION_PORTRAIT
				? verColumn:horColumn;
		waterfallView.prepare(0, Constants.WATERFALL_NUM_PER_LOAD, col);
		waterfallView.setImagePathList(imageList);
		createImageItemSize();
		waterfallView.setImageProvider(this);

		waterfallView.setOnItemClickListener(this);
		waterfallView.addOnPageListener(this);
		waterfallView.setEndlessMode(true);
		waterfallView.setup();
	}

	@Override
	protected void onActivityResult(int requestCode, int resultCode, Intent data) {
		//after start setting, it's possible that modify sliding menu position
		//so reload in case that sliding menu position preset changed
		//waterfall column also may changed
		reload();
		super.onActivityResult(requestCode, resultCode, data);
	}

	private void reload() {

		Intent intent = getIntent();
		if (isEndlessMode) {
			intent.putExtra("isEndless", true);
		}
		overridePendingTransition(0, 0);
		intent.addFlags(Intent.FLAG_ACTIVITY_NO_ANIMATION);
		finish();
		overridePendingTransition(0, 0);
		startActivity(intent);
	}

	@Override
	public void onSlideChange(final int direction) {

		Log.d("WaterFall", "onSlideChange");
		new Handler().postDelayed(new Runnable() {

			@Override
			public void run() {
				Log.d("WaterFall", "run");
				Bitmap newBackBitmap = null;
				if (direction == OnSlideChagedListener.LEFT) {
					if (getResources().getConfiguration().orientation == android.content.res.Configuration.ORIENTATION_PORTRAIT) {
						newBackBitmap = slidingMenuCreator.loadBackground(SlidingMenuLeft.BK_KEY);
					}
					else {
						newBackBitmap = slidingMenuCreator.loadBackground(SlidingMenuLeft.BK_KEY + "_landscape");
					}
				}
				else {
					if (getResources().getConfiguration().orientation == android.content.res.Configuration.ORIENTATION_PORTRAIT) {
						newBackBitmap = slidingMenuCreator.loadBackground(SlidingMenuRight.BK_KEY);
					}
					else {
						newBackBitmap = slidingMenuCreator.loadBackground(SlidingMenuRight.BK_KEY + "_landscape");
					}
				}


				updateBackground(newBackBitmap);
				if (slidingBackground != null) {
					slidingBackground.recycle();
				}
				slidingBackground = newBackBitmap;
			}
		}, 0);
	}

	public void updateBackground(Bitmap bitmap) {
		if (bitmap == null) {
			backgroundView.setBackgroundResource(Constants.SLIDING_MENU_DEFAULT_BK);
		}
		else {
			Drawable drawable = new BitmapDrawable(getResources(), bitmap);
			backgroundView.setBackground(drawable);
		}
		Animation animation = new AlphaAnimation(0.5f, 1);
		animation.setDuration(1000);
		backgroundView.setAnimation(animation);
	}

	@Override
	public int onNextPage(List<String> imagePathList) {

		if (wholeRandomManager == null) {
			wholeRandomManager = new WholeRandomManager(new SimpleEncrypter());
		}
		String path = null;
		int realCount = 0;
		for (int i = 0; i < Constants.WATERFALL_NUM_PER_LOAD; i ++) {
			path = wholeRandomManager.getRandomPath();
			if (path != null) {
				realCount ++;
				imagePathList.add(path);
			}
		}
		Toast.makeText(this, realCount + " image added.", Toast.LENGTH_SHORT).show();
		return realCount;
	}

}
