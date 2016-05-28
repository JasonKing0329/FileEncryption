package com.king.app.fileencryption.publicview;

import java.io.File;
import java.io.FilenameFilter;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Random;

import com.king.app.fileencryption.Application;
import com.king.app.fileencryption.R;
import com.king.app.fileencryption.controller.AccessController;
import com.king.app.fileencryption.controller.PictureManagerUpdate;
import com.king.app.fileencryption.controller.ThemeManager;
import com.king.app.fileencryption.controller.WholeRandomManager;
import com.king.app.fileencryption.open.gifview.MyGifManager;
import com.king.app.fileencryption.setting.SettingProperties;
import com.king.app.fileencryption.slidingmenu.SlidingMenuLeft;
import com.king.app.fileencryption.slidingmenu.SlidingMenuRight;
import com.king.app.fileencryption.sorder.controller.SOrderPictureBridge;
import com.king.app.fileencryption.sorder.entity.SOrder;
import com.king.app.fileencryption.sorder.view.SOrderChooserUpdate;
import com.king.app.fileencryption.spicture.controller.AutoPlayController;
import com.king.app.fileencryption.spicture.controller.SpictureController;
import com.king.app.fileencryption.tool.Encrypter;
import com.king.app.fileencryption.tool.SimpleEncrypter;
import com.king.app.fileencryption.util.DisplayHelper;
import com.king.app.fileencryption.util.ScreenUtils;

import android.app.Activity;
import android.app.AlertDialog;
import android.content.DialogInterface;
import android.content.res.Configuration;
import android.graphics.Bitmap;
import android.os.Bundle;
import android.os.Handler.Callback;
import android.os.Message;
import android.util.Log;
import android.view.GestureDetector;
import android.view.GestureDetector.SimpleOnGestureListener;
import android.view.MenuItem;
import android.view.View.OnClickListener;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;
import android.view.animation.Animation.AnimationListener;
import android.view.MotionEvent;
import android.view.View;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ListPopupWindow;
import android.widget.PopupMenu;
import android.widget.PopupMenu.OnMenuItemClickListener;
import android.widget.Toast;

public class FullScreenSurfActivity extends Activity implements OnClickListener
		, OnMenuItemClickListener, Callback {

	private final String TAG = "FullScreenSurfActivity";

	public static final int SRC_MODE_FOLDER = 0;
	public static final int SRC_MODE_ORDER = 1;
	public static final int SRC_MODE_RANDOM = 2;
	private int srcMode;

	private final int FLING_MODE_SEQUENCE = 0;
	private final int FLING_MODE_RANDOM_REPEAT = 1;
	private int flingMode;

	private final int FLING_SWITCH_HORIZONTAL_MIN = 100;
	private final int FLING_SWITCH_VERTICAL_MAX = 100;

	private final int FLING_TOOLBAR_VERTICAL_MIN = 100;
	private final int FLING_TOOLBAR_HORIZONTAL_MAX = 100;

	private LinearLayout accessPrivateLayout;
	private EditText accessPrivateEdit;
	private Button accessPrivateButton;

	private List<String> imagePathList;
	private ImageView imageView1, imageView2;
	private View hideView, forgroundView;
	private ImageView previousView, nextView;
	private LinearLayout gifLayout;
	private MyGifManager gifManager;

	private int currentPosition;
	private String currentImagePath;
	private LinearLayout toolbar;
	private ImageView deleteButton, favorateButton, playButton, moreButton, detailButton;

	private Encrypter encrypter;
	private String currentFolder;
	private SOrder currentOrder;
	private GestureDetector gestureDetector;
	private MyGestureDetector myGestureDetector;
	private SpictureController controller;
	private Random random;

	private List<String> tempRandomList;
	private List<String> tempRandomHistoryList;

	private WholeRandomManager randomManager;
	//for SRC_MODE_RANDOM
	private String lastImagePath, nextImagePath;
	private PopupMenu popupMenu;

	private AutoPlayController autoPlayController;

	private HorizontalGallery horizontalGallery;
	private HorizontalAdapter horizontalAdapter;
	private ImageView lastSelectView;

	private Bitmap forgroundBitmap, cacheBitmap;//cacheBitmap is used in animation period, will be recycled after animation end

	private AnimRecycleListener animRecycleListener;
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		setTheme(new ThemeManager(this).getDefaultTheme());
		super.onCreate(savedInstanceState);
		DisplayHelper.enableFullScreen(this);
		DisplayHelper.disableScreenshot(this);
		DisplayHelper.keepScreenOn(this);

		if (Application.isLollipop()) {
			setContentView(R.layout.fullscreen_surf_l);
		}
		else {
			setContentView(R.layout.fullscreen_surf);
		}
		imageView1 = (ImageView) findViewById(R.id.fullscreen_image1);
		imageView2 = (ImageView) findViewById(R.id.fullscreen_image2);
		gifLayout = (LinearLayout) findViewById(R.id.fullscreen_gifview);
		previousView = (ImageView) findViewById(R.id.fullscreen_previous);
		nextView = (ImageView) findViewById(R.id.fullscreen_next);
		toolbar = (LinearLayout) findViewById(R.id.fullscreen_toolbar);
		deleteButton = (ImageView) findViewById(R.id.fullscreen_toolbar_delete);
		favorateButton = (ImageView) findViewById(R.id.fullscreen_toolbar_addtoorder);
		playButton = (ImageView) findViewById(R.id.fullscreen_toolbar_play);
		moreButton = (ImageView) findViewById(R.id.fullscreen_toolbar_more);
		detailButton = (ImageView) findViewById(R.id.fullscreen_toolbar_detail);
		horizontalGallery = (HorizontalGallery) findViewById(R.id.horizontalGallery);
		horizontalGallery.setItemWidth(getResources().getDimensionPixelSize(R.dimen.spicture_chooser_item_width));
		deleteButton.setOnClickListener(toolbarListener);
		favorateButton.setOnClickListener(toolbarListener);
		playButton.setOnClickListener(toolbarListener);
		moreButton.setOnClickListener(toolbarListener);
		detailButton.setOnClickListener(toolbarListener);
		previousView.setOnClickListener(this);
		nextView.setOnClickListener(this);

		random = new Random();
		encrypter = new SimpleEncrypter();
		controller = new SpictureController(this);
		myGestureDetector = new MyGestureDetector();
		gestureDetector = new GestureDetector(this, myGestureDetector);
		gifManager = new MyGifManager(this, gifLayout);

		flingMode = FLING_MODE_SEQUENCE;
		animRecycleListener = new AnimRecycleListener();

		Bundle bundle = getIntent().getExtras();
		srcMode =  bundle.getInt("src_mode");
		if (srcMode == SRC_MODE_FOLDER) {
			currentFolder = bundle.getString("path");
			initDataFromFolder();
			initHorizontalGallery();
		}
		else if (srcMode == SRC_MODE_ORDER) {
			int orderId = bundle.getInt("orderId");
			initDataFromOlder(orderId);
			initHorizontalGallery();
		}
		else if (srcMode == SRC_MODE_RANDOM) {
			hideView = imageView1;
			randomManager = new WholeRandomManager(encrypter);
			executeWholeRandom();
		}

		if (imagePathList != null && imagePathList.size() > 0) {
			gifManager.setParentSize(ScreenUtils.getScreenWidth(this), ScreenUtils.getScreenHeight(this));
			if (gifManager.showGifView(imagePathList.get(0), null, MyGifManager.MATCH_PARENT)) {
				forgroundView = gifLayout;
				hideView = imageView2;
				return;
			}
			forgroundBitmap = PictureManagerUpdate.getInstance().createHDBitmap(imagePathList.get(0));
			imageView2.setImageBitmap(forgroundBitmap);
			forgroundView = imageView2;
			hideView = imageView1;
		}
	}

	private void initHorizontalGallery() {
		//horizontalGallery.setVisibility(View.VISIBLE);
		horizontalAdapter = new HorizontalAdapter(this, imagePathList);
		horizontalGallery.setAdapter(horizontalAdapter);
		horizontalGallery.setOnItemSelectListener(galleryListener);
	}

	private void executeWholeRandom() {
		createRandomPath();
		changeImage();
	}

	private void createRandomPath() {
		int maxTry = 1;
		currentImagePath = randomManager.getRandomPath();
		while (currentImagePath == null && maxTry < 5) {
			currentImagePath = randomManager.getRandomPath();
			maxTry ++;
		}
	}

	OnClickListener toolbarListener = new OnClickListener() {

		@Override
		public void onClick(View view) {
			if (view == deleteButton) {
				if (srcMode == SRC_MODE_ORDER) {
					deleteItemFromOrder();
				}
			}
			else if (view == favorateButton) {
				openOrderChooserToAddItem();
			}
			else if (view == moreButton) {
				//showListPopup();
				showMenu();
			}
			else if (view == detailButton) {
				viewDetails();
			}
			else if (view == playButton) {
				autoPlay();
			}
		}
	};

	@Override
	public boolean onMenuItemClick(MenuItem item) {
		switch (item.getItemId()) {
			case R.id.menu_fullscreen_setascover:
				openOrderChooserToSetCover();
				break;
			case R.id.menu_fullscreen_swipemode:
				new AlertDialog.Builder(FullScreenSurfActivity.this)
						.setItems(getResources().getStringArray(R.array.fullscreen_setting)
								, new DialogInterface.OnClickListener() {

									@Override
									public void onClick(DialogInterface arg0, int index) {
										flingMode = index;
									}
								}).show();
				break;

			case R.id.menu_slidingmenu_left:
				SettingProperties.savePreference(this, SlidingMenuLeft.BK_KEY, currentImagePath);
				break;
			case R.id.menu_slidingmenu_right:
				SettingProperties.savePreference(this, SlidingMenuRight.BK_KEY, currentImagePath);
				break;
			case R.id.menu_slidingmenu_left_land:
				SettingProperties.savePreference(this, SlidingMenuLeft.BK_KEY + "_landscape", currentImagePath);
				break;
			case R.id.menu_slidingmenu_right_land:
				SettingProperties.savePreference(this, SlidingMenuRight.BK_KEY + "_landscape", currentImagePath);
				break;
			default:
				break;
		}
		return true;
	}

	protected void autoPlay() {

		if (autoPlayController == null) {
			autoPlayController = new AutoPlayController(this, this);
		}
		if (stopAutoPlay()) {
			return;
		}

		if (srcMode == SRC_MODE_RANDOM) {
			playButton.setImageResource(R.drawable.actionbar_stop);
			autoPlayController.startWholeRandomAutoPlay(SettingProperties.getAnimationSpeed(this));
		}
		else {
			autoPlayController.setFileNameList(imagePathList);
			if (autoPlayController.canPlay()) {
				playButton.setImageResource(R.drawable.actionbar_stop);
				autoPlayController.startAutoPlay(SettingProperties.getAnimationSpeed(this));
			}
			else {
				String msg = getResources().getString(R.string.spicture_autoplay_tooless);
				msg = String.format(msg, SettingProperties.getMinNumberToPlay(this));
				Toast.makeText(this, msg, Toast.LENGTH_LONG).show();
			}
		}
	}

	protected void showMenu() {
		if (popupMenu == null) {
			popupMenu = new PopupMenu(this, moreButton);
			popupMenu.getMenuInflater().inflate(R.menu.fullscreen, popupMenu.getMenu());
			popupMenu.setOnMenuItemClickListener(this);
		}
		popupMenu.show();
	}

	@Deprecated
	protected void showListPopup() {

		ListPopupWindow window = new ListPopupWindow(this);
		window.setAnchorView(moreButton);
		ArrayAdapter<String> adapter = new ArrayAdapter<String>(this
				, android.R.layout.simple_dropdown_item_1line, getResources().getStringArray(R.array.fullscreen_more));
		window.setAdapter(adapter);
		window.setWidth(getResources().getDimensionPixelSize(R.dimen.fullscreen_toolbar_more_popup_width));
		window.setOnItemClickListener(new OnItemClickListener() {

			@Override
			public void onItemClick(AdapterView<?> arg0, View arg1, int position,
									long arg3) {
				if (position == 0) {//set as cover
					openOrderChooserToSetCover();
				}
			}
		});
		window.show();
	}

	private void openOrderChooserToSetCover() {
		SOrderChooserUpdate chooser = new SOrderChooserUpdate(this, new CustomDialog.OnCustomDialogActionListener() {

			@Override
			public boolean onSave(Object object) {
				if (object != null) {
					SOrder order = (SOrder) object;
					String msg = null;
					String itemPath = currentImagePath;
					if (itemPath != null) {
						order.setCoverPath(itemPath);
						if (controller.setOrderCover(order)) {
							msg = getResources().getString(R.string.spicture_myorders_set_cover_ok);
						}
						else {
							msg = getResources().getString(R.string.spicture_myorders_set_cover_fail);
						}
						if (order.getName() != null) {
							msg = msg.replace("%s", order.getName());
						}
					}
					else {
						msg = getResources().getString(R.string.login_pwd_error);
					}
					Toast.makeText(FullScreenSurfActivity.this, msg, Toast.LENGTH_LONG).show();
				}
				return true;
			}

			@Override
			public void onLoadData(HashMap<String, Object> data) {

			}

			@Override
			public boolean onCancel() {
				return false;
			}
		});
		chooser.setTitle(getResources().getString(R.string.set_as_cover));
		chooser.show();
		
		/*
		SOrderChooser chooser = new SOrderChooser(this, new OnOrderChooseListener() {
			
			@Override
			public void chooseOrder(SOrder order) {
				String msg = null;
				String itemPath = currentImagePath;
				if (itemPath != null) {
					order.setCoverPath(itemPath);
					if (controller.setOrderCover(order)) {
						msg = getResources().getString(R.string.spicture_myorders_set_cover_ok);
					}
					else {
						msg = getResources().getString(R.string.spicture_myorders_set_cover_fail);
					}
					if (order.getName() != null) {
						msg = msg.replace("%s", order.getName());
					}
				}
				else {
					msg = getResources().getString(R.string.login_pwd_error);
				}
				Toast.makeText(FullScreenSurfActivity.this, msg, Toast.LENGTH_LONG).show();
			}
		});
		chooser.setTitleCustom(getResources().getString(R.string.set_as_cover));
		chooser.show();
		*/
	}

	private void deleteItemFromOrder() {
		String msg = null;
		controller.setCurrentOrder(currentOrder);
		if (controller.deleteItemFromOrder(currentPosition)) {
			msg = getResources().getString(R.string.spicture_myorders_delete_item_ok);
			imagePathList.remove(currentPosition);
			flingToNext();
		}
		else {
			msg = getResources().getString(R.string.sorder_delete_fail);
		}

		msg = msg.replace("%s", controller.getCurrentOrder().getName());
		Toast.makeText(this, msg, Toast.LENGTH_LONG).show();
	}

	private void viewDetails() {
		String filePath = currentImagePath;
		if (filePath != null) {
			File file = new File(filePath);
			if (file.exists()) {
				new DefaultDialogManager().openDetailDialog(this, file);
			}
		}
	}

	private void openOrderChooserToAddItem() {
		SOrderChooserUpdate chooser = new SOrderChooserUpdate(this, new CustomDialog.OnCustomDialogActionListener() {

			@Override
			public boolean onSave(Object object) {
				if (object != null) {
					final SOrder order = (SOrder) object;
					final String itemPath = currentImagePath;
					if (itemPath != null) {
						if (controller.isItemExist(itemPath, order.getId())) {
							String title = getResources().getString(R.string.spicture_myorders_item_exist);
							title = String.format(title, order.getName());
							new AlertDialog.Builder(FullScreenSurfActivity.this)
									.setMessage(title)
									.setPositiveButton(R.string.ok, new DialogInterface.OnClickListener() {

										@Override
										public void onClick(DialogInterface dialog, int which) {
											addToOrder(itemPath, order);
										}
									})
									.setNegativeButton(R.string.cancel, null)
									.show();
						}
						else {
							addToOrder(itemPath, order);
						}
					}
					else {
						Toast.makeText(FullScreenSurfActivity.this, R.string.login_pwd_error, Toast.LENGTH_LONG).show();
					}
				}
				return true;
			}

			@Override
			public void onLoadData(HashMap<String, Object> data) {

			}

			@Override
			public boolean onCancel() {
				return false;
			}
		});
		chooser.setTitle(getResources().getString(R.string.add_to_order));
		chooser.show();
		/*
		SOrderChooser chooser = new SOrderChooser(this, new OnOrderChooseListener() {
			
			@Override
			public void chooseOrder(final SOrder order) {
				final String itemPath = currentImagePath;
				if (itemPath != null) {
					if (controller.isItemExist(itemPath, order.getId())) {
						String title = getResources().getString(R.string.spicture_myorders_item_exist);
						title = String.format(title, order.getName());
						new AlertDialog.Builder(FullScreenSurfActivity.this)
							.setMessage(title)
							.setPositiveButton(R.string.ok, new DialogInterface.OnClickListener() {
								
								@Override
								public void onClick(DialogInterface dialog, int which) {
									addToOrder(itemPath, order);
								}
							})
							.setNegativeButton(R.string.cancel, null)
							.show();
					}
					else {
						addToOrder(itemPath, order);
					}
				}
				else {
					Toast.makeText(FullScreenSurfActivity.this, R.string.login_pwd_error, Toast.LENGTH_LONG).show();
				}
			}
		});
		chooser.setTitleCustom(getResources().getString(R.string.add_to_order));
		chooser.show();
		*/
	}

	private void addToOrder(String path, SOrder order) {
		String msg = null;
		if (controller.addItemToOrder(path, order)) {
			msg = getResources().getString(R.string.spicture_myorders_add_ok);
		}
		else {
			msg = getResources().getString(R.string.spicture_myorders_add_fail);
		}
		if (order.getName() != null) {
			msg = msg.replace("%s", order.getName());
		}
		Toast.makeText(this, msg, Toast.LENGTH_LONG).show();
	}

	private void initDataFromOlder(int orderId) {
		SOrderPictureBridge bridge = SOrderPictureBridge.getInstance(this);
		currentOrder = bridge.queryOrder(orderId);
		bridge.getOrderItemList(currentOrder);
		imagePathList = currentOrder.getImgPathList();
		if (imagePathList != null && imagePathList.size() > 0) {
			currentImagePath = imagePathList.get(0);
		}
	}

	private void initDataFromFolder() {
		File file = new File(currentFolder);
		if (file.exists()) {
			if (imagePathList == null) {
				imagePathList = new ArrayList<String>();
			}
			else {
				imagePathList.clear();
			}
			File files[] = file.listFiles(new FilenameFilter() {

				@Override
				public boolean accept(File file, String name) {

					return name.endsWith(encrypter.getFileExtra());
				}
			});
			for (File f:files) {
				imagePathList.add(f.getPath());
			}
			if (imagePathList.size() > 0) {
				currentImagePath = imagePathList.get(0);
			}
			restoreRandomList();
		}
	}

	private void restoreRandomList() {
		if (tempRandomList == null) {
			tempRandomList = new ArrayList<String>(imagePathList.size());
		}
		else {
			tempRandomList.clear();
		}
		if (tempRandomHistoryList == null) {
			tempRandomHistoryList = new ArrayList<String>();
		}
		else {
			tempRandomHistoryList.clear();
		}
		for (String path:imagePathList) {
			tempRandomList.add(path);
		}
	}

	@Override
	public boolean onTouchEvent(MotionEvent event) {

		return gestureDetector.onTouchEvent(event);
	}

	private void changeImage() {
		Log.d(TAG, "changeImage");
		if (currentImagePath == null) {
			Log.i(TAG, "currentImagePath is null");
			return;
		}

		gifManager.setParentSize(ScreenUtils.getScreenWidth(this), ScreenUtils.getScreenHeight(this));
		if (forgroundView == gifLayout) {
			gifManager.finish();
		}
		if (gifManager.showGifView(currentImagePath, myGestureDetector.getApplearAnim(), MyGifManager.MATCH_PARENT)) {
			gifManager.refresh();
			if (forgroundView == imageView1) {
				imageView1.startAnimation(myGestureDetector.getDisapplearAnim());
				imageView1.setVisibility(View.GONE);
			}
			else if (forgroundView == imageView2) {
				imageView2.startAnimation(myGestureDetector.getDisapplearAnim());
				imageView2.setVisibility(View.GONE);
			}
			forgroundView = gifLayout;
			return;
		}

		gifLayout.setVisibility(View.GONE);
		cacheBitmap = forgroundBitmap;//get reference, recycle it after animation over
		forgroundBitmap = PictureManagerUpdate.getInstance().createHDBitmap(currentImagePath);
		if (hideView == imageView2) {
			if (forgroundView == imageView1) {
				imageView1.startAnimation(myGestureDetector.getDisapplearAnim());
				imageView1.setVisibility(View.GONE);
			}
			hideView = imageView1;
			forgroundView = imageView2;

			imageView2.setImageBitmap(forgroundBitmap);
			imageView2.startAnimation(myGestureDetector.getApplearAnim());
			imageView2.setVisibility(View.VISIBLE);
		}
		else if (hideView == imageView1) {
			if (forgroundView == imageView2) {
				imageView2.startAnimation(myGestureDetector.getDisapplearAnim());
				imageView2.setVisibility(View.GONE);
			}
			hideView = imageView2;
			forgroundView = imageView1;

			imageView1.setImageBitmap(forgroundBitmap);
			imageView1.startAnimation(myGestureDetector.getApplearAnim());
			imageView1.setVisibility(View.VISIBLE);
		}
	}

	private void flingToNext() {
		Log.d(TAG, "flingToNext");
		if (srcMode == SRC_MODE_RANDOM) {
			if (nextImagePath == null) {
				lastImagePath = currentImagePath;
				executeWholeRandom();
			}
			else {//用于记录临时的前一张和后一张
				lastImagePath = currentImagePath;
				currentImagePath = nextImagePath;
				nextImagePath = null;
				changeImage();
			}
		}
		else {
			if (imagePathList != null && imagePathList.size() > 0) {
				if (flingMode == FLING_MODE_SEQUENCE) {
					if (currentPosition == imagePathList.size() - 1) {
						currentPosition = 0;
					}
					else {
						currentPosition ++;
					}
					horizontalGallery.scrollToNext();
				}
				else if (flingMode == FLING_MODE_RANDOM_REPEAT) {
					flingRandomRepeat();
					horizontalGallery.scrollToPosition(currentPosition);
				}
				currentImagePath = imagePathList.get(currentPosition);
				changeImage();
			}
		}
	}

	private void flingToPrevious() {
		Log.d(TAG, "flingToPrevious");
		if (srcMode == SRC_MODE_RANDOM) {
			if (lastImagePath == null) {
				nextImagePath = currentImagePath;
				executeWholeRandom();
			}
			else {//用于记录临时的前一张和后一张
				nextImagePath = currentImagePath;
				currentImagePath = lastImagePath;
				lastImagePath = null;
				changeImage();
			}
		}
		else {
			if (imagePathList != null && imagePathList.size() > 0) {
				if (flingMode == FLING_MODE_SEQUENCE) {
					if (currentPosition == 0) {
						currentPosition = imagePathList.size() - 1;
					}
					else {
						currentPosition --;
					}
					horizontalGallery.scrollToPrevious();
				}
				else if (flingMode == FLING_MODE_RANDOM_REPEAT) {
					flingRandomRepeat();
					horizontalGallery.scrollToPosition(currentPosition);
				}
				currentImagePath = imagePathList.get(currentPosition);
				changeImage();
			}
		}
	}

	private void flingRandomRepeat() {
		currentPosition = Math.abs(random.nextInt()) % imagePathList.size();
	}

	private class MyGestureDetector extends SimpleOnGestureListener {

		private Animation toolbarInAnim, toolbarOutAnim, galleryInAnim, galleryOutAnim;
		private Animation appearAnim, disappearAnim;

		public MyGestureDetector() {
			disappearAnim = AnimationUtils.loadAnimation(FullScreenSurfActivity.this, R.anim.disappear);
		}

		public Animation getApplearAnim() {
			if (appearAnim == null) {
				appearAnim = AnimationUtils.loadAnimation(FullScreenSurfActivity.this, R.anim.appear);
				appearAnim.setAnimationListener(animRecycleListener);
			}
			return appearAnim;
		}

		public Animation getDisapplearAnim() {
			if (disappearAnim == null) {
				disappearAnim = AnimationUtils.loadAnimation(FullScreenSurfActivity.this, R.anim.disappear);
				disappearAnim.setAnimationListener(animRecycleListener);
			}
			return disappearAnim;
		}
		@Override
		public boolean onFling(MotionEvent e1, MotionEvent e2, float velocityX,
							   float velocityY) {
			float x1 = e1.getX();
			float x2 = e2.getX();
			float y1 = e1.getY();
			float y2 = e2.getY();
			if (x2 - x1 > FLING_SWITCH_HORIZONTAL_MIN && y2 - y1 < FLING_SWITCH_VERTICAL_MAX && y2 - y1 > -FLING_SWITCH_VERTICAL_MAX) {
				flingToPrevious();
				Log.i("FileEncryption", "position = " + currentPosition);
			}
			else if (x2 - x1 < -FLING_SWITCH_HORIZONTAL_MIN && y2 - y1 < FLING_SWITCH_VERTICAL_MAX && y2 - y1 > -FLING_SWITCH_VERTICAL_MAX) {
				flingToNext();
				Log.i("FileEncryption", "position = " + currentPosition);
			}
			if (y2 - y1 > FLING_TOOLBAR_VERTICAL_MIN && x2 - x1 < FLING_TOOLBAR_HORIZONTAL_MAX) {
				toolbar.setVisibility(View.VISIBLE);
				if (toolbarInAnim == null) {
					toolbarInAnim = AnimationUtils.loadAnimation(FullScreenSurfActivity.this, R.anim.push_down_in);
				}
				toolbar.startAnimation(toolbarInAnim);
				if (srcMode != SRC_MODE_RANDOM) {
					horizontalGallery.setVisibility(View.VISIBLE);
					if (galleryInAnim == null) {
						galleryInAnim = AnimationUtils.loadAnimation(FullScreenSurfActivity.this, R.anim.push_up_in);
					}
					horizontalGallery.startAnimation(galleryInAnim);
				}
				previousView.setVisibility(View.VISIBLE);
				nextView.setVisibility(View.VISIBLE);
			}
			else if (y2 - y1 < -FLING_TOOLBAR_VERTICAL_MIN && x2 - x1 < FLING_TOOLBAR_HORIZONTAL_MAX) {
				toolbar.setVisibility(View.GONE);
				if (toolbarOutAnim == null) {
					toolbarOutAnim = AnimationUtils.loadAnimation(FullScreenSurfActivity.this, R.anim.push_up_out);
				}
				toolbar.startAnimation(toolbarOutAnim);
				if (srcMode != SRC_MODE_RANDOM) {
					horizontalGallery.setVisibility(View.GONE);
					if (galleryOutAnim == null) {
						galleryOutAnim = AnimationUtils.loadAnimation(FullScreenSurfActivity.this, R.anim.push_down_out);
					}
					horizontalGallery.startAnimation(galleryOutAnim);
				}
				previousView.setVisibility(View.GONE);
				nextView.setVisibility(View.GONE);
			}
			return super.onFling(e1, e2, velocityX, velocityY);
		}

	}

	@Override
	public void onClick(View view) {
		if (view == previousView) {
			flingToPrevious();
		}
		else if (view == nextView) {
			flingToNext();
		}
	}

	@Override
	public boolean handleMessage(Message msg) {
		if (msg.what == AutoPlayController.AUTO_SPECIFIED_LIST) {
			handleAutoPlay(msg);
		}
		else if (msg.what == AutoPlayController.AUTO_WHOLE_RANDOM) {
			handleWholeAutoPlay();
		}
		return true;
	}

	HorizontalGallery.OnItemSelectListener galleryListener = new HorizontalGallery.OnItemSelectListener() {

		@Override
		public void onGalleryItemLongClick(View view, int position) {

		}

		@Override
		public void onGalleryItemClick(View view, int position) {
			currentPosition = position;
			currentImagePath = imagePathList.get(currentPosition);
			changeImage();
		}

		@Override
		public void onGalleryItemSelectStatus(View view, int position) {
			if (lastSelectView != null) {
				lastSelectView.setImageDrawable(null);
			}
			ImageView iv = (ImageView) view;
			if (Application.isLollipop()) {
				iv.setImageResource(R.drawable.gallery_border_choose_l);
			}
			else {
				iv.setImageResource(R.drawable.gallery_border_choose);
			}
			lastSelectView = iv;
		}
	};

	private class AnimRecycleListener implements AnimationListener {

		@Override
		public void onAnimationEnd(Animation anim) {
			Log.d(TAG, "onAnimationEnd");
			if (hideView instanceof ImageView) {
				((ImageView) hideView).setImageBitmap(null);
			}
			if (cacheBitmap != null) {
				Log.d(TAG, "recycle bitmap");
				cacheBitmap.recycle();
				cacheBitmap = null;
			}
		}

		@Override
		public void onAnimationRepeat(Animation arg0) {

		}

		@Override
		public void onAnimationStart(Animation arg0) {

		}
	}

	/**
	 * 自动播放模式下，不播放gif动画，仅仅播放gif的唯一帧
	 */
	private void handleWholeAutoPlay() {
		Log.d(TAG, "handleWholeAutoPlay");
		createRandomPath();

		cacheBitmap = forgroundBitmap;

		if (forgroundView == gifLayout) {
			gifLayout.setVisibility(View.GONE);
			forgroundView = imageView1;
			imageView1.setVisibility(View.VISIBLE);
		}

		forgroundBitmap = PictureManagerUpdate.getInstance().createHDBitmap(currentImagePath);

		ImageView target = imageView1;
		hideView = imageView2;
		if (forgroundView == imageView2) {
			target = imageView2;
			hideView = imageView1;
		}
		target.setImageBitmap(forgroundBitmap);

		Animation animation = autoPlayController.randomAnimation();
		animation.setAnimationListener(animRecycleListener);

		target.startAnimation(animation);
	}

	/**
	 * 自动播放模式下，不播放gif动画，仅仅播放gif的唯一帧
	 */
	private void handleAutoPlay(Message msg) {

		Log.d(TAG, "handleAutoPlay");
		Bundle bundle = msg.getData();
		String finish = bundle.getString("autoplay_finish");
		if (finish != null && finish.equals("true")) {
			stopAutoPlay();
			Toast.makeText(this, R.string.spicture_autoplay_finish, Toast.LENGTH_LONG).show();
			return;
		}
		int index = bundle.getInt("autoplay_index");
		//boolean scroll = bundle.getBoolean("autoplay_scroll");

		if (forgroundView == gifLayout) {
			gifLayout.setVisibility(View.GONE);
			forgroundView = imageView1;
			imageView1.setVisibility(View.VISIBLE);
		}

		cacheBitmap = forgroundBitmap;
		forgroundBitmap = PictureManagerUpdate.getInstance().createHDBitmap(currentImagePath);
		playImageAt(index);

		if (SettingProperties.isShowAnimation(this)) {

			Animation animation = autoPlayController.randomAnimation();
			animation.setAnimationListener(animRecycleListener);

			if (forgroundView == imageView1) {
				imageView1.startAnimation(animation);
			}
			else {
				imageView2.startAnimation(animation);
			}
		}
	}

	private void playImageAt(int index) {

		currentImagePath = imagePathList.get(index);
		if (imageView1.getVisibility() == View.VISIBLE) {
			imageView1.setImageBitmap(forgroundBitmap);
			hideView = imageView2;
		}
		else {
			imageView2.setImageBitmap(forgroundBitmap);
			hideView = imageView1;
		}
	}

	private boolean stopAutoPlay() {
		if (autoPlayController != null && autoPlayController.isAutoPlaying()) {
			autoPlayController.stopAutoPlay();
			playButton.setImageResource(R.drawable.actionbar_play);
			return true;
		}
		return false;
	}

	@Override
	protected void onResume() {
		stopAutoPlay();
		super.onResume();
	}

	@Override
	protected void onPause() {
		stopAutoPlay();
		super.onPause();
	}

	@Override
	public void onBackPressed() {
		if (stopAutoPlay()) {
			return;
		}
		super.onBackPressed();
	}

	@Override
	public void onConfigurationChanged(Configuration newConfig) {

		if (forgroundView == gifLayout) {
			gifManager.updateParentSize(ScreenUtils.getScreenWidth(this), ScreenUtils.getScreenHeight(this));
			gifManager.refresh();
		}
		super.onConfigurationChanged(newConfig);
	}

	@Override
	protected void onRestart() {
		checkIdentity();
		super.onRestart();
	}

	public void checkIdentity() {
		if (AccessController.getInstance().getAccessMode() == AccessController.ACCESS_MODE_PRIVATE) {

			accessPrivateLayout = (LinearLayout) findViewById(R.id.layout_access_private);
			accessPrivateLayout.setVisibility(View.VISIBLE);
			accessPrivateEdit = (EditText) findViewById(R.id.access_private_pwd);
			accessPrivateEdit.setText("");
			accessPrivateButton = (Button) findViewById(R.id.access_private_ok);
			accessPrivateButton.setOnClickListener(new View.OnClickListener() {

				@Override
				public void onClick(View v) {
					if (accessPrivateEdit.getText().toString().equals("1010520")) {
						accessPrivateLayout.setVisibility(View.GONE);
					}
					else {
						Toast.makeText(FullScreenSurfActivity.this, R.string.login_pwd_error, Toast.LENGTH_LONG).show();
					}
				}
			});
		}
	}

}
