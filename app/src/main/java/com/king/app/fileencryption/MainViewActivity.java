package com.king.app.fileencryption;

import java.util.List;

import com.king.app.fileencryption.TabActionBar.MenuListener;
import com.king.app.fileencryption.TabActionBar.OnTabSelectedListener;
import com.king.app.fileencryption.controller.AccessController;
import com.king.app.fileencryption.controller.FragmentAction;
import com.king.app.fileencryption.controller.MainViewAction;
import com.king.app.fileencryption.controller.PictureManagerUpdate;
import com.king.app.fileencryption.controller.ThemeManager;
import com.king.app.fileencryption.controller.AccessController.IdentityCheckListener;
import com.king.app.fileencryption.data.Constants;
import com.king.app.fileencryption.fragment.FileManagerFragment;
import com.king.app.fileencryption.fragment.SOrderFragment;
import com.king.app.fileencryption.fragment.SPictureFragment;
import com.king.app.fileencryption.publicview.DragImageView;
import com.king.app.fileencryption.randomgame.team.TeamGameActivity;
import com.king.app.fileencryption.randomgame.update.GameActivity;
import com.king.app.fileencryption.res.AppResManager;
import com.king.app.fileencryption.res.AppResProvider;
import com.king.app.fileencryption.res.ColorRes;
import com.king.app.fileencryption.res.JResource;
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
import com.king.app.fileencryption.util.DisplayHelper;
import com.king.app.fileencryption.util.ScreenUtils;
import com.king.app.fileencryption.wall.WallActivity;
import com.king.app.fileencryption.wall.update.NewWallActivity;
import com.king.lib.colorpicker.ColorPicker;
import com.king.lib.colorpicker.ColorPicker.OnColorPickerListener;
import com.king.lib.colorpicker.ColorPickerSelectionData;

import android.app.Activity;
import android.app.AlertDialog;
import android.app.Fragment;
import android.app.FragmentTransaction;
import android.app.ProgressDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.res.Configuration;
import android.graphics.Bitmap;
import android.graphics.drawable.BitmapDrawable;
import android.graphics.drawable.Drawable;
import android.os.Bundle;
import android.os.Handler;
import android.os.Handler.Callback;
import android.os.Message;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.animation.AlphaAnimation;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;
import android.view.animation.Animation.AnimationListener;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.Toast;
import android.widget.PopupMenu.OnMenuItemClickListener;

public class MainViewActivity extends Activity implements OnTabSelectedListener, FragmentAction
		, OnSlideChagedListener, MenuListener, OnMenuItemClickListener, Callback
		, OnColorPickerListener{

	private TabActionBar actionBar;

	private EncryptCheckService encryptCheckService;
	private ProgressDialog progressDialog;

	private MainViewAction mainViewAction;
	private FileManagerFragment fileManagerFragment;
	private SOrderFragment sOrderFragment;
	private SPictureFragment sPictureFragment;

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

	private ImageView animView;
	private DragImageView dragView;
	private int orientation;

	private ColorPicker colorPicker;

	@Override
	protected void onCreate(Bundle savedInstanceState) {

		setTheme(new ThemeManager(this).getDefaultTheme());
		super.onCreate(savedInstanceState);
		DisplayHelper.enableFullScreen(this);
		DisplayHelper.disableScreenshot(this);

		orientation = getResources().getConfiguration().orientation;

		//sliding feature check
		isSlidingEnable = SettingProperties.isMainViewSlidingEnable(this)
				&& AccessController.getInstance().getAccessMode() > AccessController.ACCESS_MODE_PRIVATE;
		if (isSlidingEnable) {
			currentSlidingMode = SettingProperties.getSlidingMenuMode(this);
			if (currentSlidingMode == SettingProperties.SLIDINGMENU_LEFT) {
				setContentView(Application.isLollipop() ? R.layout.mainview_slidingmenu_left_l : R.layout.mainview_slidingmenu_left);
			}
			else if (currentSlidingMode == SettingProperties.SLIDINGMENU_RIGHT) {
				setContentView(Application.isLollipop() ? R.layout.mainview_slidingmenu_right_l : R.layout.mainview_slidingmenu_right);
			}
			else if (currentSlidingMode == SettingProperties.SLIDINGMENU_TWOWAY) {
				setContentView(Application.isLollipop() ? R.layout.mainview_slidingmenu_twoway_l : R.layout.mainview_slidingmenu_twoway);
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
			setContentView(Application.isLollipop() ? R.layout.mainview_update_l : R.layout.mainview_update);
		}

		actionBar = new TabActionBar(this, this);
		actionBar.setOnTabSelectedListener(this);

		if (AccessController.getInstance().getAccessMode() != AccessController.ACCESS_MODE_FILEMANAGER) {
			android.content.res.Configuration configuration = getResources().getConfiguration();
			actionBar.setLandModeTab(configuration.orientation == android.content.res.Configuration.ORIENTATION_LANDSCAPE);
		}
		else {
			actionBar.hideTab();
		}

		animView = (ImageView) findViewById(R.id.page_anim_view);
		dragView = (DragImageView) findViewById(R.id.mainview_drag_browser);
		dragView.setImageResource(R.drawable.sbrowser_icon);
		int size = getResources().getDimensionPixelSize(R.dimen.mainview_browser_icon_size);
		dragView.fitImageSize(size, size);
		dragView.setOnClickListener(new View.OnClickListener() {

			@Override
			public void onClick(View view) {
				startBrowser();
			}
		});

		showFileManagerPage();

		encryptCheckService = new EncryptCheckService(this);
		encryptCheckService.check();

		progressDialog = new ProgressDialog(this);
		progressDialog.setMessage(getResources().getString(R.string.checking));

		showBrowserIcon();

		resetColors();
	}

	private void showBrowserIcon() {

		int actionbarHeight = getResources().getDimensionPixelSize(R.dimen.actionbar_height);
		if (getResources().getConfiguration().orientation ==
				android.content.res.Configuration.ORIENTATION_PORTRAIT) {
			actionbarHeight *= 2;
		}
		final int height = actionbarHeight;
		/**
		 * this is called in onCreate, so indexView's position is still unknown
		 * so make delay to wait it display, then the position would be available
		 */
		new Handler().postDelayed(new Runnable() {

			@Override
			public void run() {
				int x = ScreenUtils.getScreenWidth(MainViewActivity.this) - 50;
				int y = ScreenUtils.getScreenHeight(MainViewActivity.this)
						-  height - 50;
				int offset = dragView.getImageWidth();
				dragView.setPosition(x - offset, y - offset);
				dragView.setVisibility(View.VISIBLE);
			}
		}, 200);
	}

	private void startBrowser() {
		//startActivity(new Intent().setClass(this, MyBrowser.class));
		Intent intent = getPackageManager().getLaunchIntentForPackage("com.king.app.browser");
		intent.setAction(Intent.ACTION_MAIN);
		intent.addCategory(Intent.CATEGORY_LAUNCHER);
		startActivity(intent);
		//startAnotherApp("com.king.app.browser");
	}
	/**
	 *  这种方式启动的app会依托在本应用程序之上，即打开了browser后，再点击fileencryption，会出现browser的画面而不是fileencryption的界面
	 * @param packageName
	 */
	/*
	private void startAnotherApp(String packageName){
	    PackageInfo packageInfo = null;
	    try {
	      packageInfo = getPackageManager().getPackageInfo(packageName, 0);
	      if (packageInfo==null) {
	        System.out.println("packageInfo==null");
	      } else {
	        System.out.println("packageInfo!=null");
	      }
	    } catch (NameNotFoundException e) {
	      e.printStackTrace();
	    }


	    //<data android:scheme="app" android:host="jp.co.cybird.barcodefootballer/" />


	    Intent resolveIntent = new Intent(Intent.ACTION_MAIN, null);
	    //resolveIntent.setData(Uri.parse("app://jp.co.cybird.barcodefootballer/"));
	    resolveIntent.addCategory(Intent.CATEGORY_LAUNCHER);
	    resolveIntent.setPackage(packageInfo.packageName);
	    System.out.println("packageInfo.packageName="+packageInfo.packageName);

	    List<ResolveInfo> resolveInfoList =
	    getPackageManager().queryIntentActivities(resolveIntent, 0);

	    System.out.println("resolveInfoList.size()="+resolveInfoList.size());

	    ResolveInfo resolveInfo = resolveInfoList.iterator().next();
	    if (resolveInfo != null ) {
	      String activityPackageName = resolveInfo.activityInfo.packageName;
	      String className = resolveInfo.activityInfo.name;

	      Intent intent = new Intent(Intent.ACTION_MAIN);
	      intent.addCategory(Intent.CATEGORY_LAUNCHER);
	      ComponentName componentName = new ComponentName(activityPackageName, className);

	      intent.setComponent(componentName);
	      startActivity(intent);
	    }

	  }
	  */

	@Override
	public boolean handleMessage(Message msg) {

		if (msg.what == EncryptCheckService.SERVICE_CHECK) {
			boolean isExist = msg.getData().getBoolean("existed");
			if (isExist) {
				int size = msg.getData().getInt("size");
				String text = getResources().getString(R.string.encrypt_check_service_isexist);
				text = String.format(text, size);
				new AlertDialog.Builder(MainViewActivity.this)
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
				Toast.makeText(MainViewActivity.this, R.string.encrypt_check_service_isnotexist, Toast.LENGTH_LONG).show();
			}
		}
		else if (msg.what == EncryptCheckService.SERVICE_ENCRYPT) {
			Toast.makeText(MainViewActivity.this, R.string.encrypt_check_service_encryptok, Toast.LENGTH_LONG).show();
		}
		if (progressDialog.isShowing()) {
			progressDialog.dismiss();
		}
		return true;
	}

	@Override
	public void onBackPressed() {

		if (!mainViewAction.onBackPressed()) {
			//super.onBackPressed();//用fragment管理后，super方法不会直接finish而会在fragment的回退栈中切换
			finish();
		}
	}

	private void updateSlidingMenu() {
		if (isSlidingEnable) {
			slidingMenu.enableScroll(true);
			if (isTwoWayMenu) {
				slidingMenuCreator = mainViewAction.loadTwoWayMenu(menuLayout, menuLayoutRight);
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
				slidingMenuCreator = mainViewAction.loadMenu(menuLayout);
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
	}

	@Override
	public void showFileManagerPage() {

		if (fileManagerFragment == null) {
			fileManagerFragment = new FileManagerFragment();
			fileManagerFragment.setFragmentAction(this);
		}
		else {
			if (mainViewAction == fileManagerFragment.getMainViewAction()) {//重复点击当前页面
				return;
			}

			showSwitchAnimation();
		}

		FragmentTransaction transaction = getFragmentManager().beginTransaction();
		transaction.replace(R.id.fragment_page, fileManagerFragment);
		transaction.addToBackStack(null);
		transaction.commit();

	}

	@Override
	public void showSorderPage() {

		if (sOrderFragment == null) {
			sOrderFragment = new SOrderFragment();
			sOrderFragment.setFragmentAction(this);
		}
		else {
			if (mainViewAction == sOrderFragment.getMainViewAction()) {//重复点击当前页面
				return;
			}

			showSwitchAnimation();

		}
		FragmentTransaction transaction = getFragmentManager().beginTransaction();
		transaction.replace(R.id.fragment_page, sOrderFragment);
		transaction.addToBackStack(null);
		transaction.commit();
	}

	@Override
	public void showSpicturePage() {

		String folder = fileManagerFragment.getCurrentPath();
		if (sPictureFragment == null) {
			showSpicturePage(folder, null);
		}
		else {

			if (mainViewAction == sPictureFragment.getMainViewAction()) {//重复点击当前页面
				return;
			}

			showSwitchAnimation();

			if (folder.equals(sPictureFragment.getCurrentPath())) {
				//just re-initial
				showSpicturePage(null, null);
			}
			else {
				if (sPictureFragment.isFolderMode()) {
					showSpicturePage(folder, null);
				}
				else {
					//just re-initial
					showSpicturePage(null, null);
					//update path
					sPictureFragment.updateCurrentPath(folder);
				}
			}
		}
	}

	/**
	 *
	 * @param folder if not null, init folder's file item data
	 * @param sOrder if not null, init order's item data
	 */
	private void showSpicturePage(String folder, SOrder order) {

		Bundle bundle = null;
		if (folder != null) {
			bundle = new Bundle();
			bundle.putString(Constants.KEY_SPICTURE_INIT_MODE, SPictureFragment.INIT_MODE_FOLDER);
			bundle.putString(Constants.KEY_SPICTURE_INIT_MODE_VALUE, folder);
		}
		else if (order != null) {
			bundle = new Bundle();
			bundle.putString(Constants.KEY_SPICTURE_INIT_MODE, SPictureFragment.INIT_MODE_ORDER);
			bundle.putInt(Constants.KEY_SPICTURE_INIT_MODE_VALUE, order.getId());
		}

		if (sPictureFragment == null) {
			sPictureFragment = new SPictureFragment();
			sPictureFragment.setFragmentAction(this);
			if (bundle != null) {
				sPictureFragment.setArguments(bundle);//只能在第一次，否则会报fragment alread active异常
			}
		}
		else {
			if (bundle != null) {
				sPictureFragment.reInit(bundle);
			}
		}

		FragmentTransaction transaction = getFragmentManager().beginTransaction();
		transaction.replace(R.id.fragment_page, sPictureFragment);
		transaction.addToBackStack(null);
		transaction.commit();
	}

	@Override
	public void onFragmentInitEnd(Fragment fragment) {
		checkIdentity(false);
		actionBar.setTitle(getResources().getString(R.string.tab_filemanager));

		if (fragment == fileManagerFragment) {
			mainViewAction = fileManagerFragment.getMainViewAction();
			mainViewAction.setActionBar(actionBar);
			updateSlidingMenu();
		}
		else if (fragment == sOrderFragment) {
			mainViewAction = sOrderFragment.getMainViewAction();
			mainViewAction.setActionBar(actionBar);
			updateSlidingMenu();
		}
		else if (fragment == sPictureFragment) {
			mainViewAction = sPictureFragment.getMainViewAction();
			mainViewAction.setActionBar(actionBar);
			if (isSlidingEnable) {
				slidingMenu.enableScroll(false);
			}
		}
		//after sub-page initial actionbar
		computeActionBarLayout();
	}

	@Override
	public void onConfigurationChanged(Configuration newConfig) {
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
		mainViewAction.onConfigurationChanged(newConfig);
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

		if (newConfig.orientation != orientation) {
			orientation = newConfig.orientation;
			showBrowserIcon();
		}
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
	public void createMenu(MenuInflater menuInflater, Menu menu) {
		menuInflater.inflate(R.menu.file_manager_update, menu);
		mainViewAction.onCreateOptionsMenu(menu);
	}

	@Override
	public void onPrepareMenu(MenuInflater menuInflater, Menu menu) {
		mainViewAction.onPrepareOptionsMenu(menu);
	}

	@Override
	public OnMenuItemClickListener getMenuItemListener() {

		return this;
	}

	@Override
	public boolean onMenuItemClick(MenuItem item) {

		if (item.getItemId() == R.id.menu_exit) {
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
			startActivityForResult(new Intent().setClass(MainViewActivity.this, SettingActivity.class), 0);
		}
		else if (item.getItemId() == R.id.menu_check_all_unencrypted) {
			progressDialog.show();
			encryptCheckService.check();
		}
		else {
			mainViewAction.onOptionsItemSelected(item);
		}
		return super.onOptionsItemSelected(item);
	}

	public OnClickListener slidingMenuListener = new OnClickListener() {

		@Override
		public void onClick(View v) {
			switch (v.getId()) {
				case R.string.menu_exit:
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
					startActivityForResult(new Intent().setClass(MainViewActivity.this, SettingActivity.class), 0);
					break;
				case R.string.menu_check_all_unencrypted:
					progressDialog.show();
					encryptCheckService.check();
					break;
			}
		}
	};

	@Override
	protected void onActivityResult(int requestCode, int resultCode, Intent data) {

		SettingMemo memo = SettingMemo.getInstance();
		boolean needReload = memo.isSlidingEnableChanged(this) || memo.isSlidingModeChanged(this)
				|| memo.isPageModeChanged(this) || memo.isCascadeCoverChanged(this);
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
						Toast.makeText(MainViewActivity.this, R.string.login_pwd_error, Toast.LENGTH_LONG).show();
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
				if (isSlidingEnable && mainViewAction != sPictureFragment) {
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

	public void openRandomGame() {
		Bundle bundle = new Bundle();
		Intent intent = new Intent();
		intent.putExtras(bundle);
		intent.setClass(this, GameActivity.class);
		startActivity(intent);
	}

	public void openTeamGame() {
		Bundle bundle = new Bundle();
		Intent intent = new Intent();
		intent.putExtras(bundle);
		intent.setClass(this, TeamGameActivity.class);
		startActivity(intent);
	}

	public void openWallGallery(Bundle bundle) {
		Intent intent = new Intent();
		intent.putExtras(bundle);
//		intent.setClass(this, WallActivity.class);
		intent.setClass(this, NewWallActivity.class);
		startActivity(intent);
	}

	public void switchToPictureOrderView(SOrder sOrder) {
		actionBar.switchToSpicView();
		showSwitchAnimation();
		showSpicturePage(null, sOrder);
	}

	public void showSwitchAnimation() {
		final Bitmap snapshot = ScreenUtils.snapShotView(findViewById(R.id.fragment_page));
		animView.setImageBitmap(snapshot);
		animView.setVisibility(View.VISIBLE);
		Animation animation = AnimationUtils.loadAnimation(this, R.anim.mainview_switch_page);
		animView.startAnimation(animation);
		animation.setAnimationListener(new AnimationListener() {

			@Override
			public void onAnimationStart(Animation arg0) {
			}

			@Override
			public void onAnimationRepeat(Animation arg0) {
			}

			@Override
			public void onAnimationEnd(Animation arg0) {
				animView.setVisibility(View.GONE);
				animView.setImageBitmap(null);
				snapshot.recycle();
			}
		});
	}

	@Override
	public boolean onContextItemSelected(MenuItem item) {

		if (mainViewAction == sOrderFragment) {
			sOrderFragment.onContextItemSelected(item);
		}
		return super.onContextItemSelected(item);
	}

	public boolean isSlidingMenuOpen() {

		return slidingMenu == null ? false:slidingMenu.isOpen();
	}

	@Override
	public void onColor() {
		if (colorPicker == null) {
			colorPicker = new ColorPicker(this, this);
			colorPicker.setResourceProvider(new AppResProvider(this));
		}
		colorPicker.setSelectionData(new AppResManager().getTabActionbarList(this));
		colorPicker.show();
	}

	@Override
	public void onColorChanged(String key, int newColor) {
		if (key.equals(ColorRes.TAB_ACTIONBAR_BK)) {
			actionBar.updateBackground(newColor);
		}
		else if (key.equals(ColorRes.TAB_ACTIONBAR_TEXT)) {
			actionBar.updateTextColor(newColor);
		}
		else if (key.equals(ColorRes.TAB_ACTIONBAR_TEXT_FOCUS)) {
			actionBar.updateTextFocusColor(newColor);
		}
	}

	@Override
	public void onColorSelected(int color) {

	}

	@Override
	public void onColorSelected(List<ColorPickerSelectionData> list) {
		for (ColorPickerSelectionData data:list) {
			JResource.updateColor(data.getKey(), data.getColor());
		}
		JResource.saveColorUpdate(this);
	}

	@Override
	public void onColorCancleSelect() {
		resetColors();
	}

	private void resetColors() {
		actionBar.resetColors();
	}

	@Override
	public void onApplyDefaultColors() {
		JResource.removeColor(ColorRes.TAB_ACTIONBAR_BK);
		JResource.removeColor(ColorRes.TAB_ACTIONBAR_TEXT);
		JResource.removeColor(ColorRes.TAB_ACTIONBAR_TEXT_FOCUS);
		JResource.saveColorUpdate(this);
		resetColors();
	}

}
