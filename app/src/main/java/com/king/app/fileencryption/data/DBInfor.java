package com.king.app.fileencryption.data;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.Calendar;

import com.king.app.fileencryption.sorder.entity.STag;

import android.content.Context;

public class DBInfor {

	public static String DB_PATH;
	public static final String TABLE_ORDER = "fe_orders";
	public static final String TABLE_ORDER_LIST = "fe_order_list";
	public static final String TABLE_IMAGE_PIXEL = "fe_image_pixel";
	public static final String TABLE_FILES = "fe_files";
	public static final String TABLE_TAG = "fe_tag";
	public static final String TABLE_SEQUENCE = "sqlite_sequence";
	public static final String TABLE_ORDER_COUNT = "fe_orders_count";
	public static final String[] TOC_COL = new String[]{"o_id","o_all","o_year","o_month","o_week","o_day","o_lastyear","o_lastmonth","o_lastweek","o_lastday"};
	public static final String TO_COL_ID = "o_id";
	public static final String TO_COL_NAME = "o_name";
	public static final String TO_COL_COVER = "o_cover";
	public static final String TO_COL_TAGID = "o_tag_id";
	public static final int NUM_TO_COL_ID = 1;
	public static final int NUM_TO_COL_NAME = 2;
	public static final int NUM_TO_COL_COVER = 3;
	public static final int NUM_TO_COL_TAGID = 4;
	public static final String TOL_COL_ID = "ol_id";
	public static final String TOL_COL_OID = "ol_order_id";
	public static final String TOL_COL_PATH = "ol_item_path";
	public static final int NUM_TOL_COL_ID = 1;
	public static final int NUM_TOL_COL_OID = 2;
	public static final int NUM_TOL_COL_PATH = 3;
	public static final String TIP_COL_ID = "fip_id";
	public static final String TIP_COL_NAME = "fip_name";
	public static final String TIP_COL_WIDTH = "fip_width";
	public static final String TIP_COL_HEIGHT = "fip_height";
	public static final String TIP_COL_OTHER = "fip_other";
	public static final String TF_COL_ID = "f_id";
	public static final String TF_COL_PATH = "f_path";
	public static final String TF_COL_TIME = "f_time";
	public static final String TF_COL_TIMETAG = "f_time_tag";
	public static final String TF_COL_WIDTH = "f_width";
	public static final String TF_COL_HEIGHT = "f_height";
	public static final String TF_COL_OTHER = "f_other";
	public static final int NUM_TIP_COL_ID = 1;
	public static final int NUM_TIP_COL_NAME = 2;
	public static final int NUM_TIP_COL_WIDTH = 3;
	public static final int NUM_TIP_COL_HEIGHT = 4;
	public static final int NUM_TIP_COL_OTHER = 5;
	public static final String TT_COL_ID = "t_id";
	public static final String TT_COL_TAG = "t_tag";
	public static final STag DEFAULT_TAG = new STag(6, "other");
	public static final int NUM_TT_COL_ID = 1;
	public static final int NUM_TT_COL_TAG = 2;
	public static final String TS_COL_NAME = "name";
	public static final String TS_COL_SEQ = "seq";
	public static final int NUM_TS_COL_NAME = 1;
	public static final int NUM_TS_COL_SEQ = 2;

	public static boolean prepare(Context context) {
		DB_PATH = context.getFilesDir().getPath() + "/" + "fileencryption.db";
		if (!new File(DB_PATH).exists()) {
			try {
				InputStream in = context.getAssets().open("fileencryption.db");
				FileOutputStream out = new FileOutputStream(DB_PATH);
				byte[] buffer = new byte[1024];
				int byteread = 0;
				while ((byteread = in.read(buffer)) != -1) {
					out.write(buffer, 0, byteread);
				}
				in.close();
				out.close();
			} catch (IOException e) {
				e.printStackTrace();
				return false;
			}
		}
		return true;
	}

	public static boolean export(Context context) {
		DB_PATH = context.getFilesDir().getPath() + "/" + "fileencryption.db";
		if (new File(DB_PATH).exists()) {
			try {
				Calendar calendar = Calendar.getInstance();
				StringBuffer targetPath = new StringBuffer(Configuration.APP_DIR_DB_HISTORY);
				targetPath.append("/");
				targetPath.append(calendar.get(Calendar.YEAR)).append("_");
				targetPath.append(calendar.get(Calendar.MONTH) + 1).append("_");
				targetPath.append(calendar.get(Calendar.DAY_OF_MONTH)).append("_");
				targetPath.append(calendar.get(Calendar.HOUR)).append("_");
				targetPath.append(calendar.get(Calendar.MINUTE)).append("_");
				targetPath.append(calendar.get(Calendar.SECOND)).append(".db");

				InputStream in = new FileInputStream(DB_PATH);
				FileOutputStream out = new FileOutputStream(targetPath.toString());
				byte[] buffer = new byte[1024];
				int byteread = 0;
				while ((byteread = in.read(buffer)) != -1) {
					out.write(buffer, 0, byteread);
				}
				in.close();
				out.close();
			} catch (IOException e) {
				e.printStackTrace();
				return false;
			}
		}
		return true;
	}

	/**
	 *
	 * @param context
	 */
	public static boolean replaceDatabase(Context context, String target) {

		if (target == null || !new File(target).exists()) {
			return false;
		}
		//先检查是否存在，存在则删除
		DB_PATH = context.getFilesDir().getPath() + "/" + "fileencryption.db";
		File defaultDb = new File(DB_PATH);
		if (defaultDb.exists()) {
			defaultDb.delete();
		}
		try {
			InputStream in = new FileInputStream(target);
			File file = new File(DB_PATH);
			OutputStream fileOut = new FileOutputStream(file);
			byte[] buffer = new byte[1024];
			int length;
			while ((length = in.read(buffer))>0){
				fileOut.write(buffer, 0, length);
			}

			fileOut.flush();
			fileOut.close();
			in.close();
		} catch (IOException e) {
			e.printStackTrace();
			return false;
		}
		return true;
	}
}
