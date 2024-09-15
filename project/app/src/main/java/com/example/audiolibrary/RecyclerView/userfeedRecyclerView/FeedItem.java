package com.example.audiolibrary.RecyclerView.userfeedRecyclerView;

public class FeedItem {

    public FeedItem(String id_item, String string_item, long timestamp) {
        this.id_item = id_item;
        this.string_item = string_item;
        this.timestamp = timestamp;
    }


    public String getId_item() {
        return id_item;
    }

    public void setId_item(String id_item) {
        this.id_item = id_item;
    }

    public String getString_item() {
        return string_item;
    }

    public void setString_item(String string_item) {
        this.string_item = string_item;
    }

    public long getTimestamp() {
        return timestamp;
    }

    public void setTimestamp(long timestamp) {
        this.timestamp = timestamp;
    }


    String id_item, string_item;
    long timestamp;

}
