package com.king.app.fileencryption;

import com.king.app.fileencryption.TabActionBar.MenuListener;
import com.king.app.fileencryption.TabActionBar.OnTabSelectedListener;
import com.king.app.fileencryption.controller.AccessController;
import com.king.app.fileencryption.controller.FileManagerAction;
import com.king.app.fileencryption.controller.PageSwitcher;
import com.king.app.fileencryption.controller.PictureManagerUpdate;
import com.king.app.fileencryption.controller.ThemeManager;
import com.king.app.fileencryption.controller.AccessController.IdentityCheckListener;
import com.king.app.fileencryption.data.Constants;
import com.king.app.fileencryption.filemanager.view.FileManagerPage;
import com.king.app.fileencryption.service.EncryptCheckService;
import com.king.app.fileencryption.setting.SettingActivity;
import com.king.app.fileencryption.setting.SettingMemo;
import com.king.app.fileencryption.setting.SettingProperties;
import com.king.app.fileencryption.slidingmenu.SlidingMenuAbstract;
import com.king.app.fileencryption.slidingmenu.SlidingMenuCreator;
import com.king.app.fileencryption.slidingmenu.SlidingMenuLeft;
import com.king.app.fileencryption.slidingmenu.SlidingMenuRight;
import com.king.app.fileencryption.slidingmenu.SlidingMenuTwoWay;
import com.king.app.fileencryption.slidingmenu.SlidingMenuTwoWay.OnSlideChagedListener;
import com.king.app.fileencryption.sorder.entity.SOrder;
import com.king.app.fileencryption.sorder.entity.SOrderParcelble;
import com.king.app.fileencryption.sorder.view.SOrderPage;
import com.king.app.fileencryption.spicture.view.SPicturePage;
import com.king.app.fileencryption.util.DisplayHelper;
import com.king.app.fileencryption.wall.WallActivity;
import com.king.app.fileencryption.wall.update.NewWallActivity;

import android.app.Activity;
import android.app.AlertDialog;
import android.app.ProgressDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.res.Configuration;
import android.graphics.Bitmap;
import android.graphics.drawable.BitmapDrawable;
import android.graphics.drawable.Drawable;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.os.Handler.Callback;
import android.util.Log;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.animation.AlphaAnimation;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;
import android.widget.Button;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.Toast;
import android.widget.PopupMenu.OnMenuItemClickListener;

@Deprecated
public class FileManagerActivityUpdate extends Activity
		implements PageSwitcher, OnTabSelectedListener, MenuListener, OnMenuItemClickListener
		, OnSlideChagedListener, Callback {

	private final String TAG = "FileManagerActivity";

	private EncryptCheckService encryptCheckService;
	private FileManagerAction managerAction;
	private FileManagerPage fileManagerPage;
	private SOrderPage sOrderPage;
	private SPicturePage sPicturePage;

	private ProgressDialog progressDialog;

	private LinearLayout mainViewLayout;

	private TabActionBar actionBar;
	private LinearLayout fileManagerView, sorderView, spictureView;
	private View currentView;

	private LinearLayout accessPrivateLayout;
	private EditText accessPrivateEdit;
	private Button accessPrivateButton;

	private LinearLayout menuLayout;
	private LinearLayout menuLayoutRight;
	private SlidingMenuAbstract slidingMenu;
	private int currentSlidingMode;
	private boolean isSlidingEnable;
	private Bitmap slidingBackground;
	private boolean isTwoWayMenu;
	private View backgroundView;
	private SlidingMenuCreator slidingMenuCreator;

	@Override
	protected void onCreate(Bundle savedInstanceState) {

		setTheme(new ThemeManager(this).getDefaultTheme());
		super.onCreate(savedInstanceState);
		DisplayHelper.enableFullScreen(this);
		DisplayHelper.disableScreenshot(this);

		//sliding feature check
		isSlidingEnable = SettingProperties.isMainViewSlidingEnable(this)
				&& AccessController.getInstance().getAccessMode() > AccessController.ACCESS_MODE_PRIVATE;
		if (isSlidingEnable) {
			currentSlidingMode = SettingProperties.getSlidingMenuMode(this);
			if (currentSlidingMode == SettingProperties.SLIDINGMENU_LEFT) {
				setContentView(R.layout.mainview_slidingmenu_left);
			}
			else if (currentSlidingMode == SettingProperties.SLIDINGMENU_RIGHT) {
				setContentView(R.layout.mainview_slidingmenu_right);
			}
			else if (currentSlidingMode == SettingProperties.SLIDINGMENU_TWOWAY) {
				setContentView(R.layout.mainview_slidingmenu_twoway);
				isTwoWayMenu = true;
			}

			menuLayout = (LinearLayout) findViewById(R.id.mainview_menu_layout);
			slidingMenu = (SlidingMenuAbstract) findViewById(R.id.mainview_slidingmenu);
			if (isTwoWayMenu) {
				menuLayoutRight = (LinearLayout) findViewById(R.id.mainview_menu_layout_right);
				((SlidingMenuTwoWay) slidingMenu).addOnSlideChangedListener(this);
				backgroundView = findViewById(R.id.mainview_bk_layout);
			}
			else {
				backgroundView = slidingMenu;
			}
		}
		else {
			setContentView(R.layout.mainview);
		}

		mainViewLayout = (LinearLayout) findViewById(R.id.fe_mainview);
		actionBar = new TabActionBar(this, this);
		actionBar.setOnTabSelectedListener(this);
		fileManagerView = (LinearLayout) findViewById(R.id.layout_page_filemanager);
		sorderView = (LinearLayout) findViewById(R.id.layout_page_sorder);
		spictureView = (LinearLayout) findViewById(R.id.layout_page_spicture_gallery);

		if (AccessController.getInstance().getAccessMode() != AccessController.ACCESS_MODE_FILEMANAGER) {
			android.content.res.Configuration configuration = getResources().getConfiguration();
			if (configuration.orientation == android.content.res.Configuration.ORIENTATION_PORTRAIT) {
				//disable separate tab situation to show more icon 
//				if (DisplayHelper.isTabModel(this)) {
//					actionBar.setLandModeTab(true);
//				}
//				else {
				actionBar.setLandModeTab(false);
//				}
			}
			else {
				actionBar.setLandModeTab(true);
			}
		}
		else {
			actionBar.hideTab();
		}

		showFileManagerPage();
		// v5.10.6, not ideal, need first showFileManagerPage then show showSorderPage, otherwise view is wrong
		String initPage = getIntent().getStringExtra("init_page");
		if (initPage != null) {//init from reload
			showSorderPage();
		}

		encryptCheckService = new EncryptCheckService(this);
		encryptCheckService.check();

		progressDialog = new ProgressDialog(this);
		progressDialog.setMessage(getResources().getString(R.string.checking));
	}

	private void computeActionBarLayout() {
		//need add after show page, as actionbar icon added completely after that
		if (getResources().getConfiguration().orientation == android.content.res.Configuration.ORIENTATION_PORTRAIT) {
			actionBar.onVertical();
		}
		else {
			actionBar.onLandscape();
		}

	}

	@Override
	public boolean handleMessage(Message msg) {

		if (msg.what == EncryptCheckService.SERVICE_CHECK) {
			boolean isExist = msg.getData().getBoolean("existed");
			if (isExist) {
				int size = msg.getData().getInt("size");
				String text = getResources().getString(R.string.encrypt_check_service_isexist);
				text = String.format(text, size);
				new AlertDialog.Builder(FileManagerActivityUpdate.this)
						.setTitle(R.string.warning)
						.setMessage(text)
						.setPositiveButton(R.string.ok, new DialogInterface.OnClickListener() {

							@Override
							public void onClick(DialogInterface dialog, int which) {
								progressDialog.show();
								encryptCheckService.encrypt();
							}
						})
						.setNegativeButton(R.string.cancel, null)
						.show();
			}
			else {
				Toast.makeText(FileManagerActivityUpdate.this, R.string.encrypt_check_service_isnotexist, Toast.LENGTH_LONG).show();
			}
		}
		else if (msg.what == EncryptCheckService.SERVICE_ENCRYPT) {
			Toast.makeText(FileManagerActivityUpdate.this, R.string.encrypt_check_service_encryptok, Toast.LENGTH_LONG).show();
		}
		if (progressDialog.isShowing()) {
			progressDialog.dismiss();
		}
		return true;
	}

	@Override
	public void createMenu(MenuInflater menuInflater, Menu menu) {
		menuInflater.inflate(R.menu.file_manager_update, menu);
		managerAction.onCreateOptionsMenu(menu);
	}

	@Override
	public void onPrepareMenu(MenuInflater menuInflater, Menu menu) {
		managerAction.onPrepareOptionsMenu(menu);
	}

	@Override
	public OnMenuItemClickListener getMenuItemListener() {

		return this;
	}

	@Override
	public boolean onMenuItemClick(MenuItem item) {

		return onOptionsItemSelected(item);
	}

	public OnClickListener slidingMenuListener = new OnClickListener() {

		@Override
		public void onClick(View v) {
			switch (v.getId()) {
				case R.string.menu_exit:
					Log.i(TAG, "menu -> exit");
					finish();
					break;
				case R.string.menu_mode_switch:
					AccessController controller = AccessController.getInstance();
					if (controller.getAccessMode() > AccessController.ACCESS_MODE_PRIVATE) {
						AccessController.getInstance().changeAccessMode(AccessController.ACCESS_MODE_PRIVATE);
						checkIdentity(false);
						actionBar.setPrivateMode();
					}
					if (isSlidingEnable) {
						slidingMenu.closeMenu();
						slidingMenu.enableScroll(false);
					}
					break;
				case R.string.menu_edit:
					startActivityForResult(new Intent().setClass(FileManagerActivityUpdate.this, SettingActivity.class), 0);
					break;
				case R.string.menu_check_all_unencrypted:
					progressDialog.show();
					encryptCheckService.check();
					break;
			}
		}
	};

	public boolean onOptionsItemSelected(MenuItem item) {
		// Handle action bar item clicks here. The action bar will
		// automatically handle clicks on the Home/Up button, so long
		// as you specify a parent activity in AndroidManifest.xml.
		if (item.getItemId() == R.id.menu_exit) {
			Log.i(TAG, "menu -> exit");
			finish();
		}
		else if (item.getItemId() == R.id.menu_mode_switch) {
			AccessController controller = AccessController.getInstance();
			if (controller.getAccessMode() > AccessController.ACCESS_MODE_PRIVATE) {
				AccessController.getInstance().changeAccessMode(AccessController.ACCESS_MODE_PRIVATE);
				checkIdentity(false);
				actionBar.setPrivateMode();
			}
			if (isSlidingEnable) {
				slidingMenu.closeMenu();
				slidingMenu.enableScroll(false);
			}
		}
		else if (item.getItemId() == R.id.menu_edit) {
			startActivityForResult(new Intent().setClass(FileManagerActivityUpdate.this, SettingActivity.class), 0);
		}
		else if (item.getItemId() == R.id.menu_check_all_unencrypted) {
			progressDialog.show();
			encryptCheckService.check();
		}
		else {
			managerAction.onOptionsItemSelected(item);
		}
		return super.onOptionsItemSelected(item);
	}

	@Override
	protected void onActivityResult(int requestCode, int resultCode, Intent data) {

		Log.i(TAG, "onActivityResult");
		SettingMemo memo = SettingMemo.getInstance();
		if (currentView == sorderView) {
			if (memo.isCascadeCoverChanged(this)) {
				sOrderPage.reloadCascadeNumber();
			}
		}
		boolean needReload = memo.isSlidingEnableChanged(this) || memo.isSlidingModeChanged(this);
		if (needReload) {
			reload(null);
		}
		super.onActivityResult(requestCode, resultCode, data);
	}

	protected void reload(String page) {
		Intent intent = getIntent();
		if (page != null) {
			intent.putExtra("init_page", page);
		}
		overridePendingTransition(0, 0);
		intent.addFlags(Intent.FLAG_ACTIVITY_NO_ANIMATION);

		PictureManagerUpdate.getInstance().destroy();
		finish();
		overridePendingTransition(0, 0);
		startActivity(intent);
	}

	@Override
	public void onBackPressed() {
		managerAction.onBackPressed();
	}

	@Override
	public void onConfigurationChanged(Configuration newConfig) {
		Log.d(TAG, "onConfigurationChanged ");
		//disable separate tab situation to show more icon 
		//if (!DisplayHelper.isTabModel(this)) {
		if (newConfig.orientation == android.content.res.Configuration.ORIENTATION_PORTRAIT) {
			actionBar.setLandModeTab(false);
			actionBar.onVertical();
		}
		else {
			actionBar.setLandModeTab(true);
			actionBar.onLandscape();
		}

		//}
		managerAction.onConfigurationChanged(newConfig);
		super.onConfigurationChanged(newConfig);

		if (isSlidingEnable) {
			slidingMenu.requestLayout();//key
			if (SettingProperties.getSlidingMenuMode(this) == SettingProperties.SLIDINGMENU_LEFT) {
				slidingMenuCreator.onConfigurationChanged(menuLayout);
				if (newConfig.orientation == android.content.res.Configuration.ORIENTATION_PORTRAIT) {
					updateBackground(slidingMenuCreator.loadBackground(SlidingMenuLeft.BK_KEY));
				}
				else {
					updateBackground(slidingMenuCreator.loadBackground(SlidingMenuLeft.BK_KEY + "_landscape"));
				}
			}
			else if (SettingProperties.getSlidingMenuMode(this) == SettingProperties.SLIDINGMENU_RIGHT) {
				slidingMenuCreator.onConfigurationChanged(menuLayout);
				if (newConfig.orientation == android.content.res.Configuration.ORIENTATION_PORTRAIT) {
					updateBackground(slidingMenuCreator.loadBackground(SlidingMenuRight.BK_KEY));
				}
				else {
					updateBackground(slidingMenuCreator.loadBackground(SlidingMenuRight.BK_KEY + "_landscape"));
				}
			}
			else if (SettingProperties.getSlidingMenuMode(this) == SettingProperties.SLIDINGMENU_TWOWAY) {
				slidingMenuCreator.onConfigurationChanged(menuLayout);
				slidingMenuCreator.onConfigurationChanged(menuLayoutRight);
				if (newConfig.orientation == android.content.res.Configuration.ORIENTATION_PORTRAIT) {
					updateBackground(slidingMenuCreator.loadBackground(SlidingMenuLeft.BK_KEY));
				}
				else {
					updateBackground(slidingMenuCreator.loadBackground(SlidingMenuLeft.BK_KEY + "_landscape"));
				}
			}

			//v5.8.9 fix position offset when screen orientation changed
			new Handler().postDelayed(new Runnable() {

				@Override
				public void run() {
					slidingMenu.closeMenu();
				}
			}, 100);//必须delay, 否则没有效果。因为UI线程有时间差
		}
	}

	@Override
	public void showFileManagerPage() {

		checkIdentity(false);

		if (currentView == fileManagerView) {
			return;
		}

		showSwitchAnimation();
		recycleSPictureResource();

		if (currentView == null) {//first in
			currentView = fileManagerView;
			currentView.setVisibility(View.VISIBLE);
		}
		else {

			if (managerAction != null) {
				managerAction.saveState();
			}
			currentView.setVisibility(View.GONE);
			currentView = fileManagerView;
			currentView.setVisibility(View.VISIBLE);

		}

		actionBar.setTitle(getResources().getString(R.string.tab_filemanager));

		if (fileManagerPage == null) {
			fileManagerPage = new FileManagerPage(this, this);
		}
		else {
			//fileManagerPage.reInit();
		}
		managerAction = fileManagerPage;
		managerAction.setActionBar(actionBar);
		if (isSlidingEnable) {
			slidingMenu.enableScroll(true);
			if (isTwoWayMenu) {
				slidingMenuCreator = managerAction.loadTwoWayMenu(menuLayout, menuLayoutRight);
				Bitmap bitmap = null;
				if (getResources().getConfiguration().orientation == android.content.res.Configuration.ORIENTATION_PORTRAIT) {
					bitmap = slidingMenuCreator.loadBackground(SlidingMenuLeft.BK_KEY);
				}
				else {
					bitmap = slidingMenuCreator.loadBackground(SlidingMenuLeft.BK_KEY + "_landscape");
				}
				updateBackground(bitmap);
			}
			else {
				slidingMenuCreator = managerAction.loadMenu(menuLayout);
				Bitmap bitmap = null;
				if (getResources().getConfiguration().orientation == android.content.res.Configuration.ORIENTATION_PORTRAIT) {
					bitmap = slidingMenuCreator.loadBackground(SlidingMenuAbstract.MENU_BK_KEY);
				}
				else {
					bitmap = slidingMenuCreator.loadBackground(SlidingMenuAbstract.MENU_BK_KEY + "_landscape");
				}
				updateBackground(bitmap);
			}
		}

		computeActionBarLayout();

		recycleSOrderResource();
		recycleSPictureResource();
	}

	public void showSwitchAnimation() {
		if (currentView != null) {
			mainViewLayout.removeView(currentView);
			mainViewLayout.addView(currentView);
			Animation animation = AnimationUtils.loadAnimation(this, R.anim.mainview_switch_page);
//			animation.setAnimationListener(new AnimationListener() {
//				
//				@Override
//				public void onAnimationStart(Animation animation) {
//				}
//				
//				@Override
//				public void onAnimationRepeat(Animation animation) {
//				}
//				
//				@Override
//				public void onAnimationEnd(Animation animation) {
//				}
//			});
			currentView.startAnimation(animation);

		}
	}

	@Override
	public void showSorderPage() {
		checkIdentity(false);

		if (currentView == sorderView) {
			return;
		}

		showSwitchAnimation();
		recycleSPictureResource();

		if (managerAction != null) {
			managerAction.saveState();
		}

		if (currentView == null) {//first in
			currentView = sorderView;
			currentView.setVisibility(View.VISIBLE);
		}
		else {
			currentView.setVisibility(View.GONE);
			currentView = sorderView;
			currentView.setVisibility(View.VISIBLE);
		}
		actionBar.setTitle(getResources().getString(R.string.tab_sorder));

		if (sOrderPage == null) {
			sOrderPage = new SOrderPage(this, this);
		}
		else {
			//sOrderPage.reInit();
		}
		managerAction = sOrderPage;
		managerAction.setActionBar(actionBar);
		if (isSlidingEnable) {
			slidingMenu.enableScroll(true);
			if (isTwoWayMenu) {
				slidingMenuCreator = managerAction.loadTwoWayMenu(menuLayout, menuLayoutRight);
				Bitmap bitmap = null;
				if (getResources().getConfiguration().orientation == android.content.res.Configuration.ORIENTATION_PORTRAIT) {
					bitmap = slidingMenuCreator.loadBackground(SlidingMenuLeft.BK_KEY);
				}
				else {
					bitmap = slidingMenuCreator.loadBackground(SlidingMenuLeft.BK_KEY + "_landscape");
				}
				updateBackground(bitmap);
			}
			else {
				slidingMenuCreator = managerAction.loadMenu(menuLayout);
				Bitmap bitmap = null;
				if (getResources().getConfiguration().orientation == android.content.res.Configuration.ORIENTATION_PORTRAIT) {
					bitmap = slidingMenuCreator.loadBackground(SlidingMenuAbstract.MENU_BK_KEY);
				}
				else {
					bitmap = slidingMenuCreator.loadBackground(SlidingMenuAbstract.MENU_BK_KEY + "_landscape");
				}
				updateBackground(bitmap);
			}
		}

		computeActionBarLayout();

		recycleSPictureResource();
	}

	public void showSpicturePage(SOrder order) {

		checkIdentity(false);
		if (currentView == spictureView) {
			return;
		}

		showSwitchAnimation();
		if (managerAction != null) {
			managerAction.saveState();
		}

		currentView.setVisibility(View.GONE);
		currentView = spictureView;
		currentView.setVisibility(View.VISIBLE);;

		//actionbar title decided by show mode
		if (sPicturePage == null) {
			sPicturePage = new SPicturePage(this, order);
		}
		else {
			sPicturePage.reInit();
		}
		managerAction = sPicturePage;
		managerAction.setActionBar(actionBar);
		if (isSlidingEnable) {
			slidingMenu.enableScroll(false);
			//managerAction.loadMenu(slidingMenu, menuLayout);
		}

		computeActionBarLayout();

		recycleSOrderResource();
	}
	@Override
	public void showSpicturePage() {

		checkIdentity(false);
		if (currentView == spictureView) {
			return;
		}

		showSwitchAnimation();
		if (managerAction != null) {
			managerAction.saveState();
		}

		currentView.setVisibility(View.GONE);
		currentView = spictureView;
		currentView.setVisibility(View.VISIBLE);;

		//actionbar title decided by show mode
		if (sPicturePage == null) {

			sPicturePage = new SPicturePage(this);
		}
		else {

			sPicturePage.reInit();
		}
		managerAction = sPicturePage;
		managerAction.setActionBar(actionBar);
		if (isSlidingEnable) {
			slidingMenu.enableScroll(false);
			//managerAction.loadMenu(slidingMenu, menuLayout);
		}

		computeActionBarLayout();

		recycleSOrderResource();
	}

	private void recycleSOrderResource() {
		if (sOrderPage != null) {
			//v5.9.3 it'll caused unstable bitmap recycle error, disable for current
			//sOrderPage.recycleResource();
		}
	}

	private void recycleSPictureResource() {
		if (sPicturePage != null) {
			//v5.9.3 it'll caused unstable bitmap recycle error, disable for current
			//sPicturePage.recycleResource();

			if (currentView == spictureView) {
				//v5.10.6 animation will refer resource, so delay recycle
				//animation time is defined as anim_mainview_switch_page
				new Handler().postDelayed(new Runnable() {

					@Override
					public void run() {
						if (currentView != spictureView) {//in case that user switch back to spicture page quickly
							sPicturePage.recycleResource();
						}
					}
				}, getResources().getInteger(R.integer.anim_mainview_switch_page) * 2);
			}
		}
	}

	@Override
	protected void onResume() {
		managerAction.onResume();
		super.onResume();
	}

	@Override
	protected void onStop() {
		Log.i(TAG, "onStop");
		//TODO no effort
		//changeRecentTaskThumbnail();
		super.onStop();
	}

	@Override
	protected void onRestart() {
		Log.i(TAG, "onRestart");
		AccessController controller = AccessController.getInstance();
		if (controller.getAccessMode() == AccessController.ACCESS_MODE_SUPERUSER) {
			managerAction.reInit();
			super.onRestart();
			return;
		}
		boolean needUnlock = false;
		boolean isPrivateMode = controller.getAccessMode() == AccessController.ACCESS_MODE_PRIVATE;
		if (!isPrivateMode) {
			needUnlock = true;
			controller.changeAccessMode(AccessController.ACCESS_MODE_PRIVATE);
		}
		Log.i(TAG, "onRestart needUnlock " + needUnlock);
		checkIdentity(needUnlock);
		managerAction.reInit();
		super.onRestart();
	}

	@Override
	public void switchToPictureOrderView(SOrder order) {
		Log.i(TAG, "switchToPictureOrderView " + order == null ? "order = null" : "");
		if (order != null) {
			if (sPicturePage != null) {
				sPicturePage.startOrderView(order);
				managerAction = sPicturePage;
				showSwitchAnimation();
				currentView.setVisibility(View.GONE);
				currentView = spictureView;
				currentView.setVisibility(View.VISIBLE);
				sPicturePage.reInit();
				sPicturePage.resetNoCurrentPath();
				actionBar.switchToSpicView();
				if (isSlidingEnable) {
					slidingMenu.enableScroll(false);
				}
				recycleSOrderResource();
			}
			else {
				showSpicturePage(order);
				actionBar.switchToSpicView();
			}
			computeActionBarLayout();
		}
	}

	@Override
	public void openRandomGame(SOrder order) {
		Log.i(TAG, "openRandomGame");
		Bundle bundle = new Bundle();
		bundle.putParcelable("sorder", new SOrderParcelble(order));
		Intent intent = new Intent();
		intent.putExtras(bundle);
		intent.setClass(this, RandomGameActivity.class);
		startActivity(intent);
	}

	@Override
	public void onBack() {
		if (accessPrivateLayout != null && accessPrivateLayout.getVisibility() == View.VISIBLE) {
			finish();
		}
		onBackPressed();
	}

	public void checkIdentity(final boolean needUnlock) {
		if (needUnlock) {
			Toast.makeText(this, R.string.restart_need_unlock, Toast.LENGTH_LONG).show();
		}
		if (AccessController.getInstance().getAccessMode() == AccessController.ACCESS_MODE_PRIVATE) {

			accessPrivateLayout = (LinearLayout) findViewById(R.id.layout_access_private);
			accessPrivateLayout.setVisibility(View.VISIBLE);
			accessPrivateEdit = (EditText) findViewById(R.id.access_private_pwd);
			accessPrivateEdit.setText("");
			accessPrivateButton = (Button) findViewById(R.id.access_private_ok);
			accessPrivateButton.setOnClickListener(new OnClickListener() {

				@Override
				public void onClick(View v) {
					if (accessPrivateEdit.getText().toString().equals("1010520")) {
						accessPrivateLayout.setVisibility(View.GONE);
						if (needUnlock) {
							AccessController.getInstance().changeAccessMode(AccessController.ACCESS_MODE_PUBLIC);
						}
					}
					else {
						Toast.makeText(FileManagerActivityUpdate.this, R.string.login_pwd_error, Toast.LENGTH_LONG).show();
					}
				}
			});
		}
	}

	public void unlockAndShowPage() {
		if (accessPrivateLayout != null && accessPrivateLayout.getVisibility() == View.VISIBLE) {
			accessPrivateLayout.setVisibility(View.GONE);
		}
	}

	@Override
	public void onUnlock() {
		AccessController.getInstance().showPwdDialog(this, new IdentityCheckListener() {

			@Override
			public void pass() {
				AccessController.getInstance().changeAccessMode(AccessController.ACCESS_MODE_PUBLIC);
				unlockAndShowPage();
				actionBar.setPublicMode();
				if (isSlidingEnable && managerAction != sPicturePage) {
					slidingMenu.enableScroll(true);
				}
			}

			@Override
			public void fail() {

			}

			@Override
			public void cancel() {

			}
		});
	}

	@Override
	public void openWallGallery(Bundle bundle) {
		Intent intent = new Intent();
		intent.putExtras(bundle);
//		intent.setClass(this, WallActivity.class);
		intent.setClass(this, NewWallActivity.class);
		startActivity(intent);
	}

	@Override
	/**
	 * only called in two-way sliding menu mode
	 */
	public void onSlideChange(final int direction) {

		new Handler().postDelayed(new Runnable() {

			@Override
			public void run() {
				Bitmap newBackBitmap = null;
				if (direction == OnSlideChagedListener.LEFT) {
					if (getResources().getConfiguration().orientation == android.content.res.Configuration.ORIENTATION_PORTRAIT) {
						newBackBitmap = slidingMenuCreator.loadBackground(SlidingMenuLeft.BK_KEY);
					}
					else {
						newBackBitmap = slidingMenuCreator.loadBackground(SlidingMenuLeft.BK_KEY + "_landscape");
					}
				}
				else {
					if (getResources().getConfiguration().orientation == android.content.res.Configuration.ORIENTATION_PORTRAIT) {
						newBackBitmap = slidingMenuCreator.loadBackground(SlidingMenuRight.BK_KEY);
					}
					else {
						newBackBitmap = slidingMenuCreator.loadBackground(SlidingMenuRight.BK_KEY + "_landscape");
					}
				}

				updateBackground(newBackBitmap);

				if (slidingBackground != null) {
					slidingBackground.recycle();
				}
				slidingBackground = newBackBitmap;
			}
		}, 0);
	}

	public void updateBackground(Bitmap bitmap) {
		if (bitmap == null) {
			backgroundView.setBackgroundResource(Constants.SLIDING_MENU_DEFAULT_BK);
		}
		else {
			Drawable drawable = new BitmapDrawable(getResources(), bitmap);
			backgroundView.setBackground(drawable);
		}
		Animation animation = new AlphaAnimation(0.5f, 1);
		animation.setDuration(1000);
		backgroundView.setAnimation(animation);
	}

	@Override
	public void onColor() {
		// TODO Auto-generated method stub

	}

}
