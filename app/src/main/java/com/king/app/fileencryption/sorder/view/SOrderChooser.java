package com.king.app.fileencryption.sorder.view;

import java.util.ArrayList;
import java.util.List;

import com.king.app.fileencryption.R;
import com.king.app.fileencryption.sorder.controller.SOrderPictureBridge;
import com.king.app.fileencryption.sorder.entity.SOrder;
import com.king.app.fileencryption.sorder.view.SOrderCreater.OnOrderCreateListener;

import android.app.Activity;
import android.app.Dialog;
import android.content.Context;
import android.text.Editable;
import android.text.TextWatcher;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.Window;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.FrameLayout;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.TextView;
@Deprecated
public class SOrderChooser extends Dialog implements View.OnClickListener, OnItemClickListener {

	private ListView listView;
	private ArrayAdapter<String> adapter;
	private Button okButton, cancleButton;
	private TextView noOrderText, titleView;
	private Context context;
	private ImageView addButton, searchButton, closeButton;
	private EditText searchEdit;
	private FrameLayout searchLayout;

	private List<SOrder> totalOrderList;
	private List<String> totalOrderNames;
	private List<SOrder> orderList;
	private List<String> orderNames;
	private int chosenIndex = -1;
	private OnOrderChooseListener chooseListener;
	private View lastChosedItem;

	public interface OnOrderChooseListener {
		public void chooseOrder(SOrder order);
	}

	public SOrderChooser (Context context, OnOrderChooseListener listener) {
		this(context);
		this.chooseListener = listener;
	}
	public void setLightTheme() {
		addButton.setImageResource(R.drawable.add_dark);
		searchButton.setImageResource(R.drawable.search_dark);
		closeButton.setImageResource(R.drawable.close_dark);
	}

	public void setTitleCustom(String title) {
		titleView.setText(title);
	}

	private SOrderChooser(Context context) {
		super(context);
		//super(context, android.R.style.Theme_Holo_Dialog);
		this.context = context;
		requestWindowFeature(Window.FEATURE_NO_TITLE);
		setContentView(R.layout.dialog_order_chooser);
		listView = (ListView) findViewById(R.id.order_chooser_list);
		okButton = (Button) findViewById(R.id.order_chooser_ok);
		cancleButton = (Button) findViewById(R.id.order_chooser_cancel);
		noOrderText = (TextView) findViewById(R.id.order_chooser_noorder);
		titleView = (TextView) findViewById(R.id.order_chooser_title);
		addButton = (ImageView) findViewById(R.id.order_chooser_add_order);
		searchButton = (ImageView) findViewById(R.id.order_chooser_search);
		closeButton = (ImageView) findViewById(R.id.order_chooser_search_close);
		searchEdit = (EditText) findViewById(R.id.order_chooser_search_edit);
		searchLayout = (FrameLayout) findViewById(R.id.order_chooser_search_layout);
		okButton.setOnClickListener(this);
		cancleButton.setOnClickListener(this);
		addButton.setOnClickListener(this);
		searchButton.setOnClickListener(this);
		closeButton.setOnClickListener(this);
		listView.setOnItemClickListener(this);

		searchEdit.addTextChangedListener(new SearchTextWather());
		//inflateActionBar();
		initOrders();
	}

	private void initOrders() {
		totalOrderList = SOrderPictureBridge.getInstance(context).getOrderList();//get or load
		SOrderPictureBridge.getInstance(context).sortOrderByName();//sort by name
		totalOrderList = SOrderPictureBridge.getInstance(context).getOrderList();
		if (totalOrderList != null && totalOrderList.size() > 0) {
			orderNames = new ArrayList<String>();
			totalOrderNames = new ArrayList<String>();
			orderList = new ArrayList<SOrder>();
			String name = null;
			for (SOrder order:totalOrderList) {
				orderList.add(order);
				name = order.getName() + "(" + order.getItemNumber() + ")";
				orderNames.add(name);
				totalOrderNames.add(name);
			}
			adapter = new ArrayAdapter<String>(context, android.R.layout.simple_dropdown_item_1line, orderNames);
			listView.setAdapter(adapter);
			noOrderText.setVisibility(View.GONE);
		}
		else {
			noOrderText.setVisibility(View.VISIBLE);
		}
	}

	@Override
	public void onItemClick(AdapterView<?> parent, View view, int position,
							long id) {
		if (lastChosedItem != null) {
			lastChosedItem.setBackgroundColor(context.getResources().getColor(R.color.transparent));
		}
		view.setBackgroundColor(context.getResources().getColor(R.color.order_choose_bk));
		chosenIndex = position;
		lastChosedItem = view;
	}

	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		Activity activity = (Activity) context;
		activity.getMenuInflater().inflate(R.menu.sorder_chooser_menu, menu);
		return super.onCreateOptionsMenu(menu);
	}

	@Override
	public boolean onOptionsItemSelected(MenuItem item) {
		switch (item.getItemId()) {
			case R.id.menu_sorder_chooser_add:

				break;

			default:
				break;
		}
		return super.onOptionsItemSelected(item);
	}

	@Override
	public void onClick(View v) {
		if (v.getId() == R.id.order_chooser_ok) {
			if (chosenIndex > -1) {
				chooseListener.chooseOrder(orderList.get(chosenIndex));
			}
			dismiss();
		}
		else if (v.getId() == R.id.order_chooser_cancel) {
			dismiss();
		}
		else if (v.getId() == R.id.order_chooser_search) {
			searchLayout.setVisibility(View.VISIBLE);
			searchButton.setVisibility(View.GONE);
		}
		else if (v.getId() == R.id.order_chooser_search_close) {
			searchLayout.setVisibility(View.GONE);
			searchButton.setVisibility(View.VISIBLE);
			searchEdit.setText("");
		}
		else if (v.getId() == R.id.order_chooser_add_order) {
			SOrderCreater creater = new SOrderCreater(context, new OnOrderCreateListener() {

				@Override
				public void onReceiveError(Object object) {
					// TODO Auto-generated method stub

				}

				@Override
				public void onOk(SOrder order) {
					if (orderList == null) {
						orderList = new ArrayList<SOrder>();
					}
					orderList.add(order);
					orderNames.add(order.getName());
					adapter.notifyDataSetChanged();
					listView.setSelected(true);
					listView.setSelection(orderList.size() - 1);
				}

				@Override
				public void onCancel() {
					// TODO Auto-generated method stub

				}
			});
			creater.show();
		}
	}

	private class SearchTextWather implements TextWatcher {

		@Override
		public void afterTextChanged(Editable arg0) {
			// TODO Auto-generated method stub

		}

		@Override
		public void beforeTextChanged(CharSequence arg0, int arg1, int arg2,
									  int arg3) {
			// TODO Auto-generated method stub

		}

		@Override
		public void onTextChanged(CharSequence text, int start, int before,
								  int count) {
			Log.i("SOrderChooser", "onTextChanged(" + text + "," + start + "," + before + "," + count + ")");
			orderNames.clear();
			orderList.clear();
			if (text.toString().trim().length() == 0) {
				for (int i = 0; i < totalOrderNames.size(); i ++) {
					orderNames.add(totalOrderNames.get(i));
					orderList.add(totalOrderList.get(i));
				}
				adapter.notifyDataSetChanged();
				return;
			}

			//startWith排在前面，contains排在后面
			String target = null, prefix = text.toString().toLowerCase();
			for (int i = 0; i < totalOrderNames.size(); i ++) {
				target = totalOrderNames.get(i).toLowerCase();
				if (target.startsWith(prefix)) {
					orderNames.add(totalOrderNames.get(i));
					orderList.add(totalOrderList.get(i));
				}
			}
			for (int i = 0; i < totalOrderNames.size(); i ++) {
				target = totalOrderNames.get(i).toLowerCase();
				if (!target.startsWith(prefix) && target.contains(prefix)) {
					orderNames.add(totalOrderNames.get(i));
					orderList.add(totalOrderList.get(i));
				}
			}
			adapter.notifyDataSetChanged();
		}

	}
}
