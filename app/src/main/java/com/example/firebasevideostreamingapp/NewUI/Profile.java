package com.example.firebasevideostreamingapp.NewUI;

public class Profile {

    String name;
    String email;
    String imageUrl;
    String uid;

    public Profile() {
    }

    public Profile(String name, String email, String imageUrl, String uid) {
        this.name = name;
        this.email = email;
        this.imageUrl = imageUrl;
        this.uid = uid;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getEmail() {
        return email;
    }

    public void setEmail(String email) {
        this.email = email;
    }

    public String getImageUrl() {
        return imageUrl;
    }

    public void setImageUrl(String imageUrl) {
        this.imageUrl = imageUrl;
    }

    public String getUid() {
        return uid;
    }

    public void setUid(String uid) {
        this.uid = uid;
    }
}
