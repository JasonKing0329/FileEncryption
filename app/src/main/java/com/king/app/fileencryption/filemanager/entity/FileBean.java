package com.king.app.fileencryption.filemanager.entity;

public class FileBean {

	private long id;
	private String path;
	private long time;
	private String timeTag;
	private int width;
	private int height;
	private long size;
	private String other;
	public long getId() {
		return id;
	}
	public void setId(long id) {
		this.id = id;
	}
	public String getPath() {
		return path;
	}
	public void setPath(String path) {
		this.path = path;
	}
	public long getTime() {
		return time;
	}
	public void setTime(long time) {
		this.time = time;
	}
	public String getTimeTag() {
		return timeTag;
	}
	public void setTimeTag(String timeTag) {
		this.timeTag = timeTag;
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
	public long getSize() {
		return size;
	}
	public void setSize(long size) {
		this.size = size;
	}
	public String getOther() {
		return other;
	}
	public void setOther(String other) {
		this.other = other;
	}
	
}
