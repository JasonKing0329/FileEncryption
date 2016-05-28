package com.king.app.fileencryption;

import com.king.app.fileencryption.controller.ThemeManager;
import com.king.app.fileencryption.data.Constants;
import com.king.app.fileencryption.randomgame.RandomGamePage;
import com.king.app.fileencryption.sorder.controller.SOrderPictureBridge;
import com.king.app.fileencryption.sorder.entity.SOrder;
import com.king.app.fileencryption.util.DisplayHelper;

import android.app.Activity;
import android.app.ProgressDialog;
import android.content.res.Configuration;
import android.os.Bundle;
import android.os.Handler;
import android.os.Handler.Callback;
import android.os.Message;
import android.view.WindowManager;

@Deprecated
public class RandomGameActivity extends Activity {

	private RandomGamePage randomGamePage;
	private SOrder currentOrder;
	private ProgressDialog progressDialog;
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		setTheme(new ThemeManager(this).getDefaultTheme());
		super.onCreate(savedInstanceState);
		if (DisplayHelper.isFullScreen()) {
			getWindow().addFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN);
		}

		Bundle bundle = getIntent().getExtras();
		int orderId = bundle.getInt(Constants.KEY_RANDOM_GAME_SORDER);
		currentOrder = SOrderPictureBridge.getInstance(this).queryOrder(orderId);
		loadOrderPicture();
	}

	private void loadOrderPicture() {
		progressDialog = new ProgressDialog(this);
		progressDialog.setMessage(getResources().getString(R.string.loading));
		progressDialog.show();
		new LoadOrderThread().start();
	}

	private class LoadOrderThread extends Thread implements Callback {
		private Handler handler = new Handler(this);

		public void run() {
			SOrderPictureBridge.getInstance(RandomGameActivity.this).getOrderItemList(currentOrder);
			handler.sendMessage(new Message());
		}

		@Override
		public boolean handleMessage(Message msg) {
			randomGamePage = new RandomGamePage(RandomGameActivity.this, currentOrder);
			progressDialog.cancel();
			return true;
		}
	}

	@Override
	protected void onRestart() {
		if (randomGamePage != null) {
			randomGamePage.onRestart();
		}
		super.onRestart();
	}

	@Override
	protected void onResume() {
		if (randomGamePage != null) {
			randomGamePage.onResume();
		}
		super.onResume();
	}

	@Override
	public void onConfigurationChanged(Configuration newConfig) {
		randomGamePage.onConfigurationChanged(newConfig);
		super.onConfigurationChanged(newConfig);
	}

}
