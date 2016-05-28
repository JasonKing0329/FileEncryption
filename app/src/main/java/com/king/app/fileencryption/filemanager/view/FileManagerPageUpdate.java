package com.king.app.fileencryption.filemanager.view;

import java.io.File;
import java.util.HashMap;
import java.util.List;

import com.king.app.fileencryption.Application;
import com.king.app.fileencryption.MainViewActivity;
import com.king.app.fileencryption.R;
import com.king.app.fileencryption.TabActionBar;
import com.king.app.fileencryption.book.BookActivity;
import com.king.app.fileencryption.controller.AccessController;
import com.king.app.fileencryption.controller.MainViewAction;
import com.king.app.fileencryption.controller.PictureManagerUpdate;
import com.king.app.fileencryption.data.Constants;
import com.king.app.fileencryption.data.DBInfor;
import com.king.app.fileencryption.filemanager.controller.FileChangeListener;
import com.king.app.fileencryption.filemanager.controller.FileListController;
import com.king.app.fileencryption.filemanager.view.FilePathIndicatorView.PathIndicatorListener;
import com.king.app.fileencryption.publicview.ChangeThemeDialog;
import com.king.app.fileencryption.publicview.CustomDialog;
import com.king.app.fileencryption.publicview.DefaultDialogManager;
import com.king.app.fileencryption.setting.SettingProperties;
import com.king.app.fileencryption.slidingmenu.SlidingMenuCreator;
import com.king.app.fileencryption.surf.SurfActivity;
import com.king.app.fileencryption.surf.UiController;
import com.king.app.fileencryption.thumbfolder.ShowImageDialog;
import com.king.app.fileencryption.thumbfolder.ThumbFolderActivity;
import com.king.app.fileencryption.util.DisplayHelper;
import com.king.app.fileencryption.wall.WallActivity;

import android.app.Activity;
import android.app.AlertDialog;
import android.app.ProgressDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.Color;
import android.graphics.drawable.BitmapDrawable;
import android.os.Bundle;
import android.os.Handler;
import android.os.Handler.Callback;
import android.os.Message;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.AdapterView.OnItemLongClickListener;
import android.widget.CompoundButton;
import android.widget.CompoundButton.OnCheckedChangeListener;
import android.widget.PopupMenu.OnMenuItemClickListener;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.PopupMenu;
import android.widget.RadioButton;
import android.widget.TextView;
import android.widget.Toast;

public class FileManagerPageUpdate implements MainViewAction, FileChangeListener {

	private final String TAG = "FileManagerPage";

	private Context context;
	private View view;
	//private TextView currentPathView;
	private FilePathIndicatorView indicatorView;
	private ImageView parentView;
	private TextView nameTagView, timeTagView, imageWHView;
	private ImageView nameSortIconView, timeSortIconView;

	private ListView fileListView;
	private FileListAdapter adapter;
	private RadioButton allRadio, encryptedRadio, unencryptedRadio;
	private FileListAction listAction;
	private FileListController listController;
	private ProgressDialog progressDialog;
	private TabActionBar actionBar;

	private LinearLayout layout;
	private Bitmap background;
	private ShowImageDialog imageDialog;

	public FileManagerPageUpdate(Context context, View view) {
		this.context = context;
		this.view = view;
		listAction = new FileListAction();
		listController = new FileListController(context, listAction.getHandler(), this);

		initViewElement();
		initView();
	}

	private void initView() {
		showParentItem();
		layout.setBackgroundColor(Color.WHITE);
		refresh();
	}

	public String getCurrentPath() {
		return listController.getCurrentPath();
	}

	private void initViewElement() {
		layout = (LinearLayout) view.findViewById(R.id.layout_page_filemanager);
		showCurPathView();
		parentView = (ImageView) view.findViewById(R.id.filelist_parent);
		nameTagView = (TextView) view.findViewById(R.id.filelist_tag_name);
		timeTagView = (TextView) view.findViewById(R.id.filelist_tag_time);
		imageWHView = (TextView) view.findViewById(R.id.filelist_tag_wh);
		nameSortIconView = (ImageView) view.findViewById(R.id.filelist_tag_name_sorticon);
		timeSortIconView = (ImageView) view.findViewById(R.id.filelist_tag_time_sorticon);
		fileListView = (ListView) view.findViewById(R.id.filelist_listview);
		allRadio = (RadioButton) view.findViewById(R.id.filelist_radio_all);
		encryptedRadio = (RadioButton) view.findViewById(R.id.filelist_radio_encrypted);
		unencryptedRadio = (RadioButton) view.findViewById(R.id.filelist_radio_unencrypted);

		fileListView.setOnItemClickListener(listAction);
		fileListView.setOnItemLongClickListener(listAction);
		parentView.setOnClickListener(listAction);
		nameTagView.setOnClickListener(listAction);
		timeTagView.setOnClickListener(listAction);
		nameSortIconView.setOnClickListener(listAction);
		timeSortIconView.setOnClickListener(listAction);

		if (Application.isLollipop()) {
			parentView.setBackgroundResource(R.drawable.ripple_filemanager_tag_bk);
			nameSortIconView.setBackgroundResource(R.drawable.ripple_filemanager_tag_bk);
			timeSortIconView.setBackgroundResource(R.drawable.ripple_filemanager_tag_bk);
		}

		resetTimeSortTag();
		resetNameSortTag();

		allRadio.setOnCheckedChangeListener(listAction);
		encryptedRadio.setOnCheckedChangeListener(listAction);
		unencryptedRadio.setOnCheckedChangeListener(listAction);
	}

	/*v6.3.7 deprecated this display mode, replace it with path indicator view
	private void showCurPathView() {
		
		if (currentPathView != null) {
			currentPathView.setVisibility(View.GONE);
		}
		
		if (DisplayHelper.isTabModel(context)) {
			currentPathView = (TextView) view.findViewById(R.id.filelist_current_dir_hor);
		}
		else {
			if (context.getResources().getConfiguration().orientation
					== android.content.res.Configuration.ORIENTATION_LANDSCAPE) {
				currentPathView = (TextView) view.findViewById(R.id.filelist_current_dir_hor);
			}
			else {
				currentPathView = (TextView) view.findViewById(R.id.filelist_current_dir);
			}
		}
		currentPathView.setVisibility(View.VISIBLE);
		
		currentPathView.setText(listController.getCurrentPath());
	}
	*/

	private void showCurPathView() {
		List<PathIndicatorNode> pathList = null;
		if (indicatorView != null) {
			indicatorView.setVisibility(View.GONE);
			pathList = indicatorView.getPathList();//转屏时取出之前的pathList，转屏后新的indicatorView以此初始化indicator
		}

		if (DisplayHelper.isTabModel(context)) {
			indicatorView = (FilePathIndicatorView) view.findViewById(R.id.filelist_current_dir_hor);
		}
		else {
			if (context.getResources().getConfiguration().orientation
					== android.content.res.Configuration.ORIENTATION_LANDSCAPE) {
				indicatorView = (FilePathIndicatorView) view.findViewById(R.id.filelist_current_dir_hor);
			}
			else {
				indicatorView = (FilePathIndicatorView) view.findViewById(R.id.filelist_current_dir);
			}
		}
		indicatorView.setVisibility(View.VISIBLE);
		indicatorView.setPathIndicatorListener(listAction);

		if (pathList == null) {
			indicatorView.addPath(listController.getCurrentPath());
		}
		else {
			indicatorView.create(pathList);
		}
		//currentPathView.setText(listController.getCurrentPath());
	}

	private void resetNameSortTag() {
		nameSortIconView.setVisibility(View.INVISIBLE);
		nameSortIconView.setTag(true);//按名称默认升序
	}
	private void resetTimeSortTag() {
		timeSortIconView.setVisibility(View.INVISIBLE);
		timeSortIconView.setTag(false);//按时间默认降序
	}

	private void showImageWHView(boolean show) {
		if (DisplayHelper.isTabModel(context) || context.getResources().getConfiguration().orientation
				== android.content.res.Configuration.ORIENTATION_LANDSCAPE) {
			imageWHView.setVisibility(show ? View.VISIBLE : View.GONE);
			adapter.showImageWH(show);
		}
		else {
			imageWHView.setVisibility(View.GONE);
			adapter.showImageWH(false);
		}
	}
	private void refresh() {
		listController.findFile();
	}

	private void notifyAdapterRefresh() {
		//v6.3.7 deprecated this display mode, replace it with indicator path view
		//currentPathView.setText(listController.getCurrentPath());
		if (listController.getFilePageItemList() == null) {
			Toast.makeText(context, R.string.error_app_root_not_exist, Toast.LENGTH_LONG).show();
			return;
		}
		Log.i(TAG, "notifyAdapterRefresh -> " + listController.getCurrentPath());

		if (adapter == null) {
			adapter = new FileListAdapter(listController.getFilePageItemList(), context);
			fileListView.setAdapter(adapter);
		}
		else {
			adapter.updateList(listController.getFilePageItemList());
			if (listController.getFilePageItemList() != null && listController.getFilePageItemList().size() > 0) {
				if (!listController.getFilePageItemList().get(0).getFile().isDirectory()) {
					showImageWHView(true);
				}
			}
			adapter.notifyDataSetChanged();
		}
	}

	private void showParentItem() {
		if (listController.isRootFolder()) {
			parentView.setVisibility(View.INVISIBLE);
		}
		else {
			parentView.setVisibility(View.VISIBLE);
		}
	}

	private void showProgress() {
		progressDialog = new ProgressDialog(context);
		progressDialog.setMessage(context.getResources().getString(R.string.loading));
		progressDialog.show();
	}
	private void cancelProgress() {
		if (progressDialog != null) {
			progressDialog.cancel();
		}
	}

	@Override
	public void onCreateOptionsMenu(Menu menu) {
		onPrepareOptionsMenu(menu);
	}

	@Override
	public boolean onPrepareOptionsMenu(Menu menu) {

		Log.i(TAG, "onPrepareOptionsMenu");
		menu.setGroupVisible(R.id.group_file, true);
		menu.setGroupVisible(R.id.group_sorder, false);
		menu.setGroupVisible(R.id.group_spicture, false);
		menu.findItem(R.id.menu_edit).setVisible(true);
		return true;
	}

	@Override
	public boolean onOptionsItemSelected(final MenuItem item) {

		switch (item.getItemId()) {
    	/*
		case R.id.menu_create_folder:
			openCreateFolderDialog();
			break;
			*/
			case R.id.menu_encrypt_current_folder:
				encryptCurFolder();
				break;
			case R.id.menu_decipher_current_folder:
				decipherCurFolder();
				break;
			/*
		case R.id.menu_thumb_folder:
			startThumbView();
			break;
			*/
			case R.id.menu_export:
				DBInfor.export(context);
				break;
			case R.id.menu_import:
				openLoadFromDialog();
				break;
			case R.id.menu_change_theme:
				new ChangeThemeDialog(context, new CustomDialog.OnCustomDialogActionListener() {

					@Override
					public boolean onSave(Object object) {
						reload();
						return false;
					}

					@Override
					public void onLoadData(HashMap<String, Object> data) {
						// TODO Auto-generated method stub

					}

					@Override
					public boolean onCancel() {
						// TODO Auto-generated method stub
						return false;
					}
				}).show();
				break;
		}
		return true;
	}

	private void openLoadFromDialog() {
		new LoadFromDialog(context, LoadFromDialog.DATA_HISTORY, new CustomDialog.OnCustomDialogActionListener() {

			@Override
			public boolean onSave(Object object) {
				if (object != null) {
					File file = (File) object;
					DBInfor.replaceDatabase(context, file.getPath());
				}
				return true;
			}

			@Override
			public void onLoadData(HashMap<String, Object> data) {
				// TODO Auto-generated method stub

			}

			@Override
			public boolean onCancel() {
				// TODO Auto-generated method stub
				return false;
			}
		}).show();
	}

	private void decipherCurFolder() {
		showProgress();

	}

	private void encryptCurFolder() {
		showProgress();

	}

	@Override
	public SlidingMenuCreator loadMenu(LinearLayout menuLayout) {

		menuLayout.removeAllViews();
		SlidingMenuCreator creator = new SlidingMenuCreator(context, slidingMenuListener);
		creator.loadMenu(Constants.fileManagerMenu, menuLayout, SettingProperties.getSlidingMenuMode(context));
		return creator;
	}

	@Override
	public SlidingMenuCreator loadTwoWayMenu(LinearLayout menuLayout, LinearLayout menuLayoutRight) {
		menuLayout.removeAllViews();
		menuLayoutRight.removeAllViews();

		SlidingMenuCreator slidingMenuCreator = new SlidingMenuCreator(context, slidingMenuListener);
		slidingMenuCreator.loadMenu(Constants.fileManagerMenu, menuLayout, SettingProperties.SLIDINGMENU_LEFT);
		slidingMenuCreator.loadMenu(Constants.fileManagerMenu, menuLayoutRight, SettingProperties.SLIDINGMENU_RIGHT);
		return slidingMenuCreator;
	}

	OnClickListener slidingMenuListener = new OnClickListener() {

		@Override
		public void onClick(View v) {
			//v6.3.6 when menu is close, click space area in filemanager page and spicture page, menu item will receive action
			if (!((MainViewActivity) context).isSlidingMenuOpen()) {
				return;
			}
			boolean excuted = false;
			switch (v.getId()) {
				case R.string.menu_thumb_folder:
					startThumbView();
					break;
//			case R.string.menu_new_folder:
//				openCreateFolderDialog();
//				break;
				case R.string.menu_encrypt_current_folder:
					encryptCurFolder();
					excuted = true;
					break;
				case R.string.menu_decipher_current_folder:
					decipherCurFolder();
					excuted = true;
					break;
				case R.string.menu_export:
					DBInfor.export(context);
					excuted = true;
					break;
				case R.string.menu_load:
					openLoadFromDialog();
					excuted = true;
					break;
				case R.string.menu_change_theme:
					new ChangeThemeDialog(context, new CustomDialog.OnCustomDialogActionListener() {

						@Override
						public boolean onSave(Object object) {
							reload();
							return false;
						}

						@Override
						public void onLoadData(HashMap<String, Object> data) {
							// TODO Auto-generated method stub

						}

						@Override
						public boolean onCancel() {
							// TODO Auto-generated method stub
							return false;
						}
					}).show();
					excuted = true;
					break;
			}

			if (!excuted) {
				((MainViewActivity) context).slidingMenuListener.onClick(v);
			}
		}
	};

	protected void reload() {
		Activity activity = (Activity) context;
		Intent intent = activity.getIntent();
		activity.overridePendingTransition(0, 0);
		intent.addFlags(Intent.FLAG_ACTIVITY_NO_ANIMATION);

		PictureManagerUpdate.getInstance().destroy();
		activity.finish();
		activity.overridePendingTransition(0, 0);
		activity.startActivity(intent);
	}

	private void startThumbView() {
		Intent intent = new Intent();
		Bundle bundle = new Bundle();
		bundle.putInt(Constants.KEY_THUMBFOLDER_INIT_MODE, ThumbFolderActivity.SRC_MODE_FOLDER);
		intent.putExtras(bundle);
		intent.setClass(context, ThumbFolderActivity.class);
		((Activity) context).startActivityForResult(intent, 0);
	}

	private void startBookView(String folder) {
		Intent intent = new Intent();
		Bundle bundle = new Bundle();
		bundle.putInt(Constants.KEY_BOOK_INIT_MODE, BookActivity.FOLDER);
		if (folder != null) {
			bundle.putString(Constants.KEY_BOOK_INIT_FOLDER, folder);
		}
		intent.putExtras(bundle);
		intent.setClass(context, BookActivity.class);
		((Activity) context).startActivity(intent);
		//((Activity) context).startActivityForResult(intent, 0);
	}

	private void openCreateFolderDialog() {
		new DefaultDialogManager().openCreateFolderDialog(context
				, new DefaultDialogManager.OnDialogActionListener() {

					@Override
					public void onOk(String name) {
						String filePath = listController.getCurrentPath() + "/" + name;
						if (listController.createFolder(new File(filePath))) {
							Toast.makeText(context, R.string.success, Toast.LENGTH_LONG).show();
							refresh();
						}
						else {
							Toast.makeText(context, R.string.filelist_folder_already_exist, Toast.LENGTH_LONG).show();
						}
					}
				});
	}

	@Override
	public boolean onBackPressed() {
		if (parentView.getVisibility() == View.VISIBLE) {
			indicatorView.backToUpper();
			if (listController != null) {
				listController.findParent();
				return true;
			}
		}
		return false;

	}

	public void setBackground(Bitmap bitmap) {
		if (background == null) {
			layout.setBackgroundColor(Color.WHITE);
		}
		else {
			layout.setBackground(new BitmapDrawable(context.getResources(), background));
		}
	}

	@Override
	public void changeBackground(Bitmap bitmap) {
		background = bitmap;
	}

	private void showImage(String path) {
		if (imageDialog == null) {
			imageDialog = new ShowImageDialog(context, null, context.getResources().getDimensionPixelSize(R.dimen.actionbar_height));
		}
		imageDialog.setImagePath(path);
		imageDialog.show();
	}

	private void openByWall(String path) {
		Bundle bundle = new Bundle();
		bundle.putInt(WallActivity.MODE_KEY, WallActivity.MODE_FOLDER);
		bundle.putString(WallActivity.MODE_VALUE_KEY, path);
		((MainViewActivity) context).openWallGallery(bundle);
	}

	private void startFullScreenActivity(String path) {
		Bundle bundle = new Bundle();
		bundle.putInt("src_mode", UiController.SRC_MODE_FOLDER);
		bundle.putString("path", path);
		Intent intent = new Intent();
		intent.putExtras(bundle);
		intent.setClass(context, SurfActivity.class);
		context.startActivity(intent);
	}
	@Override
	public void onFindFileFinish() {
		notifyAdapterRefresh();
	}

	@Override
	public void onBackToRoot() {
		showParentItem();
	}

	private class FileListAction implements Callback, OnItemClickListener
			, OnItemLongClickListener, OnClickListener, OnCheckedChangeListener
			, PathIndicatorListener {

		private Handler handler = null;

		public FileListAction() {
			handler = new Handler(this);
		}
		public Handler getHandler() {
			return handler;
		}
		@Override
		public boolean handleMessage(Message msg) {

			Bundle bundle = msg.getData();
			boolean isok = bundle.getBoolean("result");
			if (isok) {
				Log.i(TAG, "FileListAction handleMessage " + isok);
				refresh();
			}
			else {
				if (msg.what == FileListController.FILE_TYPE_ENCRYPTED) {
					Toast.makeText(context, R.string.filelist_encrypt_fail, Toast.LENGTH_LONG).show();
				}
				else if (msg.what == FileListController.FILE_TYPE_UNENCRYPTED) {
					Toast.makeText(context, R.string.filelist_decipher_fail, Toast.LENGTH_LONG).show();
				}
			}
			cancelProgress();
			return true;
		}

		private boolean deleteFile(int position) {
			final File file = listController.getFilePageItemList().get(position).getFile();
			Log.i(TAG, " deleteFile " + file.getPath());
			boolean canDelete = true;

			if (!file.exists()) {
				return false;
			}
			String msg = context.getResources().getString(R.string.filelist_delete_msg);
			if (file.isDirectory()) {
				File[] subFiles = file.listFiles();
				if (subFiles.length == 0) {
					msg = context.getResources().getString(R.string.filelist_delete_empty_folder);
				}
				else if (subFiles.length < 10) {
					for (int i = 0; i < subFiles.length; i ++) {
						if (subFiles[i].isDirectory()) {
							msg = context.getResources().getString(R.string.filelist_delete_fodler_deny);
							canDelete = false;
							break;
						}
					}
					if (canDelete) {
						msg = context.getResources().getString(R.string.filelist_delete_folder);
					}
				}
				else if (subFiles.length > 10) {
					msg = context.getResources().getString(R.string.filelist_delete_fodler_deny);
					canDelete = false;
				}
			}
			final boolean canDeleted = canDelete;
			AlertDialog.Builder dialog = new AlertDialog.Builder(context);
			dialog.setTitle(R.string.filelist_delete);
			dialog.setMessage(msg);
			if (canDeleted) {
				dialog.setPositiveButton(R.string.ok, new DialogInterface.OnClickListener() {

					@Override
					public void onClick(DialogInterface dialog, int which) {
						listController.deleteFile(file);
						refresh();
						Log.i(TAG, " deleteFile ok");
					}
				})
						.setNegativeButton(R.string.cancel, new DialogInterface.OnClickListener() {

							@Override
							public void onClick(DialogInterface dialog, int which) {

							}
						});
			}
			dialog.show();
			return true;
		}

		@Override
		public boolean onItemLongClick(AdapterView<?> arg0, View arg1,
									   int position, long arg3) {
			final int pos = position;
			Log.i(TAG, "onItemLongClick" + pos);
			final FilePageItem item = listController.getFilePageItemList().get(position);
			if (item.getFile().isDirectory()) {
				String[] arrays = context.getResources().getStringArray(R.array.filelist_longclick_action_folder);
				new AlertDialog.Builder(context)
						.setItems(arrays, new DialogInterface.OnClickListener() {

							@Override
							public void onClick(DialogInterface dialog, int which) {
								if (which == 0) {//delete
									deleteFile(pos);
								}
								else if (which == 1) {//open by wall
									openByWall(item.getFile().getPath());
								}
								else if (which == 2) {//full screen
									startFullScreenActivity(item.getFile().getPath());
								}
								else if (which == 3) {//book view
									startBookView(item.getFile().getPath());
								}
							}
						}).show();
			}
			else {
				if (listController.isEncrypted(item.getFile().getPath())) {
					String[] arrays = context.getResources().getStringArray(R.array.filelist_longclick_action_decipher);
					new AlertDialog.Builder(context)
							.setItems(arrays, new DialogInterface.OnClickListener() {

								@Override
								public void onClick(DialogInterface dialog, int which) {
									if (which == 0) {
										showProgress();
										listController.decipherFile(pos);
									}
									else if (which == 1) {
										deleteFile(pos);
									}
								}
							}).show();
				}
				else {
					String[] arrays = context.getResources().getStringArray(R.array.filelist_longclick_action_encrypt);
					new AlertDialog.Builder(context)
							.setItems(arrays, new DialogInterface.OnClickListener() {

								@Override
								public void onClick(DialogInterface dialog, int which) {
									if (which == 0) {
										showProgress();
										listController.encryptFile(pos);
									}
									else if (which == 1) {
										deleteFile(pos);
									}
								}
							}).show();
				}
			}

			return true;
		}

		@Override
		public void onItemClick(AdapterView<?> arg0, View arg1, int position,
								long arg3) {
			Log.i(TAG, "onItemClick" + position);
			File file = listController.getFilePageItemList().get(position).getFile();
			if (file.isDirectory()) {
				listController.setCurrentPath(file.getPath());

				indicatorView.addPath(file.getPath());

				if (parentView.getVisibility() != View.VISIBLE) {
					showParentItem();
				}

				listController.findFile();

				//update scroll position
				listController.updateParentPosition(fileListView.getFirstVisiblePosition());
				//need be called after onFindFileFinish
				fileListView.setSelection(listController.getScrollPosition());
			}
			else if (listController.isEncryptedFile(file)) {
				if (AccessController.getInstance().getAccessMode() > AccessController.ACCESS_MODE_PRIVATE) {
					showImage(file.getPath());
				}
			}
		}

		@Override
		public void onClick(View v) {
			if (v == parentView) {
				Log.i(TAG, "parent onClick");

				showImageWHView(false);
				indicatorView.backToUpper();

				File file = new File(listController.getCurrentPath());
				if (!file.exists()) {
					listController.findFile();
				}
				else {
					listController.findParent();
					//update scroll position
					listController.updateChildPosition(fileListView.getFirstVisiblePosition());
					//need be called after onFindFileFinish
					fileListView.setSelection(listController.getScrollPosition());
				}
			}
			else if (v == nameTagView || v == nameSortIconView) {
				boolean decrease = !((Boolean) nameSortIconView.getTag());

				if (decrease) {
					nameSortIconView.setImageResource(R.drawable.sort_decrease);
				}
				else {
					nameSortIconView.setImageResource(R.drawable.sort_increase);
				}
				nameSortIconView.setTag(decrease);
				nameSortIconView.setVisibility(View.VISIBLE);
				resetTimeSortTag();
				listController.setSortMode(FileListController.SORT_BY_NAME, decrease);
				listController.sortByName(decrease);
			}
			else if (v == timeTagView || v == timeSortIconView) {

				boolean decrease = !((Boolean) timeSortIconView.getTag());

				if (decrease) {
					timeSortIconView.setImageResource(R.drawable.sort_decrease);
				}
				else {
					timeSortIconView.setImageResource(R.drawable.sort_increase);
				}
				timeSortIconView.setTag(decrease);
				timeSortIconView.setVisibility(View.VISIBLE);
				resetNameSortTag();
				listController.setSortMode(FileListController.SORT_BY_DATE, decrease);
				listController.sortByTime(decrease);
			}
		}

		@Override
		public void onCheckedChanged(CompoundButton buttonView,
									 boolean isChecked) {
			if (isChecked) {
				Log.i(TAG, "radio before check currentType = " + listController.getFileType());
				if (buttonView == allRadio) {
					encryptedRadio.setChecked(false);
					unencryptedRadio.setChecked(false);
					listController.setFindAll(true);
					listController.setFindEncrypted(false);
					listController.setFindUnEncrypted(false);
					if (listController.getFileType() != FileListController.FILE_TYPE_ALL) {
						listController.findFile();
					}
				}
				else if (buttonView == encryptedRadio) {
					allRadio.setChecked(false);
					unencryptedRadio.setChecked(false);
					listController.setFindEncrypted(true);
					listController.setFindAll(false);
					listController.setFindUnEncrypted(false);
					if (listController.getFileType() != FileListController.FILE_TYPE_ENCRYPTED) {
						listController.findFile();
					}
				}
				else if (buttonView == unencryptedRadio) {
					encryptedRadio.setChecked(false);
					allRadio.setChecked(false);
					listController.setFindUnEncrypted(true);
					listController.setFindEncrypted(false);
					listController.setFindAll(false);
					if (listController.getFileType() != FileListController.FILE_TYPE_UNENCRYPTED) {
						listController.findFile();
					}
				}

				Log.i(TAG, "radio after check currentType = " + listController.getFileType());
			}
		}
		@Override
		public void onClickPath(String path) {
			listController.setCurrentPath(path);
			showParentItem();
			listController.findFile();
		}
	}

	@Override
	public void onConfigurationChanged(
			android.content.res.Configuration newConfig) {
		showCurPathView();
		if (!DisplayHelper.isTabModel(context)) {//normal phone show image size only in landscape
			notifyAdapterRefresh();
		}
		if (imageDialog != null && imageDialog.isShowing()) {
			imageDialog.onConfigChange();
		}
	}

	@Override
	public void setActionBar(TabActionBar actionBar) {
		this.actionBar = actionBar;
		if (AccessController.getInstance().getAccessMode() != AccessController.ACCESS_MODE_FILEMANAGER) {
			actionBar.clearActionIcon();
			actionBar.addThumbIcon();
			//actionBar.addSortIcon();
			//if (!SettingProperties.isMainViewSlidingEnable(context) || DisplayHelper.isTabModel(context)) {
			actionBar.addAddIcon();
			//}
			actionBar.addRefreshIcon();
			actionBar.setOnIconClickListener(actionIconListener);
		}
	}

	OnClickListener actionIconListener = new OnClickListener() {

		@Override
		public void onClick(View v) {
			switch (v.getId()) {
				case R.id.actionbar_add:
					openCreateFolderDialog();
					break;
//			case R.id.actionbar_sort:
//				showSortPopup(v);
//				break;
				case R.id.actionbar_thumb:
					showViewModePopup(v);
					break;
				case R.id.actionbar_refresh:
					refresh();
					break;

				default:
					break;
			}
		}
	};

	protected void showViewModePopup(View v) {
		PopupMenu menu = new PopupMenu(context, v);
		menu.getMenuInflater().inflate(R.menu.filemanager_view_mode, menu.getMenu());
		menu.show();
		menu.setOnMenuItemClickListener(viewModeListener);
	}

	OnMenuItemClickListener viewModeListener = new OnMenuItemClickListener() {

		@Override
		public boolean onMenuItemClick(MenuItem item) {

			switch (item.getItemId()) {
				case R.id.menu_cover_thumb:
					startThumbView();
					break;
				case R.id.menu_book_view:
					startBookView(null);
					break;
			}
			return true;
		}

	};
	
	/*
	private void showSortPopup(View v) {
		PopupMenu menu = new PopupMenu(context, v);
		menu.getMenuInflater().inflate(R.menu.sort_order, menu.getMenu());
		menu.show();
		menu.setOnMenuItemClickListener(sortListener);
	}
	*/
	
	/*
	OnMenuItemClickListener sortListener = new OnMenuItemClickListener() {
		
		@Override
		public boolean onMenuItemClick(MenuItem item) {
			switch (item.getItemId()) {
			case R.id.menu_by_date:
				sortByTime();
				break;
			case R.id.menu_by_name:
				sortByName();
				break;
				
			default:
				break;
			}
			return true;
		}
	};
	*/

}
