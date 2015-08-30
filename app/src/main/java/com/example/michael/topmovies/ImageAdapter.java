package com.example.michael.topmovies;

import android.content.Context;
import android.net.Uri;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.FrameLayout;
import android.widget.ImageView;

import com.squareup.picasso.Picasso;

import java.util.List;

/**
 * Created by Michael on 8/22/2015.
 * Custom ArrayAdapter to fill gridView with movie posters
 */
public class ImageAdapter extends ArrayAdapter<MovieEntry> {
    Context context;
    List<MovieEntry> movies;
    LayoutInflater layoutInflater;

    public ImageAdapter(Context context, List<MovieEntry> movies) {
        //grid_item layout isn't used, only there to allow calling super()
        super(context, R.layout.grid_item, movies);
        this.context = context;
        this.movies = movies;
        layoutInflater = (LayoutInflater) context.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        //Make sure convertView is inflated already before recycling it
        if (convertView == null) {
            //Use extended ImageView with preset aspect ratio of 1.5
            convertView = new PosterImageView(context);
            convertView.setLayoutParams(new FrameLayout.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT));
            ((ImageView)convertView).setScaleType(ImageView.ScaleType.FIT_CENTER);
        }

        //Put image into imageView using Picasso
        ImageView imageView = (ImageView) convertView;
        Picasso.with(context).load(constructImageUri(movies.get(position).getPosterPath())).into(imageView);

        return imageView;
    }

    private Uri constructImageUri(final String POSTER_ID) {
        Uri.Builder builder = new Uri.Builder();
        final String SCHEME = "http";
        final String AUTHORITY = "image.tmdb.org";
        final String TITLE_PATH = "t";
        final String POSTER_PATH = "p";
        final String POSTER_SIZE = "w342";

        builder.scheme(SCHEME)
                .authority(AUTHORITY)
                .appendPath(TITLE_PATH)
                .appendPath(POSTER_PATH)
                .appendPath(POSTER_SIZE)
                .appendEncodedPath(POSTER_ID);

        Log.d("ImageAdapter", builder.build().toString());

        return builder.build();
    }
}
