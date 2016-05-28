package com.king.app.fileencryption.wall.update;

import java.io.File;
import java.io.FilenameFilter;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;

import com.king.app.fileencryption.R;
import com.king.app.fileencryption.controller.EncrypterFactory;
import com.king.app.fileencryption.controller.PictureManagerUpdate;
import com.king.app.fileencryption.controller.ThemeManager;
import com.king.app.fileencryption.controller.WholeRandomManager;
import com.king.app.fileencryption.data.Constants;
import com.king.app.fileencryption.publicview.ActionBar;
import com.king.app.fileencryption.publicview.CustomDialog;
import com.king.app.fileencryption.publicview.DefaultDialogManager;
import com.king.app.fileencryption.publicview.ActionBar.ActionBarListener;
import com.king.app.fileencryption.setting.SettingProperties;
import com.king.app.fileencryption.sorder.controller.SOrderPictureBridge;
import com.king.app.fileencryption.sorder.entity.SOrder;
import com.king.app.fileencryption.sorder.view.SOrderChooserUpdate;
import com.king.app.fileencryption.spicture.controller.SpictureController;
import com.king.app.fileencryption.surf.SurfActivity;
import com.king.app.fileencryption.surf.UiController;
import com.king.app.fileencryption.thumbfolder.ShowImageDialog;
import com.king.app.fileencryption.tool.SimpleEncrypter;
import com.king.app.fileencryption.util.DisplayHelper;
import com.king.app.fileencryption.util.ScreenUtils;
import com.king.app.fileencryption.wall.WallController;
import com.king.app.fileencryption.wall.update.NewWallAdapter.OnWallItemListener;
import com.king.app.fileencryption.waterfall.WaterFallActivity;

import android.app.Activity;
import android.app.AlertDialog;
import android.app.ProgressDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.DialogInterface.OnClickListener;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.os.Handler.Callback;
import android.support.v7.widget.DefaultItemAnimator;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.StaggeredGridLayoutManager;
import android.util.Log;
import android.util.SparseBooleanArray;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.MotionEvent;
import android.view.View;
import android.widget.CheckBox;
import android.widget.CompoundButton;
import android.widget.ImageView;
import android.widget.RadioButton;
import android.widget.Toast;
import android.widget.CompoundButton.OnCheckedChangeListener;
import android.widget.PopupMenu.OnMenuItemClickListener;

/**
 * @author JingYang
 * @version create time：2016-1-29 下午4:30:16
 *
 */
public class NewWallActivity extends Activity implements ActionBarListener, Callback
		, OnMenuItemClickListener, android.view.View.OnClickListener, OnWallItemListener {

	public static final int MODE_FOLDER = 0;
	public static final int MODE_ORDER = 1;
	public static final int MODE_LIST = 2;
	public static final String MODE_KEY = "mode";
	public static final String MODE_VALUE_KEY = "value";

	/**
	 * 删除时的透明过程
	 */
	private final int TIME_GALLERY_ANIM_REMOVE = 200;
	/**
	 * 删除时的透明过程结束后后面的item向前挤压的过程
	 */
	private final int TIME_GALLERY_ANIM_MOVE = 500;

	private int currentMode;
	private SOrder currentOrder;
	private String currentPath;
	private RecyclerView wallGallery;
	private NewWallAdapter wallAdapter;
	private List<String> imagePathList;
	private WallController wallController;

	private View actionbarLayout;
	private View bottomLayout;
	private ActionBar actionBar;
	private CheckBox checkBox;
	private RadioButton fitXYButton, centerCropButton, originButton;
	private ShowImageDialog showImageDialog;
	private ProgressDialog progressDialog;
	private SpictureController spictureController;

	private int orientation;

	/**
	 * 防止多次重新触发动画
	 */
	private boolean isActionBarShow;

	/**
	 * show/hide actionbar gesture listener
	 */
	private ActionTouchListener actionTouchListener;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		setTheme(new ThemeManager(this).getDefaultTheme());
		super.onCreate(savedInstanceState);

		DisplayHelper.enableFullScreen(this);
		DisplayHelper.disableScreenshot(this);

		setContentView(R.layout.layout_wall_update);

		orientation = getResources().getConfiguration().orientation;
		isActionBarShow = true;

		actionBar = new ActionBar(this, this);
		actionBar.setBackgroundColor(getResources().getColor(R.color.actionbar_bk_wallgalerry));
		setActionbarNormal();

		bottomLayout = findViewById(R.id.bottombar);
		actionbarLayout = findViewById(R.id.actionbar);

		wallGallery = (RecyclerView) findViewById(R.id.wall_gallery);

		//用view绑定的onTouchListener不好用，因为会和其他touch事件冲突，导致在listener中往往收不到ACTION_UP事件
		//改用Activity的dispatchTouchEvent来保证手势show/hide action bar始终有效
//		wallGallery.setOnTouchListener(new GalleryTouchListener());
		actionTouchListener = new ActionTouchListener();

		int row = getResources().getInteger(R.integer.wall_rows);
		StaggeredGridLayoutManager layoutManager = new StaggeredGridLayoutManager(
				row, StaggeredGridLayoutManager.HORIZONTAL);
		wallGallery.setLayoutManager(layoutManager);
		DefaultItemAnimator animator = new DefaultItemAnimator();
		animator.setRemoveDuration(TIME_GALLERY_ANIM_REMOVE);
		animator.setMoveDuration(TIME_GALLERY_ANIM_MOVE);
		wallGallery.setItemAnimator(animator);

		wallController = new WallController(this);

		Bundle bundle = getIntent().getExtras();
		currentMode = bundle.getInt(MODE_KEY);
		if (currentMode == MODE_FOLDER) {
			currentPath = bundle.getString(MODE_VALUE_KEY);
			initFromFolder(currentPath);
		}
		else if (currentMode == MODE_ORDER) {
			int id = bundle.getInt(MODE_VALUE_KEY);
			initFromOrder(id);
		}
		else if (currentMode == MODE_LIST) {
			imagePathList = bundle.getStringArrayList(MODE_VALUE_KEY);
		}
		changeActionbarTitle();

		fitXYButton = (RadioButton) findViewById(R.id.wall_image_fitxy);
		centerCropButton = (RadioButton) findViewById(R.id.wall_image_centercrop);
		originButton = (RadioButton) findViewById(R.id.wall_image_origin);
		fitXYButton.setOnClickListener(this);
		centerCropButton.setOnClickListener(this);
		originButton.setOnClickListener(this);
		fitXYButton.setChecked(true);

		initGallery();

		checkBox = (CheckBox) findViewById(R.id.wall_show_file_name);
		checkBox.setOnCheckedChangeListener(new OnCheckedChangeListener() {

			@Override
			public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
				wallAdapter.setShowName(isChecked);
				wallAdapter.notifyDataSetChanged();
			}
		});

		spictureController = new SpictureController(this);

		showImageDialog = new ShowImageDialog(this, null, 0);
	}

	private void computeActionbarLayout() {
		if (getResources().getConfiguration().orientation == android.content.res.Configuration.ORIENTATION_LANDSCAPE) {
			actionBar.onLandscape();
		}
		else {
			actionBar.onVertical();
		}
	}

	private void setActionbarNormal() {
		actionBar.clearActionIcon();
		actionBar.addMenuIcon();
		actionBar.addRefreshIcon();
		actionBar.addChangeIcon();
//		actionBar.addRandomChangeIcon();
		actionBar.addFullScreenIcon();
		computeActionbarLayout();
	}

	private void setActionbarSelectMode() {
		actionBar.clearActionIcon();
		actionBar.addMenuIcon();
		actionBar.addDeleteIcon();
		computeActionbarLayout();
	}

	private void initFromFolder(String path) {
		File file = new File(path);
		if (file.exists()) {
			File[] files = file.listFiles(new FilenameFilter() {

				@Override
				public boolean accept(File dir, String filename) {

					return filename.endsWith(SimpleEncrypter.FILE_EXTRA);
				}
			});
			if (files != null && files.length > 0) {
				if (imagePathList == null) {
					imagePathList = new ArrayList<String>();
				}
				else {
					imagePathList.clear();
				}
				for (File f:files) {
					imagePathList.add(f.getPath());
				}
			}
		}
	}

	private void initFromOrder(int orderId) {
		SOrderPictureBridge bridge = SOrderPictureBridge.getInstance(this);
		currentOrder = bridge.queryOrder(orderId);
		bridge.getOrderItemList(currentOrder);
		if (imagePathList == null) {
			imagePathList = currentOrder.getImgPathList();
		}
		else {
			if (imagePathList != null) {
				imagePathList.clear();
				imagePathList.addAll(currentOrder.getImgPathList());
			}
		}
	}

	private void initGallery() {
		wallAdapter = new NewWallAdapter(this, imagePathList);
		wallAdapter.setOnWallItemListener(this);
		int row = getResources().getInteger(R.integer.wall_rows);
		int screenHeight = ScreenUtils.getScreenHeight(this);
		int itemHeight = screenHeight / row;
		int itemWidth = (int) (itemHeight * 1.5f);
		wallAdapter.setImageItemSize(itemWidth, itemHeight);

		wallGallery.setAdapter(wallAdapter);
	}

	private void changeActionbarTitle() {
		if (currentMode == MODE_ORDER) {
			actionBar.setTitle(currentOrder.getName());
		}
		else if (currentMode == MODE_FOLDER) {
			String[] array = currentPath.split("/");
			actionBar.setTitle(array[array.length - 1]);
		}
		else if (currentMode == MODE_LIST) {
			actionBar.setTitle(getResources().getString(R.string.menu_random));
		}
		else {
			actionBar.setTitle(getResources().getString(R.string.wall_wallgallery));
		}
	}

	/* v6.3.4 deprecated
	private class WallParams {
		int nRows;
		int itemWidth;
		public WallParams() {
			reload();
		}

		public void reload() {
			Resources resources = getResources();
			nRows = resources.getInteger(R.integer.wall_rows);
			itemWidth = resources.getDimensionPixelOffset(R.dimen.wall_item_image_width)
					+ resources.getDimensionPixelOffset(R.dimen.wall_item_spacing_hori);
		}
	}
	*/

	@Override
	public void onWallItemClick(View view, int position) {
		if (wallAdapter.isSelectMode()) {
			wallAdapter.setChecked(position);
		}
		else {
			showImageDialog.applyTransparentBackground();
			showImageDialog.setImagePath(imagePathList.get(position));
			showImageDialog.fitImageView();
			showImageDialog.show();
		}
	}

	/**
	 * wall item被放大后显示dialog
	 * 由于放大过程背景变暗，因此打开dialog后设置黑色背景
	 * @param path
	 */
	public void showImageWithDialog(String path) {
		showImageDialog.applyBlackBackground();
		showImageDialog.setImagePath(path);
		showImageDialog.fitImageView();
		showImageDialog.show();
	}

	@Override
	public void onWallItemLongClick(View view, int position) {
		if (!wallAdapter.isSelectMode()) {
			wallAdapter.resetMap();
			wallAdapter.setSelectMode(true);
			wallAdapter.setChecked(position);
			notifyGridViewRefresh();
			setActionbarSelectMode();
		}
	}

	@Override
	public void onBackPressed() {
		if (wallAdapter.isSelectMode()) {
			wallAdapter.setSelectMode(false);
			wallAdapter.resetMap();
			wallAdapter.disableImageRecycle();
			notifyGridViewRefresh();
			setActionbarNormal();
		}
		else if (wallAdapter.isMirrorMode()) {
			wallAdapter.closeMirror();
		}
		else {
			super.onBackPressed();
		}
	}

	@Override
	public void onConfigurationChanged(android.content.res.Configuration newConfig) {

		//wallParams.reload();
		//resizeGridView();
		if (orientation != newConfig.orientation) {
			orientation = newConfig.orientation;

			StaggeredGridLayoutManager layoutManager =
					(StaggeredGridLayoutManager) wallGallery.getLayoutManager();
			layoutManager.setSpanCount(getResources().getInteger(R.integer.wall_rows));
//			wallAdapter.resetWallRes();
//			horiGridView.reset();
//			horiGridView.setRow(getResources().getInteger(R.integer.wall_rows));
//			horiGridView.setWidth(ScreenUtils.getScreenWidth(this));
//			horiGridView.getItemWidth();
//			/**
//			 * 由于采用了重新setAdapter的方式，如果旋转前，已经滚动了一段距离，旋转后scrollview会默认scroll这么段距离
//			 * 而horiGridView内部的设计是根据scroll的位置来加载visible view的，所以要确保旋转后scroll to 0
//            **/
//			horiGridView.scrollTo(0, 0);
//			horiGridView.setAdapter(wallAdapter);

			if (showImageDialog != null) {
				showImageDialog.setOrientationChanged();
				if (showImageDialog.isShowing()) {
					showImageDialog.onConfigChange();
				}
			}
			computeActionbarLayout();
		}
		super.onConfigurationChanged(newConfig);
	}

	@Override
	public void onBack() {
		finish();
	}

	@Override
	public void onDelete() {
		if (currentMode == MODE_ORDER || currentMode == MODE_FOLDER) {
			deleteSelectedFile();
		}
	}

	private void deleteSelectedFile() {
		int checkedItemNum = wallAdapter.getCheckMap().size();
		String msg = getResources().getString(R.string.thumb_folder_warning_delete);
		msg = msg.replace("%d", "" + checkedItemNum);
		new AlertDialog.Builder(this)
				.setTitle(R.string.warning)
				.setMessage(msg)
				.setPositiveButton(R.string.ok, new OnClickListener() {

					@Override
					public void onClick(DialogInterface arg0, int arg1) {
						List<Integer> list = getSelectedList();
						int index = 0;
						//从后往前删，否则易出数组越界异常
						for (int i = list.size() - 1; i > -1; i --) {
							index = list.get(i);
							if (currentMode == MODE_FOLDER) {
								String path = imagePathList.get(index);
								Log.i("FileEncryption", "delete file " + index + " " + path);
								wallController.deleteFile(path);
							}
							else {//sorder
								wallController.deleteFile(currentOrder, index);
							}

							imagePathList.remove(index);
							//show animation
							wallAdapter.notifyRemoved(index);
						}
						if (currentMode == MODE_ORDER) {
							currentOrder.setItemNumber(currentOrder.getItemNumber() - list.size());
						}

						//restore normal status
						wallAdapter.setSelectMode(false);
						setActionbarNormal();

						//notify adapter refresh in animation ending
						new Handler().postDelayed(new Runnable() {

													  @Override
													  public void run() {
														  resetGallery();
													  }
												  }  //这里扩大时间是为了避免出现try to use recycled bitmap异常
								//这是因为adapter在onRecycle和notifyDataSetChanged以及notifyItemRemoved的时序问题引发的
								, (TIME_GALLERY_ANIM_MOVE + TIME_GALLERY_ANIM_REMOVE) * 2);
					}
				})
				.setNegativeButton(R.string.cancel, null)
				.show();
	}

	@Override
	public void onIconClick(View view) {
		switch (view.getId()) {
			case R.id.actionbar_change:
				currentMode = MODE_LIST;
				changeActionbarTitle();

				progressDialog = new ProgressDialog(this);
				progressDialog.setMessage(getResources().getString(R.string.loading));
				progressDialog.show();

				loadRandomList();
				break;

			case R.id.actionbar_fullscreen:
				//FIXME full screen mode don't support SHOW_MODE_RANDOM currently
				if (currentMode == MODE_LIST) {
					Toast.makeText(this, R.string.spicture_fullscreen_not_support, Toast.LENGTH_LONG).show();
					return;
				}

				Bundle bundle = new Bundle();
				if (currentMode == MODE_FOLDER) {
					bundle.putInt("src_mode", UiController.SRC_MODE_FOLDER);
					bundle.putString("path", currentPath);
				}
				else if (currentMode == MODE_ORDER) {
					bundle.putInt("src_mode", UiController.SRC_MODE_ORDER);
					bundle.putInt("orderId", currentOrder.getId());
				}
				Intent intent = new Intent();
				intent.putExtras(bundle);
				intent.setClass(this, SurfActivity.class);
				startActivity(intent);
				break;
			case R.id.actionbar_random_change:
				break;
			default:
				break;
		}
	}
	@Override
	public void onRefresh() {
		if (currentMode == MODE_FOLDER) {
			initFromFolder(currentPath);
			notifyGridViewRefresh();
		}
		else if (currentMode == MODE_ORDER) {
			initFromOrder(currentOrder.getId());
			notifyGridViewRefresh();
		}
	}

	@Override
	public void createMenu(MenuInflater menuInflater, Menu menu) {
		loadMenu(menuInflater, menu);
	}

	@Override
	public void onPrepareMenu(MenuInflater menuInflater, Menu menu) {
		loadMenu(menuInflater, menu);
	}

	private void loadMenu(MenuInflater menuInflater, Menu menu) {
		menu.clear();
		menuInflater.inflate(R.menu.wallgallery, menu);
		if (wallAdapter.isSelectMode()) {
			menu.findItem(R.id.menu_wall_setaswall).setVisible(false);
			int checkedItemNum = wallAdapter.getCheckMap().size();
			if (checkedItemNum == 0) {
				menu.findItem(R.id.menu_thumb_deselectall).setVisible(false);
				menu.findItem(R.id.menu_thumb_setascover).setVisible(false);
				menu.findItem(R.id.menu_thumb_viewdetail).setVisible(false);
				menu.findItem(R.id.menu_thumb_selectall).setVisible(true);
				menu.findItem(R.id.menu_thumb_deselectall).setVisible(false);
			}
			else if (checkedItemNum == 1) {
				menu.findItem(R.id.menu_thumb_addtooder).setVisible(true);
				menu.findItem(R.id.menu_thumb_setascover).setVisible(true);
				menu.findItem(R.id.menu_thumb_viewdetail).setVisible(true);
				menu.findItem(R.id.menu_thumb_selectall).setVisible(true);
				menu.findItem(R.id.menu_thumb_deselectall).setVisible(true);
			}
			else if (checkedItemNum == imagePathList.size()) {
				menu.findItem(R.id.menu_thumb_addtooder).setVisible(true);
				menu.findItem(R.id.menu_thumb_setascover).setVisible(false);
				menu.findItem(R.id.menu_thumb_viewdetail).setVisible(false);
				menu.findItem(R.id.menu_thumb_selectall).setVisible(false);
				menu.findItem(R.id.menu_thumb_deselectall).setVisible(true);
			}
			else {
				menu.findItem(R.id.menu_thumb_addtooder).setVisible(true);
				menu.findItem(R.id.menu_thumb_setascover).setVisible(false);
				menu.findItem(R.id.menu_thumb_viewdetail).setVisible(false);
				menu.findItem(R.id.menu_thumb_selectall).setVisible(true);
				menu.findItem(R.id.menu_thumb_deselectall).setVisible(true);
			}
		}
		else {
			menu.findItem(R.id.menu_thumb_addtooder).setVisible(false);
			menu.findItem(R.id.menu_thumb_deselectall).setVisible(false);
			menu.findItem(R.id.menu_thumb_setascover).setVisible(false);
			menu.findItem(R.id.menu_thumb_viewdetail).setVisible(false);
			menu.findItem(R.id.menu_thumb_selectall).setVisible(false);
			menu.findItem(R.id.menu_thumb_deselectall).setVisible(false);
			menu.findItem(R.id.menu_wall_setaswall).setVisible(false);
			if (currentMode == MODE_LIST) {
				menu.findItem(R.id.menu_thumb_waterfall).setVisible(false);
			}
			else {
				menu.findItem(R.id.menu_thumb_waterfall).setVisible(true);
			}
		}
	}

	@Override
	public OnMenuItemClickListener getMenuItemListener() {

		return this;
	}

	@Override
	public boolean onMenuItemClick(MenuItem item) {
		switch (item.getItemId()) {
			case R.id.menu_wall_setaswall:
				wallController.saveDefaultWallRes();
				break;
			case R.id.menu_thumb_selectall:
				wallAdapter.selectAll();
				wallAdapter.disableImageRecycle();
				notifyGridViewRefresh();
				break;
			case R.id.menu_thumb_deselectall:
				wallAdapter.resetMap();
				wallAdapter.disableImageRecycle();
				notifyGridViewRefresh();
				break;
			case R.id.menu_thumb_addtooder:
				openOrderChooserToAddItem(getSelectedList());
				break;
			case R.id.menu_thumb_setascover:
				int index = wallAdapter.getCheckMap().keyAt(0);
				openOrderChooserToSetCover(index);
				break;
			case R.id.menu_thumb_viewdetail:
				int position = wallAdapter.getCheckMap().keyAt(0);
				viewDetails(position);
				break;
			case R.id.menu_thumb_waterfall:
				if (currentMode == MODE_FOLDER) {
					startFileWaterFallView();
				}
				else if (currentMode == MODE_ORDER) {
					startOrderWaterFallView();
				}
				break;
			default:
				break;
		}
		return false;
	}

	private void startFileWaterFallView() {
		if (currentPath != null) {
			File file = new File(currentPath);
			if (file.list().length > Constants.WATERFALL_MIN_NUMBER) {
				Intent intent = new Intent();
				intent.setClass(this, WaterFallActivity.class);
				intent.putExtra("filePath", file.getPath());
				startActivity(intent);
			}
		}
	}

	private void startOrderWaterFallView() {
		if (currentOrder != null && currentOrder.getImgPathList() != null) {
			if (currentOrder.getImgPathList().size() > Constants.WATERFALL_MIN_NUMBER) {
				Intent intent = new Intent();
				intent.setClass(this, WaterFallActivity.class);
				intent.putExtra("order", currentOrder.getId());
				startActivity(intent);
			}
		}
	}

	private void viewDetails(int pos) {
		File file = new File(imagePathList.get(pos));
		if (file != null && file.exists()) {
			new DefaultDialogManager().openDetailDialog(this, file);
		}
	}

	private void openOrderChooserToSetCover(final int pos) {

		SOrderChooserUpdate chooser = new SOrderChooserUpdate(this, new CustomDialog.OnCustomDialogActionListener() {

			@Override
			public boolean onSave(Object object) {
				if (object != null) {
					SOrder order = (SOrder) object;
					if (imagePathList.get(pos) != null && new File(imagePathList.get(pos)).exists()) {
						order.setCoverPath(imagePathList.get(pos));
						String msg = null;
						if (spictureController.setOrderCover(order)) {
							msg = getResources().getString(R.string.spicture_myorders_set_cover_ok);
						}
						else {
							msg = getResources().getString(R.string.spicture_myorders_set_cover_fail);
						}
						if (order.getName() != null) {
							msg = msg.replace("%s", order.getName());
						}
						if (spictureController.setOrderCover(order)) {

						}
						Toast.makeText(NewWallActivity.this, msg, Toast.LENGTH_LONG).show();
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
		chooser.setTitle(getResources().getString(R.string.set_as_cover));
		chooser.show();
	}
	private void openOrderChooserToAddItem(final List<Integer> selectedList) {
		SOrderChooserUpdate chooser = new SOrderChooserUpdate(this, new CustomDialog.OnCustomDialogActionListener() {

			@Override
			public boolean onSave(Object object) {
				if (object != null) {
					final SOrder order = (SOrder) object;
					for (int index : selectedList) {
						final String itemPath = imagePathList.get(index);
						if (itemPath != null) {
							if (spictureController.isItemExist(itemPath, order.getId())) {
								String title = getResources().getString(R.string.spicture_myorders_item_exist);
								title = String.format(title, order.getName());
								new AlertDialog.Builder(NewWallActivity.this)
										.setMessage(title)
										.setPositiveButton(R.string.ok, new DialogInterface.OnClickListener() {

											@Override
											public void onClick(DialogInterface dialog, int which) {
												addToOrder(itemPath, order, true);
											}
										})
										.setNegativeButton(R.string.cancel, null)
										.show();
							}
							else {
//								boolean showPopToast = false;
//								if (index == indexList.size() - 1) {
//									showPopToast = true;
//								}
//								else {
//									showPopToast = false;
//								}
								addToOrder(itemPath, order, false);
							}
						}
						else {
							Toast.makeText(NewWallActivity.this, R.string.login_pwd_error, Toast.LENGTH_LONG).show();
						}
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
	}

	private void addToOrder(String path, SOrder order, boolean showResult) {
		String msg = null;
		if (spictureController.addItemToOrder(path, order)) {
			msg = getResources().getString(R.string.spicture_myorders_add_ok);
		}
		else {
			msg = getResources().getString(R.string.spicture_myorders_add_fail);
		}
		if (order.getName() != null) {
			msg = msg.replace("%s", order.getName());
		}
		if (showResult) {
			Toast.makeText(this, msg, Toast.LENGTH_LONG).show();
		}
	}

	protected List<Integer> getSelectedList() {
		List<Integer> list = null;
		SparseBooleanArray map = wallAdapter.getCheckMap();

		if (map.size() > 0) {
			list = new ArrayList<Integer>();
			for (int i = 0; i < map.size(); i ++) {
				list.add(map.keyAt(i));
			}
			Collections.sort(list);
		}
		return list;
	}

	@Override
	public void onTextChanged(String text, int start, int before, int count) {

	}

	private void loadRandomList() {
		new LoadRandomThread().run();
	}

	@Override
	public boolean handleMessage(Message msg) {

		resetGallery();
		progressDialog.cancel();
		return true;
	}

	private void resetGallery() {
		wallAdapter.notifyDataSetChanged();
//		wallAdapter.updatePathList(imagePathList);
//		horiGridView.setAdapter(wallAdapter);
	}

	private void notifyGridViewRefresh() {
		wallAdapter.notifyDataSetChanged();
	}

	private class LoadRandomThread extends Thread {

		private Handler handler = new Handler(NewWallActivity.this);

		@Override
		public void run() {
			if (imagePathList == null) {
				imagePathList = new ArrayList<String>();
			}
			else {
				imagePathList.clear();
			}

			WholeRandomManager manager = new WholeRandomManager(EncrypterFactory.create());
			int total = SettingProperties.getCasualLookNumber(NewWallActivity.this);
			int max = manager.getTotal();
			if (max < total) {
				total = max;
			}

			String path = null;
			int maxTry = 1;

			for (int i = 0; i < total; i ++) {
				maxTry = 1;
				path = null;
				path = manager.getRandomPath();
				while (path == null && maxTry < 5) {
					path = manager.getRandomPath();
					maxTry ++;
				}
				imagePathList.add(path);
			}

			handler.sendMessage(new Message());
		}

	}

	@Override
	public void onClick(View v) {
		if (v == fitXYButton) {
			wallAdapter.changeScaleType(ImageView.ScaleType.FIT_XY);
		}
		else if (v == centerCropButton) {
			wallAdapter.changeScaleType(ImageView.ScaleType.CENTER_CROP);
		}
		else if (v == originButton) {
			wallAdapter.changeScaleType(ImageView.ScaleType.FIT_CENTER);
		}
		notifyGridViewRefresh();
	}

	@Override
	protected void onDestroy() {
		PictureManagerUpdate.getInstance().recycleWallItems();
		super.onDestroy();
	}

	/**
	 *
	 * @author JingYang
	 * 监听显示和隐藏action bar的手势操作
	 */
	private class ActionTouchListener {
		private final int OFFSET_X = 100;
		private final int OFFSET_Y = 200;

		private float startX, startY;

		public void onTouch(MotionEvent event) {
			switch (event.getAction()) {
				case MotionEvent.ACTION_DOWN:
					startX = event.getRawX();
					startY = event.getRawY();
					break;
				case MotionEvent.ACTION_MOVE:

					break;
				case MotionEvent.ACTION_UP:
					float offsetX = event.getRawX() - startX;
					float offsetY = event.getRawY() - startY;

					if (Math.abs(offsetX) < OFFSET_X) {
						if (offsetY > OFFSET_Y) {//down show
							showActionBars();
						}
						else if (offsetY < -OFFSET_Y) {//up hide
							hideActionBars();
						}
					}
					break;

				default:
					break;
			}
		}
	}

	/**
	 * 由于视图中的view有touch事件处理，导致onTouchEvent没有响应，可能是被拦截了
	 * 只能在dispatchTouchEvent中处理一定要响应的touch操作
	 */
	@Override
	public boolean dispatchTouchEvent(MotionEvent event) {
		actionTouchListener.onTouch(event);
		return super.dispatchTouchEvent(event);
	}

	@Override
	public boolean onTouchEvent(MotionEvent event) {
		return super.onTouchEvent(event);
	}

	private void hideActionBars() {
		if (isActionBarShow) {
			actionbarLayout.startAnimation(AnimationManager.getToolbarOutAnim(this));
			bottomLayout.startAnimation(AnimationManager.getBottombarOutAnim(this));
			actionbarLayout.setVisibility(View.GONE);
			bottomLayout.setVisibility(View.GONE);
		}
		isActionBarShow = false;
	}
	private void showActionBars() {
		if (!isActionBarShow) {
			actionbarLayout.startAnimation(AnimationManager.getToolbarInAnim(this));
			bottomLayout.startAnimation(AnimationManager.getBottombarInAnim(this));
			actionbarLayout.setVisibility(View.VISIBLE);
			bottomLayout.setVisibility(View.VISIBLE);
		}
		isActionBarShow = true;
	}

}
