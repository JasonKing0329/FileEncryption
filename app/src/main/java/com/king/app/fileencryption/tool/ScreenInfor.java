package com.king.app.fileencryption.tool;

import android.app.Activity;
import android.graphics.Point;

public class ScreenInfor {

	private static int width = -1;
	private static int height = -1;
	
	public static int getWidth(Activity act) {
		if (width == -1) {
			calculateScreen(act);
		}
		return width;
	}
	public static int getHeight(Activity act) {
		if (height == -1) {
			calculateScreen(act);
		}
		return height;
	}
	
	private static void calculateScreen(Activity act) {
		Point point = new Point();
		act.getWindowManager().getDefaultDisplay().getSize(point);
		width = point.x;
		height = point.y;
		
		//also right
		//DisplayMetrics dm = act.getResources().getDisplayMetrics();
		//width = dm.widthPixels;
		//height = dm.heightPixels;

		//not right
		//width = (int) (screenWidth*dm.density + 0.5f);
		//height = (int) (screenHeight*dm.density + 0.5f);
	}
	
	
}
