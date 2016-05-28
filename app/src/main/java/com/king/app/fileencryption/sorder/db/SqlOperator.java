package com.king.app.fileencryption.sorder.db;

import java.lang.reflect.UndeclaredThrowableException;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

import android.util.Log;

import com.king.app.fileencryption.data.Constants;
import com.king.app.fileencryption.data.DBInfor;
import com.king.app.fileencryption.filemanager.entity.FileBean;
import com.king.app.fileencryption.open.image.ImageValue;
import com.king.app.fileencryption.sorder.entity.SOrder;
import com.king.app.fileencryption.sorder.entity.SOrderCount;
import com.king.app.fileencryption.sorder.entity.STag;

public class SqlOperator {

	private final String TAG = "SqlOperator";
	public SqlOperator() {
		try {
			Class.forName("org.sqldroid.SqldroidDriver").newInstance();
		} catch (InstantiationException e) {
			e.printStackTrace();
		} catch (IllegalAccessException e) {
			e.printStackTrace();
		} catch (ClassNotFoundException e) {
			e.printStackTrace();
		}
	}
	public Connection connect(String dbFile) {
		try {
			//Connection connection = DriverManager.getConnection("jdbc:sqlite:" + dbFile);
			Connection connection = DriverManager.getConnection("jdbc:sqldroid:" + dbFile);
			return connection;
		} catch (SQLException e) {
			e.printStackTrace();
		} catch (Exception e) {
			e.printStackTrace();
		}
		return null;
	}

	public boolean isOrderExist(String name, Connection connection) {
		Statement stmt = null;
		try {
			stmt = connection.createStatement();
			ResultSet set = null;

			set = stmt.executeQuery("SELECT * FROM " + DBInfor.TABLE_ORDER + " WHERE " + DBInfor.TO_COL_NAME + " = '" + name + "'");

			if (set.next()) {
				return true;
			}
			set.close();
		} catch (SQLException e) {
			e.printStackTrace();
		} finally {
			try {
				if (stmt != null) {
					stmt.close();
				}
			} catch (SQLException e) {
				e.printStackTrace();
			}
		}
		return false;
	}

	public boolean insertOrder(SOrder order, Connection connection) {

		if (connection != null && order != null) {
			String sql = "INSERT INTO " + DBInfor.TABLE_ORDER
					+ "(" + DBInfor.TO_COL_NAME + "," + DBInfor.TO_COL_COVER + "," + DBInfor.TO_COL_TAGID + ")" +
					" VALUES(?,?,?)";
			PreparedStatement stmt = null;
			try {
				stmt = connection.prepareStatement(sql);
				stmt.setString(1, order.getName());
				stmt.setString(2, order.getCoverPath());
				stmt.setInt(3, order.getTag().getId());

				stmt.executeUpdate();
				return true;
			} catch (SQLException e) {
				e.printStackTrace();
			} catch (NumberFormatException e) {
				e.printStackTrace();
			} finally {
				if (stmt != null) {
					try {
						stmt.close();
					} catch (SQLException e) {
						e.printStackTrace();
					}
				}
			}
		}
		return false;
	}

	public int queryOrderIdAfterInsert(Connection connection) {
		int id = -1;
		Statement stmt = null;
		try {
			stmt = connection.createStatement();
			ResultSet set = null;

			set = stmt.executeQuery("SELECT seq FROM " + DBInfor.TABLE_SEQUENCE
					+ " WHERE " + DBInfor.TS_COL_NAME + " = '" + DBInfor.TABLE_ORDER + "'");

			if (set.next()) {
				id = set.getInt(1);
			}
			set.close();
		} catch (SQLException e) {
			e.printStackTrace();
		} finally {
			try {
				if (stmt != null) {
					stmt.close();
				}
			} catch (SQLException e) {
				e.printStackTrace();
			}
		}
		return id;
	}

	public boolean updateOrder(SOrder order, Connection connection) {

		if (connection != null && order != null) {
			String sql = "UPDATE " + DBInfor.TABLE_ORDER + " SET " + DBInfor.TO_COL_NAME + " = '" + order.getName()
					+ "', " + DBInfor.TO_COL_COVER + " = '" + order.getCoverPath()
					+ "' WHERE " + DBInfor.TO_COL_ID + " = " + order.getId();;
			Statement stmt = null;
			try {
				stmt = connection.createStatement();
				stmt.executeUpdate(sql);
				return true;
			} catch (SQLException e) {
				e.printStackTrace();
			} catch (NumberFormatException e) {
				e.printStackTrace();
			} finally {
				if (stmt != null) {
					try {
						stmt.close();
					} catch (SQLException e) {
						e.printStackTrace();
					}
				}
			}
		}
		return false;
	}

	public boolean deleteOrder(SOrder order, Connection connection) {

		if (connection != null && order != null) {
			String sql = "DELETE FROM " + DBInfor.TABLE_ORDER
					+ " WHERE " + DBInfor.TO_COL_ID + " = " + order.getId();
			Statement stmt = null;
			try {
				stmt = connection.createStatement();
				stmt.executeUpdate(sql);

				sql = "DELETE FROM " + DBInfor.TABLE_ORDER_LIST
						+ " WHERE " + DBInfor.TOL_COL_OID + " = " + order.getId();
				stmt.executeUpdate(sql);
				return true;
			} catch (SQLException e) {
				e.printStackTrace();
			} catch (NumberFormatException e) {
				e.printStackTrace();
			} finally {
				if (stmt != null) {
					try {
						stmt.close();
					} catch (SQLException e) {
						e.printStackTrace();
					}
				}
			}
		}
		return false;
	}

	public interface OnOrderItemMoveTrigger {
		public void onTrigger(String src, String target, boolean allFinish);
		public void onNotSupport(String src, boolean allFinish);
	}

	public boolean updateOrderItemPath(List<String> originPathList, String targetPath
			, OnOrderItemMoveTrigger trigger, Connection connection) {

		if (connection != null) {


			StringBuffer buffer = null;
			String name = null;
			String target = null;
			PreparedStatement pStmt = null;
			try {
				connection.setAutoCommit(false);
				int index = 1;
				for (String originPath:originPathList) {

					boolean isFinish = index == originPathList.size();

					//update table fe_order_list(ol_item_path)
					buffer = new StringBuffer();
					name = originPath.substring(originPath.lastIndexOf("/"));//include '/' symbol
					target = targetPath + name;

					//不允许移动至原目录
					if (originPath.equals(target)) {
						trigger.onNotSupport(originPath, isFinish);
						index ++;
						continue;
					}

					buffer.append("UPDATE ").append(DBInfor.TABLE_ORDER_LIST).append(" SET ")
							.append(DBInfor.TOL_COL_PATH ).append(" = '").append(target)
							.append("' WHERE ").append(DBInfor.TOL_COL_PATH).append(" = '")
							.append(originPath).append("'");
					String sql = buffer.toString();
					Log.d(TAG, sql);
					pStmt = connection.prepareStatement(sql);
					pStmt.executeUpdate();

					//update table fe_orders(o_cover)
					buffer = new StringBuffer();
					buffer.append("UPDATE ").append(DBInfor.TABLE_ORDER).append(" SET ")
							.append(DBInfor.TO_COL_COVER ).append(" = '").append(target)
							.append("' WHERE ").append(DBInfor.TO_COL_COVER).append(" = '")
							.append(originPath).append("'");sql = buffer.toString();
					sql = buffer.toString();
					Log.d(TAG, sql);
					pStmt = connection.prepareStatement(sql);
					pStmt.executeUpdate();

					trigger.onTrigger(originPath, target, isFinish);

					index ++;
				}
				connection.commit();
				//Notice:
				//it's strange that once use transaction, no matter how I
				//try to close connection or statement, when this operation over
				//try other connection operation, it will happen 'database
				//is locked' exception, application will be locked till to die
				//only setAutoCommit(true) the lock will not happened
				connection.setAutoCommit(true);

				return true;
			} catch (SQLException e) {
				e.printStackTrace();
				try {
					connection.rollback();
				} catch (SQLException e1) {
					e1.printStackTrace();
				}
			} catch (NumberFormatException e) {
				e.printStackTrace();
				try {
					connection.rollback();
				} catch (SQLException e1) {
					e1.printStackTrace();
				}
			} finally {
				try {
					if (pStmt != null) {
						pStmt.close();
					}
				} catch (SQLException e) {
					e.printStackTrace();
				}
			}

		}
		return false;
	}

	public SOrder queryOrder(int orderId, Connection connection) {
		Statement stmt = null;
		SOrder order = null;
		try {
			stmt = connection.createStatement();
			ResultSet set = null;

			set = stmt.executeQuery("SELECT * FROM " + DBInfor.TABLE_ORDER
					+ " WHERE " + DBInfor.TO_COL_ID + " = " + orderId);

			if (set.next()) {
				order = new SOrder();
				order.setId(orderId);
				order.setName(set.getString(DBInfor.NUM_TO_COL_NAME));
				order.setCoverPath(set.getString(DBInfor.NUM_TO_COL_COVER));
				order.setTag(new STag(set.getInt(DBInfor.NUM_TO_COL_TAGID), null));
			}
			set.close();
		} catch (SQLException e) {
			e.printStackTrace();
		} finally {
			try {
				if (stmt != null) {
					stmt.close();
				}
			} catch (SQLException e) {
				e.printStackTrace();
			}
		}
		return order;
	}

	public SOrder queryOrderByName(String name, Connection connection) {
		Statement stmt = null;
		SOrder order = null;
		try {
			stmt = connection.createStatement();
			ResultSet set = null;

			set = stmt.executeQuery("SELECT * FROM " + DBInfor.TABLE_ORDER
					+ " WHERE " + DBInfor.TO_COL_NAME + " = '" + name + "'");

			if (set.next()) {
				order = new SOrder();
				order.setId(set.getInt(DBInfor.NUM_TO_COL_ID));
				order.setName(set.getString(DBInfor.NUM_TO_COL_NAME));
				order.setCoverPath(set.getString(DBInfor.NUM_TO_COL_COVER));
				order.setTag(new STag(set.getInt(DBInfor.NUM_TO_COL_TAGID), null));
			}
			set.close();
		} catch (SQLException e) {
			e.printStackTrace();
		} finally {
			try {
				if (stmt != null) {
					stmt.close();
				}
			} catch (SQLException e) {
				e.printStackTrace();
			}
		}
		return order;
	}

	public List<SOrder> queryAllOrders(Connection connection) {

		List<SOrder> list = null;
		Statement stmt = null;
		try {
			stmt = connection.createStatement();
			ResultSet set = null;

			set = stmt.executeQuery("SELECT * FROM " + DBInfor.TABLE_ORDER);
			SOrder order = null;
			while (set.next()) {
				if (list == null) {
					list = new ArrayList<SOrder>();
				}
				order = new SOrder();
				order.setId(set.getInt(DBInfor.NUM_TO_COL_ID));
				order.setName(set.getString(DBInfor.NUM_TO_COL_NAME));
				order.setCoverPath(set.getString(DBInfor.NUM_TO_COL_COVER));
				order.setTag(new STag(set.getInt(DBInfor.NUM_TO_COL_TAGID), null));
				list.add(order);
			}
			if (list != null) {
				set.close();
				for (int i = 0; i < list.size(); i ++) {
					order = list.get(i);
					set = stmt.executeQuery("SELECT COUNT (" + DBInfor.TOL_COL_ID + ") FROM "
							+ DBInfor.TABLE_ORDER_LIST + " WHERE " + DBInfor.TOL_COL_OID + " = " + order.getId());
					if (set.next()) {
						order.setItemNumber(set.getInt(1));
					}
					set.close();
				}
			}
			else {
				set.close();
			}

		} catch (SQLException e) {
			e.printStackTrace();
		} finally {
			try {
				if (stmt != null) {
					stmt.close();
				}
			} catch (SQLException e) {
				e.printStackTrace();
			}
		}
		return list;
	}

	public boolean isOrderItemExist(String path, int orderId, Connection connection) {
		Statement stmt = null;
		try {
			stmt = connection.createStatement();
			ResultSet set = null;

			set = stmt.executeQuery("SELECT * FROM " + DBInfor.TABLE_ORDER_LIST + " WHERE "
					+ DBInfor.TOL_COL_OID + " = " + orderId + " AND "+ DBInfor.TOL_COL_PATH + " = '" + path + "'");

			if (set.next()) {
				return true;
			}
			set.close();
		} catch (SQLException e) {
			e.printStackTrace();
		} finally {
			try {
				if (stmt != null) {
					stmt.close();
				}
			} catch (SQLException e) {
				e.printStackTrace();
			}
		}
		return false;
	}

	public boolean insertOrderItem(int orderId, String itemPath, Connection connection) {

		if (connection != null && itemPath != null) {
			String sql = "INSERT INTO " + DBInfor.TABLE_ORDER_LIST
					+ "(" + DBInfor.TOL_COL_OID + "," + DBInfor.TOL_COL_PATH + ")" +
					" VALUES(?,?)";
			PreparedStatement stmt = null;
			try {
				stmt = connection.prepareStatement(sql);
				stmt.setInt(1, orderId);
				stmt.setString(2, itemPath);

				stmt.executeUpdate();
				return true;
			} catch (SQLException e) {
				e.printStackTrace();
			} catch (NumberFormatException e) {
				e.printStackTrace();
			} finally {
				if (stmt != null) {
					try {
						stmt.close();
					} catch (SQLException e) {
						e.printStackTrace();
					}
				}
			}
		}
		return false;
	}

	public boolean deleteOrderItem(int itemId, Connection connection) {

		if (connection != null) {
			String sql = "DELETE FROM " + DBInfor.TABLE_ORDER_LIST
					+ " WHERE " + DBInfor.TOL_COL_ID + " = " + itemId;
			Statement stmt = null;
			try {
				stmt = connection.createStatement();
				stmt.executeUpdate(sql);
				return true;
			} catch (SQLException e) {
				e.printStackTrace();
			} catch (NumberFormatException e) {
				e.printStackTrace();
			} finally {
				if (stmt != null) {
					try {
						stmt.close();
					} catch (SQLException e) {
						e.printStackTrace();
					}
				}
			}
		}
		return false;
	}

	public boolean queryOrderItems(SOrder order, Connection connection) {

		Statement stmt = null;
		try {
			stmt = connection.createStatement();
			ResultSet set = null;

			set = stmt.executeQuery("SELECT * FROM " + DBInfor.TABLE_ORDER_LIST + " WHERE " + DBInfor.TOL_COL_OID + " = " + order.getId());

			if (order.getImgPathIdList() != null) {
				order.getImgPathList().clear();
				order.getImgPathIdList().clear();
			}
			while (set.next()) {
				if (order.getImgPathList() == null) {
					order.setImgPathList(new ArrayList<String>());
					order.setImgPathIdList(new ArrayList<Integer>());
				}
				order.getImgPathIdList().add(set.getInt(DBInfor.NUM_TOL_COL_ID));
				order.getImgPathList().add(set.getString(DBInfor.NUM_TOL_COL_PATH));
			}
			if (order.getImgPathList() != null) {
				order.setItemNumber(order.getImgPathList().size());
			}
			set.close();
			return true;
		} catch (SQLException e) {
			e.printStackTrace();
		} finally {
			try {
				if (stmt != null) {
					stmt.close();
				}
			} catch (SQLException e) {
				e.printStackTrace();
			}
		}
		return false;
	}

	public List<String> queryOrderItemsByName(String name, Connection connection) {

		SOrder order = queryOrderByName(name, connection);
		if (order == null) {
			return null;
		}

		Statement stmt = null;
		try {
			stmt = connection.createStatement();
			ResultSet set = null;

			set = stmt.executeQuery("SELECT * FROM " + DBInfor.TABLE_ORDER_LIST + " WHERE "
					+ DBInfor.TOL_COL_OID + " = " + order.getId());

			if (order.getImgPathIdList() != null) {
				order.getImgPathList().clear();
				order.getImgPathIdList().clear();
			}
			while (set.next()) {
				if (order.getImgPathList() == null) {
					order.setImgPathList(new ArrayList<String>());
					order.setImgPathIdList(new ArrayList<Integer>());
				}
				order.getImgPathIdList().add(set.getInt(DBInfor.NUM_TOL_COL_ID));
				order.getImgPathList().add(set.getString(DBInfor.NUM_TOL_COL_PATH));
			}
			if (order.getImgPathList() != null) {
				order.setItemNumber(order.getImgPathList().size());
			}
			set.close();
		} catch (SQLException e) {
			e.printStackTrace();
		} finally {
			try {
				if (stmt != null) {
					stmt.close();
				}
			} catch (SQLException e) {
				e.printStackTrace();
			}
		}
		return order.getImgPathList();
	}
	public List<STag> loadTagList(Connection connection) {
		List<STag> list = null;
		Statement stmt = null;
		try {
			stmt = connection.createStatement();
			ResultSet set = null;
			String sql = "SELECT * FROM " + DBInfor.TABLE_TAG
					+ " ORDER BY " + DBInfor.TT_COL_TAG + " ASC";
			set = stmt.executeQuery(sql);
			STag tag = null;
			while (set.next()) {
				if (list == null) {
					list = new ArrayList<STag>();
				}
				tag = new STag(set.getInt(DBInfor.NUM_TT_COL_ID), set.getString(DBInfor.NUM_TT_COL_TAG));
				list.add(tag);
			}
			set.close();
		} catch (SQLException e) {
			e.printStackTrace();
		} finally {
			try {
				if (stmt != null) {
					stmt.close();
				}
			} catch (SQLException e) {
				e.printStackTrace();
			}
		}
		return list;
	}
	public STag queryTag(String tName, Connection connection) {
		Statement stmt = null;
		STag tag = null;
		try {
			stmt = connection.createStatement();
			ResultSet set = null;

			set = stmt.executeQuery("SELECT * FROM " + DBInfor.TABLE_TAG + " WHERE "
					+ DBInfor.TT_COL_TAG + " = '" + tName + "'");

			if (set.next()) {
				tag = new STag(set.getInt(DBInfor.NUM_TT_COL_ID), tName);
			}
			set.close();
		} catch (SQLException e) {
			e.printStackTrace();
		} finally {
			try {
				if (stmt != null) {
					stmt.close();
				}
			} catch (SQLException e) {
				e.printStackTrace();
			}
		}
		return tag;
	}
	public boolean insertTag(String tName, Connection connection) {
		if (connection != null && tName != null) {
			String sql = "INSERT INTO " + DBInfor.TABLE_TAG
					+ "(" + DBInfor.TT_COL_TAG + ")" +
					" VALUES(?)";
			PreparedStatement stmt = null;
			try {
				stmt = connection.prepareStatement(sql);
				stmt.setString(1, tName);

				stmt.executeUpdate();
				return true;
			} catch (SQLException e) {
				e.printStackTrace();
			} catch (NumberFormatException e) {
				e.printStackTrace();
			} finally {
				if (stmt != null) {
					try {
						stmt.close();
					} catch (SQLException e) {
						e.printStackTrace();
					}
				}
			}
		}
		return false;
	}
	public void deleteTag(STag sTag, Connection connection) {
		if (connection != null && sTag != null) {
			String sql = "DELETE FROM " + DBInfor.TABLE_TAG
					+ " WHERE " + DBInfor.TT_COL_ID + " = " + sTag.getId();
			Statement stmt = null;
			try {
				stmt = connection.createStatement();
				stmt.executeUpdate(sql);

			} catch (SQLException e) {
				e.printStackTrace();
			} catch (NumberFormatException e) {
				e.printStackTrace();
			} finally {
				if (stmt != null) {
					try {
						stmt.close();
					} catch (SQLException e) {
						e.printStackTrace();
					}
				}
			}
		}
	}

	/**
	 * 删除整个 order的内容
	 * @param tag
	 * @param list
	 * @param connection
	 */
	public void deleteAllOrdersInTag(STag tag, Connection connection) {

		if (connection != null && tag != null) {
			String sql = "DELETE FROM " + DBInfor.TABLE_ORDER
					+ " WHERE " + DBInfor.TO_COL_TAGID + " = " + tag.getId();
			Statement stmt = null;
			try {
				stmt = connection.createStatement();
				stmt.executeUpdate(sql);

			} catch (SQLException e) {
				e.printStackTrace();
			} catch (NumberFormatException e) {
				e.printStackTrace();
			} finally {
				if (stmt != null) {
					try {
						stmt.close();
					} catch (SQLException e) {
						e.printStackTrace();
					}
				}
			}
		}
	}

	public void deleteOrdersInTag(STag tag, List<SOrder> list, Connection connection) {
		//TODO
	}

	public boolean saveOrderCount(SOrderCount count, Connection connection) {

		if (connection != null && count != null) {
			StringBuffer buffer = new StringBuffer("UPDATE ");
			buffer.append(DBInfor.TABLE_ORDER_COUNT).append(" SET ")
					.append(DBInfor.TOC_COL[1]).append(" = ").append(count.countAll)
					.append(", ").append(DBInfor.TOC_COL[2]).append(" = ").append(count.countYear)
					.append(", ").append(DBInfor.TOC_COL[3]).append(" = ").append(count.countMonth)
					.append(", ").append(DBInfor.TOC_COL[4]).append(" = ").append(count.countWeek)
					.append(", ").append(DBInfor.TOC_COL[5]).append(" = ").append(count.countDay)
					.append(", ").append(DBInfor.TOC_COL[6]).append(" = ").append(count.lastYear)
					.append(", ").append(DBInfor.TOC_COL[7]).append(" = ").append(count.lastMonth)
					.append(", ").append(DBInfor.TOC_COL[8]).append(" = ").append(count.lastWeek)
					.append(", ").append(DBInfor.TOC_COL[9]).append(" = ").append(count.lastDay)
					.append(" WHERE ").append(DBInfor.TOC_COL[0]).append(" = ").append(count.orderId);

			Statement stmt = null;
			try {
				String sql = buffer.toString();
				stmt = connection.createStatement();
				stmt.executeUpdate(sql);
				return true;
			} catch (SQLException e) {
				e.printStackTrace();
			} catch (NumberFormatException e) {
				e.printStackTrace();
			} finally {
				if (stmt != null) {
					try {
						stmt.close();
					} catch (SQLException e) {
						e.printStackTrace();
					}
				}
			}
		}
		return false;
	}
	public SOrderCount queryOrderCount(int orderId, Connection connection) {
		Statement stmt = null;
		SOrderCount orderCount = null;
		try {
			stmt = connection.createStatement();
			ResultSet set = null;

			set = stmt.executeQuery("SELECT * FROM " + DBInfor.TABLE_ORDER_COUNT
					+ " WHERE " + DBInfor.TOC_COL[0] + " = " + orderId);

			if (set.next()) {
				orderCount = new SOrderCount();
				orderCount.orderId = orderId;
				orderCount.countAll = set.getInt(DBInfor.TOC_COL[1]);
				orderCount.countYear = set.getInt(DBInfor.TOC_COL[2]);
				orderCount.countMonth = set.getInt(DBInfor.TOC_COL[3]);
				orderCount.countWeek = set.getInt(DBInfor.TOC_COL[4]);
				orderCount.countDay = set.getInt(DBInfor.TOC_COL[5]);
				orderCount.lastYear = set.getInt(DBInfor.TOC_COL[6]);
				orderCount.lastMonth = set.getInt(DBInfor.TOC_COL[7]);
				orderCount.lastWeek = set.getInt(DBInfor.TOC_COL[8]);
				orderCount.lastDay = set.getInt(DBInfor.TOC_COL[9]);
			}
			set.close();
		} catch (SQLException e) {
			e.printStackTrace();
		} finally {
			try {
				if (stmt != null) {
					stmt.close();
				}
			} catch (SQLException e) {
				e.printStackTrace();
			}
		}
		return orderCount;
	}
	public boolean insertOrderCount(int orderId, Connection connection) {

		if (connection != null) {
			Statement stmt = null;
			try {
				String sql = "INSERT INTO " + DBInfor.TABLE_ORDER_COUNT
						+ " VALUES(" + orderId + ",0,0,0,0,0,0,0,0,0)";
				stmt = connection.createStatement();
				stmt.executeUpdate(sql);
				return true;
			} catch (SQLException e) {
				e.printStackTrace();
			} catch (NumberFormatException e) {
				e.printStackTrace();
			} finally {
				if (stmt != null) {
					try {
						stmt.close();
					} catch (SQLException e) {
						e.printStackTrace();
					}
				}
			}
		}
		return false;
	}
	public SOrderCount deleteOrderCount(int orderId, Connection connection) {
		Statement stmt = null;
		SOrderCount orderCount = null;
		try {
			stmt = connection.createStatement();
			ResultSet set = null;

			set = stmt.executeQuery("DELETE FROM " + DBInfor.TABLE_ORDER_COUNT
					+ " WHERE " + DBInfor.TOC_COL[0] + " = " + orderId);

			if (set.next()) {
				orderCount = new SOrderCount();
				orderCount.orderId = orderId;
				orderCount.countAll = set.getInt(DBInfor.TOC_COL[1]);
				orderCount.countYear = set.getInt(DBInfor.TOC_COL[2]);
				orderCount.countMonth = set.getInt(DBInfor.TOC_COL[3]);
				orderCount.countWeek = set.getInt(DBInfor.TOC_COL[4]);
				orderCount.countDay = set.getInt(DBInfor.TOC_COL[5]);
				orderCount.lastYear = set.getInt(DBInfor.TOC_COL[6]);
				orderCount.lastMonth = set.getInt(DBInfor.TOC_COL[7]);
				orderCount.lastWeek = set.getInt(DBInfor.TOC_COL[8]);
				orderCount.lastDay = set.getInt(DBInfor.TOC_COL[9]);
			}
			set.close();
		} catch (SQLException e) {
			e.printStackTrace();
		} finally {
			try {
				if (stmt != null) {
					stmt.close();
				}
			} catch (SQLException e) {
				e.printStackTrace();
			}
		}
		return orderCount;
	}

	/**
	 * query many items
	 * @param keyList its size must be equal with param values'
	 * @param values to save key referring result
	 * @param connection
	 */
	public void queryImageValues(List<String> keyList
			, List<ImageValue> values, Connection connection) {
		PreparedStatement stmt = null;
		ImageValue value = null;
		try {
			ResultSet set = null;
			String sql = "SELECT * FROM " + DBInfor.TABLE_IMAGE_PIXEL
					+ " WHERE " + DBInfor.TIP_COL_NAME + " = ?";
			stmt = connection.prepareStatement(sql);

			for (int i = 0; i < keyList.size(); i ++) {
				stmt.setString(1, keyList.get(i));
				set = stmt.executeQuery();
				if (set.next()) {
					value = new ImageValue();
					value.setName(keyList.get(i));
					value.setId(set.getInt(DBInfor.NUM_TIP_COL_ID));
					value.setWidth(set.getInt(DBInfor.NUM_TIP_COL_WIDTH));
					value.setHeight(set.getInt(DBInfor.NUM_TIP_COL_HEIGHT));
					value.setOther(set.getString(DBInfor.NUM_TIP_COL_OTHER));
					values.set(i, value);
				}
				set.close();
			}
		} catch (SQLException e) {
			e.printStackTrace();
		} finally {
			try {
				if (stmt != null) {
					stmt.close();
				}
			} catch (SQLException e) {
				e.printStackTrace();
			}
		}
	}

	public ImageValue queryImageValue(String key, Connection connection) {
		Statement stmt = null;
		ImageValue value = null;
		try {
			stmt = connection.createStatement();
			ResultSet set = null;

			set = stmt.executeQuery("SELECT * FROM " + DBInfor.TABLE_IMAGE_PIXEL
					+ " WHERE " + DBInfor.TIP_COL_NAME + " = '" + key + "'");

			if (set.next()) {
				value = new ImageValue();
				value.setName(key);
				value.setId(set.getInt(DBInfor.NUM_TIP_COL_ID));
				value.setWidth(set.getInt(DBInfor.NUM_TIP_COL_WIDTH));
				value.setHeight(set.getInt(DBInfor.NUM_TIP_COL_HEIGHT));
				value.setOther(set.getString(DBInfor.NUM_TIP_COL_OTHER));
			}
			set.close();
		} catch (SQLException e) {
			e.printStackTrace();
		} finally {
			try {
				if (stmt != null) {
					stmt.close();
				}
			} catch (SQLException e) {
				e.printStackTrace();
			}
		}
		return value;
	}
	public boolean insertImagePixel(ImageValue value, Connection connection) {
		if (connection != null && value != null) {
			String sql = "INSERT INTO " + DBInfor.TABLE_IMAGE_PIXEL
					+ "(" + DBInfor.TIP_COL_NAME + "," + DBInfor.TIP_COL_WIDTH + "," + DBInfor.TIP_COL_HEIGHT + ")" +
					" VALUES(?,?,?)";
			PreparedStatement stmt = null;
			try {
				stmt = connection.prepareStatement(sql);
				stmt.setString(1, value.getName());
				stmt.setInt(2, value.getWidth());
				stmt.setInt(3, value.getHeight());
				stmt.executeUpdate();
				return true;
			} catch (SQLException e) {
				e.printStackTrace();
			} catch (NumberFormatException e) {
				e.printStackTrace();
			} finally {
				if (stmt != null) {
					try {
						stmt.close();
					} catch (SQLException e) {
						e.printStackTrace();
					}
				}
			}
		}
		return false;
	}

	/**
	 * 插入多条文件记录
	 * @param list
	 * @param connection
	 * @return
	 */
	public boolean insertFileBeans(List<FileBean> list, Connection connection) {
		if (connection != null && list != null) {
			String sql = "INSERT INTO " + DBInfor.TABLE_FILES
					+ "(f_path, f_time, f_time_tag, f_width, f_height, f_size, f_other)" +
					" VALUES(?,?,?,?,?,?,?)";
			PreparedStatement stmt = null;
			try {
				stmt = connection.prepareStatement(sql);
				for (int i = 0; i < list.size(); i ++) {
					FileBean bean = list.get(i);
					stmt.setString(1, bean.getPath());
					stmt.setLong(2, bean.getTime());
					stmt.setString(3, bean.getTimeTag());
					stmt.setInt(4, bean.getWidth());
					stmt.setInt(5, bean.getHeight());
					stmt.setLong(6, bean.getSize());
					stmt.setString(7, bean.getOther());
					stmt.executeUpdate();
				}
				return true;
			} catch (SQLException e) {
				e.printStackTrace();
			} catch (NumberFormatException e) {
				e.printStackTrace();
			} finally {
				if (stmt != null) {
					try {
						stmt.close();
					} catch (SQLException e) {
						e.printStackTrace();
					}
				}
			}
		}
		return false;
	}
	/**
	 * 插入一条文件记录
	 * @param bean
	 * @param connection
	 * @return
	 */
	public boolean insertFileBean(FileBean bean, Connection connection) {
		if (connection != null && bean != null) {
			String sql = "INSERT INTO " + DBInfor.TABLE_FILES
					+ "(f_path, f_time, f_time_tag, f_width, f_height, f_size, f_other)" +
					" VALUES(?,?,?,?,?,?,?)";
			PreparedStatement stmt = null;
			try {
				stmt = connection.prepareStatement(sql);
				stmt.setString(1, bean.getPath());
				stmt.setLong(2, bean.getTime());
				stmt.setString(3, bean.getTimeTag());
				stmt.setInt(4, bean.getWidth());
				stmt.setInt(5, bean.getHeight());
				stmt.setLong(6, bean.getSize());
				stmt.setString(7, bean.getOther());
				stmt.executeUpdate();
				return true;
			} catch (SQLException e) {
				e.printStackTrace();
			} catch (NumberFormatException e) {
				e.printStackTrace();
			} finally {
				if (stmt != null) {
					try {
						stmt.close();
					} catch (SQLException e) {
						e.printStackTrace();
					}
				}
			}
		}
		return false;
	}

	/**
	 *
	 * @param connection
	 * @param orderBy eg. "fe_time DESC" or "fe_time ASC", no order by if null
	 * @return
	 */
	public List<FileBean> queryAllFileBeans(Connection connection, String orderBy) {

		List<FileBean> list = null;
		Statement stmt = null;
		try {
			stmt = connection.createStatement();
			ResultSet set = null;

			String sql = "SELECT * FROM " + DBInfor.TABLE_FILES;
			if (orderBy != null) {
				sql = sql + " ORDER BY " + orderBy;
			}
			set = stmt.executeQuery(sql);
			while (set.next()) {
				if (list == null) {
					list = new ArrayList<FileBean>();
				}
				FileBean bean = new FileBean();
				bean.setId(set.getInt(1));
				bean.setPath(set.getString(2));
				bean.setTime(set.getLong(3));
				bean.setTimeTag(set.getString(4));
				bean.setWidth(set.getInt(5));
				bean.setHeight(set.getInt(6));
				bean.setSize(set.getLong(7));
				bean.setOther(set.getString(8));
				list.add(bean);
			}
			set.close();

		} catch (SQLException e) {
			e.printStackTrace();
		} finally {
			try {
				if (stmt != null) {
					stmt.close();
				}
			} catch (SQLException e) {
				e.printStackTrace();
			}
		}
		return list;
	}

	public boolean isFileBeanExist(String path, Connection connection) {
		Statement stmt = null;
		try {
			stmt = connection.createStatement();
			ResultSet set = null;

			if (path.contains("'")) {//文件夹名中有时候会有"'"符号，会出现异常
				if (Constants.DEBUG) {
					Log.e(Constants.LOG_TAG_INIT, "delete ' in folder name");
				}
				path = path.replace("'", "");
			}
			set = stmt.executeQuery("SELECT f_id FROM " + DBInfor.TABLE_FILES
					+ " WHERE f_path = '" + path + "'");

			if (set.next()) {
				return true;
			}
			set.close();
		} catch (SQLException e) {
			e.printStackTrace();
		} finally {
			try {
				if (stmt != null) {
					stmt.close();
				}
			} catch (SQLException e) {
				e.printStackTrace();
			}
		}
		return false;
	}

	/**
	 * 查询所有的文件按添加时间组成的日期集合
	 * @param order 1：顺序，其他均为倒序
	 * @param connection
	 * @return key:value
	 * 		eg. time:2015-12-20
	 * 			count:50
	 */
	public List<HashMap<String, String>> queryFileBeanTimeGroup(int order, Connection connection) {
		List<HashMap<String, String>> list = null;
		Statement stmt = null;
		try {
			stmt = connection.createStatement();
			ResultSet set = null;

			String desc = (order == 1 ? "ASC" : "DESC");

			set = stmt.executeQuery("SELECT f_time_tag, count(f_id) FROM " + DBInfor.TABLE_FILES
					+ " GROUP BY f_time_tag ORDER BY f_time " + desc);
			while (set.next()) {
				if (list == null) {
					list = new ArrayList<HashMap<String, String>>();
				}
				HashMap<String, String> map = new HashMap<String, String>();
				map.put("time", set.getString(1));
				map.put("count", "" + set.getInt(2));
				list.add(map);
			}
			set.close();

		} catch (SQLException e) {
			e.printStackTrace();
		} finally {
			try {
				if (stmt != null) {
					stmt.close();
				}
			} catch (SQLException e) {
				e.printStackTrace();
			}
		}
		return list;
	}

	/**
	 * 查询timeTag日期下所有的文件
	 * @param timeTag eg.2015-12-20
	 * @param connection
	 * @return
	 */
	public List<FileBean> queryFileBeansByTimeTag(String timeTag, Connection connection) {

		List<FileBean> list = null;
		Statement stmt = null;
		try {
			stmt = connection.createStatement();
			ResultSet set = null;

			set = stmt.executeQuery("SELECT * FROM " + DBInfor.TABLE_FILES
					+ " WHERE f_time_tag = '" + timeTag + "'");
			while (set.next()) {
				if (list == null) {
					list = new ArrayList<FileBean>();
				}
				FileBean bean = new FileBean();
				bean.setId(set.getInt(1));
				bean.setPath(set.getString(2));
				bean.setTime(set.getLong(3));
				bean.setTimeTag(set.getString(4));
				bean.setWidth(set.getInt(5));
				bean.setHeight(set.getInt(6));
				bean.setSize(set.getLong(7));
				bean.setOther(set.getString(8));
				list.add(bean);
			}
			set.close();

		} catch (SQLException e) {
			e.printStackTrace();
		} finally {
			try {
				if (stmt != null) {
					stmt.close();
				}
			} catch (SQLException e) {
				e.printStackTrace();
			}
		}
		return list;
	}

	public boolean deleteFileBean(long id, Connection connection) {

		if (connection != null) {
			String sql = "DELETE FROM " + DBInfor.TABLE_FILES
					+ " WHERE f_id = " + id;
			Statement stmt = null;
			try {
				stmt = connection.createStatement();
				stmt.executeUpdate(sql);
				return true;
			} catch (SQLException e) {
				e.printStackTrace();
			} catch (NumberFormatException e) {
				e.printStackTrace();
			} finally {
				if (stmt != null) {
					try {
						stmt.close();
					} catch (SQLException e) {
						e.printStackTrace();
					}
				}
			}
		}
		return false;
	}

	/**
	 * v6.2 add ol_pixel to table fe_order_list
	 * record image's width and height
	 * @return
	 */
	public static boolean isBelowVersion6_2() {

		Connection connection = new SqlOperator().connect(DBInfor.DB_PATH);
		Statement stmt = null;
		try {
			String sql = "SELECT * FROM " + DBInfor.TABLE_IMAGE_PIXEL;
			stmt = connection.createStatement();
			stmt.executeQuery(sql);
			return false;
		} catch (SQLException e) {
			e.printStackTrace();
		} catch (NumberFormatException e) {
			e.printStackTrace();
		} catch (UndeclaredThrowableException e) {
			//column not exist
			e.printStackTrace();
		} finally {
			if (stmt != null) {
				try {
					stmt.close();
				} catch (SQLException e) {
					e.printStackTrace();
				}
			}
			if (connection != null) {
				try {
					connection.close();
				} catch (SQLException e) {
					e.printStackTrace();
				}
			}
		}
		return true;
	}
	/**
	 * v6.2 add ol_pixel to table fe_order_list
	 * record image's width and height
	 * @return
	 */
	public static boolean addImagePixelTable() {

		Connection connection = new SqlOperator().connect(DBInfor.DB_PATH);
		Statement stmt = null;
		try {
			String sql = "CREATE TABLE 'fe_image_pixel' (" +
					"'fip_id' INTEGER PRIMARY KEY AUTOINCREMENT NOT NULL,"
					+ "'fip_name' TEXT NOT NULL,"
					+ "'fip_width' INTEGER,"
					+ "'fip_height' INTEGER,"
					+ "'fip_other' TEXT)";

			stmt = connection.createStatement();
			stmt.executeUpdate(sql);
			return true;
		} catch (SQLException e) {
			e.printStackTrace();
		} catch (NumberFormatException e) {
			e.printStackTrace();
		} finally {
			if (stmt != null) {
				try {
					stmt.close();
				} catch (SQLException e) {
					e.printStackTrace();
				}
			}
			if (connection != null) {
				try {
					connection.close();
				} catch (SQLException e) {
					e.printStackTrace();
				}
			}
		}
		return false;
	}

	/**
	 * v7.0 add fe_files to table
	 * record every file's time/size/width/height information
	 * @return
	 */
	public static boolean isBelowVersion7_0() {

		Connection connection = new SqlOperator().connect(DBInfor.DB_PATH);
		Statement stmt = null;
		try {
			String sql = "SELECT * FROM " + DBInfor.TABLE_FILES;
			stmt = connection.createStatement();
			stmt.executeQuery(sql);
			return false;
		} catch (SQLException e) {
			e.printStackTrace();
		} catch (NumberFormatException e) {
			e.printStackTrace();
		} catch (UndeclaredThrowableException e) {
			//column not exist
			e.printStackTrace();
		} finally {
			if (stmt != null) {
				try {
					stmt.close();
				} catch (SQLException e) {
					e.printStackTrace();
				}
			}
			if (connection != null) {
				try {
					connection.close();
				} catch (SQLException e) {
					e.printStackTrace();
				}
			}
		}
		return true;
	}
	/**
	 * v7.0 add fe_files to table
	 * record every file's time/size/width/height information
	 * @return
	 */
	public static boolean addFilesTable() {

		Connection connection = new SqlOperator().connect(DBInfor.DB_PATH);
		Statement stmt = null;
		try {
			String sql = "CREATE TABLE 'fe_files' (" +
					"'f_id' INTEGER PRIMARY KEY AUTOINCREMENT,"
					+ "'f_path' TEXT,"
					+ "'f_time' INTEGER,"
					+ "'f_time_tag' TEXT,"
					+ "'f_width' INTEGER,"
					+ "'f_height' INTEGER,"
					+ "'f_size' INTEGER,"
					+ "'f_other' TEXT)";

			stmt = connection.createStatement();
			stmt.executeUpdate(sql);
			return true;
		} catch (SQLException e) {
			e.printStackTrace();
		} catch (NumberFormatException e) {
			e.printStackTrace();
		} finally {
			if (stmt != null) {
				try {
					stmt.close();
				} catch (SQLException e) {
					e.printStackTrace();
				}
			}
			if (connection != null) {
				try {
					connection.close();
				} catch (SQLException e) {
					e.printStackTrace();
				}
			}
		}
		return false;
	}

	/**
	 * 获取guide界面区域数据
	 * @param minRate f_width/f_height
	 * @param maxRate f_width/f_height
	 * @param connection
	 * @return
	 */
	public List<String> queryGuideList(float minRate, float maxRate, Connection connection) {

		List<String> list = null;
		Statement stmt = null;
		try {
			stmt = connection.createStatement();
			ResultSet set = null;

			//不知道是不是SQL语法问题，这样无法查出结果来，一个<或>可以查出结果，但是两个同时存在就一定不行
			//用between也不行
//			String where = "f_width/f_height < " + maxRate
//					+ " AND f_width/f_height > " + minRate;

			//只能这样嵌套查询，><或者between都可以
			String sql = "SELECT f_path FROM "
					+ "(SELECT f_path, f_width*1.0/f_height AS rate FROM " + DBInfor.TABLE_FILES
					+ ") WHERE rate BETWEEN " + minRate + " AND " + maxRate;
			set = stmt.executeQuery(sql);
			while (set.next()) {
				if (list == null) {
					list = new ArrayList<String>();
				}
				list.add(set.getString(1));
			}
			set.close();

		} catch (SQLException e) {
			e.printStackTrace();
		} finally {
			try {
				if (stmt != null) {
					stmt.close();
				}
			} catch (SQLException e) {
				e.printStackTrace();
			}
		}
		return list;
	}
}
