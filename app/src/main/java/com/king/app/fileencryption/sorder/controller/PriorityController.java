package com.king.app.fileencryption.sorder.controller;

import java.util.ArrayList;
import java.util.List;

import android.content.Context;
import android.content.SharedPreferences;
import android.preference.PreferenceManager;

import com.king.app.fileencryption.sorder.entity.SOrder;

public class PriorityController {

	private final String priorityKey = "sorder_priority";
	private List<PriorityOrder> priorityOrderList;
	private Context mContext;

	public PriorityController(Context context) {
		mContext = context;
		priorityOrderList = new ArrayList<PriorityOrder>();
	}

	private class PriorityOrder {
		public int priority;
		public int orderId;
		public SOrder sOrder;

		public PriorityOrder(int priority, int orderId) {
			this.priority = priority;
			this.orderId = orderId;
		}
	}

	public void updateOrdersByPriority(List<SOrder> list) {
		String str = PreferenceManager.getDefaultSharedPreferences(mContext).getString(priorityKey, null);
		if (str != null && str.length() > 0) {
			String orderIds[] = str.split(",");
			List<PriorityOrder> tempPriorityOrders = new ArrayList<PriorityOrder>();
			PriorityOrder priorityOrder = null;
			for (int i = 0; i < orderIds.length; i ++) {
				priorityOrder = new PriorityOrder(i, Integer.parseInt(orderIds[i]));
				priorityOrderList.add(priorityOrder);
				tempPriorityOrders.add(priorityOrder);
			}

			SOrder order = null;
			boolean isPriority = false;
			for (int i = list.size() - 1; i > -1 && tempPriorityOrders.size() > 0; i --) {
				order = list.get(i);
				isPriority = false;
				for (int j = 0; j < tempPriorityOrders.size(); j ++) {
					priorityOrder = tempPriorityOrders.get(j);
					if (priorityOrder.orderId == order.getId()) {
						priorityOrderList.get(priorityOrder.priority).sOrder = order;
						tempPriorityOrders.remove(j);
						isPriority = true;
						break;
					}
				}
				if (isPriority) {
					list.remove(i);
				}
			}

			updatePriorityOrderPref();

			for (int i = 0; i < priorityOrderList.size(); i ++) {
				list.add(0, priorityOrderList.get(i).sOrder);
			}
		}
	}

	/**
	 * 如果在全部order里没有找到设置过优先级的列表（该列表被删除或修改），则更新priorityOrders以及preference，保存有效的列表
	 */
	private void updatePriorityOrderPref() {
		if (priorityOrderList.size() > 0) {
			for (int i = priorityOrderList.size() - 1; i > -1; i --) {
				if (priorityOrderList.get(i).sOrder == null) {
					priorityOrderList.remove(i);
				}
			}
		}

		SharedPreferences preferences = PreferenceManager.getDefaultSharedPreferences(mContext);
		SharedPreferences.Editor editor = preferences.edit();
		if (priorityOrderList.size() > 0) {
			//reset priority & save to preference
			priorityOrderList.get(0).priority = 0;
			StringBuffer buffer = new StringBuffer("" + priorityOrderList.get(0).orderId);
			for (int i = 1; i < priorityOrderList.size(); i ++) {
				priorityOrderList.get(i).priority = i;
				buffer.append(",").append(priorityOrderList.get(i).orderId);
			}

			editor.putString(priorityKey, buffer.toString());
		}
		else {
			editor.remove(priorityKey);
		}

		editor.commit();
	}

	public PriorityOrder isPriorityOrder(SOrder order) {
		for (int i = 0; i < priorityOrderList.size(); i ++) {
			if (priorityOrderList.get(i).orderId == order.getId()) {
				return priorityOrderList.get(i);
			}
		}
		return null;
	}

	public void setAsPriorityController(SOrder order) {
		PriorityOrder priorityOrder = new PriorityOrder(priorityOrderList.size(), order.getId());
		priorityOrder.sOrder = order;
		priorityOrderList.add(priorityOrder);
		updatePriorityOrderPref();
	}

	public void cancelPriority(SOrder selectedOrder) {
		PriorityOrder priorityOrder = isPriorityOrder(selectedOrder);
		if (priorityOrder != null) {
			priorityOrderList.get(priorityOrder.priority).sOrder = null;
			updatePriorityOrderPref();
		}
	}

	public void increasePriority(SOrder selectedOrder) {
		PriorityOrder priorityOrder = isPriorityOrder(selectedOrder);
		if (priorityOrder != null) {
			if (priorityOrder.priority < priorityOrderList.size() - 1) {
				PriorityOrder temp = priorityOrderList.get(priorityOrder.priority);
				priorityOrderList.set(priorityOrder.priority, priorityOrderList.get(priorityOrder.priority + 1));
				priorityOrderList.set(priorityOrder.priority + 1, temp);
				updatePriorityOrderPref();
			}
		}
	}

	public void decreasePriority(SOrder selectedOrder) {
		PriorityOrder priorityOrder = isPriorityOrder(selectedOrder);
		if (priorityOrder != null) {
			if (priorityOrder.priority > 0) {
				PriorityOrder temp = priorityOrderList.get(priorityOrder.priority);
				priorityOrderList.set(priorityOrder.priority, priorityOrderList.get(priorityOrder.priority - 1));
				priorityOrderList.set(priorityOrder.priority - 1, temp);
				updatePriorityOrderPref();
			}
		}
	}

	public int getTotalPriorityNumbers() {
		return priorityOrderList.size();
	}

}
