package com.adasone.hm320a.util;

import com.adasone.hm320a.R;
import com.adasone.hm320a.application.Constants;

/**
 *
 */
public class VehicleTypeUtil {
    public static final  String TAG = VehicleTypeUtil.class.getSimpleName();

    public static int getVehicleTypeStringResId(int code) {
        int resId = -1;
        switch (code) {
            case Constants.VehicleType.CITY_BUS :
                resId = R.string.type_city_bus;
                break;
            case Constants.VehicleType.RURAL_BUS :
                resId = R.string.type_rural_bus;
                break;
            case Constants.VehicleType.TOWN_BUS :
                resId = R.string.type_town_bus;
                break;
            case Constants.VehicleType.INTERCITY_BUS :
                resId = R.string.type_intercity_bus;
                break;
            case Constants.VehicleType.EXPRESS_BUS :
                resId = R.string.type_express_bus;
                break;
            case Constants.VehicleType.CHARTERED_BUS :
                resId = R.string.type_chartered_bus;
                break;
            case Constants.VehicleType.SPECIAL_PASSENGER_VEHICLE :
                resId = R.string.type_special_passenger_vehicle;
                break;
            case Constants.VehicleType.REGULAR_TAXI :
                resId = R.string.type_regular_taxi;
                break;
            case Constants.VehicleType.PRIVATE_TAXI :
                resId = R.string.type_private_taxi;
                break;
            case Constants.VehicleType.GENERAL_LORRY :
                resId = R.string.type_general_lorry;
                break;
            case Constants.VehicleType.INDIVIDUAL_LORRY :
                resId = R.string.type_individual_lorry;
                break;
            case Constants.VehicleType.NON_COMMERCIAL_VEHICLE :
                resId = R.string.type_non_commercial_vehicle;
                break;
            default :
                break;
        }

        return resId;
    }
}
