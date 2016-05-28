package com.king.app.fileencryption.randomgame.team;

import com.king.app.fileencryption.Application;
import com.king.app.fileencryption.R;
import com.king.app.fileencryption.publicview.DragSideBar;

import android.app.Activity;
import android.content.Context;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.RadioButton;
import android.widget.TextView;

public class DragSideViewManager implements OnClickListener {

	public interface OnSideListener {
		public void onSaveData();
		public void onLoadData();
		public void onSaveAs();
		public void onResetOrder(int index);
		public void onNew();
		public void onTurnData();
	}
	private Context mContext;
	private DragSideBar dragSideBar;
	private View bkView;
	private RadioButton randomRadio, switchRadio;
	private RadioButton waitRadio, titleRadio, dataRadio;
	private RadioButton titleEditRadio, titleTMRadio;
	private RadioButton dataEditRadio, dataShowRadio;
	private TextView resetOrder1Button;
	private TextView resetOrder2Button;
	private TextView newButton;
	private TextView saveDataButton;
	private TextView saveAsDataButton;
	private TextView loadDataButton;
	private TextView turnDataButton;
	
	private OnSideListener onSideListener;
	
	public DragSideViewManager(Context context, DragSideBar sideBar) {
		mContext = context;
		dragSideBar = sideBar;
		dragSideBar.setLayoutRes(R.layout.game_team_side);
		
		Activity view = (Activity) context;
		bkView = view.findViewById(R.id.dragsidebar_bk);
		randomRadio = (RadioButton) view.findViewById(R.id.game_team_radio_order_random);
		switchRadio = (RadioButton) view.findViewById(R.id.game_team_radio_order_switch);
		waitRadio = (RadioButton) view.findViewById(R.id.game_team_radio_after_wait);
		titleRadio = (RadioButton) view.findViewById(R.id.game_team_radio_after_title);
		dataRadio = (RadioButton) view.findViewById(R.id.game_team_radio_after_data);
		titleEditRadio = (RadioButton) view.findViewById(R.id.game_team_radio_title_edit);
		titleTMRadio = (RadioButton) view.findViewById(R.id.game_team_radio_title_team);
		dataEditRadio = (RadioButton) view.findViewById(R.id.game_team_radio_data_edit);
		dataShowRadio = (RadioButton) view.findViewById(R.id.game_team_radio_data_show);
		resetOrder1Button = (TextView) view.findViewById(R.id.game_team_button_reset1);
		resetOrder2Button = (TextView) view.findViewById(R.id.game_team_button_reset2);
		newButton = (TextView) view.findViewById(R.id.game_team_button_new);
		saveDataButton = (TextView) view.findViewById(R.id.game_team_button_savedata);
		saveAsDataButton = (TextView) view.findViewById(R.id.game_team_button_saveas);
		loadDataButton = (TextView) view.findViewById(R.id.game_team_button_loaddata);
		turnDataButton = (TextView) view.findViewById(R.id.game_team_button_turn);
		if (Application.isLollipop()) {
			resetOrder1Button.setBackgroundResource(R.drawable.ripple_rect_white);
			resetOrder2Button.setBackgroundResource(R.drawable.ripple_rect_white);
			newButton.setBackgroundResource(R.drawable.ripple_rect_white);
			saveDataButton.setBackgroundResource(R.drawable.ripple_rect_white);
			loadDataButton.setBackgroundResource(R.drawable.ripple_rect_white);
			saveAsDataButton.setBackgroundResource(R.drawable.ripple_rect_white);
			turnDataButton.setBackgroundResource(R.drawable.ripple_rect_white);
		}
		bkView.setOnClickListener(this);
		resetOrder1Button.setOnClickListener(this);
		resetOrder2Button.setOnClickListener(this);
		newButton.setOnClickListener(this);
		saveDataButton.setOnClickListener(this);
		loadDataButton.setOnClickListener(this);
		saveAsDataButton.setOnClickListener(this);
		turnDataButton.setOnClickListener(this);
	}

	public void setOnSideListener(OnSideListener listener) {
		onSideListener = listener;
	}
	
	@Override
	public void onClick(View view) {
		if (view == resetOrder1Button) {
			if (onSideListener != null) {
				onSideListener.onResetOrder(1);
			}
		}
		else if (view == resetOrder2Button) {
			if (onSideListener != null) {
				onSideListener.onResetOrder(2);
			}
		}
		else if (view == newButton) {
			if (onSideListener != null) {
				onSideListener.onNew();
			}
		}
		else if (view == saveDataButton) {
			if (onSideListener != null) {
				onSideListener.onSaveData();
			}
		}
		else if (view == saveAsDataButton) {
			if (onSideListener != null) {
				onSideListener.onSaveAs();
			}
		}
		else if (view == loadDataButton) {
			if (onSideListener != null) {
				onSideListener.onLoadData();
			}
		}
		else if (view == turnDataButton) {
			if (onSideListener != null) {
				onSideListener.onTurnData();
			}
		}
	}

	public boolean isTitleEditable() {
		return titleEditRadio.isChecked();
	}

	public boolean isDataEditable() {
		return dataEditRadio.isChecked();
	}

	public boolean isWaitAfterRandom() {
		return waitRadio.isChecked();
	}

	public boolean isAsTeamMember() {
		return titleTMRadio.isChecked();
	}

	public boolean isFillTitleAfterRandom() {
		return titleRadio.isChecked();
	}

	public boolean isChangeOrder() {
		return switchRadio.isChecked();
	}

	public boolean isProcessRandom() {
		return randomRadio.isChecked();
	}
	
	public boolean isClickDataToShow() {
		return dataShowRadio.isChecked();
	}
	
}
