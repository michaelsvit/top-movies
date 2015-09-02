package com.example.michael.topmovies;

import android.app.Fragment;
import android.net.Uri;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import com.squareup.picasso.Picasso;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

public class DetailsFragment extends Fragment {

    static final String MOVIE_ARG_POSITION = "MovieEntry";

    private MovieEntry movieEntry;
    private View rootView;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        rootView = inflater.inflate(R.layout.fragment_details, container, false);

        //Populate views with data from movie argument
        movieEntry = getArguments().getParcelable(MOVIE_ARG_POSITION);
        new GetTrailerAndReviews().execute();

        fillDetails();

        return rootView;
    }

    private void fillDetails() {
        //Update action bar text
        ((AppCompatActivity)getActivity()).getSupportActionBar().setTitle(movieEntry.getTitle());

        ImageView backdrop = (ImageView) rootView.findViewById(R.id.details_backdrop);
        Picasso.with(getActivity())
                .load(constructBackdropUri(movieEntry.getBackdropPath()))
                .into(backdrop);

        TextView releaseDate = (TextView) rootView.findViewById(R.id.details_release_date);
        releaseDate.setText(movieEntry.getReleaseDate());

        TextView rating = (TextView) rootView.findViewById(R.id.details_rating);
        rating.setText(String.valueOf(movieEntry.getVoteAverage()));

        ImageView poster = (ImageView) rootView.findViewById(R.id.details_movie_poster);
        Picasso.with(getActivity())
                .load(constructPosterUri(movieEntry.getPosterPath()))
                .into(poster);

        TextView synopsis = (TextView) rootView.findViewById(R.id.details_synopsis);
        synopsis.setText(movieEntry.getOverview());
    }

    private void fillTrailersAndReviews() {

    }

    private Uri constructBackdropUri(String path) {
        final String BACKDROP_SIZE = "w1280";
        return constructUri(path, BACKDROP_SIZE);
    }

    private Uri constructPosterUri(String path) {
        final String POSTER_SIZE = "w342";
        return constructUri(path, POSTER_SIZE);
    }

    private Uri constructUri(String path, String size) {
        Uri.Builder builder = new Uri.Builder();
        final String SCHEME = "http";
        final String AUTHORITY = "image.tmdb.org";
        final String TITLE_PATH = "t";
        final String POSTER_PATH = "p";
        final String POSTER_ID = path;

        builder.scheme(SCHEME)
                .authority(AUTHORITY)
                .appendPath(TITLE_PATH)
                .appendPath(POSTER_PATH)
                .appendPath(size)
                .appendEncodedPath(POSTER_ID);

        Log.d("DetailsFragment", builder.build().toString());

        return builder.build();
    }

    private class GetTrailerAndReviews extends GetData {
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
