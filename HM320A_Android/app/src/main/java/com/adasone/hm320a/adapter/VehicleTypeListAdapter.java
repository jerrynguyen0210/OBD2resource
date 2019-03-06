package com.adasone.hm320a.adapter;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;

import com.adasone.hm320a.R;
import com.adasone.hm320a.application.AppApplication;
import com.adasone.hm320a.data.VehicleTypeData;
import com.adasone.hm320a.view.MarqueeTextView;

import java.util.ArrayList;

public class VehicleTypeListAdapter extends BaseAdapter{
	private Context mContext;
	private ArrayList<VehicleTypeData> mArrList;
	private int mSelectedPosition = -1;

	public VehicleTypeListAdapter(Context context, ArrayList<VehicleTypeData> arrList) {
		super();
		this.mContext = context;
		this.mArrList = arrList;
	}
	
	public void resetArrItem(ArrayList<VehicleTypeData> arrList){
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
		MarqueeTextView itemName;

		if (convertView == null) {
			convertView = LayoutInflater.from(mContext).inflate(R.layout.vehicle_type_list_item, parent, false);
			itemName = (MarqueeTextView) convertView.findViewById(R.id.tv_item_name);
			convertView.setTag(new ViewHolder(itemName));
		} else {
			ViewHolder viewHolder = (ViewHolder) convertView.getTag();
			itemName = viewHolder.itemName;
		}

		if(position == mSelectedPosition) {
			AppApplication.getAppApplication().setFontHYGothic800(itemName);
		} else {
			AppApplication.getAppApplication().setFontHYGothic600(itemName);
		}
		itemName.setText(mContext.getString(mArrList.get(position).getStringResId()));

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
		public final MarqueeTextView itemName;

		public ViewHolder(MarqueeTextView itemName) {
			this.itemName = itemName;
		}
	}
}
