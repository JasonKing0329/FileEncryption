package com.king.app.fileencryption.fragment;

import com.king.app.fileencryption.R;
import com.king.app.fileencryption.controller.FragmentAction;
import com.king.app.fileencryption.controller.MainViewAction;
import com.king.app.fileencryption.sorder.view.SOrderPageUpdate;

import android.app.Fragment;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;

public class SOrderFragment extends Fragment {

	private final String TAG = "SOrderFragment";
	private SOrderPageUpdate sOrderPage;
	private View contentView;
	private FragmentAction fragmentAction;

	public void setFragmentAction (FragmentAction action) {
		fragmentAction = action;
	}

	@Override
	public View onCreateView(LayoutInflater inflater, ViewGroup container,
			Bundle savedInstanceState) {

		Log.d(TAG, "onCreateView");
		if (contentView == null) {
			Log.d(TAG, "reload view & page");
			contentView = inflater.inflate(R.layout.page_order, null);
			sOrderPage = new SOrderPageUpdate(getActivity(), contentView);
		}
		return contentView;
	}

	public MainViewAction getMainViewAction() {

		return sOrderPage;
	}

	@Override
	public void onActivityCreated(Bundle savedInstanceState) {
		super.onActivityCreated(savedInstanceState);
		fragmentAction.onFragmentInitEnd(this);
	}

	@Override
	public boolean onContextItemSelected(MenuItem item) {
		
		if (sOrderPage != null) {
			sOrderPage.onContextItemSelected(item);
		}
		return super.onContextItemSelected(item);
	}

}
