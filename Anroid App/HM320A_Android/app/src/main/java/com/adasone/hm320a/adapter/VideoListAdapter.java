package com.adasone.hm320a.adapter;

import android.content.Context;
import android.graphics.Color;
import android.text.SpannableString;
import android.text.style.UnderlineSpan;
import android.util.TypedValue;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.CheckBox;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.adasone.hm320a.R;
import com.adasone.hm320a.application.AppApplication;
import com.adasone.hm320a.data.VideoData;

import java.util.ArrayList;

public class VideoListAdapter extends BaseAdapter{
    private Context mContext;
    private ArrayList<VideoData> mArrList;
    private int mSelectedPosition = -1;

    public static final int MODE_NORMAL = 0;
    public static final int MODE_SELECT = 1;
    private int mCurrMode = MODE_NORMAL;

    private int mExistCount = 0;
    private int mCheckedCount = 0;
    private int mLeftPadding = 0;
    private int mCheckLeftPadding = 0;

    public VideoListAdapter(Context context, ArrayList<VideoData> arrList) {
        super();
        this.mContext = context;
        this.mArrList = arrList;
        this.mExistCount = 0;
        this.mCheckedCount = 0;
        mLeftPadding = (int) TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP,
                (float)28.67, context.getResources().getDisplayMetrics());
        mCheckLeftPadding = (int) TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP,
                (float)5.66, context.getResources().getDisplayMetrics());
        if (this.mArrList != null) {
            for (VideoData data : this.mArrList) {
                if (data.getExist()) {
                    this.mExistCount++;
                }
            }
        }
    }

    public void resetArrItem(ArrayList<VideoData> arrList){
        this.mArrList = arrList;
        this.mExistCount = 0;
        this.mCheckedCount = 0;
        if (this.mArrList != null) {
            for (VideoData data : this.mArrList) {
                if (data.getExist()) {
                    this.mExistCount++;
                }
            }
        }
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
        CheckBox checkbox;
        TextView dateTextView;
        TextView fileNameTextView;
        boolean showUnderLine = false;

        if (convertView == null) {
            convertView = LayoutInflater.from(mContext).inflate(R.layout.video_list_item, parent, false);
            itemLayout = (LinearLayout) convertView.findViewById(R.id.layout_item);
            checkbox = (CheckBox) convertView.findViewById(R.id.checkbox);
            dateTextView = (TextView) convertView.findViewById(R.id.tv_date);
            fileNameTextView = (TextView) convertView.findViewById(R.id.tv_file_name);
            convertView.setTag(new ViewHolder(itemLayout, checkbox, dateTextView, fileNameTextView));
        } else {
            ViewHolder viewHolder = (ViewHolder) convertView.getTag();
            itemLayout = viewHolder.itemLayout;
            checkbox = viewHolder.checkBox;
            dateTextView = viewHolder.dateTextView;
            fileNameTextView = viewHolder.fileNameTextView;
        }

        if (mCurrMode == MODE_NORMAL) {
            checkbox.setVisibility(View.GONE);
        } else if (mCurrMode == MODE_SELECT) {
            checkbox.setVisibility(View.VISIBLE);
        }

        if (mCurrMode == MODE_NORMAL && position == mSelectedPosition) {
            itemLayout.setBackgroundResource(R.drawable.rec_list_bg_select);
            dateTextView.setTextColor(Color.WHITE);
            fileNameTextView.setTextColor(Color.WHITE);
            if (mCurrMode == MODE_SELECT) {
                dateTextView.setPadding(mCheckLeftPadding, 0, 0, 0);
            } else {
                dateTextView.setPadding(mLeftPadding, 0, 0, 0);
            }
            AppApplication.getAppApplication().setFontHYGothic800(dateTextView, fileNameTextView);
        } else if (position % 2 == 0) {
            itemLayout.setBackgroundResource(R.drawable.rec_list_bg_even);
            if (mArrList.get(position).getExist()) {
                showUnderLine = true;
                if (mCurrMode == MODE_SELECT) {
                    checkbox.setEnabled(true);
                    if (mArrList.get(position).getChecked()) {
                        checkbox.setChecked(true);
                    } else {
                        checkbox.setChecked(false);
                    }
                    dateTextView.setPadding(mCheckLeftPadding, 0, 0, 0);
                } else {
                    dateTextView.setPadding(mLeftPadding, 0, 0, 0);
                }
                dateTextView.setTextColor(Color.rgb(3, 127, 191));
                fileNameTextView.setTextColor(Color.rgb(3, 127, 191));
                AppApplication.getAppApplication().setFontHYGothic800(dateTextView, fileNameTextView);
            } else{
                if (mCurrMode == MODE_SELECT) {
                    checkbox.setEnabled(false);
                    checkbox.setChecked(false);
                    dateTextView.setPadding(mCheckLeftPadding, 0, 0, 0);
                } else {
                    dateTextView.setPadding(mLeftPadding, 0, 0, 0);
                }
                dateTextView.setTextColor(Color.rgb(49, 48, 56));
                fileNameTextView.setTextColor(Color.rgb(49, 48, 56));
                AppApplication.getAppApplication().setFontHYGothic600(dateTextView, fileNameTextView);
            }
        } else {
            itemLayout.setBackgroundResource(R.drawable.rec_list_bg_odd);
            if (mArrList.get(position).getExist()) {
                showUnderLine = true;
                if (mCurrMode == MODE_SELECT) {
                    checkbox.setEnabled(true);
                    if (mArrList.get(position).getChecked()) {
                        checkbox.setChecked(true);
                    } else {
                        checkbox.setChecked(false);
                    }
                    dateTextView.setPadding(mCheckLeftPadding, 0, 0, 0);
                } else {
                    dateTextView.setPadding(mLeftPadding, 0, 0, 0);
                }
                dateTextView.setTextColor(Color.rgb(3, 127, 191));
                fileNameTextView.setTextColor(Color.rgb(3, 127, 191));
                AppApplication.getAppApplication().setFontHYGothic800(dateTextView, fileNameTextView);
            } else {
                if (mCurrMode == MODE_SELECT) {
                    checkbox.setChecked(false);
                    checkbox.setEnabled(false);
                    dateTextView.setPadding(mCheckLeftPadding, 0, 0, 0);
                } else {
                    dateTextView.setPadding(mLeftPadding, 0, 0, 0);
                }
                dateTextView.setTextColor(Color.rgb(49, 48, 56));
                fileNameTextView.setTextColor(Color.rgb(49, 48, 56));
                AppApplication.getAppApplication().setFontHYGothic600(dateTextView, fileNameTextView);
            }
        }
        dateTextView.setText(mArrList.get(position).getDate());
        if (showUnderLine) {
            SpannableString content = new SpannableString(mArrList.get(position).getFileName());
            content.setSpan(new UnderlineSpan(), 0, content.length(), 0);
            fileNameTextView.setText(content);
        } else {
            fileNameTextView.setText(mArrList.get(position).getFileName());
        }
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

    public void setMode(int mode) {
        mCurrMode = mode;
        this.notifyDataSetChanged();
    }

    public void checkedAll() {
        for (VideoData data : mArrList) {
            if (data.getExist()) {
                data.setChecked(true);
            }
        }
        mCheckedCount = mExistCount;
        this.notifyDataSetChanged();
    }

    public void uncheckedAll() {
        for (VideoData data : mArrList) {
            if (data.getExist()) {
                data.setChecked(false);
            }
        }
        mCheckedCount = 0;
        this.notifyDataSetChanged();
    }

    public void checkToggle(int position) {
        VideoData data = mArrList.get(position);
        if (data.getExist()) {
            if (data.getChecked()) {
                data.setChecked(false);
                mCheckedCount--;
            } else {
                data.setChecked(true);
                mCheckedCount++;
            }
            this.notifyDataSetChanged();
        }
    }

    public boolean isCheckedAll() {
        return mExistCount == mCheckedCount;
    }

    public int getFileExistCount() {
        return mExistCount;
    }

    public int getFileCheckedCount() {
        return mCheckedCount;
    }

    public static class ViewHolder {
        public final LinearLayout itemLayout;
        public final CheckBox checkBox;
        public final TextView dateTextView;
        public final TextView fileNameTextView;

        public ViewHolder(LinearLayout itemLayout, CheckBox checkBox, TextView date, TextView fileName) {
            this.itemLayout = itemLayout;
            this.checkBox = checkBox;
            this.dateTextView = date;
            this.fileNameTextView = fileName;
        }
    }
}
