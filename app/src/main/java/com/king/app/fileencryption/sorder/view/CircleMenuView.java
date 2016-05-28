package com.king.app.fileencryption.sorder.view;

import java.util.ArrayList;
import java.util.List;

import com.king.app.fileencryption.Application;
import com.king.app.fileencryption.R;

import android.content.Context;
import android.util.AttributeSet;
import android.util.Log;
import android.view.Gravity;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.animation.Animation;
import android.view.animation.LinearInterpolator;
import android.view.animation.TranslateAnimation;
import android.widget.AbsoluteLayout;
import android.widget.TextView;

@SuppressWarnings("deprecation")
public class CircleMenuView extends AbsoluteLayout implements OnClickListener {

	public interface OnMenuItemListener {
		public void onMenuClick(int index);
	}

	private final String TAG = "CircleMenuView";
	private OnMenuItemListener onMenuItemListener;
	private int objLeft, objTop, objRight, objBottom;
	private int itemNum;
	private int itemWidth, itemWidthSmall;
	private int itemBkRes;
	private int itemPadding;
	private int textColor;
	private int textSize;
	private int animTime;

	private final int MENU_LEFT = 0;
	private final int MENU_RIGHT = 1;
	private final int MENU_UP = 2;
	private final int MENU_DOWN = 3;
	private final int MENU_LEFT_UP = 4;
	private final int MENU_LEFT_DOWN = 5;
	private final int MENU_RIGHT_UP = 6;
	private final int MENU_RIGHT_DOWN = 7;
	private final int MENU_ROUND = 8;

	private int circleMode;
	private double RADIUS;
	private double theta;
	private double offsetTheta;

	private class MenuItem {
		int posX;
		int posY;
		private TextView textView;
	}

	private List<MenuItem> menuItemList;

	public CircleMenuView(Context context) {
		super(context);
		init();
	}
	public CircleMenuView(Context context, AttributeSet attrs) {
		super(context, attrs);
		init();
	}

	public CircleMenuView(Context context, AttributeSet attrs, int defStyleAttr) {
		super(context, attrs, defStyleAttr);
		init();
	}

	private void init() {
		itemWidth = getResources().getDimensionPixelSize(R.dimen.sorder_cover_menu_width);
		itemWidthSmall = getResources().getDimensionPixelSize(R.dimen.sorder_cover_menu_width_small);
		itemPadding = getResources().getDimensionPixelSize(R.dimen.sorder_cover_menu_padding);
		if (Application.isLollipop()) {
			itemBkRes = R.drawable.selector_circle_menu_bk_l;
		}
		else {
			itemBkRes = R.drawable.selector_custom_dialog_icon_bk;
		}
		textColor = getResources().getColor(R.color.white);
		textSize = getResources().getDimensionPixelSize(R.dimen.sorder_cover_menu_textSize);
		animTime = getResources().getInteger(R.integer.sorder_menudlg_anim_time);

	}

	public void setOnMenuItemListener(OnMenuItemListener listener) {
		onMenuItemListener = listener;
	}

	/**
	 * 旋转菜单的依附区域
	 * @param left
	 * @param top
	 * @param width
	 * @param height
	 */
	public void setObjectArea(int left, int top, int width, int height) {
		objLeft = left;
		objTop = top;
		objRight = objLeft + width;
		objBottom = objTop + height;
	}

	/**
	 * 对于不同方位模式的弧形区域，其差别主要在于总的旋转弧度totalTheta，起始弧度offsetTheta以及间隔弧度theta
	 * 通过半径RADIUS和角度关系求解(x,y)坐标位置
	 * 弧形的决定方式是从起始弧度offsetTheta算起，沿逆时针方向扫过的区域
	 */
	private void computeCircleParams() {
		double arc = Math.sqrt(Math.pow(objRight - objLeft, 2) + Math.pow(objBottom - objTop, 2));
		RADIUS = arc / 2 + itemWidth;
		double totalTheta = Math.PI;

		switch (circleMode) {
			case MENU_LEFT:
				totalTheta = Math.PI;
				offsetTheta = Math.PI / 2;
				break;
			case MENU_RIGHT:
				totalTheta = Math.PI;
				offsetTheta = Math.PI / 2 * 3;
				break;
			case MENU_UP:
				totalTheta = Math.PI;
				offsetTheta = 0;
				break;
			case MENU_DOWN:
				totalTheta = Math.PI;
				offsetTheta = Math.PI;
				break;
			case MENU_LEFT_UP:
				double moreTheta = Math.asin(Math.abs((objRight - objLeft) / 2 - itemWidth / 2) / RADIUS);
				totalTheta = Math.PI / 2 + 2 * moreTheta;
				offsetTheta = Math.PI / 2 - moreTheta;
				break;
			case MENU_LEFT_DOWN:moreTheta = Math.asin(Math.abs((objRight - objLeft) / 2 - itemWidth / 2) / RADIUS);
				totalTheta = Math.PI / 2 + 2 * moreTheta;
				offsetTheta = Math.PI - moreTheta;
				break;
			case MENU_RIGHT_UP:moreTheta = Math.asin(Math.abs((objRight - objLeft) / 2 - itemWidth / 2) / RADIUS);
				totalTheta = Math.PI / 2 + 2 * moreTheta;
				offsetTheta = 0 - moreTheta;
				break;
			case MENU_RIGHT_DOWN:moreTheta = Math.asin(Math.abs((objRight - objLeft) / 2 - itemWidth / 2) / RADIUS);
				totalTheta = Math.PI / 2 + 2 * moreTheta;
				offsetTheta = Math.PI / 2 * 3 - moreTheta;
				break;
			case MENU_ROUND:
				totalTheta = Math.PI * 2;
				offsetTheta = 0;
				break;

			default:
				break;
		}

		if (circleMode == MENU_ROUND) {
			theta = totalTheta / itemNum;
		}
		else {
			theta = totalTheta / (itemNum - 1);
		}
	}

	/**
	 * 根据依附体在屏幕中的位置（objectArea）判断应用何种方位模式的弧形
	 */
	private void computeCircleMode() {
		int left = getLeft();
		int bottom = getBottom();
		int right = getRight();
		int top = getTop();
		int space = (int) (itemWidth * 1.5f);
		if (right - objRight < space) {
			if (objTop - top < space) {
				circleMode = MENU_LEFT_DOWN;
			}
			else {
				if (bottom - objBottom < space) {
					circleMode = MENU_LEFT_UP;
				}
				else {
					circleMode = MENU_LEFT;
				}
			}
		}
		else {
			if (objLeft - left < space) {
				if (objTop - top < space) {
					circleMode = MENU_RIGHT_DOWN;
				}
				else {
					if (bottom - objBottom < space) {
						circleMode = MENU_RIGHT_UP;
					}
					else {
						circleMode = MENU_RIGHT;
					}
				}
			}
			else {
				if (objTop - top < space) {
					circleMode = MENU_DOWN;
				}
				else {
					if (bottom - objBottom < space) {
						circleMode = MENU_UP;
					}
					else {
						circleMode = MENU_ROUND;
					}
				}
			}
		}
	}

	public void setMenuItems(String[] array) {
		itemNum = array.length;

		//先统计运用何种方位模式的弧形
		computeCircleMode();
		//根据弧形特征计算半径角度参数
		computeCircleParams();
		//计算每一个菜单view在弧形中的坐标(x,y)
		computeItemPos();

		int width = itemWidth;
		switch (circleMode) {
			case MENU_LEFT_UP:
			case MENU_LEFT_DOWN:
			case MENU_RIGHT_UP:
			case MENU_RIGHT_DOWN:
				width = itemWidthSmall;
				break;
		}
		//添加view
		for (int i = 0; i < array.length; i ++) {
			MenuItem item = menuItemList.get(i);
			TextView textView = new TextView(getContext());
			textView.setText(array[i]);
			textView.setTextColor(textColor);
			textView.setBackgroundResource(itemBkRes);
			textView.setPadding(itemPadding, itemPadding, itemPadding, itemPadding);
			textView.setGravity(Gravity.CENTER);
			textView.setTextSize(textSize);
			textView.setTag(i);
			textView.setOnClickListener(this);
			LayoutParams params = new LayoutParams(width, width, item.posX, item.posY);
			textView.setLayoutParams(params);
			addView(textView);
			item.textView = textView;
			textView.startAnimation(createAnimation(item));
		}
	}

	private Animation createAnimation(MenuItem item) {
//		TranslateAnimation animation = new TranslateAnimation(
//				(objLeft + objRight) / 2, item.posX, (objTop + objBottom) / 2, item.posY);
		//这里的位置是相对自身已布局的位置
		TranslateAnimation animation = new TranslateAnimation(
				(objLeft + objRight) / 2 - item.posX, 0, (objTop + objBottom) / 2 - item.posY, 0);
		animation.setDuration(animTime);
		//animation.setInterpolator(new LinearInterpolator());
		return animation;
	}
	private void computeItemPos() {

		Log.d(TAG, "computeItemPos circleMode=" + circleMode);
		menuItemList = new ArrayList<CircleMenuView.MenuItem>(itemNum);
		for (int i = 0; i < itemNum; i ++) {
			int index = i;
			switch (circleMode) {
			/*
			case MENU_LEFT:
			case MENU_DOWN:
			case MENU_ROUND:
			case MENU_LEFT_UP:
			case MENU_LEFT_DOWN:

				break;
			*/
				case MENU_RIGHT:
				case MENU_UP:
				case MENU_RIGHT_UP:
				case MENU_RIGHT_DOWN:
					index = itemNum - 1 - i;
					break;

				default:
					break;
			}
			MenuItem item = new MenuItem();
			double alpha = index * theta + offsetTheta;
			double offsetX = RADIUS * Math.cos(alpha);
			double offsetY = RADIUS * Math.sin(alpha);
			item.posX = (int) (objLeft + (objRight - objLeft) / 2 + offsetX - (itemWidth / 2));
			if (item.posX < getLeft()) {//防止超出边界
				item.posX = 5;
			}
			else if (item.posX + itemWidth > getRight()) {
				item.posX = getRight() - itemWidth - 5;
			}

			item.posY = (int) (objTop + (objBottom - objTop) / 2 - offsetY - (itemWidth / 2));
			if (item.posY < getTop()) {//防止超出边界
				item.posY = 5;
			}
			else if (item.posY + itemWidth > getBottom()) {
				item.posY = getBottom() - itemWidth - 5;
			}
			menuItemList.add(item);
		}
	}
	@Override
	public void onClick(View view) {
		int index = (Integer) view.getTag();
		if (onMenuItemListener != null) {
			onMenuItemListener.onMenuClick(index);
		}
	}
}
