package com.king.app.fileencryption.surf;

import java.io.File;
import java.io.FilenameFilter;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

import android.app.AlertDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.graphics.Bitmap;
import android.os.Bundle;
import android.os.Handler.Callback;
import android.os.Message;
import android.view.LayoutInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.ImageView;
import android.widget.PopupMenu;
import android.widget.Toast;
import android.widget.PopupMenu.OnMenuItemClickListener;
import android.widget.RelativeLayout;
import android.widget.RelativeLayout.LayoutParams;

import com.king.app.fileencryption.R;
import com.king.app.fileencryption.controller.EncrypterFactory;
import com.king.app.fileencryption.controller.PictureManagerUpdate;
import com.king.app.fileencryption.controller.ThemeManager;
import com.king.app.fileencryption.publicview.CustomDialog;
import com.king.app.fileencryption.publicview.DefaultDialogManager;
import com.king.app.fileencryption.setting.SettingProperties;
import com.king.app.fileencryption.slidingmenu.SlidingMenuLeft;
import com.king.app.fileencryption.slidingmenu.SlidingMenuRight;
import com.king.app.fileencryption.sorder.controller.SOrderPictureBridge;
import com.king.app.fileencryption.sorder.entity.SOrder;
import com.king.app.fileencryption.sorder.view.SOrderChooserUpdate;
import com.king.app.fileencryption.spicture.controller.AutoPlayController;
import com.king.app.fileencryption.spicture.controller.SpictureController;
import com.king.app.fileencryption.thumbfolder.ShowImageDialog;
import com.king.app.fileencryption.tool.Encrypter;
import com.king.app.fileencryption.util.DisplayHelper;

/**
 * @author JingYang
 * @version create time：2016-1-26 下午3:13:28
 *
 */
public class UiController implements SurfUiAction, OnMenuItemClickListener
		, Callback{

	public static final int SRC_MODE_FOLDER = 0;
	public static final int SRC_MODE_ORDER = 1;
	public static final int SRC_MODE_RANDOM = 2;
	private int srcMode;

	private SurfActivity surfActivity;
	private ImageView deleteButton, favorateButton, playButton, moreButton
			, detailButton, coverButton, seizeButton;

	private List<String> mImageList;
	/**
	 * init from folder
	 */
	private String currentFolder;
	/**
	 * init from order
	 */
	private SOrder currentOrder;
	private int currentPosition;
	private String currentImagePath;

	private Encrypter encrypter;
	private SpictureController controller;
	private PopupMenu popupMenu;
	private AutoPlayController autoPlayController;

	public UiController(SurfActivity activity) {
		surfActivity = activity;
		surfActivity.setTheme(new ThemeManager(surfActivity).getDefaultTheme());
		DisplayHelper.enableFullScreen(surfActivity);
		DisplayHelper.disableScreenshot(surfActivity);
		DisplayHelper.keepScreenOn(surfActivity);
		encrypter = EncrypterFactory.create();

		controller = new SpictureController(activity);
		controller.setEncrpter(encrypter);
	}

	@Override
	public void loadToolbar(RelativeLayout container) {
		View view = LayoutInflater.from(surfActivity).inflate(R.layout.activity_surf_toolbar, null);
		deleteButton = (ImageView) view.findViewById(R.id.surf_toolbar_delete);
		favorateButton = (ImageView) view.findViewById(R.id.surf_toolbar_addtoorder);
		playButton = (ImageView) view.findViewById(R.id.surf_toolbar_play);
		moreButton = (ImageView) view.findViewById(R.id.surf_toolbar_more);
		detailButton = (ImageView) view.findViewById(R.id.surf_toolbar_detail);
		coverButton = (ImageView) view.findViewById(R.id.surf_toolbar_setascover);
		seizeButton = (ImageView) view.findViewById(R.id.surf_toolbar_seize);
		deleteButton.setOnClickListener(toolbarListener);
		favorateButton.setOnClickListener(toolbarListener);
		playButton.setOnClickListener(toolbarListener);
		moreButton.setOnClickListener(toolbarListener);
		detailButton.setOnClickListener(toolbarListener);
		coverButton.setOnClickListener(toolbarListener);
		seizeButton.setOnClickListener(toolbarListener);

		LayoutParams params = new LayoutParams(LayoutParams.WRAP_CONTENT, LayoutParams.WRAP_CONTENT);
		params.addRule(RelativeLayout.ALIGN_PARENT_RIGHT);
		container.addView(view, params);
	}

	@Override
	public void loadImages(Intent intent) {

		Bundle bundle = intent.getExtras();
		srcMode =  bundle.getInt("src_mode");
		if (srcMode == SRC_MODE_FOLDER) {
			currentFolder = bundle.getString("path");
			initDataFromFolder();
		}
		else if (srcMode == SRC_MODE_ORDER) {
			int orderId = bundle.getInt("orderId");
			initDataFromOlder(orderId);
		}
//		else if (srcMode == SRC_MODE_RANDOM) {
//			hideView = imageView1;
//			randomManager = new WholeRandomManager(encrypter);
//			executeWholeRandom();
//		}
	}

	@Override
	public List<String> getImageList() {
		return mImageList;
	}

	@Override
	public void onSwitchPage(int position) {
		currentPosition = position;
		currentImagePath = mImageList.get(position);
	}

	private void initDataFromFolder() {
		File file = new File(currentFolder);
		if (file.exists()) {
			mImageList = new ArrayList<String>();
			File files[] = file.listFiles(new FilenameFilter() {

				@Override
				public boolean accept(File file, String name) {

					return name.endsWith(encrypter.getFileExtra());
				}
			});
			for (File f:files) {
				mImageList.add(f.getPath());
			}
			if (mImageList.size() > 0) {
				currentImagePath = mImageList.get(0);
			}
		}
	}

	private void initDataFromOlder(int orderId) {
		SOrderPictureBridge bridge = SOrderPictureBridge.getInstance(surfActivity);
		currentOrder = bridge.queryOrder(orderId);
		bridge.getOrderItemList(currentOrder);
		mImageList = currentOrder.getImgPathList();
		if (mImageList != null && mImageList.size() > 0) {
			currentImagePath = mImageList.get(0);
		}
	}

	OnClickListener toolbarListener = new OnClickListener() {

		@Override
		public void onClick(View view) {
			if (view == deleteButton) {
				if (currentImagePath != null) {
					showDeleteWarning();
				}
			}
			else if (view == favorateButton) {
				if (currentImagePath != null) {
					openOrderChooserToAddItem();
				}
			}
			else if (view == moreButton) {
				if (currentImagePath != null) {
					showMenu();
				}
			}
			else if (view == detailButton) {
				if (currentImagePath != null) {
					viewDetails();
				}
			}
			else if (view == playButton) {
				if (currentImagePath != null) {
					surfActivity.hideToobarAndGuide();
					autoPlay();
				}
			}
			else if (view == coverButton) {
				if (currentImagePath != null) {
					openOrderChooserToSetCover();
				}
			}
			else if (view == seizeButton) {
				if (encrypter.isGifFile(currentImagePath)) {
					Toast.makeText(surfActivity, R.string.surf_seize_not_support, Toast.LENGTH_SHORT).show();
				}
				else {
					if (currentImagePath != null) {
						ShowImageDialog dialog = new ShowImageDialog(surfActivity, null, 0);
						dialog.setImagePath(currentImagePath);
						dialog.setStartWithCrop();
						dialog.show();
					}
				}
			}
		}
	};

	protected void showMenu() {
		if (popupMenu == null) {
			popupMenu = new PopupMenu(surfActivity, moreButton);
			popupMenu.getMenuInflater().inflate(R.menu.surf_gallery, popupMenu.getMenu());
			popupMenu.setOnMenuItemClickListener(this);
		}
		popupMenu.show();
	}

	protected void showDeleteWarning() {
		new DefaultDialogManager().showWarningActionDialog(surfActivity
				, surfActivity.getResources().getString(R.string.filelist_delete_msg)
				, surfActivity.getResources().getString(R.string.ok)
				, null
				, new DialogInterface.OnClickListener() {

					@Override
					public void onClick(DialogInterface dialog, int which) {
						if (which == DialogInterface.BUTTON_POSITIVE) {
							if (srcMode == SRC_MODE_ORDER) {
								deleteItemFromOrder();
							}
							else if (srcMode == SRC_MODE_FOLDER) {
								deleteItemFromFolder();
							}
						}
					}
				});
	}

	private void openOrderChooserToSetCover() {
		SOrderChooserUpdate chooser = new SOrderChooserUpdate(surfActivity, new CustomDialog.OnCustomDialogActionListener() {

			@Override
			public boolean onSave(Object object) {
				if (object != null) {
					SOrder order = (SOrder) object;
					String msg = null;
					String itemPath = currentImagePath;
					if (itemPath != null) {
						order.setCoverPath(itemPath);
						if (controller.setOrderCover(order)) {
							msg = surfActivity.getResources().getString(R.string.spicture_myorders_set_cover_ok);
						}
						else {
							msg = surfActivity.getResources().getString(R.string.spicture_myorders_set_cover_fail);
						}
						if (order.getName() != null) {
							msg = msg.replace("%s", order.getName());
						}
					}
					else {
						msg = surfActivity.getResources().getString(R.string.login_pwd_error);
					}
					Toast.makeText(surfActivity, msg, Toast.LENGTH_LONG).show();
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
		chooser.setTitle(surfActivity.getResources().getString(R.string.set_as_cover));
		chooser.show();

	}

	private void deleteItem(boolean deleted) {
		String msg = null;
		if (deleted) {
			msg = surfActivity.getResources().getString(R.string.surf_delete_success);
			mImageList.remove(currentPosition);

			int removeIndex = currentPosition;
			if (currentPosition == mImageList.size()) {
				currentPosition --;
				currentImagePath = null;
			}
			else {
				onSwitchPage(currentPosition);
			}
			surfActivity.onPageDeleted(removeIndex, currentPosition);
		}
		else {
			msg = surfActivity.getResources().getString(R.string.surf_delete_fail);
		}

		Toast.makeText(surfActivity, msg, Toast.LENGTH_LONG).show();
	}

	private void deleteItemFromOrder() {
		controller.setCurrentOrder(currentOrder);
		boolean result = controller.deleteItemFromOrder(currentPosition);
		deleteItem(result);
	}

	private void deleteItemFromFolder() {
		controller.deleteItemFromFolder(mImageList.get(currentPosition));
		deleteItem(true);
	}

	private void viewDetails() {
		String filePath = currentImagePath;
		if (filePath != null) {
			File file = new File(filePath);
			if (file.exists()) {
				new DefaultDialogManager().openDetailDialog(surfActivity, file);
			}
		}
	}

	private void openOrderChooserToAddItem() {
		SOrderChooserUpdate chooser = new SOrderChooserUpdate(surfActivity, new CustomDialog.OnCustomDialogActionListener() {

			@Override
			public boolean onSave(Object object) {
				if (object != null) {
					final SOrder order = (SOrder) object;
					final String itemPath = currentImagePath;
					if (itemPath != null) {
						if (controller.isItemExist(itemPath, order.getId())) {
							String title = surfActivity.getResources().getString(R.string.spicture_myorders_item_exist);
							title = String.format(title, order.getName());
							new AlertDialog.Builder(surfActivity)
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
						Toast.makeText(surfActivity, R.string.login_pwd_error, Toast.LENGTH_LONG).show();
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
		chooser.setTitle(surfActivity.getResources().getString(R.string.add_to_order));
		chooser.show();
	}

	private void addToOrder(String path, SOrder order) {
		String msg = null;
		if (controller.addItemToOrder(path, order)) {
			msg = surfActivity.getResources().getString(R.string.spicture_myorders_add_ok);
		}
		else {
			msg = surfActivity.getResources().getString(R.string.spicture_myorders_add_fail);
		}
		if (order.getName() != null) {
			msg = msg.replace("%s", order.getName());
		}
		Toast.makeText(surfActivity, msg, Toast.LENGTH_LONG).show();
	}

	@Override
	public boolean onMenuItemClick(MenuItem item) {
		switch (item.getItemId()) {
			case R.id.menu_slidingmenu_left:
				SettingProperties.savePreference(surfActivity, SlidingMenuLeft.BK_KEY, currentImagePath);
				break;
			case R.id.menu_slidingmenu_right:
				SettingProperties.savePreference(surfActivity, SlidingMenuRight.BK_KEY, currentImagePath);
				break;
			case R.id.menu_slidingmenu_left_land:
				SettingProperties.savePreference(surfActivity, SlidingMenuLeft.BK_KEY + "_landscape", currentImagePath);
				break;
			case R.id.menu_slidingmenu_right_land:
				SettingProperties.savePreference(surfActivity, SlidingMenuRight.BK_KEY + "_landscape", currentImagePath);
				break;
			default:
				break;
		}
		return true;
	}

	protected void autoPlay() {

		if (autoPlayController == null) {
			autoPlayController = new AutoPlayController(surfActivity, this);
		}
		if (stopAutoPlay()) {
			return;
		}

		if (srcMode == SRC_MODE_RANDOM) {
			playButton.setImageResource(R.drawable.actionbar_stop);
			autoPlayController.startWholeRandomAutoPlay(SettingProperties.getAnimationSpeed(surfActivity));
		}
		else {
			autoPlayController.setFileNameList(mImageList);
			if (autoPlayController.canPlay()) {
				playButton.setImageResource(R.drawable.actionbar_stop);
				autoPlayController.startAutoPlay(SettingProperties.getAnimationSpeed(surfActivity));
			}
			else {
				String msg = surfActivity.getResources().getString(R.string.spicture_autoplay_tooless);
				msg = String.format(msg, SettingProperties.getMinNumberToPlay(surfActivity));
				Toast.makeText(surfActivity, msg, Toast.LENGTH_LONG).show();
			}
		}
	}

	@Override
	public boolean stopAutoPlay() {
		if (autoPlayController != null && autoPlayController.isAutoPlaying()) {
			autoPlayController.stopAutoPlay();
			playButton.setImageResource(R.drawable.actionbar_play);
			surfActivity.showToobarAndGallery();
			return true;
		}
		return false;
	}

	@Override
	public boolean handleMessage(Message msg) {
		if (msg.what == AutoPlayController.AUTO_SPECIFIED_LIST) {
			surfActivity.playPage(currentPosition);
			onSwitchPage(currentPosition);
			currentPosition ++;
			if (currentPosition == mImageList.size()) {
				currentPosition --;
				stopAutoPlay();
				Toast.makeText(surfActivity, R.string.spicture_autoplay_finish, Toast.LENGTH_LONG).show();
			}
		}
		return false;
	}

}
