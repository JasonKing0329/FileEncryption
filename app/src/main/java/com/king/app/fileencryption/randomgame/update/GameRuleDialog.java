package com.king.app.fileencryption.randomgame.update;

import java.util.HashMap;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.CheckBox;

import com.king.app.fileencryption.R;
import com.king.app.fileencryption.publicview.CustomDialog;
import com.king.app.fileencryption.randomgame.RandomRules;

public class GameRuleDialog extends CustomDialog {

	private CheckBox imgThreadOnBox, repeatBox;
	public GameRuleDialog(Context context,
			OnCustomDialogActionListener actionListener) {
		super(context, actionListener);
		requestSaveAction(true);
		requestCancelAction(true);
		setTitle(context.getResources().getString(R.string.rgame_menu_rule));
		
		if (actionListener != null) {
			HashMap<String, Object> map = new HashMap<String, Object>();
			actionListener.onLoadData(map);
			RandomRules rules = (RandomRules) map.get("data");
			if (rules != null) {
				imgThreadOnBox.setChecked(rules.isImgThreadOn());
				repeatBox.setChecked(rules.isRepeatable());
			}
		}
	}

	@Override
	protected View getCustomView() {
		View view = LayoutInflater.from(getContext()).inflate(R.layout.random_game_rule_update, null);
		imgThreadOnBox = (CheckBox) view.findViewById(R.id.rgame_check_image_thread_on);
		repeatBox = (CheckBox) view.findViewById(R.id.rgame_check_repeatable);
		return view;
	}

	@Override
	protected View getCustomToolbar() {

		return null;
	}

	@Override
	public void onClick(View view) {

		if (view == saveIcon) {
			RandomRules rules = new RandomRules();
			rules.setImgThreadOn(imgThreadOnBox.isChecked());
			rules.setRepeatable(repeatBox.isChecked());
			rules.setNumber(1);
			if (actionListener != null) {
				actionListener.onSave(rules);
			}
		}
		super.onClick(view);
	}

}
