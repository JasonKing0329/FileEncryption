package com.king.app.fileencryption.filemanager.controller;

import java.io.File;
import java.io.FileFilter;
import java.io.FilenameFilter;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;
import java.util.Locale;

import android.content.Context;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.util.Log;

import com.king.app.fileencryption.controller.EncrypterFactory;
import com.king.app.fileencryption.data.Configuration;
import com.king.app.fileencryption.filemanager.view.FilePageItem;
import com.king.app.fileencryption.setting.SettingProperties;
import com.king.app.fileencryption.tool.Encrypter;
import com.king.app.fileencryption.tool.Generater;

public class FileListController {

	private final String TAG = "FileListController";

	private Context mContext;
	private Encrypter encrypter;
	private Generater generater;
	private Handler handler;
	private String currentPath;
	private FileChangeListener fileChangeListener;

	private List<FilePageItem> filePageItemList;

	public static final int FILE_TYPE_ALL = 0;
	public static final int FILE_TYPE_ENCRYPTED = 1;
	public static final int FILE_TYPE_UNENCRYPTED = 2;
	private int currentType = FILE_TYPE_ALL;

	private boolean isFindAll, isFindEncrypted, isFindUnEncrypted;
	public static final int SORT_BY_NONE = 0;
	public static final int SORT_BY_NAME = 1;
	public static final int SORT_BY_DATE = 2;
	private int sortMode;
	private boolean sortNameDesc, sortDateDesc;

	private TraceNode traceNode;

	/**
	 * in this structure, folder trace tree only record its parent and one child
	 * if access another child, it will replace the previous child
	 * @author Yang Jing
	 *
	 */
	private class TraceNode {
		TraceNode parent;
		TraceNode child;
		int scrollPosition;
		String path;

		public TraceNode(int position) {
			scrollPosition = position;
		}
	}

	public FileListController(Context context, Handler handler, FileChangeListener listener) {
		this.handler = handler;
		mContext = context;
		fileChangeListener = listener;
		encrypter = EncrypterFactory.create();
		generater = EncrypterFactory.generater();
		currentPath = Configuration.APP_DIR_IMG;
		isFindAll = true;
		sortMode = SORT_BY_NONE;

		traceNode = new TraceNode(0);
		traceNode.path = currentPath;
	}

	public String getCurrentPath() {
		return currentPath;
	}

	public void setCurrentPath(String path) {
		this.currentPath = path;
	}

	public List<FilePageItem> getFilePageItemList() {
		return filePageItemList;
	}

	public int getFileType() {
		return currentType;
	}

	public void setSortMode(int mode, boolean decrease) {
		sortMode = mode;
		if (sortMode == SORT_BY_NAME) {
			sortNameDesc = decrease;
		}
		else if (sortMode == SORT_BY_DATE) {
			sortDateDesc = decrease;
		}
	}

	public void setFindAll(boolean isFindAll) {
		this.isFindAll = isFindAll;
	}

	public void setFindEncrypted(boolean isFindEncrypted) {
		this.isFindEncrypted = isFindEncrypted;
	}

	public void setFindUnEncrypted(boolean isFindUnEncrypted) {
		this.isFindUnEncrypted = isFindUnEncrypted;
	}

	public boolean isRootFolder() {
		return currentPath.equals(Configuration.APP_DIR_IMG);
	}

	public boolean isEncryptedFile(File file) {
		return encrypter.isEncrypted(file);
	}

	public boolean isEncrypted(String name) {
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

	public void encryptCurFolder() {
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

	public boolean encryptFile(int position) {
		final File file = filePageItemList.get(position).getFile();
		if (file.exists() && !file.isDirectory()) {
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

	public void decipherCurFolder() {
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

	public boolean decipherFile(int position) {
		final File file = filePageItemList.get(position).getFile();
		if (file.exists() && !file.isDirectory()) {
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

	public void createPageItems(File[] files) {
		if (files.length > 0) {
			boolean showOriginName = SettingProperties.isShowFileOriginMode(mContext);
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
		if (isFindAll) {
			findAllFile();
		}
		else if (isFindEncrypted) {
			findEncryptedFile();
		}
		else if (isFindUnEncrypted) {
			findUnEncryptedFile();
		}

		if (sortMode == SORT_BY_NAME) {
			sortByName(sortNameDesc);
		}
		else if (sortMode == SORT_BY_DATE) {
			sortByTime(sortDateDesc);
		}

		fileChangeListener.onFindFileFinish();
	}

	public void findParent() {
		File file = new File(currentPath);
		file = file.getParentFile();
		currentPath = file.getPath();
		if (file.getPath().equals(Configuration.APP_DIR_IMG)) {
			fileChangeListener.onBackToRoot();
			findFile();
		}
		else {
			findFile();
		}
	}

	public void deleteFile(File file) {
		if (file.isDirectory()) {
			File[] sub = file.listFiles();
			for (int i = 0; i < sub.length; i ++) {
				sub[i].delete();
			}
		}
		file.delete();
	}

	public void sortByTime(final boolean decrease) {
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
			fileChangeListener.onFindFileFinish();
		}
	}

	public void sortByName(final boolean decrease) {
		if (filePageItemList != null) {
			Collections.sort(filePageItemList, new Comparator<FilePageItem>() {

				@Override
				public int compare(FilePageItem lhs, FilePageItem rhs) {

					if (decrease) {
						return rhs.getDisplayName().toLowerCase(Locale.getDefault()).compareTo(lhs.getDisplayName().toLowerCase());
					}
					else {
						return lhs.getDisplayName().toLowerCase(Locale.getDefault()).compareTo(rhs.getDisplayName().toLowerCase());
					}
				}
			});
			fileChangeListener.onFindFileFinish();
		}
	}

	public int getScrollPosition() {
		return traceNode.scrollPosition;
	}

	/**
	 * call this after setCurrentPath
	 * @param position
	 */
	public void updateParentPosition(int position) {
		traceNode.scrollPosition = position;
		if (traceNode.child == null || !currentPath.equals(traceNode.child.path)) {
			TraceNode node = new TraceNode(0);
			node.path = currentPath;
			traceNode.child = node;
			node.parent = traceNode;
		}
		traceNode = traceNode.child;
	}

	/**
	 * call this after setCurrentPath
	 * @param position
	 */
	public void updateChildPosition(int position) {
		if (traceNode.parent != null) {
			traceNode.scrollPosition = position;
			traceNode = traceNode.parent;
		}
	}
}
