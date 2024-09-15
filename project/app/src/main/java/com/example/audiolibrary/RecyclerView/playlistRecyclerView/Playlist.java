package com.example.audiolibrary.RecyclerView.playlistRecyclerView;

public class Playlist {

    public Playlist(String uid_playlist, String title_playlist, String colvoaudio_playlist) {
        this.uid_playlist = uid_playlist;
        this.title_playlist = title_playlist;
        this.colvoaudio_playlist = colvoaudio_playlist;
    }

    public String getUid_playlist() {
        return uid_playlist;
    }

    public void setUid_playlist(String uid_playlist) {
        this.uid_playlist = uid_playlist;
    }

    public String getTitle_playlist() {
        return title_playlist;
    }

    public void setTitle_playlist(String title_playlist) {
        this.title_playlist = title_playlist;
    }

    public String getColvoaudio_playlist() {
        return colvoaudio_playlist;
    }

    public void setColvoaudio_playlist(String colvoaudio_playlist) {
        this.colvoaudio_playlist = colvoaudio_playlist;
    }

    String uid_playlist ,title_playlist, colvoaudio_playlist;

}
