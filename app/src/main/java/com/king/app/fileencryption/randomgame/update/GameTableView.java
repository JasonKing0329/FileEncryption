package com.king.app.fileencryption.randomgame.update;

import com.king.app.fileencryption.Application;
import com.king.app.fileencryption.R;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.drawable.BitmapDrawable;
import android.util.AttributeSet;
import android.view.Gravity;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.ImageView;
import android.widget.TableLayout;
import android.widget.TableRow;
import android.widget.TextView;

public class GameTableView extends TableLayout implements OnClickListener {

	private TableData[][] tableDatas;
	private OnGameTableActionListener actionListener;
	
	private int cellTextColor;
	private int cellTextSize;
	private int textZoom;
	private int cellColumn;
	private int cellRow;
	private ImageView lastSelectedHead;
	
	private int mWidth, mHeight;
	
	private TableData highLightCell;
	
	public GameTableView(Context context) {
		super(context);
		init();
	}

	public GameTableView(Context context, AttributeSet attrs) {
		super(context, attrs);
		init();
	}

	private void init() {
		cellTextColor = getResources().getColor(R.color.white);
		textZoom = getResources().getInteger(R.integer.rgame_table_text_zoom);
	}

	public void setOnTableActionListener (OnGameTableActionListener listener) {
		actionListener = listener;
	}
	
	public void setTableSize(int width, int height) {
		mWidth = width;
		mHeight = height;
	}
	
	public void create(int row, int col) {
		
		cellRow = row;
		cellColumn = col;
		tableDatas = new TableData[row + 2][col + 2];
		
		mWidth = mWidth < mHeight ? mWidth:mHeight;
		int cellWidth = mWidth/(row + 2);//consider head and count
		cellTextSize = cellWidth / textZoom;
		
		TableRow tableRow = null;
		LayoutParams rowParams = new LayoutParams(LayoutParams.WRAP_CONTENT, LayoutParams.WRAP_CONTENT);
		TableRow.LayoutParams colParams = new TableRow.LayoutParams(cellWidth, cellWidth);
		for (int i = 0; i < row + 2; i ++) {
			tableRow = new TableRow(getContext());
			tableRow.setLayoutParams(rowParams);
			addView(tableRow);
			
			if (i == 0) {//head
				for (int j = 0; j < col + 2; j ++) {
					ImageView view = new ImageView(getContext());
					view.setLayoutParams(colParams);
					if (j != 0 && j != col + 1) {
						view.setBackgroundResource(R.drawable.draws_orange_bk_normal);
					}
					tableRow.addView(view);
					
					TableData data = new TableData();
					data.setView(view);
					data.setRow(i);
					data.setCol(j);
					view.setTag(data);
					tableDatas[i][j] = data;
					view.setOnClickListener(this);
				}
			}
			else {
				for (int j = 0; j < col + 2; j ++) {
					if (j == 0) {//vertical head
						ImageView view = new ImageView(getContext());
						view.setLayoutParams(colParams);
						if (i != row + 1) {
							view.setBackgroundResource(R.drawable.draws_orange_bk_normal);
						}
						tableRow.addView(view);
						TableData data = new TableData();
						data.setView(view);
						data.setRow(i);
						data.setCol(j);
						view.setTag(data);
						tableDatas[i][j] = data;
						view.setOnClickListener(this);
					}
					else {
						TextView view =  new TextView(getContext());
						view.setLayoutParams(colParams);
						setCellStyle(view);
						if (i == row + 1 && j == col + 1) {
						}
						else {
							if (Application.isLollipop()) {
								view.setBackgroundResource(R.drawable.selector_draws_bk_l);
							}
							else {
								view.setBackgroundResource(R.drawable.selector_draws_blue_bk);
							}
						}
						tableRow.addView(view);
						TableData data = new TableData();
						data.setView(view);
						data.setRow(i);
						data.setCol(j);
						view.setTag(data);
						tableDatas[i][j] = data;
						view.setOnClickListener(this);
					}
				}
			}
		}
	}

	private void setCellStyle(TextView view) {
		view.setFocusable(true);
		view.setClickable(true);
		view.setTextColor(cellTextColor);
		view.setTextSize(cellTextSize);
		view.setGravity(Gravity.CENTER);
	}

	@Override
	public void onClick(View view) {
		TableData tableData = (TableData) view.getTag();
		if (isSpaceCell(tableData.getRow(), tableData.getCol())) {
			return;
		}
		if (tableData.getView() instanceof ImageView) {
			if (lastSelectedHead != null) {
				lastSelectedHead.setImageDrawable(null);
			}
			lastSelectedHead = (ImageView) view;
			if (Application.isLollipop()) {
				lastSelectedHead.setImageResource(R.drawable.gallery_border_choose_l);
			}
			else {
				lastSelectedHead.setImageResource(R.drawable.gallery_border_choose);
			}
			
			if (actionListener != null) {
				actionListener.onHeadClick(tableData);
			}
		}
		else {
			if (actionListener != null) {
				if (isCountView(tableData.getRow(), tableData.getCol())) {
					actionListener.onCountClick(tableData);
				}
				else {
					actionListener.onCellClick(tableData);
				}
			}
		}
	}

	private boolean isSpaceCell(int row, int col) {
		if (row == 0 && col == 0) {
			return true;
		}
		if (row == tableDatas.length - 1 && col == tableDatas[0].length - 1) {
			return true;
		}
		if (row == tableDatas.length - 1 && col == 0) {
			return true;
		}
		if (row == 0 && col == tableDatas[0].length - 1) {
			return true;
		}
		return false;
	}

	private boolean isCountView(int row, int col) {

		if (row == tableDatas.length - 1 || col == tableDatas[0].length - 1) {
			return true;
		}
		return false;
	}

	public void updateHead(String path, Bitmap bitmap) {
		if (lastSelectedHead != null) {
			TableData data = (TableData) lastSelectedHead.getTag();
			data.setImagePath(path);
			if (bitmap == null) {
				((ImageView) data.getView()).setBackgroundResource(R.drawable.draws_orange_bk_normal);
			}
			else {
				((ImageView) data.getView()).setBackground(new BitmapDrawable(getResources(), bitmap));
			}
			data.setBitmap(bitmap);
		}
	}
	
	public void notifyCellDataChanged(int row, int col) {
		float sum = 0;
		for (int i = 1; i <= cellColumn; i ++) {
			sum += tableDatas[row][i].getScore();
		}
		tableDatas[row][cellColumn + 1].setScore(sum);
		((TextView) tableDatas[row][cellColumn + 1].getView()).setText(String.format("%.1f", sum));
		
		sum = 0;
		for (int i = 1; i <= cellRow; i ++) {
			sum += tableDatas[i][col].getScore();
		}
		tableDatas[cellRow + 1][col].setScore(sum);
		((TextView) tableDatas[cellRow + 1][col].getView()).setText(String.format("%.1f", sum));
	}

	public TableData[][] getTableDatas() {
		return tableDatas;
	}

	public void setTableDatas(TableData[][] tableDatas) {
		this.tableDatas = tableDatas;
	}

	/**
	 * 
	 * @param tableDatas
	 */
	public void createFrom(TableData[][] tableDatas) {
		this.tableDatas = tableDatas;
		removeAllViews();

		int row = tableDatas.length;
		int col = tableDatas[0].length;
		cellRow = row - 2;
		cellColumn = col - 2;
		
		mWidth = mWidth < mHeight ? mWidth:mHeight;
		int cellWidth = mWidth/row;//consider head and count
		cellTextSize = cellWidth / textZoom;
		
		TableRow tableRow = null;
		LayoutParams rowParams = new LayoutParams(LayoutParams.WRAP_CONTENT, LayoutParams.WRAP_CONTENT);
		TableRow.LayoutParams colParams = new TableRow.LayoutParams(cellWidth, cellWidth);
		for (int i = 0; i < row; i ++) {
			tableRow = new TableRow(getContext());
			tableRow.setLayoutParams(rowParams);
			addView(tableRow);
			
			if (i == 0) {//head
				for (int j = 0; j < col; j ++) {
					ImageView view = new ImageView(getContext());
					view.setLayoutParams(colParams);
					if (j != 0 && j != col - 1) {
						view.setBackgroundResource(R.drawable.draws_orange_bk_normal);
					}
					tableRow.addView(view);
					
					TableData data = tableDatas[i][j];
					data.setView(view);
					view.setTag(data);
					view.setOnClickListener(this);
					if (data.getBitmap() != null) {
						view.setBackground(new BitmapDrawable(getResources(), data.getBitmap()));
					}
				}
			}
			else {
				for (int j = 0; j < col; j ++) {
					if (j == 0) {//vertical head
						ImageView view = new ImageView(getContext());
						view.setLayoutParams(colParams);
						if (i != row - 1) {
							view.setBackgroundResource(R.drawable.draws_orange_bk_normal);
						}
						tableRow.addView(view);
						
						TableData data = tableDatas[i][j];
						data.setView(view);
						view.setTag(data);
						view.setOnClickListener(this);
						if (data.getBitmap() != null) {
							view.setBackground(new BitmapDrawable(getResources(), data.getBitmap()));
						}
					}
					else {
						TextView view =  new TextView(getContext());
						view.setLayoutParams(colParams);
						setCellStyle(view);
						if (i == row - 1 && j == col - 1) {
						}
						else {
							if (Application.isLollipop()) {
								view.setBackgroundResource(R.drawable.selector_draws_bk_l);
							}
							else {
								view.setBackgroundResource(R.drawable.selector_draws_blue_bk);
							}
						}
						tableRow.addView(view);
						
						TableData data = tableDatas[i][j];
						data.setView(view);
						view.setTag(data);
						view.setOnClickListener(this);
						if (data.getScore() != 0.0f) {
							if (isCountView(i, j)) {
								view.setText(String.format("%.1f", data.getScore()));
							}
							else {
								view.setText("" + data.getScore());
							}
						}
					}
				}
			}
		}
	}

	public void setCellHightLight(int row, int col) {
		View view = tableDatas[row][col].getView();
		if (highLightCell != null) {
			if (Application.isLollipop()) {
				highLightCell.getView().setBackgroundResource(R.drawable.selector_draws_bk_l);
			}
			else {
				highLightCell.getView().setBackgroundResource(R.drawable.selector_draws_blue_bk);
			}
		}
		highLightCell = tableDatas[row][col];
		view.setBackgroundResource(R.drawable.selector_draws_red_bk);
	}
}
