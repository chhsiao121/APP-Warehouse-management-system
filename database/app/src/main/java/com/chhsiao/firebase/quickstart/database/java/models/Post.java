package com.chhsiao.firebase.quickstart.database.java.models;

import com.google.firebase.database.Exclude;
import com.google.firebase.database.IgnoreExtraProperties;

import java.util.HashMap;
import java.util.Map;

@IgnoreExtraProperties
public class Post {

    public String uid;
    public String author;
    public String name;
    public String barcode;
    public String number;
    public String location;
    public String remarks;

    public String uploadFileName;
    public int starCount = 0;
    public Map<String, Boolean> stars = new HashMap<>();

    public Post() {
        // Default constructor required for calls to DataSnapshot.getValue(Post.class)
    }

    public Post(String uid, String author,String name,String barcode,
                String number,String location, String remarks, String uploadFileName) {
        this.uid = uid;
        this.author = author;
        this.name = name;
        this.barcode = barcode;
        this.number = number;
        this.location = location;
        this.remarks = remarks;
        this.uploadFileName = uploadFileName;
    }

    @Exclude
    public Map<String, Object> toMap() {
        HashMap<String, Object> result = new HashMap<>();
        result.put("uid", uid);
        result.put("author", author);
        result.put("name", name);
        result.put("barcode", barcode);
        result.put("number", number);
        result.put("location", location);
        result.put("remarks", remarks);
        result.put("uploadFileName", uploadFileName);
        result.put("starCount", starCount);
        result.put("stars", stars);

        return result;
    }

}
