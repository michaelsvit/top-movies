package com.example.michael.topmovies;

/**
 * Created by Michael on 9/2/2015.
 */
public class Trailer {
    private String key;
    private String name;

    public Trailer(String key, String name) {
        this.key = key;
        this.name = name;
    }

    public String getKey() {
        return key;
    }

    public String getName() {
        return name;
    }
}
