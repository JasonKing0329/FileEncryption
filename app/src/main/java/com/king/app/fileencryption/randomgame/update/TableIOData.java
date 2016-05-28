package com.king.app.fileencryption.randomgame.update;

import java.io.Serializable;

public class TableIOData implements Serializable {

	/**
	 * 
	 */
	private static final long serialVersionUID = -7262727420655560528L;

	private float score;
	private String scene;
	private String scenePath;
	private int row;
	private int col;
	private String imagePath;
	
	public float getScore() {
		return score;
	}
	public void setScore(float score) {
		this.score = score;
	}
	public String getScene() {
		return scene;
	}
	public void setScene(String scene) {
		this.scene = scene;
	}
	public String getScenePath() {
		return scenePath;
	}
	public void setScenePath(String scenePath) {
		this.scenePath = scenePath;
	}
	public int getRow() {
		return row;
	}
	public void setRow(int row) {
		this.row = row;
	}
	public int getCol() {
		return col;
	}
	public void setCol(int col) {
		this.col = col;
	}
	public String getImagePath() {
		return imagePath;
	}
	public void setImagePath(String imagePath) {
		this.imagePath = imagePath;
	}
	public String getRole() {
		return role;
	}
	public void setRole(String role) {
		this.role = role;
	}
	public static long getSerialversionuid() {
		return serialVersionUID;
	}
	private String role;
}
