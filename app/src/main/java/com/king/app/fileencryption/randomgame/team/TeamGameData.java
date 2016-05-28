package com.king.app.fileencryption.randomgame.team;

public class TeamGameData {

	public static final int FLAG_NEWFILE = 0;
	public static final int FLAG_LOADED = 1;
	
	private int flag;
	private String fileName;
	private String[] titles;
	private String[][] datas;
	
	public String[] getTitles() {
		return titles;
	}
	public void setTitles(String[] titles) {
		this.titles = titles;
	}
	public String[][] getDatas() {
		return datas;
	}
	public void setDatas(String[][] datas) {
		this.datas = datas;
	}
	public String getFileName() {
		return fileName;
	}
	public void setFileName(String fileName) {
		this.fileName = fileName;
	}
	public void setFlag(int flag) {
		this.flag = flag;
	}
	public boolean isNewFile() {
		return flag == FLAG_NEWFILE;
	}

}
