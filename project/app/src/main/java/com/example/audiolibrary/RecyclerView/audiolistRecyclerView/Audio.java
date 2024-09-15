package com.example.audiolibrary.RecyclerView.audiolistRecyclerView;

import java.io.Serializable;

public class Audio implements Serializable {

    public Audio(String id_audio, String id_user, String title_audio, String performer_audio, String url_audio, int mood_happy, int mood_normal, int mood_sad, int mood_angry, long timestamp) {
        this.id_audio = id_audio;
        this.id_user = id_user;
        this.title_audio = title_audio;
        this.performer_audio = performer_audio;
        this.url_audio = url_audio;
        this.mood_happy = mood_happy;
        this.mood_normal = mood_normal;
        this.mood_sad = mood_sad;
        this.mood_angry = mood_angry;
        this.timestamp = timestamp;
    }

    public String getId_audio() {
        return id_audio;
    }

    public void setId_audio(String id_audio) {
        this.id_audio = id_audio;
    }

    public String getId_user() {
        return id_user;
    }

    public void setId_user(String id_user) {
        this.id_user = id_user;
    }

    public String getTitle_audio() {
        return title_audio;
    }

    public void setTitle_audio(String title_audio) {
        this.title_audio = title_audio;
    }

    public String getPerformer_audio() {
        return performer_audio;
    }

    public void setPerformer_audio(String performer_audio) {
        this.performer_audio = performer_audio;
    }

    public String getUrl_audio() {
        return url_audio;
    }

    public void setUrl_audio(String url_audio) {
        this.url_audio = url_audio;
    }

    public int getMood_happy() {
        return mood_happy;
    }

    public void setMood_happy(int mood_happy) {
        this.mood_happy = mood_happy;
    }

    public int getMood_normal() {
        return mood_normal;
    }

    public void setMood_normal(int mood_normal) {
        this.mood_normal = mood_normal;
    }

    public int getMood_sad() {
        return mood_sad;
    }

    public void setMood_sad(int mood_sad) {
        this.mood_sad = mood_sad;
    }

    public int getMood_angry() {
        return mood_angry;
    }

    public void setMood_angry(int mood_angry) {
        this.mood_angry = mood_angry;
    }

    public long getTimestamp() {
        return timestamp;
    }

    public void setTimestamp(long timestamp) {
        this.timestamp = timestamp;
    }

    String id_audio, id_user, title_audio, performer_audio, url_audio;
    int mood_happy, mood_normal, mood_sad, mood_angry;
    long timestamp;

}
