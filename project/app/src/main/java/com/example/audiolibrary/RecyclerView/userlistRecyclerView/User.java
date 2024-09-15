package com.example.audiolibrary.RecyclerView.userlistRecyclerView;

public class User {


    public User(String uid_user, String name_user, String colvoaudio_user, String colvo_friends, String register_date) {
        this.uid_user = uid_user;
        this.name_user = name_user;
        this.colvoaudio_user = colvoaudio_user;
        this.colvo_friends = colvo_friends;
        this.register_date = register_date;
    }

    public String getUid_user() {
        return uid_user;
    }

    public void setUid_user(String uid_user) {
        this.uid_user = uid_user;
    }

    public String getName_user() {
        return name_user;
    }

    public void setName_user(String name_user) {
        this.name_user = name_user;
    }

    public String getColvoaudio_user() {
        return colvoaudio_user;
    }

    public void setColvoaudio_user(String colvoaudio_user) {
        this.colvoaudio_user = colvoaudio_user;
    }

    public String getColvo_friends() {
        return colvo_friends;
    }

    public void setColvo_friends(String colvo_friends) {
        this.colvo_friends = colvo_friends;
    }

    public String getRegister_date() {
        return register_date;
    }

    public void setRegister_date(String register_date) {
        this.register_date = register_date;
    }

    String uid_user, name_user, colvoaudio_user, colvo_friends, register_date;

}
