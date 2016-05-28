package com.king.app.fileencryption.sorder.view;

import com.king.app.fileencryption.R;
import com.king.app.fileencryption.sorder.controller.PreviewController;
import com.king.app.fileencryption.sorder.controller.SOrderPictureBridge;
import com.king.app.fileencryption.sorder.entity.SOrder;

import android.app.Dialog;
import android.content.Context;
import android.graphics.Bitmap;
import android.os.Handler.Callback;
import android.os.Message;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AbsListView;
import android.widget.BaseAdapter;
import android.widget.GridView;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ImageView.ScaleType;

public class PreviewDialog extends Dialog implements android.view.View.OnClickListener, Callback {

	private ImageView closeButton;
	private GridView gridView;
	private SOrder previewOrder;
	private GridAdapter gridAdapter;
	
	private PreviewController previewController;
	
	public PreviewDialog(Context context, SOrder order) {
		super(context, R.style.TransparentDialog);
		getWindow().setWindowAnimations(R.style.PreviewDialogAnim);
		previewOrder = order;
		setContentView(R.layout.sorder_preview);
		
		closeButton = (ImageView) findViewById(R.id.preview_close);
		gridView = (GridView) findViewById(R.id.preview_grid);
		closeButton.setOnClickListener(this);
		
		previewController = new PreviewController(context, this);
		initGridView();
	}

	private void initGridView() {
		SOrderPictureBridge.getInstance(getContext()).getOrderItemList(previewOrder);
		computeGridParams();
		gridAdapter = new GridAdapter();
		gridView.setAdapter(gridAdapter);
		
		previewController.loadImages(previewOrder.getImgPathList());
	}

	private void computeGridParams() {
		int maxRow = getContext().getResources().getInteger(R.integer.preview_row);
		int maxCol = getContext().getResources().getInteger(R.integer.preview_column);
		int row = 0, col = maxCol;
		if (previewOrder.getImgPathList() != null) {
			int number = previewOrder.getImgPathList().size();
			if (number > 0) {
				row = (number - 1) / maxCol + 1;
				if (row > maxRow) {
					row = maxRow;
				}
			}
			if (row == 1) {
				col = number;
			}
		}
		int itemWidth = getContext().getResources().getDimensionPixelSize(R.dimen.sorder_preview_width);
		int space = getContext().getResources().getDimensionPixelSize(R.dimen.sorder_preview_space);
		int width = col * itemWidth + (col - 1) * space;
		int height = row * itemWidth + (row - 1) * space;
		LinearLayout.LayoutParams params = new LinearLayout.LayoutParams(width, height);
		gridView.setColumnWidth(itemWidth);
		gridView.setNumColumns(col);
		gridView.setLayoutParams(params);
	}

	@Override
	public void onClick(View view) {
		if (view == closeButton) {
			dismiss();
			recycle();
		}
	}

	private void recycle() {
		previewController.recycleAll();
	}

	@Override
	public void dismiss() {
		recycle();
		super.dismiss();
	}

	private class GridAdapter extends BaseAdapter {

		private AbsListView.LayoutParams params;
		
		public GridAdapter () {
			int itemWidth = getContext().getResources().getDimensionPixelSize(R.dimen.sorder_preview_width);
			params = new AbsListView.LayoutParams(itemWidth, itemWidth);
			
		}
		@Override
		public int getCount() {
			return previewOrder.getImgPathList() == null ? 0 : previewOrder.getImgPathList().size();
		}

		@Override
		public Object getItem(int pos) {
			return previewOrder.getImgPathList() == null ? 0 : previewOrder.getImgPathList().get(pos);
		}

		@Override
		public long getItemId(int pos) {
			return pos;
		}

		@Override
		public View getView(int position, View convertView, ViewGroup group) {

			ImageView imageView = null;
			if (convertView == null) {
				imageView = new ImageView(getContext());
				imageView.setLayoutParams(params);
				imageView.setScaleType(ScaleType.FIT_XY);
				previewController.setImageView(position, imageView);
				convertView = imageView;
				convertView.setTag(imageView);
			}
			else {
				imageView = (ImageView) convertView.getTag();
			}
			
			Bitmap bitmap = previewController.getBitmap(position);
			if (bitmap == null) {
				imageView.setImageResource(R.drawable.icon_loading);
			}
			else {
				imageView.setImageBitmap(bitmap);
			}
			return convertView;
		}
		
	}

	@Override
	public boolean handleMessage(Message arg0) {
		gridAdapter.notifyDataSetChanged();
		return false;
	}
}
