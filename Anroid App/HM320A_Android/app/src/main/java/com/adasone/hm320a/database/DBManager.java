package com.adasone.hm320a.database;

import android.content.ContentValues;
import android.database.Cursor;
import android.database.SQLException;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteException;
import android.database.sqlite.SQLiteOpenHelper;
import android.graphics.Point;
import android.util.Log;

import com.adasone.hm320a.application.Constants;
import com.adasone.hm320a.data.CalibrationData;
import com.adasone.hm320a.data.DriverData;
import com.adasone.hm320a.data.VehicleData;
import com.adasone.hm320a.data.VideoData;

import org.json.JSONArray;
import org.json.JSONException;

import java.text.ParseException;
import java.util.ArrayList;
import java.util.concurrent.atomic.AtomicInteger;

/**
 * reference : http://www.dmytrodanylyk.com/concurrent-database-access/
 */

public class DBManager {
    private static final String TAG = DBManager.class.getSimpleName();

    private AtomicInteger mOpenCounter = new AtomicInteger();
    private static DBManager instance;
    private static SQLiteOpenHelper mDatabaseHelper;
    private SQLiteDatabase mDatabase;

    public static synchronized void initializeInstance(SQLiteOpenHelper helper) {
        if (instance == null) {
            instance = new DBManager();
            mDatabaseHelper = helper;
        }
    }

    public static synchronized DBManager getInstance() {
        if (instance == null) {
           throw new IllegalStateException(DBManager.class.getSimpleName() +
                    " is not initialized, call initializeInstance(..) method first.");
        }

        return instance;
    }

    private synchronized SQLiteDatabase openDatabase() {
        if(mOpenCounter.incrementAndGet() == 1) {
            // Opening new database
            mDatabase = mDatabaseHelper.getWritableDatabase();
        }
        return mDatabase;
    }

    private synchronized void closeDatabase() {
        if(mOpenCounter.decrementAndGet() == 0) {
            // Closing database
            mDatabase.close();
        }
    }

    /**********************************************************************************************/
    ////    Common
    /**********************************************************************************************/
    public void clearTable(String tableName) {
        try {
            openDatabase().execSQL("DELETE FROM " + tableName + ";");
        } catch (SQLException e) {
            e.printStackTrace();
        } finally {
            closeDatabase();
        }
    }
    //*********************************************************************************************/
    //    Vehicle list
    //*********************************************************************************************/
    public boolean insertVehicleListTransaction(int vehicleType, String manufacture, JSONArray jsonArray) {
        boolean ret = true;
        long start = 0;
        long end = 0;
        int num = 0;

        SQLiteDatabase db = openDatabase();
        try {
            start = System.currentTimeMillis();
            ContentValues cv = new ContentValues();
            db.beginTransaction();
            for (int i=0; i < jsonArray.length(); i++) {
                JSONArray itemJSONArray = jsonArray.getJSONArray(i);
                cv.put(DBHelper.VehicleListColumns.VEHICLE_TYPE, String.valueOf(vehicleType));
                cv.put(DBHelper.VehicleListColumns.UNIQUE_ID, itemJSONArray.getString(0));
                cv.put(DBHelper.VehicleListColumns.MANUFACTURER, manufacture);
                cv.put(DBHelper.VehicleListColumns.MODEL, itemJSONArray.getString(1));
                cv.put(DBHelper.VehicleListColumns.FUEL_TYPE, itemJSONArray.getString(2));
                cv.put(DBHelper.VehicleListColumns.RELEASE_YEAR, itemJSONArray.getString(3));
                cv.put(DBHelper.VehicleListColumns.OBD_MANUFACTURER_ID, itemJSONArray.getString(4));
                cv.put(DBHelper.VehicleListColumns.OBD_MODEL_ID, itemJSONArray.getString(5));
                cv.put(DBHelper.VehicleListColumns.OBD_FUEL_TYPE_ID, itemJSONArray.getString(6));
                db.insert(DBHelper.Tables.VEHICLE_LIST, null, cv);
                num++;
            }
            db.setTransactionSuccessful();
        } catch (SQLException | JSONException e) {
            ret = false;
            e.printStackTrace();
        } finally {
            db.endTransaction();
            closeDatabase();
            end = System.currentTimeMillis();
            Log.i(TAG, "Vehicle list insert complete : " + (end - start) + " ms, " + num + "rows");
        }
        return ret;
    }

    public boolean deleteVehicleListTransaction(int vehicleType) {
        boolean ret = true;
        long start = 0;
        long end = 0;
        int affected_rows = -1;

        String[] args = { String.valueOf(vehicleType) };

        SQLiteDatabase db = openDatabase();
        try {
            start = System.currentTimeMillis();
            db.beginTransaction();

            affected_rows = db.delete(DBHelper.Tables.VEHICLE_LIST,
                    DBHelper.VehicleListColumns.VEHICLE_TYPE + "=?", args);

            db.setTransactionSuccessful();
        } catch (SQLException e) {
            ret = false;
            e.printStackTrace();
        } finally {
            db.endTransaction();
            closeDatabase();
            end = System.currentTimeMillis();
            Log.i(TAG, "Vehicle list delete complete : " + (end - start) + " ms, " + affected_rows + "rows");
        }
        return ret;
    }

    public ArrayList<String> getModelList(int vehicleType, String manufacturer) {
        ArrayList<String> mArrayList = new ArrayList<>();

        ArrayList<String> tmpData = new ArrayList<>();
        String[] args = { String.valueOf(vehicleType), manufacturer };

        String query = "SELECT DISTINCT " + DBHelper.VehicleListColumns.MODEL
                + " FROM " + DBHelper.Tables.VEHICLE_LIST
                + " WHERE " + DBHelper.VehicleListColumns.VEHICLE_TYPE + "=?"
                + " AND " + DBHelper.VehicleListColumns.MANUFACTURER + "=?";

        try {
            Cursor cursor = openDatabase().rawQuery(query, args);
            while (cursor.moveToNext()) {
                mArrayList.add(cursor.getString(0));
            }
            cursor.close();
        } catch (SQLException e) {
            e.printStackTrace();
        } finally {
            closeDatabase();
        }
        return mArrayList;
    }

    public ArrayList<String> getFuelTypeList(int vehicleType, String manufacturer, String model) {
        ArrayList<String> mArrayList = new ArrayList<>();

        String[] args = { String.valueOf(vehicleType), manufacturer, model };

        String query = "SELECT DISTINCT " + DBHelper.VehicleListColumns.FUEL_TYPE
                + " FROM " + DBHelper.Tables.VEHICLE_LIST
                + " WHERE " + DBHelper.VehicleListColumns.VEHICLE_TYPE + "=?"
                + " AND " + DBHelper.VehicleListColumns.MANUFACTURER + "=?"
                + " AND " + DBHelper.VehicleListColumns.MODEL + "=?";
        try {
            Cursor cursor = openDatabase().rawQuery(query, args);
            while (cursor.moveToNext()) {
                mArrayList.add(cursor.getString(0));
            }
            cursor.close();
        } catch (SQLException e) {
            e.printStackTrace();
        } finally {
            closeDatabase();
        }
        return mArrayList;
    }

    public ArrayList<String> getReleaseDateList(int vehicleType, String manufacturer, String model, String fuelType) {
        ArrayList<String> mArrayList = new ArrayList<>();

        String[] args = { String.valueOf(vehicleType), manufacturer, model, fuelType };

        String query = "SELECT DISTINCT " + DBHelper.VehicleListColumns.RELEASE_YEAR
                + " FROM " + DBHelper.Tables.VEHICLE_LIST
                + " WHERE " + DBHelper.VehicleListColumns.VEHICLE_TYPE + "=?"
                + " AND " + DBHelper.VehicleListColumns.MANUFACTURER + "=?"
                + " AND " + DBHelper.VehicleListColumns.MODEL + "=?"
                + " AND " + DBHelper.VehicleListColumns.FUEL_TYPE + "=?";
        try {
            Cursor cursor = openDatabase().rawQuery(query, args);
            while (cursor.moveToNext()) {
                mArrayList.add(cursor.getString(0));
            }
            cursor.close();
        } catch (SQLException e) {
            e.printStackTrace();
        } finally {
            closeDatabase();
        }
        return mArrayList;
    }

    public VehicleData getSelectedVehicle(int vehicleType, String manufacturer, String model, String fuelType, String releaseDate) {
        VehicleData vehicleData = null;

        String[] args = { String.valueOf(vehicleType) , manufacturer, model, fuelType, releaseDate };

        String query = "SELECT DISTINCT " + DBHelper.VehicleListColumns.VEHICLE_TYPE
                + ", " + DBHelper.VehicleListColumns.UNIQUE_ID
                + ", " + DBHelper.VehicleListColumns.MANUFACTURER
                + ", " + DBHelper.VehicleListColumns.MODEL
                + ", " + DBHelper.VehicleListColumns.FUEL_TYPE
                + ", " + DBHelper.VehicleListColumns.RELEASE_YEAR
                + ", " + DBHelper.VehicleListColumns.OBD_MANUFACTURER_ID
                + ", " + DBHelper.VehicleListColumns.OBD_MODEL_ID
                + ", " + DBHelper.VehicleListColumns.OBD_FUEL_TYPE_ID
                + " FROM " + DBHelper.Tables.VEHICLE_LIST
                + " WHERE " + DBHelper.VehicleListColumns.VEHICLE_TYPE + "=?"
                + " AND " + DBHelper.VehicleListColumns.MANUFACTURER + "=?"
                + " AND " + DBHelper.VehicleListColumns.MODEL + "=?"
                + " AND " + DBHelper.VehicleListColumns.FUEL_TYPE + "=?"
                + " AND " + DBHelper.VehicleListColumns.RELEASE_YEAR + "=?";
        try {
            Cursor cursor = openDatabase().rawQuery(query, args);
            if (cursor.moveToLast()) {
                do {
                    vehicleData = new VehicleData(Integer.valueOf(cursor.getString(0)),
                            cursor.getString(1), cursor.getString(2),
                            cursor.getString(3), cursor.getString(4), cursor.getString(5),
                            cursor.getString(6), cursor.getString(7), cursor.getString(8));
                } while (cursor.moveToNext());
            }
            cursor.close();
        } catch (SQLException e) {
            e.printStackTrace();
        } finally {
            closeDatabase();
        }
        return vehicleData;
    }

    public int getVehicleListVersion(int vehicleType) {
        int version = 0;

        String[] args = { String.valueOf(vehicleType) };

        String query = "SELECT " + DBHelper.VehicleListVersionColumns.CODE
                + " FROM " + DBHelper.Tables.VEHICLE_LIST_VERSION
                + " WHERE " + DBHelper.VehicleListVersionColumns.TYPE + "=?";
        try {
            Cursor cursor = openDatabase().rawQuery(query, args);
            if (cursor.moveToLast()) {
                do {
                    try {
                        version = Integer.parseInt(cursor.getString(0));
                    } catch (NumberFormatException ignore) {
                    }
                } while (cursor.moveToNext());
            }
            cursor.close();
        } catch (SQLiteException e) {
            e.printStackTrace();
        } finally {
            closeDatabase();
        }
        return version;
    }

    public boolean setVehicleListVersion(int vehicleType, int version) {
        boolean ret = false;
        boolean bExist = false;
        long affected_rows = -1;

        String[] args = { String.valueOf(vehicleType)};

        String query = "SELECT COUNT(" + DBHelper.VehicleListVersionColumns._ID + ")"
                + " FROM " + DBHelper.Tables.VEHICLE_LIST_VERSION
                + " WHERE " + DBHelper.VehicleListVersionColumns.TYPE + "=?";

        try {
            SQLiteDatabase db = openDatabase();

            Cursor cursor = db.rawQuery(query, args);
            if (cursor.moveToLast()) {
                do {
                    if (cursor.getInt(0) > 0) {
                        bExist = true;
                    }
                } while (cursor.moveToNext());
            }
            cursor.close();

            ContentValues cv = new ContentValues();
            cv.put(DBHelper.VehicleListVersionColumns.CODE, String.valueOf(version));

            if (bExist) { // update
                affected_rows = (long)db.update(DBHelper.Tables.VEHICLE_LIST_VERSION, cv,
                        DBHelper.VehicleListVersionColumns.TYPE + "=?", args);
                if (affected_rows > 0) {
                    ret = true;
                }
            } else { // insert
                cv.put(DBHelper.VehicleListVersionColumns.TYPE, String.valueOf(vehicleType));
                affected_rows = db.insert(DBHelper.Tables.VEHICLE_LIST_VERSION, null, cv);
                if (affected_rows != -1) {
                    ret = true;
                }
            }
        } catch (SQLiteException e) {
            e.printStackTrace();
        } finally {
            closeDatabase();
        }
        return ret;
    }
    //*********************************************************************************************/
    //    Device
    //*********************************************************************************************/
    public boolean isExistDevice(String device) {
        boolean bExist= false;

        String[] args = { device };
        String query = "SELECT COUNT(" + DBHelper.DeviceColumns._ID + ")"
                + " FROM " + DBHelper.Tables.DEVICE
                + " WHERE " + DBHelper.DeviceColumns.DEV_ID + "=?";
        try {
            Cursor cursor = openDatabase().rawQuery(query, args);
            if (cursor.moveToLast()) {
                do {
                    if (cursor.getInt(0) > 0) {
                        bExist = true;
                    }
                } while (cursor.moveToNext());
            }
            cursor.close();
        } catch (SQLiteException e) {
            e.printStackTrace();
        } finally {
            closeDatabase();
        }
        return bExist;
    }

    public long registerDevice(String device) {
        long affected_rows = -1;

        ContentValues cv = new ContentValues();
        cv.put(DBHelper.DeviceColumns.DEV_ID, device);

        try {
            SQLiteDatabase db = openDatabase();
            affected_rows = db.insert(DBHelper.Tables.DEVICE, null, cv);
        } catch (SQLiteException e) {
            e.printStackTrace();
        } finally {
            closeDatabase();
        }
        return affected_rows;
    }

    public boolean unregisterDevice(String device) {
        boolean ret = true;

        String[] args = { device };
        try {
            SQLiteDatabase db = openDatabase();
            db.delete(DBHelper.Tables.DEVICE, DBHelper.DeviceColumns.DEV_ID + "=?", args);
            db.delete(DBHelper.Tables.VEHICLE, DBHelper.VehicleColumns.DEV_ID + "=?", args);
            db.delete(DBHelper.Tables.DRIVER, DBHelper.DriverColumns.DEV_ID + "=?", args);
            db.delete(DBHelper.Tables.CALIBRATION, DBHelper.CalibrationColumns.DEV_ID + "=?", args);
            db.delete(DBHelper.Tables.VIDEO, DBHelper.VideoColumns.DEV_ID + "=?", args);
            db.delete(DBHelper.Tables.FW_VER, DBHelper.FirmwareColumns.DEV_ID + "=?", args);
        } catch (SQLiteException e) {
            e.printStackTrace();
            ret = false;
        } finally {
            closeDatabase();
        }
        return ret;
    }

    //*********************************************************************************************/
    //    Vehicle
    //*********************************************************************************************/
    public VehicleData getVehicleData(String device) {
        VehicleData data = new VehicleData();

        String[] args = { device };
        String query = "SELECT " + DBHelper.VehicleColumns.VEHICLE_TYPE
                + ", " + DBHelper.VehicleColumns.UNIQUE_ID
                + ", " + DBHelper.VehicleColumns.MANUFACTURER
                + ", " + DBHelper.VehicleColumns.MODEL
                + ", " + DBHelper.VehicleColumns.FUEL_TYPE
                + ", " + DBHelper.VehicleColumns.RELEASE_YEAR
                + ", " + DBHelper.VehicleColumns.OBD_MANUFACTURER_ID
                + ", " + DBHelper.VehicleColumns.OBD_MODEL_ID
                + ", " + DBHelper.VehicleColumns.OBD_FUEL_TYPE_ID
                + " FROM " + DBHelper.Tables.VEHICLE
                + " WHERE " + DBHelper.VehicleColumns.DEV_ID + "=?";
        try {
            Cursor cursor = openDatabase().rawQuery(query, args);
            if (cursor.moveToLast()) {
                do {
                    data.setVehicleType(Integer.valueOf(cursor.getString(0)));
                    data.setUniqueID(cursor.getString(1));
                    data.setManufacturer(cursor.getString(2));
                    data.setModel(cursor.getString(3));
                    data.setFuelType(cursor.getString(4));
                    data.setReleaseDate(cursor.getString(5));
                    data.setOBDManufacturerID(cursor.getString(6));
                    data.setOBDModelID(cursor.getString(7));
                    data.setOBDFuelTypeID(cursor.getString(8));
                } while (cursor.moveToNext());
            }
            cursor.close();
        } catch (SQLiteException e) {
            e.printStackTrace();
        } finally {
            closeDatabase();
        }
        return data;
    }

    public boolean setVehicleData(String device, VehicleData data) {
        boolean ret = false;
        boolean bExist = false;
        long affected_rows = -1;

        String[] args = { device };
        String query = "SELECT COUNT(" + DBHelper.VehicleColumns._ID + ")"
                + " FROM " + DBHelper.Tables.VEHICLE
                + " WHERE " + DBHelper.FirmwareColumns.DEV_ID + "=?";
        try {
            SQLiteDatabase db = openDatabase();

            Cursor cursor = db.rawQuery(query, args);
            if (cursor.moveToLast()) {
                do {
                    if (cursor.getInt(0) > 0) {
                        bExist = true;
                    }
                } while (cursor.moveToNext());
            }
            cursor.close();

            ContentValues cv = new ContentValues();
            cv.put(DBHelper.VehicleColumns.VEHICLE_TYPE, String.valueOf(data.getVehicleType()));
            cv.put(DBHelper.VehicleColumns.UNIQUE_ID, data.getUniqueID());
            cv.put(DBHelper.VehicleColumns.MANUFACTURER, data.getManufacturer());
            cv.put(DBHelper.VehicleColumns.MODEL, data.getModel());
            cv.put(DBHelper.VehicleColumns.FUEL_TYPE, data.getFuelType());
            cv.put(DBHelper.VehicleColumns.RELEASE_YEAR, data.getReleaseDate());
            cv.put(DBHelper.VehicleColumns.OBD_MANUFACTURER_ID, data.getOBDManufacturerID());
            cv.put(DBHelper.VehicleColumns.OBD_MODEL_ID, data.getOBDModelID());
            cv.put(DBHelper.VehicleColumns.OBD_FUEL_TYPE_ID, data.getOBDFuelTypeID());
            if (bExist) { // update
                affected_rows = (long)db.update(DBHelper.Tables.VEHICLE, cv,
                        DBHelper.VehicleColumns.DEV_ID + "=?", args);
                if (affected_rows > 0) {
                    ret = true;
                }
            } else { // insert
                cv.put(DBHelper.VehicleColumns.DEV_ID, device);
                affected_rows = db.insert(DBHelper.Tables.VEHICLE, null, cv);
                if (affected_rows != -1) {
                    ret = true;
                }
            }
        } catch (SQLiteException e) {
            e.printStackTrace();
        } finally {
            closeDatabase();
        }
        return ret;
    }

    public int deleteVehicleData(String device) {
        int affected_rows = 0;

        String[] args = { device };
        try {
            affected_rows = openDatabase().delete(DBHelper.Tables.VEHICLE,
                    DBHelper.VehicleColumns.DEV_ID + "=?", args);
        } catch (SQLiteException e) {
            e.printStackTrace();
        } finally {
            closeDatabase();
        }
        return affected_rows;
    }
    //*********************************************************************************************/
    //    Driver
    //*********************************************************************************************/
    public DriverData getDriverData(String device) {
        DriverData data = new DriverData();

        String[] args = { device };
        String query = "SELECT " + DBHelper.DriverColumns.VEHICLE_TYPE
                + ", " + DBHelper.DriverColumns.VIN
                + ", " + DBHelper.DriverColumns.VEHICLE_REG_NO
                + ", " + DBHelper.DriverColumns.BUSINESS_REG_NO
                + ", " + DBHelper.DriverColumns.DRIVER_CODE
                + " FROM " + DBHelper.Tables.DRIVER
                + " WHERE " + DBHelper.DriverColumns.DEV_ID + "=?";
        try {
            Cursor cursor = openDatabase().rawQuery(query, args);
            if (cursor.moveToLast()) {
                do {
                    data.setVehicleType(cursor.getInt(0));
                    data.setVIN(cursor.getString(1));
                    data.setVehicleRegNo(cursor.getString(2));
                    data.setBusinessRegNo(cursor.getString(3));
                    data.setDriverCode(cursor.getString(4));
                } while (cursor.moveToNext());
            }
            cursor.close();
        } catch (SQLiteException e) {
            e.printStackTrace();
        } finally {
            closeDatabase();
        }
        return data;
    }

    public boolean setDriverData(String device, DriverData data) {
        boolean ret = false;
        boolean bExist = false;
        long affected_rows = -1;

        String[] args = { device };
        String query = "SELECT COUNT(" + DBHelper.DriverColumns._ID + ")"
                + " FROM " + DBHelper.Tables.DRIVER
                + " WHERE " + DBHelper.DriverColumns.DEV_ID + "=?";
        try {
            SQLiteDatabase db = openDatabase();

            Cursor cursor = db.rawQuery(query, args);
            if (cursor.moveToLast()) {
                do {
                    if (cursor.getInt(0) > 0) {
                        bExist = true;
                    }
                } while (cursor.moveToNext());
            }
            cursor.close();

            ContentValues cv = new ContentValues();

            cv.put(DBHelper.DriverColumns.VEHICLE_TYPE, data.getVehicleType());
            cv.put(DBHelper.DriverColumns.VIN, data.getVIN());
            cv.put(DBHelper.DriverColumns.VEHICLE_REG_NO, data.getVehicleRegNo());
            cv.put(DBHelper.DriverColumns.BUSINESS_REG_NO, data.getBusinessRegNo());
            cv.put(DBHelper.DriverColumns.DRIVER_CODE, data.getDriverCode());

            if (bExist) { // update
                affected_rows = (long)db.update(DBHelper.Tables.DRIVER, cv,
                        DBHelper.DriverColumns.DEV_ID + "=?", args);
                if (affected_rows > 0) {
                    ret = true;
                }
            } else { // insert
                cv.put(DBHelper.DriverColumns.DEV_ID, device);
                affected_rows = db.insert(DBHelper.Tables.DRIVER, null, cv);
                if (affected_rows != -1) {
                    ret = true;
                }
            }
        } catch (SQLiteException e) {
            e.printStackTrace();
        } finally {
            closeDatabase();
        }
        return ret;

    }

    public int deleteDriverData(String device) {
        int affected_rows = 0;

        String[] args = { device };
        try {
            affected_rows = openDatabase().delete(DBHelper.Tables.DRIVER,
                    DBHelper.DriverColumns.DEV_ID + "=?", args);
        } catch (SQLiteException e) {
            e.printStackTrace();
        } finally {
            closeDatabase();
        }
        return affected_rows;
    }
    //*********************************************************************************************/
    //    Calibration
    //*********************************************************************************************/
    public CalibrationData getCalibrationData(String device) {
        CalibrationData data = new CalibrationData();
        String[] args = { device };
        String query = "SELECT " + DBHelper.CalibrationColumns.REG_DATE
                + ", " + DBHelper.CalibrationColumns.CAMERA_HEIGHT
                + ", " + DBHelper.CalibrationColumns.VEHICLE_WIDTH
                + ", " + DBHelper.CalibrationColumns.CAMERA_TO_BUMPER
                + ", " + DBHelper.CalibrationColumns.BONNET_Y
                + ", " + DBHelper.CalibrationColumns.CENTER_X
                + ", " + DBHelper.CalibrationColumns.VANISHING_Y
                + ", " + DBHelper.CalibrationColumns.NEAR_Y
                + ", " + DBHelper.CalibrationColumns.NEAR_LEFT_X
                + ", " + DBHelper.CalibrationColumns.NEAR_RIGHT_X
                + ", " + DBHelper.CalibrationColumns.FAR_Y
                + ", " + DBHelper.CalibrationColumns.FAR_LEFT_X
                + ", " + DBHelper.CalibrationColumns.FAR_RIGHT_X
                + ", " + DBHelper.CalibrationColumns.FAR_DISTANCE
                + ", " + DBHelper.CalibrationColumns.NEAR_DISTANCE
                + " FROM " + DBHelper.Tables.CALIBRATION
                + " WHERE " + DBHelper.CalibrationColumns.DEV_ID + "=?";
        try {
            Cursor cursor = openDatabase().rawQuery(query, args);
            if (cursor.moveToLast()) {
                do {
                    data.setDate(cursor.getString(0));
                    data.setCameraHeight(cursor.getInt(1));
                    data.setVehicleWidth(cursor.getInt(2));
                    data.setCameraToBumper(cursor.getInt(3));
                    data.setBonnetPoint(new Point(0, cursor.getInt(4)));
                    data.setCenterX(cursor.getInt(5));
                    data.setVanishingY(cursor.getInt(6));
                    data.setNearY(cursor.getInt(7));
                    data.setNearLeftX(cursor.getInt(8));
                    data.setNearRightX(cursor.getInt(9));
                    data.setFarY(cursor.getInt(10));
                    data.setFarLeftX(cursor.getInt(11));
                    data.setFarRightX(cursor.getInt(12));
                    data.setFarDistance(cursor.getInt(13));
                    data.setNearDistance(cursor.getInt(14));
                } while (cursor.moveToNext());
            }
            cursor.close();
        } catch (SQLiteException e) {
            e.printStackTrace();
        } finally {
            closeDatabase();
        }
        return data;
    }

    public boolean setCalibrationData(String device, CalibrationData data) {
        boolean ret = false;
        boolean bExist = false;
        long affected_rows = -1;

        String[] args = { device };
        String query = "SELECT COUNT(" + DBHelper.CalibrationColumns._ID + ")"
                + " FROM " + DBHelper.Tables.CALIBRATION
                + " WHERE " + DBHelper.CalibrationColumns.DEV_ID + "=?";
        try {
            SQLiteDatabase db = openDatabase();

            Cursor cursor = db.rawQuery(query, args);
            if (cursor.moveToLast()) {
                do {
                    if (cursor.getInt(0) > 0) {
                        bExist = true;
                    }
                } while (cursor.moveToNext());
            }
            cursor.close();

            ContentValues cv = new ContentValues();
            cv.put(DBHelper.CalibrationColumns.REG_DATE, data.getDate());
            cv.put(DBHelper.CalibrationColumns.CAMERA_HEIGHT, data.getCameraHeight());
            cv.put(DBHelper.CalibrationColumns.VEHICLE_WIDTH, data.getVehicleWidth());
            cv.put(DBHelper.CalibrationColumns.CAMERA_TO_BUMPER, data.getCameraToBumper());
            cv.put(DBHelper.CalibrationColumns.BONNET_Y, data.getBonnetPoint().y);
            cv.put(DBHelper.CalibrationColumns.CENTER_X, data.getCenterX());
            cv.put(DBHelper.CalibrationColumns.VANISHING_Y, data.getVanishingY());
            cv.put(DBHelper.CalibrationColumns.NEAR_Y, data.getNearY());
            cv.put(DBHelper.CalibrationColumns.NEAR_LEFT_X, data.getNearLeftX());
            cv.put(DBHelper.CalibrationColumns.NEAR_RIGHT_X, data.getNearRightX());
            cv.put(DBHelper.CalibrationColumns.FAR_Y, data.getFarY());
            cv.put(DBHelper.CalibrationColumns.FAR_LEFT_X, data.getFarLeftX());
            cv.put(DBHelper.CalibrationColumns.FAR_RIGHT_X, data.getFarRightX());
            cv.put(DBHelper.CalibrationColumns.FAR_DISTANCE, data.getFarDistance());
            cv.put(DBHelper.CalibrationColumns.NEAR_DISTANCE, data.getNearDistance());
            if (bExist) { // update
                affected_rows = (long)db.update(DBHelper.Tables.CALIBRATION, cv,
                        DBHelper.CalibrationColumns.DEV_ID + "=?", args);
                if (affected_rows > 0) {
                    ret = true;
                }
            } else { // insert
                cv.put(DBHelper.CalibrationColumns.DEV_ID, device);
                affected_rows = db.insert(DBHelper.Tables.CALIBRATION, null, cv);
                if (affected_rows != -1) {
                    ret = true;
                }
            }
        } catch (SQLiteException e) {
            e.printStackTrace();
        } finally {
            closeDatabase();
        }
        return ret;
    }

    public int deleteCalibrationData(String device) {
        int affected_rows = 0;

        String[] args = { device };
        try {
            affected_rows = openDatabase().delete(DBHelper.Tables.CALIBRATION,
                    DBHelper.CalibrationColumns.DEV_ID + "=?", args);
        } catch (SQLiteException e) {
            e.printStackTrace();
        } finally {
            closeDatabase();
        }
        return affected_rows;
    }

    //*********************************************************************************************/
    //    Video
    //*********************************************************************************************/
    public ArrayList<VideoData> getVideoList(int type, String device) {
        ArrayList<VideoData> arrayList = new ArrayList<>();

        String[] args = { device, String.valueOf(type) };
        String query = "SELECT " + DBHelper.VideoColumns.TYPE
                + ", " + DBHelper.VideoColumns.FILE_NAME
                + " FROM " + DBHelper.Tables.VIDEO
                + " WHERE " + DBHelper.VideoColumns.DEV_ID + "=?"
                + " AND " + DBHelper.VideoColumns.TYPE + "=?";
        try {
            Cursor cursor = openDatabase().rawQuery(query, args);
            while (cursor.moveToNext()) {
                try {
                    arrayList.add(new VideoData(cursor.getInt(0), cursor.getString(1)));
                } catch (IllegalArgumentException | ParseException | IndexOutOfBoundsException ignore) {
                }
            }
            cursor.close();
        } catch (SQLiteException e) {
            e.printStackTrace();
        } finally {
            closeDatabase();
        }
        return arrayList;
    }

    public boolean insertVideoList(String device, ArrayList<VideoData> arrayList) {
        boolean ret = true;

        ContentValues cv = new ContentValues();
        SQLiteDatabase db = openDatabase();
        try {
            db.beginTransaction();

            for (VideoData data : arrayList) {
                cv.put(DBHelper.VideoColumns.DEV_ID, device);
                cv.put(DBHelper.VideoColumns.TYPE, data.getType());
                cv.put(DBHelper.VideoColumns.FILE_NAME, data.getFileName());
                db.insert(DBHelper.Tables.VIDEO, null, cv);
            }
            db.setTransactionSuccessful();
        } catch (SQLiteException e) {
            e.printStackTrace();
            ret = false;
        } finally {
            db.endTransaction();
            closeDatabase();
        }
        return ret;

    }

    public int deleteVideoList(String device) {
        int affected_rows = 0;

        String[] args = { device };
        try {
            affected_rows = openDatabase().delete(DBHelper.Tables.VIDEO,
                    DBHelper.VideoColumns.DEV_ID + "=?", args);
        } catch (SQLiteException e) {
            e.printStackTrace();
        } finally {
            closeDatabase();
        }
        return affected_rows;
    }

    //*********************************************************************************************/
    //    Firmware
    //*********************************************************************************************/
    public String getFirmwareVersionName(String device) {
        String version = "";

        String[] args = { device };
        String query = "SELECT " + DBHelper.FirmwareColumns.VERSION_NAME
                + " FROM " + DBHelper.Tables.FW_VER
                + " WHERE " + DBHelper.FirmwareColumns.DEV_ID + "=?";
        try {
            Cursor cursor = openDatabase().rawQuery(query, args);
            if (cursor.moveToLast()) {
                do {
                    version = cursor.getString(0);
                } while (cursor.moveToNext());
            }
            cursor.close();
        } catch (SQLiteException e) {
            e.printStackTrace();
        } finally {
            closeDatabase();
        }
        return version;
    }

    public int getFirmwareVersionCode(String device) {
        int versionCode = 0;

        String[] args = { device };
        String query = "SELECT " + DBHelper.FirmwareColumns.VERSION_CODE
                + " FROM " + DBHelper.Tables.FW_VER
                + " WHERE " + DBHelper.FirmwareColumns.DEV_ID + "=?";
        try {
            Cursor cursor = openDatabase().rawQuery(query, args);
            if (cursor.moveToLast()) {
                do {
                    try {
                        versionCode = Integer.valueOf(cursor.getString(0));
                    } catch (NumberFormatException e) {
                        e.printStackTrace();
                        versionCode = 0;
                    }
                } while (cursor.moveToNext());
            }
            cursor.close();
        } catch (SQLiteException e) {
            e.printStackTrace();
        } finally {
            closeDatabase();
        }
        return versionCode;
    }

    public int getDtgSupported(String device) {
        int supported = Constants.DtgSupport.HW_NOT_SUPPORTED;

        String[] args = { device };
        String query = "SELECT " + DBHelper.FirmwareColumns.DTG_SUPPORT
                + " FROM " + DBHelper.Tables.FW_VER
                + " WHERE " + DBHelper.FirmwareColumns.DEV_ID + "=?";
        try {
            Cursor cursor = openDatabase().rawQuery(query, args);
            if (cursor.moveToLast()) {
                do {
                    try {
                        supported = Integer.valueOf(cursor.getString(0));
                    } catch (NumberFormatException e) {
                        e.printStackTrace();
                        supported = 0;
                    }
                } while (cursor.moveToNext());
            }
            cursor.close();
        } catch (SQLiteException e) {
            e.printStackTrace();
        } finally {
            closeDatabase();
        }
        return supported;
    }

    public boolean setFirmwareVersion(String device, String versionName, String versionCode, String dtgSupport) {
        boolean ret = false;
        long affected_rows = -1;
        boolean bExist = false;

        String[] args = { device };
        String query = "SELECT COUNT(" + DBHelper.FirmwareColumns._ID + ")"
                + " FROM " + DBHelper.Tables.FW_VER
                + " WHERE " + DBHelper.FirmwareColumns.DEV_ID + "=?";
        try {
            SQLiteDatabase db = openDatabase();

            Cursor cursor = db.rawQuery(query, args);
            if (cursor.moveToLast()) {
                do {
                    if (cursor.getInt(0) > 0) {
                        bExist = true;
                    }
                } while (cursor.moveToNext());
            }
            cursor.close();

            ContentValues cv = new ContentValues();
            if (bExist) { // update
                cv.put(DBHelper.FirmwareColumns.VERSION_NAME, versionName);
                cv.put(DBHelper.FirmwareColumns.VERSION_CODE, versionCode);
                cv.put(DBHelper.FirmwareColumns.DTG_SUPPORT, dtgSupport);
                affected_rows = (long)db.update(DBHelper.Tables.FW_VER, cv,
                        DBHelper.FirmwareColumns.DEV_ID + "=?", args);
                if (affected_rows > 0) {
                    ret = true;
                }
            } else { // insert
                cv.put(DBHelper.FirmwareColumns.DEV_ID, device);
                cv.put(DBHelper.FirmwareColumns.VERSION_NAME, versionName);
                cv.put(DBHelper.FirmwareColumns.VERSION_CODE, versionCode);
                cv.put(DBHelper.FirmwareColumns.DTG_SUPPORT, dtgSupport);
                affected_rows = db.insert(DBHelper.Tables.FW_VER, null, cv);
                if (affected_rows != -1) {
                    ret = true;
                }
            }
        } catch (SQLiteException e) {
            e.printStackTrace();
        } finally {
            closeDatabase();
        }
        return ret;
    }

    //*********************************************************************************************/
    //    Latest connected device
    //*********************************************************************************************/
    public String getLatestDeviceToken() {
        String token = "";
        String query = "SELECT " + DBHelper.LatestDevColumns.DEV_ID
                + " FROM " + DBHelper.Tables.LATEST_DEVICE;
        try {
            Cursor cursor = openDatabase().rawQuery(query, null);
            if (cursor.moveToLast()) {
                do {
                    token = cursor.getString(0);
                } while (cursor.moveToNext());
            }
            cursor.close();
        } catch (SQLiteException e) {
            e.printStackTrace();
        } finally {
            closeDatabase();
        }
        return token;
    }

    public boolean setLatestDeviceToken(String device) {
        boolean ret = false;
        long affected_rows = -1;
        boolean bExist = false;
        String query = "SELECT COUNT(" + DBHelper.LatestDevColumns._ID + ")"
                + " FROM " + DBHelper.Tables.LATEST_DEVICE;
        try {
            SQLiteDatabase db = openDatabase();

            Cursor cursor = db.rawQuery(query, null);
            if (cursor.moveToLast()) {
                do {
                    if (cursor.getInt(0) > 0) {
                        bExist = true;
                    }
                } while (cursor.moveToNext());
            }
            cursor.close();

            ContentValues cv = new ContentValues();
            cv.put(DBHelper.LatestDevColumns.DEV_ID, device);
            if (bExist) { // update
                affected_rows = (long)db.update(DBHelper.Tables.LATEST_DEVICE, cv, null, null);
                if (affected_rows > 0) {
                    ret = true;
                }
            } else { // insert
                affected_rows = db.insert(DBHelper.Tables.LATEST_DEVICE, null, cv);
                if (affected_rows != -1) {
                    ret = true;
                }
            }
        } catch (SQLiteException e) {
            e.printStackTrace();
        } finally {
            closeDatabase();
        }

        return ret;
    }
}