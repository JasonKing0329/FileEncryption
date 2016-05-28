package com.king.app.fileencryption.sorder.entity;

import java.util.List;

public class SOrder {

	private int id;
	private String name;
	private String coverPath;
	private STag tag;
	private List<String> imgPathList;
	private List<Integer> imgPathIdList;
	private int itemNumber;
	private SOrderCount orderCount;
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
	public STag getTag() {
		return tag;
	}
	public void setTag(STag tag) {
		this.tag = tag;
	}
	public String getCoverPath() {
		return coverPath;
	}
	public void setCoverPath(String coverPath) {
		this.coverPath = coverPath;
	}
	public List<String> getImgPathList() {
		return imgPathList;
	}
	public void setImgPathList(List<String> imgPathList) {
		this.imgPathList = imgPathList;
	}
	public List<Integer> getImgPathIdList() {
		return imgPathIdList;
	}
	public void setImgPathIdList(List<Integer> imgPathIdList) {
		this.imgPathIdList = imgPathIdList;
	}
	public int getItemNumber() {
		return itemNumber;
	}
	public void setItemNumber(int itemNumber) {
		this.itemNumber = itemNumber;
	}
	public SOrderCount getOrderCount() {
		return orderCount;
	}
	public void setOrderCount(SOrderCount orderCount) {
		this.orderCount = orderCount;
	}

}
