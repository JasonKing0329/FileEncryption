package com.king.app.fileencryption.res;

import java.util.ArrayList;
import java.util.List;

import android.content.Context;

import com.king.app.fileencryption.R;
import com.king.app.fileencryption.controller.ThemeManager;
import com.king.lib.colorpicker.ColorPickerSelectionData;

public class AppResManager {

	public List<ColorPickerSelectionData> getSorderChooserList(Context context) {
		List<ColorPickerSelectionData> list = new ArrayList<ColorPickerSelectionData>();
		ColorPickerSelectionData data = new ColorPickerSelectionData();
		int colorId = new ThemeManager(context).getBasicColorResId();
		data.setKey(ColorRes.SORDER_CHOOSER_BK);
		data.setName("背景");
		data.setColor(JResource.getColor(context, ColorRes.SORDER_CHOOSER_BK, colorId));
		list.add(data);

		data = new ColorPickerSelectionData();
		data.setKey(ColorRes.SORDER_CHOOSER_TITLE);
		data.setName("标题");
		data.setColor(JResource.getColor(context, ColorRes.SORDER_CHOOSER_TITLE, colorId));
		list.add(data);

		data = new ColorPickerSelectionData();
		data.setKey(ColorRes.SORDER_CHOOSER_TITLE_BK);
		data.setName("标题背景");
		data.setColor(JResource.getColor(context, ColorRes.SORDER_CHOOSER_TITLE_BK, R.color.sorder_chooser_title_bk));
		list.add(data);

		// 取消边框，弃用
//		data = new ColorPickerSelectionData();
//		data.setKey(ColorRes.SORDER_CHOOSER_TITLE_BOARDER);
//		data.setName("标题边框");
//		data.setColor(JResource.getColor(context, ColorRes.SORDER_CHOOSER_TITLE_BOARDER, colorId));
//		list.add(data);

		data = new ColorPickerSelectionData();
		data.setKey(ColorRes.SORDER_CHOOSER_ICON_BK);
		data.setName("工具栏图标背景");
		data.setColor(JResource.getColor(context, ColorRes.SORDER_CHOOSER_ICON_BK, colorId));
		list.add(data);

		data = new ColorPickerSelectionData();
		data.setKey(ColorRes.SORDER_CHOOSER_DIVIDER);
		data.setName("分界线");
		data.setColor(JResource.getColor(context, ColorRes.SORDER_CHOOSER_DIVIDER, R.color.white));
		list.add(data);

		data = new ColorPickerSelectionData();
		data.setKey(ColorRes.SORDER_CHOOSER_LIST_TEXT);
		data.setName("普通文字");
		data.setColor(JResource.getColor(context, ColorRes.SORDER_CHOOSER_LIST_TEXT, R.color.sorder_chooser_text));
		list.add(data);

		data = new ColorPickerSelectionData();
		data.setKey(ColorRes.SORDER_CHOOSER_LIST_TEXT_PRIORITY);
		data.setName("优先文字");
		data.setColor(JResource.getColor(context, ColorRes.SORDER_CHOOSER_LIST_TEXT_PRIORITY, R.color.sorder_chooser_text_priority));
		list.add(data);

		data = new ColorPickerSelectionData();
		data.setKey(ColorRes.SORDER_CHOOSER_LIST_SELECTED);
		data.setName("列表项选中状态");
		data.setColor(JResource.getColor(context, ColorRes.SORDER_CHOOSER_LIST_SELECTED, R.color.sorder_chooser_list_selected));
		list.add(data);
		return list;
	}

	public List<ColorPickerSelectionData> getSorderCreaterList(Context context) {
		List<ColorPickerSelectionData> list = new ArrayList<ColorPickerSelectionData>();
		ColorPickerSelectionData data = new ColorPickerSelectionData();
		int colorId = new ThemeManager(context).getBasicColorResId();
		data.setKey(ColorRes.SORDER_CREATER_BK);
		data.setName("背景");
		data.setColor(JResource.getColor(context, ColorRes.SORDER_CREATER_BK, colorId));
		list.add(data);

		data = new ColorPickerSelectionData();
		data.setKey(ColorRes.SORDER_CREATER_TITLE);
		data.setName("标题");
		data.setColor(JResource.getColor(context, ColorRes.SORDER_CREATER_TITLE, colorId));
		list.add(data);

		data = new ColorPickerSelectionData();
		data.setKey(ColorRes.SORDER_CREATER_TITLE_BK);
		data.setName("标题背景");
		data.setColor(JResource.getColor(context, ColorRes.SORDER_CREATER_TITLE_BK, R.color.sorder_chooser_title_bk));
		list.add(data);

		data = new ColorPickerSelectionData();
		data.setKey(ColorRes.SORDER_CREATER_ICON_BK);
		data.setName("工具栏图标背景");
		data.setColor(JResource.getColor(context, ColorRes.SORDER_CREATER_ICON_BK, colorId));
		list.add(data);

		data = new ColorPickerSelectionData();
		data.setKey(ColorRes.SORDER_CREATER_DIVIDER);
		data.setName("分界线");
		data.setColor(JResource.getColor(context, ColorRes.SORDER_CREATER_DIVIDER, R.color.white));
		list.add(data);

		data = new ColorPickerSelectionData();
		data.setKey(ColorRes.SORDER_CREATER_TEXT);
		data.setName("普通文字");
		data.setColor(JResource.getColor(context, ColorRes.SORDER_CREATER_TEXT, R.color.white));
		list.add(data);
		return list;
	}

	public List<ColorPickerSelectionData> getTabActionbarList(Context context) {
		List<ColorPickerSelectionData> list = new ArrayList<ColorPickerSelectionData>();
		ColorPickerSelectionData data = new ColorPickerSelectionData();
		int colorId = new ThemeManager(context).getBasicColorResId();
		data.setKey(ColorRes.TAB_ACTIONBAR_BK);
		data.setName("Action bar背景");
		data.setColor(JResource.getColor(context, ColorRes.TAB_ACTIONBAR_BK, colorId));
		list.add(data);

		data = new ColorPickerSelectionData();
		data.setKey(ColorRes.TAB_ACTIONBAR_TEXT);
		data.setName("Action bar文字");
		data.setColor(JResource.getColor(context, ColorRes.TAB_ACTIONBAR_TEXT, R.color.tab_actionbar_text));
		list.add(data);

		data = new ColorPickerSelectionData();
		data.setKey(ColorRes.TAB_ACTIONBAR_TEXT_FOCUS);
		data.setName("Action bar文字选中");
		data.setColor(JResource.getColor(context, ColorRes.TAB_ACTIONBAR_TEXT_FOCUS, R.color.tab_actionbar_text_focus));
		list.add(data);
		return list;
	}
}
