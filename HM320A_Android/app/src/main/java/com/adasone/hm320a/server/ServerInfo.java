package com.adasone.hm320a.server;


import com.adasone.hm320a.application.AppApplication;

public class ServerInfo {
    // TODO
    private static final String SERVER_PORT = (AppApplication.isDebug()) ? "9099" : "9099";

    private static final String SERVER_URL = "http://api.adasone.com:" + SERVER_PORT;

    //Vehicle manufacturer : POST
    public static final String VEHICLE_MANUFACTURER_RETRIEVE_URL = SERVER_URL + "/api/manufacturer";

    //Vehicle list : POST
    public static final String VEHICLE_MODEL_RETRIEVE_URL = SERVER_URL + "/api/carlist";

    public static final String PARAM_VEHICLE_TYPE = "type";
    public static final String PARAM_BRAND_ID = "brandid";

    //Firmware version retrieve & App version retrieve  : GET
    public static final String FW_VER_RETRIEVE_URL = SERVER_URL + "/api/versions";

}

