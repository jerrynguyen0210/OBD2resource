package com.adasone.hm320a.server;


import android.os.AsyncTask;

import org.json.JSONException;
import org.json.JSONObject;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.net.HttpURLConnection;
import java.net.URL;

/**
 * AsyncTask for retrieve
 * @String		Parameter of execute( ), doInBackground( ) method.
 * @Void		Parameter of onProgressUpdate( ) method.
 * @AsyncTaskResult<JSONObject>	    Return value of doInBackground( ) method, and Parameter of onPostExecute( ) method.
 */
public class RetrieveGetTask extends AsyncTask<String, Void, AsyncTaskResult<JSONObject>> {

	private static final String TAG = RetrieveGetTask.class.getSimpleName();

    private static final int CONN_TIMEOUT = 30000;
    private static final int READ_TIMEOUT = 30000;
    private final AsyncTaskListener mListener;

	public RetrieveGetTask(AsyncTaskListener listener) {
        mListener = listener;
	}

	/** Works before doInBackground( ) */
	@Override
	protected void onPreExecute() {
		super.onPreExecute();
	}

	/** Works should be done in here. */
	@Override
	protected AsyncTaskResult<JSONObject> doInBackground(String... strURL) {
        HttpURLConnection conn = null;
        InputStream is = null;
        ByteArrayOutputStream baos = null;
        String response = null;
        int responseCode;

        try {
            URL url = new URL(strURL[0]);
            conn = (HttpURLConnection)url.openConnection();
            conn.setConnectTimeout(CONN_TIMEOUT);
            conn.setReadTimeout(READ_TIMEOUT);
            conn.setRequestMethod("GET");

            conn.setRequestProperty("Cache-Control", "no-cache");
            conn.setRequestProperty("Content-Type", "application/json");
            conn.setRequestProperty("Accept", "application/json");
            conn.setDoOutput(false);
            conn.setDoInput(true);

            responseCode = conn.getResponseCode();

            if (responseCode == HttpURLConnection.HTTP_OK) {
                is = conn.getInputStream();
                baos = new ByteArrayOutputStream();
                byte[] byteBuffer = new byte[1024];
                int nLength = 0;
                while((nLength = is.read(byteBuffer, 0, byteBuffer.length)) != -1) {
                    if (isCancelled()) {
                        return new AsyncTaskResult<JSONObject>(new JSONObject());
                    } else {
                        baos.write(byteBuffer, 0, nLength);
                    }
                }
                response = new String(baos.toByteArray(), "UTF-8");
                return new AsyncTaskResult<JSONObject>(new JSONObject(response));
            } else {
                return new AsyncTaskResult<JSONObject>(
                        new Exception("HTTP response : " + conn.getResponseCode() + " : " + conn.getResponseMessage()));
            }
        } catch (IOException | NullPointerException | JSONException e) {
            e.printStackTrace();
            return new AsyncTaskResult<JSONObject>(e);
        } finally {
            try {
                if (is != null) {
                    is.close();
                }
                if (baos != null) {
                    baos.close();
                }
            } catch (IOException e) {
                e.printStackTrace();
            }

            if (conn != null) {
                conn.disconnect();
            }
        }
    }

	/** Works after doInBackground( ) */
	@Override
	protected void onPostExecute(AsyncTaskResult<JSONObject> result) {
        super.onPostExecute(result);
        if (mListener != null) {
            if (isCancelled()) {
                mListener.onCancel();
            } else if (result.getError() != null) {
                mListener.onFailure(result.getError());
            } else {
                mListener.onSuccess(result.getResult());
            }
        }
	}

    @Override
    protected void onCancelled(AsyncTaskResult<JSONObject> result) {
        super.onCancelled(result);
        if (mListener != null) {
            mListener.onCancel();
        }
    }

}