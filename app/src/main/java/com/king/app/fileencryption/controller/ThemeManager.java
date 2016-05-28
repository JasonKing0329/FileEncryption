package com.king.app.fileencryption.controller;

import com.king.app.fileencryption.R;

import android.content.Context;
import android.content.SharedPreferences;
import android.preference.PreferenceManager;
import android.util.Log;

public class ThemeManager {

	private final String TAG = "ThemeManager";
	public static final int THEME_FILEMANAGER = 0;

	public static final String THEME_FILEMANAGER_KEY = "theme_fm_value";
	public static final String THEME_THUMBFOLDER_KEY = "theme_tf_value";
	
	private String[] themeKeys;
	private Context context;
	
	private int[] themeFMvalues = new int[] {R.style.FileManagerBlue, R.style.FileManagerGreen
			, R.style.FileManagerLightGreen, R.style.FileManagerOrange, R.style.FileManagerDeepBlue, R.style.FileManagerPurple};
	private int[] themeDrawables = new int[] {R.drawable.theme_blue, R.drawable.theme_green, R.drawable.theme_lightgreen
			, R.drawable.theme_orange, R.drawable.theme_deepblue, R.drawable.theme_purple};
	private int[] themeBasicColors = new int[] {R.color.actionbar_bk_blue, R.color.actionbar_bk_green, R.color.actionbar_bk_lightgreen
			, R.color.actionbar_bk_orange, R.color.actionbar_bk_deepblue, R.color.actionbar_bk_purple};
	private int[] themeDefaultFolder = new int[] {R.drawable.default_folder, R.drawable.default_folder_lightgreen, R.drawable.default_folder_lightgreen
			, R.drawable.default_folder, R.drawable.default_folder, R.drawable.default_folder};
	
	public ThemeManager(Context context) {
		this.context = context;
		themeKeys = context.getResources().getStringArray(R.array.theme_key);
	}
	
	public String[] getThemes() {
		return themeKeys;
	}

	public int[] getThemesDrawables() {
		return themeDrawables;
	}
	
	public int getTheme(String key, int where) {
		int valueArray[] = null;
		int theme = 0;
		valueArray = themeFMvalues;
		for (int i = 0; i < themeKeys.length; i ++) {
			if (key.equals(themeKeys[i])) {
				theme = valueArray[i];
				break;
			}
		}
		return theme;
	}

	public int getTheme(int keyIndex) {
		return themeFMvalues[keyIndex];
	}
	
	public static void init(Context context) {
		SharedPreferences preferences = PreferenceManager.getDefaultSharedPreferences(context);
		String value = preferences.getString(THEME_FILEMANAGER_KEY, null);
		if (value == null) {
			SharedPreferences.Editor editor = preferences.edit();
			editor.putString(THEME_FILEMANAGER_KEY, context.getResources().getStringArray(R.array.theme_key)[0]);
			editor.putString(THEME_THUMBFOLDER_KEY, context.getResources().getStringArray(R.array.theme_key)[0]);
			editor.commit();
		}
	}

	public void saveTheme(String key) {
		SharedPreferences preferences = PreferenceManager.getDefaultSharedPreferences(context);
		SharedPreferences.Editor editor = preferences.edit();
		editor.putString(THEME_FILEMANAGER_KEY, key);
		editor.putString(THEME_THUMBFOLDER_KEY, key);
		editor.commit();
	}
	
	public int getDefaultTheme() {
		SharedPreferences preferences = PreferenceManager.getDefaultSharedPreferences(context);
		String value = null;
		int valueArray[] = null;
		int theme = 0;
		value = preferences.getString(THEME_FILEMANAGER_KEY, themeKeys[0]);
		valueArray = themeFMvalues;

		for (int i = 0; i < themeKeys.length; i ++) {
			if (value.equals(themeKeys[i])) {
				theme = valueArray[i];
				break;
			}
		}
		return theme;
	}

	public int getBasicColorResId() {
		SharedPreferences preferences = PreferenceManager.getDefaultSharedPreferences(context);
		String value = preferences.getString(THEME_FILEMANAGER_KEY, themeKeys[0]);
		int color = themeBasicColors[0];
		for (int i = 0; i < themeKeys.length; i ++) {
			if (value.equals(themeKeys[i])) {
				color = themeBasicColors[i];
				break;
			}
		}
		return color;
	}
	
	public int getBasicColor() {
		SharedPreferences preferences = PreferenceManager.getDefaultSharedPreferences(context);
		String value = preferences.getString(THEME_FILEMANAGER_KEY, themeKeys[0]);
		int color = themeBasicColors[0];
		for (int i = 0; i < themeKeys.length; i ++) {
			if (value.equals(themeKeys[i])) {
				color = themeBasicColors[i];
				break;
			}
		}
		return context.getResources().getColor(color);
	}
	
	public int getDefFolderResId() {
		SharedPreferences preferences = PreferenceManager.getDefaultSharedPreferences(context);
		String value = preferences.getString(THEME_FILEMANAGER_KEY, themeKeys[0]);
		Log.d(TAG, "getDefFolderResId value = " + value);
		int resId = themeDefaultFolder[0];
		for (int i = 0; i < themeKeys.length; i ++) {
			if (value.equals(themeKeys[i])) {
				resId = themeDefaultFolder[i];
				Log.d(TAG, "getDefFolderResId i = " + i);
				break;
			}
		}
		return resId;
	}
}
