package com.example.michael.topmovies;

import android.app.Fragment;
import android.app.FragmentManager;
import android.app.FragmentTransaction;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.res.Configuration;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.widget.LinearLayout;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

public class MainActivity extends AppCompatActivity implements
        MainActivityFragment.MoviesContainer, MainActivityFragment.TwoPane, DetailsFragment.Favorites, FavoritesDB.AddItemsToGrid {

    private boolean twoPane;
    private int screenSize;
    private int orientation;
    private FavoritesDB favoritesDB;
    protected List<MovieEntry> movieEntries;
    private String currentSorting;
    private SharedPreferences sharedPreferences;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        Configuration configuration = getResources().getConfiguration();
        screenSize = configuration.screenLayout & Configuration.SCREENLAYOUT_SIZE_MASK;
        orientation = configuration.orientation;
        twoPane = determineTwoPane();

        //Remove details fragment and holder if needed
        if((screenSize == Configuration.SCREENLAYOUT_SIZE_LARGE) && (orientation == Configuration.ORIENTATION_PORTRAIT)) {
            LinearLayout fragmentsContainer = (LinearLayout)findViewById(R.id.main_activity_fragments_container);
            fragmentsContainer.removeView(findViewById(R.id.details_fragment));
        }
        //Attach fragment(s) depending on layout size
        attachFragments();

        movieEntries = new ArrayList<>();
        sharedPreferences = PreferenceManager.getDefaultSharedPreferences(this);
        currentSorting = sharedPreferences.getString(getString(R.string.pref_sort_by_key), getString(R.string.pref_sort_by_default_value));

        if(!currentSorting.equals("Favorites")) {
            //Initiate AsyncTask to download data from TMDb
            new GetMoviesList().execute();

            if(savedInstanceState == null) {
                //Load favorites database for later use
                favoritesDB = new FavoritesDB(this, false);
            }
        } else {
            if(savedInstanceState == null) {
                //Show favorites from database
                favoritesDB = new FavoritesDB(this, true);
            }
        }
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        if(favoritesDB != null) {
            favoritesDB.closeDatabase();
        }
    }

    private void attachFragments() {
        FragmentManager fragmentManager = getFragmentManager();

        //If fragments are already attached, remove them
        Fragment fragment = fragmentManager.findFragmentByTag(MainActivityFragment.FRAGMENT_TAG);
        if(fragment != null) {
            fragmentManager.beginTransaction().remove(fragment).commit();
        }

        fragment = fragmentManager.findFragmentByTag(DetailsFragment.FRAGMENT_TAG);
        if(fragment != null) {
            if ((orientation == Configuration.ORIENTATION_PORTRAIT || screenSize == Configuration.SCREENLAYOUT_SIZE_NORMAL) && screenSize != Configuration.SCREENLAYOUT_SIZE_XLARGE) {
                attachMasterFragment(fragment);
            } else {
                //Move details fragment from master_fragment to details_fragment
                attachMasterFragment(null);
                attachDetailsFragment(fragment);
            }
        } else {
            attachMasterFragment(null);
            if(twoPane) {
                attachDetailsFragment(null);
            }
        }
    }

    private void attachMasterFragment(Fragment fragment) {
        FragmentManager fragmentManager = getFragmentManager();
        FragmentTransaction transaction = fragmentManager.beginTransaction();
        int fragmentContainer = R.id.master_fragment;

        if(fragment == null) {
            fragment = new MainActivityFragment();
            transaction.add(fragmentContainer, fragment, MainActivityFragment.FRAGMENT_TAG).commit();
        } else {
            transaction.remove(fragment).commit();
            fragmentManager.executePendingTransactions();
            transaction = fragmentManager.beginTransaction();
            transaction.add(fragmentContainer, fragment, DetailsFragment.FRAGMENT_TAG).commit();
        }
    }

    private void attachDetailsFragment(Fragment fragment) {
        FragmentManager fragmentManager = getFragmentManager();
        FragmentTransaction transaction = fragmentManager.beginTransaction();
        int fragmentContainer = R.id.details_fragment;

        if(fragment == null) {
            fragment = new DetailsFragment();
            Bundle args = new Bundle();
            args.putInt(DetailsFragment.FRAGMENT_RES_ID_POSITION, R.layout.details_fragment_blank);
            fragment.setArguments(args);
            transaction.add(fragmentContainer, fragment, "blankFragment").commit();
        } else {
            fragmentManager.popBackStackImmediate(null, FragmentManager.POP_BACK_STACK_INCLUSIVE);
            transaction.remove(fragment).commit();
            fragmentManager.executePendingTransactions();
            transaction = fragmentManager.beginTransaction();
            transaction.add(fragmentContainer, fragment, DetailsFragment.FRAGMENT_TAG).commit();
        }
    }

    private boolean determineTwoPane() {
        if(findViewById(R.id.details_fragment) != null) {
            if ((screenSize == Configuration.SCREENLAYOUT_SIZE_LARGE) && (orientation == Configuration.ORIENTATION_PORTRAIT)) {
                return false;
            } else {
                return true;
            }
        } else {
            return false;
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
        Fragment mainActivityFragment = manager.findFragmentByTag(MainActivityFragment.FRAGMENT_TAG);

        if(manager.getBackStackEntryCount() > 0) {
            manager.popBackStack();
        } else if(!twoPane && screenSize != Configuration.SCREENLAYOUT_SIZE_NORMAL && mainActivityFragment == null) {
            replaceMasterFragment(new MainActivityFragment());
        } else {
            super.onBackPressed();
        }
    }

    private void replaceMasterFragment(Fragment fragment) {
        FragmentManager fragmentManager = getFragmentManager();
        FragmentTransaction transaction = fragmentManager.beginTransaction();
        transaction.replace(R.id.master_fragment, fragment, MainActivityFragment.FRAGMENT_TAG).commit();
    }

    @Override
    public List<MovieEntry> getMovieEntries() {
        return movieEntries;
    }

    @Override
    public void showMovieDetails(MovieEntry movie) {
        //Show movie details in a new fragment
        Fragment fragment = new DetailsFragment();
        Bundle args = new Bundle();
        args.putParcelable(DetailsFragment.MOVIE_ARG_POSITION, movie);
        fragment.setArguments(args);

        FragmentTransaction transaction = getFragmentManager().beginTransaction();
        if(!twoPane) {
            transaction.replace(R.id.master_fragment, fragment, DetailsFragment.FRAGMENT_TAG);
            if(screenSize == Configuration.SCREENLAYOUT_SIZE_NORMAL) {
                transaction.addToBackStack(null);
            }
        } else {
            transaction.replace(R.id.details_fragment, fragment, DetailsFragment.FRAGMENT_TAG);
        }

        transaction.commit();
    }

    private void updateGridContent() {
        MainActivityFragment fragment = (MainActivityFragment) getFragmentManager().findFragmentByTag(MainActivityFragment.FRAGMENT_TAG);
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

    @Override
    public boolean isTwoPane() {
        return twoPane;
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
                String jsonData = downloadData(discoverUrl);
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
