package com.king.app.fileencryption.filemanager.view;

import java.util.ArrayList;
import java.util.List;

import com.king.app.fileencryption.Application;
import com.king.app.fileencryption.R;

import android.content.Context;
import android.util.AttributeSet;
import android.view.Gravity;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;
import android.view.animation.Animation.AnimationListener;
import android.widget.FrameLayout;
import android.widget.HorizontalScrollView;
import android.widget.TextView;

public class FilePathIndicatorView extends HorizontalScrollView
		implements OnClickListener, AnimationListener {

	public interface PathIndicatorListener {

		public void onClickPath(String path);
	}

	private PathIndicatorListener indicatorListener;
	private FrameLayout container;
	private List<PathIndicatorNode> pathList;

	private int indicatorPadding = 40;
	private int textColor, textSize;
	private int indicatorHeight;

	private Animation appearAnim, disappearAnim;

	public FilePathIndicatorView(Context context) {
		super(context);
		init();
	}

	public FilePathIndicatorView(Context context, AttributeSet attrs) {
		super(context, attrs);
		init();
	}

	public FilePathIndicatorView(Context context, AttributeSet attrs,
								 int defStyleAttr) {
		super(context, attrs, defStyleAttr);
		init();
	}

	private void init() {
		container = new FrameLayout(getContext());
		LayoutParams params = new LayoutParams(LayoutParams.WRAP_CONTENT, LayoutParams.WRAP_CONTENT);
		container.setLayoutParams(params);
		addView(container);

		textColor = getResources().getColor(R.color.white);
		textSize = getResources().getDimensionPixelSize(R.dimen.path_indicator_text_size);
		indicatorHeight = getResources().getDimensionPixelSize(R.dimen.path_indicator_height);
	}

	public void setPathIndicatorListener(PathIndicatorListener listener) {
		this.indicatorListener = listener;
	}

	public void create(List<PathIndicatorNode> pathList) {
		this.pathList = pathList;
		container.removeAllViews();
		if (pathList != null) {
			for (int i = 0; i < pathList.size(); i ++) {
				addIndicator(pathList.get(i));
			}
		}
	}

	public List<PathIndicatorNode> getPathList() {

		if (pathList != null) {
			for (int i = 0; i < pathList.size(); i ++) {
				PathIndicatorNode node = pathList.get(i);
				int indexInContainer = container.getChildCount() - 1 - node.getIndex();
				node.setIndexInContainer(indexInContainer);
				node.setWidth(container.getChildAt(indexInContainer).getWidth());
			}
		}
		return pathList;
	}

	private void addIndicator(PathIndicatorNode node) {

		if (node.getName() == null) {
			String array[] = node.getPath().split("/");
			String name = array[array.length - 1];
			if (name.length() == 0) {
				name = array[array.length - 2];
			}
			node.setName(name);
		}

		TextView textView = new TextView(getContext());
		LayoutParams indicatorParams = new LayoutParams(
				LayoutParams.WRAP_CONTENT, indicatorHeight);
		if (node.getIndex() == 0) {
			node.setLeft(0);
		}
		else {
			MarginLayoutParams params = indicatorParams;
			if (node.getLeft() != -1) {
				params.leftMargin = node.getLeft();
			}
			else {
				View lastItem = container.getChildAt(0);
				params.leftMargin = lastItem.getLeft() + lastItem.getWidth() - 40;
				node.setLeft(params.leftMargin);
			}
		}
		textView.setLayoutParams(indicatorParams);
		if (Application.isLollipop()) {
			//引用selector_path_indicator_bk_l竟然会导致FC
			textView.setBackground(getResources().getDrawable(R.drawable.selector_path_indicator_bk));//必须给每一个加载
		}
		else {
			textView.setBackground(getResources().getDrawable(R.drawable.selector_path_indicator_bk));//必须给每一个加载
		}
		textView.setText(node.getName());
		textView.setTextColor(textColor);
		textView.setGravity(Gravity.CENTER_VERTICAL);
		textView.setPadding(indicatorPadding, 0, indicatorPadding, 0);
		textView.setTag(node);
		textView.setTextSize(textSize);
		textView.setOnClickListener(this);
		if (node.getIndex() == 0) {
			container.addView(textView);
		}
		else {
			container.addView(textView, 0);
		}
	}

	public void addPath(String path) {
		if (pathList == null) {
			pathList = new ArrayList<PathIndicatorNode>();
		}
		PathIndicatorNode node = new PathIndicatorNode();
		node.setPath(path);
		node.setIndex(pathList.size());
		pathList.add(node);
		addIndicator(node);

		container.getChildAt(0).startAnimation(getAppearAnimation());
	}

	private Animation getAppearAnimation() {
		if (appearAnim == null) {
			appearAnim = AnimationUtils.loadAnimation(getContext(), R.anim.path_indicator_in);
		}
		return appearAnim;
	}

	private Animation getDisappearAnimation() {
		if (disappearAnim == null) {
			disappearAnim = AnimationUtils.loadAnimation(getContext(), R.anim.path_indicator_out);
			disappearAnim.setAnimationListener(this);
		}
		return disappearAnim;
	}


	@Override
	public void onClick(View view) {

		PathIndicatorNode node = (PathIndicatorNode) view.getTag();
		int indexInContainer = container.getChildCount() - 1 - node.getIndex();
		for (int i = indexInContainer - 1; i >= 0; i--) {
			container.removeViewAt(i);
		}
		for (int i = pathList.size() - 1; i >= node.getIndex() + 1; i --) {
			pathList.remove(i);
		}

		if (indicatorListener != null) {
			indicatorListener.onClickPath(node.getPath());
		}
	}

	public void backToUpper() {
		container.getChildAt(0).startAnimation(getDisappearAnimation());
	}

	@Override
	public void onAnimationEnd(Animation arg0) {
		//must remove it after animation end, otherwise the effect can't be seen
		container.removeViewAt(0);
		pathList.remove(pathList.size() - 1);
	}

	@Override
	public void onAnimationRepeat(Animation arg0) {

	}

	@Override
	public void onAnimationStart(Animation arg0) {

	}
}
