package com.example.michael.topmovies;

import android.app.Activity;
import android.app.Fragment;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.support.v7.app.ActionBar;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.squareup.picasso.Picasso;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

public class DetailsFragment extends Fragment {

    public static final String MOVIE_ARG_POSITION = "MovieEntry";
    public static final String FRAGMENT_RES_ID_POSITION = "resId";
    public static final String FRAGMENT_TAG = "detailsFragment";
    private final String LOG_TAG = DetailsFragment.class.getSimpleName();

    private MovieEntry movieEntry;
    private View rootView;
    private Favorites callback;
    private FavoritesDB favoritesDB;

    @Override
    public void onDestroy() {
        super.onDestroy();
        if(favoritesDB != null) {
            favoritesDB.closeDatabase();
        }
    }

    public interface Favorites {
        void removeFavoriteFromGrid(MovieEntry movieEntry);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        Bundle args = getArguments();
        // Inflate the layout for this fragment
        int resId;
        if(args.containsKey(FRAGMENT_RES_ID_POSITION)) {
            resId = args.getInt(FRAGMENT_RES_ID_POSITION);
        } else {
            resId = R.layout.fragment_details;
        }
        rootView = inflater.inflate(resId, container, false);

        favoritesDB = new FavoritesDB(getActivity(), false);

        movieEntry = args.getParcelable(MOVIE_ARG_POSITION);
        if (movieEntry != null) {
            setHasOptionsMenu(true);

            //Populate views with data from movie argument
            new GetTrailerAndReviews().execute();
            fillDetails();
        }

        return rootView;
    }

    @Override
    public void onActivityCreated(Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);

        Activity activity = getActivity();
        try {
            callback = (Favorites) activity;
        } catch (ClassCastException e) {
            Log.e(LOG_TAG, "MainActivity did not implement Favorites");
        }
    }

    @Override
    public void onCreateOptionsMenu(Menu menu, MenuInflater inflater) {
        //Check if the movie is a favorite and show favorite menu action
        MenuItem favoritesMenuItem = menu.findItem(R.id.action_favorite);

        if (favoritesDB != null) {
            if (favoritesDB.isFavorite(movieEntry.getId())) {
                favoritesMenuItem.setIcon(android.R.drawable.btn_star_big_on);
            }
        } else {
            Log.e(LOG_TAG, "Trying to access null favorites database");
        }

        favoritesMenuItem.setVisible(true);
        super.onCreateOptionsMenu(menu, inflater);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        if(item.getItemId() == R.id.action_favorite) {
            //Toggle icon state and  favorite state
            toggleFavorite(item);
            return true;
        } else {
            return super.onOptionsItemSelected(item);
        }
    }

    private void toggleFavorite(MenuItem item) {
        int movieId = movieEntry.getId();
        //Check if the movie is already a favorite
        if (favoritesDB != null) {
            if(favoritesDB.isFavorite(movieId)) {
                item.setIcon(android.R.drawable.btn_star_big_off);
                favoritesDB.removeFavorite(movieId);
                callback.removeFavoriteFromGrid(movieEntry);
            } else {
                item.setIcon(android.R.drawable.btn_star_big_on);
                favoritesDB.setFavorite(movieEntry);
            }
        }
    }

    private void fillDetails() {
        //Update action bar text
        Activity activity = getActivity();
        if (activity != null) {
            ActionBar actionBar = ((AppCompatActivity) activity).getSupportActionBar();
            if (actionBar != null) {
                actionBar.setTitle(movieEntry.getTitle());
            }

            ImageView backdrop = (ImageView) rootView.findViewById(R.id.details_backdrop);
            Picasso.with(activity)
                    .load(constructBackdropUri(movieEntry.getBackdropPath()))
                    .into(backdrop);

            TextView releaseDate = (TextView) rootView.findViewById(R.id.details_release_date);
            releaseDate.setText(movieEntry.getReleaseDate());

            TextView rating = (TextView) rootView.findViewById(R.id.details_rating);
            rating.setText(String.valueOf(movieEntry.getVoteAverage()));

            ImageView poster = (ImageView) rootView.findViewById(R.id.details_movie_poster);
            Picasso.with(activity)
                    .load(constructPosterUri(movieEntry.getPosterPath()))
                    .into(poster);

            TextView synopsis = (TextView) rootView.findViewById(R.id.details_synopsis);
            synopsis.setText(movieEntry.getOverview());
        }
    }

    private void fillTrailersAndReviews() {
        Activity activity = getActivity();
        if(activity != null) {
            if (movieEntry.getTrailers() != null) {
                LinearLayout trailersContainer = (LinearLayout) rootView.findViewById(R.id.details_trailers_container);
                //Fill trailers section with entries and add separator lines
                fillTrailers(activity, trailersContainer);
            }

            if (movieEntry.getReviews() != null) {
                LinearLayout reviewsContainer = (LinearLayout) rootView.findViewById(R.id.details_reviews_container);
                //Fill reviews section with reviews and add separator lines
                fillReviews(activity, reviewsContainer);
            }
        }
    }

    private void fillReviews(Activity activity, LinearLayout reviewsContainer) {
        for (Review review : movieEntry.getReviews()) {
            TextView authorTextView = new TextView(activity);
            LinearLayout.LayoutParams authorLayoutParams = new LinearLayout.LayoutParams(LinearLayout.LayoutParams.MATCH_PARENT, LinearLayout.LayoutParams.WRAP_CONTENT);
            authorLayoutParams.setMargins(16, 8, 16, 8);
            authorTextView.setLayoutParams(authorLayoutParams);
            authorTextView.setText(review.getAuthor());
            authorTextView.setTextSize(16);
            authorTextView.setGravity(Gravity.LEFT);
            authorTextView.setTextColor(getResources().getColor(R.color.textSecondary));
            reviewsContainer.addView(authorTextView);

            TextView contentTextView = new TextView(activity);
            LinearLayout.LayoutParams contentLayoutParams = new LinearLayout.LayoutParams(LinearLayout.LayoutParams.MATCH_PARENT, LinearLayout.LayoutParams.WRAP_CONTENT);
            contentLayoutParams.setMargins(8, 16, 8, 16);
            contentTextView.setLayoutParams(contentLayoutParams);
            contentTextView.setText(review.getContent());
            contentTextView.setTextSize(14);
            contentTextView.setGravity(Gravity.LEFT);
            contentTextView.setTextColor(getResources().getColor(R.color.textPrimary));
            reviewsContainer.addView(contentTextView);

            View view = new View(activity);
            LinearLayout.LayoutParams layoutParams = new LinearLayout.LayoutParams(LinearLayout.LayoutParams.MATCH_PARENT, 1);
            layoutParams.setMargins(24, 0, 24, 0);
            view.setLayoutParams(layoutParams);
            view.setBackgroundColor(getResources().getColor(R.color.textSecondary));
            reviewsContainer.addView(view);
        }
        //Remove last line separator
        int reviewsChildCount = reviewsContainer.getChildCount();
        if (reviewsChildCount > 0) {
            reviewsContainer.removeViewAt(reviewsChildCount - 1);
        }
    }

    private void fillTrailers(Activity activity, LinearLayout trailersContainer) {
        for (final Trailer trailer : movieEntry.getTrailers()) {
            TextView trailerTextView = new TextView(activity);
            trailerTextView.setLayoutParams(new LinearLayout.LayoutParams(LinearLayout.LayoutParams.MATCH_PARENT, LinearLayout.LayoutParams.WRAP_CONTENT));
            trailerTextView.setPadding(0, 40, 0, 40);
            trailerTextView.setText(trailer.getName());
            trailerTextView.setTextSize(20);
            trailerTextView.setGravity(Gravity.CENTER);
            trailerTextView.setTextColor(getResources().getColor(R.color.textPrimary));
            trailerTextView.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    Intent videoIntent = new Intent(Intent.ACTION_VIEW, Uri.parse(getVideoUrl(trailer.getKey())));
                    startActivity(videoIntent);
                }
            });
            trailersContainer.addView(trailerTextView);

            View view = new View(activity);
            LinearLayout.LayoutParams layoutParams = new LinearLayout.LayoutParams(LinearLayout.LayoutParams.MATCH_PARENT, 1);
            layoutParams.setMargins(24, 0, 24, 0);
            view.setLayoutParams(layoutParams);
            view.setBackgroundColor(getResources().getColor(R.color.textSecondary));
            trailersContainer.addView(view);
        }
        //Remove last line separator
        int trailersChildCount = trailersContainer.getChildCount();
        if (trailersChildCount > 0) {
            trailersContainer.removeViewAt(trailersChildCount - 1);
        }
    }

    private String getVideoUrl(String key) {
        String youtubeUrl = "https://www.youtube.com/watch?v=";
        return youtubeUrl + key;
    }

    private Uri constructBackdropUri(String path) {
        final String BACKDROP_SIZE = "w1280";
        return constructUri(path, BACKDROP_SIZE);
    }

    private Uri constructPosterUri(String path) {
        final String POSTER_SIZE = "w342";
        return constructUri(path, POSTER_SIZE);
    }

    private Uri constructUri(final String POSTER_ID, String size) {
        Uri.Builder builder = new Uri.Builder();
        final String SCHEME = "http";
        final String AUTHORITY = "image.tmdb.org";
        final String TITLE_PATH = "t";
        final String POSTER_PATH = "p";

        builder.scheme(SCHEME)
                .authority(AUTHORITY)
                .appendPath(TITLE_PATH)
                .appendPath(POSTER_PATH)
                .appendPath(size)
                .appendEncodedPath(POSTER_ID);

        Log.d(LOG_TAG, builder.build().toString());

        return builder.build();
    }

    private class GetTrailerAndReviews extends GetData {
        @Override
        protected void onPostExecute(Void aVoid) {
            super.onPostExecute(aVoid);

            fillTrailersAndReviews();
        }

        @Override
        protected Void doInBackground(Void... params) {
            try {
                //Get movie trailers
                String trailersUrl = constructTrailersUrl(movieEntry.getId());
                List<Trailer> trailers = parseTrailersJson(downloadData(trailersUrl));
                movieEntry.setTrailers(trailers);

                //Get movie reviews
                String reviewsUrl = constructReviewsUrl(movieEntry.getId());
                List<Review> reviews = parseReviewsJson(downloadData(reviewsUrl));
                movieEntry.setReviews(reviews);
            } catch (IOException e) {
            Log.e(LOG_TAG, "Error downloading data from url");
            }
            return null;
        }

        private String constructTrailersUrl(final int movieId) {

            final String[] PATH = {"movie", String.valueOf(movieId), "videos"};
            final String[] PARAM_KEYS = {"api_key"};
            final String[] PARAM_VALUES = {"73365d6bd1beec3e25e8a8986f7f9679"};

            return constructUrl(PATH, PARAM_KEYS, PARAM_VALUES).toString();
        }

        private String constructReviewsUrl(final int movieId) {

            final String[] PATH = {"movie", String.valueOf(movieId), "reviews"};
            final String[] PARAM_KEYS = {"api_key"};
            final String[] PARAM_VALUES = {"73365d6bd1beec3e25e8a8986f7f9679"};

            return constructUrl(PATH, PARAM_KEYS, PARAM_VALUES).toString();
        }

        private List<Trailer> parseTrailersJson(final String jsonData) {
            final String RESULTS_TAG = "results";
            final String KEY_TAG = "key";
            final String NAME_TAG = "name";
            final String TYPE_TAG = "type";

            try {
                List<Trailer> trailers = new ArrayList<>();
                JSONObject jsonStart = new JSONObject(jsonData);
                JSONArray results = jsonStart.getJSONArray(RESULTS_TAG);
                for(int i = 0; i < results.length(); i++) {
                    JSONObject trailer = results.getJSONObject(i);
                    if(trailer.getString(TYPE_TAG).equals("Trailer")) {
                        String key = trailer.getString(KEY_TAG);
                        String name = trailer.getString(NAME_TAG);
                        trailers.add(new Trailer(key, name));
                    }
                }
                return trailers;
            } catch (JSONException e) {
                e.printStackTrace();
                Log.e(LOG_TAG, "Error parsing json data");
            }
            return null;
        }

        private List<Review> parseReviewsJson(final String jsonData) {
            final String RESULTS_TAG = "results";
            final String AUTHOR_TAG = "author";
            final String CONTENT_TAG = "content";

            try {
                List<Review> reviews = new ArrayList<>();
                JSONObject jsonStart = new JSONObject(jsonData);
                JSONArray results = jsonStart.getJSONArray(RESULTS_TAG);
                for(int i = 0; i < results.length(); i++) {
                    JSONObject review = results.getJSONObject(i);
                    String author = review.getString(AUTHOR_TAG);
                    String content = review.getString(CONTENT_TAG);
                    reviews.add(new Review(author, content));
                }
                return reviews;
            } catch (JSONException e) {
                e.printStackTrace();
                Log.e(LOG_TAG, "Error parsing json data");
            }
            return null;
        }
    }
}
