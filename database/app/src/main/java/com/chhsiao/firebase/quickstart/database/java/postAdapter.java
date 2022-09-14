package com.chhsiao.firebase.quickstart.database.java;
import static java.security.AccessController.getContext;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.navigation.NavController;
import androidx.navigation.Navigation;
import androidx.recyclerview.widget.RecyclerView;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.quickstart.database.R;
import com.chhsiao.firebase.quickstart.database.java.models.Post;
import com.firebase.ui.database.FirebaseRecyclerAdapter;
import com.firebase.ui.database.FirebaseRecyclerOptions;

// FirebaseRecyclerAdapter is a class provided by
// FirebaseUI. it provides functions to bind, adapt and show
// database contents in a Recycler View
public class postAdapter extends FirebaseRecyclerAdapter<Post, postAdapter.postsViewholder> {

    public postAdapter(
            @NonNull FirebaseRecyclerOptions<Post> options)
    {
        super(options);
    }

    // Function to bind the view in Card view(here"person.xml") iwth data in
    // model class(here "person.class")
    @Override
    protected void onBindViewHolder(@NonNull postsViewholder holder, int position, @NonNull Post model)
    {
        final DatabaseReference postRef = getRef(position);
        // Set click listener for the whole post view
        final String postKey = postRef.getKey();
        holder.itemView.setOnClickListener(new View.OnClickListener() { //這裡會跳進去每個itemView的詳細資料
            @Override
            public void onClick(View v) {

//                // Launch PostDetailFragment
//                NavController navController = Navigation.findNavController(requireActivity(),
//                        R.id.nav_host_fragment);
//                Bundle args = new Bundle();
//                args.putString(PostDetailFragment.EXTRA_POST_KEY, postKey);
//                navController.navigate(R.id.action_MainFragment_to_PostDetailFragment, args);
            }
        });
        // Determine if the current user has liked this post and set UI accordingly
        if (model.stars.containsKey(getUid())) {
            holder.starView.setImageResource(R.drawable.ic_toggle_star_24);
        } else {
            holder.starView.setImageResource(R.drawable.ic_toggle_star_outline_24);
        }
        // Add firstname from model class (here
        // "person.class")to appropriate view in Card
        // view (here "person.xml")
//        holder.firstname.setText(model.getFirstname());

        // Add lastname from model class (here
        // "person.class")to appropriate view in Card
        // view (here "person.xml")
//        holder.lastname.setText(model.getLastname());

        // Add age from model class (here
        // "person.class")to appropriate view in Card
        // view (here "person.xml")
//        holder.age.setText(model.getAge());
    }

    // Function to tell the class about the Card view (here
    // "person.xml")in
    // which the data will be shown
    @NonNull
    @Override
    public postsViewholder onCreateViewHolder(@NonNull ViewGroup parent, int viewType)
    {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_post, parent, false);
        return new postsViewholder(view);
    }

    // Sub Class to create references of the views in Crad
    // view (here "person.xml")
    static class postsViewholder extends RecyclerView.ViewHolder {
        public TextView titleView;
        public TextView authorView;
        public ImageView starView;
        public TextView numStarsView;
        public TextView bodyView;

        public postsViewholder(View itemView) {
            super(itemView);

            titleView = itemView.findViewById(R.id.postName);
            authorView = itemView.findViewById(R.id.postAuthor);
            starView = itemView.findViewById(R.id.star);
            numStarsView = itemView.findViewById(R.id.postNumStars);
            bodyView = itemView.findViewById(R.id.postNumber);
        }

        public void bindToPost(Post post, View.OnClickListener starClickListener) {
            titleView.setText(post.name);
            authorView.setText(post.author);
            numStarsView.setText(String.valueOf(post.starCount));
            bodyView.setText(post.number);

            starView.setOnClickListener(starClickListener);
        }
    }
    public String getUid() {
        return FirebaseAuth.getInstance().getCurrentUser().getUid();
    }
}
