package com.example.audiolibrary.RecyclerView.friendlistRecyclerView;

public class Friend {

    public Friend(String uid_user, String name_user, String colvoaudio_user, String colvofriend_user, String reg_date_user) {
        this.uid_user = uid_user;
        this.name_user = name_user;
        this.colvoaudio_user = colvoaudio_user;
        this.colvofriend_user = colvofriend_user;
        this.reg_date_user = reg_date_user;
        this.profile_image = profile_image;
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

    public String getColvofriend_user() {
        return colvofriend_user;
    }

    public void setColvofriend_user(String colvofriend_user) {
        this.colvofriend_user = colvofriend_user;
    }

    public String getReg_date_user() {
        return reg_date_user;
    }

    public void setReg_date_user(String reg_date_user) {
        this.reg_date_user = reg_date_user;
    }

    public String getProfile_image() {
        return profile_image;
    }

    public void setProfile_image(String profile_image) {
        this.profile_image = profile_image;
    }

    String uid_user, name_user, colvoaudio_user, colvofriend_user, reg_date_user, profile_image;

}
