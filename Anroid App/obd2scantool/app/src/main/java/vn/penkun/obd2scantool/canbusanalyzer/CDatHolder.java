package vn.penkun.obd2scantool.canbusanalyzer;

import java.util.ArrayList;

import vn.penkun.obd2scantool.canbusanalyzer.CCanBusInterface.CCanFrame;

public class CDatHolder {
    private static CDatHolder sDatHolder;
    private ArrayList<CDatTable> mTableList = new ArrayList();
    private ArrayList<CDatTrace> mTraceList;

    public class CDat {
        protected byte[] mData;
        protected int mDataLen;
        protected int mFrameId;
        protected boolean mIsRequest;

        public byte[] getData() {
            return this.mData;
        }

        public boolean isRequest() {
            return this.mIsRequest;
        }

        public int getFrameId() {
            return this.mFrameId;
        }

        public int getDataLen() {
            return this.mDataLen;
        }

        public String getDataStr() {
            if (this.mIsRequest) {
                return "Request";
            }
            String s = "";
            for (int i = 0; i < this.mDataLen; i++) {
                if (i == 0) {
                    s = String.format("%1$02X", new Object[]{Byte.valueOf(this.mData[i])});
                } else {
                    s = s + String.format(" %1$02X", new Object[]{Byte.valueOf(this.mData[i])});
                }
            }
            return s;
        }
    }

    public class CDatTable {
        private ArrayList<CDatItem> mDatList = new ArrayList();

        public class CDatItem extends CDat {
            private int mCount;
            private int mInterval;
            private int mLastTimeStamp;

            public CDatItem(CCanFrame frame) {
                super();
                this.mFrameId = frame.ID;
                this.mIsRequest = frame.RTR;
                this.mLastTimeStamp = frame.TimeStamp;
                this.mData = new byte[8];
                copyDataFromFrame(frame);
                this.mInterval = 0;
                this.mCount = 1;
            }

            private void copyDataFromFrame(CCanFrame frame) {
                if (!this.mIsRequest) {
                    this.mDataLen = frame.Len;
                    if (this.mDataLen > 8) {
                        this.mDataLen = 8;
                    }
                    System.arraycopy(frame.Data, 0, this.mData, 0, this.mDataLen);
                }
            }

            public void update(CCanFrame frame) {
                this.mInterval = frame.TimeStamp - this.mLastTimeStamp;
                if (this.mInterval == 0) {
                    this.mLastTimeStamp = frame.TimeStamp;
                    this.mCount++;
                    copyDataFromFrame(frame);
                } else {
                    this.mLastTimeStamp = frame.TimeStamp;
                    this.mCount++;
                    copyDataFromFrame(frame);
                }
            }

            public int getCount() {
                return this.mCount;
            }

            public int getInterval() {
                return this.mInterval;
            }

            public int compare(CCanFrame frame) {
                if (frame.ID > this.mFrameId) {
                    return 1;
                }
                if (frame.ID < this.mFrameId) {
                    return -1;
                }
                if (frame.RTR == this.mIsRequest) {
                    return 0;
                }
                if (frame.RTR) {
                    return 1;
                }
                return -1;
            }
        }

        public ArrayList<CDatItem> getDatList() {
            return this.mDatList;
        }

        public void clear() {
            this.mDatList.clear();
        }

        public void add(CCanFrame frame) {
            CDatItem item;
            int insPos = -1;
            int i = 0;
            while (i < this.mDatList.size()) {
                item = (CDatItem) this.mDatList.get(i);
                int compRes = item.compare(frame);
                if (compRes == 0) {
                    item.update(frame);
                    return;
                } else if (compRes == -1) {
                    insPos = i;
                    break;
                } else {
                    i++;
                }
            }
            item = new CDatItem(frame);
            if (insPos >= 0) {
                this.mDatList.add(insPos, item);
            } else {
                this.mDatList.add(item);
            }
        }
    }

    public class CDatTrace {
        private ArrayList<CDatItem> mBufList = new ArrayList();
        private ArrayList<CDatItem> mDatList = new ArrayList();
        private String mLastErrorString;

        public class CDatItem extends CDat {
            private int mTimeStamp;

            public CDatItem(CCanFrame frame) {
                super();
                this.mFrameId = frame.ID;
                this.mIsRequest = frame.RTR;
                this.mTimeStamp = frame.TimeStamp;
                this.mData = new byte[8];
                copyDataFromFrame(frame);
            }

            private void copyDataFromFrame(CCanFrame frame) {
                if (!this.mIsRequest) {
                    this.mDataLen = frame.Len;
                    if (this.mDataLen > 8) {
                        this.mDataLen = 8;
                    }
                    System.arraycopy(frame.Data, 0, this.mData, 0, this.mDataLen);
                }
            }

            public int getTimeStamp() {
                return this.mTimeStamp;
            }

            public double getTime() {
                return ((double) this.mTimeStamp) / 10000.0d;
            }
        }

        public ArrayList<CDatItem> getDatList() {
            return this.mDatList;
        }

        public void clear() {
            this.mDatList.clear();
            synchronized (this.mBufList) {
                this.mBufList.clear();
            }
        }

        public void add(CCanFrame frame) {
            synchronized (this.mBufList) {
                this.mBufList.add(new CDatItem(frame));
            }
        }

        public void update() {
            synchronized (this.mBufList) {
                this.mDatList.addAll(this.mBufList);
                this.mBufList.clear();
            }
        }

        public String getLastErrorString() {
            return this.mLastErrorString;
        }

        public int getCount() {
            return this.mDatList.size();
        }

        public CDatItem getItem(int index) {
            return (CDatItem) this.mDatList.get(index);
        }
    }

    private CDatHolder() {
        this.mTableList.add(new CDatTable());
        this.mTableList.add(new CDatTable());
        this.mTraceList = new ArrayList();
        this.mTraceList.add(new CDatTrace());
        this.mTraceList.add(new CDatTrace());
    }

    public static CDatHolder getInstance() {
        if (sDatHolder == null) {
            sDatHolder = new CDatHolder();
        }
        return sDatHolder;
    }

    public CDatTable getTable(int index) {
        if (index < 0 || index >= this.mTableList.size()) {
            return null;
        }
        return (CDatTable) this.mTableList.get(index);
    }

    public CDatTrace getTrace(int index) {
        if (index < 0 || index >= this.mTraceList.size()) {
            return null;
        }
        return (CDatTrace) this.mTraceList.get(index);
    }

    public void clearTables() {
        for (int i = 0; i < this.mTableList.size(); i++) {
            ((CDatTable) this.mTableList.get(i)).clear();
        }
    }

    public void clearTraces() {
        for (int i = 0; i < this.mTraceList.size(); i++) {
            ((CDatTrace) this.mTraceList.get(i)).clear();
        }
    }

    public void clearData() {
        clearTables();
        clearTraces();
    }
}
