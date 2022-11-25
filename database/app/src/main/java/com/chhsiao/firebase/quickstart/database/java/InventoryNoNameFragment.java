package com.chhsiao.firebase.quickstart.database.java;

import android.Manifest;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.graphics.Bitmap;
import android.net.Uri;
import android.os.Bundle;

import androidx.activity.result.ActivityResultLauncher;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.provider.MediaStore;
import android.text.TextUtils;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Toast;

import com.chhsiao.firebase.quickstart.database.java.capture.CaptureAct;
import com.chhsiao.firebase.quickstart.database.java.models.Post;
import com.chhsiao.firebase.quickstart.database.java.models.PostV2;
import com.chhsiao.firebase.quickstart.database.java.models.User;
import com.chhsiao.firebase.quickstart.database.java.viewholder.PostT22ViewHolder;
import com.firebase.ui.database.FirebaseRecyclerAdapter;
import com.firebase.ui.database.FirebaseRecyclerOptions;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.Query;
import com.google.firebase.database.ValueEventListener;
import com.google.firebase.quickstart.database.R;

import com.google.firebase.quickstart.database.databinding.FragmentInventoryNoNameBinding;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;
import com.google.firebase.storage.StorageTask;
import com.google.firebase.storage.UploadTask;
import com.journeyapps.barcodescanner.ScanContract;
import com.journeyapps.barcodescanner.ScanOptions;
import com.squareup.picasso.Picasso;

import java.io.ByteArrayOutputStream;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;


public class InventoryNoNameFragment extends BaseFragment {

    private static final String REQUIRED = "Required";
    private static final String TAG = "InventoryFragment";
    public static String proLocation;
    public static boolean fieldName;
    private static final int PICK_CAMERA_REQUEST = 100;
    ScanOptions options = new ScanOptions();
    private Bitmap mImageBitmap;
    private StorageTask mUploadTask;

    public InventoryNoNameFragment() {
        // Required empty public constructor
    }
    // [START define_database_reference]
    private DatabaseReference mDatabase;
    // [END define_database_reference]
    private StorageReference mStorageRef;
    private FragmentInventoryNoNameBinding binding;
    private FirebaseRecyclerAdapter<Post, PostT22ViewHolder> mAdapter;
    private RecyclerView mRecycler;
    private LinearLayoutManager mManager;


    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        proLocation = requireArguments().getString("location");
        if(ContextCompat.checkSelfPermission(getActivity(), Manifest.permission.CAMERA)!= PackageManager.PERMISSION_GRANTED){
            ActivityCompat.requestPermissions(getActivity(),new String[]{Manifest.permission.CAMERA},PICK_CAMERA_REQUEST);
        }
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        binding = FragmentInventoryNoNameBinding.inflate(inflater, container, false);
        proLocation = requireArguments().getString("location");
        mDatabase = FirebaseDatabase.getInstance().getReference();
        mStorageRef = FirebaseStorage.getInstance().getReference();
        mRecycler=binding.listT22View;
        mRecycler.setHasFixedSize(true);
        // Inflate the layout for this fragment
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
        binding.btnScanBarcode.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                barcodeLauncher.launch(options);
            }
        });
        binding.btnPhotograph.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);
                startActivityForResult(intent,100);
            }
        });
        binding.btnok.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                submitPost();
            }
        });
        binding.refreshLayout.setColorSchemeColors(getResources().getColor(R.color.material_cyan_a700));
        binding.refreshLayout.setOnRefreshListener(()->{
            mAdapter.notifyDataSetChanged();
            binding.refreshLayout.setRefreshing(false);
        });
    }
    @Override
    public void onActivityCreated(Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);

        // Set up Layout Manager, reverse layout
        mManager = new LinearLayoutManager(getActivity());
        mManager.setReverseLayout(true);
        mManager.setStackFromEnd(true);
        mRecycler.setLayoutManager(mManager);
        mRecycler.setItemAnimator(null);
        mRecycler.setNestedScrollingEnabled(false);


        // Set up FirebaseRecyclerAdapter with the Query
//        Query query = mDatabase.child(proLocation).child("posts").limitToLast(50);
        Query query = mDatabase.child(proLocation).child("verison2").child("posts").limitToLast(50);

        FirebaseRecyclerOptions<Post> options =
                new FirebaseRecyclerOptions.Builder<Post>()
                        .setQuery(query, Post.class)
                        .build();
        mAdapter = new FirebaseRecyclerAdapter<Post, PostT22ViewHolder>(options) {
            @Override
            public PostT22ViewHolder onCreateViewHolder(ViewGroup viewGroup, int i) {
                LayoutInflater inflater = LayoutInflater.from(viewGroup.getContext());
                return new PostT22ViewHolder(inflater.inflate(R.layout.item_post_t22, viewGroup, false));
            }
            @Override
            protected void onBindViewHolder(PostT22ViewHolder viewHolder, int position, final Post model) {
                final DatabaseReference postRef = getRef(position);
                // Set click listener for the whole post view
                final String postKey = postRef.getKey();
                viewHolder.itemView.setOnClickListener(new View.OnClickListener() { //這裡會跳進去每個itemView的詳細資料
                    @Override
                    public void onClick(View v) {
                        // Launch PostDetailFragment
//                        NavController navController = Navigation.findNavController(requireActivity(),
//                                R.id.nav_host_fragment);
//                        Bundle args = new Bundle();
//                        args.putString(PostDetailFragment.EXTRA_POST_KEY, postKey);
//                        navController.navigate(R.id.action_MainFragment_to_PostDetailFragment, args);
                        Toast.makeText(getContext(), postKey, Toast.LENGTH_SHORT).show();
                    }
                });
                viewHolder.barcodeView.setText(model.barcode);
                viewHolder.itemNameView.setText(model.name);
                viewHolder.numberView.setText(model.number);
                viewHolder.uploadTimeView.setText(model.location);
                viewHolder.remarksView.setText(model.remarks);
                String uploadFileName = model.uploadFileName;
                if(uploadFileName!=null){
                    StorageReference storageRef = FirebaseStorage.getInstance().getReference();
                    storageRef.child(uploadFileName).getDownloadUrl().addOnSuccessListener(new OnSuccessListener<Uri>() {
                        @Override
                        public void onSuccess(Uri uri) {
                            // Got the download URL for 'users/me/profile.png'
                            Picasso.get().load(uri).into(viewHolder.imageItemView1);
                        }
                    }).addOnFailureListener(new OnFailureListener() {
                        @Override
                        public void onFailure(@NonNull Exception exception) {
                            // Handle any errors
                            Toast.makeText(getContext(), "Failed to load image from Firebase.", Toast.LENGTH_SHORT).show();
                        }
                    });
                }

            }
        };
        mRecycler.setAdapter(mAdapter);
    }
    @Override
    public void onStart() {
        super.onStart();
        if (mAdapter != null) {
            mAdapter.startListening();
        }
    }

    @Override
    public void onStop() {
        super.onStop();
        if (mAdapter != null) {
            mAdapter.stopListening();
        }
    }
    public String getUid() {
        return FirebaseAuth.getInstance().getCurrentUser().getUid();
    }

    private final ActivityResultLauncher<ScanOptions> barcodeLauncher = registerForActivityResult(new ScanContract(),
            result -> {
                if(result.getContents()!=null){
                    binding.textInputLayoutBarcode.getEditText().setText(result.getContents());
                }
                else {
                    Toast.makeText(getActivity(),"No Results",Toast.LENGTH_SHORT).show();
                }
            });

    @Override
    public void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == PICK_CAMERA_REQUEST ){
//            mImageUri = data.getData();
            if(data.getExtras().get("data")!=null){
                mImageBitmap = (Bitmap)data.getExtras().get("data");
                binding.imageViewItem.setImageBitmap(mImageBitmap);
            }


        }
//            if(requestCode==100){
//            Bitmap bitmap = (Bitmap)data.getExtras().get("data");
//            binding.imageViewItem.setImageBitmap(bitmap);
//        }
    }
    private void writeNewPost(String userId, String username, String name, String barcode, String number, String location, String remarks, String uploadFileName) {
        // Create new post at /user-posts/$userid/$postid and at
        // /posts/$postid simultaneously
        String key = mDatabase.child(proLocation).child("verison2").child("posts").push().getKey();
        Date dNow = new Date( );
        // object using hashCode() method
        int code = dNow.hashCode();
        SimpleDateFormat ft = new SimpleDateFormat ("yyyy.MM.dd.HH.mm.ss");
        String time = ft.format(dNow).toString().replace(":",".").replace(" ","_");
        PostV2 postv2 = new PostV2(userId, username, name, barcode,number,location,remarks, uploadFileName,time);
        Map<String, Object> postValues = postv2.toMap();
        Map<String, Object> childUpdates = new HashMap<>();
        childUpdates.put("/"+proLocation+"/verison2/posts/" + key, postValues);
        childUpdates.put("/"+proLocation+"/verison2/user-posts/" + userId + "/" + key, postValues);
        mDatabase.updateChildren(childUpdates);
    }
    private void submitPost() {
        final String barcode = binding.textInputLayoutBarcode.getEditText().getText().toString();
        final String number = binding.textInputLayoutNumber.getEditText().getText().toString();

        //Title is required
        if (TextUtils.isEmpty(barcode)) {
//            binding.fieldNumber.setError(REQUIRED);
            binding.textInputLayoutBarcode.setError(REQUIRED);
            return;
        }
        if (TextUtils.isEmpty(number)) {
//            binding.fieldNumber.setError(REQUIRED);
            binding.textInputLayoutNumber.setError(REQUIRED);
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
        binding.textInputLayoutBarcode.setEnabled(false);
        binding.textInputLayoutNumber.setEnabled(false);


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
                            writeNewPost(userId, user.username, null, barcode, number, proLocation, null, uploadFileName);
//                            writeNewPost(userId, user.username, location, number, count, format, remarks, barcode, snumber, unit, name, uploadFileName);
                        }

                        binding.textInputLayoutBarcode.setEnabled(true);
                        binding.textInputLayoutNumber.setEnabled(true);

                    }

                    @Override
                    public void onCancelled(@NonNull DatabaseError databaseError) {
                        Log.w(TAG, "getUser:onCancelled", databaseError.toException());
                        binding.textInputLayoutBarcode.setEnabled(true);
                        binding.textInputLayoutNumber.setEnabled(true);


                    }
                });
    }

    private String uploadFile() {
        final String fileName;
        if (mImageBitmap != null) {
            fileName = System.currentTimeMillis() + ".jpg";
            //                Bitmap original = MediaStore.Images.Media.getBitmap(requireActivity().getContentResolver(), mImageUri);
            ByteArrayOutputStream stream = new ByteArrayOutputStream();
            Bitmap resized = Bitmap.createScaledBitmap ( mImageBitmap , 1000 , 1000 , true ) ;
            ByteArrayOutputStream baos = new ByteArrayOutputStream();
            resized.compress(Bitmap.CompressFormat.JPEG, 50, baos);
            byte[] imageByte = baos.toByteArray();
            StorageReference fileReference = mStorageRef.child(fileName);
            mUploadTask = fileReference.putBytes(imageByte);

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

}