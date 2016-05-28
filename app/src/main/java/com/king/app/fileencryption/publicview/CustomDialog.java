package com.king.app.fileencryption.publicview;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

import com.king.app.fileencryption.Application;
import com.king.app.fileencryption.R;
import com.king.app.fileencryption.controller.ThemeManager;

import android.app.Dialog;
import android.content.Context;
import android.graphics.drawable.GradientDrawable;
import android.graphics.drawable.StateListDrawable;
import android.text.TextWatcher;
import android.util.Log;
import android.view.MotionEvent;
import android.view.View;
import android.view.WindowManager.LayoutParams;
import android.widget.EditText;
import android.widget.FrameLayout;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

public abstract class CustomDialog extends Dialog implements android.view.View.OnClickListener {
	private View rootView;

	private TextView titleView;
	private View dividerView;
	private TextView nullContentView;;
	private LinearLayout customView;
	private LinearLayout customToolbar;
	private View titleLayout;

	private ImageView okButton, saveButton;
	protected ImageView saveIcon, cancelIcon;
	private ImageView closeButton, searchButton;
	private EditText searchEdit;
	private FrameLayout searchLayout;
	protected Context context;
	protected OnCustomDialogActionListener actionListener;

	//用于记录当前加的所有的icon，更换颜色的时候需要使用
	private List<ImageView> currentButtonList;

	private Point startPoint, touchPoint;
	private LayoutParams windowParams;
	private boolean isZoom;
	private boolean enableZoom = false;
	private final int ZOOM_AREA = 20;
	private final int ZOOM_TOP = 1;
	private final int ZOOM_BOTTOM = 3;
	private int zoomDirection;

	public CustomDialog(Context context, OnCustomDialogActionListener actionListener) {
		super(context, R.style.DrawsCustomDialog);
		this.context = context;
		this.actionListener = actionListener;
		if (Application.isLollipop()) {
			setContentView(R.layout.dialog_custom_l);
		}
		else {
			setContentView(R.layout.dialog_custom);
		}
		currentButtonList = new ArrayList<ImageView>();
		titleView = (TextView) findViewById(R.id.dialog_custom_title);
		dividerView = findViewById(R.id.dialog_custom_divider);
		rootView = titleView.getRootView();
		rootView.setBackgroundColor(new ThemeManager(context).getBasicColor());
		nullContentView = (TextView) findViewById(R.id.dialog_custom_view_null);
		okButton = (ImageView) findViewById(R.id.dialog_custom_save);
		saveButton = (ImageView) findViewById(R.id.dialog_custom_save1);
		cancelIcon = (ImageView) findViewById(R.id.dialog_custom_cancle);
		customView = (LinearLayout) findViewById(R.id.dialog_custom_view);
		customToolbar = (LinearLayout) findViewById(R.id.dialog_custom_toolbar);
		searchButton = (ImageView) findViewById(R.id.dialog_custom_search);
		closeButton = (ImageView) findViewById(R.id.order_chooser_search_close);
		searchEdit = (EditText) findViewById(R.id.order_chooser_search_edit);
		searchLayout = (FrameLayout) findViewById(R.id.order_chooser_search_layout);
		okButton.setOnClickListener(this);
		saveButton.setOnClickListener(this);
		cancelIcon.setOnClickListener(this);
		searchButton.setOnClickListener(this);
		closeButton.setOnClickListener(this);

		titleLayout = findViewById(R.id.custom_dlg_title_layout);
		if (Application.isLollipop()) {
			searchEdit.setBackgroundResource(R.drawable.custom_dialog_search_bk_l);
			titleLayout.setBackgroundResource(R.drawable.custom_dialog_title_bk_normal_l);
		}

		saveIcon = okButton;//default

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
		view = getCustomToolbar();
		if (view != null) {
			customToolbar.addView(view);
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

	public void requestSaveAction(boolean need) {
		saveIcon = saveButton;
		saveIcon.setVisibility(need ? View.VISIBLE:View.GONE);
		if (need) {
			currentButtonList.add(saveIcon);
		}
	}

	public void requestOkAction(boolean need) {
		saveIcon = okButton;
		saveIcon.setVisibility(need ? View.VISIBLE:View.GONE);
		if (need) {
			currentButtonList.add(saveIcon);
		}
	}

	public void requestCancelAction(boolean need) {
		cancelIcon.setVisibility(need ? View.VISIBLE:View.GONE);
		if (need) {
			currentButtonList.add(cancelIcon);
		}
	}

	public void requestSearchAction(boolean need) {
		searchButton.setVisibility(need ? View.VISIBLE:View.GONE);
		if (need) {
			currentButtonList.add(searchButton);
		}
	}

	public void registTextChangeListener(TextWatcher textWatcher) {
		searchEdit.addTextChangedListener(textWatcher);
	}

	protected abstract View getCustomView();

	protected abstract View getCustomToolbar();

	public void setTitle(String text) {
		titleView.setText(text);
	}
	public void setTitle(int resId) {
		titleView.setText(context.getResources().getString(resId));
	}
	public void setTitleColor(int color) {
		titleView.setTextColor(color);
	}

	public void setDividerColor(int color) {
		dividerView.setBackgroundColor(color);
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
		if (view == cancelIcon) {
			dismiss();
		}
		else if (view == saveIcon) {
			dismiss();
		}

		else if (view == searchButton) {
			searchLayout.setVisibility(View.VISIBLE);
		}
		else if (view == closeButton) {
			searchLayout.setVisibility(View.GONE);
		}
	}

	public interface OnCustomDialogActionListener {
		public static final String DATA_TYPE = "data_type";
		/**
		 *
		 * @param object
		 * @return if false, then don't dismiss dialog
		 */
		public boolean onSave(Object object);
		/**
		 *
		 * @return if false, then don't dismiss dialog
		 */
		public boolean onCancel();
		public void onLoadData(HashMap<String, Object> data);
	}

	public void applyGreyStyle() {
		rootView.setBackgroundColor(context.getResources().getColor(R.color.dialog_background));
	}

	public void setBackgroundColor(int color) {
		rootView.setBackgroundColor(color);
	}

	protected void updateToobarIconBk(int color) {
		for (ImageView view:currentButtonList) {
			StateListDrawable stateDrawable = (StateListDrawable) view.getBackground();
			GradientDrawable drawable = (GradientDrawable) stateDrawable.getCurrent();
			drawable.setColor(color);
		}
	}

	protected void updateTitleBk(int color) {
		GradientDrawable drawable = (GradientDrawable) titleLayout.getBackground();
		drawable.setColor(color);
	}

	protected void updateTitleBorderColor(int color) {
		GradientDrawable drawable = (GradientDrawable) titleLayout.getBackground();
		drawable.setStroke(getContext().getResources()
						.getDimensionPixelSize(R.dimen.custom_dialog_icon_frame_width)
				, color);

	}
}
