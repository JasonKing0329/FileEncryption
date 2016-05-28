package com.king.app.fileencryption.sorder.view;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

import com.king.app.fileencryption.Application;
import com.king.app.fileencryption.MainViewActivity;
import com.king.app.fileencryption.R;
import com.king.app.fileencryption.TabActionBar;
import com.king.app.fileencryption.book.BookActivity;
import com.king.app.fileencryption.controller.AccessController;
import com.king.app.fileencryption.controller.MainViewAction;
import com.king.app.fileencryption.controller.PictureManagerUpdate;
import com.king.app.fileencryption.controller.AccessController.IdentityCheckListener;
import com.king.app.fileencryption.data.Constants;
import com.king.app.fileencryption.data.DBInfor;
import com.king.app.fileencryption.publicview.CustomDialog;
import com.king.app.fileencryption.publicview.FullScreenSurfActivity;
import com.king.app.fileencryption.publicview.HorizontalIndexView;
import com.king.app.fileencryption.publicview.HorizontalIndexView.OnPageSelectListener;
import com.king.app.fileencryption.publicview.HorizontalIndexView.PageIndexOutOfBoundsException;
import com.king.app.fileencryption.setting.SettingProperties;
import com.king.app.fileencryption.slidingmenu.SlidingMenuCreator;
import com.king.app.fileencryption.sorder.controller.IndexCreator;
import com.king.app.fileencryption.sorder.controller.SOrderPictureBridge;
import com.king.app.fileencryption.sorder.controller.ScrollController;
import com.king.app.fileencryption.sorder.entity.SOrder;
import com.king.app.fileencryption.sorder.entity.SOrderCount;
import com.king.app.fileencryption.sorder.entity.STag;
import com.king.app.fileencryption.sorder.view.SOrderGridAdapter.CoverMode;
import com.king.app.fileencryption.surf.SurfActivity;
import com.king.app.fileencryption.surf.UiController;
import com.king.app.fileencryption.thumbfolder.ThumbFolderActivity;
import com.king.app.fileencryption.util.DisplayHelper;
import com.king.app.fileencryption.util.ScreenUtils;
import com.king.app.fileencryption.wall.WallActivity;

import android.app.Activity;
import android.app.AlertDialog;
import android.app.ProgressDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.res.Configuration;
import android.graphics.Bitmap;
import android.os.Bundle;
import android.os.Handler;
import android.os.Handler.Callback;
import android.os.Message;
import android.text.InputType;
import android.util.Log;
import android.view.ContextMenu;
import android.view.ContextMenu.ContextMenuInfo;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.View.OnCreateContextMenuListener;
import android.view.animation.Animation;
import android.view.animation.Animation.AnimationListener;
import android.view.animation.AnimationUtils;
import android.widget.AdapterView;
import android.widget.EditText;
import android.widget.ExpandableListView;
import android.widget.ImageView;
import android.widget.PopupMenu;
import android.widget.PopupMenu.OnMenuItemClickListener;
import android.widget.TextView;
import android.widget.ExpandableListView.ExpandableListContextMenuInfo;
import android.widget.ExpandableListView.OnChildClickListener;
import android.widget.GridView;
import android.widget.LinearLayout;
import android.widget.Toast;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.AdapterView.OnItemLongClickListener;

public class SOrderPageUpdate implements MainViewAction
		, OnItemClickListener, OnItemLongClickListener
		, OnChildClickListener, OnCreateContextMenuListener
		, OnPageSelectListener {

	private final String TAG = "SOrderPage";
	private Context context;

	private View view;
	private final int VIEW_GRID = 0;
	private final int VIEW_LIST = 1;
	private int currentView;

	private TabActionBar actionBar;
	private GridView gridView;
	private SOrderGridAdapter gridAdapter;
	private ProgressDialog progressDialog;
	private List<SOrder> currentPageOrders;
	private int currentPage;
	private ImageView previousPageView, nextPageView;

	private View pageItemContainer;
	private ImageView switchAnimView;
	private boolean showAnimation;
	private SOrderMenuDialog sOrderMenuDialog;

	private ExpandableListView expandableView;
	private SOrderExpandableListAdapter expandableAdapter;
	private List<STag> tagList;
	private List<List<SOrder>> orderListInExpandable;

	private SOrderPictureBridge bridge;

	private int currentOrderBy = -1;

	private HorizontalIndexView horizontalIndexView;
	private IndexCreator indexCreator;

	private ScrollController scrollController;

	public SOrderPageUpdate(Context context, View view) {
		this.context = context;
		this.view = view;
		bridge = SOrderPictureBridge.getInstance(context);
		currentView = VIEW_GRID;
		currentOrderBy = SettingProperties.getOrderMode(context);
		currentPage = 1;
		scrollController = new ScrollController(context);
		initGridViewElement();
		initGridView();
	}

	private void initGridView() {
		refresh();
	}

	private void initGridViewElement() {

		gridView = (GridView) view.findViewById(R.id.sorder_gridview);
		gridView.setOnItemClickListener(this);
		gridView.setOnItemLongClickListener(this);
		gridView.setOnScrollListener(scrollController);

		previousPageView = (ImageView) view.findViewById(R.id.sorder_previous_page);
		nextPageView = (ImageView) view.findViewById(R.id.sorder_next_page);

		if (currentView == VIEW_GRID && SettingProperties.isPageModeEnable(context)) {

			switchAnimView = (ImageView) view.findViewById(R.id.sorder_switch_anim_view);
			pageItemContainer = view.findViewById(R.id.sorder_page_view);
			//目的在于switch page时的动画，动画是通过snap shot截取layout view，加上背景效果更好
			pageItemContainer.setBackgroundColor(context.getResources().getColor(R.color.sorder_bk));

			horizontalIndexView = (HorizontalIndexView) view.findViewById(R.id.sorder_page_index);
			horizontalIndexView.setOnPageSelectListener(this);

			if (SettingProperties.isMainViewSlidingEnable(context)) {
				horizontalIndexView.setScroll(false);
			}

			int size = context.getResources().getDimensionPixelSize(R.dimen.page_index_fixgrid_item_width_large);
			int horPadding = context.getResources().getDimensionPixelSize(R.dimen.page_index_padding_hor);
			int minSize = context.getResources().getDimensionPixelSize(R.dimen.page_index_fixgrid_item_width_small);
			horizontalIndexView.setIndexSize(size, minSize);
			horizontalIndexView.setItemSpace(horPadding);
			horizontalIndexView.setMaxWidth(ScreenUtils.getScreenWidth(context));

			indexCreator = new IndexCreator(context, bridge);
		}
	}

	private void showPreNextGuide(boolean preEnable, boolean nextEnable) {
		previousPageView.setVisibility(View.VISIBLE);
		nextPageView.setVisibility(View.VISIBLE);
		previousPageView.setEnabled(preEnable);
		nextPageView.setEnabled(nextEnable);
		if (preEnable) previousPageView.setImageResource(R.drawable.previous_page);
		else previousPageView.setImageResource(R.drawable.previous_page_dis);
		if (nextEnable) nextPageView.setImageResource(R.drawable.next_page);
		else nextPageView.setImageResource(R.drawable.next_page_dis);
		previousPageView.setOnClickListener(pageIndexListener);
		nextPageView.setOnClickListener(pageIndexListener);
	}

	private void showProgress() {
		progressDialog = new ProgressDialog(context);
		progressDialog.setMessage(context.getResources().getString(R.string.loading));
		progressDialog.show();
	}

	private class LoadOrderThread extends Thread implements Callback {

		private Handler handler = new Handler(this);

		public LoadOrderThread() {

		}

		public void run() {
			Bundle bundle = new Bundle();
			if (currentOrderBy == SettingProperties.ORDER_BY_DATE) {//by date
				bridge.sortOrderByDate();
			}
			else {
				bridge.reloadOrders();
				//bridge.getOrderList();
			}
			bundle.putBoolean("result", true);
			Message message = new Message();
			message.setData(bundle);
			handler.sendMessage(message);
		}

		@Override
		public boolean handleMessage(Message msg) {

			Bundle bundle = msg.getData();
			if (currentOrderBy == SettingProperties.ORDER_BY_NAME) {//by name
				bridge.sortOrderByName();
			}
			else if (currentOrderBy == SettingProperties.ORDER_BY_ITEMNUMBER) {//by item number
				bridge.sortOrderByItemNumber();
			}
			boolean result = bundle.getBoolean("result");
			if (result) {
				if (SettingProperties.isPageModeEnable(context) && currentView == VIEW_GRID) {
					if (bridge.getOrderList() != null && bridge.getOrderList().size() != 0) {
						horizontalIndexView.setIndexList(indexCreator.createIndex());
					}
					initPagePreNextGuid();
					showAnimation = false;
					horizontalIndexView.select(currentPage);
				}
				else {
					currentPageOrders = bridge.getOrderList();
					notifyUpdate();
				}
			}
			progressDialog.cancel();
			return false;
		}

	}

	private void refresh() {
		showProgress();
		new LoadOrderThread().start();
	}

	public void initPagePreNextGuid() {
		if (horizontalIndexView.getPagesNumber() < 2) {
			showPreNextGuide(false, false);
		}
		else {
			if (currentPage == horizontalIndexView.getPagesNumber()) {
				showPreNextGuide(true, false);
			}
			else if (currentPage == 1) {
				showPreNextGuide(false, true);
			}
			else {
				showPreNextGuide(true, true);
			}
		}
	}

	private void notifyUpdate() {
		Log.d(TAG, "notifyUpdate");
		if (currentView == VIEW_GRID) {
			notifyGridViewUpdate();
			/*
			if (gridAdapter == null) {
				gridAdapter = new SOrderGridAdapter(context, bridge.getOrderList());
				gridView.setAdapter(gridAdapter);
			}
			else {
				gridAdapter.setSorderList(bridge.getOrderList());
				if (gridView.getAdapter() == null) {
					gridView.setAdapter(gridAdapter);
				}
				else {
					gridAdapter.notifyDataSetChanged();
				}
			}
			*/
		}
		else {
			if (horizontalIndexView != null) {
				horizontalIndexView.setVisibility(View.GONE);
			}

			loadTagList();
			suitExpandList();
			if (expandableAdapter == null) {
				expandableAdapter = new SOrderExpandableListAdapter(context, orderListInExpandable, tagList);
				expandableView.setAdapter(expandableAdapter);
			}
			else {
				expandableAdapter.setTagList(tagList);
				expandableAdapter.setListInExpand(orderListInExpandable);

				if (expandableView.getAdapter() == null) {
					expandableView.setAdapter(expandableAdapter);
				}
				else {
					expandableAdapter.notifyDataSetChanged();
				}
			}
		}
	}
	private void notifyGridViewUpdate() {
		Log.d(TAG, "notifyGridViewUpdate");
		if (horizontalIndexView != null) {
			if (SettingProperties.isPageModeEnable(context) && currentView == VIEW_GRID) {
				if (horizontalIndexView.getPagesNumber() < 2) {
					horizontalIndexView.setVisibility(View.GONE);
				}
				else {
					horizontalIndexView.setVisibility(View.VISIBLE);
					horizontalIndexView.show();
				}
			}
			else {
				horizontalIndexView.setVisibility(View.GONE);
			}
		}

		if (gridAdapter == null) {
			gridAdapter = new SOrderGridAdapter(context, currentPageOrders);
			gridView.setAdapter(gridAdapter);
			scrollController.setGridAdapter(gridAdapter);
		}

		gridAdapter.setSorderList(currentPageOrders);
		scrollController.notifyCoverDataChanged();//adapt covers

		if (gridView.getAdapter() == null) {
			gridView.setAdapter(gridAdapter);
		}
		else {
			gridAdapter.notifyDataSetChanged();
		}

		//horizontalIndexView.requestLayoutFixGrid();
	}

	OnClickListener pageIndexListener = new OnClickListener() {

		@Override
		public void onClick(View view) {
			if (view == previousPageView) {
				if (currentPage == 2) {
					previousPageView.setImageResource(R.drawable.previous_page_dis);
					previousPageView.setEnabled(false);
				}
				if (!nextPageView.isEnabled()) {
					nextPageView.setImageResource(R.drawable.next_page);
					nextPageView.setEnabled(true);
				}
				currentPage --;
			}
			else if (view == nextPageView) {
				if (currentPage == horizontalIndexView.getPagesNumber() - 1) {
					nextPageView.setImageResource(R.drawable.next_page_dis);
					nextPageView.setEnabled(false);
				}
				if (!previousPageView.isEnabled()) {
					previousPageView.setImageResource(R.drawable.previous_page);
					previousPageView.setEnabled(true);
				}
				currentPage ++;
			}
			else {
				currentPage = (Integer) view.getTag();
				if (currentPage == 1) {
					previousPageView.setImageResource(R.drawable.previous_page_dis);
					previousPageView.setEnabled(false);
					if (horizontalIndexView.getPagesNumber() > 1) {
						nextPageView.setImageResource(R.drawable.next_page_dis);
						nextPageView.setEnabled(false);
					}
					else {
						nextPageView.setImageResource(R.drawable.next_page);
						nextPageView.setEnabled(true);
					}
				}
				else if (currentPage == horizontalIndexView.getPagesNumber()) {
					nextPageView.setImageResource(R.drawable.next_page_dis);
					nextPageView.setEnabled(false);
					previousPageView.setImageResource(R.drawable.previous_page);
					previousPageView.setEnabled(true);
				}
				else {
					if (!previousPageView.isEnabled()) {
						previousPageView.setImageResource(R.drawable.previous_page);
						previousPageView.setEnabled(true);
					}
					if (!nextPageView.isEnabled()) {
						nextPageView.setImageResource(R.drawable.next_page);
						nextPageView.setEnabled(true);
					}
				}
			}
			horizontalIndexView.select(currentPage);
		}
	};

	@Override
	public void onSelect(int index) {
		try {
			currentPage = index;
			currentPageOrders = indexCreator.getPageItem(index);
			startSwitchAnimation();
			notifyUpdate();
		} catch (PageIndexOutOfBoundsException e) {
			Log.i("SOrderPage", "index(" + index + ") is out of bounds");
			e.printStackTrace();
		}
	}

	private void startSwitchAnimation() {

		if (showAnimation) {//防止第一次进入sorder page就snapshot
			if (currentView == VIEW_GRID && SettingProperties.isPageModeEnable(context)) {
				final Bitmap snapshot = ScreenUtils.snapShotView(pageItemContainer);
				switchAnimView.setImageBitmap(snapshot);
				switchAnimView.setVisibility(View.VISIBLE);
				Animation animation = AnimationUtils.loadAnimation(context, R.anim.sorder_switch_page);
				switchAnimView.startAnimation(animation);
				animation.setAnimationListener(new AnimationListener() {

					@Override
					public void onAnimationStart(Animation arg0) {
					}

					@Override
					public void onAnimationRepeat(Animation arg0) {
					}

					@Override
					public void onAnimationEnd(Animation arg0) {
						switchAnimView.setVisibility(View.GONE);
						switchAnimView.setImageBitmap(null);
						snapshot.recycle();
					}
				});
			}
		}
		showAnimation = true;
	}

	public void recycleResource() {
		PictureManagerUpdate.getInstance().recycleOrderPreview();
		PictureManagerUpdate.getInstance().recycleOrderCovers();
	}

	@Override
	public boolean onOptionsItemSelected(MenuItem item) {

		switch (item.getItemId()) {
		/*
		case R.id.menu_create_order:
			AccessController controller = AccessController.getInstance();
			if (controller.getAccessMode() == AccessController.ACCESS_MODE_PRIVATE) {
				controller.showPwdDialog(context, new IdentityCheckListener() {

					@Override
					public void pass() {
						openCreateOrderDialog();
					}

					@Override
					public void fail() {

					}

					@Override
					public void cancel() {

					}
				});
			}
			else {
				openCreateOrderDialog();
			}
			break;
		case R.id.menu_by_date:
			if (currentOrderBy != ORDER_BY_DATE) {
				currentOrderBy = ORDER_BY_DATE;
				bridge.sortOrderByDate();
				if (isPageModeEnable()) {
					choosePage(1);
				}
				else {
					currentPageOrders = bridge.getOrderList();
					notifyUpdate();
				}
			}
			break;
		case R.id.menu_by_name:
			if (currentOrderBy != ORDER_BY_NAME) {
				currentOrderBy = ORDER_BY_NAME;
				bridge.sortOrderByName();
				if (isPageModeEnable()) {
					choosePage(1);
				}
				else {
					currentPageOrders = bridge.getOrderList();
					notifyUpdate();
				}
			}
			break;
		 */
			case R.id.menu_view_grid:
				PictureManagerUpdate.getInstance().recycleExpandOrderCovers();
				showGridView();
				break;
			case R.id.menu_view_list:
				currentPageOrders = bridge.getOrderList();
				showExpandableView();
				break;
			/*
		case R.id.menu_thumb_folder:
			startThumbView();
			break;
			*/
			default:
				break;
		}
		return false;
	}

	@Override
	public SlidingMenuCreator loadMenu(LinearLayout menuLayout) {
		menuLayout.removeAllViews();
		SlidingMenuCreator creator = new SlidingMenuCreator(context, slidingMenuListener);
		creator.loadMenu(Constants.sorderMenu, menuLayout, SettingProperties.getSlidingMenuMode(context));
		return creator;
	}

	@Override
	public SlidingMenuCreator loadTwoWayMenu(LinearLayout menuLayout,
											 LinearLayout menuLayoutRight) {
		menuLayout.removeAllViews();
		menuLayoutRight.removeAllViews();

		SlidingMenuCreator slidingMenuCreator = new SlidingMenuCreator(context, slidingMenuListener);
		slidingMenuCreator.loadMenu(Constants.sorderMenu, menuLayout, SettingProperties.SLIDINGMENU_LEFT);
		slidingMenuCreator.loadMenu(Constants.sorderMenu, menuLayoutRight, SettingProperties.SLIDINGMENU_RIGHT);
		return slidingMenuCreator;
	}

	OnClickListener slidingMenuListener = new OnClickListener() {

		@Override
		public void onClick(View v) {
			//v6.3.6 when menu is close, click space area in filemanager page and spicture page, menu item will receive action
			if (!((MainViewActivity) context).isSlidingMenuOpen()) {
				return;
			}

			boolean excuted = false;

			switch (v.getId()) {
				case R.string.menu_view_grid:
					showGridView();
					TextView menuItem = (TextView) v;
					menuItem.setText(R.string.menu_view_list);
					menuItem.setId(R.string.menu_view_list);
					break;
				case R.string.menu_view_list:
					currentPageOrders = bridge.getOrderList();
					showExpandableView();
					menuItem = (TextView) v;
					menuItem.setText(R.string.menu_view_grid);
					menuItem.setId(R.string.menu_view_grid);
					break;
				case R.string.menu_thumb_folder:
					checkToStartThumbView();
					break;

				case R.string.menu_create_order:
					addNewOrder();
					break;
				default:
					break;
			}
			if (!excuted) {
				((MainViewActivity) context).slidingMenuListener.onClick(v);
			}
		}
	};

	private void startThumbView() {
		Intent intent = new Intent();
		Bundle bundle = new Bundle();
		bundle.putInt(Constants.KEY_THUMBFOLDER_INIT_MODE, ThumbFolderActivity.SRC_MODE_ORDER);
		intent.putExtras(bundle);
		intent.setClass(context, ThumbFolderActivity.class);
		((Activity) context).startActivityForResult(intent, 0);
	}

	private void startBookView(SOrder order) {
		Intent intent = new Intent();
		Bundle bundle = new Bundle();
		bundle.putInt(Constants.KEY_BOOK_INIT_MODE, BookActivity.ORDER);
		if (order != null) {
			bundle.putInt(Constants.KEY_BOOK_INIT_ORDER, order.getId());
		}
		intent.putExtras(bundle);
		intent.setClass(context, BookActivity.class);
		((Activity) context).startActivity(intent);
		//((Activity) context).startActivityForResult(intent, 0);
	}


	private void showExpandableView() {
		if (currentView != VIEW_LIST) {
			currentView = VIEW_LIST;
			gridView.setVisibility(View.GONE);
			previousPageView.setVisibility(View.GONE);
			nextPageView.setVisibility(View.GONE);

			expandableView = (ExpandableListView) view.findViewById(R.id.sorder_expandableview);
			expandableView.setVisibility(View.VISIBLE);
			expandableView.setOnChildClickListener(this);
			expandableView.setOnCreateContextMenuListener(this);

			notifyUpdate();
		}
	}

	private void loadTagList() {

		if (tagList != null) {
			tagList.clear();
		}
		if (tagList == null) {
			tagList = new ArrayList<STag>();
		}
		tagList = bridge.loadTagList();
	}

	private void suitExpandList() {
		if (tagList == null || tagList.size() == 0) {
			return;
		}
		if (orderListInExpandable != null) {
			for (int i = 0; i < orderListInExpandable.size(); i ++) {
				orderListInExpandable.get(i).clear();
			}
			orderListInExpandable.clear();
		}

		orderListInExpandable = new ArrayList<List<SOrder>>();
		List<SOrder> subList = null;
		for (int i = 0; i < tagList.size(); i ++) {
			subList = new ArrayList<SOrder>();
			orderListInExpandable.add(subList);
		}
		List<SOrder> list = currentPageOrders;
		for (SOrder order:list) {
			if (order.getTag().getName() == null) {
				order.setTag(getTag(order.getTag().getId()));
			}
			addToTagSubList(order);
		}
	}

	private void addToTagSubList(SOrder order) {
		for (int i = 0; i < tagList.size(); i ++) {
			if (tagList.get(i).getId() == order.getTag().getId()) {
				orderListInExpandable.get(i).add(order);
				break;
			}
		}
	}

	private STag getTag(int id) {
		STag tag = null;
		for (STag t:tagList) {
			if (t.getId() == id) {
				tag = t;
				break;
			}
		}
		if (tag == null) {
			tag = DBInfor.DEFAULT_TAG;
		}
		return tag;
	}

	private void showGridView() {
		if (currentView != VIEW_GRID) {
			currentView = VIEW_GRID;
			expandableView.setVisibility(View.GONE);
			gridView.setVisibility(View.VISIBLE);
			refresh();
		}
	}

	public void openCreateOrderDialog() {
//		EditText edit = new EditText(context);
//		edit.setInputType(InputType.TYPE_CLASS_TEXT);
//		final EditText ed = edit;

		SOrderCreaterUpdate creater = new SOrderCreaterUpdate(context, new CustomDialog.OnCustomDialogActionListener() {

			@Override
			public boolean onSave(Object object) {

				if (object == null) {
					Toast.makeText(context, R.string.sorder_create_fail, Toast.LENGTH_LONG).show();
				}
				else {
					Toast.makeText(context, R.string.sorder_success, Toast.LENGTH_LONG).show();
					refresh();
				}
				return true;
			}

			@Override
			public void onLoadData(HashMap<String, Object> data) {

			}

			@Override
			public boolean onCancel() {

				return false;
			}
		});
		creater.show();
		/*
		new SOrderCreater(context, new OnOrderCreateListener() {

			@Override
			public void onReceiveError(Object object) {
				Toast.makeText(context, R.string.sorder_create_fail, Toast.LENGTH_LONG).show();
			}

			@Override
			public void onOk(SOrder order) {
				Toast.makeText(context, R.string.sorder_success, Toast.LENGTH_LONG).show();
				refresh();

			@Override
			public void onCancel() {

			}
		}).show();*/
	}

	@Override
	public void onCreateOptionsMenu(Menu menu) {
		onPrepareOptionsMenu(menu);
	}

	@Override
	public boolean onPrepareOptionsMenu(Menu menu) {

		menu.setGroupVisible(R.id.group_file, false);
		menu.setGroupVisible(R.id.group_sorder, true);
		menu.setGroupVisible(R.id.group_spicture, false);
		menu.findItem(R.id.menu_edit).setVisible(true);
		menu.findItem(R.id.menu_view_list).setVisible(
				currentView == VIEW_GRID);
		menu.findItem(R.id.menu_view_grid).setVisible(
				currentView == VIEW_LIST);
		return true;
	}

	@Override
	public boolean onBackPressed() {
		return false;
	}

	@Override
	public void changeBackground(Bitmap bitmap) {

	}

	@Override
	public void onItemClick(AdapterView<?> parent, View view, int position,
							long id) {
		bridge.accessOrder(currentPageOrders.get(position));
		((MainViewActivity) context).switchToPictureOrderView(currentPageOrders.get(position));
	}

	@Override
	public boolean onChildClick(ExpandableListView parent, View v,
								int groupPosition, int childPosition, long id) {
		((MainViewActivity) context).switchToPictureOrderView(
				orderListInExpandable.get(groupPosition).get(childPosition));
		return false;
	}

	@Override
	public boolean onItemLongClick(AdapterView<?> parent, View view,
								   int position, long id) {

		//截图需要截非按压效果的
		view.findViewById(R.id.sorder_grid_item_layout).setBackgroundResource(R.drawable.shape_order_background);
		Bitmap cover = ScreenUtils.snapShotView(view);
		int viewPos[] = new int[2];
		view.getLocationOnScreen(viewPos);

		final int pos = position;
		sOrderMenuDialog = new SOrderMenuDialog(context, cover, viewPos[0], viewPos[1]
				, new CircleMenuView.OnMenuItemListener() {

			@Override
			public void onMenuClick(int which) {

				sOrderMenuDialog.dismiss();

				if (which == 0) {//rename
					renameOrder(currentPageOrders.get(pos));
				}
				if (which == 1) {//delete
					if (currentPageOrders.get(pos).getItemNumber() > 0) {
						openDeleteWarningDlg(pos);
					}
					else {
						deleteOrder(currentPageOrders.get(pos));
					}
				}
				else if (which == 2) {//decipher as folder
					decipherOrderAsFolder(currentPageOrders.get(pos));
				}
				else if (which == 3) {//fullscreen
					startFullScreenActivity(currentPageOrders.get(pos));
				}
				else if (which == 4) {//view access count
					showAccessCount(currentPageOrders.get(pos));
				}
				else if (which == 5) {
					bridge.accessOrder(currentPageOrders.get(pos));
					openByWall(currentPageOrders.get(pos));
				}
				else if (which == 6) {
					bridge.accessOrder(currentPageOrders.get(pos));
					startBookView(currentPageOrders.get(pos));
				}
				else if (which == 7) {
					new PreviewDialog(context, currentPageOrders.get(pos)).show();
				}

			}
		});
		sOrderMenuDialog.show();

		//截图后必须还原，否则以后再press没有效果
		if (Application.isLollipop()) {
			view.findViewById(R.id.sorder_grid_item_layout).setBackgroundResource(R.drawable.selector_order_background_l);
		}
		else {
			view.findViewById(R.id.sorder_grid_item_layout).setBackgroundResource(R.drawable.selector_order_background);
		}
		return true;
	}

	protected void startFullScreenActivity(SOrder order) {
		Bundle bundle = new Bundle();

//		bundle.putInt("src_mode", FullScreenSurfActivity.SRC_MODE_ORDER);
//		bundle.putInt("orderId", order.getId());
//		Intent intent = new Intent();
//		intent.putExtras(bundle);
//		intent.setClass(context, FullScreenSurfActivity.class);

		bundle.putInt("src_mode", UiController.SRC_MODE_ORDER);
		bundle.putInt("orderId", order.getId());
		Intent intent = new Intent();
		intent.putExtras(bundle);
		intent.setClass(context, SurfActivity.class);
		context.startActivity(intent);
	}

	protected void openByWall(SOrder order) {
		Bundle bundle = new Bundle();
		bundle.putInt(WallActivity.MODE_KEY, WallActivity.MODE_ORDER);
		bundle.putInt(WallActivity.MODE_VALUE_KEY, order.getId());
		((MainViewActivity) context).openWallGallery(bundle);
	}

	protected void showAccessCount(SOrder order) {
		SOrderCount orderCount = order.getOrderCount();
		if (orderCount == null) {
			orderCount = bridge.queryOrderCount(order.getId());
			order.setOrderCount(orderCount);
		}
		StringBuffer buffer = new StringBuffer("总访问量： ");
		buffer.append(orderCount.countAll)
				.append("\n").append("年访问量:  ").append(orderCount.countYear)
				.append("\n").append("月访问量:  ").append(orderCount.countMonth)
				.append("\n").append("一周访问量:  ").append(orderCount.countWeek)
				.append("\n").append("今日访问量:  ").append(orderCount.countDay);
		new AlertDialog.Builder(context)
				.setTitle(null)
				.setMessage(buffer.toString())
				.show();
	}

	protected void decipherOrderAsFolder(final SOrder order) {
		showProgress();
		new Thread() {
			public void run() {
				boolean result = bridge.decipherOrderAsFolder(order);
				Message message = new Message();
				if (result) {
					message.what = 1;
				}
				saveAsHandler.sendMessage(message);
			}
		}.start();
	}

	Handler saveAsHandler = new Handler() {

		@Override
		public void handleMessage(Message msg) {
			progressDialog.cancel();
			if (msg.what == 1) {
				Toast.makeText(context, R.string.sorder_success, Toast.LENGTH_LONG).show();
			}
			super.handleMessage(msg);
		}

	};

	private void deleteOrder(SOrder order) {
		if (bridge.deleteOrder(order)) {
			refresh();
		}
		else {
			Toast.makeText(context, R.string.sorder_delete_fail, Toast.LENGTH_LONG).show();
		}
	}

	private void openDeleteWarningDlg(final int pos) {
		new AlertDialog.Builder(context)
				.setTitle(R.string.warning)
				.setMessage(R.string.sorder_warning_delete)
				.setPositiveButton(R.string.ok, new DialogInterface.OnClickListener() {

					@Override
					public void onClick(DialogInterface dialog, int which) {
						deleteOrder(currentPageOrders.get(pos));
					}
				})
				.setNegativeButton(R.string.cancel, null)
				.show();
	}

	private void openDeleteWarningDlg(final int group, final int child) {
		new AlertDialog.Builder(context)
				.setTitle(R.string.warning)
				.setMessage(R.string.sorder_warning_delete)
				.setPositiveButton(R.string.ok, new DialogInterface.OnClickListener() {

					@Override
					public void onClick(DialogInterface dialog, int which) {
						deleteOrder(orderListInExpandable.get(group).get(child));
					}
				})
				.setNegativeButton(R.string.cancel, null)
				.show();
	}

	private class RenameDialog implements DialogInterface.OnClickListener {

		private SOrder rOrder;
		private EditText edit = new EditText(context);
		public RenameDialog(SOrder order) {
			rOrder = order;
			edit.setInputType(InputType.TYPE_CLASS_TEXT);
			edit.setText(order.getName());
			edit.setSelectAllOnFocus(true);
		}
		public View createView () {
			return edit;
		}
		@Override
		public void onClick(DialogInterface view, int which) {
			String name = edit.getText().toString();
			if (name != null && name.length() > 0) {
				if (bridge.isOrderExist(name)) {
					Toast.makeText(context, R.string.sorder_name_already_exist, Toast.LENGTH_LONG).show();
				}
				else {
					SOrder order = rOrder;
					order.setName(name);
					if (bridge.renameOrderName(order)) {
						refresh();
					}
					else {
						Toast.makeText(context, R.string.sorder_rename_fail, Toast.LENGTH_LONG).show();
					}
				}
			}
		}
	}

	private void renameOrder(SOrder order) {
		RenameDialog view = new RenameDialog(order);
		AlertDialog.Builder dialog = new AlertDialog.Builder(context);
		dialog.setTitle(R.string.menu_create_order);
		dialog.setView(view.createView());
		dialog.setPositiveButton(R.string.ok, view);
		dialog.setNegativeButton(R.string.cancel, null);
		dialog.show();
	}

	@Override
	public void onConfigurationChanged(Configuration newConfig) {
		if (sOrderMenuDialog != null) {
			sOrderMenuDialog.dismiss();
		}
	}

	@Override
	public void onCreateContextMenu(ContextMenu menu, View v, ContextMenuInfo menuInfo) {
		ExpandableListView.ExpandableListContextMenuInfo info = (ExpandableListView.ExpandableListContextMenuInfo) menuInfo;
		int type = ExpandableListView.getPackedPositionType(info.packedPosition);
		int group = ExpandableListView.getPackedPositionGroup(info.packedPosition);
		Activity activity = (Activity) context;
		if(type == ExpandableListView.PACKED_POSITION_TYPE_CHILD){
			activity.getMenuInflater().inflate(R.menu.context_sorder_longclick, menu);
		}
		else if (type == ExpandableListView.PACKED_POSITION_TYPE_GROUP) {
			activity.getMenuInflater().inflate(R.menu.context_sorder_head_longclick, menu);
		}
	}

	public void onContextItemSelected(MenuItem item) {
		ExpandableListContextMenuInfo info = (ExpandableListContextMenuInfo) item.getMenuInfo();
		int group = ExpandableListView.getPackedPositionGroup(info.packedPosition);
		int type = ExpandableListView.getPackedPositionType(info.packedPosition);
		int child = 0;
		if (type == ExpandableListView.PACKED_POSITION_TYPE_CHILD) {
			child = ExpandableListView.getPackedPositionChild(info.packedPosition);
			switch (item.getItemId()) {
				case R.id.menu_sorder_rename:

					renameOrder(orderListInExpandable.get(group).get(child));
					break;
				case R.id.menu_sorder_delete:

					if (orderListInExpandable.get(group).get(child).getItemNumber() > 0) {
						openDeleteWarningDlg(group, child);
					}
					else {
						deleteOrder(orderListInExpandable.get(group).get(child));
					}
					break;
				case R.id.menu_sorder_change_tag:

					break;
				case R.id.menu_sorder_decipher_as_folder:
					decipherOrderAsFolder(orderListInExpandable.get(group).get(child));

					break;
				case R.id.menu_sorder_access_count:
					showAccessCount(orderListInExpandable.get(group).get(child));
					break;
				case R.id.menu_sorder_open_by_wall:
					bridge.accessOrder(orderListInExpandable.get(group).get(child));
					openByWall(orderListInExpandable.get(group).get(child));
					break;
				case R.id.menu_sorder_book_view:
					bridge.accessOrder(orderListInExpandable.get(group).get(child));
					startBookView(orderListInExpandable.get(group).get(child));
					break;
				case R.id.menu_sorder_preview:
					new PreviewDialog(context, orderListInExpandable.get(group).get(child)).show();
					break;
			}
		}
		else if (type == ExpandableListView.PACKED_POSITION_TYPE_GROUP) {
			switch (item.getItemId()) {
				case R.id.menu_sorder_head_delete:
					if (orderListInExpandable.get(group).size() > 0) {
						warningDeleteTag(group, 3);
					}
					else {
						bridge.deleteTag(tagList.get(group), null);
					}
					break;
				case R.id.menu_sorder_head_rename:

					break;

			}
		}
	}

	private void warningDeleteTag (final int group, final int left) {
		String message = context.getResources().getString(R.string.sorder_delete_tag_warning);
		message = message.replace("%d", "" + left);
		AlertDialog.Builder builder = new AlertDialog.Builder(context);
		builder.setTitle(R.string.warning).setMessage(message)
				.setPositiveButton(R.string.ok, new DialogInterface.OnClickListener() {

					@Override
					public void onClick(DialogInterface arg0, int arg1) {
						int remain = left;
						remain --;
						if (remain > 0) {
							warningDeleteTag(group, remain);
						}
						else {
							executeDeleteTag(group);
							return;
						}
					}
				})
				.setNegativeButton(R.string.cancel, null)
				.show();
	}

	private void executeDeleteTag(int group) {
		bridge.deleteTag(tagList.get(group), orderListInExpandable.get(group));
	}

	@Override
	public void setActionBar(TabActionBar actionBar) {
		this.actionBar = actionBar;
		actionBar.clearActionIcon();
		actionBar.addSortIcon();
		actionBar.addThumbIcon();
		actionBar.addAddIcon();
		actionBar.addRefreshIcon();
		actionBar.setOnIconClickListener(actionIconListener);
	}

	OnClickListener actionIconListener = new OnClickListener() {

		@Override
		public void onClick(View v) {
			switch (v.getId()) {
				case R.id.actionbar_add:
					addNewOrder();
					break;
				case R.id.actionbar_sort:
					showSortPopup(v);
					break;
				case R.id.actionbar_thumb:
					showSelectCoverPopup(v);
					break;

				case R.id.actionbar_refresh:
					refresh();
					break;
				default:
					break;
			}
		}
	};

	public void checkToStartThumbView() {
		if (AccessController.getInstance().getAccessMode() < AccessController.ACCESS_MODE_PUBLIC) {
			AccessController.getInstance().showPwdDialog(context, new IdentityCheckListener() {

				@Override
				public void pass() {
					startThumbView();
				}

				@Override
				public void fail() {

				}

				@Override
				public void cancel() {

				}
			});
		}
		else {
			startThumbView();
		}
	}

	protected void addNewOrder() {
		AccessController controller = AccessController.getInstance();
		if (controller.getAccessMode() == AccessController.ACCESS_MODE_PRIVATE) {
			controller.showPwdDialog(context, new IdentityCheckListener() {

				@Override
				public void pass() {
					openCreateOrderDialog();
				}

				@Override
				public void fail() {
				}

				@Override
				public void cancel() {

				}
			});
		}
		else {
			openCreateOrderDialog();
		}
	}

	protected void showSelectCoverPopup(View v) {
		PopupMenu menu = new PopupMenu(context, v);
		menu.getMenuInflater().inflate(R.menu.sorder_select_cover_mode, menu.getMenu());
		menu.show();
		menu.setOnMenuItemClickListener(selectCoverListener);
	}

	OnMenuItemClickListener selectCoverListener = new OnMenuItemClickListener() {

		@Override
		public boolean onMenuItemClick(MenuItem item) {

			switch (item.getItemId()) {
				case R.id.menu_cover_thumb:
					checkToStartThumbView();
					break;
				case R.id.menu_book_view:
					startBookView(null);
					break;
				case R.id.menu_cover_single:
					if (currentView == VIEW_GRID) {
						if (gridAdapter != null) {
							gridAdapter.setCoverMode(CoverMode.SINGLE);
							notifyUpdate();
						}
					}
					break;
				case R.id.menu_cover_cascade:
					if (currentView == VIEW_GRID) {
						if (gridAdapter != null) {
							gridAdapter.setCoverMode(CoverMode.CASCADE);
							notifyUpdate();
						}
					}
					break;
				case R.id.menu_cover_cascade_rotate:
					if (currentView == VIEW_GRID) {
						if (gridAdapter != null) {
							gridAdapter.setCoverMode(CoverMode.CASCADE_ROTATE);
							notifyUpdate();
						}
					}
					break;
				case R.id.menu_cover_grid:
					if (currentView == VIEW_GRID) {
						if (gridAdapter != null) {
							gridAdapter.setCoverMode(CoverMode.GRID);
							notifyUpdate();
						}
					}
					break;
			}
			return true;
		}

	};

	private void showSortPopup(View v) {
		PopupMenu menu = new PopupMenu(context, v);
		menu.getMenuInflater().inflate(R.menu.sort_order, menu.getMenu());
		menu.show();
		menu.setOnMenuItemClickListener(sortListener);
	}

	OnMenuItemClickListener sortListener = new OnMenuItemClickListener() {

		@Override
		public boolean onMenuItemClick(MenuItem item) {
			switch (item.getItemId()) {
				case R.id.menu_by_date:
					if (currentOrderBy != SettingProperties.ORDER_BY_DATE) {
						currentOrderBy = SettingProperties.ORDER_BY_DATE;
						bridge.sortOrderByDate();
						if (SettingProperties.isPageModeEnable(context) && currentView == VIEW_GRID) {
							horizontalIndexView.setIndexList(indexCreator.createIndex(SettingProperties.ORDER_BY_DATE));
							horizontalIndexView.select(1);
							//choosePage(1);
						}
						else {
							currentPageOrders = bridge.getOrderList();
							notifyUpdate();
						}
					}
					break;
				case R.id.menu_by_name:
					if (currentOrderBy != SettingProperties.ORDER_BY_NAME) {
						currentOrderBy = SettingProperties.ORDER_BY_NAME;
						bridge.sortOrderByName();
						if (SettingProperties.isPageModeEnable(context) && currentView == VIEW_GRID) {
							horizontalIndexView.setIndexList(indexCreator.createIndex(SettingProperties.ORDER_BY_NAME));
							horizontalIndexView.select(1);
						}
						else {
							currentPageOrders = bridge.getOrderList();
							notifyUpdate();
						}
					}
					break;

				default:
					break;
			}
			return true;
		}
	};

	public void reloadCascadeNumber() {
		scrollController.reloadCascadeNum(context);
	}

}
