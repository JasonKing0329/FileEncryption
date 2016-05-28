package com.king.app.fileencryption.spicture.controller;

import java.io.File;
import java.util.ArrayList;
import java.util.List;
import java.util.Random;

import com.king.app.fileencryption.R;
import com.king.app.fileencryption.controller.PageSwitcher;
import com.king.app.fileencryption.controller.PictureManagerUpdate;
import com.king.app.fileencryption.controller.WholeRandomManager;
import com.king.app.fileencryption.data.Constants;
import com.king.app.fileencryption.setting.SettingProperties;
import com.king.app.fileencryption.sorder.controller.SOrderPictureBridge;
import com.king.app.fileencryption.sorder.entity.SOrder;
import com.king.app.fileencryption.tool.Encrypter;
import com.king.app.fileencryption.tool.ScreenInfor;

import android.app.Activity;
import android.content.Context;
import android.graphics.Bitmap;
import android.os.Handler;
import android.os.Handler.Callback;
import android.os.Message;
import android.util.Log;
import android.view.ViewGroup.LayoutParams;

public class SpictureController {

	private final String TAG = "SpictureController";

	private Context context;
	private Handler uiHandler;
	private Encrypter encrypter;

	private String currentPath;
	private List<String> fileNameList;

	private SOrderPictureBridge opBridge;
	private SOrder currentOrder;
	private PageSwitcher pageSwitcher;

	private final int SHOW_MODE_RANK = 0;
	public static final int SHOW_MODE_ORDER = 1;
	private final int SHOW_MODE_RANDOM = 2;
	public static final int SHOW_MODE_FOLDER = 3;
	
	public SpictureController(Context context) {
		this.context = context;
		opBridge = SOrderPictureBridge.getInstance(context);
	}
	
	public void registCallback(Callback callback) {
		uiHandler = new Handler(callback);
	}

	public void setEncrpter(Encrypter encrypter) {
		this.encrypter = encrypter;
	}
	
	public void setCurrentPath(String path) {
		currentPath = path;
	}

	public String getCurrentPath() {
		return currentPath;
	}

	public void setCurrentOrder(SOrder order) {
		currentOrder = order;
	}
	
	public void setCurrentOrder(int orderId) {
		currentOrder = opBridge.queryOrder(orderId);
	}

	public SOrder getCurrentOrder() {
		return currentOrder;
	}

	public List<String> getFileNameList() {
		return fileNameList;
	}

	public void loadChooserItems(int mode) {
		new LoadChooserThread(mode).start();
	}

	private class LoadChooserThread extends Thread{

		private int mode = SHOW_MODE_FOLDER;
		
		public LoadChooserThread(int mode) {
			LoadChooserThread.this.mode = mode;
		}

		public void run() {
			if (mode == SHOW_MODE_FOLDER) {
				loadChooserByCurFolder();
			}
			else if (mode == SHOW_MODE_RANDOM) {
				loadChooserByRandom();
			}
			else if (mode == SHOW_MODE_ORDER) {
				loadChooserByOrder();
			}
			else if (mode == SHOW_MODE_RANK) {
				
			}
			Message msg = new Message();
			msg.what = Constants.STATUS_LOAD_CHOOSERITEM_FINISH;
			uiHandler.sendMessage(msg);
		}

	}

	private boolean isImageFile(String name) {

		if (name == null) {
			return false;
		}
		return name.toLowerCase().endsWith(".jpg") || name.endsWith(".jpeg")
				|| name.endsWith(".png") || name.endsWith(".gif")
				|| name.endsWith(".bmp");
	}
	
	//for byFolder fileNameList is fileName
	private void loadChooserByCurFolder() {

		Log.i(TAG, "loadChooserByCurFolder");

		if (fileNameList != null) {
			fileNameList.clear();
		}
		
		File[] files = new File(currentPath).listFiles();

		if (files != null && files.length > 0) {
			for (int i = 0; i < files.length; i++) {
				if (encrypter.isEncrypted(files[i])) {
					String name = encrypter.decipherOriginName(files[i]);
					if (isImageFile(name)) {
						if (fileNameList == null) {
							fileNameList = new ArrayList<String>();
						}
						fileNameList.add(files[i].getPath());
					}
				} else {
					if (isImageFile(files[i].getName())) {
						if (fileNameList == null) {
							fileNameList = new ArrayList<String>();
						}
						fileNameList.add(files[i].getPath());
					}
				}
			}
		}
	}
	//for byRandom fileNameList is filepath
	/*deprecated in v5.8.9, use wholerandommanager api to replace
	@Deprecated
	private void loadChooserByRandom() {

		Log.i(TAG, "loadChooserByRandom");
		recycleResource();
		
		Random random = new Random();
		String curPath = Configuration.APP_DIR_IMG;
		File files[] = new File(curPath).listFiles(new FilenameFilter() {
			
			@Override
			public boolean accept(File parent, String name) {

				return name.endsWith(encrypter.getFileExtra()) || new File(parent.getPath() + "/" + name).isDirectory();
			}
		});
		
		if (files != null && files.length > 0) {
			if (fileNameList == null) {
				fileNameList = new ArrayList<String>();
			}
			else {
				fileNameList.clear();
			}
			int index = 0;
			File file = null;
			
			while (fileNameList.size() < SettingProperties.getCasualLookNumber(context)) {
				index = Math.abs(random.nextInt()) % files.length;
				file = files[index];
				while (file.isDirectory()) {
					File files2[] = file.listFiles();
					if (files2.length == 0) {
						break;
					}
					index = Math.abs(random.nextInt()) % files2.length;
					file = files2[index];
				}
				if (!file.isDirectory() && !fileNameList.contains(file.getPath())) {
					if (encrypter.isEncrypted(file)) {
						String name = encrypter.decipherOriginName(file);
						if (isImageFile(name)) {
							fileNameList.add(file.getPath());
						}
					} else {
						if (isImageFile(file.getPath())) {
							fileNameList.add(file.getPath());
						}
					}
				}
			}
		}
	}
	*/

	/* v5.8.9 updated, use wholerandommanager api to replace*/
	private void loadChooserByRandom() {

		Log.i(TAG, "loadChooserByRandom");
		
		if (fileNameList == null) {
			fileNameList = new ArrayList<String>();
		}
		else {
			fileNameList.clear();
		}
		
		WholeRandomManager manager = new WholeRandomManager(encrypter);
		int total = SettingProperties.getCasualLookNumber(context);
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
			fileNameList.add(path);
		}
		
	}

	//for byOrder fileNameList is filePath
	private void loadChooserByOrder() {

		Log.i(TAG, "loadChooserByOrder");
		
		if (currentOrder != null) {
			opBridge.getOrderItemList(currentOrder);
			fileNameList = currentOrder.getImgPathList();
		}
	}

	public void recycleResource() {
		Log.i(TAG, "recycleResource");
		PictureManagerUpdate.getInstance().recycleSpictureItems();
		if (fileNameList != null) {
			fileNameList.clear();
		}
	}
	public void getSuitableLayout(Bitmap bitmap, int orientation, LayoutParams params) {
		int screenWidth = ScreenInfor.getWidth((Activity) context);
		int screenHeight = ScreenInfor.getHeight((Activity) context);
		int chooserSize = context.getResources().getDimensionPixelSize(R.dimen.spicture_chooser_item_width);

		Log.i(TAG, String.format("(screenWidth,screenHeight,chooserSize):(%d,%d,%d)", screenWidth, screenHeight, chooserSize));
		int maxWidth = screenWidth;
		int maxHeight = screenHeight;
		double zoomFactor = 0;
		if (orientation == 1) {
			maxHeight = screenHeight - chooserSize;
			zoomFactor = 2;
		}
		else if (orientation == 2) {
			int temp = screenHeight;
			screenHeight = screenWidth;
			screenWidth = temp;
			maxWidth = screenWidth - chooserSize;
			zoomFactor = 2;
		}
		int realWidth = bitmap.getWidth();
		int realHeight = bitmap.getHeight();
		Log.i(TAG, String.format("(maxWidth,maxHeight,realWidth,realHeight):(%d,%d,%d,%d)"
				, maxWidth, maxHeight, realWidth, realHeight));
		if (realWidth > realHeight) {
			if ((float) realWidth / (float) realHeight > (float) maxWidth / (float) maxHeight) {
				//free based on X
				if (maxWidth - realWidth < maxWidth / 2) {
					Log.i(TAG, "showImageMatchScreen -> free based on X -> matchparent");
					params.width = LayoutParams.MATCH_PARENT;
					params.height = LayoutParams.MATCH_PARENT;
				} else {
					Log.i(TAG, "showImageMatchScreen -> free based on X -> matchreal");
					params.width = (int)((double)realWidth*zoomFactor);
					params.height = (int)((double)realHeight*zoomFactor*((double)realHeight/(double)realWidth));
				}
			}
			else {
				//restrict Y max = maxHeight, based on Y
				if (maxWidth - realWidth < maxWidth / 2) {
					Log.i(TAG, "showImageMatchScreen -> restrict Y max = maxHeight, based on Y -> matchparent");
					params.width = LayoutParams.MATCH_PARENT;
					params.height = LayoutParams.MATCH_PARENT;
				} else {
					Log.i(TAG, "showImageMatchScreen -> restrict Y max = maxHeight, based on Y -> usereal");
					params.width = (int)((double)realWidth*zoomFactor);
					params.height = (int)((double)realHeight*zoomFactor*((double)realHeight/(double)realWidth));
				}
			}
		}
		else {
			if ((float) realHeight / (float) realWidth > (float) maxHeight / (float) maxWidth) {
				//free based on Y
				if (maxHeight - realHeight < maxHeight / 2) {
					Log.i(TAG, "showImageMatchScreen -> free based on Y -> matchparent");
					params.width = LayoutParams.MATCH_PARENT;
					params.height = LayoutParams.MATCH_PARENT;
				} else {
					Log.i(TAG, "showImageMatchScreen -> free based on Y -> usereal");
					params.width = (int)((double)realWidth*zoomFactor*((double)realWidth/(double)realHeight));
					params.height = (int)((double)realHeight*zoomFactor);
				}
			}
			else {
				//restrict X max = maxWidth, based on X
				if (maxHeight - realHeight < maxHeight / 2) {
					Log.i(TAG, "showImageMatchScreen -> restrict X max = maxHeight, based on X -> matchparent");
					params.width = LayoutParams.MATCH_PARENT;
					params.height = LayoutParams.MATCH_PARENT;
				} else {
					Log.i(TAG, "showImageMatchScreen -> restrict X max = maxHeight, based on X -> usereal");
					params.width = (int)((double)realWidth*zoomFactor*((double)realWidth/(double)realHeight));
					params.height = (int)((double)realHeight*zoomFactor);
				}
			}
		}
		Log.i(TAG, String.format("(paramW, paramH):(%d,%d)", params.width, params.height));
	}
	
	public boolean deleteItemFromOrder(int pos) {
		boolean result = false;
		if (currentOrder != null) {
			if (opBridge.deleteItemFromOrder(currentOrder, pos)) {
				currentOrder.setItemNumber(currentOrder.getItemNumber() + 1);
				result = true;
			}
		}
		return result;
	}

	public void deleteItemFromFolder(String path) {
		File file = new File(path);
		if (encrypter.isEncrypted(file)) {
			file.delete();
			path = path.replace(encrypter.getFileExtra(), encrypter.getNameExtra());
			file = new File(path);
			file.delete();
			Log.i("FileEncryption", "delete file " + file.getPath());
		}
		else {
			if (file.exists()) {
				file.delete();
			}
		}
	}
	public boolean setOrderCover(SOrder order) {
		boolean result = false;
		if (opBridge.setOrderCover(order)) {
			result = true;
		}
		return result;
	}

	public boolean isItemExist(String path, int orderId) {
		boolean result = false;
			if (opBridge.isItemExist(path, orderId)) {
				result = true;
			}
		return result;
	}
	public boolean addItemToOrder(String path, SOrder order) {
		boolean result = false;
			if (opBridge.addToOrder(path, order.getId())) {
				order.setItemNumber(order.getItemNumber() + 1);
				result = true;
			}
		return result;
	}

	public void switchToRandomGame() {
			pageSwitcher = (PageSwitcher) context;
			pageSwitcher.openRandomGame(currentOrder);
	}

	public void shuffleList(int currentShowMode) {
		if (fileNameList != null && fileNameList != null) {
			Log.i(TAG, "shuffleList");
			Random random = new Random();
			int size = fileNameList.size();
			String tempString = null;
			int index = 0;
			for (int i = 0; i < size; i ++) {
				index = Math.abs(random.nextInt()) % size;

				if (currentShowMode == SHOW_MODE_ORDER) {
					opBridge.shuffleOrderItems(currentOrder, i, index);
				}
				else {
					tempString = fileNameList.get(i);
					fileNameList.set(i, fileNameList.get(index));
					fileNameList.set(index, tempString);
				}
			}
		}
	}

	public boolean isImageExist(int index) {
		String path = fileNameList.get(index);
		if (path == null) {
			return false;
		}
		File file = new File(path);
		if (!file.exists() || file.isDirectory()) {
			return false;
		}
		return true;
	}
	
}
