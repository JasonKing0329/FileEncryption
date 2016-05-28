package com.king.app.fileencryption;

import com.king.app.fileencryption.controller.AccessController;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.CompoundButton;
import android.widget.CompoundButton.OnCheckedChangeListener;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.RadioButton;
import android.widget.TextView;
import android.widget.Toast;

public class ModeInitActivity extends Activity implements OnClickListener, OnCheckedChangeListener {

	private RadioButton pubRadio, priRadio, fileRadio;
	private EditText pwdEdit;
	private TextView nextView;
	private LinearLayout hiddenGroup;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_mode_init);

		pubRadio = (RadioButton) findViewById(R.id.mode_init_radio_public);
		priRadio = (RadioButton) findViewById(R.id.mode_init_radio_private);
		fileRadio = (RadioButton) findViewById(R.id.mode_init_radio_filemanager);
		pwdEdit = (EditText) findViewById(R.id.mode_init_edit_pwd);
		nextView = (TextView) findViewById(R.id.mode_init_next);
		hiddenGroup = (LinearLayout) findViewById(R.id.mode_init_hide_group);

		priRadio.setChecked(true);
		nextView.setOnClickListener(this);
		pubRadio.setOnCheckedChangeListener(this);
		priRadio.setOnCheckedChangeListener(this);
		fileRadio.setOnCheckedChangeListener(this);
	}

	@Override
	public void onClick(View v) {
		if (pubRadio.isChecked()) {
			if (pwdEdit.getText().toString().equals("1010520")) {
				AccessController.getInstance().changeAccessMode(AccessController.ACCESS_MODE_PUBLIC);
				startFileManager();
			}
			else {
				Toast.makeText(this, R.string.mode_init_next_fail, Toast.LENGTH_LONG).show();
			}
		}
		else if (priRadio.isChecked()){
			AccessController.getInstance().changeAccessMode(AccessController.ACCESS_MODE_PRIVATE);
			startFileManager();
		}
		else if (fileRadio.isChecked()){
			AccessController.getInstance().changeAccessMode(AccessController.ACCESS_MODE_FILEMANAGER);
			startFileManager();
		}
	}

	private void startFileManager() {
		startActivity(new Intent().setClass(this, MainViewActivity.class));
		finish();
	}

	@Override
	public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
		if (isChecked) {
			if (buttonView == pubRadio) {
				Toast.makeText(this, R.string.mode_init_public_warning, Toast.LENGTH_LONG).show();
				hiddenGroup.setVisibility(View.VISIBLE);
			}
			else if (buttonView == priRadio) {
				Toast.makeText(this, R.string.mode_init_private_warning, Toast.LENGTH_LONG).show();
				hiddenGroup.setVisibility(View.INVISIBLE);
			}
			else if (buttonView == fileRadio) {
				Toast.makeText(this, R.string.mode_init_filemanager_warning, Toast.LENGTH_LONG).show();
				hiddenGroup.setVisibility(View.INVISIBLE);
			}
		}
	}

}
