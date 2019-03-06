package com.adasone.hm320a.canbusanalyzer;

import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.SharedPreferences.Editor;
import android.hardware.usb.UsbDevice;
import android.hardware.usb.UsbDeviceConnection;
import android.hardware.usb.UsbEndpoint;
import android.hardware.usb.UsbInterface;
import android.hardware.usb.UsbManager;
import android.os.SystemClock;
import android.preference.PreferenceManager;
import android.support.v4.internal.view.SupportMenu;
import android.widget.Toast;

import com.adasone.hm320a.R;

import java.util.HashMap;

import com.adasone.hm320a.canbusanalyzer.CCanBusInterface.CCanFrame;
import com.adasone.hm320a.canbusanalyzer.CCanBusInterface.CCanRate;
import com.adasone.hm320a.canbusanalyzer.CDatHolder.CDatTable;
import com.adasone.hm320a.canbusanalyzer.CDatHolder.CDatTrace;
import com.adasone.hm320a.canbusanalyzer.CLogger.IOnLogStr;

//import obd2scantool.R;

public class CAdapter {
    public static final String ACTION_LOG_STR = "vn.penkun.obd2scantool.canbusanalyzer.CADAPTER_LOG_STR";
    public static final String ACTION_USB_PERMISSION = "vn.penkun.obd2scantool.canbusanalyzer.USB_PERMISSION";
    public static final int CAN_CHANNEL_1 = 0;
    public static final int CAN_CHANNEL_2 = 1;
    protected static final int CHANNEL_LOG_LEVEL_DATA = 1;
    protected static final int CHANNEL_LOG_LEVEL_GENERAL = 0;
    protected static final int CHANNEL_LOG_LEVEL_NONE = -1;
    public static final String EXTRA_LOG_STR = "logstr";
    public static final int[] PRODUCT_ID = new int[]{82, 83, 95};
    public static final int USB_SEND_MAX_TIMEOUT = 1000;
    public static final int VENDOR_ID = 1240;
    protected static int mChannelLogLevel = 1;
    private static CAdapter sAdapter;
    private CCanBusInterface mCanBusInterface = CCanBusInterface.getInstance();
    private CChannel mChannel1 = new CChannel(0);
    private CChannel mChannel2 = new CChannel(1);
    private CChannel[] mChannelArr = new CChannel[2];
    private UsbDeviceConnection mConnection;
    private Context mContext = null;
    private CDatHolder mDatHolder = CDatHolder.getInstance();
    private UsbDevice mDevice;
    private UsbInterface mInterface;
    private IOnLogStr mOnLogStr;
    private IOnStateChanged mOnStateChanged = null;
    private boolean mStateOn = false;
    private UsbManager mUsbManager;

    public class CChannel {
        private CCanSettings mCanSettings = new CCanSettings();
        private CDatTable mDatTable;
        private CDatTrace mDatTrace;
        private UsbEndpoint mEndpointIn1;
        private UsbEndpoint mEndpointOut1;
        private byte[] mGetBuf = new byte[64];
        private int mIndex;
        private boolean mIsRunning = false;
        private CReadThread mReadThread;
        private byte[] mSendBuf = new byte[64];

        public class CCanSettings {
            private static final int DEFAULT_RATE = 80;
            private static final String KEY_PREF_IS_EXTENDED = "CanSettings.IsExtended";
            private static final String KEY_PREF_RATE_INDEX = "CanSettings.RateIndex";
            private boolean mIsExtended;
            private CCanRate mRate;

            private void load() {
                SharedPreferences pref = PreferenceManager.getDefaultSharedPreferences(CGeneral.context);
                String addS = Integer.toString(CChannel.this.mIndex);
                this.mIsExtended = pref.getBoolean(KEY_PREF_IS_EXTENDED + addS, false);
                this.mRate = CAdapter.this.mCanBusInterface.getRateByIndex(pref.getInt(KEY_PREF_RATE_INDEX + addS, 80));
                if (this.mRate == null) {
                    this.mRate = CAdapter.this.mCanBusInterface.getRateByIndex(80);
                }
            }

            public void save() {
                Editor editor = PreferenceManager.getDefaultSharedPreferences(CGeneral.context).edit();
                String addS = Integer.toString(CChannel.this.mIndex);
                editor.putBoolean(KEY_PREF_IS_EXTENDED + addS, this.mIsExtended);
                editor.putInt(KEY_PREF_RATE_INDEX + addS, this.mRate.rate);
                editor.commit();
            }

            public CCanSettings() {
                load();
            }

            public boolean isExtended() {
                return this.mIsExtended;
            }

            public void setIsExtended(boolean value) {
                this.mIsExtended = value;
            }

            public CCanRate getRate() {
                return this.mRate;
            }

            public void setRate(CCanRate value) {
                this.mRate = value;
            }
        }

        private class CReadThread extends Thread {
            private static final int FRAME_DATA_LEN = 21;
            private byte[] mFrameBuf;
            private boolean mIsFinish;
            private int mReadCnt;

            private CReadThread() {
                this.mReadCnt = 0;
                this.mIsFinish = false;
            }

            private void parseFrameBuffer() {
                CCanFrame frame = new CCanFrame();
                frame.ID = CTools.getDWordFromBytes(this.mFrameBuf, 0);
                frame.TimeStamp = CTools.getDWordFromBytes(this.mFrameBuf, 4);
                frame.RTR = this.mFrameBuf[10] != (byte) 0;
                frame.Len = this.mFrameBuf[12];
                if (frame.Len > 8) {
                    frame.Len = 8;
                }
                System.arraycopy(this.mFrameBuf, 13, frame.Data, 0, frame.Len);
                CChannel.this.mDatTable.add(frame);
                CChannel.this.mDatTrace.add(frame);
            }

            public void run() {
                super.run();
                this.mFrameBuf = new byte[21];
                sendStartPacket();
                while (!this.mIsFinish) {
                    if (CChannel.this.getData(CChannel.this.mGetBuf, 64, 5)) {
                        byte frameCnt = CChannel.this.mGetBuf[0];
                        if (frameCnt < (byte) 1 || frameCnt > (byte) 3) {
                            SystemClock.sleep(1);
                        } else {
                            for (byte i = (byte) 0; i < frameCnt; i++) {
                                System.arraycopy(CChannel.this.mGetBuf, (i * 21) + 1, this.mFrameBuf, 0, 21);
                                parseFrameBuffer();
                            }
                        }
                    } else {
                        SystemClock.sleep(10);
                    }
                }
            }

            public void finish() {
                this.mIsFinish = true;
            }

            private void sendStartPacket() {
                CChannel.this.clearSendBuf();
                CTools.setDWordToBytes(2, CChannel.this.mSendBuf, 0);
                CTools.setDWordToBytes(2088763392, CChannel.this.mSendBuf, 4);
                CTools.setDWordToBytes(2089811968, CChannel.this.mSendBuf, 8);
                CTools.setDWordToBytes(16775884, CChannel.this.mSendBuf, 12);
                CTools.setDWordToBytes(16775884, CChannel.this.mSendBuf, 16);
                CTools.setDWordToBytes(16775890, CChannel.this.mSendBuf, 20);
                CTools.setDWordToBytes(2089909823, CChannel.this.mSendBuf, 24);
                CTools.setDWordToBytes(2090316152, CChannel.this.mSendBuf, 28);
                CTools.setDWordToBytes(2089909737, CChannel.this.mSendBuf, 32);
                CTools.setDWordToBytes(13020872, CChannel.this.mSendBuf, 36);
                CTools.setDWordToBytes(13324512, CChannel.this.mSendBuf, 40);
                CTools.setDWordToBytes(SupportMenu.USER_MASK, CChannel.this.mSendBuf, 44);
                CTools.setDWordToBytes(2147344384, CChannel.this.mSendBuf, 48);
                CTools.setDWordToBytes(2372776, CChannel.this.mSendBuf, 52);
                CTools.setDWordToBytes(16775926, CChannel.this.mSendBuf, 56);
                CTools.setDWordToBytes(2088773164, CChannel.this.mSendBuf, 60);
                CChannel.this.sendData(CChannel.this.mSendBuf, 64, 5);
            }
        }

        private void clearSendBuf() {
            for (int i = 0; i < this.mSendBuf.length; i++) {
                this.mSendBuf[i] = (byte) 0;
            }
        }

        private int sendData(byte[] buffer, int length, int timeout) {
            if (timeout == 0) {
                timeout = CAdapter.USB_SEND_MAX_TIMEOUT;
            }
            int res = CAdapter.this.mConnection.bulkTransfer(this.mEndpointOut1, buffer, length, timeout);
            if (CAdapter.mChannelLogLevel >= 1) {
                logStrChannel("SendRes = " + res, 1);
                if (res >= 2) {
                    String S = "SendData ";
                    for (int i = 0; i < Math.min(res, 64); i++) {
                        if (i % 8 == 0) {
                            if (i % 16 == 0) {
                                S = S + "\n";
                            } else {
                                S = S + "  ";
                            }
                        }
                        S = S + YarConverter.ByteToHex(buffer[i]) + " ";
                    }
                    logStrChannel(S, 1);
                }
            }
            return res;
        }

        private boolean getData(byte[] buffer, int length, int timeout) {
            return CAdapter.this.mConnection.bulkTransfer(this.mEndpointIn1, buffer, length, timeout) != -1;
        }

        public void setEndpoint(UsbEndpoint endpoint, boolean isOut, int index) {
            if (isOut) {
                switch (index) {
                    case 1:
                        this.mEndpointOut1 = endpoint;
                        return;
                    default:
                        return;
                }
            }
            switch (index) {
                case 1:
                    this.mEndpointIn1 = endpoint;
                    return;
                default:
                    return;
            }
        }

        protected CChannel(int index) {
            this.mIndex = index;
            CDatHolder datHolder = CDatHolder.getInstance();
            this.mDatTable = datHolder.getTable(this.mIndex);
            this.mDatTrace = datHolder.getTrace(this.mIndex);
        }

        public CCanSettings getCanSettings() {
            return this.mCanSettings;
        }

        private void logStrChannel(String s, int logLevel) {
            if (logLevel <= CAdapter.mChannelLogLevel) {
                String s1 = "Channel" + Integer.toString(this.mIndex + 1) + ": " + s;
                Intent i = new Intent(CAdapter.ACTION_LOG_STR);
                i.putExtra(CAdapter.EXTRA_LOG_STR, s1);
                CGeneral.context.sendBroadcast(i);
            }
        }

        public boolean start() {
            logStrChannel("start", 0);
            logStrChannel("rate " + this.mCanSettings.getRate().toString(), 0);
            open();
            this.mReadThread = new CReadThread();
            this.mReadThread.start();
            this.mIsRunning = true;
            return true;
        }

        public boolean open() {
            logStrChannel("open", 0);
            clearSendBuf();
            CTools.setDWordToBytes(1, this.mSendBuf, 0);
            CTools.setDWordToBytes(0, this.mSendBuf, 4);
            CTools.setDWordToBytes(-1, this.mSendBuf, 8);
            CTools.setDWordToBytes(0, this.mSendBuf, 12);
            CTools.setDWordToBytes(0, this.mSendBuf, 16);
            CTools.setDWordToBytes(0, this.mSendBuf, 20);
            CTools.setDWordToBytes(this.mCanSettings.getRate().getBTR0(), this.mSendBuf, 24);
            CTools.setDWordToBytes(this.mCanSettings.getRate().getBTR1(), this.mSendBuf, 28);
            CTools.setDWordToBytes(0, this.mSendBuf, 32);
            CTools.setDWordToBytes(1, this.mSendBuf, 36);
            CTools.setDWordToBytes(268448135, this.mSendBuf, 40);
            CTools.setDWordToBytes(0, this.mSendBuf, 44);
            CTools.setDWordToBytes(128, this.mSendBuf, 48);
            CTools.setDWordToBytes(268448071, this.mSendBuf, 52);
            CTools.setDWordToBytes(10, this.mSendBuf, 56);
            CTools.setDWordToBytes(4260618, this.mSendBuf, 60);
            sendData(this.mSendBuf, 64, 5);
            return true;
        }

        public void stop() {
            logStrChannel("stop", 0);
            if (this.mIsRunning) {
                this.mReadThread.finish();
                try {
                    this.mReadThread.join(1000);
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
            }
            this.mIsRunning = false;
        }

        public boolean isRunning() {
            return this.mIsRunning;
        }
    }

    public interface IOnStateChanged {
        void onStateChanged(boolean z);
    }

    public boolean isRunning() {
        for (int i = 0; i < this.mChannelArr.length; i++) {
            if (this.mChannelArr[0].isRunning()) {
                return true;
            }
        }
        return false;
    }

    public CChannel getChannel(int channelIndex) {
        switch (channelIndex) {
            case 0:
                return this.mChannel1;
            case 1:
                return this.mChannel2;
            default:
                return null;
        }
    }

    private void logStr(String s) {
        if (this.mOnLogStr != null) {
            this.mOnLogStr.onLogStr(s);
        }
    }

    private CAdapter() {
        this.mChannelArr[0] = this.mChannel1;
        this.mChannelArr[1] = this.mChannel2;
    }

    public static CAdapter getInstance() {
        if (sAdapter == null) {
            sAdapter = new CAdapter();
        }
        return sAdapter;
    }

    public void setOnLogStr(IOnLogStr onLogStr) {
        this.mOnLogStr = onLogStr;
    }

    public void setOnStateChanged(IOnStateChanged onStateChanged) {
        this.mOnStateChanged = onStateChanged;
    }

    public void test() {
        logStr("CAdaper. Test");
    }

    public void init(Context context) {
        this.mContext = context;
        this.mUsbManager = (UsbManager) this.mContext.getSystemService("usb");
        if (this.mUsbManager == null) {
            logStr("ERROR! USB_SERVICE not supported");
        }
    }

    public void processReceiveIntent(Intent intent) {
        if ("android.hardware.usb.action.USB_DEVICE_DETACHED".equals(intent.getAction())) {
            logStr("USB device detached");
            UsbDevice device = (UsbDevice) intent.getParcelableExtra("device");
            if (device != null && device.getVendorId() == VENDOR_ID) {
                logStr("CanBusAdapter was detached");
                setStateOn(false);
            }
        }
    }

    private void showMessage(int resId) {
        if (this.mContext != null) {
            Toast.makeText(this.mContext, resId, Toast.LENGTH_LONG).show();
        }
    }

    private boolean checkProductId(int id) {
        for (int pId : PRODUCT_ID) {
            if (pId == id) {
                return true;
            }
        }
        return false;
    }

    public void check() {
        HashMap<String, UsbDevice> deviceList = this.mUsbManager.getDeviceList();
        logStr("Usb devices count:" + deviceList.size());
        for (UsbDevice device : deviceList.values()) {
            logStr("VendorID: " + device.getVendorId() + ", ProductId: " + device.getProductId());
            if (device.getVendorId() == VENDOR_ID && checkProductId(device.getProductId())) {
                logStr("CanBusAdapter is found");
                this.mDevice = device;
                if (this.mUsbManager.hasPermission(this.mDevice)) {
                    setDevice(false);
                    return;
                }
                logStr("No permission for device");
                if (this.mContext != null) {
                    this.mUsbManager.requestPermission(this.mDevice, PendingIntent.getBroadcast(this.mContext, 0, new Intent(ACTION_USB_PERMISSION), 0));
                    return;
                }
                logStr("Can't request permission. No context");
                return;
            }
        }
        showMessage(R.string.str_msg_adapter_not_connected);
    }

    private void setDevice(boolean makeLog) {
        if (makeLog) {
            logStr("SetDevice " + this.mDevice);
        }
        int interfaceCount = this.mDevice.getInterfaceCount();
        if (makeLog) {
            logStr("interfaceCount = " + Integer.toString(interfaceCount));
        }
        if (interfaceCount == 0) {
            logStr("No interface enabled! Please reconnect adapter.");
        }
        int intefraceIndex = -1;
        for (int n = 0; n < interfaceCount; n++) {
            UsbInterface intf = this.mDevice.getInterface(n);
            if (makeLog) {
                logStr(intf.toString());
                logStr("");
            }
            if (intf.getInterfaceClass() == 255) {
                intefraceIndex = n;
                break;
            }
        }
        if (intefraceIndex >= 0) {
            this.mInterface = this.mDevice.getInterface(intefraceIndex);
            if (this.mInterface.getEndpointCount() != 0) {
                if (makeLog) {
                    logStr("Endpoints Count: " + this.mInterface.getEndpointCount());
                }
                for (int i = 0; i < this.mInterface.getEndpointCount(); i++) {
                    UsbEndpoint endpoint = this.mInterface.getEndpoint(i);
                    int eType = endpoint.getType();
                    int eDir = endpoint.getDirection();
                    int eNum = endpoint.getEndpointNumber();
                    if (makeLog) {
                        logStr("Endpoint " + i + " type = " + eType + " dir = " + eDir + " num = " + eNum);
                    }
                    if (eType == 2) {
                        if (eDir == 128) {
                            if (makeLog) {
                                logStr("IN endpoint: " + this.mInterface.getEndpoint(i));
                            }
                            switch (eNum) {
                                case 1:
                                    this.mChannelArr[0].setEndpoint(endpoint, false, 1);
                                    break;
                                case 3:
                                    this.mChannelArr[1].setEndpoint(endpoint, false, 1);
                                    break;
                                default:
                                    break;
                            }
                        }
                        switch (eNum) {
                            case 2:
                                this.mChannelArr[0].setEndpoint(endpoint, true, 1);
                                break;
                            case 4:
                                this.mChannelArr[1].setEndpoint(endpoint, true, 1);
                                break;
                        }
                        if (makeLog) {
                            logStr("OUT endpoint: " + this.mInterface.getEndpoint(i));
                        }
                    }
                }
                if (this.mDevice != null) {
                    this.mConnection = this.mUsbManager.openDevice(this.mDevice);
                    if (this.mConnection == null || !this.mConnection.claimInterface(this.mInterface, true)) {
                        if (makeLog) {
                            logStr("open device FAIL!");
                        }
                        this.mConnection = null;
                        return;
                    }
                    if (makeLog) {
                        logStr("open device SUCCESS!");
                    }
                    setStateOn(true);
                }
            } else if (makeLog) {
                logStr("could not find endpoint");
            }
        } else if (makeLog) {
            logStr("could not find interface");
        }
    }

    private void setStateOn(boolean stateOn) {
        if (this.mStateOn != stateOn) {
            this.mStateOn = stateOn;
            if (!this.mStateOn) {
                for (CChannel ch : this.mChannelArr) {
                    if (ch.isRunning()) {
                        ch.stop();
                    }
                }
            }
            if (this.mOnStateChanged != null) {
                this.mOnStateChanged.onStateChanged(this.mStateOn);
            }
        }
    }

    public boolean isStateOn() {
        return this.mStateOn;
    }
}
