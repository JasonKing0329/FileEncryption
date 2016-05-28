package com.king.app.fileencryption.service;

import java.io.File;
import java.io.FileFilter;
import java.util.ArrayList;
import java.util.List;

import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.os.Handler.Callback;

import com.king.app.fileencryption.controller.EncrypterFactory;
import com.king.app.fileencryption.filemanager.view.FolderManager;
import com.king.app.fileencryption.tool.Encrypter;
import com.king.app.fileencryption.tool.Generater;
import com.king.app.fileencryption.tool.SimpleEncrypter;

public class EncryptCheckService implements FileFilter {

	public static final int SERVICE_CHECK = 0;
	public static final int SERVICE_ENCRYPT = 1;
	public static final int SERVICE_INSERT_FILE = 2;
	
	private Encrypter encrypter;
	private Generater generater;
	private List<File> unEncryptedList;
	private Handler handler;
	
	public EncryptCheckService(Callback callback) {
		unEncryptedList = new ArrayList<File>();
		handler = new Handler(callback);
	}

	public void check() {
		unEncryptedList.clear();
		new CheckThread().start();
	}

	public void encrypt() {
		if (generater == null) {
			encrypter = EncrypterFactory.create();
			generater = EncrypterFactory.generater();
		}
		new EncryptThread().start();
	}
	
	private class CheckThread extends Thread {
		@Override
		public void run() {
			List<File> folderList = new FolderManager().collectAllFolders();
			File[] files = null;
			for (int i = 0; i < folderList.size(); i ++) {
				files = folderList.get(i).listFiles(EncryptCheckService.this);
				if (files != null && files.length > 0) {
					for (File file:files) {
						unEncryptedList.add(file);
					}
				}
			}
			
			Message message = new Message();
			message.what = SERVICE_CHECK;
			Bundle bundle = new Bundle();
			if (unEncryptedList.size() > 0) {
				bundle.putBoolean("existed", true);
				bundle.putInt("size", unEncryptedList.size());
			}
			else {
				bundle.putBoolean("existed", false);
			}
			message.setData(bundle);
			handler.sendMessage(message);
		}
	}

	private class EncryptThread extends Thread {
		private List<String> targetList;
		public EncryptThread() {
			targetList = new ArrayList<String>();
		}
		@Override
		public void run() {
			for (File file:unEncryptedList) {
				if (file.exists()) {
					String target = encrypter.encrypt(file, generater.generateName());
					targetList.add(target);
				}
			}
			
			Message message = new Message();
			message.what = SERVICE_CHECK;
			handler.sendMessage(message);
			
			message = new Message();
			message.what = SERVICE_INSERT_FILE;
			message.obj = targetList;
			handler.sendMessage(message);
		}
	}

	@Override
	public boolean accept(File file) {

		return !file.isDirectory() && !file.getName().endsWith(SimpleEncrypter.FILE_EXTRA) 
				&& !file.getName().endsWith(SimpleEncrypter.NAME_EXTRA);
	}
}
