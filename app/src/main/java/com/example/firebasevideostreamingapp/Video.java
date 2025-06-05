package com.example.firebasevideostreamingapp;

public class Video {

    //Variable as per Firebase Realtime Database, and must be same as Firebase Database
    private String name,videouri,search;

    //empty constructor while working with Firebase Realtime Database
    public Video() {
    }

    //parameterize constructor
    public Video(String name, String videouri, String search) {
        this.name = name;
        this.videouri = videouri;
        this.search = search;
    }

    //Getters and Setters
    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getVideouri() {
        return videouri;
    }

    public void setVideouri(String videouri) {
        this.videouri = videouri;
    }

    public String getSearch() {
        return search;
    }

    public void setSearch(String search) {
        this.search = search;
    }
}
