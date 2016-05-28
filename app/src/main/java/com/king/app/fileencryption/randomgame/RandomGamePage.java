package com.king.app.fileencryption.randomgame;

import com.king.app.fileencryption.Application;
import com.king.app.fileencryption.R;
import com.king.app.fileencryption.controller.AccessController;
import com.king.app.fileencryption.controller.AccessController.IdentityCheckListener;
import com.king.app.fileencryption.controller.PictureManagerUpdate;
import com.king.app.fileencryption.publicview.ActionBar;
import com.king.app.fileencryption.publicview.ActionBar.ActionBarListener;
import com.king.app.fileencryption.randomgame.controller.RandomGameController;
import com.king.app.fileencryption.randomgame.controller.RandomGameUiController;
import com.king.app.fileencryption.sorder.entity.SOrder;

import android.app.Activity;
import android.content.Context;
import android.content.res.Configuration;
import android.graphics.Bitmap;
import android.os.Handler.Callback;
import android.os.Handler;
import android.os.Message;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.PopupMenu.OnMenuItemClickListener;
import android.widget.Toast;

@Deprecated
public class RandomGamePage implements OnClickListener, ActionBarListener, OnMenuItemClickListener {

	private Context context;
	private Chooser chooser;
	private VerticalChooser portraitChooser;
	private HorizontalChooser landscapeChooser;
	private SOrder currentOrder;

	private ImageView imageView;
	private Button startButton, restartButton, sceneButton;
	private LinearLayout resultLayout;
	private LinearLayout currentItemLayout;

	private int curLeftNumber;

	private RandomRules randomRules;
	private boolean isRandoming;
	private RandomGameController gameController;
	private RandomGameUiController uiController;

	private ImageView accessPrivateCover;
	private ActionBar actionBar;

	public RandomGamePage(Context context, SOrder currentOrder) {
		this.context = context;
		this.currentOrder = currentOrder;
		gameController = new RandomGameController();
		uiController = new RandomGameUiController(context);
		Configuration configuration = context.getResources().getConfiguration();
		onConfigurationChanged(configuration);
	}

	public void onConfigurationChanged(Configuration newConfig) {

		Activity view = (Activity) context;
		view.setContentView(Application.isLollipop() ? R.layout.layout_random_game_l : R.layout.layout_random_game);

		actionBar = new ActionBar(context, this);
		actionBar.addMenuIcon();

		imageView = (ImageView) view.findViewById(R.id.rgame_image_thread);
		accessPrivateCover = (ImageView) view.findViewById(R.id.rgame_private);
		startButton = (Button) view.findViewById(R.id.rgame_button_start);
		restartButton = (Button) view.findViewById(R.id.rgame_button_restart);
		sceneButton = (Button) view.findViewById(R.id.rgame_button_scene);
		resultLayout = (LinearLayout) view.findViewById(R.id.rgame_result_layout);
		startButton.setOnClickListener(this);
		restartButton.setOnClickListener(this);
		sceneButton.setOnClickListener(this);

		imageView.setImageBitmap(PictureManagerUpdate.getInstance().createSingleOrderCover(currentOrder.getCoverPath(), context));
		if (newConfig.orientation == Configuration.ORIENTATION_PORTRAIT) {
			if (landscapeChooser == null) {
				landscapeChooser = new HorizontalChooser(context, currentOrder.getImgPathList());
				chooser = landscapeChooser;//注意顺序
			}
			else {
				chooser = landscapeChooser;//注意顺序
				chooser.reInit();
			}
		}
		else if (newConfig.orientation == Configuration.ORIENTATION_LANDSCAPE) {
			if (portraitChooser == null) {
				portraitChooser = new VerticalChooser(context, currentOrder.getImgPathList());
				chooser = portraitChooser;//注意顺序
			}
			else {
				chooser = portraitChooser;//注意顺序
				chooser.reInit();
			}
		}
	}

	private boolean isRandoming() {
		return isRandoming;
	}

	private void startRandoming() {
		isRandoming = true;
	}

	private void stopRandoming() {
		isRandoming = false;
		int index = gameController.randomSelect();
		if (index == -1) {
			Toast.makeText(context, R.string.rgame_no_random_item, Toast.LENGTH_LONG).show();
		}
		else {
			Bitmap bitmap = null;
			try {
				bitmap = PictureManagerUpdate.getInstance().getSpictureItem(currentOrder.getImgPathList().get(index), context);
				imageView.setImageBitmap(bitmap);
			} catch (Exception e) {
				Toast.makeText(context, R.string.login_pwd_error, Toast.LENGTH_LONG).show();
			}

			doReplaceOrFollowby(currentOrder, index);
		}
	}

	private void doReplaceOrFollowby(SOrder order, int index) {
		if (!uiController.hasRegistedRule()) {
			uiController.registRule(randomRules);
		}
		if (randomRules.isReplace()) {
			if (randomRules.getNumber() == 1) {
				currentItemLayout = uiController.replaceResult(resultLayout, order, index);
			}
			else {
				if (curLeftNumber == randomRules.getNumber()) {
					currentItemLayout = uiController.replaceResult(resultLayout, order, index);
				}
				else {
					uiController.followbyCurrentItem(currentItemLayout, order, index);
				}
			}
			reduceLeftNumber();
		}
		else {
			if (randomRules.getNumber() == 1) {
				uiController.followbyResult(resultLayout, order, index);
			}
			else {
				if (curLeftNumber == randomRules.getNumber()) {
					currentItemLayout = uiController.followbyResult(resultLayout, order, index);
				}
				else {
					uiController.followbyCurrentItem(currentItemLayout, order, index);
				}
				reduceLeftNumber();
			}
		}
	}

	private void reduceLeftNumber() {
		curLeftNumber --;
	}

	private void restoreLeftNumber() {
		if (randomRules != null) {
			curLeftNumber = randomRules.getNumber();
		}
	}

	@Override
	public void onClick(View v) {
		if (v == startButton) {
			if (randomRules == null) {
				startButton.setText(context.getResources().getString(R.string.rgame_start));
				Toast.makeText(context, R.string.rgame_set_rule_first, Toast.LENGTH_LONG).show();
				return;
			}
			if (randomRules.isImgThreadOn()) {
				if (startButton.getText().equals(context.getResources().getString(R.string.rgame_start))) {
					startButton.setText(context.getResources().getString(R.string.rgame_stop));
					startRandoming();
					new RandomThread().start();
				}
				else if (startButton.getText().equals(context.getResources().getString(R.string.rgame_stop))){
					stopRandoming();
					if (randomRules.getNumber() > 1 && curLeftNumber > 0) {
						startButton.setText(context.getResources().getString(R.string.rgame_continue));
					}
					else {
						startButton.setText(context.getResources().getString(R.string.rgame_start));
					}
					if (curLeftNumber == 0) {
						restoreLeftNumber();
					}
				}
				else if (startButton.getText().equals(context.getResources().getString(R.string.rgame_continue))) {
					startButton.setText(context.getResources().getString(R.string.rgame_stop));
					startRandoming();
					new RandomThread().start();
				}
			}
			else {
				if (!gameController.hasRegistedRules()) {
					gameController.registRule(randomRules);
					gameController.registRange(0, currentOrder.getItemNumber() - 1);
					gameController.startOneCircle();
				}
				stopRandoming();
			}
		}
		else if (v == restartButton) {
			if (isRandoming()) {
				stopRandoming();
			}
			startButton.setText(context.getResources().getString(R.string.rgame_start));
			gameController.closeOneCircle();
			gameController.cancelRegist();
			uiController.cancelRegistRule();
			resultLayout.removeAllViews();
			restoreLeftNumber();
		}
		else if (v == sceneButton) {
			if (randomRules.isScreenOn()) {
				if (currentItemLayout != null) {
					if (randomRules.getNumber() == curLeftNumber) {//curLeftNumber已还原到最初
						uiController.followbyCurrentItem(currentItemLayout, gameController.getRandomScene());
					}
					else {
						String msg = context.getResources().getString(R.string.rgame_img_random_unfinish);
						msg = String.format(msg, curLeftNumber);
						Toast.makeText(context, msg, Toast.LENGTH_LONG).show();
					}
				}
			}
		}
	}

	public void openSetRuleDialog() {
		new RandomGameRuleDialog(context, new RandomGameRuleDialog.OnRuleSelectListener() {

			@Override
			public void onSelectOk(RandomRules rules) {
				randomRules = rules;
				curLeftNumber = rules.getNumber();
				if (randomRules.isScreenOn()) {
					sceneButton.setVisibility(View.VISIBLE);
				}
				else {
					sceneButton.setVisibility(View.GONE);
				}
				Toast.makeText(context, R.string.rgame_rule_setok, Toast.LENGTH_LONG).show();
			}

			@Override
			public void onCancel() {

			}
		}).show();;
	}

	private class RandomThread extends Thread implements Callback {

		private Handler handler = new Handler(this);
		public void run() {
			while (isRandoming()) {
				try {
					Message message = new Message();
					handler.sendMessage(message);
					Thread.sleep(100);
				} catch (InterruptedException e) {
					e.printStackTrace();
					break;
				}
			}
		}
		@Override
		public boolean handleMessage(Message msg) {

			if (!gameController.hasRegistedRules()) {
				gameController.registRule(randomRules);
				gameController.registRange(0, currentOrder.getItemNumber() - 1);
				gameController.startOneCircle();
			}
			int index = gameController.randomProcessing();
			if (index == -1) {
				stopRandoming();
			}
			else {
				try {
					imageView.setImageBitmap(
							PictureManagerUpdate.getInstance().getSpictureItem(currentOrder.getImgPathList().get(index), context));
				} catch (Exception e) {
					Toast.makeText(context, R.string.login_pwd_error, Toast.LENGTH_LONG).show();
				}
			}
			return true;
		}

	}

	public void onResume() {
		if (isRandoming()) {
			isRandoming = false;
		}
	}

	public void onRestart() {
		if (AccessController.getInstance().getAccessMode() == AccessController.ACCESS_MODE_PRIVATE) {
			accessPrivateCover.setVisibility(View.VISIBLE);
			AccessController.getInstance().showPwdDialog(context, new IdentityCheckListener() {

				@Override
				public void pass() {
					accessPrivateCover.setVisibility(View.GONE);
				}

				@Override
				public void fail() {
					Toast.makeText(context, R.string.rgame_check_error_exit, Toast.LENGTH_LONG).show();
					((Activity) context).finish();
				}

				@Override
				public void cancel() {
					((Activity) context).finish();
				}
			});
		}
	}

	@Override
	public void onBack() {
		// TODO Auto-generated method stub

	}

	@Override
	public void onDelete() {
		// TODO Auto-generated method stub

	}

	@Override
	public void onRefresh() {
		// TODO Auto-generated method stub

	}

	@Override
	public void onIconClick(View view) {
		// TODO Auto-generated method stub

	}

	@Override
	public void createMenu(MenuInflater menuInflater, Menu menu) {
		menuInflater.inflate(R.menu.random_game, menu);
	}

	@Override
	public void onPrepareMenu(MenuInflater menuInflater, Menu menu) {

	}

	@Override
	public OnMenuItemClickListener getMenuItemListener() {

		return this;
	}

	@Override
	public void onTextChanged(String text, int start, int before, int count) {
		// TODO Auto-generated method stub

	}

	@Override
	public boolean onMenuItemClick(MenuItem item) {
		switch (item.getItemId()) {
			case R.id.menu_random_rule:
				openSetRuleDialog();
				break;

			default:
				break;
		}
		return true;
	}

}
