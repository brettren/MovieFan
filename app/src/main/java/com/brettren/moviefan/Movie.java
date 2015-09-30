package com.brettren.moviefan;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by Brett on 15/9/26.
 */
public class Movie {
    public String id;
    public String title;
    public String release_date;
    public String backdrop_path;
    public String poster_path;

    public String vote_average;
    public String vote_count;
    public String overview;
    public String tagline;
    public String runtime;
    public String revenue;
    public List<Video> videos;

    public Movie(){
        videos = new ArrayList<>();
    }

    public Movie(String id, String title, String release_date, String backdrop_path, String poster_path) {
        this.id = id;
        this.title = title;
        this.release_date = release_date;
        this.backdrop_path = backdrop_path;
        this.poster_path = poster_path;
    }

    public Movie(String id, String title, String release_date, String backdrop_path, String poster_path,
                 String vote_average, String vote_count, String overview, String tagline, String runtime, String revenue, List<Video> videos) {
        this.id = id;
        this.title = title;
        this.release_date = release_date;
        this.backdrop_path = backdrop_path;
        this.poster_path = poster_path;
        this.vote_average = vote_average;

        this.vote_count = vote_count;
        this.overview = overview;
        this.tagline = tagline;
        this.runtime = runtime;
        this.revenue = revenue;

        this.videos = videos;
    }
}
