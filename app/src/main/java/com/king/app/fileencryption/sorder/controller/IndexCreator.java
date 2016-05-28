package com.king.app.fileencryption.sorder.controller;

import java.util.ArrayList;
import java.util.List;

import com.king.app.fileencryption.publicview.HorizontalIndexView;
import com.king.app.fileencryption.publicview.HorizontalIndexView.IndexItem;
import com.king.app.fileencryption.publicview.HorizontalIndexView.PageIndexOutOfBoundsException;
import com.king.app.fileencryption.setting.SettingProperties;
import com.king.app.fileencryption.sorder.entity.SOrder;

import android.content.Context;

public class IndexCreator {

	private Context mContext;
	private int PAGE_MAX_ITEM;
	private int totalPages;
	private SOrderPictureBridge bridge;
	private int defaultMode;
	
	public IndexCreator(Context context, SOrderPictureBridge bridge) {
		this.bridge = bridge;
		mContext = context;
		PAGE_MAX_ITEM = SettingProperties.getSOrderPageNumber(context);
		defaultMode = SettingProperties.getOrderMode(context);
	}

	public List<IndexItem> createIndex() {
		return createIndex(defaultMode);
	}
	public List<IndexItem> createIndex(int mode) {
		
		List<HorizontalIndexView.IndexItem> list = null;
		totalPages = (bridge.getOrderList().size() - 1) / PAGE_MAX_ITEM + 1;
		if (totalPages > 1) {
			list = new ArrayList<HorizontalIndexView.IndexItem>();
			HorizontalIndexView.IndexItem indexItem = null;
			SOrder order = null;
			
			if (mode == SettingProperties.ORDER_BY_DATE) {

				for (int i = 1; i <= totalPages; i ++) {
					indexItem = new HorizontalIndexView.IndexItem();
					indexItem.index = "" + i;
					list.add(indexItem);
				}
			}
			else if (mode == SettingProperties.ORDER_BY_NAME) {
				for (int i = 1; i <= totalPages; i ++) {
					indexItem = new HorizontalIndexView.IndexItem();
					order = bridge.getOrderList().get((i - 1) * PAGE_MAX_ITEM);
					if (order.getName().length() < 2) {
						indexItem.index = "" + order.getName().charAt(0);
					}
					else {
						indexItem.index = order.getName().substring(0, 2).toLowerCase();
					}
					list.add(indexItem);
				}
			}
		}
		return list;
	}

	public List<SOrder> getPageItem(int index) throws PageIndexOutOfBoundsException {
		if (index < 1 || index > totalPages) {
			throw new PageIndexOutOfBoundsException();
		}
		List<SOrder> list = new ArrayList<SOrder>();
		int start = (index - 1) * PAGE_MAX_ITEM, end = 0;
		if (index == totalPages) {
			end = bridge.getOrderList().size();
		}
		else {
			end = index * PAGE_MAX_ITEM;
		}
		for (int i = start; i < end; i ++) {
			list.add(bridge.getOrderList().get(i));
		}
		return list;
	}
}
