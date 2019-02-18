package com.adasone.hm320a.util;


public class CommUtil {

    public static class Usb {
        public static final String API_VER = "1";
        public static final String CHAR_SPLIT = ",";

        public static final int POS_TYPE = 0;
        public static final int POS_CMD = 1;
        public static final int POS_RESULT = 2;
        public static final int ERR_CODE = 3;

        public static final String DATA_TYPE_REQ = "REQ";
        public static final String DATA_TYPE_RESP = "RES";
        public static final String CHAR_RESP_SUCCESS = "S";
        public static final String CHAR_RESP_FAIL = "F";
        public static final String END_MARK = "END";
        public static final String CHAR_NORMAL_VIDEO = "N";
        public static final String CHAR_EVENT_VIDEO = "E";

        public static final String CMD_CODE_READ_VEHICLE_INFO = "101";
        public static final String CMD_CODE_WRITE_VEHICLE_INFO = "102";

        public static final String CMD_CODE_READ_CALIBRATION_INFO = "201";
        public static final String CMD_CODE_WRITE_CALIBRATION_INFO = "202";
        public static final String CMD_CODE_RECEIVE_CALIBRATION_NORMAL_PIC = "203";
        public static final String CMD_CODE_RECEIVE_CALIBRATION_AUTO_FIRST_PIC = "204";
        public static final String CMD_CODE_RECEIVE_CALIBRATION_AUTO_SECOND_PIC = "205";

        public static final String CMD_CODE_READ_DTG_DRIVER_INFO = "301";
        public static final String CMD_CODE_WRITE_DTG_DRIVER_INFO = "302";

        public static final String CMD_CODE_VIDEO_LIST = "401";
        public static final String CMD_CODE_DOWNLOAD_VIDEO = "402";
        public static final String CMD_CODE_DOWNLOAD_CANCEL = "403";


        public static final String CMD_CODE_VERSION_INFO = "501";
        public static final String CMD_CODE_SEND_FW_FILE = "502";

        public static final String CMD_CODE_SOFT_CLOSE = "601";

        public static final String REASON_NOT_SUPPORTED = "01";
        public static final String REASON_BUSY = "02";
        public static final String REASON_NOT_EXIST_FILE = "03";
        public static final String REASON_SAVE_FAIL = "04";
        public static final String REASON_INVALID_ARG = "05";
        public static final String REASON_FILE_SIZE_ZERO = "06";
    }

}