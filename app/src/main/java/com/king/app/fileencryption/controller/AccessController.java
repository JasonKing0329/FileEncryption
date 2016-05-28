package com.king.app.fileencryption.controller;

import com.king.app.fileencryption.R;

import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.text.InputType;
import android.widget.EditText;
import android.widget.Toast;

public class AccessController {

	public static final int ACCESS_MODE_FILEMANAGER = 101;
	public static final int ACCESS_MODE_PRIVATE = 102;
	public static final int ACCESS_MODE_PUBLIC = 103;
	public static final int ACCESS_MODE_SUPERUSER = 104;
	
	private static AccessController controller;
	private int accessMode;
	
	private AccessController() {
		accessMode = ACCESS_MODE_PRIVATE;
	}
	
	public static AccessController getInstance() {
		if (controller == null) {
			controller = new AccessController();
		}
		return controller;
	}
	
	public int getAccessMode() {
		return accessMode;
	}
	
	public void changeAccessMode(int mode) {
		accessMode = mode;
	}
	
	public void showPwdDialog(final Context context, final IdentityCheckListener listener) {
		EditText edit = new EditText(context);
		edit.setInputType(InputType.TYPE_CLASS_TEXT | InputType.TYPE_TEXT_VARIATION_PASSWORD);
		final EditText pwdCheck = edit;
		AlertDialog.Builder dialog = new AlertDialog.Builder(context);
		dialog.setTitle(R.string.identity_check);
		dialog.setView(pwdCheck);
		dialog.setPositiveButton(R.string.ok, new DialogInterface.OnClickListener() {
			
			@Override
			public void onClick(DialogInterface dialog, int which) {
				if (pwdCheck.getText().toString().equals("1010520")) {
					listener.pass();
				}
				else {
					Toast.makeText(context, R.string.login_pwd_error, Toast.LENGTH_LONG).show();
					listener.fail();
				}
			}
		});
		dialog.setNegativeButton(R.string.cancel, new DialogInterface.OnClickListener() {
			
			@Override
			public void onClick(DialogInterface dialog, int which) {
				listener.cancel();
			}
		});
		dialog.show();
	}
	
	public interface IdentityCheckListener {
		public void pass();
		public void fail();
		public void cancel();
	}

}
