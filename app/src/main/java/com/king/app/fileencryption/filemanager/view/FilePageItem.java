package com.king.app.fileencryption.filemanager.view;

import java.io.File;

import com.king.app.fileencryption.open.image.ImageValue;

import android.graphics.drawable.Drawable;

public class FilePageItem {

	private File file;
	private long date;
	private String strDate;
	private String originName;
	private String displayName;
	private Drawable icon;
	private ImageValue imageValue;
	
	public File getFile() {
		return file;
	}
	public void setFile(File file) {
		this.file = file;
	}
	public long getDate() {
		return date;
	}
	public void setDate(long date) {
		this.date = date;
	}
	public String getStrDate() {
		return strDate;
	}
	public void setStrDate(String strDate) {
		this.strDate = strDate;
	}
	public String getOriginName() {
		return originName;
	}
	public void setOriginName(String originName) {
		this.originName = originName;
	}
	public String getDisplayName() {
		return displayName;
	}
	public void setDisplayName(String displayName) {
		this.displayName = displayName;
	}
	public Drawable getIcon() {
		return icon;
	}
	public void setIcon(Drawable icon) {
		this.icon = icon;
	}
	public ImageValue getImageValue() {
		return imageValue;
	}
	public void setImageValue(ImageValue imageValue) {
		this.imageValue = imageValue;
	}
}
