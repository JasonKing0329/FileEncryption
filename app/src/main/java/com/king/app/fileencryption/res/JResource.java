package com.king.app.fileencryption.res;

import android.content.Context;
import android.widget.Toast;

import com.king.app.fileencryption.R;
import com.king.app.fileencryption.data.Configuration;
import com.king.lib.colorpicker.ColorFormatter;
import com.king.lib.colorpicker.ResourceProvider;
import com.king.lib.resmanager.JResManager;
import com.king.lib.resmanager.action.JResManagerCallback;
import com.king.lib.resmanager.exception.JResNotFoundException;
import com.king.lib.resmanager.exception.JResParseException;

/**
 * @author JingYang
 * @version create time：2016-1-13 下午3:44:29
 *
 */
public class JResource {

	private static JResManager jResManager;

	public static void initializeColors() {
//		jResManager = JResManager.getInstance();
		jResManager = JResManager.createInstance();
		try {
			jResManager.parseColorFile(Configuration.EXTEND_RES_COLOR);
		} catch (JResParseException e) {
			e.printStackTrace();
		}
	}

	@Deprecated
	public static void registCallback(JResManagerCallback callback) {
		jResManager.registCallback(callback);
	}

	public static int getColor(Context context, String resName, int defaultResId) {
		try {
			int color = jResManager.getColor(context, resName);
			return color;
		} catch (JResNotFoundException e) {
			e.printStackTrace();
		} catch (JResParseException e) {
			e.printStackTrace();
		}

		if (defaultResId == ResourceProvider.FLAG_DEFAULT) {
			return ResourceProvider.FLAG_DEFAULT;
		}
		return context.getResources().getColor(defaultResId);
	}

	public static void updateColor(String resName, int newColor) {
		jResManager.updateColor(resName, "#" + ColorFormatter.formatColor(newColor));
	}

	public static void removeColor(String resName) {
		jResManager.removeColorResource(resName);
	}

	public static void saveColorUpdate(Context context) {
		if (jResManager.saveColorUpdate()) {
			Toast.makeText(context, R.string.colorpicker_update_success, Toast.LENGTH_SHORT).show();
		}
		else {
			Toast.makeText(context, R.string.colorpicker_update_failed, Toast.LENGTH_SHORT).show();
		}
	}

}
