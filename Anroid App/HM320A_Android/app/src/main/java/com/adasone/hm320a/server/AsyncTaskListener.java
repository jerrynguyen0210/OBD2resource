package com.adasone.hm320a.server;

import org.json.JSONObject;

public interface AsyncTaskListener {
    void onSuccess(JSONObject jsonObject);
    void onFailure(Throwable thrown);
    void onCancel();
    void onProgressUpdate(int progress);
}