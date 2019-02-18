package com.adasone.hm320a.adapter;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.LinearLayout;

import com.adasone.hm320a.R;
import com.adasone.hm320a.application.AppApplication;
import com.adasone.hm320a.view.MarqueeTextView;

import java.util.ArrayList;

public class VehicleSelectListAdapter extends BaseAdapter{
	private Context mContext;
	private ArrayList<String> mArrList;
	private int mSelectedPosition = 0;

	public VehicleSelectListAdapter(Context context, ArrayList<String> arrList) {
		super();
		this.mContext = context;
		this.mArrList = arrList;
	}
	
	public void resetArrItem(ArrayList<String> arrList){
		this.mArrList = arrList;
		this.notifyDataSetChanged();
	}


	@Override
	public int getCount() {
		if (mArrList != null) {
			return mArrList.size();
		} else {
			return 0;
		}
	}

	@Override
	public long getItemId(int position) {
		// TODO Auto-generated method stub
		return position;
	}

	@Override
	public View getView(int position, View convertView, ViewGroup parent) {
		LinearLayout itemLayout;
		MarqueeTextView itemName;

		if (convertView == null) {
			convertView = LayoutInflater.from(mContext).inflate(R.layout.vehicle_list_item, parent, false);
			itemLayout = (LinearLayout) convertView.findViewById(R.id.layout_item);
			itemName = (MarqueeTextView) convertView.findViewById(R.id.tv_item_name);
			convertView.setTag(new ViewHolder(itemLayout, itemName));
		} else {
			ViewHolder viewHolder = (ViewHolder) convertView.getTag();
			itemName = viewHolder.itemName;
			itemLayout = viewHolder.itemLayout;
		}

		AppApplication.getAppApplication().setFontHYGothic600(itemName);
		if (position == mSelectedPosition) {
			itemLayout.setBackgroundResource(R.drawable.vehicle_info3_list_select);
		} else {
			itemLayout.setBackgroundResource(R.drawable.vehicle_info3_list_unselect);

		}
		itemName.setText(mArrList.get(position));
		return convertView;

	}

	@Override
	public Object getItem(int position) {
		return mArrList.get(position);
	}

	public void setSelectedPosition(int select) {
		mSelectedPosition = select;
	}

	public int getSelectedPosition() {
		return mSelectedPosition;
	}

	private static class ViewHolder {
		public final LinearLayout itemLayout;
		public final MarqueeTextView itemName;

		public ViewHolder(LinearLayout itemLayout, MarqueeTextView itemName) {
			this.itemLayout = itemLayout;
			this.itemName = itemName;
		}
	}
}
