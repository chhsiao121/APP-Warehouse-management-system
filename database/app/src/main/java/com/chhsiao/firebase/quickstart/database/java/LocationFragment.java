package com.chhsiao.firebase.quickstart.database.java;

import static android.content.ContentValues.TAG;

import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.os.Bundle;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.navigation.NavController;
import androidx.navigation.Navigation;
import androidx.navigation.fragment.NavHostFragment;

import android.text.TextUtils;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.Toast;

import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.Query;
import com.google.firebase.database.ValueEventListener;
import com.google.firebase.quickstart.database.R;
import com.google.firebase.quickstart.database.databinding.FragmentLocationBinding;

import java.util.ArrayList;


public class LocationFragment extends BaseFragment {
    private FragmentLocationBinding binding;
    private ArrayList<String> arrayList_localtion;
    private ArrayAdapter<String> arrayAdapter_localtion;
    private static final String REQUIRED = "Required";
    private DatabaseReference mDatabase;
    Context context;
    String location;
    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        context = getContext();
        if(!networkIsConnect()){
            AlertDialog.Builder builder = new AlertDialog.Builder(context);
            builder.setMessage("請開啟網路再使用此APP");
            builder.setTitle("網路連線發生問題");
            builder.setPositiveButton("收到!", new DialogInterface.OnClickListener() {
                @Override
                public void onClick(DialogInterface dialog, int which) {
                }
            });
            AlertDialog dialog = builder.create();
            dialog.show();
        }
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        binding = FragmentLocationBinding.inflate(inflater, container, false);

        mDatabase = FirebaseDatabase.getInstance().getReference().child("location_array");
        arrayList_localtion = new ArrayList<>();
        return binding.getRoot();
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        ValueEventListener postListener = new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                // Get Post object and use the values to update the UI
                arrayList_localtion = (ArrayList) dataSnapshot.getValue();
                if(arrayList_localtion != null){
                    arrayAdapter_localtion = new ArrayAdapter<>(context,
                        R.layout.support_simple_spinner_dropdown_item,arrayList_localtion);
                    binding.actLocations.setAdapter(arrayAdapter_localtion);
                    binding.actLocations.setThreshold(1);
                }
                // ..
            }

            @Override
            public void onCancelled(DatabaseError databaseError) {
                // Getting Post failed, log a message
                Log.w(TAG, "loadPost:onCancelled", databaseError.toException());
            }
        };
        mDatabase.addValueEventListener(postListener);

        binding.btnNewlocation.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                location = binding.textInputLayoutLocation.getEditText().getText().toString();
                if (TextUtils.isEmpty(location)) {
                    binding.textInputLayoutLocation.setError(REQUIRED);
                    return;
                }
                if(arrayList_localtion!=null){
                    for(String i : arrayList_localtion){
                        if(i.equals(location)){
                            Toast.makeText(context,"請勿輸入已存在地點",Toast.LENGTH_SHORT).show();
                        }
                    }
                    mDatabase.child(String.valueOf(arrayList_localtion.size())).setValue(location).addOnSuccessListener(new OnSuccessListener<Void>() {
                        @Override
                        public void onSuccess(Void unused) {
                            Toast.makeText(context,"新增盤點地點成功",Toast.LENGTH_SHORT).show();
                        }
                    }).addOnFailureListener(new OnFailureListener() {
                        @Override
                        public void onFailure(@NonNull Exception e) {
                            Toast.makeText(context,"新增盤點地點失敗QQ",Toast.LENGTH_SHORT).show();
                        }
                    });
                }
                else {
                    mDatabase.child("0").setValue(location).addOnSuccessListener(new OnSuccessListener<Void>() {
                        @Override
                        public void onSuccess(Void unused) {
                            Toast.makeText(context,"新增盤點地點成功",Toast.LENGTH_SHORT).show();
                        }
                    }).addOnFailureListener(new OnFailureListener() {
                        @Override
                        public void onFailure(@NonNull Exception e) {
                            Toast.makeText(context,"新增盤點地點失敗QQ",Toast.LENGTH_SHORT).show();
                        }
                    });
                }

            }
        });

        binding.fabgo.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                String go_location = binding.actLocations.getText().toString();
                if(go_location.equals("")){
                    Toast.makeText(context,"請選擇盤點地點!!",Toast.LENGTH_SHORT).show();
                }
                else{
                    Bundle args = new Bundle();
                    args.putString("location", go_location);
                    NavHostFragment.findNavController(LocationFragment.this)
                            .navigate(R.id.action_LocationFragment_to_MainFragment,args);
                }
            }
        });

        binding.btnVersion2.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                String go_location = binding.actLocations.getText().toString();
                if(go_location.equals("")){
                    Toast.makeText(context,"請選擇盤點地點!!",Toast.LENGTH_SHORT).show();
                }
                else{
                    Bundle args = new Bundle();
                    args.putString("location", go_location);
                    NavHostFragment.findNavController(LocationFragment.this)
                            .navigate(R.id.action_LocationFragment_to_HomeT22Fragment,args);
                }
            }
        });
    }

    private boolean networkIsConnect(){
        ConnectivityManager connectivityManager = (ConnectivityManager) getActivity().getSystemService(Context.CONNECTIVITY_SERVICE);
        NetworkInfo networkInfo = connectivityManager.getActiveNetworkInfo();
        if (networkInfo != null){
            return networkInfo.isConnected();
        }else {
            return false;
        }
    }
}