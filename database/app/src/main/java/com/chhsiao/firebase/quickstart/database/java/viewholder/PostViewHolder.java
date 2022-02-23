package com.chhsiao.firebase.quickstart.database.java.viewholder;

import android.view.View;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.recyclerview.widget.RecyclerView;

import com.google.firebase.quickstart.database.R;
import com.chhsiao.firebase.quickstart.database.java.models.Post;

public class PostViewHolder extends RecyclerView.ViewHolder {

    public TextView titleView;
    public TextView authorView;
    public ImageView starView;
    public TextView numStarsView;
    public TextView bodyView;

    public PostViewHolder(View itemView) {
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
        bodyView.setText(post.count);

        starView.setOnClickListener(starClickListener);
    }
}
