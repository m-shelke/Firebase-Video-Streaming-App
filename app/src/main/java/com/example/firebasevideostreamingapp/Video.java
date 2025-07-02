package com.example.firebasevideostreamingapp;

public class Video {

    //Variable as per Firebase Realtime Database, and must be same as Firebase Database
    private String name,videouri,search,userName,description;

    //empty constructor while working with Firebase Realtime Database
    public Video() {
    }

    //parameterize constructor
    public Video(String name, String videouri, String search, String userName, String description) {
        this.name = name;
        this.videouri = videouri;
        this.search = search;
        this.userName = userName;
        this.description = description;
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

    public String getUserName() {
        return userName;
    }

    public void setUserName(String userName) {
        this.userName = userName;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }
}
