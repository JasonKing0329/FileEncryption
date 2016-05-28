package com.king.app.fileencryption.filemanager.view;

import java.io.File;
import java.io.FileFilter;
import java.io.FilenameFilter;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.List;

import com.king.app.fileencryption.FileManagerActivityUpdate;
import com.king.app.fileencryption.R;
import com.king.app.fileencryption.TabActionBar;
import com.king.app.fileencryption.book.BookActivity;
import com.king.app.fileencryption.controller.AccessController;
import com.king.app.fileencryption.controller.EncrypterFactory;
import com.king.app.fileencryption.controller.FileManagerAction;
import com.king.app.fileencryption.controller.PageSwitcher;
import com.king.app.fileencryption.controller.PictureManagerUpdate;
import com.king.app.fileencryption.data.Configuration;
import com.king.app.fileencryption.data.Constants;
import com.king.app.fileencryption.data.DBInfor;
import com.king.app.fileencryption.publicview.ChangeThemeDialog;
import com.king.app.fileencryption.publicview.CustomDialog;
import com.king.app.fileencryption.publicview.DefaultDialogManager;
import com.king.app.fileencryption.setting.SettingProperties;
import com.king.app.fileencryption.slidingmenu.SlidingMenuCreator;
import com.king.app.fileencryption.thumbfolder.ShowImageDialog;
import com.king.app.fileencryption.thumbfolder.ThumbFolderActivity;
import com.king.app.fileencryption.tool.Encrypter;
import com.king.app.fileencryption.tool.Generater;
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

@Deprecated
public class FileManagerPage implements FileManagerAction {

	private final String TAG = "FileManagerPage";

	private Context context;
	private TextView currentPathView;
	private ImageView parentView;
	private TextView nameTagView, timeTagView;
	private ImageView nameSortIconView, timeSortIconView;

	private ListView fileListView;
	private FileListAdapter adapter;
	private List<FilePageItem> filePageItemList;
	private List<String> originNameList;
	private RadioButton allRadio, encryptedRadio, unencryptedRadio;
	private final int FILE_TYPE_ALL = 0;
	private final int FILE_TYPE_ENCRYPTED = 1;
	private final int FILE_TYPE_UNENCRYPTED = 2;
	private int currentType = FILE_TYPE_ALL;
	private FileListAction listAction;
	public static String currentPath;
	private ProgressDialog progressDialog;
	private TabActionBar actionBar;
	private PageSwitcher pageSwitcher;

	private LinearLayout layout;
	private Bitmap background;
	private ShowImageDialog imageDialog;

	public FileManagerPage(Context context, PageSwitcher switcher) {
		this.context = context;
		this.pageSwitcher = switcher;
		if (listAction == null) {
			listAction = new FileListAction();
		}

		initViewElement();
		initView();
	}

	private void initView() {
		currentPath = Configuration.APP_DIR_IMG;
		showParentItem();
		layout.setBackgroundColor(Color.WHITE);
		refresh();
	}

	private void initViewElement() {
		Activity view = (Activity) context;

		layout = (LinearLayout) view.findViewById(R.id.layout_page_filemanager);
		currentPathView = (TextView) view.findViewById(R.id.filelist_current_dir);
		parentView = (ImageView) view.findViewById(R.id.filelist_parent);
		nameTagView = (TextView) view.findViewById(R.id.filelist_tag_name);
		timeTagView = (TextView) view.findViewById(R.id.filelist_tag_time);
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
		resetTimeSortTag();
		resetNameSortTag();

		allRadio.setOnCheckedChangeListener(listAction);
		encryptedRadio.setOnCheckedChangeListener(listAction);
		unencryptedRadio.setOnCheckedChangeListener(listAction);
	}

	private void resetNameSortTag() {
		nameSortIconView.setVisibility(View.GONE);
		nameSortIconView.setTag(true);//按名称默认升序
	}
	private void resetTimeSortTag() {
		timeSortIconView.setVisibility(View.GONE);
		timeSortIconView.setTag(false);//按时间默认降序
	}

	@Override
	public void reInit() {
		initViewElement();
		setBackground(background);
		if (currentType == FILE_TYPE_ALL) {
			allRadio.setChecked(true);
		}
		else if (currentType == FILE_TYPE_ENCRYPTED) {
			encryptedRadio.setChecked(true);
		}
		else if (currentType == FILE_TYPE_UNENCRYPTED) {
			unencryptedRadio.setChecked(true);
		}
		showParentItem();
//		currentPathView.setText(currentPath);
//		if (adapter != null) {
//			adapter.updateList(fileList);
//			fileListView.setAdapter(adapter);
//		}
		if (SettingProperties.isShowFileOriginMode(context)) {
			if (originNameList == null || originNameList.size() != filePageItemList.size()) {
				refresh();
			}
			else {
				notifyAdapterRefresh();
			}
		}
		else {
			notifyAdapterRefresh();
		}
		fileListView.setAdapter(adapter);
	}

	private void refresh() {
		if (SettingProperties.isShowFileOriginMode(context)) {
			if (originNameList == null) {
				originNameList = new ArrayList<String>();
			}
			else {
				originNameList.clear();
			}
		}
		listAction.findFile();
	}

	private void notifyAdapterRefresh() {
		currentPathView.setText(currentPath);
		if (filePageItemList == null) {
			Toast.makeText(context, R.string.error_app_root_not_exist, Toast.LENGTH_LONG).show();
			return;
		}
		Log.i(TAG, "notifyAdapterRefresh -> " + currentPath);

		if (adapter == null) {
			adapter = new FileListAdapter(filePageItemList, context);
			fileListView.setAdapter(adapter);
		}
		else {
			adapter.updateList(filePageItemList);
			adapter.notifyDataSetChanged();
		}
	}

	private void showParentItem() {
		if (currentPath.equals(Configuration.APP_DIR_IMG)) {
			parentView.setVisibility(View.GONE);
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
				listAction.encryptCurFolder();
				break;
			case R.id.menu_decipher_current_folder:
				listAction.decipherCurFolder();
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
			boolean excuted = false;
			switch (v.getId()) {
				case R.string.menu_thumb_folder:
					startThumbView();
					break;
//			case R.string.menu_new_folder:
//				openCreateFolderDialog();
//				break;
				case R.string.menu_encrypt_current_folder:
					listAction.encryptCurFolder();
					excuted = true;
					break;
				case R.string.menu_decipher_current_folder:
					listAction.decipherCurFolder();
					excuted = true;
					break;
				case R.string.menu_export:
					DBInfor.export(context);
					excuted = true;
					break;
				case R.string.menu_load:
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
				((FileManagerActivityUpdate) context).slidingMenuListener.onClick(v);
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
						String filePath = currentPath + "/" + name;
						if (listAction.createFolder(new File(filePath))) {
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
	public void onBackPressed() {
		if (parentView.getVisibility() != View.VISIBLE) {
			((Activity) context).finish();
		}
		else {
			if (listAction != null) {
				listAction.findParent();
			}
		}

	}

	@Override
	public void saveState() {

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

	private class FileListAction implements Callback, OnItemClickListener
			, OnItemLongClickListener, OnClickListener, OnCheckedChangeListener {

		private Handler handler = null;
		private Encrypter encrypter;
		private Generater generater;

		public FileListAction() {
			encrypter = EncrypterFactory.create();
			generater = EncrypterFactory.generater();
			handler = new Handler(this);
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
				if (msg.what == FILE_TYPE_ENCRYPTED) {
					Toast.makeText(context, R.string.filelist_encrypt_fail, Toast.LENGTH_LONG).show();
				}
				else if (msg.what == FILE_TYPE_UNENCRYPTED) {
					Toast.makeText(context, R.string.filelist_decipher_fail, Toast.LENGTH_LONG).show();
				}
			}
			cancelProgress();
			return true;
		}

		private boolean isEncrypted(String name) {
			if (name.endsWith(encrypter.getFileExtra())) {
				return true;
			}
			return false;
		}

		public boolean createFolder(File file) {
			if (!file.exists()) {
				Log.i(TAG, "createFolder");
				file.mkdir();
				return true;
			}
			return false;
		}
		private boolean deleteFile(int position) {
			final File file = filePageItemList.get(position).getFile();
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
						if (file.isDirectory()) {
							File[] sub = file.listFiles();
							for (int i = 0; i < sub.length; i ++) {
								sub[i].delete();
							}
						}
						file.delete();
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

		private void encryptCurFolder() {
			showProgress();
			new Thread() {

				public void run() {
					File files[] = new File(currentPath).listFiles();
					for (File file:files) {
						if (!file.isDirectory()
								&& !file.getName().endsWith(encrypter.getFileExtra())
								&& !file.getName().endsWith(encrypter.getNameExtra())) {
							encrypter.encrypt(file, generater.generateName());
						}
					}
					Message message = new Message();
					message.what = FILE_TYPE_ENCRYPTED;
					Bundle bundle = new Bundle();
					bundle.putBoolean("result", true);
					message.setData(bundle);
					handler.sendMessage(message);
				}
			}.start();
		}

		private boolean encryptFile(int position) {
			final File file = filePageItemList.get(position).getFile();
			if (file.exists() && !file.isDirectory()) {
				showProgress();
				new Thread() {

					public void run() {
						boolean result = encrypter.encrypt(file, generater.generateName())
								== null ? false:true;
						Log.i(TAG, " encryptFile " + result + " " + file.getPath());
						Message message = new Message();
						message.what = FILE_TYPE_ENCRYPTED;
						Bundle bundle = new Bundle();
						bundle.putBoolean("result", result);
						message.setData(bundle);
						handler.sendMessage(message);
					}
				}.start();
			}
			return false;
		}

		private void decipherCurFolder() {
			showProgress();
			new Thread() {

				public void run() {
					File files[] = new File(currentPath).listFiles();
					for (File file:files) {
						if (!file.isDirectory()
								&& file.getName().endsWith(encrypter.getFileExtra())) {
							encrypter.restore(file, null);
						}
					}
					Message message = new Message();
					message.what = FILE_TYPE_UNENCRYPTED;
					Bundle bundle = new Bundle();
					bundle.putBoolean("result", true);
					message.setData(bundle);
					handler.sendMessage(message);
				}
			}.start();
		}

		private boolean decipherFile(int position) {
			final File file = filePageItemList.get(position).getFile();
			if (file.exists() && !file.isDirectory()) {
				showProgress();
				new Thread() {

					public void run() {
						boolean result = encrypter.restore(file, null);
						Log.i(TAG, " decipherFile " + result + " " + file.getPath());
						Message message = new Message();
						message.what = FILE_TYPE_UNENCRYPTED;
						Bundle bundle = new Bundle();
						bundle.putBoolean("result", result);
						message.setData(bundle);
						handler.sendMessage(message);
					}
				}.start();
			}
			return false;
		}

		public void findEncryptedFile() {
			Log.i(TAG, "findEncryptedFile");
			File[] files = new File(currentPath).listFiles(new FileFilter() {

				@Override
				public boolean accept(File file) {
					return encrypter.isEncrypted(file) || file.isDirectory();
				}

			});
			createPageItems(files);
			currentType = FILE_TYPE_ENCRYPTED;
		}

		private void createPageItems(File[] files) {
			if (files.length > 0) {
				boolean showOriginName = SettingProperties.isShowFileOriginMode(context);
				filePageItemList = new ArrayList<FilePageItem>();
				FilePageItem item = null;
				for (File f:files) {
					item = new FilePageItem();
					item.setFile(f);
					item.setDate(f.lastModified());
					if (encrypter.isEncrypted(f)) {
						item.setOriginName(encrypter.decipherOriginName(f));
					}
					if (showOriginName && item.getOriginName() != null) {
						item.setDisplayName(item.getOriginName());
					}
					else {
						item.setDisplayName(f.getName());
					}
					filePageItemList.add(item);
				}
			}
			else {
				filePageItemList.clear();
			}
		}

		public void findUnEncryptedFile() {
			Log.i(TAG, "findUnEncryptedFile");
			File[] files = new File(currentPath).listFiles(new FilenameFilter() {

				@Override
				public boolean accept(File arg0, String name) {

					return !name.endsWith(encrypter.getFileExtra()) && !name.endsWith(encrypter.getNameExtra());
				}
			});
			createPageItems(files);
			currentType = FILE_TYPE_UNENCRYPTED;
		}

		public void findAllFile() {
			Log.i(TAG, "findAllFile");
			File[] files = new File(currentPath).listFiles(new FilenameFilter() {//过滤掉name的encrypt文件

				@Override
				public boolean accept(File arg0, String name) {

					return !name.endsWith(encrypter.getNameExtra());
				}
			});
			createPageItems(files);
			currentType = FILE_TYPE_ALL;

		}

		public void findFile() {
			if (allRadio.isChecked()) {
				findAllFile();
			}
			else if (encryptedRadio.isChecked()) {
				findEncryptedFile();
			}
			else if (unencryptedRadio.isChecked()) {
				findUnEncryptedFile();
			}
			notifyAdapterRefresh();
		}

		public void findParent() {
			File file = new File(currentPath);
			file = file.getParentFile();
			currentPath = file.getPath();
			if (file.getPath().equals(Configuration.APP_DIR_IMG)) {
				showParentItem();
				findFile();
			}
			else {
				findFile();
			}
		}

		protected void openByWall(String path) {
			Bundle bundle = new Bundle();
			bundle.putInt(WallActivity.MODE_KEY, WallActivity.MODE_FOLDER);
			bundle.putString(WallActivity.MODE_VALUE_KEY, path);
			pageSwitcher.openWallGallery(bundle);
		}

		@Override
		public boolean onItemLongClick(AdapterView<?> arg0, View arg1,
									   int position, long arg3) {
			final int pos = position;
			Log.i(TAG, "onItemLongClick" + pos);
			final FilePageItem item = filePageItemList.get(position);
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
								else if (which == 2) {//book view
									startBookView(item.getFile().getPath());
								}
							}
						}).show();
			}
			else {
				if (isEncrypted(item.getFile().getPath())) {
					String[] arrays = context.getResources().getStringArray(R.array.filelist_longclick_action_decipher);
					new AlertDialog.Builder(context)
							.setItems(arrays, new DialogInterface.OnClickListener() {

								@Override
								public void onClick(DialogInterface dialog, int which) {
									if (which == 0) {
										decipherFile(pos);
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
										encryptFile(pos);
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
			File file = filePageItemList.get(position).getFile();
			if (file.isDirectory()) {
				currentPath = file.getPath();
				if (parentView.getVisibility() == View.GONE) {
					showParentItem();
				}

				resetTimeSortTag();
				resetNameSortTag();

				findFile();
			}
			else if (encrypter.isEncrypted(file)) {
				if (AccessController.getInstance().getAccessMode() > AccessController.ACCESS_MODE_PRIVATE) {
					showImage(file.getPath());
				}
			}
		}

		@Override
		public void onClick(View v) {
			if (v == parentView) {
				Log.i(TAG, "parent onClick");

				resetTimeSortTag();
				resetNameSortTag();

				File file = new File(currentPath);
				if (!file.exists()) {
					findFile();
				}
				else {
					findParent();
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
				sortByName(decrease);
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
				sortByTime(decrease);
			}
		}

		@Override
		public void onCheckedChanged(CompoundButton buttonView,
									 boolean isChecked) {
			if (isChecked) {
				Log.i(TAG, "radio before check currentType = " + currentType);
				if (buttonView == allRadio) {
					encryptedRadio.setChecked(false);
					unencryptedRadio.setChecked(false);
					if (currentType != FILE_TYPE_ALL) {
						findFile();
					}
				}
				else if (buttonView == encryptedRadio) {
					allRadio.setChecked(false);
					unencryptedRadio.setChecked(false);
					if (currentType != FILE_TYPE_ENCRYPTED) {
						findFile();
					}
				}
				else if (buttonView == unencryptedRadio) {
					encryptedRadio.setChecked(false);
					allRadio.setChecked(false);
					if (currentType != FILE_TYPE_UNENCRYPTED) {
						findFile();
					}
				}

				if (nameSortIconView.getVisibility() == View.VISIBLE) {
					boolean decrease = (Boolean) nameSortIconView.getTag();
					sortByName(decrease);
				}
				else if (timeSortIconView.getVisibility() == View.VISIBLE) {
					boolean decrease = (Boolean) timeSortIconView.getTag();
					sortByTime(decrease);
				}
				Log.i(TAG, "radio after check currentType = " + currentType);
			}
		}
	}

	@Override
	public void onConfigurationChanged(
			android.content.res.Configuration newConfig) {
		if (imageDialog != null && imageDialog.isShowing()) {
			imageDialog.onConfigChange();
		}
	}

	@Override
	public void onResume() {
		showParentItem();
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

	private void sortByTime(final boolean decrease) {
		if (filePageItemList != null) {
			Collections.sort(filePageItemList, new Comparator<FilePageItem>() {

				@Override
				/**
				 * compare 需要用-1,0,1表示大小关系，不能用<0,>0判断
				 * @param lhs
				 * @param rhs
				 * @return -1, 0, 1
				 */
				public int compare(FilePageItem lhs, FilePageItem rhs) {

					long result = 0;
					if (decrease) {
						result = rhs.getDate() - lhs.getDate();
					}
					else {
						result = lhs.getDate() - rhs.getDate();
					}

					if (result < 0) {
						return - 1;
					}
					if (result == 0) {
						return 0;
					}
					else {
						return 1;
					}
				}
			});
			notifyAdapterRefresh();
		}
	}

	private void sortByName(final boolean decrease) {
		if (filePageItemList != null) {
			Collections.sort(filePageItemList, new Comparator<FilePageItem>() {

				@Override
				public int compare(FilePageItem lhs, FilePageItem rhs) {

					if (decrease) {
						return rhs.getDisplayName().toLowerCase().compareTo(lhs.getDisplayName().toLowerCase());
					}
					else {
						return lhs.getDisplayName().toLowerCase().compareTo(rhs.getDisplayName().toLowerCase());
					}
				}
			});
			notifyAdapterRefresh();
		}
	}
	
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
