package com.king.app.fileencryption.randomgame;

import com.king.app.fileencryption.R;

import android.app.Dialog;
import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.EditText;
import android.widget.RadioButton;
import android.widget.Toast;

@Deprecated
public class RandomGameRuleDialog extends Dialog implements OnClickListener {

	public interface OnRuleSelectListener {
		public void onSelectOk(RandomRules rules);
		public void onCancel();
	}
	
	private Button okButton, cancelButton;
	private RadioButton autoRadio, manualRadio, replaceRadio, followbyRadio;
	private CheckBox imgThreadOnBox, screenOnBox, repeatBox;
	private EditText numberEdit;
	
	private OnRuleSelectListener ruleSelectListener;
	private Context context;
	
	private RandomGameRuleDialog(Context context) {
		super(context);
		this.context = context;
		View view = LayoutInflater.from(context).inflate(R.layout.random_game_rule, null);
		setContentView(view);
		okButton = (Button) view.findViewById(R.id.rgame_rule_ok);
		cancelButton = (Button) view.findViewById(R.id.rgame_rule_cancel);
		autoRadio = (RadioButton) view.findViewById(R.id.rgame_radio_auto);
		manualRadio = (RadioButton) view.findViewById(R.id.rgame_radio_manual);
		replaceRadio = (RadioButton) view.findViewById(R.id.rgame_radio_replace);
		followbyRadio = (RadioButton) view.findViewById(R.id.rgame_radio_followby);
		imgThreadOnBox = (CheckBox) view.findViewById(R.id.rgame_check_image_thread_on);
		screenOnBox = (CheckBox) view.findViewById(R.id.rgame_check_scene_on);
		repeatBox = (CheckBox) view.findViewById(R.id.rgame_check_repeatable);
		numberEdit = (EditText) view.findViewById(R.id.rgame_edit_random_number);
		okButton.setOnClickListener(this);
		cancelButton.setOnClickListener(this);
		
		setTitle(context.getResources().getString(R.string.rgame_menu_rule));
	}

	public RandomGameRuleDialog(Context context, OnRuleSelectListener listener) {
		this(context);
		ruleSelectListener = listener;
	}

	@Override
	public void onClick(View v) {
		if (v == okButton) {
			RandomRules rules = new RandomRules();
			rules.setAuto(autoRadio.isChecked());
			rules.setReplace(replaceRadio.isChecked());
			rules.setImgThreadOn(imgThreadOnBox.isChecked());
			rules.setScreenOn(screenOnBox.isChecked());
			rules.setRepeatable(repeatBox.isChecked());
			try {
				rules.setNumber(Integer.parseInt(numberEdit.getText().toString()));
				ruleSelectListener.onSelectOk(rules);
				dismiss();
			} catch (Exception e) {
				Toast.makeText(context, R.string.rgame_rule_number_null, Toast.LENGTH_LONG).show();
				e.printStackTrace();
			}
		}
		else if (v == cancelButton) {
			ruleSelectListener.onCancel();
			dismiss();
		}
	}

}
