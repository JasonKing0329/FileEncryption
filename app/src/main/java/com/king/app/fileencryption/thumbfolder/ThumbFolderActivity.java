package com.king.app.fileencryption.thumbfolder;

import java.io.File;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

import com.king.app.fileencryption.Application;
import com.king.app.fileencryption.R;
import com.king.app.fileencryption.controller.AccessController;
import com.king.app.fileencryption.controller.PictureManagerUpdate;
import com.king.app.fileencryption.controller.ThemeManager;
import com.king.app.fileencryption.data.Configuration;
import com.king.app.fileencryption.data.Constants;
import com.king.app.fileencryption.filemanager.view.FolderDialog;
import com.king.app.fileencryption.publicview.ActionBar;
import com.king.app.fileencryption.publicview.CustomDialog;
import com.king.app.fileencryption.publicview.DefaultDialogManager;
import com.king.app.fileencryption.publicview.DragImageView;
import com.king.app.fileencryption.publicview.FullScreenSurfActivity;
import com.king.app.fileencryption.publicview.ActionBar.ActionBarListener;
import com.king.app.fileencryption.setting.SettingActivity;
import com.king.app.fileencryption.sorder.entity.SOrder;
import com.king.app.fileencryption.sorder.view.SOrderChooserUpdate;
import com.king.app.fileencryption.spicture.controller.SpictureController;
import com.king.app.fileencryption.thumbfolder.IndexView.OnIndexSelectListener;
import com.king.app.fileencryption.util.DisplayHelper;
import com.king.app.fileencryption.util.ScreenUtils;
import com.king.app.fileencryption.waterfall.WaterFallActivity;

import android.app.Activity;
import android.app.AlertDialog;
import android.app.ProgressDialog;
import android.content.DialogInterface;
import android.content.DialogInterface.OnClickListener;
import android.content.Intent;
import android.graphics.Bitmap;
import android.os.Bundle;
import android.os.Handler;
import android.os.Handler.Callback;
import android.os.Message;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;
import android.view.animation.LinearInterpolator;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.AdapterView.OnItemLongClickListener;
import android.widget.BaseAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.GridView;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.ScrollView;
import android.widget.PopupMenu.OnMenuItemClickListener;
import android.widget.TextView;
import android.widget.Toast;

public class ThumbFolderActivity extends Activity
		implements OnItemClickListener, OnItemLongClickListener, Callback
		, ActionBarListener, OnMenuItemClickListener, OnIndexSelectListener{

	private final String TAG = "ThumbFolderActivity";

	public static final int SRC_MODE_FOLDER = 0;
	public static final int SRC_MODE_ORDER = 1;
	private int srcMode;
	private SOrder currentOrder;
	private File selectedFile;

	private boolean isChooserMode;

	private LinearLayout accessPrivateLayout;
	private EditText accessPrivateEdit;
	private Button accessPrivateButton;

	private ActionBar actionBar;

	private ListView folderListView;
	private GridView gridView;
	private List<File> folderList;
	private List<SOrder> orderList;
	private List<String> imageFileList;
	private FolderListAdapter folderListAdapter;
	private GridAdapterProvider gridAdapterProvider;
	//	private PopupWindow imagePopupWidow;
	private View lastChosedFolder;
	private ProgressDialog progressDialog;
	private Controller controller;
	private SpictureController spictureController;

	private List<File> tempFileList;
	private List<SOrder> tempOrderList;

	private PictureManagerUpdate pictureManager;

	private ShowImageDialog imageDialog;
	private IndexCreator indexCreator;
	private ScrollView indexViewParent;
	private IndexView indexView;
	//private IndexStateControlView indexStateControlView;
	private DragImageView dragView;

	private MoveController moveController;
	private FolderDialog folderDialog;
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		setTheme(new ThemeManager(this).getDefaultTheme());
		super.onCreate(savedInstanceState);

		DisplayHelper.enableFullScreen(this);
		DisplayHelper.disableScreenshot(this);

		setContentView(Application.isLollipop() ? R.layout.thumbfolder_main_l : R.layout.thumbfolder_main);

		folderListView = (ListView) findViewById(R.id.thumbfolder_listview);
		gridView = (GridView) findViewById(R.id.thumbfolder_gridview);
		indexViewParent = (ScrollView) findViewById(R.id.thumbfolder_indexview_parent);
		indexView = (IndexView) findViewById(R.id.thumbfolder_indexview);
		//indexStateControlView = (IndexStateControlView) findViewById(R.id.thumbfolder_indexview_control);
		dragView = (DragImageView) findViewById(R.id.thumbfolder_indexview_control);
		gridView.setCacheColorHint(0);
		folderListView.setOnItemClickListener(this);
		folderListView.setOnItemLongClickListener(this);
		gridView.setOnItemClickListener(this);
		gridView.setOnItemLongClickListener(this);
		indexView.setOnIndexSelectListener(this);

		int size = getResources().getDimensionPixelSize(R.dimen.thumbfolder_index_control_width);
		dragView.setImageResource(R.drawable.index_control);
		dragView.fitImageSize(size, size);
		dragView.setOnClickListener(new View.OnClickListener() {

			@Override
			public void onClick(View view) {
				if (indexViewParent.getVisibility() == View.GONE) {
					indexViewParent.setVisibility(View.VISIBLE);
				}
				else {
					indexViewParent.setVisibility(View.GONE);
				}
			}
		});

		controller = new Controller(this);
		spictureController = new SpictureController(this);
		spictureController.registCallback(this);
		spictureController.setEncrpter(controller.getEncrypter());

		moveController = new MoveController(this, this);

		pictureManager = PictureManagerUpdate.getInstance();

		gridAdapterProvider = new GridAdapterProvider(this, gridView);
		indexCreator = new IndexCreator(indexView);

		isChooserMode = getIntent().getExtras().getBoolean(Constants.KEY_THUMBFOLDER_CHOOSER_MODE, false);

		actionBar = new ActionBar(this, this);
		if (!isChooserMode) {
			actionBar.addMenuIcon();
			actionBar.addGalleryIcon();
			actionBar.addRefreshIcon();
		}
		actionBar.addSearchIcon();

		if (getResources().getConfiguration().orientation == android.content.res.Configuration.ORIENTATION_LANDSCAPE) {
			actionBar.onLandscape();
		}
		else {
			actionBar.onVertical();
		}
		loadFolderList();

		//it's better to write at last, cause previous initialization may cost a long time
		//it make sure that the delay time can wait for the right position of indexView's
		initIndexStateController();
	}

	private void initIndexStateController() {

		//notice use indexViewParent or indexView
		//position should use indexView, but visibility should base on its parent
		if (indexViewParent.getVisibility() != View.VISIBLE) {//when change orientation need consider about this
			//show in the right-bottom corner of screen
			dragView.setPosition(ScreenUtils.getScreenWidth(this) - dragView.getImageWidth() - 100
					, ScreenUtils.getScreenHeight(this) - dragView.getImageWidth() - 100);
			return;
		}
		/**
		 * this is called in onCreate, so indexView's position is still unknown
		 * so make delay to wait it display, then the position would be available
		 */
		new Handler().postDelayed(new Runnable() {

			@Override
			public void run() {
				int pos[] = new int[2];
				indexView.getLocationOnScreen(pos);//position should use indexView
				int offset = dragView.getImageWidth();
				dragView.setPosition(pos[0] - offset, pos[1]);
				dragView.setVisibility(View.VISIBLE);
			}
		}, 200);
	}

	View.OnTouchListener touchListener = new View.OnTouchListener() {

		@Override
		public boolean onTouch(View view, MotionEvent event) {
			switch (event.getAction()) {
				case MotionEvent.ACTION_MOVE:

					break;

				default:
					break;
			}
			return false;
		}
	};

	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		loadMenu(getMenuInflater(), menu);
		return true;
	}

	@Override
	public boolean onPrepareOptionsMenu(Menu menu) {
		loadMenu(getMenuInflater(), menu);
		return true;
	}

	@Override
	public boolean onMenuItemClick(MenuItem item) {
		onOptionsItemSelected(item);
		return true;
	}

	private void loadMenu(MenuInflater menuInflater, Menu menu) {
		menu.clear();
		if (gridAdapterProvider.isActionMode()) {
			menuInflater.inflate(R.menu.thumbfolder_actionmode, menu);
			int checkedItemNum = gridAdapterProvider.getCheckedNum();
			if (checkedItemNum == 0) {
				menu.findItem(R.id.menu_thumb_addtooder).setVisible(false);
				menu.findItem(R.id.menu_thumb_setascover).setVisible(false);
				menu.findItem(R.id.menu_thumb_viewdetail).setVisible(false);
				menu.findItem(R.id.menu_thumb_delete).setVisible(false);
				menu.findItem(R.id.menu_thumb_selectall).setVisible(true);
				menu.findItem(R.id.menu_thumb_deselectall).setVisible(false);
			}
			else if (checkedItemNum == 1) {
				menu.findItem(R.id.menu_thumb_addtooder).setVisible(true);
				menu.findItem(R.id.menu_thumb_setascover).setVisible(true);
				menu.findItem(R.id.menu_thumb_viewdetail).setVisible(true);
				menu.findItem(R.id.menu_thumb_delete).setVisible(true);
				menu.findItem(R.id.menu_thumb_selectall).setVisible(true);
				menu.findItem(R.id.menu_thumb_deselectall).setVisible(true);
			}
			else if (checkedItemNum == imageFileList.size()) {
				menu.findItem(R.id.menu_thumb_addtooder).setVisible(true);
				menu.findItem(R.id.menu_thumb_setascover).setVisible(false);
				menu.findItem(R.id.menu_thumb_viewdetail).setVisible(false);
				menu.findItem(R.id.menu_thumb_delete).setVisible(true);
				menu.findItem(R.id.menu_thumb_selectall).setVisible(false);
				menu.findItem(R.id.menu_thumb_deselectall).setVisible(true);
			}
			else {
				menu.findItem(R.id.menu_thumb_addtooder).setVisible(true);
				menu.findItem(R.id.menu_thumb_setascover).setVisible(false);
				menu.findItem(R.id.menu_thumb_viewdetail).setVisible(false);
				menu.findItem(R.id.menu_thumb_delete).setVisible(true);
				menu.findItem(R.id.menu_thumb_selectall).setVisible(true);
				menu.findItem(R.id.menu_thumb_deselectall).setVisible(true);
			}
		}
		else {
			menuInflater.inflate(R.menu.thumbfolder, menu);
		}
	}

	private void loadFolderList() {

		Bundle bundle = getIntent().getExtras();
		srcMode = bundle.getInt(Constants.KEY_THUMBFOLDER_INIT_MODE);

		if (srcMode == SRC_MODE_FOLDER) {
			actionBar.setTitle(getResources().getString(R.string.thumb_folder_allfolder));
			tempFileList = new ArrayList<File>();

			folderList = controller.getAllFolders();

			if (folderList != null) {
				for (int j = 0; j < folderList.size(); j ++) {
					tempFileList.add(folderList.get(j));
				}
			}
			indexCreator.createFromFileList(tempFileList);
			indexViewParent.setVisibility(View.VISIBLE);
		}
		else if (srcMode == SRC_MODE_ORDER) {
			actionBar.setTitle(getResources().getString(R.string.thumb_folder_allolder));
			tempOrderList = new ArrayList<SOrder>();
			orderList = controller.loadOrderList();
			if (orderList != null) {
				for (int j = 0; j < orderList.size(); j ++) {
					tempOrderList.add(orderList.get(j));
				}
			}
			indexCreator.createFromOrderList(tempOrderList);
			indexViewParent.setVisibility(View.VISIBLE);
		}
		if (folderListAdapter == null) {
			folderListAdapter = new FolderListAdapter();
		}
		folderListView.setAdapter(folderListAdapter);
	}

	@Override
	public boolean onOptionsItemSelected(MenuItem item) {
		int position = -1;
		switch (item.getItemId()) {
			case R.id.menu_thumb_addtooder:
				openOrderChooserToAddItem(gridAdapterProvider.getSelectedList());
				break;
			case R.id.menu_thumb_setascover:
				position = gridAdapterProvider.getCheckedPosition();
				openOrderChooserToSetCover(position);
				break;
			case R.id.menu_thumb_viewdetail:
				position = gridAdapterProvider.getCheckedPosition();
				viewDetails(position);
				break;
			case R.id.menu_move_to_folder:
				openFolderDialog();
				break;
			case R.id.menu_thumb_delete:
				deleteSelectedFile();
				break;
			case R.id.menu_thumb_selectall:
				gridAdapterProvider.selectAll();
				break;
			case R.id.menu_thumb_deselectall:
				gridAdapterProvider.deSelectAll();
				break;
			case R.id.menu_thumb_waterfall:
				if (srcMode == SRC_MODE_FOLDER) {
					startFileWaterFallView();
				}
				else {
					startOrderWaterFallView();
				}
				break;
			case R.id.menu_thumb_setting:
				startSettingView();
				break;
			default:
				break;
		}
		return super.onOptionsItemSelected(item);
	}

	private void openFolderDialog() {
		folderDialog = new FolderDialog(this,
				new CustomDialog.OnCustomDialogActionListener() {

					@Override
					public boolean onSave(Object object) {
						final File targetFile = (File) object;
						List<Integer> list = gridAdapterProvider.getSelectedList();
						if (list != null && list.size() > 0) {
							List<String> pathList = new ArrayList<String>();
							for (int i = 0; i < list.size(); i ++) {
								pathList.add(imageFileList.get(list.get(i)));
							}
							final List<String> pList = pathList;

							moveController.showProgress();
							new Thread() {
								public void run() {
									moveController.moveToFolder(pList, targetFile, moveController.getHandler());
								}
							}.start();
						}
						return false;
					}

					@Override
					public void onLoadData(HashMap<String, Object> data) {
						data.put(Constants.KEY_FOLDERDLG_ROOT, Configuration.APP_DIR_IMG);
					}

					@Override
					public boolean onCancel() {
						// TODO Auto-generated method stub
						return false;
					}
				});
		folderDialog.setTitle(getResources().getString(R.string.move_to_folder));
		folderDialog.show();
	}

	private void startSettingView() {
		startActivity(new Intent().setClass(ThumbFolderActivity.this, SettingActivity.class));
	}

	private void startFileWaterFallView() {
		if (selectedFile != null) {
			if (selectedFile.list().length > Constants.WATERFALL_MIN_NUMBER) {
				Intent intent = new Intent();
				intent.setClass(ThumbFolderActivity.this, WaterFallActivity.class);
				intent.putExtra("filePath", selectedFile.getPath());
				startActivity(intent);
			}
		}
		else {
			startBrowser();
		}
	}

	private void startBrowser() {
		Intent intent = getPackageManager().getLaunchIntentForPackage("com.king.app.browser");
		intent.setAction(Intent.ACTION_MAIN);
		intent.addCategory(Intent.CATEGORY_LAUNCHER);
		startActivity(intent);
	}
	private void startOrderWaterFallView() {
		if (currentOrder != null && currentOrder.getImgPathList() != null) {
			if (currentOrder.getImgPathList().size() > Constants.WATERFALL_MIN_NUMBER) {
				Intent intent = new Intent();
				intent.setClass(ThumbFolderActivity.this, WaterFallActivity.class);
				intent.putExtra("order", currentOrder.getId());
				startActivity(intent);
			}
		}
		else {
			startBrowser();
		}
	}

	private void deleteSelectedFile() {
		int checkedItemNum = gridAdapterProvider.getCheckedNum();
		String msg = getResources().getString(R.string.thumb_folder_warning_delete);
		msg = msg.replace("%d", "" + checkedItemNum);
		new AlertDialog.Builder(this)
				.setTitle(R.string.warning)
				.setMessage(msg)
				.setPositiveButton(R.string.ok, new OnClickListener() {

					@Override
					public void onClick(DialogInterface arg0, int arg1) {
						List<Integer> list = gridAdapterProvider.getSelectedList();
						int index = 0;
						//从后往前删，否则易出数组越界异常
						for (int i = list.size() - 1; i > -1; i --) {
							index = list.get(i);
							if (srcMode == SRC_MODE_FOLDER) {
								controller.deleteItemFromFolder(imageFileList.get(index));
							}
							else {//sorder
								controller.deleteItemFromOrder(currentOrder, index);
							}

							gridAdapterProvider.removeImage(index);
							imageFileList.remove(index);
						}
						if (srcMode == SRC_MODE_ORDER) {
							currentOrder.setItemNumber(currentOrder.getItemNumber() - list.size());
							refreshFolderList();
						}
						gridAdapterProvider.showActionMode(false);
						gridAdapterProvider.refresh(true);
					}
				})
				.setNegativeButton(R.string.cancel, null)
				.show();
	}

	@Override
	public void onItemClick(AdapterView<?> parent, View view, int position,
							long id) {
//		hidePopupWindow();
		if (parent == folderListView) {

			if (lastChosedFolder == null) {
				lastChosedFolder = view;
			}
			else {
				if (view != lastChosedFolder) {
					if (gridAdapterProvider.isActionMode()) {
						gridAdapterProvider.showActionMode(false);
					}
				}
				lastChosedFolder.setBackground(null);
			}
			view.setBackgroundResource(R.drawable.gallery_border_choose);
			lastChosedFolder = view;

			view.findViewById(R.id.thumb_folder_item_image).startAnimation(getFolderAnimation());

			if (srcMode == SRC_MODE_FOLDER) {
				selectedFile = tempFileList.get(position);
				if (selectedFile != null && selectedFile.exists()) {
					actionBar.setTitle(selectedFile.getName());
					File[] currentImageFiles = selectedFile.listFiles();
					if (currentImageFiles == null || currentImageFiles.length == 0) {
						findViewById(R.id.thumbfolder_noitemview).setVisibility(View.VISIBLE);
						gridView.setVisibility(View.GONE);
					}
					else {
						findViewById(R.id.thumbfolder_noitemview).setVisibility(View.GONE);
						gridView.setVisibility(View.VISIBLE);
					}
					spictureController.setCurrentPath(selectedFile.getPath());
					loadGridImages();
				}
			}
			else {//order
				currentOrder = tempOrderList.get(position);
				actionBar.setTitle(currentOrder.getName());
				if (currentOrder.getItemNumber() == 0) {
					findViewById(R.id.thumbfolder_noitemview).setVisibility(View.VISIBLE);
					gridView.setVisibility(View.GONE);
				}
				else {
					findViewById(R.id.thumbfolder_noitemview).setVisibility(View.GONE);
					gridView.setVisibility(View.VISIBLE);
				}
				spictureController.setCurrentOrder(currentOrder);
				loadGridImages();
				controller.accessOrder(currentOrder);
			}
		}
		else if (parent == gridView) {
			if (isChooserMode) {
				getIntent().putExtra(Constants.KEY_THUMBFOLDER_CHOOSE_CONTENT, imageFileList.get(position));
				setResult(0, getIntent());
				finish();
			}
			else {
				view.startAnimation(AnimationUtils.loadAnimation(this, R.anim.thumb_item_longclick));
				//showImage(view, position);
				showImage(position);
			}
		}
	}

	/**
	 * origin codes issue: LinearInterpolator is not working in xml definition
	 * must use java codes to set it
	 * @return
	 */
	private Animation getFolderAnimation() {
		Animation animation = AnimationUtils.loadAnimation(this, R.anim.thumb_folder_click);
		LinearInterpolator interpolator = new LinearInterpolator();
		animation.setInterpolator(interpolator);
		return animation;
	}

	private void showImage(int position) {
		if (imageDialog == null) {
			imageDialog = new ShowImageDialog(this, null, 0);
		}
		imageDialog.setImagePath(imageFileList.get(position));
		imageDialog.fitImageView();
		imageDialog.show();
	}

	/*
	private void showImage(int position) { 1
		File file = new File(imageFileList.get(position));
		if (file != null && file.exists()) {
			showBitmap = ImageFactory.getInstance(encrypter).createEncryptedThumbnail(file.getPath(), 1080*1920);
		}

		if (showImageDialog == null) {
			LinearLayout layout = new LinearLayout(this);
			showImageView = new ImageView(this);
			//popView.setScaleType(ScaleType.FIT_CENTER);
			showImageView.setScaleType(ScaleType.MATRIX);
			showImageView.setOnTouchListener(new ZoomListener());

			showImageView.setOnClickListener(new View.OnClickListener() {

				@Override
				public void onClick(View v) {
					if (imageFileList != null) {
						if (chosedItemIndex > 0 && chosedItemIndex < imageFileList.size()) {
							folderItemLongClick(chosedItemIndex);
						}
					}
				}
			});

			layout.addView(showImageView);
			showImageDialog = new Dialog(this, R.style.TransparentDialog);
			showImageDialog.setContentView(layout);

		}

		if (showBitmap != null) {
			showImageView.setImageBitmap(showBitmap);
		}
		else {
			showImageView.setImageResource(R.drawable.ic_launcher);
		}
		setShowImageLayoutParams();

		showImageDialog.show();
	}

	private void setShowImageLayoutParams() {
		Point point = DisplayHelper.getScreenSize(this);
		int height = point.y - getResources().getDimensionPixelOffset(R.dimen.screen_notifybar_height) - actionBar.getHeight();

		showImageView.setLayoutParams(new LinearLayout.LayoutParams(point.x, height));

		if (showBitmap != null) {
			Matrix matrix = new Matrix();
			matrix.postTranslate(point.x/2 - showBitmap.getWidth()/2, height/2 - showBitmap.getHeight()/2);
			showImageView.setImageMatrix(matrix);
		}
	}
	*/

	@Override
	public void onConfigurationChanged(android.content.res.Configuration newConfig) {

		if (imageDialog != null) {
			imageDialog.setOrientationChanged();
			if (imageDialog.isShowing()) {
				imageDialog.onConfigChange();
			}
		}
		if (folderDialog != null) {
			folderDialog.updateHeight();
		}

		if (newConfig.orientation == android.content.res.Configuration.ORIENTATION_LANDSCAPE) {
			actionBar.onLandscape();
		}
		else {
			actionBar.onVertical();
		}

		initIndexStateController();
		super.onConfigurationChanged(newConfig);
	}

	/*
	private void folderItemLongClick(int position) {
		final int pos = position;
		String[] arrays = getResources().getStringArray(R.array.thumb_folder_item_longclick);
		new AlertDialog.Builder(this)
			.setItems(arrays, new DialogInterface.OnClickListener() {

				@Override
				public void onClick(DialogInterface dialog, int which) {
					if (which == 0) {//add to order
						List<Integer> list = new ArrayList<Integer>();
						list.add(pos);
						openOrderChooserToAddItem(list);
					}
					else if (which == 1) {//set as cover
						openOrderChooserToSetCover(pos);
					}
					else if (which == 2) {//view details
						viewDetails(pos);
					}
					else if (which == 3) {//delete
						deleteItemFromFolder(pos);
					}
				}
			}).show();
	}
	*/

	private void loadGridImages() {
		progressDialog = new ProgressDialog(this);
		progressDialog.setMessage(getResources().getString(R.string.loading));
		progressDialog.show();
		if (srcMode == SRC_MODE_FOLDER) {
			spictureController.loadChooserItems(SpictureController.SHOW_MODE_FOLDER);
		}
		else {
			spictureController.loadChooserItems(SpictureController.SHOW_MODE_ORDER);
		}

	}

	private class FolderListAdapter extends BaseAdapter {

		@Override
		public int getCount() {

			if (srcMode == SRC_MODE_FOLDER) {
				return tempFileList == null ? 0:tempFileList.size();
			}
			else {//order
				return tempOrderList == null ? 0:tempOrderList.size();
			}
		}

		@Override
		public Object getItem(int position) {

			if (srcMode == SRC_MODE_FOLDER) {
				return tempFileList == null ? position:tempFileList.get(position);
			}
			else {
				return tempOrderList == null ? position:tempOrderList.get(position);
			}
		}

		@Override
		public long getItemId(int position) {
			return position;
		}

		@Override
		public View getView(int position, View convertView, ViewGroup parent) {

			convertView = LayoutInflater.from(ThumbFolderActivity.this).inflate(R.layout.thumbfolder_folder_item, null);
			ImageView imageView = (ImageView) convertView.findViewById(R.id.thumb_folder_item_image);

			TextView textView = (TextView) convertView.findViewById(R.id.thumb_folder_item_name);
			if (srcMode == SRC_MODE_FOLDER) {
				textView.setText(tempFileList.get(position).getName());
			}
			else {//order
				SOrder order = tempOrderList.get(position);
				textView.setText(order.getName() + "(" + order.getItemNumber() + ")");
				Bitmap bitmap = pictureManager.getOrderCircleCover(order.getCoverPath(), ThumbFolderActivity.this);
				if (bitmap != null) {
					imageView.setImageBitmap(bitmap);
				}
			}
			return convertView;
		}

	}

	@Override
	public boolean onItemLongClick(AdapterView<?> parent, View view,
								   int position, long id) {

		if (parent == folderListView) {
			view.findViewById(R.id.thumb_folder_item_image).startAnimation(getFolderAnimation());
		}
		else if (parent == gridView) {
			/**
			 * as showActionMode will notify adapter to refresh, so the view will be replaced by new view
			 * need notify new view start animation
			 */
			gridAdapterProvider.notifyShowAnimation(position);
			showActionMode(position);
		}
		return true;
	}

	private void showActionMode(int position) {
		gridAdapterProvider.startActionMode(position);
	}



	@Override
	public void onBackPressed() {
		//To fix: showImageDialog>click setasslidingmenubk icon>popup listwindow
		//>back>show image dialog again>click setasslidingmenubk, there is no action
		if (imageDialog != null) {
			imageDialog.dismiss();
		}

		if (gridAdapterProvider.isActionMode()) {
			//TODO action bar change
			gridAdapterProvider.showActionMode(false);
			gridAdapterProvider.refresh(false);
		}
		else {
			super.onBackPressed();
		}
	}


	private void deleteItemFromFolder(int pos) {
		// TODO Auto-generated method stub

	}

	private void viewDetails(int pos) {
		File file = new File(imageFileList.get(pos));
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
					if (imageFileList.get(pos) != null && new File(imageFileList.get(pos)).exists()) {
						order.setCoverPath(imageFileList.get(pos));
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
						Toast.makeText(ThumbFolderActivity.this, msg, Toast.LENGTH_LONG).show();
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
		/*
		SOrderChooser chooser = new SOrderChooser(this, new SOrderChooser.OnOrderChooseListener() {

			@Override
			public void chooseOrder(SOrder order) {
				if (imageFileList.get(pos) != null && new File(imageFileList.get(pos)).exists()) {
					order.setCoverPath(imageFileList.get(pos));
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
					Toast.makeText(ThumbFolderActivity.this, msg, Toast.LENGTH_LONG).show();
				}
			}
		});
		chooser.setTitleCustom(getResources().getString(R.string.set_as_cover));
		chooser.show();
		*/
	}

	private void refreshFolderList() {
		folderListAdapter.notifyDataSetChanged();
	}

	private void openOrderChooserToAddItem(final List<Integer> indexList) {

		SOrderChooserUpdate chooser = new SOrderChooserUpdate(this, new CustomDialog.OnCustomDialogActionListener() {

			@Override
			public boolean onSave(Object object) {
				if (object != null) {
					final SOrder order = (SOrder) object;
					for (int index : indexList) {
						final String itemPath = imageFileList.get(index);
						if (itemPath != null) {
							if (spictureController.isItemExist(itemPath, order.getId())) {
								String title = getResources().getString(R.string.spicture_myorders_item_exist);
								title = String.format(title, order.getName());
								new AlertDialog.Builder(ThumbFolderActivity.this)
										.setMessage(title)
										.setPositiveButton(R.string.ok, new DialogInterface.OnClickListener() {

											@Override
											public void onClick(DialogInterface dialog, int which) {
												addToOrder(itemPath, order, true);
												refreshFolderList();
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
							Toast.makeText(ThumbFolderActivity.this, R.string.login_pwd_error, Toast.LENGTH_LONG).show();
						}
					}
					refreshFolderList();
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
		SOrderChooser chooser = new SOrderChooser(this, new SOrderChooser.OnOrderChooseListener() {

			@Override
			public void chooseOrder(final SOrder order) {
				for (int index : indexList) {
					final String itemPath = imageFileList.get(index);
					if (itemPath != null) {
						if (spictureController.isItemExist(itemPath, order.getId())) {
							String title = getResources().getString(R.string.spicture_myorders_item_exist);
							title = String.format(title, order.getName());
							new AlertDialog.Builder(ThumbFolderActivity.this)
								.setMessage(title)
								.setPositiveButton(R.string.ok, new DialogInterface.OnClickListener() {

									@Override
									public void onClick(DialogInterface dialog, int which) {
										addToOrder(itemPath, order, true);
										refreshFolderList();
									}
								})
								.setNegativeButton(R.string.cancel, null)
								.show();
						}
						else {
//							boolean showPopToast = false;
//							if (index == indexList.size() - 1) {
//								showPopToast = true;
//							}
//							else {
//								showPopToast = false;
//							}
							addToOrder(itemPath, order, false);
						}
					}
					else {
						Toast.makeText(ThumbFolderActivity.this, R.string.login_pwd_error, Toast.LENGTH_LONG).show();
					}
				}
				refreshFolderList();
			}
		});
		chooser.setTitleCustom(getResources().getString(R.string.add_to_order));
		chooser.show();
		*/
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

	@Override
	public boolean handleMessage(Message msg) {

		switch (msg.what) {
			case Constants.STATUS_LOAD_CHOOSERITEM_FINISH:
				Log.d(TAG, "handleMessage STATUS_LOAD_CHOOSERITEM_FINISH");
				imageFileList = spictureController.getFileNameList();
				gridAdapterProvider.refresh(imageFileList);
				progressDialog.cancel();
				break;

			case Constants.STATUS_MOVE_FILE_DONE:
				Log.d(TAG, "handleMessage STATUS_MOVE_FILE_DONE");
				moveController.updateProgress();
				break;
			case Constants.STATUS_MOVE_FILE_FINISH:
				Log.d(TAG, "handleMessage STATUS_MOVE_FILE_FINISH");
				moveController.updateProgress();
				moveController.cancleProgress(true);
				refreshGridView();
				if (gridAdapterProvider.isActionMode()) {
					gridAdapterProvider.showActionMode(false);
					gridAdapterProvider.refresh(false);
				}
				break;
			case Constants.STATUS_MOVE_FILE_UNSUPORT:
				Log.d(TAG, "handleMessage STATUS_MOVE_FILE_UNSUPORT");
				Bundle bundle = msg.getData();
				boolean isFinish = bundle.getBoolean(Constants.KEY_MOVETO_UNSUPPORT_FINISH);
				String src = bundle.getString(Constants.KEY_MOVETO_UNSUPPORT_SRC);
				String error = getResources().getString(R.string.move_src_to_src) + "\n" + src;

				moveController.addError(error);
				if (isFinish) {
					moveController.cancleProgress(false);
				}
				break;
			default:
				break;
		}
		return true;
	}

	private void refreshGridView() {
		loadGridImages();
	}

	@Override
	protected void onPause() {
		//finish();
		super.onPause();
	}

	@Override
	protected void onStop() {
		super.onStop();
	}

	@Override
	protected void onDestroy() {
		//不能写在onStop里，否则从WaterfallActivity返回时会发生try to use recycled bitmap异常
		pictureManager.recycleCircleOrderCover();
		super.onDestroy();
	}

	@Override
	protected void onResume() {
		super.onResume();
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
						Toast.makeText(ThumbFolderActivity.this, R.string.login_pwd_error, Toast.LENGTH_LONG).show();
					}
				}
			});
		}
	}

	@Override
	public void onBack() {
		finish();
	}

	@Override
	public void onRefresh() {
		loadFolderList();
	}

	@Override
	public void createMenu(MenuInflater menuInflater, Menu menu) {
		loadMenu(menuInflater, menu);
	}

	@Override
	public void onPrepareMenu(MenuInflater menuInflater, Menu menu) {
		loadMenu(menuInflater, menu);
	}

	@Override
	public OnMenuItemClickListener getMenuItemListener() {
		return this;
	}

	@Override
	public void onTextChanged(String text, int start, int before, int count) {
		Log.i("ThumbFolderActivity", "onTextChanged(" + text + "," + start + "," + before + "," + count + ")");

		if (srcMode == SRC_MODE_FOLDER) {
			if (tempFileList == null) {
				tempFileList = new ArrayList<File>();
			}
			else {
				tempFileList.clear();
			}
			if (text.toString().trim().length() == 0) {
				for (int i = 0; i < folderList.size(); i ++) {
					tempFileList.add(folderList.get(i));
				}
				folderListAdapter.notifyDataSetChanged();
				actionBar.setTitle(getResources().getString(R.string.thumb_folder_allfolder));
				return;
			}

			//startWith排在前面，contains排在后面
			actionBar.setTitle(text);
			String target = null, prefix = text.toString().toLowerCase();
			for (int i = 0; i < folderList.size(); i ++) {
				target = folderList.get(i).getName().toLowerCase();
				if (target.startsWith(prefix)) {
					tempFileList.add(folderList.get(i));
				}
			}
			for (int i = 0; i < folderList.size(); i ++) {
				target = folderList.get(i).getName().toLowerCase();
				if (!target.startsWith(prefix) && target.contains(prefix)) {
					tempFileList.add(folderList.get(i));
				}
			}
			folderListAdapter.notifyDataSetChanged();
		}
		else if (srcMode == SRC_MODE_ORDER) {
			if (tempOrderList == null) {
				tempOrderList = new ArrayList<SOrder>();
			}
			else {
				tempOrderList.clear();
			}
			if (text.toString().trim().length() == 0) {
				for (int i = 0; i < orderList.size(); i ++) {
					tempOrderList.add(orderList.get(i));
				}
				folderListAdapter.notifyDataSetChanged();
				actionBar.setTitle(getResources().getString(R.string.thumb_folder_allolder));
				return;
			}

			//startWith排在前面，contains排在后面
			actionBar.setTitle(text);
			String target = null, prefix = text.toString().toLowerCase();
			for (int i = 0; i < orderList.size(); i ++) {
				target = orderList.get(i).getName().toLowerCase();
				if (target.startsWith(prefix)) {
					tempOrderList.add(orderList.get(i));
				}
			}
			for (int i = 0; i < orderList.size(); i ++) {
				target = orderList.get(i).getName().toLowerCase();
				if (!target.startsWith(prefix) && target.contains(prefix)) {
					tempOrderList.add(orderList.get(i));
				}
			}
			folderListAdapter.notifyDataSetChanged();
		}
	}

	@Override
	public void onDelete() {
		// TODO Auto-generated method stub

	}

	@Override
	public void onIconClick(View view) {
		switch (view.getId()) {
			case R.id.actionbar_gallery:
				//Deprecated
				//startActivity(new Intent().setClass(this, FlingGalleryActivity.class));

				Bundle bundle = new Bundle();
				bundle.putInt("src_mode", FullScreenSurfActivity.SRC_MODE_RANDOM);
				Intent intent = new Intent();
				intent.putExtras(bundle);
				intent.setClass(this, FullScreenSurfActivity.class);
				startActivity(intent);
				break;

			default:
				break;
		}
	}

	@Override
	public void onSelect(String index) {
		if (folderListView != null) {
			folderListView.setSelection(indexCreator.getIndexPosition(index));
		}
	}

}
