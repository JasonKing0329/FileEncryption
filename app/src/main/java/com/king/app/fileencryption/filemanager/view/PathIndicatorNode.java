package com.king.app.fileencryption.filemanager.view;

public class PathIndicatorNode {

	private String name;
	private String path;
	private int index;
	private int indexInContainer;
	private int left;
	private int width;
	
	public PathIndicatorNode() {
		left = -1;
		width = -1;
		index = -1;
		indexInContainer = -1;
	}
	
	public String getName() {
		return name;
	}
	public void setName(String name) {
		this.name = name;
	}
	public String getPath() {
		return path;
	}
	public void setPath(String path) {
		this.path = path;
	}
	public int getIndex() {
		return index;
	}
	public void setIndex(int index) {
		this.index = index;
	}
	public int getIndexInContainer() {
		return indexInContainer;
	}
	public void setIndexInContainer(int indexInContainer) {
		this.indexInContainer = indexInContainer;
	}
	public int getLeft() {
		return left;
	}
	public void setLeft(int left) {
		this.left = left;
	}
	public int getWidth() {
		return width;
	}
	public void setWidth(int width) {
		this.width = width;
	}
}
