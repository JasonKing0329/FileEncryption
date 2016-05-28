package com.king.lib.saveas;

import java.util.HashMap;

import android.app.Dialog;
import android.content.Context;
import android.content.res.Configuration;
import android.util.Log;
import android.util.TypedValue;
import android.view.Gravity;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup.MarginLayoutParams;
import android.view.WindowManager.LayoutParams;
import android.widget.CheckBox;
import android.widget.LinearLayout;
import android.widget.TextView;

public abstract class CustomPage extends Dialog implements android.view.View.OnClickListener{

	public interface OnCustomPageActionListener {
		public static final String DATA_TYPE = "data_type";
		/**
		 *
		 * @return if false, then don't dismiss dialog
		 */
		public boolean onCancel();
		public void onSelect(Object object);
		public void onAction(Object object);
		public void onLoadData(HashMap<String, Object> data);
	}

	private View rootView;

	private TextView titleView;
	private LinearLayout titleLayout;
	private TextView nullContentView;;
	private LinearLayout customView;
	private LinearLayout customToolbar;

	protected TextView delView, editView, newWindowView, closeView, okView, addView;

	//private FrameLayout searchLayout;
	protected Context context;
	protected OnCustomPageActionListener actionListener;

	private Point startPoint, touchPoint;
	private LayoutParams windowParams;
	private boolean isZoom;
	private boolean enableZoom = false;
	private final int ZOOM_AREA = 20;
	private final int ZOOM_TOP = 1;
	private final int ZOOM_BOTTOM = 3;
	private int zoomDirection;

	public CustomPage(Context context, OnCustomPageActionListener actionListener) {
		super(context, R.style.DlgSaveAs);
		this.context = context;
		this.actionListener = actionListener;

		setContentView(R.layout.dlg_custom);
		titleView = (TextView) findViewById(R.id.custom_title);
		rootView = titleView.getRootView();
		nullContentView = (TextView) findViewById(R.id.custom_view_null);
		customView = (LinearLayout) findViewById(R.id.custom_view);
		customToolbar = (LinearLayout) findViewById(R.id.custom_toolbar);
		titleLayout = (LinearLayout) findViewById(R.id.custom_title_layout);

		if (Configs.isLollipop()) {
//			searchEdit.setBackgroundResource(R.drawable.custom_dialog_search_bk_l);
//			findViewById(R.id.custom_dlg_title_layout).setBackgroundResource(R.drawable.custom_dialog_title_bk_normal_l);
		}

		windowParams = getWindow().getAttributes();
		touchPoint = new Point();
		startPoint = new Point();

		View view = getCustomView();
		if (view != null) {
			customView.addView(view);
		}
		else {
			customView.setVisibility(View.GONE);
			nullContentView.setVisibility(View.VISIBLE);
		}

		initDefaultAction();

		view = getCustomToolbar();
		if (view != null) {
			customToolbar.addView(view);
		}
	}

	private void initDefaultAction() {
		LinearLayout layout = new LinearLayout(getContext());
		layout.setOrientation(LinearLayout.HORIZONTAL);
		layout.setGravity(Gravity.CENTER);
		int width = getContext().getResources().getDimensionPixelSize(
				R.dimen.folderdlg_toolbar_action_width);
		int height = getContext().getResources().getDimensionPixelSize(
				R.dimen.folderdlg_toolbar_action_height);
		LinearLayout.LayoutParams params = new LinearLayout.LayoutParams(
				width, height);
		MarginLayoutParams marginParams = params;
		marginParams.leftMargin = getContext().getResources().getDimensionPixelSize(R.dimen.folderdlg_toolmenu_space);

		int textUnit = 16;
		delView = new TextView(getContext());
		delView.setLayoutParams(params);
		delView.setText("删除");
		delView.setGravity(Gravity.CENTER);
		delView.setVisibility(View.GONE);
		delView.setTextSize(TypedValue.COMPLEX_UNIT_DIP, textUnit);
		delView.setTextColor(getContext().getResources().getColor(R.color.dlg_folder_action_text));
		editView = new TextView(getContext());
		editView.setLayoutParams(params);
		editView.setText("编辑");
		editView.setVisibility(View.GONE);
		editView.setGravity(Gravity.CENTER);
		editView.setTextSize(TypedValue.COMPLEX_UNIT_DIP, textUnit);
		editView.setTextColor(getContext().getResources().getColor(R.color.dlg_folder_action_text));
		newWindowView = new TextView(getContext());
		newWindowView.setLayoutParams(params);
		newWindowView.setText("新窗口");
		newWindowView.setVisibility(View.GONE);
		newWindowView.setGravity(Gravity.CENTER);
		newWindowView.setTextSize(TypedValue.COMPLEX_UNIT_DIP, textUnit);
		newWindowView.setTextColor(getContext().getResources().getColor(R.color.dlg_folder_action_text));
		closeView = new TextView(getContext());
		closeView.setLayoutParams(params);
		closeView.setText("关闭");
		closeView.setGravity(Gravity.CENTER);
		closeView.setTextSize(TypedValue.COMPLEX_UNIT_DIP, textUnit);
		closeView.setTextColor(getContext().getResources().getColor(R.color.dlg_folder_action_text));
		okView = new TextView(getContext());
		okView.setLayoutParams(params);
		okView.setText("确认");
		okView.setVisibility(View.GONE);
		okView.setGravity(Gravity.CENTER);
		okView.setTextSize(TypedValue.COMPLEX_UNIT_DIP, textUnit);
		okView.setTextColor(getContext().getResources().getColor(R.color.dlg_folder_action_text));
		addView = new TextView(getContext());
		addView.setLayoutParams(params);
		addView.setText("添加");
		addView.setVisibility(View.GONE);
		addView.setGravity(Gravity.CENTER);
		addView.setTextSize(TypedValue.COMPLEX_UNIT_DIP, textUnit);
		addView.setTextColor(getContext().getResources().getColor(R.color.dlg_folder_action_text));

		delView.setOnClickListener(this);
		editView.setOnClickListener(this);
		newWindowView.setOnClickListener(this);
		closeView.setOnClickListener(this);
		okView.setOnClickListener(this);
		addView.setOnClickListener(this);
		if (Configs.isLollipop()) {
//			delView.setBackgroundResource(R.drawable.browser_ripple_oval_grey_bk);
//			editView.setBackgroundResource(R.drawable.browser_ripple_oval_grey_bk);
//			newWindowView.setBackgroundResource(R.drawable.browser_ripple_oval_grey_bk);
//			closeView.setBackgroundResource(R.drawable.browser_ripple_oval_grey_bk);
//			okView.setBackgroundResource(R.drawable.browser_ripple_oval_grey_bk);
//			addView.setBackgroundResource(R.drawable.browser_ripple_oval_grey_bk);
		}
		layout.addView(delView);
		layout.addView(editView);
		layout.addView(newWindowView);
		layout.addView(addView);
		layout.addView(okView);
		layout.addView(closeView);

		customToolbar.addView(layout);
	}

	/**
	 * call this to set suitable dialog size match for screen orientation
	 */
	public void computeHeight() {
		if (context.getResources().getConfiguration().orientation == Configuration.ORIENTATION_PORTRAIT) {
			int screenHeight = ScreenUtils.getScreenHeight(context);
			if (DisplayHelper.isTabModel(context)) {
				setHeight(screenHeight * 3 / 4);
			}
			else {
				setHeight(screenHeight * 4 / 5);
			}
		}
		else {
			int screenHeight = ScreenUtils.getScreenHeight(context);
			if (DisplayHelper.isTabModel(context)) {
				setHeight(screenHeight * 2 / 3);
			}
			else {
				setHeight(screenHeight - 80);
			}
		}
	}

	/**
	 * need set exact height before enable zoom
	 * @param enable
	 */
	public void enableZoom(boolean enable) {
		enableZoom = enable;
		if (windowParams.height == LayoutParams.MATCH_PARENT || windowParams.height == LayoutParams.WRAP_CONTENT
				|| windowParams.height < ZOOM_AREA * 2) {
			enableZoom = false;
		}
	}

	private class Point {
		float x;
		float y;
	}

	@Override
	/**
	 * notice: getRawX/Y是相对屏幕的坐标，getX/Y是相对控件的
	 * 要实现拖动效果，只能用getRawX/Y，用getX/Y会出现拖动不流畅并且抖动的效果
	 * (from internet: getX getY获取的是相对于child 左上角点的 x y 当第一次获取的时候通过layout设置了child一个新的位置 马上 再次获取x y时就会变了 变成了 新的x y
	 * 然后马上layout 然后又获取了新的x y又。。。。所以会看到 一个view不断地在屏幕上闪来闪去)
	 */
	public boolean onTouchEvent(MotionEvent event) {
		int action = event.getAction();
		switch (action) {
			case MotionEvent.ACTION_DOWN:
				if (enableZoom) {
					float xToView = event.getX();
					float yToView = event.getY();
					if (yToView < ZOOM_AREA) {
						isZoom = true;
						zoomDirection = ZOOM_TOP;
					}
					else if (yToView > windowParams.height - ZOOM_AREA) {
						isZoom = true;
						zoomDirection = ZOOM_BOTTOM;
					}
					Log.d("CustomDialog", "ACTION_DOWN xToView=" + xToView + ", yToView=" + yToView);
				}

				float x = event.getRawX();//
				float y = event.getRawY();
				startPoint.x = x;
				startPoint.y = y;
				Log.d("CustomDialog", "ACTION_DOWN x=" + x + ", y=" + y);
				break;
			case MotionEvent.ACTION_MOVE:
				x = event.getRawX();
				y = event.getRawY();
				touchPoint.x = x;
				touchPoint.y = y;
				float dx = touchPoint.x - startPoint.x;
				float dy = touchPoint.y - startPoint.y;

				if (enableZoom && isZoom) {
					zoom((int)dy);
				}
				else {
					move((int)dx, (int)dy);
				}

				startPoint.x = x;
				startPoint.y = y;
				break;
			case MotionEvent.ACTION_UP:
				if (enableZoom) {
					isZoom = false;
				}
				break;

			default:
				break;
		}
		return super.onTouchEvent(event);
	}

	public void requestCloseAction(boolean need) {
		closeView.setVisibility(need ? View.VISIBLE:View.GONE);
	}

	public void requestOkAction(boolean need) {
		okView.setVisibility(need ? View.VISIBLE:View.GONE);
	}

	public void requestAddAction(boolean need) {
		addView.setVisibility(need ? View.VISIBLE:View.GONE);
	}

	public void requestDelAction(boolean need) {
		delView.setVisibility(need ? View.VISIBLE:View.GONE);
	}

	public void requestEditAction(boolean need) {
		editView.setVisibility(need ? View.VISIBLE:View.GONE);
	}

	public void requestOpenInNewTabAction(boolean need) {
		newWindowView.setVisibility(need ? View.VISIBLE:View.GONE);
	}

	protected abstract View getCustomView();

	protected abstract View getCustomToolbar();

	public void setTitle(String text) {
		titleView.setText(text);
	}
	public void setTitle(int resId) {
		titleView.setText(context.getResources().getString(resId));
	}

	/**
	 * 设置dialog的偏移位置
	 * @param x 负数向左，正数向右
	 * @param y 负数向上，正数向下
	 */
	public void setPositionOffset(int x, int y) {

		windowParams.x = x;
		windowParams.y = y;
		getWindow().setAttributes(windowParams);
	}

	private void move(int x, int y) {

		windowParams.x += x;
		windowParams.y += y;
		getWindow().setAttributes(windowParams);//must have
	}

	public void setWidth(int w) {
		windowParams.width = w;
		getWindow().setAttributes(windowParams);
	}

	private void zoom(int dy) {
		if (zoomDirection == ZOOM_TOP) {
			windowParams.height -= dy;
			windowParams.y += dy;
		}
		else if (zoomDirection == ZOOM_BOTTOM) {
			windowParams.height += dy;
		}
		getWindow().setAttributes(windowParams);

		if (zoomDirection == ZOOM_BOTTOM) {
			windowParams.y -= (dy / 2);
			getWindow().setAttributes(windowParams);
		}
		Log.d("CustomDialog", "zoom windowParams.height=" + windowParams.height + " dy=" + dy
				+ " windowParams.y=" + windowParams.y);
	}

	public void setHeight(int h) {
		windowParams.height = h;
		getWindow().setAttributes(windowParams);
	}

	@Override
	public void onClick(View view) {
		if (view == closeView) {
			dismiss();
		}
	}

	public void applyGreyStyle() {
		rootView.setBackgroundColor(context.getResources().getColor(R.color.dlg_saveas_background));
	}
}
