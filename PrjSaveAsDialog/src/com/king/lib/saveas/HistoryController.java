package com.king.lib.saveas;

import java.util.ArrayList;
import java.util.List;

import android.content.Context;
import android.content.SharedPreferences;
import android.preference.PreferenceManager;

/**
 * @author JingYang
 * @version create time：2016-1-29 上午10:34:32
 *
 */
public class HistoryController {

	private static final String KEY_PATHLIST = "key_download_path_list";
	private static final int MAX_PATH_NUMBER = 7;

	public static List<String> getLatestPaths(Context context, String defaultPath) {
		List<String> list = new ArrayList<String>();
		SharedPreferences preferences = PreferenceManager.getDefaultSharedPreferences(context);
		String string = preferences.getString(KEY_PATHLIST, null);
		if (string == null) {
			if (defaultPath == null) {
				list.add(SimpleFolderSelectDialog.SDCARD);
			}
			else {
				list.add(defaultPath);
			}
		}
		else {
			String[] array = string.split(",");
			for (String path:array) {
				list.add(path);
			}
			if (array.length == 0) {
				list.add(SimpleFolderSelectDialog.SDCARD);
			}
		}
		return list;
	}

	public static void saveLatestPath(Context context, List<String> pathList) {
		if (pathList.size() > MAX_PATH_NUMBER) {
			for (int i = pathList.size() - 1; i > MAX_PATH_NUMBER - 1 ; i --) {
				pathList.remove(i);
			}
		}

		StringBuffer buffer = new StringBuffer();
		buffer.append(pathList.get(0));
		for (int i = 1; i < pathList.size(); i ++) {
			buffer.append(",").append(pathList.get(i));
		}

		SharedPreferences preferences = PreferenceManager.getDefaultSharedPreferences(context);
		SharedPreferences.Editor editor = preferences.edit();
		editor.putString(KEY_PATHLIST, buffer.toString());
		editor.commit();
	}

}
