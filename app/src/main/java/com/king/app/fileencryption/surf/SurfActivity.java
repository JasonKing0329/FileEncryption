package com.king.app.fileencryption.surf;

import com.king.app.fileencryption.R;
import com.king.app.fileencryption.surf.GalleryAdapter.OnGalleryClickListener;

import android.app.Activity;
import android.content.res.Configuration;
import android.os.Bundle;
import android.os.Handler;
import android.support.v4.view.ViewPager.OnPageChangeListener;
import android.support.v7.widget.DefaultItemAnimator;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;
import android.widget.RelativeLayout;
import android.widget.RelativeLayout.LayoutParams;

/**
 * @author JingYang
 * @version create time：2016-1-26 下午3:01:18
 *
 * SurfActivity only process basic function of ImageGallery which include:
 * show guide list
 * show/zoom current image
 * slide to switch images
 * click guide item to switch image
 * and the animation of those actions
 *
 * tool bar action and other actions of FileEncryption application should be process by UiController
 * this is for isolating this module
 */
public class SurfActivity extends Activity implements /*OnItemSelectListener
	,*/ OnPageChangeListener, OnClickListener, OnGalleryClickListener{

	private final boolean DEBUG = true;
	private final String TAG = "SurfActivity";

	/**
	 * 删除时的透明过程
	 */
	private final int TIME_GALLERY_ANIM_REMOVE = 200;
	/**
	 * 删除时的透明过程结束后后面的item向前挤压的过程
	 */
	private final int TIME_GALLERY_ANIM_MOVE = 500;

	private ExtendedViewPager viewPager;
	//	private HorizontalGallery guideGallery;
	private RecyclerView gallery;
	private RelativeLayout toolbarContainer;

	//	private HorizontalAdapter guideAdapter;
	private GalleryAdapter galleryAdapter;
	private SurfAdapter pagerAdapter;

	/**
	 * record guide item selection status
	 */
//	private ImageView lastSelectItemView;

	/**
	 * implement specific function of FileEncryption application
	 */
	private SurfUiAction uiAction;

	private Animation toolbarInAnim, toolbarOutAnim;
	private Animation galleryInAnim, galleryOutAnim;

	/**
	 * the setCurrentItem method of ViewPager will trigger onPageSelected callback method
	 * but there is scroll gallery operation in onPageSelected method
	 * so, use this value to avoid scroll gallery when trigger is from click event
	 */
	private boolean eventFromClick;

	private int orientation;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		uiAction = new UiController(this);
		super.onCreate(savedInstanceState);

		orientation = getResources().getConfiguration().orientation;

		setContentView(R.layout.activity_surf);
		toolbarContainer = (RelativeLayout) findViewById(R.id.surf_toolbar_container);
		viewPager = (ExtendedViewPager) findViewById(R.id.surf_view_pager);
//		guideGallery = (HorizontalGallery) findViewById(R.id.surf_guide);
		gallery = (RecyclerView) findViewById(R.id.surf_gallery);

		uiAction.loadToolbar(toolbarContainer);
		uiAction.loadImages(getIntent());

//		guideAdapter = new HorizontalAdapter(this, uiAction.getImageList());
//		guideGallery.setAdapter(guideAdapter);
//		guideGallery.setOnItemSelectListener(this);
		galleryAdapter = new GalleryAdapter(this, uiAction.getImageList());
		galleryAdapter.setGalleryListener(this);
		//orientation
		LinearLayoutManager layoutManager = new LinearLayoutManager(this);
		layoutManager.setOrientation(LinearLayoutManager.HORIZONTAL);
//		layoutManager.setRecycleChildrenOnDetach(true);
		gallery.setLayoutManager(layoutManager);
		//animation
		DefaultItemAnimator animator = new DefaultItemAnimator();
		animator.setRemoveDuration(TIME_GALLERY_ANIM_REMOVE);
		animator.setMoveDuration(TIME_GALLERY_ANIM_MOVE);
		gallery.setItemAnimator(animator);
		gallery.setAdapter(galleryAdapter);

		updateGalleryOrientation(orientation);

		pagerAdapter = new SurfAdapter(this, uiAction.getImageList());
		pagerAdapter.setOnClickListener(this);
		viewPager.setAdapter(pagerAdapter);
		viewPager.setOnPageChangeListener(this);
	}

	@Override
	public void onGalleryItemClick(View view, int position) {
		if (DEBUG) {
			Log.d(TAG, "onGalleryItemClick " + position);
		}
		eventFromClick = true;
		viewPager.setCurrentItem(position);//will trigger onPageSelected
	}

	private void updateGallerySelection(int position) {
		galleryAdapter.setSelection(position);
	}

	@Override
	public void onGalleryItemLongClick(View view, int position) {

	}

//	@Override
//	public void onGalleryItemSelectStatus(View view, int position) {
//		if (lastSelectItemView != null) {
//			lastSelectItemView.setImageDrawable(null);
//		}
//		ImageView iv = (ImageView) view;
//		iv.setImageResource(R.drawable.surf_guide_border);
//		lastSelectItemView = iv;
//	}

	@Override
	public void onPageScrollStateChanged(int arg0) {
		if (DEBUG) {
			Log.d(TAG, "onPageScrollStateChanged " + arg0);
		}
	}

	@Override
	public void onPageScrolled(int arg0, float arg1, int arg2) {
//    	if (DEBUG) {
//			Log.d(TAG, "onPageScrolled " + arg0 + "," + arg2 + "," + arg1);
//		}
	}

	@Override
	public void onPageSelected(int position) {
		if (DEBUG) {
			Log.d(TAG, "onPageSelected " + position);
		}
		uiAction.onSwitchPage(position);

		//view pager滑动会执行onPageSelected，执行了setCurrentItem后也会执行onPageSelected
		//只有在发生view pager滑动事件才需要执行gallery的focus改变
		if (!eventFromClick) {
//        	guideGallery.scrollToPosition(position);
			gallery.scrollToPosition(position);
		}
		eventFromClick = false;
		updateGallerySelection(position);
	}

	@Override
	public void onClick(View v) {
		if (toolbarContainer.getVisibility() == View.VISIBLE) {
			hideToobarAndGuide();
		}
		else {
			showToobarAndGallery();
		}
	}

	public Animation getToolbarInAnim() {
		if (toolbarInAnim == null) {
			toolbarInAnim = AnimationUtils.loadAnimation(SurfActivity.this, R.anim.push_down_in);
		}
		return toolbarInAnim;
	}

	public Animation getToolbarOutAnim() {
		if (toolbarOutAnim == null) {
			toolbarOutAnim = AnimationUtils.loadAnimation(SurfActivity.this, R.anim.push_up_out);
		}
		return toolbarOutAnim;
	}

	public Animation getGuideInAnim() {
		if (galleryInAnim == null) {
			galleryInAnim = AnimationUtils.loadAnimation(SurfActivity.this, R.anim.push_up_in);
		}
		return galleryInAnim;
	}

	public Animation getGuideOutAnim() {
		if (galleryOutAnim == null) {
			galleryOutAnim = AnimationUtils.loadAnimation(SurfActivity.this, R.anim.push_down_out);
		}
		return galleryOutAnim;
	}

	@Override
	protected void onDestroy() {
		if (uiAction != null) {
			uiAction.stopAutoPlay();
		}
		super.onDestroy();
	}

	@Override
	protected void onResume() {
		if (uiAction != null) {
			uiAction.stopAutoPlay();
		}
		super.onResume();
	}

	@Override
	protected void onPause() {
		if (uiAction != null) {
			uiAction.stopAutoPlay();
		}
		super.onPause();
	}

	@Override
	public void onBackPressed() {
		if (uiAction != null) {
			if (uiAction.stopAutoPlay()) {
				return;
			}
		}
		super.onBackPressed();
	}

	/**
	 * call this when auto-play or after delete operation
	 * @param index
	 */
	public void playPage(int index) {

		viewPager.setCurrentItem(index);
//		guideGallery.scrollToPosition(index);
	}

	/**
	 * if new position is less than 0, it means there are no items
	 * @param position
	 * @param newPosition
	 */
	public void onPageDeleted(int position, int newPosition) {
		if (newPosition < 0) {
			galleryAdapter.notifyRemoved(position);
			pagerAdapter.notifyDataSetChanged();
		}
		else {
			updateGallerySelection(newPosition);
			galleryAdapter.notifyRemoved(position);
			/**
			 * 由于RecyclerView没有提供item click和longclick监听
			 * 因此自己给adapter里的item定义了onClick和onLongClick监听
			 * 而每个view的position是注册在view.setTag里的，因此只是notifyItemRemoved
			 * 的话，position就更新不了，之后再点击，position就是错误的
			 * 而如果立马执行notifyDataSetChanged的话，动画又会被冲掉
			 * 因此设置post延时，延时对应onCreate里设置的值
			 */
			new Handler().postDelayed(new Runnable() {

										  @Override
										  public void run() {
											  galleryAdapter.notifyDataSetChanged();
										  }
									  }  //这里扩大时间是为了避免出现try to use recycled bitmap异常
					//这是因为adapter在onRecycle和notifyDataSetChanged以及notifyItemRemoved的时序问题引发的
					, (TIME_GALLERY_ANIM_MOVE + TIME_GALLERY_ANIM_REMOVE) * 2);
			pagerAdapter.notifyDataSetChanged();
		}
	}

	public void hideToobarAndGuide() {
		toolbarContainer.startAnimation(getToolbarOutAnim());
		toolbarContainer.setVisibility(View.GONE);
//		guideGallery.startAnimation(getGuideOutAnim());
//		guideGallery.setVisibility(View.GONE);
		gallery.startAnimation(getGuideOutAnim());
		gallery.setVisibility(View.GONE);
	}

	public void showToobarAndGallery() {
		toolbarContainer.startAnimation(getToolbarInAnim());
		toolbarContainer.setVisibility(View.VISIBLE);
//		guideGallery.startAnimation(getGuideInAnim());
//		guideGallery.setVisibility(View.VISIBLE);
		gallery.startAnimation(getGuideInAnim());
		gallery.setVisibility(View.VISIBLE);
	}

	private void updateGalleryOrientation(int orientation) {
		LinearLayoutManager manager = (LinearLayoutManager) gallery.getLayoutManager();
		RelativeLayout.LayoutParams params = (LayoutParams) gallery.getLayoutParams();
		if (orientation == Configuration.ORIENTATION_LANDSCAPE) {
			manager.setOrientation(LinearLayoutManager.VERTICAL);
			params.height = LayoutParams.MATCH_PARENT;
			params.width = getResources().getDimensionPixelSize(R.dimen.spicture_chooser_item_width);
			params.removeRule(RelativeLayout.ALIGN_PARENT_BOTTOM);
			params.addRule(RelativeLayout.ALIGN_PARENT_RIGHT);
			params.bottomMargin = 0;
			params.rightMargin = getResources().getDimensionPixelSize(R.dimen.surf_gallery_margin_right);
		}
		else {
			manager.setOrientation(LinearLayoutManager.HORIZONTAL);
			params.width = LayoutParams.MATCH_PARENT;
			params.height = getResources().getDimensionPixelSize(R.dimen.spicture_chooser_item_width);
			params.removeRule(RelativeLayout.ALIGN_PARENT_RIGHT);
			params.addRule(RelativeLayout.ALIGN_PARENT_BOTTOM);
			params.rightMargin = 0;
			params.bottomMargin = getResources().getDimensionPixelSize(R.dimen.surf_gallery_margin_bottom);
		}
		gallery.setLayoutParams(params);
		gallery.setLayoutManager(manager);
	}

	@Override
	public void onConfigurationChanged(Configuration newConfig) {
		if (newConfig.orientation != orientation) {
			orientation = newConfig.orientation;
			updateGalleryOrientation(orientation);
		}
		super.onConfigurationChanged(newConfig);
	}

}
