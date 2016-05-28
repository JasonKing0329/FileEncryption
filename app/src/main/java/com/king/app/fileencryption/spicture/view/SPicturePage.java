package com.king.app.fileencryption.spicture.view;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.util.HashMap;

import com.king.app.fileencryption.Application;
import com.king.app.fileencryption.FileManagerActivityUpdate;
import com.king.app.fileencryption.R;
import com.king.app.fileencryption.TabActionBar;
import com.king.app.fileencryption.controller.AccessController;
import com.king.app.fileencryption.controller.AccessController.IdentityCheckListener;
import com.king.app.fileencryption.controller.EncrypterFactory;
import com.king.app.fileencryption.controller.FileManagerAction;
import com.king.app.fileencryption.controller.PictureManagerUpdate;
import com.king.app.fileencryption.data.Configuration;
import com.king.app.fileencryption.data.Constants;
import com.king.app.fileencryption.filemanager.view.FileManagerPage;
import com.king.app.fileencryption.open.gifview.GifView;
import com.king.app.fileencryption.open.image.ZoomListener;
import com.king.app.fileencryption.publicview.CustomDialog;
import com.king.app.fileencryption.publicview.DefaultDialogManager;
import com.king.app.fileencryption.publicview.FullScreenSurfActivity;
import com.king.app.fileencryption.publicview.HorizontalGallery.OnItemSelectListener;
import com.king.app.fileencryption.setting.SettingProperties;
import com.king.app.fileencryption.slidingmenu.SlidingMenuCreator;
import com.king.app.fileencryption.sorder.entity.SOrder;
import com.king.app.fileencryption.sorder.view.SOrderChooserUpdate;
import com.king.app.fileencryption.spicture.controller.AutoPlayController;
import com.king.app.fileencryption.spicture.controller.SpictureController;
import com.king.app.fileencryption.tool.Encrypter;
import com.king.app.fileencryption.tool.ScreenInfor;
import com.king.app.fileencryption.waterfall.WaterFallActivity;

import android.app.Activity;
import android.app.AlertDialog;
import android.app.ProgressDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.Matrix;
import android.os.Bundle;
import android.os.Handler;
import android.os.Handler.Callback;
import android.os.Message;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup.LayoutParams;
import android.widget.AdapterView;
import android.widget.ImageView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.AdapterView.OnItemLongClickListener;
import android.widget.ImageView.ScaleType;
import android.widget.LinearLayout;
import android.widget.Toast;

@Deprecated
public class SPicturePage implements FileManagerAction, SPictureChooseListener, OnItemSelectListener
		, OnItemClickListener, OnItemLongClickListener, Callback{

	private final String TAG = "SPicturePage";

	private SpictureController controller;
	private AutoPlayController autoPlayController;
	private Context context;
	private ImageView showView;
	private LinearLayout showGifView;

	private Chooser sChooser;
	private VerticalChooser portraitChooser;
	private HorizontalChooser landscapeChooser;

	private Encrypter encrypter;

	private final int VIEW_GRID = 0;
	private final int VIEW_GALLERY = 1;
	private final int SHOW_MODE_RANK = 0;
	private final int SHOW_MODE_ORDER = 1;
	private final int SHOW_MODE_RANDOM = 2;
	private final int SHOW_MODE_FOLDER = 3;
	private int currentView;
	private int currentShowMode;
	private ImageView lastChosenItem;
	private ProgressDialog progressDialog;
	private View scroller;
	private TabActionBar actionBar;

	public SPicturePage(Context context) {
		Log.i(TAG, "create from normal");
		this.context = context;
		encrypter = EncrypterFactory.create();
		currentView = VIEW_GALLERY;
		currentShowMode = SHOW_MODE_FOLDER;
		controller = new SpictureController(context);
		controller.setEncrpter(encrypter);
		controller.setCurrentPath(FileManagerPage.currentPath);
		controller.registCallback(this);
		android.content.res.Configuration configuration = context.getResources().getConfiguration();
		onConfigurationChanged(configuration);
		initGalleryData();

	}

	public SPicturePage(Context context, SOrder order) {
		Log.i(TAG, "create from sorder");
		this.context = context;
		encrypter = EncrypterFactory.create();
		currentView = VIEW_GALLERY;
		currentShowMode = SHOW_MODE_ORDER;
		controller = new SpictureController(context);
		controller.setEncrpter(encrypter);
		controller.setCurrentPath(FileManagerPage.currentPath);
		controller.setCurrentOrder(order);
		controller.registCallback(this);
		android.content.res.Configuration configuration = context.getResources().getConfiguration();
		onConfigurationChanged(configuration);
		initGalleryData();
	}

	private void initGalleryViewElement() {
		Log.i(TAG, "initGalleryViewElement");
		Activity view = (Activity) context;

//		galleryLayout = (LinearLayout) view
//				.findViewById(R.id.layout_page_spicture_gallery);
		showView = (ImageView) view.findViewById(R.id.spicture_imageview);
		showGifView = (LinearLayout) view.findViewById(R.id.spicture_gifview);
	}

	private void initGridViewElement() {
		Activity view = (Activity) context;
		view.setContentView(R.layout.page_spicture_grid);
	}

	private void initGalleryData() {
		if (currentShowMode == SHOW_MODE_ORDER) {
			Log.i(TAG, "initGalleryData currentShowMode = order");
			loadPicturesFromOrder();
		}
		else {
			Log.i(TAG, "initGalleryData currentShowMode = " + currentShowMode);
			loadCurFolderPictures();
		}
	}

	public void startOrderView(SOrder order) {
		Log.i(TAG, "startOrderView");
		currentShowMode = SHOW_MODE_ORDER;
		controller.setCurrentOrder(order);
		changeActionBarTitle();
		prepareActionBar();
	}

	private void changeActionBarTitle() {

		if (currentShowMode == SHOW_MODE_FOLDER) {
			if (controller.getCurrentPath().equals(Configuration.APP_DIR_IMG)) {
				actionBar.setTitle("root");
			}
			else {
				actionBar.setTitle(controller.getCurrentPath().substring(Configuration.APP_DIR_IMG.length()));
			}
		}
		if (currentShowMode == SHOW_MODE_ORDER) {
			actionBar.setTitle(controller.getCurrentOrder().getName());
		}
		if (currentShowMode == SHOW_MODE_RANDOM) {
			actionBar.setTitle(context.getResources().getString(R.string.menu_random));
		}
	}

	@Override
	public void onConfigurationChanged(android.content.res.Configuration newConfig) {
		initGalleryViewElement();
		Activity view = (Activity) context;
		if (newConfig.orientation == android.content.res.Configuration.ORIENTATION_PORTRAIT) {
			Log.i(TAG, "ORIENTATION_PORTRAIT");
			view.findViewById(R.id.spicture_chooser_portrait).setVisibility(View.GONE);
			scroller = view.findViewById(R.id.horizontalGallery);
			scroller.setVisibility(View.VISIBLE);
			if (landscapeChooser == null) {
				Log.i(TAG, "init landscapeChooser");
				landscapeChooser = new HorizontalChooser(context);
				sChooser = landscapeChooser;
			}
			else {
				Log.i(TAG, "reinit landscapeChooser");
				sChooser = landscapeChooser;
				sChooser.updateList(controller.getFileNameList());
				sChooser.reInit();
			}
		}
		else if (newConfig.orientation == android.content.res.Configuration.ORIENTATION_LANDSCAPE) {
			Log.i(TAG, "ORIENTATION_LANDSCAPE");
			view.findViewById(R.id.horizontalGallery).setVisibility(View.GONE);
			scroller = view.findViewById(R.id.spicture_chooser_portrait);
			scroller.setVisibility(View.VISIBLE);
			if (portraitChooser == null) {
				Log.i(TAG, "init portraitChooser");
				portraitChooser = new VerticalChooser(context, null);
				sChooser = portraitChooser;
			}
			else {
				Log.i(TAG, "reinit portraitChooser");
				sChooser = portraitChooser;
				sChooser.updateList(controller.getFileNameList());
				sChooser.reInit();
			}
		}
		sChooser.setOnChooseListener(this);

		//很奇怪如果不重新用代码设置，imageview将充不满父linearlayout空间
		LinearLayout.LayoutParams params = (android.widget.LinearLayout.LayoutParams) view.findViewById(R.id.spicture_imageview).getLayoutParams();
		params.width = LinearLayout.LayoutParams.MATCH_PARENT;
		params.height = LinearLayout.LayoutParams.MATCH_PARENT;
	}

	private void playGifImage(String filePath) {
		Log.i(TAG, "playGifImage " + filePath);
		/**
		 * refer包里的gifview处理有bug,直接用setGifImage切换图片会有FC问题,只能另辟蹊径每次重新创建一个gifview
		 */
		GifView gifView = null;
		try {
			gifView = (GifView) showGifView.getChildAt(0);
		} catch (Exception e) {
			gifView = null;
		}
		if (gifView != null) {
			gifView.finish();
			showGifView.removeView(gifView);
		}

		showView.setVisibility(View.GONE);//ImageView
		try {
			/**
			 * refer包里的gifview处理有bug,直接用setGifImage切换图片会有FC问题,
			 * 只能另辟蹊径每次重新创建一个gifview
			 */
			gifView = new GifView(context);
			gifView.setGifImage(new FileInputStream(filePath));
			showGifView.addView(gifView);
			showGifView.setVisibility(View.VISIBLE);
			// showGifView.setGifImage(new FileInputStream(filePath));
		} catch (FileNotFoundException e) {
			showGifView.setVisibility(View.INVISIBLE);
			Log.e(TAG, e.getMessage());
			e.printStackTrace();
		} catch (Exception e) {
			Log.e(TAG, e.getMessage());
			e.printStackTrace();
		}
	}
	private void playGifImage(byte[] datas) {
		Log.i(TAG, "playGifImage byte[]");
		/**
		 * refer包里的gifview处理有bug,直接用setGifImage切换图片会有FC问题,只能另辟蹊径每次重新创建一个gifview
		 */
		GifView gifView = null;
		try {
			gifView = (GifView) showGifView.getChildAt(0);
		} catch (Exception e) {
			gifView = null;
		}
		if (gifView != null) {
			gifView.finish();
			showGifView.removeView(gifView);
		}

		showView.setVisibility(View.GONE);//ImageView
		try {
			/**
			 * refer包里的gifview处理有bug,直接用setGifImage切换图片会有FC问题,
			 * 只能另辟蹊径每次重新创建一个gifview
			 */
			gifView = new GifView(context);
			gifView.setGifImage(datas);
			showGifView.addView(gifView);
			showGifView.setVisibility(View.VISIBLE);
			// showGifView.setGifImage(new FileInputStream(filePath));
		} catch (Exception e) {
			showGifView.setVisibility(View.INVISIBLE);
			Log.e(TAG, e.getMessage());
			e.printStackTrace();
		}
	}

	private void chooseImage(int index) {
		Log.i(TAG, "chooseImage " + index);
		if (controller.getFileNameList() == null || controller.getFileNameList().size() == 0) {
			return;
		}
		String filePath = getItemFilePath(index);
		Log.i(TAG, "chooseImage -> filepath " + filePath);
		if (filePath == null) {
			Toast.makeText(context, R.string.login_pwd_error, Toast.LENGTH_LONG).show();
			return;
		}

		if (filePath.endsWith(".gif")) {
			playGifImage(filePath);
			return;
		}

		if (encrypter.isEncrypted(new File(filePath))) {
			String originName = encrypter.decipherOriginName(new File(filePath));
			Log.i(TAG, "chooseImage -> originName = " + originName);
			if (originName != null && originName.endsWith(".gif")) {
				playGifImage(encrypter.decipherToByteArray(new File(filePath)));
				return;
			}
		}

		/**
		 * 若非gif图片,则用普通的imageview显示
		 */
		showGifView.setVisibility(View.GONE);
		showView.setVisibility(View.VISIBLE);

		Bitmap bitmap = PictureManagerUpdate.getInstance().createHDSpicture(
				controller.getFileNameList().get(index), context, getOrienForCreateImage());

		if (bitmap != null) {
			//showImageMatchScreen(bitmap);
			android.content.res.Configuration configuration = context.getResources().getConfiguration();
			showView.setImageBitmap(bitmap);
			int viewWidth = 0;
			int viewHeight = 0;
			if (configuration.orientation == android.content.res.Configuration.ORIENTATION_PORTRAIT) {
				viewWidth = ScreenInfor.getWidth((Activity) context);
				viewHeight = context.getResources().getDimensionPixelSize(R.dimen.spicture_view_height);
			}
			else {
				viewWidth = context.getResources().getDimensionPixelSize(R.dimen.spicture_view_width);
				viewHeight = context.getResources().getDimensionPixelSize(R.dimen.spicture_view_height);
			}
			Matrix matrix = new Matrix();
			matrix.postTranslate(viewWidth/2 - bitmap.getWidth()/2, viewHeight/2 - bitmap.getHeight()/2);
			showView.setImageMatrix(matrix);
			showView.setOnTouchListener(new ZoomListener(false));
		}
		else {
			Log.i(TAG, "chooseImage -> bitmap = null");
			showView.setImageResource(R.drawable.ic_launcher);
		}
		//showChosedImage(bitmap);
	}

	/**
	 * 用于加载缩略图过程根据屏幕横向纵向加载不同的尺寸
	 * @return
	 */
	public int getOrienForCreateImage() {
		android.content.res.Configuration configuration = context.getResources().getConfiguration();
		if (configuration.orientation == android.content.res.Configuration.ORIENTATION_PORTRAIT) {
			return 1;
		}
		else {
			return 2;
		}
	}

	/**
	 * 将图片按适当比例显示在ImageView中
	 * @param bitmap
	 */
	private void showImageMatchScreen(Bitmap bitmap) {

		Log.i(TAG, "showImageMatchScreen");
		LayoutParams params = showView.getLayoutParams();
		controller.getSuitableLayout(bitmap, getOrienForCreateImage(), params);
		showView.setLayoutParams(params);
		showView.setScaleType(ScaleType.FIT_CENTER);
		showView.setImageBitmap(bitmap);
	}

	private void viewDetails(int pos) {
		String filePath = getItemFilePath(pos);
		Log.i(TAG, "viewDetails " + filePath);
		if (filePath != null) {
			File file = new File(filePath);
			if (file.exists()) {
				new DefaultDialogManager().openDetailDialog(context, file);
			}
		}
	}

	private void deleteItemFromOrder(int pos) {
		Log.i(TAG, "deleteItemFromOrder " + pos);
		String msg = null;
		if (controller.deleteItemFromOrder(pos)) {
			msg = context.getResources().getString(R.string.spicture_myorders_delete_item_ok);
			refreshPage();
		}
		else {
			msg = context.getResources().getString(R.string.sorder_delete_fail);
		}

		msg = msg.replace("%s", controller.getCurrentOrder().getName());
		Toast.makeText(context, msg, Toast.LENGTH_LONG).show();
	}

	private void openOrderChooserToSetCover(final int position) {
		Log.i(TAG, "openOrderChooserToSetCover " + position);

		SOrderChooserUpdate chooser = new SOrderChooserUpdate(context, new CustomDialog.OnCustomDialogActionListener() {

			@Override
			public boolean onSave(Object object) {
				if (object != null) {
					SOrder order = (SOrder) object;
					String msg = null;
					String itemPath = getItemFilePath(position);
					Log.i(TAG, "openOrderChooserToSetCover itemPath = " + itemPath);
					if (itemPath != null) {
						order.setCoverPath(itemPath);
						if (controller.setOrderCover(order)) {
							msg = context.getResources().getString(R.string.spicture_myorders_set_cover_ok);
						}
						else {
							msg = context.getResources().getString(R.string.spicture_myorders_set_cover_fail);
						}
						if (order.getName() != null) {
							msg = msg.replace("%s", order.getName());
						}
					}
					else {
						msg = context.getResources().getString(R.string.login_pwd_error);
					}
					Toast.makeText(context, msg, Toast.LENGTH_LONG).show();
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
		chooser.setTitle(context.getResources().getString(R.string.set_as_cover));
		//chooser.setLightTheme();
		chooser.show();

		/*
		SOrderChooser chooser = new SOrderChooser(context, new OnOrderChooseListener() {

			@Override
			public void chooseOrder(SOrder order) {
				String msg = null;
				String itemPath = getItemFilePath(position);
				Log.i(TAG, "openOrderChooserToSetCover itemPath = " + itemPath);
				if (itemPath != null) {
					order.setCoverPath(itemPath);
					if (controller.setOrderCover(order)) {
						msg = context.getResources().getString(R.string.spicture_myorders_set_cover_ok);
					}
					else {
						msg = context.getResources().getString(R.string.spicture_myorders_set_cover_fail);
					}
					if (order.getName() != null) {
						msg = msg.replace("%s", order.getName());
					}
				}
				else {
					msg = context.getResources().getString(R.string.login_pwd_error);
				}
				Toast.makeText(context, msg, Toast.LENGTH_LONG).show();
			}
		});
		chooser.setTitleCustom(context.getResources().getString(R.string.set_as_cover));
		chooser.setLightTheme();
		chooser.show();
		*/
	}

	private String getItemFilePath(int index) {
		String filePath = null;
//		if (currentShowMode == SHOW_MODE_FOLDER) {
//			filePath = currentPath + "/" + fileNameList.get(index);
//		}

		filePath = controller.getFileNameList().get(index);
		return filePath;
	}

	private void openOrderChooserToAddItem(final int position) {
		Log.i(TAG, "openOrderChooserToAddItem " + position);

		SOrderChooserUpdate chooser = new SOrderChooserUpdate(context, new CustomDialog.OnCustomDialogActionListener() {

			@Override
			public boolean onSave(Object object) {
				if (object != null) {
					final SOrder order = (SOrder) object;
					final String itemPath = getItemFilePath(position);
					Log.i(TAG, "openOrderChooserToAddItem itemPath = " + itemPath);
					if (itemPath != null) {
						if (controller.isItemExist(itemPath, order.getId())) {
							String title = context.getResources().getString(R.string.spicture_myorders_item_exist);
							title = String.format(title, order.getName());
							new AlertDialog.Builder(context)
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
						Toast.makeText(context, R.string.login_pwd_error, Toast.LENGTH_LONG).show();
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
		chooser.setTitle(context.getResources().getString(R.string.add_to_order));
		chooser.show();
		/*
		SOrderChooser chooser = new SOrderChooser(context, new OnOrderChooseListener() {

			@Override
			public void chooseOrder(final SOrder order) {
				final String itemPath = getItemFilePath(position);
				Log.i(TAG, "openOrderChooserToAddItem itemPath = " + itemPath);
				if (itemPath != null) {
					if (controller.isItemExist(itemPath, order.getId())) {
						String title = context.getResources().getString(R.string.spicture_myorders_item_exist);
						title = String.format(title, order.getName());
						new AlertDialog.Builder(context)
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
					Toast.makeText(context, R.string.login_pwd_error, Toast.LENGTH_LONG).show();
				}
			}
		});
		chooser.setTitleCustom(context.getResources().getString(R.string.add_to_order));
		chooser.setLightTheme();
		chooser.show();
		*/
	}

	private void addToOrder(String path, SOrder order) {
		String msg = null;
		if (controller.addItemToOrder(path, order)) {
			msg = context.getResources().getString(R.string.spicture_myorders_add_ok);
		}
		else {
			msg = context.getResources().getString(R.string.spicture_myorders_add_fail);
		}
		if (order.getName() != null) {
			msg = msg.replace("%s", order.getName());
		}
		Toast.makeText(context, msg, Toast.LENGTH_LONG).show();
	}

	@Override
	public boolean onOptionsItemSelected(MenuItem item) {

		switch (item.getItemId()) {
			case R.id.menu_view_current_folder:
				resetNoCurrentOrder();
				currentShowMode = SHOW_MODE_FOLDER;
				loadCurFolderPictures();
				changeActionBarTitle();
				break;

			case R.id.menu_order:
				resetNoCurrentPath();
				currentShowMode = SHOW_MODE_ORDER;
				loadPicturesFromOrder();
				changeActionBarTitle();
				break;
			/*
		case R.id.menu_random:
			currentShowMode = SHOW_MODE_RANDOM;
			loadRandomPictures();
			changeActionBarTitle();
			break;
			*/
			case R.id.menu_rank:
				currentShowMode = SHOW_MODE_RANK;
				break;

			case R.id.menu_shuffle_gallery:
				shuffleList();
				break;
			case R.id.menu_random_game:
				if (AccessController.getInstance().getAccessMode() == AccessController.ACCESS_MODE_PRIVATE) {
					AccessController.getInstance().showPwdDialog(context, new IdentityCheckListener() {

						@Override
						public void pass() {
							controller.switchToRandomGame();
						}

						@Override
						public void fail() {

						}

						@Override
						public void cancel() {

						}
					});
				}
				else {
					controller.switchToRandomGame();
				}
				break;
			case R.id.menu_thumb_waterfall:
				if (currentShowMode == SHOW_MODE_FOLDER) {
					startFileWaterFallView();
				}
				else if (currentShowMode == SHOW_MODE_ORDER) {
					startOrderWaterFallView();
				}
				break;
			/*
		case R.id.menu_auto_play:
			if (controller.isAutoPlaying()) {
				stopAutoPlay();
			}
			else {
				autoPlay();
			}
			break;
		case R.id.menu_fullscreen:
			Bundle bundle = new Bundle();
			if (currentShowMode == SHOW_MODE_FOLDER) {
				bundle.putInt("src_mode", FullScreenSurfActivity.SRC_MODE_FOLDER);
				bundle.putString("path", controller.getCurrentPath());
			}
			else if (currentShowMode == SHOW_MODE_ORDER) {
				bundle.putInt("src_mode", FullScreenSurfActivity.SRC_MODE_ORDER);
				bundle.putInt("orderId", controller.getCurrentOrder().getId());
			}
			Intent intent = new Intent();
			intent.putExtras(bundle);
			intent.setClass(context, FullScreenSurfActivity.class);
			context.startActivity(intent);
			break;
			*/
		}
		return true;
	}

	private void startFileWaterFallView() {
		String currentPath = controller.getCurrentPath();
		if (currentPath != null) {
			File file = new File(currentPath);
			if (file.list().length > Constants.WATERFALL_MIN_NUMBER) {
				Intent intent = new Intent();
				intent.setClass(context, WaterFallActivity.class);
				intent.putExtra("filePath", file.getPath());
				((Activity) context).startActivity(intent);
			}
		}
	}

	private void startOrderWaterFallView() {
		SOrder currentOrder = controller.getCurrentOrder();
		if (currentOrder != null && currentOrder.getImgPathList() != null) {
			if (currentOrder.getImgPathList().size() > Constants.WATERFALL_MIN_NUMBER) {
				Intent intent = new Intent();
				intent.setClass(context, WaterFallActivity.class);
				intent.putExtra("order", currentOrder.getId());
				((Activity) context).startActivity(intent);
			}
		}
	}

	@Override
	public SlidingMenuCreator loadMenu(LinearLayout menuLayout) {
//		menuLayout.removeAllViews();
//		SlidingMenuCreator creator = new SlidingMenuCreator(context, slidingMenuListener);
//		creator.loadMenu(Constants.spictureMenu, menuLayout, SettingProperties.getSlidingMenuMode(context));
		return null;
	}

	@Override
	public SlidingMenuCreator loadTwoWayMenu(LinearLayout menuLayout,
											 LinearLayout menuLayoutRight) {
		// TODO Auto-generated method stub
		return null;
	}

	OnClickListener slidingMenuListener = new OnClickListener() {

		@Override
		public void onClick(View v) {
			boolean excuted = false;

			switch (v.getId()) {
				case R.string.menu_view_current_folder:
					currentShowMode = SHOW_MODE_FOLDER;
					loadCurFolderPictures();
					changeActionBarTitle();
					break;

				case R.string.menu_order:
					currentShowMode = SHOW_MODE_ORDER;
					break;
				case R.string.menu_rank:
					currentShowMode = SHOW_MODE_RANK;
					break;

				case R.string.menu_shuffle_gallery:
					shuffleList();
					break;
				case R.string.menu_random_game:
					if (AccessController.getInstance().getAccessMode() == AccessController.ACCESS_MODE_PRIVATE) {
						AccessController.getInstance().showPwdDialog(context, new IdentityCheckListener() {

							@Override
							public void pass() {
								controller.switchToRandomGame();
							}

							@Override
							public void fail() {

							}

							@Override
							public void cancel() {

							}
						});
					}
					else {
						controller.switchToRandomGame();
					}
					break;
			}
			if (!excuted) {
				((FileManagerActivityUpdate) context).slidingMenuListener.onClick(v);
			}
		}
	};

	private void autoPlay() {
		Log.i(TAG, "autoPlay");
		if (controller.getFileNameList() != null) {

			if (autoPlayController == null) {
				autoPlayController = new AutoPlayController(context, this);
			}
			autoPlayController.setFileNameList(controller.getFileNameList());
			int min_number = SettingProperties.getMinNumberToPlay(context);

			if (!autoPlayController.canPlay()) {
				String msg = context.getResources().getString(R.string.spicture_autoplay_tooless);
				msg = String.format(msg, min_number);
				Toast.makeText(context, msg, Toast.LENGTH_LONG).show();
			}
			else {
				/*
				if (text == null) {
					EditText edit = new EditText(context);
					edit.setInputType(InputType.TYPE_CLASS_NUMBER);
					final EditText timeEdit = edit;
					AlertDialog.Builder dialog = new AlertDialog.Builder(context);
					dialog.setTitle(R.string.spicture_autoplay_time);
					dialog.setView(timeEdit);
					dialog.setPositiveButton(R.string.ok, new DialogInterface.OnClickListener() {

						@Override
						public void onClick(DialogInterface dialog, int which) {
							try {
								int time = Integer.parseInt(timeEdit.getText().toString());
								startAutoPlay(time);
							} catch (Exception e) {
								Toast.makeText(context, R.string.login_pwd_error, Toast.LENGTH_LONG).show();
							}
						}
					});
					dialog.setNegativeButton(R.string.cancel, null);
					dialog.show();
				}
				else {
				*/
				actionBar.changePlayStatus(true);
				startAutoPlay(SettingProperties.getAnimationSpeed(context));
				//}
			}
		}
	}

	private void startAutoPlay(int time) {
		Log.i(TAG, "startAutoPlay");
		if (!autoPlayController.isAutoPlaying()) {
			sChooser.setVisibility(View.GONE);
			autoPlayController.startAutoPlay(time);
		}
	}

	private void stopAutoPlay() {
		autoPlayController.stopAutoPlay();
		sChooser.setVisibility(View.VISIBLE);
		actionBar.changePlayStatus(false);
	}

	@Override
	public void onCreateOptionsMenu(Menu menu) {
		Log.i(TAG, "onCreateOptionsMenu");
		onPrepareOptionsMenu(menu);
	}

	@Override
	public boolean onPrepareOptionsMenu(Menu menu) {
		Log.i(TAG, "onPrepareOptionsMenu");
		menu.setGroupVisible(R.id.group_file, false);
		menu.setGroupVisible(R.id.group_sorder, false);
		menu.setGroupVisible(R.id.group_spicture, true);
		menu.findItem(R.id.menu_edit).setVisible(true);
		if (controller.getCurrentOrder() != null && currentShowMode == SHOW_MODE_ORDER) {
			menu.findItem(R.id.menu_random_game).setVisible(true);
		}
		else {
			menu.findItem(R.id.menu_random_game).setVisible(false);
		}

		if (currentShowMode == SHOW_MODE_RANDOM || currentShowMode == SHOW_MODE_RANK) {
			menu.findItem(R.id.menu_thumb_waterfall).setVisible(false);
			if (controller.getCurrentPath() != null) {
				menu.findItem(R.id.menu_view_current_folder).setVisible(true);
			}
			else {
				menu.findItem(R.id.menu_view_current_folder).setVisible(false);
			}

			if (controller.getCurrentOrder() != null) {
				menu.findItem(R.id.menu_order).setVisible(true);
			}
			else {
				menu.findItem(R.id.menu_order).setVisible(false);
			}
		}
		else {
			menu.findItem(R.id.menu_thumb_waterfall).setVisible(true);
			if (currentShowMode == SHOW_MODE_ORDER) {
				if (controller.getCurrentPath() != null) {
					menu.findItem(R.id.menu_view_current_folder).setVisible(true);
				}
				else {
					menu.findItem(R.id.menu_view_current_folder).setVisible(false);
				}
			}
			else {
				menu.findItem(R.id.menu_view_current_folder).setVisible(false);
			}
			menu.findItem(R.id.menu_order).setVisible(false);
		}
		/*
		if (controller.isAutoPlaying()) {
			menu.findItem(R.id.menu_auto_play).setTitle(context.getResources().getString(R.string.menu_stop_auto_play));
		}
		else {
			menu.findItem(R.id.menu_auto_play).setTitle(context.getResources().getString(R.string.menu_auto_play));
		}
		*/
		return true;
	}

	@Override
	public void onBackPressed() {
		Log.i(TAG, "onBackPressed");
		if (autoPlayController != null && autoPlayController.isAutoPlaying()) {
			stopAutoPlay();
		}
		else {
			if (context != null) {
				((Activity) context).finish();
			}
		}
	}

	@Override
	public void saveState() {
		Log.i(TAG, "saveState");
		if (autoPlayController != null && autoPlayController.isAutoPlaying()) {
			stopAutoPlay();
			Log.i(TAG, "isautoplay " + autoPlayController.isAutoPlaying());
		}
	}

	public void recycleResource() {
		Log.i(TAG, "recycleResource");
		showView.setImageBitmap(null);
		if (sChooser != null) {
			sChooser.prepareRecycle();
		}
		if (controller != null) {
			controller.recycleResource();
		}
	}

	private boolean isReinit;
	@Override
	public void reInit() {
		isReinit = true;
		if (currentView == VIEW_GALLERY) {
			Log.i(TAG, "reInit VIEW_GALLERY");

			//loadxxx 里有bitmap.recycle操作，所以如果事先注册给了chooser，chooser会因为线程更新过程中就使用了recycle的bitmap，造成FC
			//onConfigurationChanged(configuration);
			controller.setCurrentPath(FileManagerPage.currentPath);

			if (currentShowMode == SHOW_MODE_FOLDER) {
				//onConfigurationChanged(configuration);
				reinitViewElement();
				loadCurFolderPictures();
			}
			else if (currentShowMode == SHOW_MODE_RANDOM) {
				//onConfigurationChanged(configuration);
				reinitViewElement();
				sChooser.updateList(controller.getFileNameList());
				sChooser.notifyAdapterRefresh();
			}
			else if (currentShowMode == SHOW_MODE_ORDER) {
				//onConfigurationChanged(configuration);
				reinitViewElement();
				loadPicturesFromOrder();
			}
			changeActionBarTitle();

		} else if (currentView == VIEW_GRID) {
			Log.i(TAG, "reInit VIEW_GRID");
			initGridViewElement();

			//setBackground(background);

		}
	}

	private void reinitViewElement() {
		android.content.res.Configuration newConfig = context.getResources().getConfiguration();
		Activity view = (Activity) context;
		if (newConfig.orientation == android.content.res.Configuration.ORIENTATION_PORTRAIT) {
			Log.i(TAG, "ORIENTATION_PORTRAIT");
			view.findViewById(R.id.spicture_chooser_portrait).setVisibility(View.GONE);
			scroller = view.findViewById(R.id.horizontalGallery);
			scroller.setVisibility(View.VISIBLE);
			if (landscapeChooser == null) {
				Log.i(TAG, "init landscapeChooser");
				landscapeChooser = new HorizontalChooser(context);
				sChooser = landscapeChooser;
			}
			else {
				Log.i(TAG, "reinit landscapeChooser");
				sChooser = landscapeChooser;
			}
		}
		else if (newConfig.orientation == android.content.res.Configuration.ORIENTATION_LANDSCAPE) {
			Log.i(TAG, "ORIENTATION_LANDSCAPE");
			view.findViewById(R.id.horizontalGallery).setVisibility(View.GONE);
			scroller = view.findViewById(R.id.spicture_chooser_portrait);
			scroller.setVisibility(View.VISIBLE);
			if (portraitChooser == null) {
				Log.i(TAG, "init portraitChooser");
				portraitChooser = new VerticalChooser(context, null);
				sChooser = portraitChooser;
			}
			else {
				Log.i(TAG, "reinit portraitChooser");
				sChooser = portraitChooser;
			}
		}
		sChooser.setOnChooseListener(this);

		//很奇怪如果不重新用代码设置，imageview将充不满父linearlayout空间
		LinearLayout.LayoutParams params = (android.widget.LinearLayout.LayoutParams) view.findViewById(R.id.spicture_imageview).getLayoutParams();
		params.width = LinearLayout.LayoutParams.MATCH_PARENT;
		params.height = LinearLayout.LayoutParams.MATCH_PARENT;
	}

	private void refreshPage() {

		Log.i(TAG, "refreshPage");
		controller.recycleResource();

		if (currentShowMode == SHOW_MODE_FOLDER) {
			Log.i(TAG, "currentShowMode folder");
			loadCurFolderPictures();
		}
		else if (currentShowMode == SHOW_MODE_RANDOM) {
			Log.i(TAG, "currentShowMode random");
			loadRandomPictures();
		}
		else if (currentShowMode == SHOW_MODE_ORDER) {
			Log.i(TAG, "currentShowMode order");
			loadPicturesFromOrder();
		}
	}

	private void loadCurFolderPictures() {
		Log.i(TAG, "loadCurFolderPictures");
		progressDialog = new ProgressDialog(context);
		progressDialog.setMessage(context.getResources().getString(R.string.loading));
		progressDialog.show();
		controller.loadChooserItems(SHOW_MODE_FOLDER);
	}
	private void loadRandomPictures() {
		Log.i(TAG, "loadRandomPictures");
		progressDialog = new ProgressDialog(context);
		progressDialog.setMessage(context.getResources().getString(R.string.loading));
		progressDialog.show();
		controller.loadChooserItems(SHOW_MODE_RANDOM);
	}

	private void loadPicturesFromOrder() {
		Log.i(TAG, "loadPicturesFromOrder");
		progressDialog = new ProgressDialog(context);
		progressDialog.setMessage(context.getResources().getString(R.string.loading));
		progressDialog.show();
		controller.loadChooserItems(SHOW_MODE_ORDER);
	}

	@Override
	public boolean handleMessage(Message msg) {

		switch (msg.what) {
			case Constants.STATUS_LOAD_CHOOSERITEM_FINISH://load gallery
				handleLoadChooserItem();
				break;

			case AutoPlayController.AUTO_SPECIFIED_LIST://auto play
				handleAutoPlay(msg);
				break;

		}
		progressDialog.cancel();
		return true;
	}

	private void handleAutoPlay(Message msg) {

		Bundle bundle = msg.getData();
		String finish = bundle.getString("autoplay_finish");
		if (finish != null && finish.equals("true")) {
			stopAutoPlay();
			Toast.makeText(context, R.string.spicture_autoplay_finish, Toast.LENGTH_LONG).show();
			return;
		}
		int index = bundle.getInt("autoplay_index");
		boolean scroll = bundle.getBoolean("autoplay_scroll");
		playImageAt(index, scroll);

		if (SettingProperties.isShowAnimation(context)) {
			if (showGifView.getVisibility() == View.VISIBLE) {
				showGifView.startAnimation(autoPlayController.randomAnimation());
			}
			else {
				showView.startAnimation(autoPlayController.randomAnimation());
			}
		}
	}

	private void handleLoadChooserItem() {
		Log.i(TAG, "handleLoadChooserItem");

		if (SettingProperties.isLoadAsRandom(context)) {
			shuffleList();
		}
		else {
			//PictureManager.getInstance().changeMaxChooserItem(controller.getFileNameList().size());
			sChooser.updateList(controller.getFileNameList());
			sChooser.notifyAdapterRefresh();
		}
		chooseImage(0);
	}

	public void playImageAt(final int index, boolean scroll) {
		Log.i(TAG, "playImageAt " + index + " scroll = " + scroll);
		if (scroll) {
			// scroller.scrollTo(itemWidth * chosenIndex, 0);//scrollTo需要单独线程进行
			new Handler().postDelayed((new Runnable() {
				@Override
				public void run() {
					int itemWidth = (int) context.getResources().getDimensionPixelSize(R.dimen.spicture_chooser_item_width);
					int xToCenter = (ScreenInfor.getWidth((Activity)context) - itemWidth)/2;
					int itemX = itemWidth * index;
					int maxWidth = itemWidth * (controller.getFileNameList().size() + 1);
					if (itemX <= xToCenter) {

					}
					else {
						if (itemX > maxWidth - ScreenInfor.getWidth((Activity)context)) {
							scroller.scrollTo(maxWidth - ScreenInfor.getWidth((Activity)context), 0);
						}
						else {
							scroller.scrollTo(itemX - xToCenter, 0);
						}
					}
					chooseImage(index);
					lastChosenItem = (ImageView) sChooser.getChildAt(index);
					if (lastChosenItem != null) {
						lastChosenItem.setImageResource(R.drawable.gallery_border_choose);
					}
				}
			}), 5);
		}
		else {
			chooseImage(index);
		}
	}

	@Override
	public void changeBackground(Bitmap bitmap) {
		//background = bitmap;
	}

	private void shuffleList() {
		controller.shuffleList(currentShowMode);
		sChooser.updateList(controller.getFileNameList());
		sChooser.notifyAdapterRefresh();
	}

//	private void setBackground(Bitmap bitmap) {
//		Log.i(TAG, "setBackground");
//		if (currentView == VIEW_GALLERY) {
//			if (background == null) {
//				galleryLayout.setBackgroundColor(Color.WHITE);
//			} else {
//				galleryLayout.setBackground(new BitmapDrawable(context
//						.getResources(), background));
//			}
//		} else if (currentView == VIEW_GRID) {
//			if (background == null) {
//				gridLayout.setBackgroundColor(Color.WHITE);
//			} else {
//				gridLayout.setBackground(new BitmapDrawable(context
//						.getResources(), background));
//			}
//		}

//	}

	private void listenerChoose(View view, int position) {
		onGalleryItemSelectStatus(view, position);
		chooseImage(position);
	}

	private void listenerLongTouch(View view, int position) {
		Log.i(TAG, "listenerLongTouch " + position);
		listenerChoose(view, position);//change to select status

		final int pos = position;
		String[] arrays = null;
		if (currentShowMode == SHOW_MODE_ORDER) {
			arrays = context.getResources().getStringArray(R.array.spicture_orderview_longclick);
		}
		else {
			arrays = context.getResources().getStringArray(R.array.spicture_longclick);
		}
		new AlertDialog.Builder(context)
				.setItems(arrays, new DialogInterface.OnClickListener() {

					@Override
					public void onClick(DialogInterface dialog, int which) {
						if (which == 0) {//add to order
							Log.i(TAG, "listenerLongTouch choose addtoorder");
							if (controller.isImageExist(pos)) {
								openOrderChooserToAddItem(pos);
							}
						}
						else if (which == 1) {//set as cover
							Log.i(TAG, "listenerLongTouch choose setascover");
							if (controller.isImageExist(pos)) {
								openOrderChooserToSetCover(pos);
							}
						}
						else if (which == 2) {//view details
							Log.i(TAG, "listenerLongTouch choose viewdetail");
							if (controller.isImageExist(pos)) {
								viewDetails(pos);
							}
						}
						else if (which == 3) {//delete from order
							Log.i(TAG, "listenerLongTouch choose delfromorder");
							deleteItemFromOrder(pos);
						}
					}
				}).show();
	}

	@Override
	public void onChoose(View view, int position) {
		listenerChoose(view, position);
	}

	@Override
	public void onLongTouch(View view, int position) {
		listenerLongTouch(view, position);
	}

	@Override
	public boolean onItemLongClick(AdapterView<?> parent, View view,
								   int position, long id) {
		listenerLongTouch(view, position);
		return false;
	}

	@Override
	public void onItemClick(AdapterView<?> parent, View view, int position,
							long id) {
		listenerChoose(view, position);
	}


	///////////////////////////////////////////////////HorizontalGallery.OnItemSelectListener
	@Override
	public void onGalleryItemClick(View view, int position) {
		chooseImage(position);
	}

	@Override
	public void onGalleryItemLongClick(View view, int position) {
		listenerLongTouch(view, position);
	}

	@Override
	public void onGalleryItemSelectStatus(View view, int position) {
		if (lastChosenItem != null) {
			lastChosenItem.setImageDrawable(null);
		}
		ImageView iv = (ImageView) view;
		if (Application.isLollipop()) {
			iv.setImageResource(R.drawable.gallery_border_choose_l);
		}
		else {
			iv.setImageResource(R.drawable.gallery_border_choose);
		}
		lastChosenItem = iv;
	}
	//////////////////////////////////////////////////////////////////////////////////////////////////////

	@Override
	public void onResume() {
		Log.i(TAG, "onResume");
		if (autoPlayController != null && autoPlayController.isAutoPlaying()) {
			Log.i(TAG, "onResume stopAutoPlay");
			stopAutoPlay();
		}
	}

	@Override
	public void setActionBar(TabActionBar actionBar) {
		this.actionBar = actionBar;
		changeActionBarTitle();
		prepareActionBar();
	}

	private void prepareActionBar() {
		actionBar.clearActionIcon();
		actionBar.addPlayIcon();
		actionBar.addChangeIcon();
		actionBar.addFullScreenIcon();
		actionBar.setOnIconClickListener(actionIconListener);
	}

	OnClickListener actionIconListener = new OnClickListener() {

		@Override
		public void onClick(View v) {
			switch (v.getId()) {
				case R.id.actionbar_play:
					if (autoPlayController != null && autoPlayController.isAutoPlaying()) {
						stopAutoPlay();
					}
					else {
						autoPlay();
					}
					break;
				case R.id.actionbar_change:
					currentShowMode = SHOW_MODE_RANDOM;
					loadRandomPictures();
					changeActionBarTitle();
					break;
				case R.id.actionbar_fullscreen:
					//FIXME full screen mode don't support SHOW_MODE_RANDOM currently
					if (currentShowMode == SHOW_MODE_RANDOM || currentShowMode == SHOW_MODE_RANK) {
						Toast.makeText(context, R.string.spicture_fullscreen_not_support, Toast.LENGTH_LONG).show();
						return;
					}

					Bundle bundle = new Bundle();
					if (currentShowMode == SHOW_MODE_FOLDER) {
						bundle.putInt("src_mode", FullScreenSurfActivity.SRC_MODE_FOLDER);
						bundle.putString("path", controller.getCurrentPath());
					}
					else if (currentShowMode == SHOW_MODE_ORDER) {
						bundle.putInt("src_mode", FullScreenSurfActivity.SRC_MODE_ORDER);
						bundle.putInt("orderId", controller.getCurrentOrder().getId());
					}
					Intent intent = new Intent();
					intent.putExtras(bundle);
					intent.setClass(context, FullScreenSurfActivity.class);
					context.startActivity(intent);
					break;
				default:
					break;
			}
		}

	};

	public void resetNoCurrentPath() {
		controller.setCurrentPath(null);
	}

	public void resetNoCurrentOrder() {
		controller.setCurrentOrder(null);
	}

}
