package com.example.michael.topmovies;

import android.app.FragmentManager;
import android.app.FragmentTransaction;
import android.content.Intent;
import android.content.SharedPreferences;
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
import java.util.ArrayList;
import java.util.List;

public class MainActivity extends AppCompatActivity implements
        MainActivityFragment.GetMoviesList, DetailsFragment.Favorites, FavoritesDB.AddItemsToGrid {

    private FavoritesDB favoritesDB;
    protected List<MovieEntry> movieEntries;
    private String currentSorting;
    private SharedPreferences sharedPreferences;

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

        if(!currentSorting.equals("Favorites")) {
            //Initiate AsyncTask to download data from TMDb
            new GetMoviesList().execute();

            //Load favorites database for later use
            favoritesDB = new FavoritesDB(this, false);
        } else {
            //Show favorites from database
            favoritesDB = new FavoritesDB(this, true);
        }
    }

    @Override
    protected void onResume() {
        super.onResume();

        //Check if sorting preference has changed
        final String sortingPreference = sharedPreferences.getString(getString(R.string.pref_sort_by_key), getString(R.string.pref_sort_by_default_value));
        if(!currentSorting.equals(sortingPreference)) {
            movieEntries.clear();
            if(sortingPreference.equals("Favorites")) {
                movieEntries.addAll(favoritesDB.getFavorites());
                updateGridContent();
            } else {
                new GetMoviesList().execute();
            }
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
    public List<MovieEntry> getMovieEntries() {
        return movieEntries;
    }

    @Override
    public void showMovieDetails(MovieEntry movie) {
        //Show movie details in a new fragment
        DetailsFragment fragment = new DetailsFragment();
        Bundle args = new Bundle();
        args.putParcelable(DetailsFragment.MOVIE_ARG_POSITION, movie);
        fragment.setArguments(args);

        android.app.FragmentTransaction transaction = getFragmentManager().beginTransaction();
        transaction.replace(R.id.activity_main_container, fragment);
        transaction.addToBackStack(null);
        transaction.commit();
    }

    private void updateGridContent() {
        MainActivityFragment fragment = (MainActivityFragment) getFragmentManager().findFragmentByTag("grid_view_fragment");
        if (fragment != null) {
            fragment.updateGridContent();
        }
    }

    @Override
    public FavoritesDB getFavoritesDB() {
        return favoritesDB;
    }

    @Override
    public void removeFavoriteFromGrid(MovieEntry movieEntry) {
        movieEntries.remove(movieEntry);
        updateGridContent();
    }

    @Override
    public void addFavorites(List<MovieEntry> movieEntries) {
        this.movieEntries.addAll(movieEntries);
        updateGridContent();
    }

    private class GetMoviesList extends GetData {
        @Override
        protected void onPostExecute(Void aVoid) {
            super.onPostExecute(aVoid);

            updateGridContent();
        }

        @Override
        protected Void doInBackground(Void... params) {
            try {
                String discoverUrl = constructDiscoverUrl();
                Log.d(LOG_TAG, "Constructed URL: " + discoverUrl);
                String jsonData = downloadData(discoverUrl);
                Log.d(LOG_TAG, "JSON data: " + jsonData);
                List<MovieEntry> movies = parseDiscoverJson(jsonData);

                if (movies != null) {
                    movieEntries.addAll(movies);
                }

                return null;
            } catch (IOException e) {
                Log.e(LOG_TAG, "Error downloading data from url");
            }
            return null;
        }

        private List<MovieEntry> parseDiscoverJson(final String jsonData) {
            final String RESULTS_TAG = "results";
            final String ID_TAG = "id";
            final String TITLE_TAG = "title";
            final String OVERVIEW_TAG = "overview";
            final String RELEASE_DATE_TAG = "release_date";
            final String POSTER_PATH_TAG = "poster_path";
            final String BACKDROP_PATH_TAG = "backdrop_path";
            final String VOTE_AVERAGE_TAG = "vote_average";

            try {
                List<MovieEntry> movies = new ArrayList<>();
                JSONObject jsonStart = new JSONObject(jsonData);
                JSONArray results = jsonStart.getJSONArray(RESULTS_TAG);
                for(int i = 0; i < results.length(); i++) {
                    JSONObject movie = results.getJSONObject(i);
                    int id = movie.getInt(ID_TAG);
                    String title = movie.getString(TITLE_TAG);
                    String overview = movie.getString(OVERVIEW_TAG);
                    String releaseDate = movie.getString(RELEASE_DATE_TAG);
                    String posterPath = movie.getString(POSTER_PATH_TAG);
                    String backdropPath = movie.getString(BACKDROP_PATH_TAG);
                    double voteAverage = movie.getDouble(VOTE_AVERAGE_TAG);

                    movies.add(new MovieEntry(id, title, overview, releaseDate, posterPath, backdropPath, voteAverage));
                }
                return movies;
            } catch (JSONException e) {
                e.printStackTrace();
                Log.e(LOG_TAG, "Error parsing json data");
            }
            return null;
        }

        private String constructDiscoverUrl() {

            final String[] PATH = {"discover", "movie"};
            final String[] PARAM_KEYS = {"sort_by", "vote_count.gte", "api_key"};
            final String[] PARAM_VALUES = {getSortingParam() ,"100", "/* replace with own api key */"};

            return constructUrl(PATH, PARAM_KEYS, PARAM_VALUES).toString();
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
    }
}
