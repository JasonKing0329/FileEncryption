package com.king.app.fileencryption.util;

import java.io.File;

import android.graphics.BitmapFactory;

import com.king.app.fileencryption.filemanager.entity.FileBean;
import com.king.app.fileencryption.tool.Encrypter;

public class ImageFileUtil {

	public static void getWidthHeight(FileBean bean, File file, Encrypter encrypter) {
		byte datas[] = encrypter.decipherToByteArray(file);
		if (datas != null) {
			BitmapFactory.Options opts = new BitmapFactory.Options();
			opts.inJustDecodeBounds = true;// 对bitmap不分配空间，只是用于计算文件options的各种属性(本程序需要计算width,height)
			BitmapFactory.decodeByteArray(datas, 0, datas.length, opts);
			bean.setWidth(opts.outWidth);
			bean.setHeight(opts.outHeight);
		}
	}
}
