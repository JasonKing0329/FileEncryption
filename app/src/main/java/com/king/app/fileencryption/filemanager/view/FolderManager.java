package com.king.app.fileencryption.filemanager.view;

import java.io.File;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;

import com.king.app.fileencryption.data.Configuration;

public class FolderManager {

	public List<File> loadList(String rootPath) {
		List<File> list = null;
		File file = new File(rootPath);
		File[] files = file.listFiles();
		if (files.length > 0) {
			list = new ArrayList<File>();
			for (File f:files) {
				list.add(f);
			}
		}
		return list;
	}

	public List<File> addSubFolders(File file) {
		List<File> list = null;
		File[] subFile = file.listFiles();
		if (file.isDirectory()) {
			if (subFile.length > 0) {
				list = new ArrayList<File>();
				for (File f:subFile) {
					if (f.isDirectory()) {
						list.add(f);
					}
				}
			}
		}
		if (list != null && list.size() == 0) {
			list = null;
		}
		if (list != null) {
			Collections.sort(list, new Comparator<File>() {

				@Override
				public int compare(File f1, File f2) {

					return f1.getName().compareTo(f2.getName());
				}
			});
		}
		return list;
	}

	public boolean hasFolderChild(File file) {
		File[] subFile = file.listFiles();
		if (file.isDirectory()) {
			if (subFile.length > 0) {
				for (File f:subFile) {
					if (f.isDirectory()) {
						return true;
					}
				}
			}
		}
		return false;
	}

	/**
	 * collect all deepest folder in list
	 * @param targetList
	 * @param file
	 * @return
	 */
	private boolean collectFolders(List<File> targetList, File file) {
		boolean hasChildFolder = false;
		File[] child = file.listFiles();
		for (int i = 0; i < child.length; i ++) {
			if (child[i].isDirectory()) {
				hasChildFolder = collectFolders(targetList, child[i]);
			}
		}
		if (!hasChildFolder) {
			targetList.add(file);
			return true;
		}
		return false;
	}

	/**
	 * collect all deepest folder in one list
	 * apply in thumbfolder folder list and bookview keyword flow
	 * @return
	 */
	public List<File> collectAllFolders() {
		List<File> folderList = new ArrayList<File>();
		File file = new File(Configuration.APP_DIR_IMG);
		collectFolders(folderList, file);
		return folderList;
	}
	
	/**
	 * 
	 * @param parent parent folder path
	 * @param folderName target folder name to create
	 * @return if null, folder already exist
	 */
	public File createFolder(String parent, String folderName) {
		if (parent == null) {
			parent = Configuration.APP_DIR_IMG;
		}
		
		File file = new File(parent + "/" + folderName);
		if (file.exists()) {
			return null;
		}
		
		file.mkdir();
		return file;
	}
}
