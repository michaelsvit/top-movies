package com.example.michael.topmovies;

import android.content.Context;
import android.content.res.Configuration;
import android.util.DisplayMetrics;
import android.widget.ImageView;

/**
 * Created by Michael on 8/30/2015.
 * Extended ImageView set up for a movie poster
 */
public class PosterImageView extends ImageView {
    Context context;
    public PosterImageView(Context context) {
        super(context);
        setScaleType(ScaleType.FIT_CENTER);
        this.context = context;
    }

    @Override
    protected void onMeasure(int widthMeasureSpec, int heightMeasureSpec) {
        super.onMeasure(widthMeasureSpec, heightMeasureSpec);
        final double aspectRatio = 1.5;

        if (context.getResources().getConfiguration().orientation == Configuration.ORIENTATION_PORTRAIT) {
            int measuredWidth = getMeasuredWidth();
            setMeasuredDimension(measuredWidth, (int)(measuredWidth * aspectRatio));
        } else {
            int measuredHeight = getScreenHeight();
            setMeasuredDimension((int) (measuredHeight / aspectRatio), measuredHeight);
        }
    }

    private int getScreenHeight() {
        DisplayMetrics displayMetrics = context.getResources().getDisplayMetrics();
        return displayMetrics.heightPixels;
    }
}
