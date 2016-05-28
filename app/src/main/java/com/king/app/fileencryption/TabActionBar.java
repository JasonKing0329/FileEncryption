package com.king.app.fileencryption;

import java.util.ArrayList;
import java.util.List;

import com.king.app.fileencryption.controller.ThemeManager;
import com.king.app.fileencryption.res.ColorRes;
import com.king.app.fileencryption.res.JResource;

import android.app.Activity;
import android.content.Context;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.HorizontalScrollView;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.PopupMenu;
import android.widget.TextView;
import android.widget.LinearLayout.LayoutParams;
import android.widget.PopupMenu.OnMenuItemClickListener;

public class TabActionBar implements OnClickListener {

	private Context context;
	private TextView titleView;
	private TextView fmTab, sorderTab, spicTab;
	private TextView fmTabLand, sorderTabLand, spicTabLand;
	private ImageView backButton, menuButton, sortButton, thumbButton, colorButton
			, addButton, playButton, unlockButton, fullScreenButton, changeButton, refreshButton;
	private OnTabSelectedListener tabSelectedListener;
	private OnClickListener iconListener;
	private LinearLayout actionbarTab, actionbarTabLand;
	private View actionbarTopLayout;

	private PopupMenu popupMenu;
	private MenuListener menuListener;
	private List<View> currentButtons;
	private List<View> tempSaveButtons;

	private TextView selectedTab, selectedTabLand;

	private int textColor, textFocusColor;
	private int defaultBkColor;

	public TabActionBar(Context context, MenuListener menuListener) {
		this.context = context;
		this.menuListener = menuListener;
		currentButtons = new ArrayList<View>();
		defaultBkColor = new ThemeManager(context).getBasicColorResId();
		init();
		selectedTab = fmTab;
		setTabTextColor(selectedTab, true);
		selectedTabLand = fmTabLand;
		setTabTextColor(selectedTabLand, true);
	}

	private void init() {
		Activity view = (Activity) context;

		backButton = (ImageView) view.findViewById(R.id.actionbar_back);
		menuButton = (ImageView) view.findViewById(R.id.actionbar_menu);
		colorButton = (ImageView) view.findViewById(R.id.actionbar_color);
		addButton = (ImageView) view.findViewById(R.id.actionbar_add);
		thumbButton = (ImageView) view.findViewById(R.id.actionbar_thumb);
		sortButton = (ImageView) view.findViewById(R.id.actionbar_sort);
		playButton = (ImageView) view.findViewById(R.id.actionbar_play);
		fullScreenButton = (ImageView) view.findViewById(R.id.actionbar_fullscreen);
		changeButton = (ImageView) view.findViewById(R.id.actionbar_change);
		unlockButton = (ImageView) view.findViewById(R.id.actionbar_unlock);
		refreshButton = (ImageView) view.findViewById(R.id.actionbar_refresh);
		titleView = (TextView) view.findViewById(R.id.actionbar_title);
		fmTab = (TextView) view.findViewById(R.id.actionbar_tab_file);
		sorderTab = (TextView) view.findViewById(R.id.actionbar_tab_sorder);
		spicTab = (TextView) view.findViewById(R.id.actionbar_tab_spicture);
		fmTabLand = (TextView) view.findViewById(R.id.actionbar_tab_file_land);
		sorderTabLand = (TextView) view.findViewById(R.id.actionbar_tab_sorder_land);
		spicTabLand = (TextView) view.findViewById(R.id.actionbar_tab_spicture_land);
		actionbarTopLayout = view.findViewById(R.id.actionbar_icon_layout);
		actionbarTab = (LinearLayout) view.findViewById(R.id.actionbar_tab_layout);
		actionbarTabLand = (LinearLayout) view.findViewById(R.id.actionbar_tab_layout_land);

		backButton.setOnClickListener(this);
		unlockButton.setOnClickListener(this);
		menuButton.setOnClickListener(this);
		colorButton.setOnClickListener(this);
		fmTab.setOnClickListener(this);
		sorderTab.setOnClickListener(this);
		spicTab.setOnClickListener(this);
		fmTabLand.setOnClickListener(this);
		sorderTabLand.setOnClickListener(this);
		spicTabLand.setOnClickListener(this);
	}

	public void setLandModeTab(boolean land) {
		if (land) {
			actionbarTabLand.setVisibility(View.VISIBLE);
			actionbarTab.setVisibility(View.GONE);
		}
		else {
			actionbarTabLand.setVisibility(View.GONE);
			actionbarTab.setVisibility(View.VISIBLE);
		}
	}

	public void setOnTabSelectedListener(OnTabSelectedListener listener) {
		tabSelectedListener = listener;
	}

	public void rePrepare() {
		init();
		rePrepareIconListener();
		reprePareIcon();
		setTabTextColor(selectedTab, true);
	}

	private void setTabTextColor(TextView tab, boolean isFocus) {
		if (isFocus) {
			tab.setTextColor(textFocusColor);
		}
		else {
			tab.setTextColor(textColor);
		}
	}

	private void reprePareIcon() {
		for (int i = 0; i < currentButtons.size(); i++) {
			currentButtons.get(i).setVisibility(View.VISIBLE);
		}
	}

	private void rePrepareIconListener() {
		addButton.setOnClickListener(iconListener);
		thumbButton.setOnClickListener(iconListener);
		sortButton.setOnClickListener(iconListener);
		playButton.setOnClickListener(iconListener);
		fullScreenButton.setOnClickListener(iconListener);
		changeButton.setOnClickListener(iconListener);
		refreshButton.setOnClickListener(iconListener);
	}

	public void setOnIconClickListener(OnClickListener onClickListener) {
		iconListener = onClickListener;
		rePrepareIconListener();
	}

	public interface OnTabSelectedListener {
		public void showFileManagerPage();
		public void showSorderPage();
		public void showSpicturePage();
	}

	public interface MenuListener {

		public void createMenu(MenuInflater menuInflater, Menu menu);
		public void onPrepareMenu(MenuInflater menuInflater, Menu menu);
		public OnMenuItemClickListener getMenuItemListener();
		public void onBack();
		public void onUnlock();
		public void onColor();
	}

	@Override
	public void onClick(View v) {
		if (v == menuButton) {
			if (popupMenu == null) {

				createMenu();
			}
			else {
				menuListener.onPrepareMenu(popupMenu.getMenuInflater(), popupMenu.getMenu());
			}
			popupMenu.show();
		}
		else if (v == colorButton) {
			menuListener.onColor();
		}
		else if (v == backButton) {
			menuListener.onBack();
		}
		else if (v == unlockButton) {
			menuListener.onUnlock();
		}
		else if (tabSelectedListener != null) {
			if (v == fmTab || v == fmTabLand) {
				setTabTextColor(selectedTab, false);
				selectedTab = fmTab;
				setTabTextColor(selectedTab, true);
				setTabTextColor(selectedTabLand, false);
				selectedTabLand = fmTabLand;
				setTabTextColor(selectedTabLand, true);
				tabSelectedListener.showFileManagerPage();
			}
			else if (v == sorderTab || v == sorderTabLand) {
				setTabTextColor(selectedTab, false);
				selectedTab = sorderTab;
				setTabTextColor(selectedTab, true);
				setTabTextColor(selectedTabLand, false);
				selectedTabLand = sorderTabLand;
				setTabTextColor(selectedTabLand, true);
				tabSelectedListener.showSorderPage();
			}
			else if (v == spicTab || v == spicTabLand) {
				setTabTextColor(selectedTab, false);
				selectedTab = spicTab;
				setTabTextColor(selectedTab, true);
				setTabTextColor(selectedTabLand, false);
				selectedTabLand = spicTabLand;
				setTabTextColor(selectedTabLand, true);
				tabSelectedListener.showSpicturePage();
			}
		}
	}

	private void createMenu() {
		popupMenu = new PopupMenu(context, menuButton);
		menuListener.createMenu(popupMenu.getMenuInflater(), popupMenu.getMenu());
		//menuWindow.setWidth(context.getResources().getDimensionPixelSize(R.dimen.actionbar_menu_width));
		popupMenu.setOnMenuItemClickListener(menuListener.getMenuItemListener());
	}

	public void onLandscape() {
//		LinearLayout.LayoutParams params = (LayoutParams) iconContainer.getLayoutParams();
//		int iconNumber = currentButtons.size();
//		if (currentButtons.contains(menuButton)) {
//			iconNumber --;
//		}
//		if (iconNumber < context.getResources().getInteger(R.integer.tabactionbar_max_icon)) {
//			params.width = context.getResources().getDimensionPixelSize(R.dimen.actionbar_icon_width) * iconNumber;
//		}
//		else {
//			params.width = context.getResources().getDimensionPixelSize(R.dimen.tabactionbar_icon_max_width);
//		}
	}

	public void onVertical() {
//		LinearLayout.LayoutParams params = (LayoutParams) iconContainer.getLayoutParams();
//		int iconNumber = currentButtons.size();
//		if (currentButtons.contains(menuButton)) {
//			iconNumber --;
//		}
//		if (iconNumber < context.getResources().getInteger(R.integer.tabactionbar_max_icon)) {
//			params.width = context.getResources().getDimensionPixelSize(R.dimen.actionbar_icon_width) * iconNumber;
//		}
//		else {
//			params.width = context.getResources().getDimensionPixelSize(R.dimen.tabactionbar_icon_max_width);
//		}
	}

	public void clearActionIcon() {
		for (View v:currentButtons) {
			v.setVisibility(View.GONE);
		}
		currentButtons.clear();
	}

	public void addAddIcon() {
		currentButtons.add(addButton);
		addButton.setVisibility(View.VISIBLE);
	}

	public void addThumbIcon() {
		currentButtons.add(thumbButton);
		thumbButton.setVisibility(View.VISIBLE);
	}

	public void addSortIcon() {
		currentButtons.add(sortButton);
		sortButton.setVisibility(View.VISIBLE);
	}

	public void addPlayIcon() {
		currentButtons.add(playButton);
		playButton.setVisibility(View.VISIBLE);
	}

	public void addFullScreenIcon() {
		currentButtons.add(fullScreenButton);
		fullScreenButton.setVisibility(View.VISIBLE);
	}

	public void addChangeIcon() {
		currentButtons.add(changeButton);
		changeButton.setVisibility(View.VISIBLE);
	}

	public void addRefreshIcon() {
		currentButtons.add(refreshButton);
		refreshButton.setVisibility(View.VISIBLE);
	}

	public void setTitle(String text) {
		titleView.setText(text);
	}
	public String getTitle() {
		return titleView.getText().toString();
	}

	public void hideTab() {
		actionbarTab.setVisibility(View.GONE);
		actionbarTabLand.setVisibility(View.GONE);
	}

	public void switchToSpicView() {
		setTabTextColor(selectedTab, false);
		selectedTab = spicTab;
		setTabTextColor(selectedTab, true);
		setTabTextColor(selectedTabLand, false);
		selectedTabLand = spicTabLand;
		setTabTextColor(selectedTabLand, true);
	}

	public void setPrivateMode() {
		if (currentButtons.size() > 0) {
			tempSaveButtons = new ArrayList<View>();
			for (View view:currentButtons) {
				tempSaveButtons.add(view);
			}
		}
		clearActionIcon();
		unlockButton.setVisibility(View.VISIBLE);
		currentButtons.add(unlockButton);
		menuButton.setEnabled(false);
		colorButton.setEnabled(false);
		fmTab.setEnabled(false);
		fmTabLand.setEnabled(false);
		sorderTab.setEnabled(false);
		sorderTabLand.setEnabled(false);
		spicTab.setEnabled(false);
		spicTabLand.setEnabled(false);
	}

	public void setPublicMode() {
		clearActionIcon();
		if (tempSaveButtons != null && tempSaveButtons.size() > 0) {
			for (View view:tempSaveButtons) {
				currentButtons.add(view);
			}
		}
		menuButton.setEnabled(true);
		colorButton.setEnabled(true);
		fmTab.setEnabled(true);
		fmTabLand.setEnabled(true);
		sorderTab.setEnabled(true);
		sorderTabLand.setEnabled(true);
		spicTab.setEnabled(true);
		spicTabLand.setEnabled(true);
	}

	public void changePlayStatus(boolean isPlay) {
		playButton.setImageResource(isPlay ? R.drawable.actionbar_stop : R.drawable.actionbar_play);
	}

	public void updateBackground(int newColor) {
		actionbarTopLayout.setBackgroundColor(newColor);
		actionbarTab.setBackgroundColor(newColor);
		actionbarTabLand.setBackgroundColor(newColor);
	}

	public void updateTextColor(int newColor) {
		textColor = newColor;
		if (selectedTab.getVisibility() == View.VISIBLE) {
			if (selectedTab != fmTab) {
				setTabTextColor(fmTab, false);
				setTabTextColor(fmTabLand, false);
			}
			if (selectedTab != sorderTab) {
				setTabTextColor(sorderTab, false);
				setTabTextColor(sorderTabLand, false);
			}
			if (selectedTab != spicTab) {
				setTabTextColor(spicTab, false);
				setTabTextColor(spicTabLand, false);
			}
		}
		else {
			if (selectedTabLand != fmTabLand) {
				setTabTextColor(fmTab, false);
				setTabTextColor(fmTabLand, false);
			}
			if (selectedTabLand != sorderTabLand) {
				setTabTextColor(sorderTab, false);
				setTabTextColor(sorderTabLand, false);
			}
			if (selectedTabLand != spicTabLand) {
				setTabTextColor(spicTab, false);
				setTabTextColor(spicTabLand, false);
			}
		}
	}

	public void updateTextFocusColor(int newColor) {
		textFocusColor = newColor;
		setTabTextColor(selectedTab, true);
		setTabTextColor(selectedTabLand, true);
	}

	public void resetColors() {
		updateBackground(JResource.getColor(context
				, ColorRes.TAB_ACTIONBAR_BK, defaultBkColor));
		updateTextColor(JResource.getColor(context
				, ColorRes.TAB_ACTIONBAR_TEXT, R.color.tab_actionbar_text));
		updateTextFocusColor(JResource.getColor(context
				, ColorRes.TAB_ACTIONBAR_TEXT_FOCUS, R.color.tab_actionbar_text_focus));
	}
}
