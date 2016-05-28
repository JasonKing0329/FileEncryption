package com.king.app.fileencryption.guide.controller;

import java.sql.Connection;
import java.sql.SQLException;
import java.util.List;

import android.content.Context;
import android.util.Log;

import com.king.app.fileencryption.R;
import com.king.app.fileencryption.data.Configuration;
import com.king.app.fileencryption.data.DBInfor;
import com.king.app.fileencryption.sorder.db.SqlOperator;

public class GuideController {

	private class RateData {
		float min;
		float max;
		public RateData(float min, float max) {
			this.min = min;
			this.max = max;
		}
	}
	
	private Context mContext;
	
	public GuideController(Context context) {
		mContext = context;
	}

	public List<String> getLine1LeftList() {
		List<String> list = getListFromDatabase(R.dimen.guide_line1_left_width
				, R.dimen.guide_line1_left_height, 0.067f);
		if (Configuration.DEBUG) {
			Log.d(Configuration.TAG_AUTO_VIEW, "getLine1LeftList size=" + (list == null ? 0:list.size()));
		}
		return list;
	}

	public List<String> getLine1RightList() {
		List<String> list = getListFromDatabase(R.dimen.guide_line1_right_width
				, R.dimen.guide_line1_right_height, 0.092f);
		if (Configuration.DEBUG) {
			Log.d(Configuration.TAG_AUTO_VIEW, "getLine1RightList size=" + (list == null ? 0:list.size()));
		}
		return list;
	}

	public List<String> getLine2SquareList() {
		List<String> list = getListFromDatabase(R.dimen.guide_line2_square_size
				, R.dimen.guide_line2_square_size, 0.2f);
		if (Configuration.DEBUG) {
			Log.d(Configuration.TAG_AUTO_VIEW, "getLine2SquareList size=" + (list == null ? 0:list.size()));
		}
		return list;
	}

	public List<String> getLine2RightList() {
		List<String> list = getListFromDatabase(R.dimen.guide_line2_right_width
				, R.dimen.guide_line2_right_height, 0.067f);
		if (Configuration.DEBUG) {
			Log.d(Configuration.TAG_AUTO_VIEW, "getLine2RightList size=" + (list == null ? 0:list.size()));
		}
		return list;
	}

	public List<String> getline3List() {
		List<String> list = getListFromDatabase(R.dimen.guide_line3_width
				, R.dimen.guide_line3_height, 0.112f);
		if (Configuration.DEBUG) {
			Log.d(Configuration.TAG_AUTO_VIEW, "getline3List size=" + (list == null ? 0:list.size()));
		}
		return list;
	}

	private List<String> getListFromDatabase(int resWidth, int resHeight, float offset) {
		SqlOperator operator = new SqlOperator();
		Connection connection = operator.connect(DBInfor.DB_PATH);

		RateData rate = createRateData(resWidth, resHeight, offset);
		List<String> list = operator.queryGuideList(rate.min, rate.max, connection);
		
		try {
			connection.close();
		} catch (SQLException e) {
			e.printStackTrace();
		}
		return list;
	}

	private RateData createRateData(int resWidth, int resHeight, float offset) {
		int width = mContext.getResources().getDimensionPixelSize(resWidth);
		int height = mContext.getResources().getDimensionPixelSize(resHeight);
		float rate = (float) width / (float) height;
		float min = rate - offset;
		float max = rate + offset;
		if (Configuration.DEBUG) {
			Log.d(Configuration.TAG_AUTO_VIEW, "getListFromDatabase [" + width + "," + height + "]"
					+ "; rate[" + min + "," + max + "]");
		}
		return new RateData(min, max);
	}

}
