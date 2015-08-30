package com.example.michael.topmovies;

import android.app.FragmentManager;
import android.app.FragmentTransaction;
import android.content.Intent;
import android.content.SharedPreferences;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.ArrayList;
import java.util.List;

public class MainActivity extends AppCompatActivity implements MainActivityFragment.GetMoviesList {

    protected List<MovieEntry> movieEntries;
    String currentSorting;
    SharedPreferences sharedPreferences;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        //Attach gridView fragment to the activity, or the last viewed fragment if one exists
        if(savedInstanceState == null) {
            MainActivityFragment mainActivityFragment = new MainActivityFragment();
            FragmentTransaction transaction = getFragmentManager().beginTransaction();
            transaction.add(R.id.activity_main_container, mainActivityFragment, MainActivityFragment.FRAGMENT_TAG).commit();
        }

        movieEntries = new ArrayList<>();
        sharedPreferences = PreferenceManager.getDefaultSharedPreferences(this);
        currentSorting = sharedPreferences.getString(getString(R.string.pref_sort_by_key), getString(R.string.pref_sort_by_default_value));

        //Initiate AsyncTask to download data from TMDb
        new GetData().execute();
    }

    @Override
    protected void onResume() {
        super.onResume();

        //Check if sorting preference has changed
        final String sortingPreference = sharedPreferences.getString(getString(R.string.pref_sort_by_key), "");
        if(!currentSorting.equals(sortingPreference)) {
            movieEntries.clear();
            new GetData().execute();
            updateGridContent();
            currentSorting = sortingPreference;
        }
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.menu_main, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        int id = item.getItemId();

        //noinspection SimplifiableIfStatement
        if (id == R.id.action_settings) {
            Intent settingsIntent = new Intent(this, SettingsActivity.class);
            startActivity(settingsIntent);
            return true;
        }

        return super.onOptionsItemSelected(item);
    }

    @Override
    public void onBackPressed() {
        FragmentManager manager = getFragmentManager();
        if(manager.getBackStackEntryCount() > 0) {
            manager.popBackStack();
        } else {
            super.onBackPressed();
        }
    }

    @Override
    public List<MovieEntry> getMoviesList() {
        return movieEntries;
    }

    @Override
    public void showMovieDetails(MovieEntry movie) {
        DetailsFragment fragment = new DetailsFragment();
        Bundle args = new Bundle();
        args.putParcelable(DetailsFragment.MOVIE_ARG_POSITION, movie);
        fragment.setArguments(args);

        android.app.FragmentTransaction transaction = getFragmentManager().beginTransaction();
        transaction.replace(R.id.activity_main_container, fragment);
        transaction.addToBackStack(null);
        transaction.commit();

        //Make sure action bar is not hidden
        try {
            getSupportActionBar().show();
        } catch (Exception e) {
            e.printStackTrace();
            Log.e("MainActivity", "SupportActionBar is null");
        }
    }

    private void updateGridContent() {
        MainActivityFragment fragment = (MainActivityFragment) getFragmentManager().findFragmentByTag("grid_view_fragment");
        if (fragment != null) {
            fragment.updateGridContent();
        }
    }

    private class GetData extends AsyncTask<Void, Void, String> {

        String LOG_TAG = GetData.class.getSimpleName();

        @Override
        protected void onPostExecute(String jsonData) {
            super.onPostExecute(jsonData);

            parseJson(jsonData);

            //Notify fragment that the data set has changed
            updateGridContent();
        }

        private void parseJson(final String jsonData) {
            final String RESULTS_TAG = "results";
            final String TITLE_TAG = "title";
            final String OVERVIEW_TAG = "overview";
            final String RELEASE_DATE_TAG = "release_date";
            final String POSTER_PATH_TAG = "poster_path";
            final String BACKDROP_PATH_TAG = "backdrop_path";
            final String VOTE_AVERAGE_TAG = "vote_average";

            try {
                JSONObject jsonStart = new JSONObject(jsonData);
                JSONArray results = jsonStart.getJSONArray(RESULTS_TAG);
                for(int i = 0; i < results.length(); i++) {
                    JSONObject movie = results.getJSONObject(i);
                    String title = movie.getString(TITLE_TAG);
                    String overview = movie.getString(OVERVIEW_TAG);
                    String releaseDate = movie.getString(RELEASE_DATE_TAG);
                    String posterPath = movie.getString(POSTER_PATH_TAG);
                    String backdropPath = movie.getString(BACKDROP_PATH_TAG);
                    double voteAverage = movie.getDouble(VOTE_AVERAGE_TAG);

                    movieEntries.add(new MovieEntry(title, overview, releaseDate, posterPath, backdropPath, voteAverage));
                }
            } catch (JSONException e) {
                e.printStackTrace();
                Log.e(LOG_TAG, "Error parsing json data");
            }
        }

        @Override
        protected String doInBackground(Void... params) {
            try {
                String url = constructUrl();
                Log.d(LOG_TAG, "Constructed url is: " + url);
                return downloadData(url);
            } catch (IOException e) {
                Log.e(LOG_TAG, "Error downloading data from url");
            }

            return null;
        }

        private String constructUrl() {
            final String SCHEME = "https";
            final String AUTHORITY = "api.themoviedb.org";
            final String API_VERSION = "3";
            final String SEARCH_TYPE = "discover";
            final String ITEM_TYPE = "movie";
            final String SORT_PARAM = "sort_by";
            final String MIN_VOTE_COUNT_PARAM = "vote_count.gte";
            final String API_KEY_PARAM = "api_key";
            final String API_KEY = "/* replace with own api key */";

            Uri.Builder builder = new Uri.Builder();
            builder.scheme(SCHEME)
                    .authority(AUTHORITY)
                    .appendPath(API_VERSION)
                    .appendPath(SEARCH_TYPE)
                    .appendPath(ITEM_TYPE)
                    .appendQueryParameter(SORT_PARAM, getSortingParam())
                    .appendQueryParameter(MIN_VOTE_COUNT_PARAM, "100")
                    .appendQueryParameter(API_KEY_PARAM, API_KEY);

            return builder.build().toString();
        }

        private String getSortingParam() {
            SharedPreferences preferences = PreferenceManager.getDefaultSharedPreferences(MainActivity.this);
            String sortByPref = preferences.getString(getString(R.string.pref_sort_by_key), getString(R.string.pref_sort_by_default_value));
            String sortParam = "";

            if(sortByPref.equals("Popularity")) {
                sortParam += "popularity";
            } else if(sortByPref.equals("Rating")) {
                sortParam += "vote_average";
            }

            sortParam += ".desc";

            return sortParam;
        }

        private String downloadData(String urlString) throws IOException {
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
}
