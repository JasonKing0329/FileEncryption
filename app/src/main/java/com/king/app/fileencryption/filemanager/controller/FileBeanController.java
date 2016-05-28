package com.king.app.fileencryption.filemanager.controller;

import java.io.File;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;

import com.king.app.fileencryption.controller.EncrypterFactory;
import com.king.app.fileencryption.filemanager.entity.FileBean;
import com.king.app.fileencryption.tool.Encrypter;
import com.king.app.fileencryption.util.FileSizeUtil;
import com.king.app.fileencryption.util.ImageFileUtil;

public class FileBeanController {

	private SimpleDateFormat dateFormat;
	private Encrypter encrypter;
	
	public FileBeanController() {

		dateFormat = new SimpleDateFormat("yyyy-MM-dd", Locale.CHINA);
		encrypter = EncrypterFactory.create();
	}
	
	public FileBean createFileBean(File file) {
		FileBean bean = new FileBean();
		bean.setPath(file.getPath());
		bean.setTime(file.lastModified());
		bean.setTimeTag(dateFormat.format(new Date(file.lastModified())));
		try {
			bean.setSize(FileSizeUtil.getFileSize(file));
		} catch (Exception e) {
			e.printStackTrace();
		}
		ImageFileUtil.getWidthHeight(bean, file, encrypter);
		return bean;
	}
}
