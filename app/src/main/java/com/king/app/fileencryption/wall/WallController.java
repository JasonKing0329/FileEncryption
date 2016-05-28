package com.king.app.fileencryption.wall;

import java.io.File;

import com.king.app.fileencryption.R;
import com.king.app.fileencryption.controller.EncrypterFactory;
import com.king.app.fileencryption.sorder.controller.SOrderPictureBridge;
import com.king.app.fileencryption.sorder.entity.SOrder;
import com.king.app.fileencryption.tool.Encrypter;

import android.content.Context;
import android.content.SharedPreferences;
import android.preference.PreferenceManager;
import android.util.Log;

public class WallController {

	private final String WALL_DEFAULT_KEY = "wall_default_key";
	private Context context;
	private int wallIndex;
	private int[] wallRes = new int[] {
		R.drawable.wall_bk1, R.drawable.wall_bk2, R.drawable.wall_bk3
		, R.drawable.wall_bk4, R.drawable.wall_bk5
	};
	
	public WallController(Context context) {
		this.context = context;
	}
	
	public int getDefaultWallRes() {
		SharedPreferences preferences = PreferenceManager.getDefaultSharedPreferences(context);
		int res = preferences.getInt(WALL_DEFAULT_KEY, R.drawable.wall_bk1);
		return res;
	}

	public void saveDefaultWallRes() {
		SharedPreferences preferences = PreferenceManager.getDefaultSharedPreferences(context);
		SharedPreferences.Editor editor = preferences.edit();
		editor.putInt(WALL_DEFAULT_KEY, wallRes[wallIndex]);
		editor.commit();
	}
	
	public int changeWallRes() {
		if (wallIndex == wallRes.length - 1) {
			wallIndex = 0;
		}
		else {
			wallIndex ++;
		}
		return wallRes[wallIndex];
	}

	public void deleteFile(String path) {
		File file = new File(path);
		Encrypter encrypter = EncrypterFactory.create();
		if (encrypter.isEncrypted(file)) {
			file.delete();
			path = path.replace(encrypter.getFileExtra(), encrypter.getNameExtra());
			file = new File(path);
			file.delete();
			Log.i("FileEncryption", "delete file " + path);
		}
		else {
			if (file.exists()) {
				file.delete();
				Log.i("FileEncryption", "delete file " + path);
			}
		}
	}

	public void deleteFile(SOrder currentOrder, int index) {
		SOrderPictureBridge.getInstance(context).deleteItemFromOrder(currentOrder, index);
	}
}
