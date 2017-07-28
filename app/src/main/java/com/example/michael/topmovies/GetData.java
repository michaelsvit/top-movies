package com.example.michael.topmovies;

import android.net.Uri;
import android.os.AsyncTask;
import android.support.annotation.NonNull;
import android.util.Log;

import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;

/**
 * Created by Michael on 9/2/2015.
 */
public class GetData extends AsyncTask<Void, Void, Void> {

    String LOG_TAG = GetData.class.getSimpleName();

    @Override
    protected Void doInBackground(Void... params) {
        return null;
    }

    @NonNull
    protected Uri constructUrl(final String[] PATH, final String[] PARAM_KEYS, final String[] PARAM_VALUES) {
        final String SCHEME = "https";
        final String AUTHORITY = "api.themoviedb.org";
        final String API_VERSION = "3";

        Uri.Builder builder = new Uri.Builder();
        builder.scheme(SCHEME)
                .authority(AUTHORITY)
                .appendPath(API_VERSION);
        for(String pathSegment : PATH) {
            builder.appendPath(pathSegment);
        }
        for(int i = 0; i < PARAM_KEYS.length; i++) {
            builder.appendQueryParameter(PARAM_KEYS[i], PARAM_VALUES[i]);
        }

        return builder.build();
    }

    protected String downloadData(String urlString) throws IOException {
        String jsonData = "";
        InputStream is = null;
        final int BUFFER_SIZE = 500;

        try {
            //Initiate connection
            URL url = new URL(urlString);
            HttpURLConnection connection = (HttpURLConnection) url.openConnection();
            connection.setReadTimeout(10000);
            connection.setConnectTimeout(15000);
            connection.setRequestMethod("GET");
            connection.connect();
            Log.d(LOG_TAG, "Connection response code: " + connection.getResponseCode());

            //Read data
            is = connection.getInputStream();
            InputStreamReader reader = new InputStreamReader(is);
            char[] buffer = new char[BUFFER_SIZE];
            int charsRead;

            while ((charsRead = reader.read(buffer)) != -1) {
                jsonData += String.valueOf(buffer,0,charsRead);
                buffer = new char[BUFFER_SIZE];
            }
        } finally {
            if(is != null) {
                is.close();
            }
        }

        return jsonData;
    }
}

