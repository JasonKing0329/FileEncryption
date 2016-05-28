package com.king.app.fileencryption.slidingmenu;

import android.content.Context;
import android.content.res.TypedArray;
import android.util.AttributeSet;
import android.util.Log;
import android.util.TypedValue;
import android.view.MotionEvent;
import android.view.ViewGroup;
import android.widget.LinearLayout;

import com.king.app.fileencryption.R;
import com.king.app.fileencryption.util.ScreenUtils;
import com.nineoldandroids.view.ViewHelper;

public class SlidingMenuRight extends SlidingMenuAbstract
{
	public static final String BK_KEY = "sliding_menu_bk_right";
	/**
	 * 鐏炲繐绠风�钘夊
	 */
	private int mScreenWidth;
	/**
	 * dp
	 */
	private int mMenuLefttPadding;
	/**
	 * 閼挎粌宕熼惃鍕啍鎼达拷
	 */
	private int mMenuWidth;
	private int mHalfMenuWidth;

	private boolean isOpen;

	private boolean once;

	private ViewGroup mMenu;
	private ViewGroup mContent;
	
	private boolean enableScroll = true;

	public SlidingMenuRight(Context context, AttributeSet attrs)
	{
		this(context, attrs, 0);

	}

	public SlidingMenuRight(Context context, AttributeSet attrs, int defStyle)
	{
		super(context, attrs, defStyle);
		
		MENU_BK_KEY = BK_KEY;
		
		mScreenWidth = ScreenUtils.getScreenWidth(context);

		TypedArray a = context.getTheme().obtainStyledAttributes(attrs,
				R.styleable.SlidingMenuLeft, defStyle, 0);
		int n = a.getIndexCount();
		for (int i = 0; i < n; i++)
		{
			int attr = a.getIndex(i);
			switch (attr)
			{
			case R.styleable.SlidingMenuLeft_leftPadding:
				// 姒涙顓�0
				mMenuLefttPadding = a.getDimensionPixelSize(attr,
						(int) TypedValue.applyDimension(
								TypedValue.COMPLEX_UNIT_DIP, 50f,
								getResources().getDisplayMetrics()));// 姒涙顓绘稉锟�DP
				break;
			}
		}
		a.recycle();
	}

	public SlidingMenuRight(Context context)
	{
		this(context, null, 0);
	}

	@Override
	protected void onMeasure(int widthMeasureSpec, int heightMeasureSpec)
	{
		/**
		 * 閺勫墽銇氶惃鍕啎缂冾喕绔存稉顏勵啍鎼达拷
		 */
		if (!once)
		{
			LinearLayout wrapper = (LinearLayout) getChildAt(0);
			mContent = (ViewGroup) wrapper.getChildAt(0);
			mMenu = (ViewGroup) wrapper.getChildAt(1);

			mMenuWidth = mScreenWidth - mMenuLefttPadding;
			mHalfMenuWidth = mMenuWidth / 2;
			mMenu.getLayoutParams().width = mMenuWidth;
			mContent.getLayoutParams().width = mScreenWidth;

		}
		super.onMeasure(widthMeasureSpec, heightMeasureSpec);

	}

	@Override
	protected void onLayout(boolean changed, int l, int t, int r, int b)
	{
		super.onLayout(changed, l, t, r, b);
		if (changed)
		{
			Log.d("SlidingMenu", "onLayout changed");
			// 鐏忓棜褰嶉崡鏇㈡閽橈拷
			//this.scrollTo(mMenuWidth, 0);
			once = true;
		}
	}

	@Override
	public void requestLayout() {
		mScreenWidth = ScreenUtils.getScreenWidth(getContext());
		once = false;
		mMenuLefttPadding = getContext().getResources().getDimensionPixelSize(R.dimen.slidingmenu_rightpadding);
		super.requestLayout();
	}

	@Override
	public void enableScroll(boolean enable) {
		enableScroll = enable;
	}

	@Override
	public boolean onInterceptTouchEvent(MotionEvent ev) {
		if (enableScroll) {
			return super.onInterceptTouchEvent(ev);
		}
		else {
			return false;
		}
	}

	@Override
	public boolean onTouchEvent(MotionEvent ev)
	{
		if (enableScroll) {
			int action = ev.getAction();
			switch (action)
			{
			// Up閺冭绱濇潻娑滎攽閸掋倖鏌囬敍灞筋洤閺嬫粍妯夌粈鍝勫隘閸╃喎銇囨禍搴ゅ綅閸楁洖顔旀惔锔跨閸楀﹤鍨�灞藉弿閺勫墽銇氶敍灞芥儊閸掓瑩娈ｉ挊锟�
			case MotionEvent.ACTION_UP:
				int scrollX = getScrollX();
				Log.d("SlidingMenu", "scrollX=" + scrollX);
				if (scrollX > mHalfMenuWidth)
				{
					this.smoothScrollTo(mMenuWidth, 0);
					isOpen = true;
				} else
				{
					this.smoothScrollTo(0, 0);
					isOpen = false;
				}
				return true;
			}
			return super.onTouchEvent(ev);
		}
		else {
			return false;
		}
	}

	/**
	 * 閹垫挸绱戦懣婊冨礋
	 */
	public void openMenu()
	{
		if (isOpen)
			return;
		this.smoothScrollTo(mMenuWidth, 0);
		isOpen = true;
	}

	/**
	 * 閸忔娊妫撮懣婊冨礋
	 */
	public void closeMenu()
	{
		if (isOpen)
		{
			this.smoothScrollTo(0, 0);
			isOpen = false;
		}
	}

	/**
	 * 閸掑洦宕查懣婊冨礋閻樿埖锟�
	 */
	public void toggle()
	{
		if (isOpen)
		{
			closeMenu();
		} else
		{
			openMenu();
		}
	}

	@Override
	protected void onScrollChanged(int l, int t, int oldl, int oldt)
	{
		super.onScrollChanged(l, t, oldl, oldt);
		
		//sliding from left to right, l is decrease
		//sliding from right to left, l is increase
		float scale = (mMenuWidth - l) * 1.0f / mMenuWidth;
		float contentScale = 0.8f + scale * 0.2f;
		float menuScale = 1 - 0.3f * scale;
		//ViewHelper.setScaleX(mContent, contentScale);//x鏂瑰悜鐨勬敹缂╀細澧炲ぇ涓巑enu鐨勭┖鐧借竟璺濓紝浣嗘槸涓嶆敹缂╃殑璇濇晥鏋滃張涓嶅お濂�
		ViewHelper.setPivotX(mContent, 0);
		ViewHelper.setPivotY(mContent, mContent.getHeight() / 2);//鍏堣缃ソ杞村績锛屾敹缂╁氨涓嶄細鍑虹幇杞村績涓�鐨勬儏鍐�
		ViewHelper.setScaleY(mContent, contentScale);
		
		ViewHelper.setScaleX(mMenu, menuScale);
		ViewHelper.setScaleY(mMenu, menuScale);
		//ViewHelper.setTranslationX(mMenu, 0.7f - mMenuWidth * scale * 0.7f);//鐢变簬鍥惧眰鍏崇郴锛屽彸渚ц彍鍗曚笉鍔犱綅缃钩绉�
		ViewHelper.setAlpha(mMenu, 0.6f + 0.4f * (1 - scale));

	}

	@Override
	public boolean isOpen() {
		return isOpen;
	}

}
