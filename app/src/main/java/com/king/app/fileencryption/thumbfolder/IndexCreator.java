package com.king.app.fileencryption.thumbfolder;

import java.io.File;
import java.util.HashMap;
import java.util.List;
import java.util.TreeSet;

import com.king.app.fileencryption.sorder.entity.SOrder;

public class IndexCreator {

	private IndexView indexView;
	private HashMap<String, Integer> indexMap;
	
	public IndexCreator(IndexView indexView) {
		this.indexView = indexView;
		indexMap = new HashMap<String, Integer>();
	}

	public void createFromFileList(List<File> list) {
		indexMap.clear();
		indexView.clearAllIndex();
		if (list != null && list.size() > 0) {
			TreeSet<String> set = new TreeSet<String>();
			String index = null;
			for (int i = 0; i < list.size(); i ++) {
				index = "" + list.get(i).getName().toUpperCase().charAt(0);
				if (set.add(index)) {
					indexMap.put(index, i);
					indexView.addIndex(index);
				}
			}
			indexView.build();
		}
	}
	
	public void createFromOrderList(List<SOrder> list) {
		indexMap.clear();
		indexView.clearAllIndex();
		if (list != null && list.size() > 0) {
			TreeSet<String> set = new TreeSet<String>();
			String index = null;
			for (int i = 0; i < list.size(); i ++) {
				index = "" + list.get(i).getName().toUpperCase().charAt(0);
				if (set.add(index)) {
					indexMap.put(index, i);
					indexView.addIndex(index);
				}
			}
			indexView.build();
		}
	}

	public int getIndexPosition(String index) {
		return indexMap.get(index);
	}
}
