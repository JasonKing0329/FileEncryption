package com.king.app.fileencryption.sorder.view;

import java.util.ArrayList;
import java.util.List;

import com.king.app.fileencryption.R;
import com.king.app.fileencryption.sorder.controller.SOrderPictureBridge;
import com.king.app.fileencryption.sorder.entity.SOrder;
import com.king.app.fileencryption.sorder.entity.STag;

import android.app.Dialog;
import android.content.Context;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.EditText;
import android.widget.Spinner;
@Deprecated
public class SOrderCreater extends Dialog implements View.OnClickListener {

	private Button okButton, cancleButton;
	private EditText orderName, tagName;
	private Spinner tagSpinner;
	private Button addTagButton;
	private CheckBox onlyAddTag;
	private List<STag> tagList;
	private Context context;
	private SOrderPictureBridge bridge;
	private OnOrderCreateListener outListener;
	
	public interface OnOrderCreateListener {
		public void onOk(SOrder order);
		public void onCancel();
		public void onReceiveError(Object object);
	}
	
	public SOrderCreater(Context context, OnOrderCreateListener listener) {
		this(context);
		outListener = listener;
	}
	
	private SOrderCreater(Context context) {
		super(context);

		this.context = context;
		bridge = SOrderPictureBridge.getInstance(context);
		setTitle(R.string.sorder_create_order);
		setContentView(R.layout.create_order);
		okButton = (Button) findViewById(R.id.create_order_ok);
		cancleButton = (Button) findViewById(R.id.create_order_cancel);
		orderName = (EditText) findViewById(R.id.create_order_name);
		tagSpinner = (Spinner) findViewById(R.id.create_order_tag);
		loadTagList();
		if (tagList == null || tagList.size() == 0) {
			tagSpinner.setVisibility(View.GONE);
			findViewById(R.id.create_order_notag).setVisibility(View.VISIBLE);
		}
		else {
			List<String> spinnerList = new ArrayList<String>();
			
			int index = 0, i = 0;
			for (STag tag:tagList) {
				spinnerList.add(tag.getName());
				if (tag.getName().equals("person")) {
					index = i;
				}
				i ++;
			}
			ArrayAdapter<String> adapter = new ArrayAdapter<String>(context
					, android.R.layout.simple_dropdown_item_1line, spinnerList);
			tagSpinner.setAdapter(adapter);
			tagSpinner.setSelection(index);
			
		}
		addTagButton = (Button) findViewById(R.id.create_order_new_tag);
		tagName = (EditText) findViewById(R.id.create_order_new_tag_name);
		onlyAddTag = (CheckBox) findViewById(R.id.create_order_only_tag);
		addTagButton.setOnClickListener(this);
		okButton.setOnClickListener(this);
		cancleButton.setOnClickListener(this);
	}

	private void loadTagList() {
		tagList = bridge.loadTagList();
	}

	@Override
	public void onClick(View view) {
		if (view == addTagButton) {
			tagSpinner.setVisibility(View.GONE);
			tagName.setVisibility(View.VISIBLE);
		}
		else if (view == cancleButton) {
			if (outListener != null) {
				outListener.onCancel();
			}
			dismiss();
		}
		else if (view == okButton) {
			String name = orderName.getText().toString();
			String tName = null;
			if (tagSpinner.getVisibility() == View.VISIBLE) {
				tName = tagList.get(tagSpinner.getSelectedItemPosition()).getName();
			}
			else {
				tName = tagName.getText().toString();
			}
			if (tName == null || tName.trim().length() == 0 && tagName.getVisibility() == View.VISIBLE) {
				tagName.setError(context.getResources().getString(R.string.input_no_null));
				return;
			}
			if (onlyAddTag.isChecked()) {
				if (bridge.queryTag(tName) != null) {
					tagName.setError(context.getResources().getString(R.string.sorder_tag_already_exist));
				}
				else {
					bridge.addTag(tName);
					dismiss();
				}
			}
			else {
				if (name != null && name.length() > 0) {
					STag sTag = null;
					sTag = bridge.queryTag(tName);
					if (sTag == null) {
						sTag = bridge.addTag(tName);
					}

					if (bridge.isOrderExist(name)) {
						orderName.setError(context.getResources().getString(R.string.sorder_order_already_exist));
					}
					else {
						SOrder order = new SOrder();
						order.setName(name);
						order.setTag(sTag);
						boolean isok = bridge.addOrder(order);
						if (outListener != null) {
							if (isok) {
								outListener.onOk(order);
							}
							else {
								outListener.onReceiveError(null);
							}
						}
						dismiss();
					}
				}
				else {
					orderName.setError(context.getResources().getString(R.string.input_no_null));
				}
			}
		}
	}

}
