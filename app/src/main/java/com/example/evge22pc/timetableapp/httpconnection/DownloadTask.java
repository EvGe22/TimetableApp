package com.example.evge22pc.timetableapp.httpconnection;

import android.os.AsyncTask;
import android.support.annotation.Nullable;

import com.example.evge22pc.timetableapp.data.DBHelper;
import com.example.evge22pc.timetableapp.data.MyLog;

import java.io.IOException;
import java.io.InputStream;
import java.net.HttpURLConnection;
import java.net.URL;

public class DownloadTask{


    /**
     * @param myUrl The url for which Stream is opened
     * @return  Returns the Stream for a given url
     * @throws IOException
     */
    @Nullable
    public static InputStream getStream(String myUrl) throws IOException {
        InputStream is = null;
        try {
            URL url = new URL(myUrl);
            HttpURLConnection conn = (HttpURLConnection) url.openConnection();
            conn.setReadTimeout(10000 /* milliseconds */);
            conn.setConnectTimeout(15000 /* milliseconds */);
            conn.setRequestMethod("GET");
            conn.setDoInput(true);

            // Starts the query
            conn.connect();
            int response = conn.getResponseCode();
            MyLog.v("Response code: " + Integer.toString(response));
            is = conn.getInputStream();
            return is;

            // Makes sure that the InputStream is closed after the app is
            // finished using it.
        } catch (Exception e) {
            e.printStackTrace();
            return null;
        }
    }
}
