package com.king.app.fileencryption.randomgame.team;

import com.king.app.fileencryption.R;

import android.content.Context;
import android.graphics.Bitmap;
import android.util.AttributeSet;
import android.view.View;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ScrollView;
import android.widget.ImageView.ScaleType;

public class TeamGameTable extends LinearLayout {

	public interface OnTableListener {
		public void onClickTableTitle(int col);
		public void onClickTableData(int row, int col);
	}

	private LinearLayout titleLayout;
	private ImageView[] titleImageViews;

	private ScrollView cellScrollView;
	private LinearLayout cellLayout;
	private ImageView[][] cellImageViews;
	private String[] titleDatas;
	private String[][] cellDatas;

	private int dividerHeight;

	private int row, column, titleColumn;
	private OnTableListener onTableListener;

	public TeamGameTable(Context context, AttributeSet attrs) {
		super(context, attrs);

		setOrientation(LinearLayout.VERTICAL);
		dividerHeight = getResources().getDimensionPixelSize(R.dimen.game_team_divider);

		titleLayout = new LinearLayout(context);
		titleLayout.setLayoutParams(new LayoutParams(LayoutParams.MATCH_PARENT, LayoutParams.WRAP_CONTENT));
		addView(titleLayout);

		View divider = new View(context);
		LayoutParams params = new LayoutParams(LayoutParams.MATCH_PARENT, dividerHeight);
		divider.setLayoutParams(params);
		divider.setBackgroundResource(R.drawable.shape_team_game_divider);
		addView(divider);

		cellScrollView = new ScrollView(context);
		cellScrollView.setLayoutParams(new LayoutParams(LayoutParams.MATCH_PARENT, LayoutParams.MATCH_PARENT));
		addView(cellScrollView);
		cellLayout = new LinearLayout(context);
		cellLayout.setLayoutParams(new LayoutParams(LayoutParams.MATCH_PARENT, LayoutParams.WRAP_CONTENT));
		cellLayout.setOrientation(LinearLayout.VERTICAL);
		cellScrollView.addView(cellLayout);

	}

	public void setOnTableListener(OnTableListener listener) {
		onTableListener = listener;
	}

	public void setTitleColumn(int column) {
		titleColumn = column;
	}

	public void setDataColumn(int column) {
		this.column = column;
	}

	public void setDataRow(int row) {
		this.row = row;
	}

	public void build() {

		//build title
		titleLayout.removeAllViews();

		titleImageViews = new ImageView[titleColumn];
		titleDatas = new String[titleColumn];
		int width = getWidth() / titleColumn;
		LayoutParams params = new LayoutParams(width, width);

		for (int i = 0; i < titleColumn; i ++) {
			titleImageViews[i] = new ImageView(getContext());
			titleImageViews[i].setLayoutParams(params);
			titleImageViews[i].setBackgroundResource(R.drawable.selector_draws_orange_bk);
			titleImageViews[i].setOnClickListener(titleListener);
			titleImageViews[i].setTag(i);
			titleImageViews[i].setScaleType(ScaleType.FIT_XY);
			titleLayout.addView(titleImageViews[i]);
		}

		//build data
		cellLayout.removeAllViews();

		cellImageViews = new ImageView[row][];
		cellDatas = new String[row][];
		width = getWidth() / column;
		params = new LayoutParams(width, width);
		for (int i = 0; i < row; i ++) {
			LinearLayout rowLayout = new LinearLayout(getContext());
			rowLayout.setLayoutParams(new LayoutParams(LayoutParams.MATCH_PARENT, LayoutParams.WRAP_CONTENT));
			rowLayout.setOrientation(LinearLayout.HORIZONTAL);
			cellImageViews[i] = new ImageView[column];
			cellDatas[i] = new String[column];
			for (int j = 0; j < column; j ++) {
				cellImageViews[i][j] = new ImageView(getContext());
				cellImageViews[i][j].setLayoutParams(params);
				cellImageViews[i][j].setBackgroundResource(R.drawable.selector_draws_blue_bk);
				cellImageViews[i][j].setOnClickListener(dataListener);
				cellImageViews[i][j].setTag(i + "," + j);
				cellImageViews[i][j].setScaleType(ScaleType.FIT_XY);
				rowLayout.addView(cellImageViews[i][j]);
			}
			cellLayout.addView(rowLayout);
		}
	}

	private OnClickListener titleListener = new OnClickListener() {

		@Override
		public void onClick(View view) {
			int col = (Integer) view.getTag();
			if (onTableListener != null) {
				onTableListener.onClickTableTitle(col);
			}
		}
	};
	private OnClickListener dataListener = new OnClickListener() {

		@Override
		public void onClick(View view) {
			String tag = (String) view.getTag();
			String[] array = tag.split(",");
			int row = Integer.parseInt(array[0]);
			int col = Integer.parseInt(array[1]);
			if (onTableListener != null) {
				onTableListener.onClickTableData(row, col);
			}
		}
	};

	public int getCellWidth() {
		if (cellImageViews != null) {
			return cellImageViews[0][0].getWidth();
		}
		return 0;
	}

	public int getTitleWidth() {
		if (cellImageViews != null) {
			return titleImageViews[0].getWidth();
		}
		return 0;
	}

	/**
	 * 在当前列下填充第一个为null的单元格
	 * @param column
	 * @param path
	 * @param bitmap
	 */
	public void setNextRowCellDataAtColumn(int column, String path, Bitmap bitmap) {
		for (int i = 0; i < row; i ++) {
			if (cellDatas[i][column] == null) {
				cellDatas[i][column] = path;
				cellImageViews[i][column].setImageBitmap(bitmap);
				break;
			}
		}
	}

	/**
	 * 在标题栏填充第一个为null的单元格
	 * @param path
	 * @param bitmap
	 */
	public void setNextTitleData(String path, Bitmap bitmap) {
		for (int i = 0; i < titleColumn; i ++) {
			if (titleDatas[i] == null) {
				titleDatas[i] = path;
				titleImageViews[i].setImageBitmap(bitmap);
				break;
			}
		}
	}

	public void setTitleDataAtColumn(int column, String path, Bitmap bitmap) {
		titleDatas[column] = path;
		titleImageViews[column].setImageBitmap(bitmap);
	}

	public void setCellDataAt(int row, int col, String path, Bitmap bitmap) {
		cellDatas[row][col] = path;
		cellImageViews[row][col].setImageBitmap(bitmap);
	}

	public String[] getTitleDatas() {
		return titleDatas;
	}

	public String[][] getCellDatas() {
		return cellDatas;
	}

	public String getCellData(int row, int col) {
		return cellDatas[row][col];
	}

	public View getCellViewAt(int row, int col) {
		return cellImageViews[row][col];
	}

	public View getTitleViewAt(int col) {
		return titleImageViews[col];
	}

}
