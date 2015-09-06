package com.example.michael.topmovies;

import android.app.Activity;
import android.content.res.Configuration;
import android.os.Bundle;
import android.support.v7.app.ActionBar;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.GridView;

import java.util.List;

/**
 * A placeholder fragment containing a simple view.
 */
public class MainActivityFragment extends android.app.Fragment {

    final String LOG_TAG = MainActivityFragment.class.getSimpleName();
    public static final String FRAGMENT_TAG = "mainActivityFragment";

    MoviesContainer moviesContainerCallback;
    TwoPane twoPaneCallback;
    ArrayAdapter<MovieEntry> adapter;
    List<MovieEntry> movieEntries;

    public interface MoviesContainer {
        List<MovieEntry> getMovieEntries();
        void showMovieDetails(MovieEntry movie);
    }

    public interface TwoPane {
        boolean isTwoPane();
    }

    public MainActivityFragment() {
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View rootView = inflater.inflate(R.layout.fragment_main, container, false);

        GridView gridView = (GridView) rootView.findViewById(R.id.grid_view);
        gridView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                moviesContainerCallback.showMovieDetails(movieEntries.get(position));
            }
        });

        return rootView;
    }

    @Override
    public void onResume() {
        super.onResume();

        //Set title text to "Top Movies"
        AppCompatActivity activity = (AppCompatActivity) getActivity();
        if(activity != null) {
            try {
                ActionBar supportActionBar = activity.getSupportActionBar();
                if (supportActionBar != null) {
                    supportActionBar.setTitle("Top Movies");
                }
            } catch (NullPointerException e) {
                e.printStackTrace();
                Log.e(LOG_TAG, "SupportActionBar is null");
            }
        }
    }

    @Override
    public void onActivityCreated(Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);

        Activity activity = getActivity();
        try {
            moviesContainerCallback = (MoviesContainer) activity;
            twoPaneCallback = (TwoPane) activity;
        } catch (ClassCastException e) {
            Log.e(LOG_TAG, "Activity did not implement MoviesContainer or TwoPane interface");
        }

        movieEntries = moviesContainerCallback.getMovieEntries();

        //Bind adapter to GridView
        adapter = new ImageAdapter(activity, movieEntries);
        GridView gridView = (GridView) activity.findViewById(R.id.grid_view);
        //Set grid view number of columns according to two-pane mode and orientation
        if(twoPaneCallback.isTwoPane()) {
            if((getResources().getConfiguration().screenLayout & Configuration.SCREENLAYOUT_SIZE_MASK) == Configuration.SCREENLAYOUT_SIZE_LARGE) {
                gridView.setNumColumns(2);
            } else {
                if(getResources().getConfiguration().orientation == Configuration.ORIENTATION_PORTRAIT) {
                    gridView.setNumColumns(1);
                }
            }
        }
        gridView.setAdapter(adapter);
    }

    public void updateGridContent() {
        if (adapter != null) {
            adapter.notifyDataSetChanged();
        }
    }
}
