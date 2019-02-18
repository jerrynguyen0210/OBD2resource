package com.adasone.hm320a.server;


import android.os.AsyncTask;
import android.util.Log;

import org.json.JSONException;
import org.json.JSONObject;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.HttpURLConnection;
import java.net.URL;

/**
 * AsyncTask for download
 * @String		Parameter of execute( ), doInBackground( ) method.
 * @Integer	Parameter of onProgressUpdate( ) method.
 * @AsyncTaskResult<JSONObject>	    Return value of doInBackground( ) method, and Parameter of onPostExecute( ) method.
 */
public class DownloadTask extends AsyncTask<String, Integer, AsyncTaskResult<JSONObject>> {

	private static final String TAG = DownloadTask.class.getSimpleName();

    private static final int CONN_TIMEOUT = 5000;
    private static final int READ_TIMEOUT = 5000;
    private final AsyncTaskListener mListener;
    private final long mFileSize;
    private final String mFilePath;

	public DownloadTask(AsyncTaskListener listener, long fileSize, String filePath) {
        mListener = listener;
        mFileSize = fileSize;
        mFilePath = filePath;
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
        OutputStream os = null;
        String response = null;

        int responseCode;
        Log.d(TAG, "" + mFilePath);
        try {
            File file = new File(mFilePath);
            if (file.exists()) {
                boolean del = file.delete();
            }
            Log.d(TAG, "Length of file: " + strURL[0]);
            URL url = new URL(strURL[0]);
            conn = (HttpURLConnection)url.openConnection();
            conn.setConnectTimeout(CONN_TIMEOUT);
            conn.setReadTimeout(READ_TIMEOUT);

            // expect HTTP 200 OK, so we don't mistakenly save error report instead of the file
            responseCode = conn.getResponseCode();
            if (responseCode != HttpURLConnection.HTTP_OK) {
                return new AsyncTaskResult<JSONObject>(
                            new Exception("HTTP response : " + responseCode + " : " + conn.getResponseMessage()));
            }

            // download the file
            is = conn.getInputStream();
            os = new FileOutputStream(mFilePath);

            byte data[] = new byte[4096];
            long total = 0;
            int count;

            while ((count = is.read(data)) != -1) {
                if (isCancelled()) {
                    return new AsyncTaskResult<JSONObject>(new JSONObject());
                }
                total += count;
                // publishing the progress....
                if (mFileSize > 0) {
                    publishProgress((int) (total * 100 / mFileSize));
                }
                os.write(data, 0, count);
            }
            os.flush();
            JSONObject obj = new JSONObject();
            obj.put("result", "success");
            obj.put("filepath", mFilePath);
            return new AsyncTaskResult<JSONObject>(obj);
        } catch (IOException | NullPointerException | JSONException  e) {
            e.printStackTrace();
            return new AsyncTaskResult<JSONObject>(e);
        } finally {
            try {
                if (is != null) {
                    is.close();
                }
                if (os != null) {
                    os.close();
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

    @Override
    protected void onProgressUpdate(Integer... progress) {
        super.onProgressUpdate(progress[0]);
        if (mListener != null) {
            mListener.onProgressUpdate(progress[0]);
        }
    }
}