package com.adasone.hm320a.data;


import android.os.Parcel;
import android.os.Parcelable;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;
import java.util.StringTokenizer;

public class VideoData implements Parcelable {
    public static final int TYPE_UNKNOWN = -1;
    public static final int TYPE_NORMAL = 0;
    public static final int TYPE_EVENT = 1;

    private int mType;
    private String mDate;
    private String mTime;
    private String mFileName;
    private boolean mExist;
    private boolean mCheck;

    public VideoData() {
        this.mType = TYPE_UNKNOWN;
        this.mDate = "";
        this.mTime = "";
        this.mFileName = "";
        this.mExist = false;
        this.mCheck = false;
    }

    public VideoData(int type, String fileName) throws IllegalArgumentException, ParseException, IndexOutOfBoundsException{
        this.mType = type;
        this.mFileName = fileName;
        this.mExist = false;
        this.mCheck = false;
        parsingDateAndTime();
    }

    public int getType() {
        return mType;
    }
    public String getDate() {
        return mDate;
    }
    public String getTime() {
        return mTime;
    }
    public String getFileName() {
        return mFileName;
    }
    public boolean getExist() {
        return mExist;
    }
    public boolean getChecked() {
        return mCheck;
    }

    public void setExist(boolean exit) {
        mExist = exit;
    }
    public void setChecked(boolean check) {
        mCheck = check;
    }

    private void parsingDateAndTime() throws IllegalArgumentException, ParseException, IndexOutOfBoundsException {
        String strDateTime = "";

        SimpleDateFormat inFormat = new SimpleDateFormat("yyyyMMdd_HHmmss", Locale.US);
        SimpleDateFormat outDateTimeFormat;

        if (Locale.KOREA.equals(Locale.getDefault())) {
            outDateTimeFormat = new SimpleDateFormat("yyyy.MM.dd|HH:mm:ss", Locale.KOREA);
        } else {
            outDateTimeFormat = new SimpleDateFormat("dd MMM yyyy|HH:mm:ss", Locale.US);
        }
        Date date = inFormat.parse(new String(this.mFileName.getBytes(), 1, 15));
        strDateTime = outDateTimeFormat.format(date);

        StringTokenizer tokens = new StringTokenizer(strDateTime, "|");
        if (tokens.hasMoreElements()) {
            this.mDate = tokens.nextToken();
        }
        if (tokens.hasMoreElements()) {
            this.mTime = tokens.nextToken();
        }
    }

    public void copyFrom(VideoData data) {
        mType = data.getType();
        mDate = data.getDate();
        mTime = data.getTime();
        mFileName = data.getFileName();
        mExist = data.getExist();
        mCheck = data.getChecked();
    }

    public void initialize() {
        this.mType = TYPE_UNKNOWN;
        this.mDate = "";
        this.mTime = "";
        this.mFileName = "";
        this.mExist = false;
        this.mCheck = false;
    }

    @Override
    public String toString() {
        return super.toString();
    }

    @Override
    public int hashCode(){
        return mFileName.hashCode();
    }

    @Override
    public boolean equals(Object obj) {
        if(obj instanceof VideoData) {
            VideoData temp = (VideoData) obj;

            if(this.mFileName.equals(temp.mFileName)) {
                return true;
            }
        }
        return false;
    }

    /**
     * Constructs a VideoData from a Parcel
     * @param parcel Source Parcel
     */
    public VideoData (Parcel parcel) {
        this.mType = parcel.readInt();
        this.mDate = parcel.readString();
        this.mTime = parcel.readString();
        this.mFileName = parcel.readString();
        this.mExist = parcel.readByte() != 0;
        this.mCheck = parcel.readByte() != 0;
    }


    @Override
    public int describeContents() {
        return 0;
    }

    @Override
    public void writeToParcel(Parcel dest, int flags) {
        dest.writeInt(mType);
        dest.writeString(mDate);
        dest.writeString(mTime);
        dest.writeString(mFileName);
        dest.writeByte((byte) (mExist ? 1 : 0));
        dest.writeByte((byte) (mCheck ? 1 : 0));
    }

    // Method to recreate a Question from a Parcel
    public static Creator<VideoData> CREATOR = new Creator<VideoData>() {

        @Override
        public VideoData createFromParcel(Parcel source) {
            return new VideoData(source);
        }

        @Override
        public VideoData[] newArray(int size) {
            return new VideoData[size];
        }

    };

}