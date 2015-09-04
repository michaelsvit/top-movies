package com.example.michael.topmovies;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.os.AsyncTask;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by Michael on 9/4/2015.
 */
public class FavoritesDB {
    FavoritesDbHelper dbHelper;
    SQLiteDatabase db;

    public FavoritesDB(Context context) {
        dbHelper = new FavoritesDbHelper(context);
        new GetDB().execute();
    }

    public void setFavorite(MovieEntry movieEntry) {
        if(db != null) {
            ContentValues values = new ContentValues();
            values.put(FavoritesContract.Favorites.COLUMN_NAME_MOVIE_ID, movieEntry.getId());
            values.put(FavoritesContract.Favorites.COLUMN_NAME_MOVIE_TITLE, movieEntry.getTitle());
            values.put(FavoritesContract.Favorites.COLUMN_NAME_MOVIE_OVERVIEW, movieEntry.getOverview());
            values.put(FavoritesContract.Favorites.COLUMN_NAME_MOVIE_RELEASE_DATE, movieEntry.getReleaseDate());
            values.put(FavoritesContract.Favorites.COLUMN_NAME_MOVIE_POSTER_PATH, movieEntry.getPosterPath());
            values.put(FavoritesContract.Favorites.COLUMN_NAME_MOVIE_BACKDROP_PATH, movieEntry.getPosterPath());
            values.put(FavoritesContract.Favorites.COLUMN_NAME_MOVIE_VOTE_AVERAGE, movieEntry.getVoteAverage());
            db.insert(FavoritesContract.Favorites.TABLE_NAME, null, values);
        }
    }

    public void removeFavorite(int id) {
        if(db != null) {
            String selection = FavoritesContract.Favorites.COLUMN_NAME_MOVIE_ID + "=?";
            String[] selectionArgs = {String.valueOf(id)};
            db.delete(FavoritesContract.Favorites.TABLE_NAME, selection, selectionArgs);
        }
    }

    public boolean isFavorite(int id) {
        String selection = FavoritesContract.Favorites.COLUMN_NAME_MOVIE_ID + "=?";
        String[] selectionArgs = {String.valueOf(id)};
        if (db != null) {
            Cursor c = db.query(FavoritesContract.Favorites.TABLE_NAME, null, selection, selectionArgs, null, null, null);
            if(c.moveToFirst()) {
                return true;
            }
        }
        return false;
    }

    public List<MovieEntry> getFavorites() {
        List<MovieEntry> movieEntries = new ArrayList<>();

        String sortOrder = FavoritesContract.Favorites._ID + " DESC";

        if (db != null) {
            Cursor c = db.query(FavoritesContract.Favorites.TABLE_NAME, null, null, null, null, null, sortOrder);
            if(c.moveToFirst()) {
                do {
                    MovieEntry entry = new MovieEntry();

                    entry.setId(c.getInt(c.getColumnIndex(FavoritesContract.Favorites.COLUMN_NAME_MOVIE_ID)));
                    entry.setTitle(c.getString(c.getColumnIndex(FavoritesContract.Favorites.COLUMN_NAME_MOVIE_TITLE)));
                    entry.setOverview(c.getString(c.getColumnIndex(FavoritesContract.Favorites.COLUMN_NAME_MOVIE_OVERVIEW)));
                    entry.setReleaseDate(c.getString(c.getColumnIndex(FavoritesContract.Favorites.COLUMN_NAME_MOVIE_RELEASE_DATE)));
                    entry.setPosterPath(c.getString(c.getColumnIndex(FavoritesContract.Favorites.COLUMN_NAME_MOVIE_POSTER_PATH)));
                    entry.setBackdropPath(c.getString(c.getColumnIndex(FavoritesContract.Favorites.COLUMN_NAME_MOVIE_BACKDROP_PATH)));
                    entry.setVoteAverage(c.getDouble(c.getColumnIndex(FavoritesContract.Favorites.COLUMN_NAME_MOVIE_VOTE_AVERAGE)));

                    movieEntries.add(entry);
                } while (c.moveToNext());
            }
        }

        return movieEntries;
    }

    private class GetDB extends AsyncTask<Void, Void, Void> {

        @Override
        protected Void doInBackground(Void... params) {
            db = dbHelper.getWritableDatabase();
            return null;
        }
    }
}
