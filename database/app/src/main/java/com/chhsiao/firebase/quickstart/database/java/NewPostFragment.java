package com.chhsiao.firebase.quickstart.database.java;

import android.app.AlertDialog;
import android.content.ContentResolver;
import android.content.DialogInterface;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.text.TextUtils;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.webkit.MimeTypeMap;
import android.widget.Toast;

import androidx.activity.result.ActivityResultLauncher;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.navigation.fragment.NavHostFragment;

import com.chhsiao.firebase.quickstart.database.java.models.Post;
import com.chhsiao.firebase.quickstart.database.java.models.User;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.google.firebase.quickstart.database.R;
import com.google.firebase.quickstart.database.databinding.FragmentNewPostBinding;
import com.chhsiao.firebase.quickstart.database.java.capture.CaptureAct;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;
import com.google.firebase.storage.StorageTask;
import com.google.firebase.storage.UploadTask;
import com.journeyapps.barcodescanner.ScanContract;
import com.journeyapps.barcodescanner.ScanOptions;

import java.util.HashMap;
import java.util.Map;

import static android.app.Activity.RESULT_OK;

public class NewPostFragment extends BaseFragment {
    private static final String TAG = "NewPostFragment";
    private static final String REQUIRED = "Required";
    private static final int PICK_IMAGE_REQUEST = 1;

    private DatabaseReference mDatabase;
    private StorageReference mStorageRef;

    private FragmentNewPostBinding binding;
    private StorageTask mUploadTask;
    private Uri mImageUri;
    ScanOptions options = new ScanOptions();
    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        binding = FragmentNewPostBinding.inflate(inflater, container, false);
        binding.fabSubmitPost.show();
        binding.fabback.show();
        return binding.getRoot();
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        mDatabase = FirebaseDatabase.getInstance().getReference();
        mStorageRef = FirebaseStorage.getInstance().getReference();
        options.setDesiredBarcodeFormats(ScanOptions.ALL_CODE_TYPES);
        options.setCaptureActivity(CaptureAct.class);
//        options.setPrompt("Scan a barcode");
        options.setCameraId(0);  // Use a specific camera of the device
        options.setOrientationLocked(false);
        options.setBeepEnabled(false);
        options.setBarcodeImageEnabled(false);
        binding.fabBarcodeScan.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                barcodeLauncher.launch(options);
            }
        });
        binding.fabAddImage.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                openFileChooser();
            }
        });
        binding.fabSubmitPost.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
//                binding.fabSubmitPost.hide();
                submitPost();
            }
        });
        binding.fabback.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                binding.fabback.hide();
                NavHostFragment.findNavController(NewPostFragment.this)
                        .navigate(R.id.action_NewPostFragment_to_MainFragment);
            }
        });
    }

    private void openFileChooser(){
        Intent intent = new Intent();
        intent.setType("image/*");
        intent.setAction(Intent.ACTION_GET_CONTENT);
        startActivityForResult(intent, PICK_IMAGE_REQUEST);

    }

    private String getFileExtension(Uri uri) {
        ContentResolver cR = getActivity().getContentResolver();
        MimeTypeMap mime = MimeTypeMap.getSingleton();
        return mime.getExtensionFromMimeType(cR.getType(uri));
    }
    private String uploadFile() {
        final String fileName;
        if (mImageUri != null) {
            fileName = System.currentTimeMillis() + "." + getFileExtension(mImageUri);
            StorageReference fileReference = mStorageRef.child(fileName);
            mUploadTask = fileReference.putFile(mImageUri);

            // Register observers to listen for when the download is done or if it fails
            mUploadTask.addOnFailureListener(new OnFailureListener() {
                @Override
                public void onFailure(@NonNull Exception e) {
                    Toast.makeText(getContext(), "image upload failure", Toast.LENGTH_SHORT).show();
                }
            }).addOnSuccessListener(new OnSuccessListener<UploadTask.TaskSnapshot>() {
                @Override
                public void onSuccess(UploadTask.TaskSnapshot taskSnapshot) {
                    Log.e(TAG, "image upload success: ");
                }
            });
        } else {
            Toast.makeText(getContext(), "No file selected", Toast.LENGTH_SHORT).show();
            fileName = null;
        }
        return fileName;
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == PICK_IMAGE_REQUEST && resultCode == RESULT_OK
                && data != null && data.getData() != null) {
            mImageUri = data.getData();
            binding.imageView.setImageURI(mImageUri);
        }
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
        final String barcode = binding.fieldBarcode.getText().toString();


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

        String FileName = "null";
        if (mUploadTask != null && mUploadTask.isInProgress()) {
            Toast.makeText(getContext(), "Upload in progress", Toast.LENGTH_SHORT).show();
        } else {
            FileName = uploadFile();

        }

        final String uploadFileName = FileName;
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
                            writeNewPost(userId, user.username, location, number, count, format, remarks, barcode, snumber, unit, name, uploadFileName);
                        }

                        setEditingEnabled(true);
                        NavHostFragment.findNavController(NewPostFragment.this)
                                .navigate(R.id.action_NewPostFragment_to_MainFragment);
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
        binding.fieldBarcode.setEnabled(enabled);
        if (enabled) {
            binding.fabSubmitPost.show();
        } else {
            binding.fabSubmitPost.hide();
        }
    }

    private void writeNewPost(String userId, String username, String location, String number, String count, String format,
                              String remarks, String barcode, String snumber, String unit, String name, String uploadFileName) {
        // Create new post at /user-posts/$userid/$postid and at
        // /posts/$postid simultaneously
        String key = mDatabase.child("posts").push().getKey();
        Post post = new Post(userId, username, location, snumber,name,format,unit,number,count,remarks, barcode, uploadFileName);
        Map<String, Object> postValues = post.toMap();

        Map<String, Object> childUpdates = new HashMap<>();
        childUpdates.put("/posts/" + key, postValues);
        childUpdates.put("/user-posts/" + userId + "/" + key, postValues);

        mDatabase.updateChildren(childUpdates);
    }
    private final ActivityResultLauncher<ScanOptions> barcodeLauncher = registerForActivityResult(new ScanContract(),
            result -> {
                if(result.getContents()!=null){
                    AlertDialog.Builder builder = new AlertDialog.Builder(getContext());
                    builder.setMessage(result.getContents());
                    builder.setTitle("Scanning Result");
                    builder.setPositiveButton("Scan Again", new DialogInterface.OnClickListener() {
                        @Override
                        public void onClick(DialogInterface dialog, int which) {
                            barcodeLauncher.launch(options);
                        }
                    }).setNegativeButton("finish", new DialogInterface.OnClickListener() {
                        @Override
                        public void onClick(DialogInterface dialog, int which) {
                            binding.fieldBarcode.setText(result.getContents());
                        }
                    });
                    AlertDialog dialog = builder.create();
                    dialog.show();
                }
                else {
                    Toast.makeText(getActivity(),"No Results",Toast.LENGTH_SHORT).show();
                }
            });





}
