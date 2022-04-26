package com.chhsiao.firebase.quickstart.database.java;
import static android.app.Activity.RESULT_OK;

import androidx.activity.OnBackPressedCallback;
import androidx.activity.result.ActivityResultLauncher;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.navigation.fragment.NavHostFragment;

import android.app.AlertDialog;
import android.content.ContentResolver;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;

import com.chhsiao.firebase.quickstart.database.java.capture.CaptureAct;
import com.chhsiao.firebase.quickstart.database.java.models.Post;
import com.chhsiao.firebase.quickstart.database.java.models.User;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.google.firebase.quickstart.database.databinding.FragmentUpdatePostBinding;
import androidx.fragment.app.Fragment;

import android.text.TextUtils;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.webkit.MimeTypeMap;
import android.widget.Toast;

import com.google.firebase.quickstart.database.R;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;
import com.google.firebase.storage.StorageTask;
import com.google.firebase.storage.UploadTask;
import com.journeyapps.barcodescanner.ScanContract;
import com.journeyapps.barcodescanner.ScanOptions;
import com.leinardi.android.speeddial.SpeedDialActionItem;
import com.leinardi.android.speeddial.SpeedDialView;
import com.squareup.picasso.Picasso;

import java.util.HashMap;
import java.util.Map;


public class UpdatePostFragment extends BaseFragment {
    private FragmentUpdatePostBinding binding;
    private StorageTask mUploadTask;
    private DatabaseReference mDatabase;
    private StorageReference mStorageRef;
    private Uri mImageUri;
    private Context context;
    private String mPostKey;
    private String location,name,number,remarks,barcode,uploadFileName,loadFileName;
    private final String userId = getUid();
    public static final String EXTRA_POST_KEY = "post_key";
    private static final String TAG = "UpdatePostFragment";
    private static final String REQUIRED = "Required";
    private static final int PICK_IMAGE_REQUEST = 1;
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
                NavHostFragment.findNavController(UpdatePostFragment.this)
                        .navigate(R.id.action_UpdatePostFragment_to_MainFragment);
            }
        };
        requireActivity().getOnBackPressedDispatcher().addCallback(this, callback);

        // The callback can be enabled or disabled here or in handleOnBackPressed()
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        binding = FragmentUpdatePostBinding.inflate(inflater, container, false);
        // Get post key from arguments
        mPostKey = requireArguments().getString(EXTRA_POST_KEY);
        if (mPostKey == null) {
            throw new IllegalArgumentException("Must pass EXTRA_POST_KEY");
        }
        context = getContext();
        location = requireArguments().getString("location");
        name = requireArguments().getString("name");
        number = requireArguments().getString("number");
        remarks = requireArguments().getString("remarks");
        barcode = requireArguments().getString("barcode");
        loadFileName = requireArguments().getString("uploadFileName"); //資料庫中影像名稱
        uploadFileName = loadFileName;

//        binding.fabSubmitPost.show();
//        binding.fabback.show();
        binding.textInputLayoutLocation.getEditText().setText(location);
        binding.textInputLayoutName.getEditText().setText(name);
        binding.textInputLayoutNumber.getEditText().setText(number);
        binding.textInputLayoutRemarks.getEditText().setText(remarks);
        binding.textInputLayoutBarcode.getEditText().setText(barcode);
//        binding.fieldLocation.setText(location);
//        binding.fieldName.setText(name);
//        binding.fieldNumber.setText(number);
//        binding.fieldRemarks.setText(remarks);
//        binding.fieldBarcode.setText(barcode);
        mDatabase = FirebaseDatabase.getInstance().getReference();
        mStorageRef = FirebaseStorage.getInstance().getReference();
        if (loadFileName!=null) {
            mStorageRef.child(loadFileName).getDownloadUrl().addOnSuccessListener(new OnSuccessListener<Uri>() {
                @Override
                public void onSuccess(Uri uri) {
                    // Got the download URL for 'users/me/profile.png'
                    Picasso.get().load(uri).into(binding.imageView);
                }
            }).addOnFailureListener(new OnFailureListener() {
                @Override
                public void onFailure(@NonNull Exception exception) {
                    // Handle any errors
                    binding.imageView.setImageResource(R.drawable.images);
                    Toast.makeText(context, "Failed to load image from Firebase.", Toast.LENGTH_SHORT).show();
                }
            });
        }
        return binding.getRoot();
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

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
                        NavHostFragment.findNavController(UpdatePostFragment.this)
                                .navigate(R.id.action_UpdatePostFragment_to_MainFragment);
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

    @Override
    public void onStart() {
        super.onStart();

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
    private void deleteFile(String file){
        StorageReference desertRef = mStorageRef.child(file);

        // Delete the file
        desertRef.delete().addOnSuccessListener(new OnSuccessListener<Void>() {
            @Override
            public void onSuccess(Void aVoid) {
                // File deleted successfully
                Toast.makeText(context, "已移除前次盤點圖片", Toast.LENGTH_SHORT).show();
            }
        }).addOnFailureListener(new OnFailureListener() {
            @Override
            public void onFailure(@NonNull Exception exception) {
                // Uh-oh, an error occurred!
                Toast.makeText(context, "移除前次盤點圖片失敗", Toast.LENGTH_SHORT).show();

            }
        });
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
                    Toast.makeText(context, "image upload failure", Toast.LENGTH_SHORT).show();
                }
            }).addOnSuccessListener(new OnSuccessListener<UploadTask.TaskSnapshot>() {
                @Override
                public void onSuccess(UploadTask.TaskSnapshot taskSnapshot) {
                    Toast.makeText(context, "image upload success", Toast.LENGTH_SHORT).show();
                    Log.e(TAG, "image upload success: ");
                }
            });
        } else {
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

        final String name = binding.textInputLayoutName.getEditText().getText().toString();
        final String barcode = binding.textInputLayoutBarcode.getEditText().getText().toString();
        final String number = binding.textInputLayoutNumber.getEditText().getText().toString();
        final String location = binding.textInputLayoutLocation.getEditText().getText().toString();
        final String remarks = binding.textInputLayoutRemarks.getEditText().getText().toString();
//        final String name = binding.fieldName.getText().toString();
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

        String FileName = null;
        if (mUploadTask != null && mUploadTask.isInProgress()) {
            Toast.makeText(getContext(), "尚有其他上傳程序執行中，請再試一次", Toast.LENGTH_SHORT).show();
        } else {
            FileName = uploadFile(); //有選到新照片進行上傳照片到Storage
        }
        if(FileName!=null){
            uploadFileName = FileName;
        }

        if(loadFileName!=null) {
            if(!uploadFileName.equals(loadFileName)){
                Toast.makeText(context, "要刪了!!!!!!!!!!!!!!!!!!!", Toast.LENGTH_LONG).show();
                deleteFile(loadFileName);
            }
        }

        // Disable button so there are no multi-posts
        setEditingEnabled(false);
        Toast.makeText(getContext(), "Posting...", Toast.LENGTH_SHORT).show();

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
                        }

                        setEditingEnabled(true);
                        NavHostFragment.findNavController(UpdatePostFragment.this)
                                .navigate(R.id.action_UpdatePostFragment_to_MainFragment);
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
//        if (enabled) {
//            binding.fabSubmitPost.show();
//        } else {
//            binding.fabSubmitPost.hide();
//        }
    }
    private void writeNewPost(String userId, String username, String name, String barcode, String number, String location, String remarks, String uploadFileName) {
        // Create new post at /user-posts/$userid/$postid and at
        // /posts/$postid simultaneously
        Post post = new Post(userId, username, name, barcode,number,location,remarks, uploadFileName);
        Map<String, Object> postValues = post.toMap();
        Map<String, Object> childUpdates = new HashMap<>();
        childUpdates.put("/posts/" + mPostKey, postValues);
        childUpdates.put("/user-posts/" + userId + "/" + mPostKey, postValues);
        mDatabase.updateChildren(childUpdates).addOnSuccessListener(new OnSuccessListener<Void>() {
            @Override
            public void onSuccess(Void unused) {
                Toast.makeText(context, "更新資料成功", Toast.LENGTH_SHORT).show();
//                binding.fabSubmitPost.show();
            }
        })
        .addOnFailureListener(new OnFailureListener() {
            @Override
            public void onFailure(@NonNull Exception e) {
                Toast.makeText(context, "更新資料失敗QQ", Toast.LENGTH_SHORT).show();
//                binding.fabSubmitPost.show();
            }
        });
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