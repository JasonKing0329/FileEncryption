package com.king.app.fileencryption.surf;

import java.io.File;
import java.io.IOException;
import java.util.List;

import pl.droidsonroids.gif.GifDrawable;
import pl.droidsonroids.gif.GifImageView;

import com.king.app.fileencryption.R;
import com.king.app.fileencryption.controller.EncrypterFactory;
import com.king.app.fileencryption.controller.PictureManagerUpdate;
import com.king.app.fileencryption.tool.Encrypter;

import android.content.Context;
import android.graphics.Bitmap;
import android.support.v4.view.PagerAdapter;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;

/**
 * @author JingYang
 * @version create time：2016-1-26 下午3:12:39
 *
 */
public class SurfAdapter extends PagerAdapter {

	private final boolean DEBUG = true;
	private final String TAG = "SurfAdapter";
	private Context mContext;
	private List<String> mList;
	private Encrypter encrypter;

	private OnClickListener onClickListener;

	public SurfAdapter(Context context, List<String> list) {
		mContext = context;
		mList = list;
		encrypter = EncrypterFactory.create();
	}

	public void setOnClickListener(OnClickListener listener) {
		onClickListener = listener;
	}

	@Override
	public int getCount() {

		return mList == null ? 0:mList.size();
	}

	@Override
	public View instantiateItem(ViewGroup container, int position) {
		if (DEBUG) {
			Log.d(TAG, "instantiateItem " + position);
		}
		if (encrypter.isGifFile(mList.get(position))) {
			View view = LayoutInflater.from(mContext).inflate(R.layout.activity_surf_item_gif, null);
			GifImageView gifView = (GifImageView) view.findViewById(R.id.surf_item_gifview);
			gifView.setOnClickListener(onClickListener);
			byte[] bytes = encrypter.decipherToByteArray(new File(mList.get(position)));
			try {
				GifDrawable drawable = new GifDrawable(bytes);
				gifView.setImageDrawable(drawable);
			} catch (IOException e) {
				gifView.setImageResource(R.drawable.ic_launcher);
				e.printStackTrace();
			}
			container.addView(view, ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.MATCH_PARENT);
			return view;
		}
		else {
			TouchImageView img = new TouchImageView(container.getContext());
			img.setOnClickListener(onClickListener);
			Bitmap bitmap = PictureManagerUpdate.getInstance().createHDBitmap(mList.get(position));
			img.setImageBitmap(bitmap);
			container.addView(img, ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.MATCH_PARENT);
			return img;
		}
	}

	@Override
	public void destroyItem(ViewGroup container, int position, Object object) {
		if (DEBUG) {
			Log.d(TAG, "destroyItem " + position);
		}
		container.removeView((View) object);
	}

	@Override
	public boolean isViewFromObject(View view, Object object) {
		if (DEBUG) {
			Log.d(TAG, "isViewFromObject");
		}
		return view == object;
	}


	//源码缺陷，用notifyDataSetChanged无法完成数据更新，需要覆写这个方法成这样notifyDataSetChanged才有作用
	/**
	 *
	 * 存在的问题：这不是PagerAdapter中的Bug，通常情况下，调用notifyDataSetChanged
	 * 		方法会让ViewPager通过Adapter的getItemPosition方法查询一遍所有child view
	 * 		，这种情况下，所有child view位置均为POSITION_NONE，表示所有的child view都
	 * 		不存在，ViewPager会调用destroyItem方法销毁，并且重新生成，加大系统开销，并
	 * 		在一些复杂情况下导致逻辑问题。特别是对于只是希望更新child view内容的时候，
	 * 		造成了完全不必要的开销。
	 * 更有效地方法：更为靠谱的方法是因地制宜，根据自己的需求来实现notifyDataSetChanged
	 * 		的功能，比如，在仅需要对某个View内容进行更新时，在instantiateItem()时，用
	 * 		View.setTag方法加入标志，在需要更新信息时，通过findViewWithTag的方法找到对
	 * 		应的View进行更新即可。
	 */
	@Override
	public int getItemPosition(Object object) {
		return PagerAdapter.POSITION_NONE;
	}

}
