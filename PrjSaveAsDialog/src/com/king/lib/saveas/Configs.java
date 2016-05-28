package com.king.lib.saveas;

import android.os.Build;

/**
 * @author JingYang
 * @version create time：2016-1-29 上午10:45:47
 *
 */
public class Configs {

	public static boolean isLollipop() {
		return Build.VERSION.SDK_INT >= 21;//Build.VERSION_CODES.L;
	}

}
