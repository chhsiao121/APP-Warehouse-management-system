package com.chhsiao.firebase.quickstart.database.java.models;

import com.google.firebase.database.Exclude;
import com.google.firebase.database.IgnoreExtraProperties;

import java.util.HashMap;
import java.util.Map;
@IgnoreExtraProperties
public class T22 {
    public String uid;
    public String author;
    public String name;
    public String number;
    public String time;
    public String remarks;
    public String state;
    public String barcode;
    public String modeId;
    public String taskId;
    public String locationId;
    public String uploadFileName1;
    public String uploadFileName2;
    public String uploadFileName3;

    public T22(){
    }
    public T22(String uid, String author,String name,String number, String time,String remarks, String state,String barcode,
               String modeId,String taskId,String locationId,String uploadFileName1,String uploadFileName2,String uploadFileName3) {
        this.uid = uid;
        this.author = author;
        this.name = name;
        this.number = number;
        this.time = time;
        this.remarks = remarks;
        this.state = state;
        this.barcode = barcode;
        this.modeId = modeId;
        this.taskId = taskId;
        this.locationId = locationId;
        this.uploadFileName1 = uploadFileName1;
        this.uploadFileName2 = uploadFileName2;
        this.uploadFileName3 = uploadFileName3;
    }
    @Exclude
    public Map<String, Object> toMap() {
        HashMap<String, Object> result = new HashMap<>();
        result.put("uid", uid);
        result.put("author", author);
        result.put("name", name);
        result.put("number", number);
        result.put("time", time);
        result.put("remarks", remarks);
        result.put("state", state);
        result.put("barcode", barcode);
        result.put("modeId", modeId);
        result.put("taskId", taskId);
        result.put("locationId", locationId);
        result.put("uploadFileName1", uploadFileName1);
        result.put("uploadFileName2", uploadFileName2);
        result.put("uploadFileName3", uploadFileName3);
        return result;
    }
}
