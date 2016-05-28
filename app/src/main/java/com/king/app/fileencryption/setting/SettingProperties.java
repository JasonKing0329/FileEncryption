package com.king.app.fileencryption.setting;

import com.king.app.fileencryption.R;

import android.content.Context;
import android.content.SharedPreferences;
import android.preference.PreferenceManager;

public class SettingProperties {

	public static final int START_VIEW_CLASSIC = 0;
	public static final int START_VIEW_GUIDE = 1;
	public static final int START_VIEW_TIMELINE = 2;

	public static final int ORDER_BY_DATE = 0;
	public static final int ORDER_BY_NAME = 1;
	public static final int ORDER_BY_ITEMNUMBER = 2;

	public static final int AUTOPLAY_MODE_SEQUENCE = 0;
	public static final int AUTOPLAY_MODE_RANDOM = 1;
	public static final int AUTOPLAY_MODE_REPEATABLE = 2;

	public static final int SLIDINGMENU_LEFT = 0;
	public static final int SLIDINGMENU_RIGHT = 1;
	public static final int SLIDINGMENU_TWOWAY = 2;

	public static int getWaterfallColNum(Context context) {
		SharedPreferences preferences = PreferenceManager.getDefaultSharedPreferences(context);
		return Integer.parseInt(preferences.getString("setting_waterfall_colnumber", "3"));
	}

	public static int getStartViewMode(Context context) {
		String modes[] = context.getResources().getStringArray(R.array.setting_start_view_key);
		SharedPreferences preferences = PreferenceManager.getDefaultSharedPreferences(context);
		String loadMode = preferences.getString("setting_start_view", modes[0]);
		if (loadMode.equals(modes[0])) {
			return START_VIEW_CLASSIC;
		}
		else if (loadMode.equals(modes[1])) {
			return START_VIEW_GUIDE;
		}
		else {
			return START_VIEW_TIMELINE;
		}
	}

	public static int getWaterfallHorColNum(Context context) {
		SharedPreferences preferences = PreferenceManager.getDefaultSharedPreferences(context);
		return Integer.parseInt(preferences.getString("setting_waterfall_colnumber_hor", "4"));
	}

	public static int getCascadeCoverNumber(Context context) {
		SharedPreferences preferences = PreferenceManager.getDefaultSharedPreferences(context);
		return Integer.parseInt(preferences.getString("setting_cover_cascade_num", "2"));
	}

	public static boolean isFingerPrintEnable(Context context) {
		SharedPreferences preferences = PreferenceManager.getDefaultSharedPreferences(context);
		return preferences.getBoolean("setting_enable_fingerprint", false);
	}

	public static boolean isMainViewSlidingEnable(Context context) {
		SharedPreferences preferences = PreferenceManager.getDefaultSharedPreferences(context);
		return preferences.getBoolean("setting_slidingmenu_enable", false);
	}

	public static boolean isShowFileOriginMode(Context context) {
		SharedPreferences preferences = PreferenceManager.getDefaultSharedPreferences(context);
		return preferences.getBoolean("setting_show_original_name", false);
	}

	public static int getOrderMode(Context context) {
		String modes[] = context.getResources().getStringArray(R.array.setting_sorder_mode_key);
		SharedPreferences preferences = PreferenceManager.getDefaultSharedPreferences(context);
		String loadMode = preferences.getString("setting_sorder_mode", modes[0]);
		if (loadMode.equals(modes[1])) {
			return ORDER_BY_NAME;
		}
		else if (loadMode.equals(modes[2])) {
			return ORDER_BY_DATE;
		}
		else if (loadMode.equals(modes[3])) {
			return ORDER_BY_ITEMNUMBER;
		}
		else {
			return ORDER_BY_NAME;
		}
	}

	public static int getAutoPlayMode(Context context) {
		String[] modes = context.getResources().getStringArray(R.array.setting_auto_play_mode_key);
		SharedPreferences preferences = PreferenceManager.getDefaultSharedPreferences(context);
		String mode = preferences.getString("setting_auto_play_mode", modes[0]);
		if (mode.equals(modes[0])) {
			return AUTOPLAY_MODE_SEQUENCE;
		}
		else if (mode.equals(modes[1])) {
			return AUTOPLAY_MODE_RANDOM;
		}
		else {
			return AUTOPLAY_MODE_SEQUENCE;
		}
	}

	public static int getSlidingMenuMode(Context context) {
		String[] modes = context.getResources().getStringArray(R.array.setting_slidingmenu_mode_value);
		SharedPreferences preferences = PreferenceManager.getDefaultSharedPreferences(context);
		String mode = preferences.getString("setting_slidingmenu_mode", modes[0]);
		if (mode.equals(modes[0])) {
			return SLIDINGMENU_LEFT;
		}
		else if (mode.equals(modes[1])) {
			return SLIDINGMENU_RIGHT;
		}
		else if (mode.equals(modes[2])) {
			return SLIDINGMENU_TWOWAY;
		}
		else {
			return SLIDINGMENU_LEFT;
		}
	}

	//翻页模式仅在grid view下起作用
	public static boolean isPageModeEnable(Context context) {
		SharedPreferences preferences = PreferenceManager.getDefaultSharedPreferences(context);
		return preferences.getBoolean("setting_sorder_enable_page", false);
	}

	public static int getSOrderPageNumber(Context context) {
		SharedPreferences preferences = PreferenceManager.getDefaultSharedPreferences(context);
		String text = preferences.getString("setting_sorder_page_numbers", "16");
		return Integer.parseInt(text);
	}

	public static int getMinNumberToPlay(Context context) {

		SharedPreferences preferences = PreferenceManager.getDefaultSharedPreferences(context);
		String text = preferences.getString("setting_min_items", "7");
		int min_number = Integer.parseInt(text);
		return min_number;
	}

	public static boolean isShowAnimation(Context context) {

		SharedPreferences preferences = PreferenceManager.getDefaultSharedPreferences(context);
		boolean showAnimation = preferences.getBoolean("setting_show_animation", true);
		return showAnimation;
	}

	public static boolean isLoadAsRandom(Context context) {
		SharedPreferences preferences = PreferenceManager.getDefaultSharedPreferences(context);
		boolean startAsRandom = preferences.getBoolean("setting_open_random", false);
		return startAsRandom;
	}

	public static int getAnimationSpeed(Context context) {

		SharedPreferences preferences = PreferenceManager.getDefaultSharedPreferences(context);
		String[] speeds = context.getResources().getStringArray(R.array.setting_auto_play_time_key);
		String text = preferences.getString("setting_auto_play_speed", speeds[0]);
		int speed = context.getResources().getInteger(R.integer.autoplay_speed_fast);
		if (text.equals(speeds[1])) {//normal
			speed = context.getResources().getInteger(R.integer.autoplay_speed_normal);
		}
		else if (text.equals(speeds[2])) {//slow
			speed = context.getResources().getInteger(R.integer.autoplay_speed_slow);
		}
		return speed;
	}

	public static int getCasualLookNumber(Context context) {
		SharedPreferences preferences = PreferenceManager.getDefaultSharedPreferences(context);
		String text = preferences.getString("setting_edit_casual", "20");
		return Integer.parseInt(text);
	}

	public static void savePreference(Context context, String key, String value) {
		SharedPreferences preferences = PreferenceManager.getDefaultSharedPreferences(context);
		SharedPreferences.Editor editor = preferences.edit();
		editor.putString(key, value);
		editor.commit();
	}
}
