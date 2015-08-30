package com.example.michael.topmovies;

import android.content.Context;
import android.widget.ImageView;

/**
 * Created by Michael on 8/30/2015.
 */
public class PosterImageView extends ImageView {
    public PosterImageView(Context context) {
        super(context);
    }

    @Override
    protected void onMeasure(int widthMeasureSpec, int heightMeasureSpec) {
        super.onMeasure(widthMeasureSpec, heightMeasureSpec);
        int measuredWidth = getMeasuredWidth();
        setMeasuredDimension(measuredWidth, (int)(measuredWidth * 1.5));
    }
}
