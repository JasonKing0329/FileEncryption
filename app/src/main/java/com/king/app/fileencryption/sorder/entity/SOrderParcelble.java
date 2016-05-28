package com.king.app.fileencryption.sorder.entity;

import java.util.ArrayList;
import java.util.List;

import android.annotation.SuppressLint;
import android.os.Parcel;
import android.os.Parcelable;

@SuppressLint("ParcelCreator")
public class SOrderParcelble implements Parcelable {

	private SOrder order;
	
	public SOrderParcelble (SOrder order) {
		this.order = order;
	}
	
	public SOrder getOrder() {
		return order;
	}

	public void setOrder(SOrder order) {
		this.order = order;
	}

	@Override
	public int describeContents() {

		return 0;
	}

	@Override
	public void writeToParcel(Parcel dest, int flags) {
		if (order != null) {
			dest.writeInt(order.getId());
			dest.writeInt(order.getItemNumber());
			dest.writeString(order.getName());
			dest.writeString(order.getCoverPath());
//			if (order.getCoverBitmap().isRecycled()) {
//				dest.writeParcelable(order.getCoverBitmap(), 0);
//			}
//			dest.writeParcelable(order.getCoverBitmap(), 0);
			//dest.writeList(order.getImgItemList());
			dest.writeList(order.getImgPathIdList());
			dest.writeList(order.getImgPathList());
		}
	}
	
	public SOrderParcelble(Parcel in) {
		if (order == null) {
			order = new SOrder();
			order.setId(in.readInt());
			order.setItemNumber(in.readInt());
			order.setName(in.readString());
			order.setCoverPath(in.readString());
			//order.setCoverBitmap((Bitmap) in.readParcelable(null));
			//List<Bitmap> list = new ArrayList<Bitmap>();
			List<Integer> list1 = new ArrayList<Integer>();
			List<String> list2 = new ArrayList<String>();
			//in.readList(list, null);
			in.readList(list1, null);
			in.readList(list2, null);
			//order.setImgItemList(list);
			order.setImgPathIdList(list1);
			order.setImgPathList(list2);
		}
	}

	public static final Parcelable.Creator<SOrderParcelble> CREATOR = new Creator<SOrderParcelble>(){

		@Override
		public SOrderParcelble createFromParcel(Parcel source) {

			return new SOrderParcelble(source);
		}

		@Override
		public SOrderParcelble[] newArray(int size) {

			return new SOrderParcelble[size];
		}  
		
	};

}
