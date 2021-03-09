package com.google.firebase.quickstart.database.java.models;

import com.google.firebase.database.Exclude;
import com.google.firebase.database.IgnoreExtraProperties;

import java.util.HashMap;
import java.util.Map;

@IgnoreExtraProperties
public class Post2 {

    public String uid;
    public String author;
    public String location;
    public String snumber;
    public String name;
    public String format;
    public String unit;
    public String number;
    public String count;
    public String remarks;

    public int starCount = 0;
    public Map<String, Boolean> stars = new HashMap<>();

    public Post2() {
        // Default constructor required for calls to DataSnapshot.getValue(Post.class)
    }

    public Post2(String uid, String author,String location, String snumber,String name,
                 String format,String unit,String number,String count,String remarks) {
        this.uid = uid;
        this.author = author;
        this.location = location;
        this.snumber = snumber;
        this.name = name;
        this.format = format;
        this.unit = unit;
        this.number = number;
        this.count = count;
        this.remarks = remarks;
    }

    @Exclude
    public Map<String, Object> toMap() {
        HashMap<String, Object> result = new HashMap<>();
        result.put("uid", uid);
        result.put("author", author);
        result.put("location", location);
        result.put("snumber", snumber);
        result.put("name", name);
        result.put("format", format);
        result.put("unit", unit);
        result.put("number", number);
        result.put("count", count);
        result.put("remarks", remarks);
        result.put("starCount", starCount);
        result.put("stars", stars);

        return result;
    }

}
