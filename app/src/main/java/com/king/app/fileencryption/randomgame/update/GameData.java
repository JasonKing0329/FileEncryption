package com.king.app.fileencryption.randomgame.update;

public class GameData {

	public static final int FLAG_NEWFILE = 0;
	public static final int FLAG_LOADED = 1;
	
	private int flag;
	private String fileName;
	private String tableName;
	private TableData[][] tableDatas;
	
	private RandomController randomController;

	public RandomController getRandomController() {
		return randomController;
	}

	public void setRandomController(RandomController randomController) {
		this.randomController = randomController;
	}

	public GameData() {
		flag = FLAG_NEWFILE;
	}
	
	public String getFileName() {
		return fileName;
	}

	public void setFileName(String fileName) {
		this.fileName = fileName;
	}

	public String getTableName() {
		return tableName;
	}

	public void setTableName(String tableName) {
		this.tableName = tableName;
	}

	public TableData[][] getTableDatas() {
		return tableDatas;
	}

	public void setTableDatas(TableData[][] tableDatas) {
		this.tableDatas = tableDatas;
	}
	
	public void setFlag(int flag) {
		this.flag = flag;
	}
	
	public boolean isNewFile() {
		return flag == FLAG_NEWFILE;
	}
}
