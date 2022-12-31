package com.chhsiao.firebase.quickstart.database.java.models;
import com.google.firebase.database.IgnoreExtraProperties;

@IgnoreExtraProperties
public class Task {
    public String id;
    public String name;
    public String time;

    public Task() {
        // Default constructor required for calls to DataSnapshot.getValue(User.class)
    }

    public Task(String id,String name, String time) {
        this.id = id;
        this.name = name;
        this.time = time;
    }
}
