package com.king.app.fileencryption.publicview;

import java.util.ArrayList;
import java.util.List;

import com.king.app.fileencryption.R;

import android.app.Activity;
import android.content.Context;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemSelectedListener;
import android.widget.HorizontalScrollView;
import android.widget.ImageView;
import android.widget.LinearLayout.LayoutParams;
import android.widget.PopupMenu;
import android.widget.PopupMenu.OnMenuItemClickListener;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;
import android.widget.Spinner;
import android.widget.TextView;

public class ActionBar implements OnClickListener, TextWatcher {

	private Context context;
	private ImageView backButton, menuButton, addButton, editButton
		, titleIcon, saveButton, cancelButton, galleryButton
		, searchButton, closeButton, refreshButton, changeButton
		, fullScreenButton, randomChangeButton, deleteButton, saveButton1, showButton;
	private TextView titleView;
	private EditText searchEdit;
	private ActionBarListener actionBarListener;
	private List<View> currentButtons, tempButotns;
	private HorizontalScrollView iconContainer;
	//private Spinner levelSpinner, courtSpinner;
	private LinearLayout layout;
	private RelativeLayout searchLayout;
	
	private PopupMenu popupMenu;
	
	public ActionBar(Context context, ActionBarListener listener) {
		this.context = context;
		actionBarListener = listener;
		Activity view = (Activity) context;
		layout = (LinearLayout) view.findViewById(R.id.actionbar);
		searchLayout = (RelativeLayout) view.findViewById(R.id.actionbar_search_layout);
		backButton = (ImageView) view.findViewById(R.id.actionbar_back);
		menuButton = (ImageView) view.findViewById(R.id.actionbar_menu);
		addButton = (ImageView) view.findViewById(R.id.actionbar_add);
		editButton = (ImageView) view.findViewById(R.id.actionbar_edit);
		saveButton = (ImageView) view.findViewById(R.id.actionbar_save);
		saveButton1 = (ImageView) view.findViewById(R.id.actionbar_save1);
		cancelButton = (ImageView) view.findViewById(R.id.actionbar_cancel);
		galleryButton = (ImageView) view.findViewById(R.id.actionbar_gallery);
		refreshButton = (ImageView) view.findViewById(R.id.actionbar_refresh);
		searchButton = (ImageView) view.findViewById(R.id.actionbar_search);
		changeButton = (ImageView) view.findViewById(R.id.actionbar_change);
		randomChangeButton = (ImageView) view.findViewById(R.id.actionbar_random_change);
		fullScreenButton = (ImageView) view.findViewById(R.id.actionbar_fullscreen);
		closeButton = (ImageView) view.findViewById(R.id.actionbar_search_close);
		deleteButton = (ImageView) view.findViewById(R.id.actionbar_delete);
		showButton = (ImageView) view.findViewById(R.id.actionbar_show);
		titleIcon = (ImageView) view.findViewById(R.id.actionbar_title_icon);
		searchEdit = (EditText) view.findViewById(R.id.actionbar_search_edittext);
		iconContainer = (HorizontalScrollView) view.findViewById(R.id.actionbar_icon_container);
		backButton.setOnClickListener(this);
		menuButton.setOnClickListener(this);
		addButton.setOnClickListener(this);
		refreshButton.setOnClickListener(this);
		editButton.setOnClickListener(this);
		saveButton.setOnClickListener(this);
		saveButton1.setOnClickListener(this);
		showButton.setOnClickListener(this);
		cancelButton.setOnClickListener(this);
		searchButton.setOnClickListener(this);
		randomChangeButton.setOnClickListener(this);
		fullScreenButton.setOnClickListener(this);
		deleteButton.setOnClickListener(this);
		closeButton.setOnClickListener(this);
		changeButton.setOnClickListener(this);
		galleryButton.setOnClickListener(this);
		searchEdit.addTextChangedListener(this);
		titleView = (TextView) view.findViewById(R.id.actionbar_title);
		
		currentButtons = new ArrayList<View>();
		
	}

	public interface ActionBarListener {
		public void onBack();
		public void onDelete();
		public void onRefresh();
		public void onIconClick(View view);
		public void createMenu(MenuInflater menuInflater, Menu menu);
		public void onPrepareMenu(MenuInflater menuInflater, Menu menu);
		public OnMenuItemClickListener getMenuItemListener();
		public void onTextChanged(String text, int start, int before, int count);
	}
	
	public interface ActionSpinnerListener {
		public void onTitleFilterListener(int indexLevel, int indexCourt);
	}
	
	/*
	public void addActionSpinnerListener(final ActionSpinnerListener listener) {

		levelSpinner.setOnItemSelectedListener(new OnItemSelectedListener() {

			@Override
			public void onItemSelected(AdapterView<?> arg0, View arg1,
					int index, long arg3) {
				listener.onTitleFilterListener(index, courtSpinner.getSelectedItemPosition());
			}

			@Override
			public void onNothingSelected(AdapterView<?> arg0) {
				
			}
		});
		courtSpinner.setOnItemSelectedListener(new OnItemSelectedListener() {

			@Override
			public void onItemSelected(AdapterView<?> arg0, View arg1,
					int index, long arg3) {
				listener.onTitleFilterListener(levelSpinner.getSelectedItemPosition(), index);
			}

			@Override
			public void onNothingSelected(AdapterView<?> arg0) {
				
			}
		});
	}
	*/
	
	public int getHeight() {
		return context.getResources().getDimensionPixelSize(R.dimen.actionbar_height);
	}
	
	public void onLandscape() {
		LinearLayout.LayoutParams params = (LayoutParams) iconContainer.getLayoutParams();
		int iconNumber = currentButtons.size();
		if (currentButtons.contains(menuButton)) {
			iconNumber --;
		}
		if (iconNumber < context.getResources().getInteger(R.integer.actionbar_max_icon)) {
			params.width = context.getResources().getDimensionPixelSize(R.dimen.actionbar_icon_width) * iconNumber;
		}
		else {
			params.width = context.getResources().getDimensionPixelSize(R.dimen.actionbar_icon_max_width);
		}
	}

	public void onVertical() {
		LinearLayout.LayoutParams params = (LayoutParams) iconContainer.getLayoutParams();
		int iconNumber = currentButtons.size();
		if (currentButtons.contains(menuButton)) {
			iconNumber --;
		}
		if (iconNumber < context.getResources().getInteger(R.integer.actionbar_max_icon)) {
			params.width = context.getResources().getDimensionPixelSize(R.dimen.actionbar_icon_width) * iconNumber;
		}
		else {
			params.width = context.getResources().getDimensionPixelSize(R.dimen.actionbar_icon_max_width);
		}
	}
	
	public void clearActionIcon() {
		for (View v:currentButtons) {
			v.setVisibility(View.GONE);
		}
		currentButtons.clear();
	}
	public void addEditIcon() {
		currentButtons.add(editButton);
		editButton.setVisibility(View.VISIBLE);
	}
	public void addMenuIcon() {
		currentButtons.add(menuButton);
		menuButton.setVisibility(View.VISIBLE);
	}
	public void addAddIcon() {
		currentButtons.add(addButton);
		addButton.setVisibility(View.VISIBLE);
	}
	public void addGalleryIcon() {
		currentButtons.add(galleryButton);
		galleryButton.setVisibility(View.VISIBLE);
	}
	public void addSaveIcon() {
		currentButtons.add(saveButton1);
		saveButton1.setVisibility(View.VISIBLE);
	}
	public void addShowIcon() {
		currentButtons.add(showButton);
		showButton.setVisibility(View.VISIBLE);
	}
	public void addFullScreenIcon() {
		currentButtons.add(fullScreenButton);
		fullScreenButton.setVisibility(View.VISIBLE);
	}
	public void addDeleteIcon() {
		currentButtons.add(deleteButton);
		deleteButton.setVisibility(View.VISIBLE);
	}
	public void addRandomChangeIcon() {
		currentButtons.add(randomChangeButton);
		randomChangeButton.setVisibility(View.VISIBLE);
	}
	public void addRefreshIcon() {
		currentButtons.add(refreshButton);
		refreshButton.setVisibility(View.VISIBLE);
	}
	public void addChangeIcon() {
		currentButtons.add(changeButton);
		changeButton.setVisibility(View.VISIBLE);
	}
	public void addCancelIcon() {
		currentButtons.add(cancelButton);
		cancelButton.setVisibility(View.VISIBLE);
	}
	public void addTitleIcon(int resId) {
		titleIcon.setImageResource(resId);
		currentButtons.add(titleIcon);
		titleIcon.setVisibility(View.VISIBLE);
	}
	public void addSearchIcon() {
		currentButtons.add(searchButton);
		searchButton.setVisibility(View.VISIBLE);
	}
	public void closeSearch() {
		
		Animation animation = AnimationUtils.loadAnimation(context, R.anim.disappear);
		searchLayout.startAnimation(animation);
		searchLayout.setVisibility(View.GONE);
		
		animation = AnimationUtils.loadAnimation(context, R.anim.appear);
		iconContainer.startAnimation(animation);
		iconContainer.setVisibility(View.VISIBLE);
	}
	public void addSearchLayout() {

		Animation animation = AnimationUtils.loadAnimation(context, R.anim.appear);
		searchLayout.startAnimation(animation);
		searchLayout.setVisibility(View.VISIBLE);

		animation = AnimationUtils.loadAnimation(context, R.anim.disappear);
		iconContainer.startAnimation(animation);
		iconContainer.setVisibility(View.GONE);
	}
	
	/*
	public void addTitleFilter() {
		currentButtons.add(levelSpinner);
		currentButtons.add(courtSpinner);
		levelSpinner.setVisibility(View.VISIBLE);
		levelSpinner.setSelection(0);
		courtSpinner.setVisibility(View.VISIBLE);
		courtSpinner.setSelection(0);
	}
	*/
	
	public void setTitle(String text) {
		titleView.setText(text);
	}
	public String getTitle() {
		return titleView.getText().toString();
	}
	
	@Override
	public void onClick(View view) {
		if (view == backButton) {
			actionBarListener.onBack();
		}
		else if (view == deleteButton) {
			actionBarListener.onDelete();
		}
		else if (view == addButton) {
			setEditMode(true);
		}
		else if (view == editButton) {
			setEditMode(true);
		}
		else if (view == menuButton) {
			if (popupMenu == null) {
				
				createMenu();
			}
			else {
				actionBarListener.onPrepareMenu(popupMenu.getMenuInflater(), popupMenu.getMenu());
			}
			popupMenu.show();
		}
		else if (view == cancelButton) {
			setEditMode(false);
		}
		else if (view == saveButton) {
			setEditMode(false);
		}
		else if (view == searchButton) {
			addSearchLayout();
		}
		else if (view == closeButton) {
			closeSearch();
		}
		else if (view == refreshButton) {
			actionBarListener.onRefresh();
		}
		else {
			actionBarListener.onIconClick(view);
		}
	}

	private void setEditMode(boolean b) {
		if (b) {
			saveButton.setVisibility(View.VISIBLE);
			cancelButton.setVisibility(View.VISIBLE);
			for (View v:currentButtons) {
				v.setVisibility(View.GONE);
			}
		}
		else {
			saveButton.setVisibility(View.GONE);
			cancelButton.setVisibility(View.GONE);
			for (View v:currentButtons) {
				v.setVisibility(View.VISIBLE);
			}
		}
	}

	private void createMenu() {
		popupMenu = new PopupMenu(context, menuButton);
		actionBarListener.createMenu(popupMenu.getMenuInflater(), popupMenu.getMenu());
		//menuWindow.setWidth(context.getResources().getDimensionPixelSize(R.dimen.actionbar_menu_width));
		popupMenu.setOnMenuItemClickListener(actionBarListener.getMenuItemListener());
	}
	
	public boolean isHidden() {
		return layout.getVisibility() == View.GONE ? true:false;
	}

	public boolean isShowing() {
		return layout.getVisibility() == View.VISIBLE ? true:false;
	}
	
	public void hide() {
		layout.setVisibility(View.GONE);
	}
	
	public void show() {
		layout.setVisibility(View.VISIBLE);
	}
	public boolean dismissMenu() {
		return false;
	}

	@Override
	public void beforeTextChanged(CharSequence s, int start, int count,
			int after) {
		
	}

	@Override
	public void onTextChanged(CharSequence s, int start, int before, int count) {
		actionBarListener.onTextChanged(s.toString(), start, before, count);
	}

	@Override
	public void afterTextChanged(Editable s) {
		
	}

	public void setBackgroundColor(int color) {
		layout.setBackgroundColor(color);
	}
}
