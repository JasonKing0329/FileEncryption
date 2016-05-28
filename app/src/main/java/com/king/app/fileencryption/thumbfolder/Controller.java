package com.king.app.fileencryption.thumbfolder;

import java.io.File;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;

import android.content.Context;
import android.util.Log;

import com.king.app.fileencryption.filemanager.view.FolderManager;
import com.king.app.fileencryption.sorder.controller.SOrderPictureBridge;
import com.king.app.fileencryption.sorder.entity.SOrder;
import com.king.app.fileencryption.tool.Encrypter;
import com.king.app.fileencryption.tool.SimpleEncrypter;

public class Controller {

	private Context mContext;
	private SOrderPictureBridge sOrderPictureBridge;
	private Encrypter encrypter;
	
	public Controller(Context context) {

		mContext = context;
		encrypter = new SimpleEncrypter();
		sOrderPictureBridge = SOrderPictureBridge.getInstance(mContext);
	}

	public Encrypter getEncrypter() {
		return encrypter;
	}
	
	public List<SOrder> loadOrderList() {
		List<SOrder> list = null;
		sOrderPictureBridge.loadOrders();
		sOrderPictureBridge.sortOrderByName();
		list = sOrderPictureBridge.getOrderList();
		return list;
	}

	public void deleteItemFromOrder(SOrder currentOrder, int index) {
		sOrderPictureBridge.deleteItemFromOrder(currentOrder, index);
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

	public void accessOrder(SOrder order) {
		sOrderPictureBridge.accessOrder(order);
	}

	public String getFileOriginName(File file) {
		if (encrypter.isEncrypted(file)) {
			return encrypter.decipherOriginName(file);
		}
		return null;
	}

	public List<File> getAllFolders() {
		List<File> folderList = new FolderManager().collectAllFolders();
		
		Collections.sort(folderList, new Comparator<File>() {

			@Override
			public int compare(File f1, File f2) {
				
				return f1.getName().toUpperCase().compareTo(f2.getName().toUpperCase());
			}
		});
		return folderList;
	}

}
