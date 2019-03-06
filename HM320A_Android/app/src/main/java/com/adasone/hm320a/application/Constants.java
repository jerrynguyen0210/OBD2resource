package com.adasone.hm320a.application;


public class Constants {

    public class Fonts {
        public static final String HYN_GOTHIC_MEDIUM = "fonts/H2NGTM.ttf";
        public static final String HY_SUPUNG_BOLD = "fonts/H2SUPB.ttf";
        public static final String HY_GOTHIC_A1_400 = "fonts/HYGothic_A1_400.ttf";
        public static final String HY_GOTHIC_A1_500 = "fonts/HYGothic_A1_500.ttf";
        public static final String HY_GOTHIC_A1_600 = "fonts/HYGothic_A1_600.ttf";
        public static final String HY_GOTHIC_A1_700 = "fonts/HYGothic_A1_700.ttf";
        public static final String HY_GOTHIC_A1_800 = "fonts/HYGothic_A1_800.ttf";
        public static final String HY_GOTHIC_A1_900 = "fonts/HYGothic_A1_900.ttf";
    }

    public class Extra {
        public static final String VIDEO_LIST = "video_list";
        public static final String SELECT_VIDEO = "select_video";
    }

    public class Menu {
        public static final int APP_FINISH = 1;

        public static final int VEHICLE = 10;
        public static final int VEHICLE_INFO_EDIT = 11;

        public static final int CALIBRATION_CAM_LOCATION = 20;
        public static final int CALIBRATION_BONNET = 21;
        public static final int CALIBRATION_VANISH = 22;
        public static final int CALIBRATION_DISTANCE = 23;
        public static final int CALIBRATION_TOTAL = 24;

        public static final int AUTO_CALIBRATION_CHESS = 25;
        public static final int AUTO_CALIBRATION_BONNET = 26;
        public static final int AUTO_CALIBRATION_TOTAL = 27;

        public static final int DTG = 30;
        public static final int DTG_DRIVER_INFO_EDIT = 31;

        public static final int VIDEO = 40;

        public static final int FIRMWARE = 50;
    }

    public class Anim {
        public static final int NONE = 0;
        public static final int SLIDING = 1;
        public static final int FADE = 2;
    }

    public class AttachMode {
        public static final int USB_CONN = 1;
        public static final int AFTER_APP_RUNNING = 2;
        public static final int LAUNCH_APP = 3;
    }

    public class DtgSupport {
        public static final int HW_NOT_SUPPORTED = 0;
        public static final int HW_SUPPORTED = 1;
        public static final int HW_SW_SUPPORTED = 2;
    }

    public class VehicleType {
        public static final int CITY_BUS = 11;
        public static final int RURAL_BUS = 12;
        public static final int TOWN_BUS = 13;
        public static final int INTERCITY_BUS = 14;
        public static final int EXPRESS_BUS = 15;
        public static final int CHARTERED_BUS = 16;
        public static final int SPECIAL_PASSENGER_VEHICLE = 17;
        public static final int REGULAR_TAXI = 21;
        public static final int PRIVATE_TAXI = 22;
        public static final int GENERAL_LORRY = 31;
        public static final int INDIVIDUAL_LORRY = 32;
        public static final int NON_COMMERCIAL_VEHICLE = 41;
    }

    public class Zoom {
        public static final float LEVEL1 = 1.0f;
        public static final float LEVEL2 = 2.0f;
        public static final float LEVEL3 = 4.0f;
        public static final float LEVEL4 = 6.0f;
    }

    public static final long RESP_TIMEOUT = 5000;

    public static final String DEFAULT_DEV_CODE = "dev0001";

    public static final String DIR_STR = "/";
    public static final String HM320A_DIR = "/hm320a";
    public static final String LOG_DIR = HM320A_DIR + "/logs";
    public static final String FW_DIR = HM320A_DIR + "/firmware";
    public static final String DEVICE_DIR = HM320A_DIR + "/device";

    //  Device sub-dir
    public static final String CALIBRATION_DIR = "/calibration";
    public static final String VIDEO_DIR = "/video";
    public static final String VIDEO_NORMAL_DIR = VIDEO_DIR + "/normal";
    public static final String VIDEO_EVENT_DIR = VIDEO_DIR + "/event";

    public class BundleKey {
    }

    public class NotifyMsg {
        public static final int CHANGE_VEHICLE_INFO = 10001;
        public static final int CHANGE_DTG_DRIVER_INFO = 10002;
        public static final int VIDEO_COUNT_REFRESH = 10003;
        public static final int CALIBRATION_IMAGE_REFRESH = 10004;
    }
}
