package com.chhsiao.firebase.quickstart.database.java;

import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.graphics.Color;
import android.net.Uri;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Toast;

import androidx.activity.OnBackPressedCallback;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.core.content.res.ResourcesCompat;
import androidx.navigation.fragment.NavHostFragment;
import androidx.recyclerview.widget.RecyclerView;

import com.chhsiao.firebase.quickstart.database.java.models.Comment;
import com.chhsiao.firebase.quickstart.database.java.models.Post;
import com.chhsiao.firebase.quickstart.database.java.models.User;
import com.chhsiao.firebase.quickstart.database.java.viewholder.CommentViewHolder;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.database.ChildEventListener;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.google.firebase.quickstart.database.R;
import com.google.firebase.quickstart.database.databinding.FragmentPostDetailBinding;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;
import com.leinardi.android.speeddial.SpeedDialActionItem;
import com.leinardi.android.speeddial.SpeedDialView;
import com.squareup.picasso.Picasso;

import java.util.ArrayList;
import java.util.List;

public class PostDetailFragment extends BaseFragment {



    private static final String TAG = "PostDetailFragment";

    public static final String EXTRA_POST_KEY = "post_key";

    private DatabaseReference mPostReference;
    private DatabaseReference mCommentsReference;
    private ValueEventListener mPostListener;
    private String mPostKey;
    private CommentAdapter mAdapter;
    private Post post;
    private Context context;
    private String author,location,name,number,remarks,barcode;
    private String uploadFileName;
    private StorageReference storageReference;
    private String proLocation;
    private FragmentPostDetailBinding binding;

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
                NavHostFragment.findNavController(PostDetailFragment.this)
                        .navigate(R.id.action_PostDetailFragment_to_MainFragment,args);
            }
        };
        requireActivity().getOnBackPressedDispatcher().addCallback(this, callback);

        // The callback can be enabled or disabled here or in handleOnBackPressed()
    }

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        binding = FragmentPostDetailBinding.inflate(inflater, container, false);
        return binding.getRoot();
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        proLocation = MainFragment.proLocation;
        // Get post key from arguments
        mPostKey = requireArguments().getString(EXTRA_POST_KEY);
        if (mPostKey == null) {
            throw new IllegalArgumentException("Must pass EXTRA_POST_KEY");
        }
        context = getContext();
        // Initialize Database
        mPostReference = FirebaseDatabase.getInstance().getReference().child(proLocation)
                .child("posts").child(mPostKey);
        mCommentsReference = FirebaseDatabase.getInstance().getReference().child(proLocation)
                .child("post-comments").child(mPostKey);
//        var storageRef = storage.ref();
//        storageReference = storage.getReference();

//        binding.buttonPostComment.setOnClickListener(new View.OnClickListener() {
//            @Override
//            public void onClick(View view) {
//                postComment();
//            }
//        });
//        binding.recyclerPostComments.setLayoutManager(new LinearLayoutManager(getContext()));



        //FloatingActionButtonSpeedDial

        SpeedDialActionItem itemEdit = new SpeedDialActionItem.Builder(
                R.id.fab_edit, R.drawable.ic_baseline_edit_note_24)
                .setLabel("編輯盤點項目")
                .setLabelClickable(false)
                .setTheme(R.style.AppTheme_Cyan)
                .create();
        SpeedDialActionItem itemDel = new SpeedDialActionItem.Builder(
                R.id.fab_del, R.drawable.ic_baseline_delete_forever_24)
                .setLabel("刪除此盤點項目")
                .setLabelClickable(false)
                .setTheme(R.style.AppTheme_Cyan)
                .create();
        SpeedDialActionItem itemBack = new SpeedDialActionItem.Builder(
                R.id.fab_back, R.drawable.ic_baseline_arrow_back_24)
                .setLabel("返回")
                .setLabelClickable(false)
                .setTheme(R.style.AppTheme_Cyan)
                .create();

        binding.speedDial.addActionItem(itemEdit);
        binding.speedDial.addActionItem(itemDel);
        binding.speedDial.addActionItem(itemBack);
        binding.speedDial.setOnActionSelectedListener(new SpeedDialView.OnActionSelectedListener() {
            @Override
            public boolean onActionSelected(SpeedDialActionItem actionItem) {
                switch (actionItem.getId()){
                    case R.id.fab_edit:
                        if(!post.uid.equals(getUid())){
                            Toast.makeText(getContext(),"無法編輯其他使用者盤點之物件",Toast.LENGTH_SHORT).show();
                        }
                        else{
                            Bundle args = new Bundle();
                            args.putString(PostDetailFragment.EXTRA_POST_KEY, mPostKey);
                            if(post != null) {
                                args.putString("location",location);
                                args.putString("name",name);
                                args.putString("number",number);
                                args.putString("remarks",remarks);
                                args.putString("barcode",barcode);
                                args.putString("uploadFileName",uploadFileName);
                            }
                            NavHostFragment.findNavController(PostDetailFragment.this)
                                    .navigate(R.id.action_PostDetailFragment_to_UpdatePostFragment,args);
                        }
                        break;
                    case R.id.fab_del:
                        AlertDialog.Builder builder = new AlertDialog.Builder(getContext());
                        builder.setMessage("確定要刪除\n盤點碼"+post.barcode+"的"+post.name+"嗎");
                        builder.setTitle("刪除此盤點項目");
                        builder.setPositiveButton("確定", new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialog, int which) {
                                deleteData();
                            }
                        }).setNegativeButton("取消", new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialog, int which) {
                                Toast.makeText(getContext(), "已取消", Toast.LENGTH_SHORT).show();
                            }
                        });
                        AlertDialog dialog = builder.create();
                        dialog.show();
                        break;
                    case R.id.fab_back:
                        Bundle args = new Bundle();
                        args.putString("location", proLocation);
                        NavHostFragment.findNavController(PostDetailFragment.this)
                                .navigate(R.id.action_PostDetailFragment_to_MainFragment,args);
                        break;
                }
                return false;
            }
        });


    }
    private void deleteData(){
        mPostReference.removeValue().addOnCompleteListener(new OnCompleteListener<Void>() {
            @Override
            public void onComplete(@NonNull Task<Void> task) {
                if(task.isSuccessful()){
                    Toast.makeText(context, "已刪除post", Toast.LENGTH_SHORT).show();

                }
            }
        });
        DatabaseReference mUserpostReference = FirebaseDatabase.getInstance().getReference().child(proLocation)
                .child("user-posts").child(post.uid).child(mPostKey);
        mUserpostReference.removeValue().addOnCompleteListener(new OnCompleteListener<Void>() {
            @Override
            public void onComplete(@NonNull Task<Void> task) {
                if(task.isSuccessful()){
                    Toast.makeText(context, "已刪除user-post", Toast.LENGTH_SHORT).show();

                }
            }
        });

        if (post.uploadFileName != null) {
            uploadFileName = post.uploadFileName;
            FirebaseStorage storage = FirebaseStorage.getInstance();
            StorageReference storageRef = storage.getReference();
            storageRef.child(uploadFileName).delete().addOnSuccessListener(new OnSuccessListener<Void>() {
                @Override
                public void onSuccess(Void aVoid) {
                    // File deleted successfully
                    Toast.makeText(context, "已刪除圖片", Toast.LENGTH_SHORT).show();
                }
            }).addOnFailureListener(new OnFailureListener() {
                @Override
                public void onFailure(@NonNull Exception exception) {
                    // Uh-oh, an error occurred!
                }
            });
        }
        Bundle args = new Bundle();
        args.putString("location", proLocation);
        NavHostFragment.findNavController(PostDetailFragment.this)
                .navigate(R.id.action_PostDetailFragment_to_MainFragment,args);
    }

    @Override
    public void onStart() {
        super.onStart();



        // Add value event listener to the post
        ValueEventListener postListener = new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                // Get Post object and use the values to update the UI
                if(dataSnapshot.exists()) {
                    post = dataSnapshot.getValue(Post.class);
                    author = post.author;
                    location = post.location;
                    name = post.name;
                    number = post.number;
                    remarks = post.remarks;
                    barcode = post.barcode;
                    binding.postAuthorLayout.postAuthor.setText(author);
                    binding.postTextLayout.postLocation.setText(location);
                    binding.postTextLayout.postName.setText(name);
                    binding.postTextLayout.postNumber.setText(number);
                    binding.postTextLayout.postRemarks.setText(remarks);
                    binding.postTextLayout.postBarcode.setText(barcode);

                    if (post.uploadFileName != null) {
                        uploadFileName = post.uploadFileName;
                    }
                }
                if(uploadFileName!=null){
                    FirebaseStorage storage = FirebaseStorage.getInstance();
                    StorageReference storageRef = storage.getReference();
                    storageRef.child(uploadFileName).getDownloadUrl().addOnSuccessListener(new OnSuccessListener<Uri>() {
                        @Override
                        public void onSuccess(Uri uri) {
                            // Got the download URL for 'users/me/profile.png'
                            Picasso.get().load(uri).into(binding.imageView2);
                        }
                    }).addOnFailureListener(new OnFailureListener() {
                        @Override
                        public void onFailure(@NonNull Exception exception) {
                            // Handle any errors
                            binding.imageView2.setImageResource(R.drawable.images);
                            Toast.makeText(context, "Failed to load image from Firebase.", Toast.LENGTH_SHORT).show();
                        }
                    });
                }


//                storageReference.child(post.uploadFileName).getDownloadUrl().addOnSuccessListener(new OnSuccessListener<Uri>() {
//                    @Override
//                    public void onSuccess(Uri uri) {
//                        binding.imageView2.setImageURI(uri);
//                    }
//                }).addOnFailureListener(new OnFailureListener() {
//                    @Override
//                    public void onFailure(@NonNull Exception exception) {
//                        Toast.makeText(getContext(), "Failed to load image from FB.",
//                                Toast.LENGTH_SHORT).show();
//                    }
//                });

            }

            @Override
            public void onCancelled(DatabaseError databaseError) {
                // Getting Post failed, log a message
                Log.w(TAG, "loadPost:onCancelled", databaseError.toException());
                Toast.makeText(getContext(), "Failed to load post.",
                        Toast.LENGTH_SHORT).show();
            }
        };
        mPostReference.addValueEventListener(postListener);

        // Keep copy of post listener so we can remove it when app stops
        mPostListener = postListener;




        // Listen for comments
        mAdapter = new CommentAdapter(getContext(), mCommentsReference);
//        binding.recyclerPostComments.setAdapter(mAdapter);


    }


    @Override
    public void onStop() {
        super.onStop();

        // Remove post value event listener
        if (mPostListener != null) {
            mPostReference.removeEventListener(mPostListener);
        }

        // Clean up comments listener
        mAdapter.cleanupListener();
    }

    private void postComment() {
        final String uid = getUid();
        FirebaseDatabase.getInstance().getReference().child("users").child(uid)
                .addListenerForSingleValueEvent(new ValueEventListener() {
                    @Override
                    public void onDataChange(DataSnapshot dataSnapshot) {
                        // Get user information
                        User user = dataSnapshot.getValue(User.class);
                        String authorName = user.username;

                        // Create new comment object
//                        String commentText = binding.fieldCommentText.getText().toString();
//                        Comment comment = new Comment(uid, authorName, commentText);

                        // Push the comment, it will appear in the list
//                        mCommentsReference.push().setValue(comment);

                        // Clear the field
//                        binding.fieldCommentText.setText(null);
                    }

                    @Override
                    public void onCancelled(DatabaseError databaseError) {

                    }
                });
    }

    private static class CommentAdapter extends RecyclerView.Adapter<CommentViewHolder> {

        private Context mContext;
        private DatabaseReference mDatabaseReference;
        private ChildEventListener mChildEventListener;

        private List<String> mCommentIds = new ArrayList<>();
        private List<Comment> mComments = new ArrayList<>();

        public CommentAdapter(final Context context, DatabaseReference ref) {
            mContext = context;
            mDatabaseReference = ref;

            // Create child event listener
            ChildEventListener childEventListener = new ChildEventListener() {
                @Override
                public void onChildAdded(DataSnapshot dataSnapshot, String previousChildName) {
                    Log.d(TAG, "onChildAdded:" + dataSnapshot.getKey());

                    // A new comment has been added, add it to the displayed list
                    Comment comment = dataSnapshot.getValue(Comment.class);

                    // Update RecyclerView
                    mCommentIds.add(dataSnapshot.getKey());
                    mComments.add(comment);
                    notifyItemInserted(mComments.size() - 1);
                }

                @Override
                public void onChildChanged(DataSnapshot dataSnapshot, String previousChildName) {
                    Log.d(TAG, "onChildChanged:" + dataSnapshot.getKey());

                    // A comment has changed, use the key to determine if we are displaying this
                    // comment and if so displayed the changed comment.
                    Comment newComment = dataSnapshot.getValue(Comment.class);
                    String commentKey = dataSnapshot.getKey();

                    int commentIndex = mCommentIds.indexOf(commentKey);
                    if (commentIndex > -1) {
                        // Replace with the new data
                        mComments.set(commentIndex, newComment);

                        // Update the RecyclerView
                        notifyItemChanged(commentIndex);
                    } else {
                        Log.w(TAG, "onChildChanged:unknown_child:" + commentKey);
                    }
                }

                @Override
                public void onChildRemoved(DataSnapshot dataSnapshot) {
                    Log.d(TAG, "onChildRemoved:" + dataSnapshot.getKey());

                    // A comment has changed, use the key to determine if we are displaying this
                    // comment and if so remove it.
                    String commentKey = dataSnapshot.getKey();

                    int commentIndex = mCommentIds.indexOf(commentKey);
                    if (commentIndex > -1) {
                        // Remove data from the list
                        mCommentIds.remove(commentIndex);
                        mComments.remove(commentIndex);

                        // Update the RecyclerView
                        notifyItemRemoved(commentIndex);
                    } else {
                        Log.w(TAG, "onChildRemoved:unknown_child:" + commentKey);
                    }
                }

                @Override
                public void onChildMoved(DataSnapshot dataSnapshot, String previousChildName) {
                    Log.d(TAG, "onChildMoved:" + dataSnapshot.getKey());

                    // A comment has changed position, use the key to determine if we are
                    // displaying this comment and if so move it.
                    Comment movedComment = dataSnapshot.getValue(Comment.class);
                    String commentKey = dataSnapshot.getKey();

                    // ...
                }

                @Override
                public void onCancelled(DatabaseError databaseError) {
                    Log.w(TAG, "postComments:onCancelled", databaseError.toException());
                    Toast.makeText(mContext, "Failed to load comments.",
                            Toast.LENGTH_SHORT).show();
                }
            };
            ref.addChildEventListener(childEventListener);

            // Store reference to listener so it can be removed on app stop
            mChildEventListener = childEventListener;
        }

        @Override
        public CommentViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
            LayoutInflater inflater = LayoutInflater.from(mContext);
            View view = inflater.inflate(R.layout.item_comment, parent, false);
            return new CommentViewHolder(view);
        }

        @Override
        public void onBindViewHolder(CommentViewHolder holder, int position) {
            Comment comment = mComments.get(position);
            holder.authorView.setText(comment.author);
            holder.bodyView.setText(comment.text);
        }

        @Override
        public int getItemCount() {
            return mComments.size();
        }

        public void cleanupListener() {
            if (mChildEventListener != null) {
                mDatabaseReference.removeEventListener(mChildEventListener);
            }
        }

    }
}
