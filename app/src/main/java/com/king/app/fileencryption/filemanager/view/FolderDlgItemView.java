package com.king.app.fileencryption.filemanager.view;

import java.io.File;

import com.king.app.fileencryption.Application;
import com.king.app.fileencryption.R;

import android.content.Context;
import android.graphics.drawable.Drawable;
import android.os.Build;
import android.util.AttributeSet;
import android.view.Gravity;
import android.view.View;
import android.widget.ImageView;
import android.widget.ImageView.ScaleType;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.view.View.OnClickListener;;

public class FolderDlgItemView extends LinearLayout implements OnClickListener {

	public interface OnFolderSelectListener {
		public void onFolderSelect(FolderDlgItemView view);
	}

	private class ContainerHolder {
		ImageView arrowIconView;
		ImageView folderIconView;
		TextView nameView;
	}
	/**
	 * container contains this level folder item, its content is added by container.addView
	 * this level's sub folder item, they are a FolderDlgItemView object, added by this.addView
	 */
	private LinearLayout container;

	private File file;
	private Drawable icon, hasExpandIcon, expandIcon;
	private int iconSize;
	private int expandIconSize;
	private int level;
	private int levelSpace;
	private int textLeftSpace;
	private FolderDlgItemView parent;
	private boolean hasExpand;
	private boolean isExpand;
	private OnFolderSelectListener folderListener;
	private ContainerHolder containerHolder;

	public FolderDlgItemView(Context context, File file, int level) {
		this(context);
		this.file = file;
		this.level = level;
		setOrientation(VERTICAL);
		icon = getResources().getDrawable(R.drawable.directory_icon);
		hasExpandIcon = getResources().getDrawable(R.drawable.arrow_expand_right);
		expandIcon = getResources().getDrawable(R.drawable.arrow_expand_bottom);
		iconSize = getResources().getDimensionPixelSize(R.dimen.filelist_icon_size);
		expandIconSize = iconSize / 2;
		levelSpace = getResources().getDimensionPixelSize(R.dimen.folderdlg_level_space);
		textLeftSpace = getResources().getDimensionPixelSize(R.dimen.folderdlg_text_left_space);

		//init container, contain current root folder
		container = new LinearLayout(context);
		LayoutParams params = new LayoutParams(LayoutParams.MATCH_PARENT, LayoutParams.MATCH_PARENT);
		container.setLayoutParams(params);
		container.setOrientation(HORIZONTAL);
		container.setGravity(Gravity.CENTER_VERTICAL);
		container.setPadding(20, 20, 20, 20);

		if (Application.isLollipop()) {
			//L的波纹效果
			container.setBackground(getResources().getDrawable(R.drawable.selector_folderdlg_item_bk_l));
		}
		else {
			container.setBackground(getResources().getDrawable(R.drawable.selector_folderdlg_item_bk));
		}

		addView(container);
		container.setOnClickListener(this);

		initView();
	}

	public FolderDlgItemView(Context context) {
		super(context);
	}

	public FolderDlgItemView(Context context, AttributeSet attrs) {
		super(context, attrs);
	}

	public FolderDlgItemView(Context context, AttributeSet attrs,
							 int defStyleAttr) {
		super(context, attrs, defStyleAttr);
	}

	private void initView() {

		if (file != null && file.isDirectory()) {
			containerHolder = new ContainerHolder();
			containerHolder.arrowIconView = new ImageView(getContext());
			LayoutParams params = new LayoutParams(expandIconSize, expandIconSize);
			int leftMargin = levelSpace * level;
			((MarginLayoutParams) params).leftMargin = leftMargin;
			containerHolder.arrowIconView.setImageDrawable(hasExpandIcon);
			containerHolder.arrowIconView.setLayoutParams(params);
			containerHolder.arrowIconView.setScaleType(ScaleType.FIT_CENTER);
			container.addView(containerHolder.arrowIconView);
			containerHolder.arrowIconView.setVisibility(View.INVISIBLE);

			containerHolder.folderIconView = new ImageView(getContext());
			params = new LayoutParams(iconSize, iconSize);
			containerHolder.folderIconView.setImageDrawable(icon);
			containerHolder.folderIconView.setLayoutParams(params);
			container.addView(containerHolder.folderIconView);

			containerHolder.nameView = new TextView(getContext());
			params = new LayoutParams(LayoutParams.MATCH_PARENT, LayoutParams.MATCH_PARENT);
			leftMargin = textLeftSpace;
			((MarginLayoutParams) params).leftMargin = leftMargin;
			containerHolder.nameView.setText(file.getName());
			containerHolder.nameView.setLayoutParams(params);
			containerHolder.nameView.setTextColor(getResources().getColor(R.color.black));
			containerHolder.nameView.setGravity(Gravity.CENTER_VERTICAL);
			container.addView(containerHolder.nameView);
		}
	}

	public void setOnFolderSelectListener(OnFolderSelectListener listener) {
		folderListener = listener;
	}

	public void addSubView(File file, int level, boolean hasExp) {
		FolderDlgItemView view = new FolderDlgItemView(getContext(), file, level);
		view.setParentView(this);
		view.setOnFolderSelectListener(folderListener);
		view.setHasExpand(hasExp);
		addView(view);
	}

	public int getLevel() {
		return level;
	}

	public File getFile() {
		return file;
	}

	public void setParentView(FolderDlgItemView view) {
		this.parent = view;
	}

	public FolderDlgItemView getParentView() {
		return parent;
	}

	public boolean hasExpand() {
		return hasExpand;
	}

	public void setHasExpand(boolean has) {
		hasExpand = has;
		if (hasExpand) {
			container.getChildAt(0).setVisibility(View.VISIBLE);
		}
		else {
			container.getChildAt(0).setVisibility(View.INVISIBLE);
		}
	}

	public void setExpand(boolean expand) {
		isExpand = expand;
		if (expand) {
			((ImageView) container.getChildAt(0)).setImageDrawable(expandIcon);
		}
		else {
			for (int i = getChildCount() - 1; i > 0 ; i --) {
				removeViewAt(i);
			}
			((ImageView) container.getChildAt(0)).setImageDrawable(hasExpandIcon);
		}
	}

	public boolean isExpand() {
		return isExpand;
	}

	@Override
	public void onClick(View view) {
		if (folderListener != null) {
			folderListener.onFolderSelect(this);
		}
	}

	public void setContainerSelected(boolean select) {
		container.setSelected(select);
	}

	public void refreshContainer() {
		if (containerHolder != null) {
			if (hasExpand) {
				containerHolder.arrowIconView.setVisibility(View.VISIBLE);
			}
			containerHolder.nameView.setText(file.getName());
		}
	}
}
