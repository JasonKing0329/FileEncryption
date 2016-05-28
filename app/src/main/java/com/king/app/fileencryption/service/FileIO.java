package com.king.app.fileencryption.service;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;

import android.content.Context;

public class FileIO {

	public void moveFile(String src, String target) {
		File srcfFile = new File(src);
		copyFile(srcfFile, new File(target));
		srcfFile.delete();
	}

	public void copyFile(File src, File target) {
		try {
			if (!target.exists()) {
				target.createNewFile();
			}
			InputStream fileIn = new FileInputStream(src);
			OutputStream fileOut = new FileOutputStream(target);
			byte[] buffer = new byte[1024];
			int length;
			while ((length = fileIn.read(buffer))>0){
				fileOut.write(buffer, 0, length);
			}

			fileOut.flush();
			fileOut.close();
			fileIn.close();
		} catch (IOException e) {
			e.printStackTrace();
		}
	}

	/**
	 * 从assets目录复制res文件
	 * @param context
	 */
	public static void copyResFromAssets(Context context, String resFile, String targetPath) {

		File file = new File(targetPath);
		if (!file.exists()) {
			try {
				InputStream assetsIn = context.getAssets().open(resFile);
				OutputStream fileOut = new FileOutputStream(targetPath);
				byte[] buffer = new byte[1024];
				int length;
				while ((length = assetsIn.read(buffer))>0){
					fileOut.write(buffer, 0, length);
				}

				fileOut.flush();
				fileOut.close();
				assetsIn.close();
			} catch (IOException e) {
				e.printStackTrace();
			}
		}
	}

}
