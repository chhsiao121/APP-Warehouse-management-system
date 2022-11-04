package com.chhsiao.firebase.quickstart.database.java;

import static android.app.Activity.RESULT_OK;

import android.Manifest;
import android.app.AlertDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.graphics.Bitmap;
import android.net.Uri;
import android.os.Bundle;

import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;
import androidx.fragment.app.Fragment;
import androidx.navigation.NavController;
import androidx.navigation.Navigation;
import androidx.navigation.fragment.NavHostFragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.provider.MediaStore;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Toast;

import com.chhsiao.firebase.quickstart.database.java.capture.CaptureAct;
import com.chhsiao.firebase.quickstart.database.java.models.Post;
import com.chhsiao.firebase.quickstart.database.java.viewholder.PostT22ViewHolder;
import com.firebase.ui.database.FirebaseRecyclerAdapter;
import com.firebase.ui.database.FirebaseRecyclerOptions;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.Query;
import com.google.firebase.quickstart.database.R;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;
import com.journeyapps.barcodescanner.ScanContract;
import com.journeyapps.barcodescanner.ScanOptions;
import com.squareup.picasso.Picasso;
import com.google.firebase.quickstart.database.databinding.FragmentInventoryBinding;
import com.journeyapps.barcodescanner.ScanContract;
import com.journeyapps.barcodescanner.ScanOptions;
/**
 * A simple {@link Fragment} subclass.
 * create an instance of this fragment.
 */
public class InventoryFragment extends BaseFragment {

    private static final String TAG = "InventoryFragment";
    public static String proLocation;
    private static final int PICK_CAMERA_REQUEST = 100;
    ScanOptions options = new ScanOptions();
    private Uri mImageUri;
    public InventoryFragment() {
        // Required empty public constructor
    }
    // [START define_database_reference]
    private DatabaseReference mDatabase;
    // [END define_database_reference]
    private FragmentInventoryBinding binding;
    private FirebaseRecyclerAdapter<Post, PostT22ViewHolder> mAdapter;
    private RecyclerView mRecycler;
    private LinearLayoutManager mManager;


    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        proLocation = requireArguments().getString("location");
        if(ContextCompat.checkSelfPermission(getActivity(), Manifest.permission.CAMERA)!=PackageManager.PERMISSION_GRANTED){
            ActivityCompat.requestPermissions(getActivity(),new String[]{Manifest.permission.CAMERA},PICK_CAMERA_REQUEST);
        }
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        binding = FragmentInventoryBinding.inflate(inflater, container, false);


        proLocation = requireArguments().getString("location");
        mDatabase = FirebaseDatabase.getInstance().getReference().child(proLocation);
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
                mAdapter.notifyDataSetChanged();
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
        Query query = mDatabase.child("posts").limitToLast(50);

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
                Bitmap bitmap = (Bitmap)data.getExtras().get("data");
                binding.imageViewItem.setImageBitmap(bitmap);
            }


        }
//            if(requestCode==100){
//            Bitmap bitmap = (Bitmap)data.getExtras().get("data");
//            binding.imageViewItem.setImageBitmap(bitmap);
//        }
    }
}