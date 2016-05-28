package com.king.app.fileencryption.book;

import java.io.File;
import java.io.FilenameFilter;
import java.util.ArrayList;
import java.util.Collections;
import java.util.LinkedList;
import java.util.List;
import java.util.Queue;
import java.util.Random;

import com.king.app.fileencryption.controller.PictureManagerUpdate;
import com.king.app.fileencryption.filemanager.view.FolderManager;
import com.king.app.fileencryption.open.image.ImageValue;
import com.king.app.fileencryption.open.image.ImageValueController;
import com.king.app.fileencryption.sorder.controller.SOrderPictureBridge;
import com.king.app.fileencryption.sorder.entity.SOrder;
import com.king.app.fileencryption.tool.SimpleEncrypter;
import com.king.app.fileencryption.util.ScreenUtils;

import android.content.Context;
import android.graphics.Bitmap;
import android.util.Log;

public class BookHelper {

	private static final String TAG = "BookHelper";
	private static BookHelper bookHelper;
	private BookHelper() {

	}

	public static BookHelper getInstance() {
		if (bookHelper == null) {
			bookHelper = new BookHelper();
		}
		return bookHelper;
	}

	public List<Bitmap> loadImage(List<ImageValue> list, Context context) {

		if (list == null) {
			return null;
		}
		List<Bitmap> bitmaps = new ArrayList<Bitmap>();
		int width = ScreenUtils.getScreenWidth(context);
		int height = ScreenUtils.getScreenHeight(context);
		if (list.size() == 2) {
			height = width / 2;
		}
		else if (list.size() > 2) {
			height = width / 2;
			width = width / 2;
		}

		Bitmap bitmap = null;
		for (ImageValue value:list) {
			//如果全用createHDBitmap会发生OOM
			if (value == null) {
				Log.d(TAG, "loadImage value = null, list.size=" + list.size());
			}
			else {
				bitmap = PictureManagerUpdate.getInstance().createImage(
						value.getPath(), width * height, context, 1);
			}
			if (bitmap == null) {
				bitmap = PictureManagerUpdate.getInstance().getUnavailableItemImage(context);
			}
			bitmaps.add(bitmap);
		}
		return bitmaps;
	}

	public static List<List<ImageValue>> orderPageItems(List<String> imgPathList) {
		if (imgPathList == null) {
			return null;
		}
		List<List<ImageValue>> list = new ArrayList<List<ImageValue>>();
		Collections.shuffle(imgPathList);
		ImageValueController controller = new ImageValueController();

		List<ImageValue> values = new ArrayList<ImageValue>();
		controller.queryImagePixelFrom(imgPathList, values);

		sortImageValue(list, values);
		return list;
	}

	/**
	 * sort规则:
	 * BookPage一共采用了6种显示布局模式
	 * 1张图片布局，2张图片布局，3张图片布局（包含3种）
	 * 对于1张图片布局，图片只要fitCenter全屏显示即可
	 * 对于2张图片布局，要求2张图片至少有一张是属于宽高差不多的
	 * 对于3张图片布局，3中图片模式对应了3中不同改的图片尺寸要求
	 * （1. 三种图片纵向顺序布局，适用于3张图片都是宽比高数值大，或者差不多
	 *  2. 左边2个纵向，右边一个充满，适用于3张图片都是高比宽数值大
	 *  3. 上面2个横向，下面一个充满，适用于2个高比宽数值大，1个宽比高数值大或差不多）
	 * 对于4张图片布局，要求4张图片满足宽高都差不多
	 *
	 *  实现过程：
	 *  采用3个队列，先将所有的图片根据value保存的宽高信息进行分类。分为宽比高数值大的widthQueue，高比宽数值大的heightQueue，
	 *  以及宽高差值在0.2倍之间的middleQueue。
	 *  为了能够匹配所有数量的图片，middleQueue的图片作为万能尺寸图片，即可当做widthQueue类图片，也可当做heightQueue类
	 *
	 *  采用while循环，利用随机数生成每页的图片数量
	 *  根据规则判断适合从哪个队列中弹出图片来组成满足规则的图片列表，每凑齐对应数量的图片就将代表该页图片的values加入至list中
	 * @param list 生成的page，以及page中应包含的图片列表
	 * @param values 当前全部图片的value信息列表
	 */
	private static void sortImageValue(List<List<ImageValue>> list,
									   List<ImageValue> values) {
		Queue<ImageValue> widthQueue = new LinkedList<ImageValue>();
		Queue<ImageValue> heightQueue = new LinkedList<ImageValue>();
		Queue<ImageValue> middleQueue = new LinkedList<ImageValue>();
		for (ImageValue value:values) {
			if (value.getWidth() > value.getHeight()) {
				if ((float) value.getWidth() / (float) value.getHeight() > 1.2f) {
					widthQueue.offer(value);
				}
				else {
					middleQueue.offer(value);
				}
			}
			else {
				if ((float) value.getHeight() / (float) value.getWidth() > 1.2f) {
					heightQueue.offer(value);
				}
				else {
					middleQueue.offer(value);
				}
			}
		}

		Random random = new Random();
		int[] sizeMode = new int[] {1, 2, 3, 4};
		int size = 1;

		boolean hasMore = true;
		List<ImageValue> subList = null;

		while (hasMore) {

			size = sizeMode[Math.abs(random.nextInt()) % sizeMode.length];

			int left = widthQueue.size() + heightQueue.size() + middleQueue.size();
			Log.d(TAG, "random size=" + size + ", left number=" + left);

			/**************************特殊情况先做判断**********************************/
			if (left < size) {//剩余图片不足随机数制定的图片张数
				size = left;
			}

			if (size == 4) {
				if (heightQueue.size() < 4) {//4张图布局要求4张图片都是高比宽数值大
					size --;
				}
			}

			//3张图布局可以接纳任何图片，会在后面匹配3种模式

			if (size == 2) {//2个的模式需满足2个图片宽高都差不多或者宽比高数值大
				if (middleQueue.size() + widthQueue.size() < 2) {
					if (left > 2) {
						size = 3;
					}
					else {
						size = 1;
					}
				}
			}

			/************************************************************/

			Log.d(TAG, "real size=" + size);
			if (size == 1) {//可以添加任意尺寸图片
				//从最多的queue里取
				Queue<ImageValue> offerQueue = null;
				if (widthQueue.size() > heightQueue.size()) {
					offerQueue = widthQueue.size() > middleQueue.size() ? widthQueue : middleQueue;
				}
				else {
					offerQueue = heightQueue.size() > middleQueue.size() ? heightQueue : middleQueue;
				}

				subList = new ArrayList<ImageValue>();
				subList.add(offerQueue.poll());
				list.add(subList);
			}

			else if (size == 2) {//添加两个是middleQueue或者widthQueue的图片，优先widthQueue
				int n = 0;
				subList = new ArrayList<ImageValue>();
				while (widthQueue.size() > 0 && n < 2) {
					subList.add(widthQueue.poll());
					n ++;
				}

				if (n == 0) {
					subList.add(middleQueue.poll());
					subList.add(middleQueue.poll());
				}
				else if (n == 1) {
					subList.add(middleQueue.poll());
				}
				list.add(subList);
			}
			else if (size == 3) {//有3种布局模式，最复杂
				if (widthQueue.size() + middleQueue.size() > 2) {//模式1，纵向线性布局
					boolean apply = Math.abs(random.nextInt()) % 2 == 0 ? false : true;
					apply = apply || (heightQueue.size() + middleQueue.size() < 2);//模式3必须要求至少2张属于height or middle, 另外一张是width or middle
					if (apply) {
						Log.d(TAG, "size 3 apply mode 1");
						subList = new ArrayList<ImageValue>();

						//第一个添加模式标志，Bookpage在显示的时候根据该tag
						//从而判断运用哪种显示模式
						ImageValue value = widthQueue.size() == 0 ? middleQueue.poll() : widthQueue.poll();
						value.setTag(1);
						subList.add(value);
						subList.add(widthQueue.size() == 0 ? middleQueue.poll() : widthQueue.poll());
						subList.add(widthQueue.size() == 0 ? middleQueue.poll() : widthQueue.poll());
						list.add(subList);
						if (widthQueue.size() == 0 && heightQueue.size() == 0 && middleQueue.size() == 0) {
							hasMore = false;
						}
						continue;
					}
				}

				if (heightQueue.size() > 2) {//模式2,左边2个vertical线性布局，右边一个充满
					boolean apply = Math.abs(random.nextInt()) % 2 == 0 ? false : true;
					apply = apply || (middleQueue.size() + widthQueue.size() == 0);//模式3必须要求至少2张属于height or middle, 另外一张是width or middle
					if (apply) {
						Log.d(TAG, "size 3 apply mode 2");
						subList = new ArrayList<ImageValue>();

						ImageValue value = heightQueue.poll();
						value.setTag(2);//第一个添加模式标志
						subList.add(value);
						subList.add(heightQueue.poll());
						subList.add(heightQueue.poll());
						list.add(subList);
						if (widthQueue.size() == 0 && heightQueue.size() == 0 && middleQueue.size() == 0) {
							hasMore = false;
						}
						continue;
					}
				}

				//模式3，上面2个横向线性布局，下面一个充满。要求上面两个是height or middle，下面一个是width or middle
				Log.d(TAG, "size 3 apply mode 3");
				subList = new ArrayList<ImageValue>();

				ImageValue value = heightQueue.size() == 0 ? middleQueue.poll() : heightQueue.poll();
				value.setTag(3);//第一个添加模式标志
				subList.add(value);
				subList.add(heightQueue.size() == 0 ? middleQueue.poll() : heightQueue.poll());
				subList.add(widthQueue.size() == 0 ? middleQueue.poll() : widthQueue.poll());
				list.add(subList);
			}
			else if (size == 4) {
				subList = new ArrayList<ImageValue>();
				subList.add(heightQueue.poll());
				subList.add(heightQueue.poll());
				subList.add(heightQueue.poll());
				subList.add(heightQueue.poll());
				list.add(subList);
			}

			if (widthQueue.size() == 0 && heightQueue.size() == 0 && middleQueue.size() == 0) {
				hasMore = false;
			}
		}
	}

	/*
	public static List<List<String>> orderPageItems(List<String> imgPathList) {

		if (imgPathList == null) {
			return null;
		}
		List<List<String>> list = new ArrayList<List<String>>();
		Collections.shuffle(imgPathList);
		List<String> pathList = null;
		
		Random random = new Random();
		int[] sizeMode = new int[] {1, 3};
		int size = 1;
		for (int i = 0; i < imgPathList.size(); ) {
			pathList = new ArrayList<String>();
			
			size = sizeMode[Math.abs(random.nextInt()) % 2];
			for (int j = 0; j < size && i < imgPathList.size(); j ++, i ++) {
				pathList.add(imgPathList.get(i));
			}
			list.add(pathList);
			Log.d(TAG, "orderPageItems " + "page " + i + " have " + pathList.size() + " items");
		}
		return list;
	}
	*/

	public static List<String> createFolderIndex() {

		List<String> list = new ArrayList<String>();
		List<File> fileList = new FolderManager().collectAllFolders();;
		for (int i = 0; i < fileList.size(); i ++) {
			list.add(fileList.get(i).getPath());
		}
		return list;
	}

	public static List<SOrder> createOrderIndex(Context context) {

		List<SOrder> list = SOrderPictureBridge.getInstance(context).getOrderList();;

		return list;
	}

	public static List<String> getPathList(String folder) {

		List<String> list = null;
		File file = new File(folder);
		if (file.exists()) {
			File array[] = file.listFiles(new FilenameFilter() {

				@Override
				public boolean accept(File arg0, String name) {

					return name.endsWith(SimpleEncrypter.FILE_EXTRA);
				}
			});
			if (array.length > 0) {
				list = new ArrayList<String>();
				for (File f:array) {
					list.add(f.getPath());
				}
			}
		}
		return list;
	}

	public static List<String> getPathList(SOrder order, Context context) {

		List<String> list = null;
		SOrderPictureBridge.getInstance(context).getOrderItemList(order);
		list = order.getImgPathList();
		return list;
	}

	public static SOrder queryOrder(int id, Context context) {

		SOrder order = SOrderPictureBridge.getInstance(context).queryOrder(id);
		return order;
	}

	public static void accessOrder(Context context, SOrder order) {
		SOrderPictureBridge.getInstance(context).accessOrder(order);
	}
}
