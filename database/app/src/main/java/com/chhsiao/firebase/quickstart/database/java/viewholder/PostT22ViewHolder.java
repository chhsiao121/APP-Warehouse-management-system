package com.chhsiao.firebase.quickstart.database.java.viewholder;
import android.view.View;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.google.firebase.quickstart.database.R;
import com.chhsiao.firebase.quickstart.database.java.models.Post;
public class PostT22ViewHolder extends RecyclerView.ViewHolder  {
    public TextView barcodeView;
    public TextView itemNameView;
    public TextView numberView;
    public TextView uploadTimeView;
    public TextView remarksView;
    public ImageView imageItemView1;
    public ImageView imageItemView2;
    public ImageView imageItemView3;
    public PostT22ViewHolder(@NonNull View itemView) {
        super(itemView);
        barcodeView = itemView.findViewById(R.id.barcode);
        itemNameView = itemView.findViewById(R.id.itemName);
        numberView = itemView.findViewById(R.id.number);
        uploadTimeView = itemView.findViewById(R.id.uploadTime);
        remarksView = itemView.findViewById(R.id.remarks);
        imageItemView1 = itemView.findViewById(R.id.imageItem1);
        imageItemView2 = itemView.findViewById(R.id.imageItem2);
        imageItemView3 = itemView.findViewById(R.id.imageItem3);
    }
    public void bindToPost(Post post, View.OnClickListener starClickListener) {
        barcodeView.setText(post.barcode);
        itemNameView.setText(post.name);
        numberView.setText(post.number);
        uploadTimeView.setText(post.location);
        remarksView.setText(post.remarks);
    }
}
