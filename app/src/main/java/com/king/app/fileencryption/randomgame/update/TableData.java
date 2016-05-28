package com.king.app.fileencryption.randomgame.update;

import android.graphics.Bitmap;
import android.view.View;

public class TableData {

	private View view;
	private float score;
	private String scene;
	private String scenePath;
	private int row;
	private int col;
	private String imagePath;
	private Bitmap bitmap;
	private String role;
	
	public TableData() {
		
	}
	
	public TableData(TableIOData ioData) {
		score = ioData.getScore();
		scene = ioData.getScene();
		scenePath = ioData.getScenePath();
		role = ioData.getRole();
		col = ioData.getCol();
		imagePath = ioData.getImagePath();
		row = ioData.getRow();
	}
	
	public TableIOData toIOData() {
		TableIOData ioData = new TableIOData();
		ioData.setScene(scene);
		ioData.setScenePath(scenePath);
		ioData.setScore(score);
		ioData.setRole(role);
		ioData.setImagePath(imagePath);
		ioData.setRow(row);
		ioData.setCol(col);
		return ioData;
	}

	public View getView() {
		return view;
	}
	public void setView(View view) {
		this.view = view;
	}
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
	public Bitmap getBitmap() {
		return bitmap;
	}
	public void setBitmap(Bitmap bitmap) {
		this.bitmap = bitmap;
	}
	public String getRole() {
		return role;
	}
	public void setRole(String role) {
		this.role = role;
	}
}
