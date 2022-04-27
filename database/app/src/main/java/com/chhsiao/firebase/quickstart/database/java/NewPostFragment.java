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

import androidx.activity.OnBackPressedCallback;
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
import com.leinardi.android.speeddial.SpeedDialActionItem;
import com.leinardi.android.speeddial.SpeedDialView;

import java.util.HashMap;
import java.util.Map;

import static android.app.Activity.RESULT_OK;

public class NewPostFragment extends BaseFragment {
    private static final String TAG = "NewPostFragment";
    private static final String REQUIRED = "Required";
    private static final int PICK_IMAGE_REQUEST = 1;

    private DatabaseReference mDatabase;
    private StorageReference mStorageRef;
    private String proLocation;
    private FragmentNewPostBinding binding;
    private StorageTask mUploadTask;
    private Uri mImageUri;
    ScanOptions options = new ScanOptions();

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        // This callback will only be called when MyFragment is at least Started.
        //返回建功能
        OnBackPressedCallback callback = new OnBackPressedCallback(true /* enabled by default */) {
            @Override
            public void handleOnBackPressed() {
                // Handle the back button event
                Bundle args = new Bundle();
                args.putString("location", proLocation);
                NavHostFragment.findNavController(NewPostFragment.this)
                        .navigate(R.id.action_NewPostFragment_to_MainFragment,args);
            }
        };
        requireActivity().getOnBackPressedDispatcher().addCallback(this, callback);

        // The callback can be enabled or disabled here or in handleOnBackPressed()
    }

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        binding = FragmentNewPostBinding.inflate(inflater, container, false);
//        binding.fabSubmitPost.show();
//        binding.fabback.show();
        return binding.getRoot();

    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        proLocation = MainFragment.proLocation;
        mDatabase = FirebaseDatabase.getInstance().getReference();
        mStorageRef = FirebaseStorage.getInstance().getReference();
        options.setDesiredBarcodeFormats(ScanOptions.ALL_CODE_TYPES);
        options.setCaptureActivity(CaptureAct.class);
//        options.setPrompt("Scan a barcode");
        options.setCameraId(0);  // Use a specific camera of the device
        options.setOrientationLocked(false);
        options.setBeepEnabled(false);
        options.setBarcodeImageEnabled(false);
        SpeedDialActionItem itemBack = new SpeedDialActionItem.Builder(
                R.id.fab_back, R.drawable.ic_baseline_arrow_back_24)
                .setLabel("返回")
                .setLabelClickable(false)
                .setTheme(R.style.AppTheme_Cyan)
                .create();
        SpeedDialActionItem itemBarcodeScan = new SpeedDialActionItem.Builder(
                R.id.fab_scan, R.drawable.ic_baseline_qr_code_scanner_24)
                .setLabel("掃描盤點碼")
                .setLabelClickable(false)
                .setTheme(R.style.AppTheme_Cyan)
                .create();
        SpeedDialActionItem itemAddImage = new SpeedDialActionItem.Builder(
                R.id.fab_addImage, R.drawable.ic_baseline_add_a_photo_24)
                .setLabel("新增盤點照片")
                .setLabelClickable(false)
                .setTheme(R.style.AppTheme_Cyan)
                .create();
        SpeedDialActionItem itemSubmitPost = new SpeedDialActionItem.Builder(
                R.id.fab_submit, R.drawable.ic_navigation_check_24)
                .setLabel("上傳此盤點")
                .setLabelClickable(false)
                .setTheme(R.style.AppTheme_Cyan)
                .create();

        binding.speedDial.addActionItem(itemSubmitPost);
        binding.speedDial.addActionItem(itemAddImage);
        binding.speedDial.addActionItem(itemBarcodeScan);
        binding.speedDial.addActionItem(itemBack);
        binding.speedDial.setOnActionSelectedListener(new SpeedDialView.OnActionSelectedListener() {
            @Override
            public boolean onActionSelected(SpeedDialActionItem actionItem) {
                switch (actionItem.getId()){
                    case R.id.fab_back:
                        Bundle args = new Bundle();
                        args.putString("location", proLocation);
                        NavHostFragment.findNavController(NewPostFragment.this)
                                .navigate(R.id.action_NewPostFragment_to_MainFragment,args);
                        break;
                    case R.id.fab_scan:
                        barcodeLauncher.launch(options);
                        break;
                    case R.id.fab_addImage:
                        openFileChooser();
                        break;
                    case R.id.fab_submit:
                        submitPost();
                        break;
                }
                return false;
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


//        final String count = binding.fieldCount.getText().toString();
//        final String format = binding.fieldFormat.getText().toString();

//        final String snumber = binding.fieldSNumber.getText().toString();
//        final String unit = binding.fieldUnit.getText().toString();
//        final String name = binding.fieldName.getText().toString();
        final String name = binding.textInputLayoutName.getEditText().getText().toString();
        final String barcode = binding.textInputLayoutBarcode.getEditText().getText().toString();
        final String number = binding.textInputLayoutNumber.getEditText().getText().toString();
        final String location = binding.textInputLayoutLocation.getEditText().getText().toString();
        final String remarks = binding.textInputLayoutRemarks.getEditText().getText().toString();
//        final String barcode = binding.fieldBarcode.getText().toString();
//        final String number = binding.fieldNumber.getText().toString();
//        final String location = binding.fieldLocation.getText().toString();
//        final String remarks = binding.fieldRemarks.getText().toString();

        // Title is required
        if (TextUtils.isEmpty(name)) {
//            binding.fieldName.setError(REQUIRED);
            binding.textInputLayoutName.setError(REQUIRED);
            return;
        }
        //Title is required
        if (TextUtils.isEmpty(number)) {
//            binding.fieldNumber.setError(REQUIRED);
            binding.textInputLayoutNumber.setError(REQUIRED);
            return;
        }
        // Title is required
        if (TextUtils.isEmpty(location)) {
//            binding.fieldLocation.setError(REQUIRED);
            binding.textInputLayoutLocation.setError(REQUIRED);
            return;
        }

//        // Title is required
//        if (TextUtils.isEmpty(snumber)) {
//            binding.fieldSNumber.setError(REQUIRED);
//            return;
//        }



//        // Title is required
//        if (TextUtils.isEmpty(format)) {
//            binding.fieldFormat.setError(REQUIRED);
//            return;
//        }

//        // Title is required
//        if (TextUtils.isEmpty(unit)) {
//            binding.fieldUnit.setError(REQUIRED);
//            return;
//        }


//        // Title is required
//        if (TextUtils.isEmpty(count)) {
//            binding.fieldCount.setError(REQUIRED);
//            return;
//        }

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
                            writeNewPost(userId, user.username, name, barcode, number, location, remarks, uploadFileName);
//                            writeNewPost(userId, user.username, location, number, count, format, remarks, barcode, snumber, unit, name, uploadFileName);
                        }

                        setEditingEnabled(true);
                        Bundle args = new Bundle();
                        args.putString("location", proLocation);
                        NavHostFragment.findNavController(NewPostFragment.this)
                                .navigate(R.id.action_NewPostFragment_to_MainFragment,args);
                    }

                    @Override
                    public void onCancelled(@NonNull DatabaseError databaseError) {
                        Log.w(TAG, "getUser:onCancelled", databaseError.toException());
                        setEditingEnabled(true);
                    }
                });
    }

    private void setEditingEnabled(boolean enabled) {

        binding.textInputLayoutName.setEnabled(enabled);
        binding.textInputLayoutBarcode.setEnabled(enabled);
        binding.textInputLayoutNumber.setEnabled(enabled);
        binding.textInputLayoutLocation.setEnabled(enabled);
        binding.textInputLayoutRemarks.setEnabled(enabled);

//        binding.fieldLocation.setEnabled(enabled);

//        binding.fieldSNumber.setEnabled(enabled);
//        binding.fieldName.setEnabled(enabled);
//        binding.fieldFormat.setEnabled(enabled);
//        binding.fieldUnit.setEnabled(enabled);
//        binding.fieldNumber.setEnabled(enabled);
//        binding.fieldCount.setEnabled(enabled);
//        binding.fieldRemarks.setEnabled(enabled);
//        binding.fieldBarcode.setEnabled(enabled);
//        if (enabled) {
//            binding.fabSubmitPost.show();
//        } else {
//            binding.fabSubmitPost.hide();
//        }
    }
    private void writeNewPost(String userId, String username, String name, String barcode, String number, String location, String remarks, String uploadFileName) {
        // Create new post at /user-posts/$userid/$postid and at
        // /posts/$postid simultaneously
        String key = mDatabase.child(proLocation).child("posts").push().getKey();
        Post post = new Post(userId, username, name, barcode,number,location,remarks, uploadFileName);
        Map<String, Object> postValues = post.toMap();
        Map<String, Object> childUpdates = new HashMap<>();
        childUpdates.put("/"+proLocation+"/posts/" + key, postValues);
        childUpdates.put("/"+proLocation+"/user-posts/" + userId + "/" + key, postValues);
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
//                            binding.fieldBarcode.setText(result.getContents());
                            binding.textInputLayoutBarcode.getEditText().setText(result.getContents());
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
