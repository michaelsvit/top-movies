package com.example.michael.topmovies;

import android.app.Activity;
import android.content.Context;
import android.os.Bundle;
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
    GetMoviesList callback;
    ArrayAdapter<MovieEntry> adapter;
    List<MovieEntry> movieEntries;

    public interface GetMoviesList {
        List<MovieEntry> getMoviesList();
        void showMovieDetails(MovieEntry movie);
    }

    public MainActivityFragment() {
    }

    @Override
    public void onAttach(Context context) {
        super.onAttach(context);


    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View rootView = inflater.inflate(R.layout.fragment_main, container, false);

        GridView gridView = (GridView) rootView.findViewById(R.id.grid_view);
        gridView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                callback.showMovieDetails(movieEntries.get(position));
            }
        });

        return rootView;
    }

    @Override
    public void onResume() {
        super.onResume();

        //Set title text to "Top Movies"
        AppCompatActivity activity = (AppCompatActivity) getActivity();
        if(activity != null)
        activity.getSupportActionBar().setTitle("Top Movies");
    }

    @Override
    public void onActivityCreated(Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);

        Activity activity = getActivity();
        try {
            callback = (GetMoviesList) activity;
        } catch (ClassCastException e) {
            Log.e(LOG_TAG, "Activity did not implement GetMoviesList interface");
        }

        movieEntries = callback.getMoviesList();

        //Bind adapter to GridView
        adapter = new ImageAdapter(activity, movieEntries);
        GridView gridView = (GridView) activity.findViewById(R.id.grid_view);
        gridView.setAdapter(adapter);
    }

    public void updateGridContent() {
        adapter.notifyDataSetChanged();
    }
}
