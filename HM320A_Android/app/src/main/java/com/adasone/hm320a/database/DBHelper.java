package com.adasone.hm320a.database;

import android.content.Context;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;

public class DBHelper extends SQLiteOpenHelper {
    private static final String TAG = DBHelper.class.getSimpleName();
    public Context mContext = null;

    public static final String DATABASE_NAME = "hm320u.db";

    // 2017-05-16 : version 1. initialize

    public static final int DATABASE_VERSION = 1;

    public interface Tables {
        public static final String VEHICLE_LIST = "vehicle_list";
        public static final String VEHICLE_LIST_VERSION = "vehicle_list_version";
        public static final String DEVICE = "device";
        public static final String VEHICLE = "vehicle";
        public static final String DRIVER = "driver";
        public static final String CALIBRATION = "calibration";
        public static final String VIDEO = "video";
        public static final String FW_VER = "fw_ver";
        public static final String LATEST_DEVICE = "latest_device";
    }

    public interface VehicleListColumns {
        public static final String _ID = "_id";
        public static final String VEHICLE_TYPE = "vehicle_type";
        public static final String UNIQUE_ID = "unique_id";
        public static final String MANUFACTURER = "manufacturer";
        public static final String MODEL = "model";
        public static final String FUEL_TYPE = "fuel_type";
        public static final String RELEASE_YEAR = "release_year";
        public static final String OBD_MANUFACTURER_ID = "manufacturer_id";
        public static final String OBD_MODEL_ID = "model_id";
        public static final String OBD_FUEL_TYPE_ID = "fuel_type_id";
    }

    public interface VehicleListVersionColumns {
        public static final String _ID = "_id";
        public static final String TYPE = "type";
        public static final String CODE = "code";
    }

    public interface DeviceColumns {
        public static final String _ID = "_id";
        public static final String DEV_ID = "dev_id";
    }

    public interface VehicleColumns {
        public static final String _ID = "_id";
        public static final String DEV_ID = "dev_id";
        public static final String VEHICLE_TYPE = "v_vehicle_type";
        public static final String UNIQUE_ID = "v_unique_id";
        public static final String MANUFACTURER = "v_manufacturer";
        public static final String MODEL = "v_model";
        public static final String FUEL_TYPE = "v_fuel_type";
        public static final String RELEASE_YEAR = "v_release_year";
        public static final String OBD_MANUFACTURER_ID = "v_manufacturer_id";
        public static final String OBD_MODEL_ID = "v_model_id";
        public static final String OBD_FUEL_TYPE_ID = "v_fuel_type_id";
    }

    public interface DriverColumns {
        public static final String _ID = "_id";
        public static final String DEV_ID = "dev_id";
        public static final String VEHICLE_TYPE = "d_vehicle_type";
        public static final String VIN = "d_vin";
        public static final String VEHICLE_REG_NO = "d_vehicle_reg_no";
        public static final String BUSINESS_REG_NO = "d_business_reg_no";
        public static final String DRIVER_CODE = "d_driver_code";
    }

    public interface CalibrationColumns {
        public static final String _ID = "_id";
        public static final String DEV_ID = "dev_id";
        public static final String REG_DATE = "reg_date";
        public static final String CAMERA_HEIGHT = "camera_height";
        public static final String VEHICLE_WIDTH = "vehicle_width";
        public static final String CAMERA_TO_BUMPER = "camera_to_bumper";
        public static final String BONNET_Y = "bonnet_y";
        public static final String CENTER_X = "center_x";
        public static final String VANISHING_Y = "vanishing_y";
        public static final String NEAR_Y = "near_y";
        public static final String NEAR_LEFT_X = "near_left_x";
        public static final String NEAR_RIGHT_X = "near_right_x";
        public static final String FAR_Y = "far_y";
        public static final String FAR_LEFT_X = "far_left_x";
        public static final String FAR_RIGHT_X = "far_right_x";
        public static final String FAR_DISTANCE = "far_distance";
        public static final String NEAR_DISTANCE = "near_distance";
    }

    public interface VideoColumns {
        public static final String _ID = "_id";
        public static final String DEV_ID = "dev_id";
        public static final String TYPE = "type";
        public static final String FILE_NAME = "filename";
    }

    public interface FirmwareColumns {
        public static final String _ID = "_id";
        public static final String DEV_ID = "dev_id";
        public static final String VERSION_NAME = "version_name";
        public static final String VERSION_CODE = "version_code";
        public static final String DTG_SUPPORT = "dtg_support";
    }

    public interface LatestDevColumns {
        public static final String _ID = "_id";
        public static final String DEV_ID = "dev_id";
    }


    public DBHelper(Context context) {
        super(context, DATABASE_NAME, null, DATABASE_VERSION);
        mContext = context;
    }

    @Override
    public void onCreate(SQLiteDatabase db) {

        // Vehicle List
        db.execSQL("CREATE TABLE IF NOT EXISTS " + Tables.VEHICLE_LIST
                + " (" + VehicleListColumns._ID  + " INTEGER PRIMARY KEY AUTOINCREMENT"
                + "," + VehicleListColumns.VEHICLE_TYPE  + " TEXT NOT NULL"
                + "," + VehicleListColumns.UNIQUE_ID  + " TEXT NOT NULL UNIQUE"
                + "," + VehicleListColumns.MANUFACTURER + " TEXT NOT NULL"
                + "," + VehicleListColumns.MODEL + " TEXT NOT NULL"
                + "," + VehicleListColumns.FUEL_TYPE + " TEXT NOT NULL"
                + "," + VehicleListColumns.RELEASE_YEAR + " TEXT NOT NULL"
                + "," + VehicleListColumns.OBD_MANUFACTURER_ID + " TEXT NOT NULL"
                + "," + VehicleListColumns.OBD_MODEL_ID + " TEXT NOT NULL"
                + "," + VehicleListColumns.OBD_FUEL_TYPE_ID + " TEXT NOT NULL"
                + ");");

        // Vehicle List DB version
        db.execSQL("CREATE TABLE IF NOT EXISTS " + Tables.VEHICLE_LIST_VERSION
                + " (" + VehicleListVersionColumns._ID + " INTEGER PRIMARY KEY AUTOINCREMENT "
                + "," + VehicleListVersionColumns.TYPE + " TEXT"
                + "," + VehicleListVersionColumns.CODE + " TEXT"
                + ");");


        // Device
        db.execSQL("CREATE TABLE IF NOT EXISTS " + Tables.DEVICE
                + " (" + DeviceColumns._ID  + " INTEGER PRIMARY KEY AUTOINCREMENT"
                + "," + DeviceColumns.DEV_ID  + " TEXT NOT NULL UNIQUE"
                + ");");

        // Vehicle
        db.execSQL("CREATE TABLE IF NOT EXISTS " + Tables.VEHICLE
                + " (" + VehicleColumns._ID  + " INTEGER PRIMARY KEY AUTOINCREMENT"
                + "," + VehicleColumns.DEV_ID  + " TEXT NOT NULL UNIQUE"
                + "," + VehicleColumns.VEHICLE_TYPE  + " TEXT NOT NULL"
                + "," + VehicleColumns.UNIQUE_ID  + " TEXT NOT NULL UNIQUE"
                + "," + VehicleColumns.MANUFACTURER  + " TEXT NOT NULL"
                + "," + VehicleColumns.MODEL  + " TEXT NOT NULL"
                + "," + VehicleColumns.FUEL_TYPE  + " TEXT NOT NULL"
                + "," + VehicleColumns.RELEASE_YEAR  + " TEXT NOT NULL"
                + "," + VehicleColumns.OBD_MANUFACTURER_ID  + " TEXT NOT NULL"
                + "," + VehicleColumns.OBD_MODEL_ID  + " TEXT NOT NULL"
                + "," + VehicleColumns.OBD_FUEL_TYPE_ID  + " TEXT NOT NULL"
                + ");");

        // Driver
        db.execSQL("CREATE TABLE IF NOT EXISTS " + Tables.DRIVER
                + " (" + DriverColumns._ID  + " INTEGER PRIMARY KEY AUTOINCREMENT"
                + "," + DriverColumns.DEV_ID  + " TEXT NOT NULL UNIQUE"
                + "," + DriverColumns.VEHICLE_TYPE  + " INTEGER NOT NULL"
                + "," + DriverColumns.VIN  + " TEXT NOT NULL"
                + "," + DriverColumns.VEHICLE_REG_NO  + " TEXT NOT NULL"
                + "," + DriverColumns.BUSINESS_REG_NO  + " TEXT NOT NULL"
                + "," + DriverColumns.DRIVER_CODE  + " TEXT NOT NULL"
                + ");");

        // Calibration
        db.execSQL("CREATE TABLE IF NOT EXISTS " + Tables.CALIBRATION
                + " (" + CalibrationColumns._ID  + " INTEGER PRIMARY KEY AUTOINCREMENT"
                + "," + CalibrationColumns.DEV_ID  + " TEXT NOT NULL UNIQUE"
                + "," + CalibrationColumns.REG_DATE  + " TEXT NOT NULL"
                + "," + CalibrationColumns.CAMERA_HEIGHT  + " INTEGER NOT NULL"
                + "," + CalibrationColumns.VEHICLE_WIDTH  + " INTEGER NOT NULL"
                + "," + CalibrationColumns.CAMERA_TO_BUMPER  + " INTEGER NOT NULL"
                + "," + CalibrationColumns.BONNET_Y  + " INTEGER NOT NULL"
                + "," + CalibrationColumns.CENTER_X  + " INTEGER NOT NULL"
                + "," + CalibrationColumns.VANISHING_Y  + " INTEGER NOT NULL"
                + "," + CalibrationColumns.NEAR_Y  + " INTEGER NOT NULL"
                + "," + CalibrationColumns.NEAR_LEFT_X  + " INTEGER NOT NULL"
                + "," + CalibrationColumns.NEAR_RIGHT_X  + " INTEGER NOT NULL"
                + "," + CalibrationColumns.FAR_Y  + " INTEGER NOT NULL"
                + "," + CalibrationColumns.FAR_LEFT_X  + " INTEGER NOT NULL"
                + "," + CalibrationColumns.FAR_RIGHT_X  + " INTEGER NOT NULL"
                + "," + CalibrationColumns.FAR_DISTANCE  + " INTEGER NOT NULL"
                + "," + CalibrationColumns.NEAR_DISTANCE  + " INTEGER NOT NULL"
                + ");");

        // Video List
        db.execSQL("CREATE TABLE IF NOT EXISTS " + Tables.VIDEO
                + " (" + VideoColumns._ID  + " INTEGER PRIMARY KEY AUTOINCREMENT"
                + "," + VideoColumns.DEV_ID  + " TEXT NOT NULL"
                + "," + VideoColumns.TYPE  + " INTEGER NOT NULL"
                + "," + VideoColumns.FILE_NAME  + " TEXT NOT NULL"
                + ");");

        // Firmware version
        db.execSQL("CREATE TABLE IF NOT EXISTS " + Tables.FW_VER
                + " (" + FirmwareColumns._ID  + " INTEGER PRIMARY KEY AUTOINCREMENT"
                + "," + FirmwareColumns.DEV_ID  + " TEXT NOT NULL UNIQUE"
                + "," + FirmwareColumns.VERSION_NAME  + " TEXT NOT NULL"
                + "," + FirmwareColumns.VERSION_CODE  + " TEXT NOT NULL"
                + "," + FirmwareColumns.DTG_SUPPORT  + " TEXT NOT NULL"
                + ");");

        // Latest connection device
        db.execSQL("CREATE TABLE IF NOT EXISTS " + Tables.LATEST_DEVICE
                + " (" + LatestDevColumns._ID  + " INTEGER PRIMARY KEY AUTOINCREMENT"
                + "," + LatestDevColumns.DEV_ID  + " TEXT NOT NULL UNIQUE"
                + ");");
    }

    @Override
    public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
        // TODO Auto-generated method stub
        onCreate(db);
    }
}
