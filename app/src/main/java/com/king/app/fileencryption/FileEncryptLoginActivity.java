package com.king.app.fileencryption;


import com.king.app.fileencryption.controller.AccessController;
import com.king.app.fileencryption.controller.FingerPrintController;
import com.king.app.fileencryption.controller.FingerPrintController.SimpleIdentifyListener;
import com.king.app.fileencryption.controller.ThemeManager;
import com.king.app.fileencryption.data.Configuration;
import com.king.app.fileencryption.data.DBInfor;
import com.king.app.fileencryption.guide.GuideActivity;
import com.king.app.fileencryption.publicview.ActionBar;
import com.king.app.fileencryption.publicview.DefaultDialogManager;
import com.king.app.fileencryption.publicview.ActionBar.ActionBarListener;
import com.king.app.fileencryption.res.JResource;
import com.king.app.fileencryption.service.FileDBService;
import com.king.app.fileencryption.service.OnServiceProgressListener;
import com.king.app.fileencryption.setting.SettingProperties;
import com.king.app.fileencryption.timeline.TimeLineActivity;
import com.king.app.fileencryption.timeline.update.TimeLineUpdateActivity;

import android.app.Activity;
import android.app.ActivityManager;
import android.app.ActivityManager.RunningServiceInfo;
import android.content.ComponentName;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.ServiceConnection;
import android.os.Bundle;
import android.os.IBinder;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.animation.AlphaAnimation;
import android.view.animation.Animation;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.PopupMenu.OnMenuItemClickListener;
import android.widget.Toast;

public class FileEncryptLoginActivity extends Activity implements ActionBarListener, OnClickListener
		, OnServiceProgressListener{

	private EditText userEdit, pwdEdit;
	private Button okButton, fingerPrintButton;
	private FingerPrintController fingerPrint;

	private TextView progressTextView;
	private ImageView loadingBkView;
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		setTheme(new ThemeManager(this).getDefaultTheme());
		super.onCreate(savedInstanceState);

		if (!Configuration.init()) {
			Toast.makeText(this, R.string.error_app_root_create_fail, Toast.LENGTH_LONG).show();
		}
		if (!DBInfor.prepare(this)) {
			Toast.makeText(this, R.string.error_database_create_fail, Toast.LENGTH_LONG).show();
		}
		Configuration.initV6_2Change();
		Configuration.initV7_0Change();
		Configuration.initParams(this);

		if (SettingProperties.isFingerPrintEnable(this)) {
			fingerPrint = new FingerPrintController(this);
			if (fingerPrint.isSupported() && fingerPrint.hasRegistered()) {
				startFingerPrintDialog();
				return;
			}
		}

		setContentView(Application.isLollipop() ? R.layout.login_l : R.layout.login);

		userEdit = (EditText) findViewById(R.id.login_edit_user);
		pwdEdit = (EditText) findViewById(R.id.login_edit_pwd);
		okButton = (Button) findViewById(R.id.login_button_ok);
		fingerPrintButton = (Button) findViewById(R.id.login_button_fingerprint);

		okButton.setOnClickListener(this);

		ActionBar actionBar = new ActionBar(this, this);
		actionBar.setTitle(getResources().getString(R.string.app_name));
		actionBar.addTitleIcon(R.drawable.app_icon);


		//dev mode
        /*
		if (true) {
            AccessController.getInstance().changeAccessMode(AccessController.ACCESS_MODE_SUPERUSER);
            startActivity(new Intent().setClass(FileEncryptLoginActivity.this, FileManagerActivityUpdate.class));
            finish();
            return;
		}
		*/

		checkFingerPrintPart();
	}

	@Override
	public void onClick(View v) {
		if (v == okButton) {
			//super command
			if (pwdEdit.getText().toString().equals("jyjyjyjyjyjyjyjy")) {
				superUser();
				return;
			}

			// 取消super suer以为的权限登陆
//			if (userEdit.getText().toString().equals("jysddx") && pwdEdit.getText().toString().equals("king")) {
//	            startActivity(new Intent().setClass(FileEncryptLoginActivity.this, ModeInitActivity.class));
//	            finish();
//			}
//			else {
//				Toast.makeText(FileEncryptLoginActivity.this, R.string.login_pwd_error, Toast.LENGTH_LONG).show();
//			}

		}
		else if (v == fingerPrintButton) {
			startFingerPrintDialog();
		}
	}

	private void startFingerPrintDialog() {
		if (fingerPrint.hasRegistered()) {
			boolean withPW = false;
			fingerPrint.showIdentifyDialog(withPW, new SimpleIdentifyListener() {

				@Override
				public void onSuccess() {
					superUser();
				}

				@Override
				public void onFail() {

				}

				@Override
				public void onCancel() {
					finish();
				}
			});
		}
		else {
			Toast.makeText(this, R.string.login_finger_not_register, Toast.LENGTH_LONG).show();
		}
	}

	private void checkFingerPrintPart() {
		fingerPrint = new FingerPrintController(this);
		if (fingerPrint.isSupported()) {
			fingerPrintButton.setVisibility(View.VISIBLE);
			fingerPrintButton.setOnClickListener(this);
		}
		else {

			fingerPrintButton.setVisibility(View.GONE);
		}
	}

	private boolean executeInsertProcess;

	protected void superUser() {
//		startService(new Intent().setClass(this, FileDBService.class));
		new DefaultDialogManager().showWarningActionDialog(this
				, getResources().getString(R.string.login_start_service_insert)
				, getResources().getString(R.string.yes)
				, getResources().getString(R.string.allno)
				, getResources().getString(R.string.no)
				, new DialogInterface.OnClickListener() {

					@Override
					public void onClick(DialogInterface dialog, int which) {
						if (which == DialogInterface.BUTTON_POSITIVE) {
							executeInsertProcess = true;
							showLoading();
							if (!isServiceRunning()) {
								bindService(new Intent().setClass(FileEncryptLoginActivity.this, FileDBService.class)
										, connection, BIND_AUTO_CREATE);
							}
						}
						else if (which == DialogInterface.BUTTON_NEGATIVE) {

							showLoading();
							if (!isServiceRunning()) {
								bindService(new Intent().setClass(FileEncryptLoginActivity.this, FileDBService.class)
										, connection, BIND_AUTO_CREATE);
							}
						}
						else {//netrual, all no
							onServiceDone();
						}
					}
				});
	}

	private boolean isServiceRunning() {
		ActivityManager manager = (ActivityManager) getSystemService(ACTIVITY_SERVICE);
		for (RunningServiceInfo service : manager.getRunningServices(Integer.MAX_VALUE)) {
			if ("com.king.app.fileencryption.service.FileDBService"
					.equals(service.service.getClassName())) {
				return true;
			}
		}
		return false;
	}
	private void showLoading() {
		setContentView(R.layout.activity_login_loading);
		progressTextView = (TextView) findViewById(R.id.login_loading_progress_num);
		loadingBkView = (ImageView) findViewById(R.id.login_loading_bk);
//		loadingBkView.startAnimation(getLoadingBkAnimation());
	}

//	private Animation getLoadingBkAnimation() {
//		AlphaAnimation animation = new AlphaAnimation(0, 1.0f);
//		animation.setDuration(2000);
//		return animation;
//	}

	ServiceConnection connection = new ServiceConnection() {

		@Override
		public void onServiceDisconnected(ComponentName name) {

		}

		@Override
		public void onServiceConnected(ComponentName name, IBinder service) {
			FileDBService fileDBService = ((FileDBService.FileDbBinder) service).getService();
			fileDBService.setOnProgressListener(FileEncryptLoginActivity.this);
			fileDBService.startWork(executeInsertProcess);
		}
	};

	@Override
	public boolean onCreateOptionsMenu(Menu menu) {

		// Inflate the menu; this adds items to the action bar if it is present.
		getMenuInflater().inflate(R.menu.file_encryption_main, menu);
		return true;
	}

	@Override
	public boolean onOptionsItemSelected(MenuItem item) {
		// Handle action bar item clicks here. The action bar will
		// automatically handle clicks on the Home/Up button, so long
		// as you specify a parent activity in AndroidManifest.xml.
		return super.onOptionsItemSelected(item);
	}


	@Override
	public void onBack() {
		finish();
	}

	@Override
	public void onRefresh() {
		// TODO Auto-generated method stub

	}


	@Override
	public void createMenu(MenuInflater menuInflater, Menu menu) {
		// TODO Auto-generated method stub

	}


	@Override
	public void onPrepareMenu(MenuInflater menuInflater, Menu menu) {
		// TODO Auto-generated method stub

	}


	@Override
	public OnMenuItemClickListener getMenuItemListener() {
		// TODO Auto-generated method stub
		return null;
	}


	@Override
	public void onTextChanged(String text, int start, int before, int count) {
		// TODO Auto-generated method stub

	}

	@Override
	public void onDelete() {
		// TODO Auto-generated method stub

	}

	@Override
	public void onIconClick(View view) {
		// TODO Auto-generated method stub

	}

	@Override
	public void onServiceProgress(int progress) {
		progressTextView.setText(progress + "%");
	}

	@Override
	public void onServiceDone() {

		JResource.initializeColors();

		AccessController.getInstance().changeAccessMode(AccessController.ACCESS_MODE_SUPERUSER);

		int startViewMode = SettingProperties.getStartViewMode(this);
		switch (startViewMode) {
			case SettingProperties.START_VIEW_CLASSIC:
				startActivity(new Intent().setClass(FileEncryptLoginActivity.this, MainViewActivity.class));
				break;
			case SettingProperties.START_VIEW_GUIDE:
				startActivity(new Intent().setClass(FileEncryptLoginActivity.this, GuideActivity.class));
				break;
			default:
				startActivity(new Intent().setClass(FileEncryptLoginActivity.this, TimeLineUpdateActivity.class));
				break;
		}
		finish();
	}

}
