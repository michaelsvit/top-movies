package com.example.michael.topmovies;

import android.provider.BaseColumns;

/**
 * Created by Michael on 9/4/2015.
 */
public final class FavoritesContract {

    public static final String TEXT_TYPE = " TEXT";
    public static final String INTEGER_TYPE = " INTEGER";
    public static final String DOUBLE_TYPE = " DOUBLE PRECISION";
    public static final String COMMA_SEP = ",";
    public static final String SQL_CREATE_FAVORITES = "CREATE TABLE " + Favorites.TABLE_NAME + " ("
            + Favorites._ID + INTEGER_TYPE + "PRIMARY KEY,"
            + Favorites.COLUMN_NAME_MOVIE_ID + INTEGER_TYPE + COMMA_SEP
            + Favorites.COLUMN_NAME_MOVIE_TITLE + TEXT_TYPE + COMMA_SEP
            + Favorites.COLUMN_NAME_MOVIE_OVERVIEW + TEXT_TYPE + COMMA_SEP
            + Favorites.COLUMN_NAME_MOVIE_RELEASE_DATE + TEXT_TYPE + COMMA_SEP
            + Favorites.COLUMN_NAME_MOVIE_POSTER_PATH + TEXT_TYPE + COMMA_SEP
            + Favorites.COLUMN_NAME_MOVIE_BACKDROP_PATH + TEXT_TYPE + COMMA_SEP
            + Favorites.COLUMN_NAME_MOVIE_VOTE_AVERAGE + DOUBLE_TYPE + ")";
    public static final String SQL_DELETE_FAVORITES = "DROP TABLE IF EXISTS" + Favorites.TABLE_NAME;

    public FavoritesContract() {
    }

    public static abstract class Favorites implements BaseColumns {
        public static final String TABLE_NAME = "favorites";
        public static final String COLUMN_NAME_MOVIE_ID = "id";
        public static final String COLUMN_NAME_MOVIE_TITLE = "title";
        public static final String COLUMN_NAME_MOVIE_OVERVIEW = "overview";
        public static final String COLUMN_NAME_MOVIE_RELEASE_DATE = "release_date";
        public static final String COLUMN_NAME_MOVIE_POSTER_PATH = "poster_path";
        public static final String COLUMN_NAME_MOVIE_BACKDROP_PATH = "backdrop_path";
        public static final String COLUMN_NAME_MOVIE_VOTE_AVERAGE = "vote_average";
    }
}
