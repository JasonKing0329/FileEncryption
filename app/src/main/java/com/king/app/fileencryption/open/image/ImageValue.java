package com.king.app.fileencryption.open.image;

public class ImageValue {

	/**
	 * effect on runtime
	 */
	private String path;
	
	/**
	 * effect on runtime and database storage
	 */
	private int id;
	private String name;
	private int width;
	private int height;
	private String other;
	private Object tag;
	
	public int getId() {
		return id;
	}
	public void setId(int id) {
		this.id = id;
	}
	public String getName() {
		return name;
	}
	public void setName(String name) {
		this.name = name;
	}
	public int getWidth() {
		return width;
	}
	public void setWidth(int width) {
		this.width = width;
	}
	public int getHeight() {
		return height;
	}
	public void setHeight(int height) {
		this.height = height;
	}
	public String getOther() {
		return other;
	}
	public void setOther(String other) {
		this.other = other;
	}
	public String getPath() {
		return path;
	}
	public void setPath(String path) {
		this.path = path;
	}
	
	public static String generateName(String path) {
		String name = null;
		if (path != null) {
			int index = path.lastIndexOf("/");
			name = path.substring(index + 1);
			index = name.indexOf(".");
			name = name.substring(0, index);
		}
		return name;
	}
	public Object getTag() {
		return tag;
	}
	public void setTag(Object tag) {
		this.tag = tag;
	}
}
