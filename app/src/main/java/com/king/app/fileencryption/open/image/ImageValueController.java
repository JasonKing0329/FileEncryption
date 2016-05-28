package com.king.app.fileencryption.open.image;

import java.sql.Connection;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;

import com.king.app.fileencryption.data.DBInfor;
import com.king.app.fileencryption.filemanager.view.FilePageItem;
import com.king.app.fileencryption.sorder.db.SqlOperator;


public class ImageValueController implements ImageValueListener {

	@Override
	public void onCreate(String path, int width, int height) {
		
		String name = ImageValue.generateName(path);
		
		ImageValue value = queryImagePixel(name);
		if (value == null) {
			value = new ImageValue();
			value.setPath(path);
			value.setName(name);
			value.setWidth(width);
			value.setHeight(height);
			addImagePixel(value);
		}
	}

	public ImageValue queryImagePixel(String key) {
		ImageValue value = null;
		SqlOperator operator = new SqlOperator();
		Connection connection = operator.connect(DBInfor.DB_PATH);
		
		value = operator.queryImageValue(key, connection);

		try {
			connection.close();
		} catch (SQLException e) {
			e.printStackTrace();
		}
		return value;
	}

	public void addImagePixel(ImageValue value) {
		SqlOperator operator = new SqlOperator();
		Connection connection = operator.connect(DBInfor.DB_PATH);
		
		operator.insertImagePixel(value, connection);

		try {
			connection.close();
		} catch (SQLException e) {
			e.printStackTrace();
		}
	}

	/**
	 * 
	 * @param pathList
	 * @param values should be not null but size is 0.
	 */
	public void queryImagePixelFrom(List<String> pathList, List<ImageValue> values) {
		if (pathList != null && values != null) {

			List<String> keyList = new ArrayList<String>();
			for (String path:pathList) {
				values.add(null);
				keyList.add(ImageValue.generateName(path));
			}
			
			queryImagePixel(keyList, values);
			
			for (int i = 0; i < values.size(); i ++) {
				if (values.get(i) == null) {
					ImageValue value = new ImageValue();
					value.setPath(pathList.get(i));
					values.set(i, value);
				}
				else {
					values.get(i).setPath(pathList.get(i));
				}
			}
		}
	}
	
	private void queryImagePixel(List<String> keyList, List<ImageValue> values) {
		if (keyList != null && values != null) {
			SqlOperator operator = new SqlOperator();
			Connection connection = operator.connect(DBInfor.DB_PATH);
			
			operator.queryImageValues(keyList, values, connection);
			
			try {
				connection.close();
			} catch (SQLException e) {
				e.printStackTrace();
			}

		}
	}
	
	public void queryImagePixelFrom(List<FilePageItem> list) {

		if (list != null) {
			List<ImageValue> values = new ArrayList<ImageValue>();
			List<String> keyList = new ArrayList<String>();
			for (FilePageItem item:list) {
				values.add(null);
				keyList.add(ImageValue.generateName(item.getFile().getPath()));
			}
			
			queryImagePixel(keyList, values);
			
			for (int i = 0; i < list.size(); i ++) {
				if (values.get(i) != null) {
					values.get(i).setPath(list.get(i).getFile().getPath());
					list.get(i).setImageValue(values.get(i));
				}
			}
		}
	}

}
