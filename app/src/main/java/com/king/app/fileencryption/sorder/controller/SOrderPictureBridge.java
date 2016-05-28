package com.king.app.fileencryption.sorder.controller;

import java.io.File;
import java.sql.Connection;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;

import android.content.Context;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;

import com.king.app.fileencryption.controller.EncrypterFactory;
import com.king.app.fileencryption.data.Configuration;
import com.king.app.fileencryption.data.Constants;
import com.king.app.fileencryption.data.DBInfor;
import com.king.app.fileencryption.service.FileIO;
import com.king.app.fileencryption.sorder.db.SqlOperator;
import com.king.app.fileencryption.sorder.entity.SOrder;
import com.king.app.fileencryption.sorder.entity.SOrderCount;
import com.king.app.fileencryption.sorder.entity.STag;
import com.king.app.fileencryption.tool.Encrypter;
import com.king.app.fileencryption.tool.SimpleEncrypter;

public class SOrderPictureBridge {

	private static SOrderPictureBridge bridge;
	private List<SOrder> orderList;

	private SOrderPictureBridge(Context context) {

	}

	public static SOrderPictureBridge getInstance(Context context) {
		if (bridge == null) {
			bridge = new SOrderPictureBridge(context);
		}
		return bridge;
	}

	public List<SOrder> getOrderList() {
		if (orderList == null) {
			loadOrders();
		}
		return orderList;
	}

	public void reloadOrders() {
		clearOrderList();
		loadOrders();
	}

	public void loadOrders() {
		SqlOperator operator = new SqlOperator();
		Connection connection = operator.connect(DBInfor.DB_PATH);

		orderList = operator.queryAllOrders(connection);

		try {
			connection.close();
		} catch (SQLException e) {
			e.printStackTrace();
		}
	}

	public SOrder queryOrder(int orderId) {
		SOrder order = null;
		SqlOperator operator = new SqlOperator();
		Connection connection = operator.connect(DBInfor.DB_PATH);

		order = operator.queryOrder(orderId, connection);
		SOrderCount orderCount = operator.queryOrderCount(orderId, connection);
		order.setOrderCount(orderCount);

		try {
			connection.close();
		} catch (SQLException e) {
			e.printStackTrace();
		}
		return order;
	}

	public boolean isOrderExist(String name) {
		boolean result = false;
		SqlOperator operator = new SqlOperator();
		Connection connection = operator.connect(DBInfor.DB_PATH);

		result = operator.isOrderExist(name, connection);

		try {
			connection.close();
		} catch (SQLException e) {
			e.printStackTrace();
		}
		return result;
	}

	/**
	 * after add OK, set id for the order
	 * @param order
	 * @return
	 */
	public boolean addOrder(SOrder order) {
		boolean result = false;
		SqlOperator operator = new SqlOperator();
		Connection connection = operator.connect(DBInfor.DB_PATH);

		result = operator.insertOrder(order, connection);
		order.setId(operator.queryOrderIdAfterInsert(connection));
		operator.insertOrderCount(order.getId(), connection);

		try {
			connection.close();
		} catch (SQLException e) {
			e.printStackTrace();
		}
		return result;
	}

	public boolean isItemExist(String itemPath, int orderId) {
		boolean result = false;
		SqlOperator operator = new SqlOperator();
		Connection connection = operator.connect(DBInfor.DB_PATH);

		result = operator.isOrderItemExist(itemPath, orderId, connection);

		try {
			connection.close();
		} catch (SQLException e) {
			e.printStackTrace();
		}
		return result;
	}

	public boolean addToOrder(String itemPath, int orderId) {
		boolean result = false;
		SqlOperator operator = new SqlOperator();
		Connection connection = operator.connect(DBInfor.DB_PATH);

		result = operator.insertOrderItem(orderId, itemPath, connection);

		try {
			connection.close();
		} catch (SQLException e) {
			e.printStackTrace();
		}
		return result;
	}

	public List<STag> loadTagList() {
		List<STag> list = null;
		SqlOperator operator = new SqlOperator();
		Connection connection = operator.connect(DBInfor.DB_PATH);

		list = operator.loadTagList(connection);

		try {
			connection.close();
		} catch (SQLException e) {
			e.printStackTrace();
		}
		return list;
	}
	public boolean setOrderCover(SOrder order) {
		boolean result = false;
		SqlOperator operator = new SqlOperator();
		Connection connection = operator.connect(DBInfor.DB_PATH);

		result = operator.updateOrder(order, connection);

		try {
			connection.close();
		} catch (SQLException e) {
			e.printStackTrace();
		}
		return result;
	}

	public boolean deleteOrder(SOrder order) {
		boolean result = false;
		SqlOperator operator = new SqlOperator();
		Connection connection = operator.connect(DBInfor.DB_PATH);

		result = operator.deleteOrder(order, connection);

		try {
			connection.close();
		} catch (SQLException e) {
			e.printStackTrace();
		}
		return result;
	}

	public boolean renameOrderName(SOrder order) {
		boolean result = false;
		SqlOperator operator = new SqlOperator();
		Connection connection = operator.connect(DBInfor.DB_PATH);

		result = operator.updateOrder(order, connection);

		try {
			connection.close();
		} catch (SQLException e) {
			e.printStackTrace();
		}
		return result;
	}

	public boolean deleteItemFromOrder(SOrder order, int pos) {
		boolean result = false;
		SqlOperator operator = new SqlOperator();
		Connection connection = operator.connect(DBInfor.DB_PATH);

		result = operator.deleteOrderItem(order.getImgPathIdList().get(pos), connection);

		try {
			connection.close();
		} catch (SQLException e) {
			e.printStackTrace();
		}
		return result;
	}

	public void getOrderItemList(SOrder order) {
		if (order == null) {
			return;
		}
		SqlOperator operator = new SqlOperator();
		Connection connection = operator.connect(DBInfor.DB_PATH);

		operator.queryOrderItems(order, connection);

		try {
			connection.close();
		} catch (SQLException e) {
			e.printStackTrace();
		}
	}

	public boolean decipherOrderAsFolder(SOrder order) {
		boolean result = true;
		SqlOperator operator = new SqlOperator();
		Connection connection = operator.connect(DBInfor.DB_PATH);

		if (order.getImgPathList() == null || order.getImgPathList().size() == 0) {
			operator.queryOrderItems(order, connection);
		}

		try {
			connection.close();
		} catch (SQLException e) {
			e.printStackTrace();
		}

		if (order.getImgPathList() != null) {
			Encrypter encrypter = new SimpleEncrypter();
			String target = Configuration.APP_DIR_IMG_SAVEAS + "/" + order.getName();
			File file = new File(target);
			if (!file.exists()) {
				file.mkdir();
			}
			for (String path:order.getImgPathList()) {
				file = new File(path);
				encrypter.restore(file, target);
			}
		}

		return result;
	}

	private void clearOrderList() {

		if (orderList != null) {
			orderList.clear();
		}
	}
	public void sortOrderByDate() {
		if (orderList != null) {
			clearOrderList();

			SqlOperator operator = new SqlOperator();
			Connection connection = operator.connect(DBInfor.DB_PATH);

			orderList = operator.queryAllOrders(connection);
			if (orderList != null) {
				int size = orderList.size();
				SOrder order = null;
				for (int i = 0; i < size/2; i ++) {
					order = orderList.get(i);
					orderList.set(i, orderList.get(size - 1 - i));
					orderList.set(size - 1 - i, order);
				}
			}

			try {
				connection.close();
			} catch (SQLException e) {
				e.printStackTrace();
			}
		}
	}

	public void sortOrderByName() {
		if (orderList != null) {
			Collections.sort(orderList, new Comparator<SOrder>() {

				@Override
				public int compare(SOrder lhs, SOrder rhs) {

					return lhs.getName().toLowerCase().compareTo(rhs.getName().toLowerCase());
				}

			});
		}
	}

	public void shuffleOrderItems(SOrder currentOrder, int i, int index) {
		List<String> names = currentOrder.getImgPathList();
		String name = names.get(i);
		names.set(i, names.get(index));
		names.set(index, name);

		List<Integer> ids = currentOrder.getImgPathIdList();
		int id = ids.get(i);
		ids.set(i, ids.get(index));
		ids.set(index, id);
	}
	public void sortOrderByItemNumber() {
		if (orderList != null) {
			Collections.sort(orderList, new Comparator<SOrder>() {

				@Override
				public int compare(SOrder lhs, SOrder rhs) {

					return rhs.getItemNumber() - lhs.getItemNumber();
				}
			});
		}
	}

	public STag queryTag(String tName) {
		STag tag = null;
		SqlOperator operator = new SqlOperator();
		Connection connection = operator.connect(DBInfor.DB_PATH);

		tag = operator.queryTag(tName, connection);

		try {
			connection.close();
		} catch (SQLException e) {
			e.printStackTrace();
		}
		return tag;
	}

	public STag addTag(String tName) {
		STag tag = null;
		SqlOperator operator = new SqlOperator();
		Connection connection = operator.connect(DBInfor.DB_PATH);

		operator.insertTag(tName, connection);
		tag = queryTag(tName);

		try {
			connection.close();
		} catch (SQLException e) {
			e.printStackTrace();
		}
		return tag;
	}

	public void deleteTag(STag sTag, List<SOrder> list) {
		SqlOperator operator = new SqlOperator();
		Connection connection = operator.connect(DBInfor.DB_PATH);

		operator.deleteTag(sTag, connection);
		if (list != null && list.size() > 0) {
			operator.deleteAllOrdersInTag(sTag, connection);
		}

		try {
			connection.close();
		} catch (SQLException e) {
			e.printStackTrace();
		}
	}

	public SOrderCount queryOrderCount(int orderId) {
		SqlOperator operator = new SqlOperator();
		Connection connection = operator.connect(DBInfor.DB_PATH);

		SOrderCount count = operator.queryOrderCount(orderId, connection);
		try {
			connection.close();
		} catch (SQLException e) {
			e.printStackTrace();
		}
		return count;
	}
	public void accessOrder(SOrder order) {
		SqlOperator operator = new SqlOperator();
		Connection connection = operator.connect(DBInfor.DB_PATH);

		SOrderCount orderCount = order.getOrderCount();
		if (orderCount == null) {
			orderCount = operator.queryOrderCount(order.getId(), connection);
			order.setOrderCount(orderCount);
		}
		orderCount.countAll ++;
		Calendar calendar = Calendar.getInstance();
		if (calendar.get(Calendar.YEAR) != orderCount.lastYear) {
			orderCount.countYear = 1;
			orderCount.countMonth = 1;
			orderCount.countWeek = 1;
			orderCount.countDay = 1;
		}
		else {
			orderCount.countYear ++;
			if (calendar.get(Calendar.MONTH) + 1 != orderCount.lastMonth) {
				orderCount.countMonth = 1;
				orderCount.countDay = 1;
			}
			else {
				orderCount.countMonth ++;
			}

			if (calendar.get(Calendar.WEEK_OF_YEAR) != orderCount.lastWeek) {
				orderCount.countWeek = 1;
				orderCount.countDay = 1;
			}
			else {
				orderCount.countWeek ++;
				if (calendar.get(Calendar.DAY_OF_MONTH) != orderCount.lastDay) {
					orderCount.countDay = 1;
				}
				else {
					orderCount.countDay ++;
				}
			}
		}
		orderCount.lastYear = calendar.get(Calendar.YEAR);
		orderCount.lastMonth = calendar.get(Calendar.MONTH) + 1;
		orderCount.lastWeek = calendar.get(Calendar.WEEK_OF_YEAR);
		orderCount.lastDay = calendar.get(Calendar.DAY_OF_MONTH);

		operator.saveOrderCount(orderCount, connection);

		try {
			connection.close();
		} catch (SQLException e) {
			e.printStackTrace();
		}
	}

	public List<String> loadSOrderPreviews(SOrder order, int number) {
		if (order.getImgPathList() == null) {
			getOrderItemList(order);
		}

		List<String> list = null;
		if (order.getImgPathList() != null) {
			int count = 0, maxNumber = order.getImgPathList().size();
			list = new ArrayList<String>();
			Collections.shuffle(order.getImgPathList());
			while (count < number && count < maxNumber) {
				list.add(order.getImgPathList().get(count));
				count ++;
			}
		}
		return list;
	}

	public List<String> loadSOrderCovers(SOrder order, int number) {
		if (order.getImgPathList() == null) {
			getOrderItemList(order);
		}

		List<String> list = null;
		if (order.getImgPathList() != null) {
			int count = 0, maxNumber = order.getImgPathList().size();
			list = new ArrayList<String>();
			Collections.shuffle(order.getImgPathList());
			while (count < number && count < maxNumber) {
				list.add(order.getImgPathList().get(count));
				count ++;
			}
		}
		return list;
	}

	public void moveToFolder(List<String> pathList, File targetFolder
			, final Handler handler) {
		SqlOperator operator = new SqlOperator();
		Connection connection = operator.connect(DBInfor.DB_PATH);

		operator.updateOrderItemPath(pathList, targetFolder.getPath()
				, new SqlOperator.OnOrderItemMoveTrigger() {

					@Override
					public void onTrigger(String src, String target, boolean isAllFinish) {
						FileIO fileIO = new FileIO();
						Encrypter encrypter = EncrypterFactory.create();
						fileIO.moveFile(src, target);

						src = src.replace(encrypter.getFileExtra(), encrypter.getNameExtra());
						target = target.replace(encrypter.getFileExtra(), encrypter.getNameExtra());
						fileIO.moveFile(src, target);

						Message msg = new Message();
						msg.what = isAllFinish ? Constants.STATUS_MOVE_FILE_FINISH:Constants.STATUS_MOVE_FILE_DONE;
						handler.sendMessage(msg);
					}

					@Override
					public void onNotSupport(String src, boolean allFinish) {
						Message msg = new Message();
						msg.what = Constants.STATUS_MOVE_FILE_UNSUPORT;
						Bundle bundle = new Bundle();
						bundle.putBoolean(Constants.KEY_MOVETO_UNSUPPORT_FINISH, allFinish);
						bundle.putString(Constants.KEY_MOVETO_UNSUPPORT_SRC, src);
						msg.setData(bundle);
						handler.sendMessage(msg);
					}
				}, connection);

		try {
			connection.close();
		} catch (SQLException e) {
			e.printStackTrace();
		}
	}

}
