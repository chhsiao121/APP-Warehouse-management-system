package com.chhsiao.firebase.quickstart.database.java;

import android.view.View;
import android.widget.ProgressBar;

import androidx.fragment.app.Fragment;

import com.google.firebase.auth.FirebaseAuth;

public class BaseFragment extends Fragment {
    private ProgressBar mProgressBar;
    private final String taskJson ="inventory_task.json";
    private final String locationJson ="inventory_location.json";
    public void setProgressBar(int resId) {
        mProgressBar = getView().findViewById(resId);
    }

    public void showProgressBar() {
        if (mProgressBar != null) {
            mProgressBar.setVisibility(View.VISIBLE);
        }
    }

    public void hideProgressBar() {
        if (mProgressBar != null) {
            mProgressBar.setVisibility(View.INVISIBLE);
        }
    }

    public String getUid() {
        return FirebaseAuth.getInstance().getCurrentUser().getUid();
    }
    public String getTaskJson(){return taskJson;}
    public String getLocationJson(){return locationJson;}
}
