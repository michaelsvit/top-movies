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

public class DetailsFragment extends Fragment {

    static final String MOVIE_ARG_POSITION = "MovieEntry";

    private MovieEntry movieEntry;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        View rootView = inflater.inflate(R.layout.fragment_details, container, false);

        //Populate views with data from movie argument
        movieEntry = getArguments().getParcelable(MOVIE_ARG_POSITION);

        fillDetails(rootView);

        return rootView;
    }

    private void fillDetails(View rootView) {
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

}
