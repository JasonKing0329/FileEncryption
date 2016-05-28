package com.king.app.fileencryption.randomgame;

public class RandomRules {

	private boolean isAuto;
	private boolean isReplace;
	private boolean isImgThreadOn;
	private boolean isScreenOn;
	private boolean isRepeatable;
	private int number;
	public RandomRules() {
		isAuto = false;
		isReplace = true;
		isImgThreadOn = false;
		isScreenOn = false;
		number = 0;
	}
	public boolean isAuto() {
		return isAuto;
	}
	public void setAuto(boolean isAuto) {
		this.isAuto = isAuto;
	}
	public boolean isReplace() {
		return isReplace;
	}
	public void setReplace(boolean isReplace) {
		this.isReplace = isReplace;
	}
	public boolean isImgThreadOn() {
		return isImgThreadOn;
	}
	public void setImgThreadOn(boolean isImgThreadOn) {
		this.isImgThreadOn = isImgThreadOn;
	}
	public boolean isScreenOn() {
		return isScreenOn;
	}
	public void setScreenOn(boolean isScreenOn) {
		this.isScreenOn = isScreenOn;
	}
	public boolean isRepeatable() {
		return isRepeatable;
	}
	public void setRepeatable(boolean isRepeatable) {
		this.isRepeatable = isRepeatable;
	}
	public int getNumber() {
		return number;
	}
	public void setNumber(int number) {
		this.number = number;
	}
}
