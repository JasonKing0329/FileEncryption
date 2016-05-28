package com.king.app.fileencryption.controller;

import java.io.File;
import java.io.FilenameFilter;
import java.util.ArrayList;
import java.util.List;
import java.util.Random;

import android.util.Log;

import com.king.app.fileencryption.data.Configuration;
import com.king.app.fileencryption.tool.Encrypter;

public class WholeRandomManager {

	private final String TAG = "WholeRandomManager";
	
	/**
	 * folders under APP_DIR_IMG
	 */
	private List<File> rootDirectories;
	
	/**
	 * the last index of available file in each folder
	 */
	private List<Integer> folderStep;
	
	private Encrypter encrypter;
	
	private int total;

	public WholeRandomManager(Encrypter encrypter) {
		this.encrypter = encrypter;
		countAvailableRandom();
	}
	
	private void countAvailableRandom() {
		if (folderStep == null) {
			folderStep = new ArrayList<Integer>();
		}
		else {
			folderStep.clear();
		}
		if (rootDirectories == null) {
			rootDirectories = new ArrayList<File>();
		}
		else {
			rootDirectories.clear();
		}
		
		total = 0;
		File file = new File(Configuration.APP_DIR_IMG);
		collectFolders(file);
	}

	public int getTotal() {
		return total;
	}

	/**
	 * collect all deepest folder in list
	 * @param targetList
	 * @param file
	 * @return
	 */
	private void collectFolders(File file) {

		File[] child = file.listFiles(encryptFileFilter);
		if (child.length > 0) {
			total += child.length;
			folderStep.add(total);
			rootDirectories.add(file);
		}
		else {
			child = file.listFiles();
			for (int i = 0; i < child.length; i ++) {
				if (child[i].isDirectory()) {
					collectFolders(child[i]);
				}
			}
		}
	}

	FilenameFilter encryptFileFilter = new FilenameFilter() {
		
		@Override
		public boolean accept(File dir, String filename) {
			return filename.endsWith(encrypter.getFileExtra());
		}
	};
	
	public String getRandomPath() {
		
		if (folderStep.size() == 0) {
			return null;
		}
		int index = Math.abs(new Random().nextInt()) % folderStep.get(folderStep.size() - 1);
		int begin = 0;
		String filePath = null;
		StringBuffer buffer = new StringBuffer("index = " + index);
		for (int i = 0; i < folderStep.size(); i ++) {
			if (index >= begin && index < folderStep.get(i)) {

				File f = null;
				try {
					File[] subs = rootDirectories.get(i).listFiles(encryptFileFilter);
					f = subs[index - begin];
				} catch (Exception exception) {
					countAvailableRandom();
					Log.i(TAG, "getRandomPath (i, index, begin)("
							+ i + "," + index + "," + begin + ") " + rootDirectories.get(i).getName());
					return null;
				}

				filePath = f.getPath();
				break;
			}
			begin = folderStep.get(i);
		}
		if (filePath == null) {
			Log.d(TAG, buffer.toString());
		}
		return filePath;
	}
}
