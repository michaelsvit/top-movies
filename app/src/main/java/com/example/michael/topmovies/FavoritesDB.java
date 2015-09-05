package com.example.michael.topmovies;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.os.AsyncTask;
import android.util.Log;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by Michael on 9/4/2015.
 */
public class FavoritesDB {
    private final String LOG_TAG = FavoritesDB.class.getSimpleName();

    private FavoritesDbHelper dbHelper;
    private SQLiteDatabase db;
    private AddItemsToGrid callback;

    public interface AddItemsToGrid {
        void addFavorites(List<MovieEntry> movieEntries);
    }

    public FavoritesDB(Context context, boolean requestFavorites) {
        //Request AddItemsToGrid implementation in context
        try {
            callback = (AddItemsToGrid) context;
        } catch (ClassCastException e) {
            Log.e(LOG_TAG, "Calling activity did not implement AddItemsToGrid interface");
        }

        dbHelper = new FavoritesDbHelper(context);
        if (requestFavorites) {
            new GetDB().execute(true);
        } else {
            new GetDB().execute(false);
        }
    }

    public void setFavorite(MovieEntry movieEntry) {
        if(db != null) {
            ContentValues values = new ContentValues();
            values.put(FavoritesContract.Favorites.COLUMN_NAME_MOVIE_ID, movieEntry.getId());
            values.put(FavoritesContract.Favorites.COLUMN_NAME_MOVIE_TITLE, movieEntry.getTitle());
            values.put(FavoritesContract.Favorites.COLUMN_NAME_MOVIE_OVERVIEW, movieEntry.getOverview());
            values.put(FavoritesContract.Favorites.COLUMN_NAME_MOVIE_RELEASE_DATE, movieEntry.getReleaseDate());
            values.put(FavoritesContract.Favorites.COLUMN_NAME_MOVIE_POSTER_PATH, movieEntry.getPosterPath());
            values.put(FavoritesContract.Favorites.COLUMN_NAME_MOVIE_BACKDROP_PATH, movieEntry.getBackdropPath());
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

    private class GetDB extends AsyncTask<Boolean, Void, Void> {

        boolean requestFavorites;

        @Override
        protected void onPostExecute(Void aVoid) {
            if(requestFavorites) {
                callback.addFavorites(getFavorites());
            }
        }

        @Override
        protected Void doInBackground(Boolean... params) {
            requestFavorites = params[0];
            db = dbHelper.getWritableDatabase();
            return null;
        }
    }
}
