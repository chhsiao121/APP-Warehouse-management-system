package com.google.firebase.quickstart.database.java;

import android.os.Bundle;
import android.text.TextUtils;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.navigation.fragment.NavHostFragment;

import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.google.firebase.quickstart.database.R;
import com.google.firebase.quickstart.database.databinding.FragmentNewItemBinding;
import com.google.firebase.quickstart.database.java.models.Post;
import com.google.firebase.quickstart.database.java.models.Post2;
import com.google.firebase.quickstart.database.java.models.User;

import java.util.HashMap;
import java.util.Map;

public class NewItemFragment extends BaseFragment {
    private static final String TAG = "NewPostFragment";
    private static final String REQUIRED = "Required";

    private DatabaseReference mDatabase;

    private FragmentNewItemBinding binding;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        binding = FragmentNewItemBinding.inflate(inflater, container, false);
        return binding.getRoot();
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        mDatabase = FirebaseDatabase.getInstance().getReference();

        binding.fabSubmitPost.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                submitPost();
            }
        });
    }

    private void submitPost() {
        final String location = binding.fieldLocation.getText().toString();
        final String number = binding.fieldNumber.getText().toString();
        final String count = binding.fieldCount.getText().toString();
        final String format = binding.fieldFormat.getText().toString();
        final String remarks = binding.fieldRemarks.getText().toString();
        final String snumber = binding.fieldSNumber.getText().toString();
        final String unit = binding.fieldUnit.getText().toString();
        final String name = binding.fieldName.getText().toString();


        // Title is required
        if (TextUtils.isEmpty(location)) {
            binding.fieldLocation.setError(REQUIRED);
            return;
        }

        // Title is required
        if (TextUtils.isEmpty(snumber)) {
            binding.fieldSNumber.setError(REQUIRED);
            return;
        }

        // Title is required
        if (TextUtils.isEmpty(name)) {
            binding.fieldName.setError(REQUIRED);
            return;
        }

        // Title is required
        if (TextUtils.isEmpty(format)) {
            binding.fieldFormat.setError(REQUIRED);
            return;
        }

        // Title is required
        if (TextUtils.isEmpty(unit)) {
            binding.fieldUnit.setError(REQUIRED);
            return;
        }

        // Title is required
        if (TextUtils.isEmpty(number)) {
            binding.fieldNumber.setError(REQUIRED);
            return;
        }
        // Title is required
        if (TextUtils.isEmpty(count)) {
            binding.fieldCount.setError(REQUIRED);
            return;
        }

        // Title is required
//        if (TextUtils.isEmpty(remarks)) {
//            binding.fieldRemarks.setError(REQUIRED);
//            return;
//        }

        // Disable button so there are no multi-posts
        setEditingEnabled(false);
        Toast.makeText(getContext(), "Posting...", Toast.LENGTH_SHORT).show();

        final String userId = getUid();
        mDatabase.child("users").child(userId).addListenerForSingleValueEvent(
                new ValueEventListener() {
                    @Override
                    public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                        // Get user value
                        User user = dataSnapshot.getValue(User.class);

                        if (user == null) {
                            // User is null, error out
                            Log.e(TAG, "User " + userId + " is unexpectedly null");
                            Toast.makeText(getContext(),
                                    "Error: could not fetch user.",
                                    Toast.LENGTH_SHORT).show();
                        } else {
                            // Write new post
                            writeNewItem(userId, user.username, location, number, count, format, remarks, snumber, unit, name);
                        }
                        setEditingEnabled(true);
                        NavHostFragment.findNavController(NewItemFragment.this)
                                .navigate(R.id.action_NewItemFragment_to_MainFragment);
                    }

                    @Override
                    public void onCancelled(@NonNull DatabaseError databaseError) {
                        Log.w(TAG, "getUser:onCancelled", databaseError.toException());
                        setEditingEnabled(true);
                    }
                });
    }

    private void setEditingEnabled(boolean enabled) {
        binding.fieldLocation.setEnabled(enabled);
        binding.fieldSNumber.setEnabled(enabled);
        binding.fieldName.setEnabled(enabled);
        binding.fieldFormat.setEnabled(enabled);
        binding.fieldUnit.setEnabled(enabled);
        binding.fieldNumber.setEnabled(enabled);
        binding.fieldCount.setEnabled(enabled);
        binding.fieldRemarks.setEnabled(enabled);
        if (enabled) {
            binding.fabSubmitPost.show();
        } else {
            binding.fabSubmitPost.hide();
        }
    }

    private void writeNewPost(String userId, String username, String title, String body) {
        // Create new post at /user-posts/$userid/$postid and at
        // /posts/$postid simultaneously
        String key = mDatabase.child("posts").push().getKey();
        Post post = new Post(userId, username, title, body);
        Map<String, Object> postValues = post.toMap();

        Map<String, Object> childUpdates = new HashMap<>();
        childUpdates.put("/posts/" + key, postValues);
        childUpdates.put("/user-posts/" + userId + "/" + key, postValues);

        mDatabase.updateChildren(childUpdates);
    }

    private void writeNewItem(String userId, String username, String location, String number, String count, String format,
                              String remarks, String snumber, String unit, String name) {

        // Create new post at /user-posts/$userid/$postid and at
        // /posts/$postid simultaneously
        String key = mDatabase.child("posts").push().getKey();
        Post2 post = new Post2(userId, username, location, snumber,name,format,unit,number,count,remarks);
        Map<String, Object> postValues = post.toMap();

        Map<String, Object> childUpdates = new HashMap<>();
        childUpdates.put("/posts/" + key, postValues);
        childUpdates.put("/user-posts/" + userId + "/" + key, postValues);

        mDatabase.updateChildren(childUpdates);
    }
}
