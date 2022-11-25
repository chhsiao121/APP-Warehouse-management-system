package com.chhsiao.firebase.quickstart.database.java;

import android.net.Uri;
import android.os.Bundle;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Toast;

import com.chhsiao.firebase.quickstart.database.java.models.Post;
import com.chhsiao.firebase.quickstart.database.java.models.PostV2;
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
import com.google.firebase.quickstart.database.databinding.FragmentPostResultBinding;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;
import com.squareup.picasso.Picasso;


public class PostResultFragment extends BaseFragment {
    public static String proLocation;
    private FragmentPostResultBinding binding;
    // [START define_database_reference]
    private DatabaseReference mDatabase;
    // [END define_database_reference]
    private StorageReference mStorageRef;
    private RecyclerView mRecycler;
    private LinearLayoutManager mManager;
    private FirebaseRecyclerAdapter<PostV2, PostT22ViewHolder> mAdapter;
    public PostResultFragment() {
        // Required empty public constructor
    }


    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        proLocation = requireArguments().getString("location");
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        binding = FragmentPostResultBinding.inflate(inflater, container, false);
        proLocation = requireArguments().getString("location");
        mDatabase = FirebaseDatabase.getInstance().getReference();
        mStorageRef = FirebaseStorage.getInstance().getReference();
        mRecycler=binding.postList;
        mRecycler.setHasFixedSize(true);
        // Inflate the layout for this fragment
        return binding.getRoot();
    }
    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

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
        Query query = mDatabase.child(proLocation).child("verison2").child("posts").limitToLast(50);
        FirebaseRecyclerOptions<PostV2> options =
                new FirebaseRecyclerOptions.Builder<PostV2>()
                        .setQuery(query, PostV2.class)
                        .build();
        mAdapter = new FirebaseRecyclerAdapter<PostV2, PostT22ViewHolder>(options) {
            @Override
            public PostT22ViewHolder onCreateViewHolder(ViewGroup viewGroup, int i) {
                LayoutInflater inflater = LayoutInflater.from(viewGroup.getContext());
                return new PostT22ViewHolder(inflater.inflate(R.layout.item_post_t22, viewGroup, false));
            }
            @Override
            protected void onBindViewHolder(PostT22ViewHolder viewHolder, int position, final PostV2 model) {
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
}