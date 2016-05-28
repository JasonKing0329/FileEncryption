package com.king.app.fileencryption.timeline.controller;

import java.sql.Connection;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

import com.king.app.fileencryption.data.Constants;
import com.king.app.fileencryption.data.DBInfor;
import com.king.app.fileencryption.filemanager.entity.FileBean;
import com.king.app.fileencryption.sorder.db.SqlOperator;

public class TimeLineController {

	// 以后可能做3列，取3和4的公倍数
	private final int GROUP_MAX_NUM = 12;

	private final int PAGE_NUMBER = 20;
	private int currentPage;
	private int totalPage;

	private List<HashMap<String, String>> totalGroupList;
	private List<HashMap<String, String>> groupList;
	private HashMap<String, List<FileBean>> contentMap;
	private List<String> headerList;
	private List<FileBean> fileBeanList;
	private List<String> bkList;

	private SqlOperator sqlOperator;

	// Sticky grid view的bug，第一项一开始滑动后总是显示空白
	private boolean enableFakeData = true;

	public TimeLineController() {
		totalGroupList = new ArrayList<HashMap<String,String>>();
		groupList = new ArrayList<HashMap<String,String>>();
		contentMap = new HashMap<String, List<FileBean>>();
		sqlOperator = new SqlOperator();
		headerList = new ArrayList<String>();
	}

	public List<HashMap<String, String>> getGroupList() {
		return groupList;
	}

	public HashMap<String, List<FileBean>> getContentMap() {
		return contentMap;
	}

	public List<FileBean> getFileBeanList() {
		return fileBeanList;
	}

	public List<String> getHeaderList() {
		return headerList;
	}

	/**
	 * 如果tag含冒号，冒号后是按GROUP_MAX_NUM分后页码
	 * @param timeTag "2015-12-25" or "2015-12-25:0"
	 */
	@Deprecated
	public void loadFileBeans(String timeTag) {
		Connection connection = sqlOperator.connect(DBInfor.DB_PATH);

		String tag = timeTag;
		int page = -1;
		if (timeTag.contains(":")) {//总数小于GROUP_MAX_NUM，分页
			String array[] = timeTag.split(":");
			tag = array[0];
			page = Integer.parseInt(array[1]);
		}
		List<FileBean> list = sqlOperator.queryFileBeansByTimeTag(tag, connection);

		if (page != -1) {
			int start = page * GROUP_MAX_NUM;
			if (start + GROUP_MAX_NUM < list.size()) {
				contentMap.put(timeTag, new ArrayList<FileBean>(list.subList(start, start + GROUP_MAX_NUM)));
			}
			else {
				contentMap.put(timeTag, new ArrayList<FileBean>(list.subList(start, list.size())));
			}
		}
		else {
			contentMap.put(timeTag, list);
		}

		try {
			connection.close();
		} catch (SQLException e) {
			e.printStackTrace();
		}
	}

	/**
	 * 查询时间线上所有的文件，并按照日期分组
	 * 适用于v7.2及以后
	 */
	public boolean loadTimeLineItems() {
		Connection connection = sqlOperator.connect(DBInfor.DB_PATH);

		fileBeanList = sqlOperator.queryAllFileBeans(connection, DBInfor.TF_COL_TIME + " DESC");

		if (fileBeanList == null || fileBeanList.size() == 0) {
			return false;
		}

		if (Constants.FEATURE_TIMELINE_ENABLE_BK) {
			// Sticky grid view的bug，第一项一开始滑动后总是显示空白
			// 用假数据占据第一个header
			if (enableFakeData) {
				contentMap.put("fakeData", new ArrayList<FileBean>());
				headerList.add("fakeData");
			}
		}

		List<FileBean> subList = null;
		for (int i = 0; i < fileBeanList.size(); i ++) {
			String timeTag = fileBeanList.get(i).getTimeTag();
			if (contentMap.get(timeTag) == null) {
				subList = new ArrayList<FileBean>();
				contentMap.put(timeTag, subList);
				headerList.add(timeTag);
			}
			subList.add(fileBeanList.get(i));
		}

		try {
			connection.close();
		} catch (SQLException e) {
			e.printStackTrace();
		}
		return true;
	}

	public List<String> getIndicatorBkList() {
		if (bkList == null) {
			Connection connection = sqlOperator.connect(DBInfor.DB_PATH);

			bkList = sqlOperator.queryOrderItemsByName(Constants.TIMELINE_INDICATOR_DEFAULT_ORDER, connection);

			try {
				connection.close();
			} catch (SQLException e) {
				e.printStackTrace();
			}
		}
		return bkList;
	}

	/**
	 * 查询所有的文件按添加时间组成的日期集合
	 * 适用于v7.2之前
	 * @return
	 */
	@Deprecated
	public boolean loadTimeGroups() {
		Connection connection = sqlOperator.connect(DBInfor.DB_PATH);

		List<HashMap<String, String>> list = sqlOperator.queryFileBeanTimeGroup(0, connection);
		if (list != null) {
			//由于主界面由listview嵌套gridview实现，嵌套的gridview会在listview某一项加载时，其item内全部内容一次性加载
			//这就在性能上造成了很大的问题，比如一个日期内如有成百上千张图片，会一次性加载成百上千张
			//为了解决这个问题，通过将日期内图片分页的方式，减少因listview滑动通知gridview变化的item数量
			//一次性加载能控制在GROUP_MAX_NUM以内
			for (HashMap<String, String> map:list) {
				String tag = map.get("time");
				int count = Integer.parseInt(map.get("count"));
				int index = 0;
				int originCount = count;
				while (count > GROUP_MAX_NUM) {
					HashMap<String, String> newMap = new HashMap<String, String>();
					newMap.put("time", tag + ":" + index);
					newMap.put("count", "" + originCount);//分页的时候只需记录该日期group总的数量
					totalGroupList.add(newMap);
					count -= GROUP_MAX_NUM;
					index ++;
				}
				if (index == 0) {
					HashMap<String, String> newMap = new HashMap<String, String>();
					newMap.put("time", tag);
					newMap.put("count", "" + originCount);
					totalGroupList.add(newMap);
				}
				else {
					HashMap<String, String> newMap = new HashMap<String, String>();
					newMap.put("time", tag + ":" + index);
					newMap.put("count", "" + originCount);
					totalGroupList.add(newMap);
				}
			}

			totalPage = (totalGroupList.size() - 1) / PAGE_NUMBER + 1;
		}

		try {
			connection.close();
		} catch (SQLException e) {
			e.printStackTrace();
		}

		return totalGroupList == null ? false:true;
	}

	public boolean hasNextPage() {
		return currentPage != totalPage;
	}

	public void nextPage() {
		if (currentPage < totalPage) {
			for (int i = currentPage * PAGE_NUMBER; i < currentPage * PAGE_NUMBER + PAGE_NUMBER
					&& i < totalGroupList.size(); i ++) {
				groupList.add(totalGroupList.get(i));
			}
			currentPage ++;
		}
	}
}
