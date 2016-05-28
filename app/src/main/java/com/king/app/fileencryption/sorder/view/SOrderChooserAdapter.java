package com.king.app.fileencryption.sorder.view;

import java.util.List;

import com.king.app.fileencryption.R;
import com.king.app.fileencryption.res.ColorRes;
import com.king.app.fileencryption.res.JResource;
import com.king.app.fileencryption.sorder.entity.SOrder;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.View.OnClickListener;
import android.widget.BaseAdapter;
import android.widget.ImageView;
import android.widget.TextView;

public class SOrderChooserAdapter extends BaseAdapter {

	private List<SOrder> sorderList;
	private Context mContext;
	private int priorityNumber;
	private int priorityColor;
	private int normalColor;
	private boolean hasPriorityColor;
	private PreviewListener previewListener;
	
	private int selectedIndex = -1;
	private int itemSelectColor, itemNormalColor;
	
	public SOrderChooserAdapter(Context context, List<SOrder> list) {
		mContext = context;
		sorderList = list;
		resetColor();
		itemNormalColor = context.getResources().getColor(R.color.transparent);
		hasPriorityColor = true;
		previewListener = new PreviewListener();
	}
	
	public void resetColor() {
		priorityColor = JResource.getColor(mContext
				, ColorRes.SORDER_CHOOSER_LIST_TEXT_PRIORITY
				, R.color.sorder_chooser_text_priority);
		normalColor = JResource.getColor(mContext
				, ColorRes.SORDER_CHOOSER_LIST_TEXT
				, R.color.sorder_chooser_text);
		itemSelectColor = JResource.getColor(mContext
				, ColorRes.SORDER_CHOOSER_LIST_SELECTED
				, R.color.sorder_chooser_list_selected);
	}
	
	public void updateTextColor(int color) {
		normalColor = color;
		notifyDataSetChanged();
	}

	public void updatePriorityTextColor(int color) {
		priorityColor = color;
		notifyDataSetChanged();
	}

	public void updateListSelectedColor(int color) {
		itemSelectColor = color;
		notifyDataSetChanged();
	}
	public void updateSOrderList(List<SOrder> list) {
		sorderList = list;
	}
	
	public void setPriorityNumber(int number) {
		priorityNumber = number;
	}

	public void setSelectedIndex(int index) {
		selectedIndex = index;
	}
	
	@Override
	public int getCount() {
		return sorderList == null ? 0 : sorderList.size();
	}

	@Override
	public Object getItem(int position) {
		return sorderList == null ? null : sorderList.get(position);
	}

	@Override
	public long getItemId(int position) {
		return position;
	}

	@Override
	public View getView(int position, View convertView, ViewGroup parent) {

		ViewHolder holder = null;
		if (convertView == null) {
			holder = new ViewHolder();
			convertView = LayoutInflater.from(mContext)
					.inflate(R.layout.sorder_chooser_list_item, null);
			holder.textView = (TextView) convertView.findViewById(R.id.sorder_chooser_list_text);
			holder.imageView = (ImageView) convertView.findViewById(R.id.sorder_chooser_list_preview);
			
			convertView.setTag(holder);
		}
		else {
			holder = (ViewHolder) convertView.getTag();
		}
		SOrder order = sorderList.get(position);
		holder.textView.setText(order.getName() + "(" + order.getItemNumber() + ")");
		holder.imageView.setTag(position);
		holder.imageView.setOnClickListener(previewListener);
		if (hasPriorityColor) {
			if (position < priorityNumber) {
				holder.textView.setTextColor(priorityColor);
			}
			else {
				holder.textView.setTextColor(normalColor);
			}
		}
		else {
			holder.textView.setTextColor(normalColor);
		}
		
		if (position == selectedIndex) {
			convertView.setBackgroundColor(itemSelectColor);
		}
		else {
			convertView.setBackgroundColor(itemNormalColor);
		}
		return convertView;
	}

	public void hasPriorityColor(boolean hascolor) {
		hasPriorityColor = hascolor;
	}
	
	private class ViewHolder {
		TextView textView;
		ImageView imageView;
	}
	
	private class PreviewListener implements OnClickListener {

		@Override
		public void onClick(View view) {
			if (sorderList != null) {
				int index = (Integer) view.getTag();
				new PreviewDialog(mContext, sorderList.get(index)).show();
			}
		}
		
	}

}
