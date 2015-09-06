package com.example.michael.topmovies;

import android.app.Activity;
import android.content.Context;
import android.content.res.Configuration;
import android.util.DisplayMetrics;
import android.widget.GridView;
import android.widget.ImageView;

/**
 * Created by Michael on 8/30/2015.
 * Extended ImageView set up for a movie poster
 */
public class PosterImageView extends ImageView {

    Context context;
    GridView gridView;

    public PosterImageView(Context context) {
        super(context);
        setScaleType(ScaleType.FIT_CENTER);
        this.context = context;
        getGridViewWidth();
    }

    private void getGridViewWidth() {
        gridView = (GridView) ((Activity) context).findViewById(R.id.grid_view);
    }

    @Override
    protected void onMeasure(int widthMeasureSpec, int heightMeasureSpec) {
        super.onMeasure(widthMeasureSpec, heightMeasureSpec);
        final double aspectRatio = 1.5;

        Configuration configuration = getResources().getConfiguration();
        if (configuration.orientation == Configuration.ORIENTATION_PORTRAIT) {
            int measuredWidth = getMeasuredWidth();
            setMeasuredDimension(measuredWidth, (int)(measuredWidth * aspectRatio));
        } else {
            if ((configuration.screenLayout & Configuration.SCREENLAYOUT_SIZE_MASK) < Configuration.SCREENLAYOUT_SIZE_LARGE) {
                int measuredHeight = getScreenHeight();
                setMeasuredDimension((int) (measuredHeight / aspectRatio), measuredHeight);
            } else {
                int measuredWidth = gridView.getWidth() / gridView.getNumColumns();
                setMeasuredDimension(measuredWidth, (int) (measuredWidth * aspectRatio));
            }
        }
    }

    private int getScreenHeight() {
        DisplayMetrics displayMetrics = getResources().getDisplayMetrics();
        return displayMetrics.heightPixels;
    }
}
