package com.king.app.fileencryption.sorder.view;

import java.util.ArrayList;
import java.util.List;

import com.king.app.fileencryption.Application;
import com.king.app.fileencryption.R;
import com.king.app.fileencryption.controller.ThemeManager;
import com.king.app.fileencryption.publicview.CustomDialog;
import com.king.app.fileencryption.res.AppResManager;
import com.king.app.fileencryption.res.AppResProvider;
import com.king.app.fileencryption.res.ColorRes;
import com.king.app.fileencryption.res.JResource;
import com.king.app.fileencryption.sorder.controller.SOrderPictureBridge;
import com.king.app.fileencryption.sorder.entity.SOrder;
import com.king.app.fileencryption.sorder.entity.STag;
import com.king.lib.colorpicker.ColorPicker;
import com.king.lib.colorpicker.ColorPicker.OnColorPickerListener;
import com.king.lib.colorpicker.ColorPickerSelectionData;

import android.content.Context;
import android.graphics.drawable.GradientDrawable;
import android.graphics.drawable.StateListDrawable;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.CheckBox;
import android.widget.CompoundButton;
import android.widget.CompoundButton.OnCheckedChangeListener;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.Spinner;
import android.widget.TextView;

public class SOrderCreaterUpdate extends CustomDialog implements
		View.OnClickListener, OnColorPickerListener {

	private TextView noTagView;
	private TextView tagTextView, orderTextView;
	private EditText orderName, tagNameEdit;
	private ImageView addTagsButton, cancelAddButton;
	private ImageView colorButton;
	private Spinner tagSpinner;
	private CheckBox onlyAddTag;
	private List<STag> tagList;
	private SOrderPictureBridge bridge;

	private ColorPicker colorPicker;
	private int themeBasicColor;

	public SOrderCreaterUpdate(Context context, OnCustomDialogActionListener listener) {
		super(context, listener);

		themeBasicColor = new ThemeManager(context).getBasicColorResId();
		this.context = context;
		setTitle(R.string.sorder_create_order);
		requestSaveAction(true);
		requestCancelAction(true);
		//FIXME 需要考虑转屏
		//setHeight(context.getResources().getDimensionPixelSize(R.dimen.dialog_loadfrom_height));

		bridge = SOrderPictureBridge.getInstance(context);

		loadTagList();
		if (tagList == null || tagList.size() == 0) {
			tagSpinner.setVisibility(View.GONE);
			noTagView.setVisibility(View.VISIBLE);
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
			ArrayAdapter<String> adapter = new CustomArrayAdapter<String>(context
					, spinnerList);
			tagSpinner.setAdapter(adapter);
			tagSpinner.setSelection(index);

		}

		onlyAddTag.setOnCheckedChangeListener(new OnCheckedChangeListener() {

			@Override
			public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
				if (isChecked) {
					orderName.setEnabled(false);
					onClick(addTagsButton);
					cancelAddButton.setVisibility(View.GONE);
				}
				else {
					orderName.setEnabled(true);
					onClick(cancelAddButton);
				}
			}
		});
	}

	private void loadTagList() {
		tagList = bridge.loadTagList();
	}

	@Override
	public void onClick(View view) {
		if (view == addTagsButton) {
			tagSpinner.setVisibility(View.GONE);
			addTagsButton.setVisibility(View.GONE);
			noTagView.setVisibility(View.GONE);
			tagNameEdit.setVisibility(View.VISIBLE);
			cancelAddButton.setVisibility(View.VISIBLE);
		}
		else if (view == cancelAddButton) {
			tagNameEdit.setVisibility(View.GONE);
			cancelAddButton.setVisibility(View.GONE);
			tagSpinner.setVisibility(View.VISIBLE);
			addTagsButton.setVisibility(View.VISIBLE);
		}
		else if (view == cancelIcon) {
			actionListener.onCancel();
		}
		else if (view == saveIcon) {
			String name = orderName.getText().toString();
			String tName = null;
			if (tagSpinner.getVisibility() == View.VISIBLE) {
				tName = tagList.get(tagSpinner.getSelectedItemPosition()).getName();
			}
			else {
				tName = tagNameEdit.getText().toString();
			}
			if (tName == null || tName.trim().length() == 0 && tagNameEdit.getVisibility() == View.VISIBLE) {
				tagNameEdit.setError(context.getResources().getString(R.string.input_no_null));
				return;
			}
			if (onlyAddTag.isChecked()) {
				if (bridge.queryTag(tName) != null) {
					tagNameEdit.setError(context.getResources().getString(R.string.sorder_tag_already_exist));
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
						if (isok) {
							actionListener.onSave(order);
						}
						else {
							actionListener.onSave(null);
						}
						dismiss();
					}
				}
				else {
					orderName.setError(context.getResources().getString(R.string.input_no_null));
				}
			}
		}
		else if (view == colorButton) {
			if (colorPicker == null) {
				colorPicker = new ColorPicker(context, this);
				colorPicker.setResourceProvider(new AppResProvider(context));
			}
			colorPicker.setSelectionData(new AppResManager().getSorderCreaterList(context));
			colorPicker.show();
		}
		super.onClick(view);
	}

	@Override
	protected View getCustomView() {
		View view = LayoutInflater.from(getContext()).inflate(R.layout.create_order_update, null);
		orderName = (EditText) view.findViewById(R.id.create_order_name);
		tagNameEdit = (EditText) view.findViewById(R.id.create_order_tag_edit);
		tagSpinner = (Spinner) view.findViewById(R.id.create_order_tag);
		noTagView = (TextView) view.findViewById(R.id.create_order_notag);
		orderTextView = (TextView) view.findViewById(R.id.create_order_text2);
		tagTextView = (TextView) view.findViewById(R.id.create_order_text1);
		onlyAddTag = (CheckBox) view.findViewById(R.id.create_order_only_tag);
		addTagsButton = (ImageView) view.findViewById(R.id.create_order_tag_add);
		cancelAddButton = (ImageView) view.findViewById(R.id.create_order_tag_cancel);
		addTagsButton.setOnClickListener(this);
		cancelAddButton.setOnClickListener(this);
		tagSpinner.setDropDownVerticalOffset(getContext().getResources().getDimensionPixelOffset(R.dimen.spinner_dd_offset_y));

		if (Application.isLollipop()) {
			orderName.setBackgroundResource(R.drawable.custom_dialog_search_bk_l);
			tagNameEdit.setBackgroundResource(R.drawable.custom_dialog_search_bk_l);
		}
		return view;
	}

	@Override
	protected View getCustomToolbar() {

		View view = LayoutInflater.from(getContext()).inflate(R.layout.sorder_creater_actionbar, null);
		colorButton = (ImageView) view.findViewById(R.id.order_creater_color);
		colorButton.setOnClickListener(this);
		return view;
	}

	private class CustomArrayAdapter<T> extends ArrayAdapter<T>
	{
		public CustomArrayAdapter(Context ctx, List<T> objects)
		{
			super(ctx, R.layout.spinner_dropdown_text, objects);
		}

		//其它构造函数

		@Override
		public View getDropDownView(int position, View convertView, ViewGroup parent)
		{
			View view = super.getView(position, convertView, parent);

//	            TextView text = (TextView)view.findViewById(android.R.id.text1);
//	            text.setTextColor(getContext().getResources().getColor(R.color.black));//choose your color :) 

			return view;

		}

	}

	@Override
	public void onColorChanged(String key, int newColor) {
		if (key.equals(ColorRes.SORDER_CREATER_BK)) {
			setBackgroundColor(newColor);
		}
		else if (key.equals(ColorRes.SORDER_CREATER_TITLE)) {
			setTitleColor(newColor);
		}
		else if (key.equals(ColorRes.SORDER_CREATER_ICON_BK)) {
			updateIconBk(colorButton, newColor);
			updateToobarIconBk(newColor);
		}
		else if (key.equals(ColorRes.SORDER_CREATER_TITLE_BK)) {
			updateTitleBk(newColor);
		}
		else if (key.equals(ColorRes.SORDER_CREATER_TITLE_BOARDER)) {
			updateTitleBorderColor(newColor);
		}
		else if (key.equals(ColorRes.SORDER_CREATER_DIVIDER)) {
			setDividerColor(newColor);
		}
		else if (key.equals(ColorRes.SORDER_CREATER_TEXT)) {
			tagTextView.setTextColor(newColor);
			orderTextView.setTextColor(newColor);
			onlyAddTag.setTextColor(newColor);
			noTagView.setTextColor(newColor);
		}
	}

	private void updateIconBk(ImageView view, int color) {
		StateListDrawable stateDrawable = (StateListDrawable) view.getBackground();
		GradientDrawable drawable = (GradientDrawable) stateDrawable.getCurrent();
		drawable.setColor(color);
	}

	@Override
	public void onColorSelected(int color) {
		// TODO Auto-generated method stub

	}

	@Override
	public void onColorSelected(List<ColorPickerSelectionData> list) {
		for (ColorPickerSelectionData data:list) {
			JResource.updateColor(data.getKey(), data.getColor());
		}
		JResource.saveColorUpdate(context);
	}

	@Override
	public void onColorCancleSelect() {
		resetColors();
	}

	@Override
	public void onApplyDefaultColors() {
		JResource.removeColor(ColorRes.SORDER_CREATER_BK);
		JResource.removeColor(ColorRes.SORDER_CREATER_TITLE);
		JResource.removeColor(ColorRes.SORDER_CHOOSER_TITLE_BK);
		JResource.removeColor(ColorRes.SORDER_CHOOSER_TITLE_BOARDER);
		JResource.removeColor(ColorRes.SORDER_CHOOSER_ICON_BK);
		JResource.removeColor(ColorRes.SORDER_CHOOSER_DIVIDER);
		JResource.removeColor(ColorRes.SORDER_CREATER_TEXT);
		JResource.saveColorUpdate(context);
		resetColors();
	}

	@Override
	public void show() {
		resetColors();
		super.show();
	}

	private void resetColors() {
		setBackgroundColor(JResource.getColor(context
				, ColorRes.SORDER_CREATER_BK, themeBasicColor));
		setTitleColor(JResource.getColor(context
				, ColorRes.SORDER_CREATER_TITLE, themeBasicColor));
		setDividerColor(JResource.getColor(context
				, ColorRes.SORDER_CREATER_DIVIDER
				, R.color.white));
		updateIconBk(colorButton, JResource.getColor(context
				, ColorRes.SORDER_CREATER_ICON_BK, themeBasicColor));
		updateToobarIconBk(JResource.getColor(context
				, ColorRes.SORDER_CREATER_ICON_BK, themeBasicColor));
		updateTitleBk(JResource.getColor(context
				, ColorRes.SORDER_CREATER_TITLE_BK
				, R.color.white));
		updateTitleBorderColor(JResource.getColor(context
				, ColorRes.SORDER_CREATER_TITLE_BOARDER, themeBasicColor));
		tagTextView.setTextColor(JResource.getColor(context
				, ColorRes.SORDER_CREATER_TEXT
				, R.color.white));
		orderTextView.setTextColor(JResource.getColor(context
				, ColorRes.SORDER_CREATER_TEXT
				, R.color.white));
		onlyAddTag.setTextColor(JResource.getColor(context
				, ColorRes.SORDER_CREATER_TEXT
				, R.color.white));
		noTagView.setTextColor(JResource.getColor(context
				, ColorRes.SORDER_CREATER_TEXT
				, R.color.white));
	}

}
