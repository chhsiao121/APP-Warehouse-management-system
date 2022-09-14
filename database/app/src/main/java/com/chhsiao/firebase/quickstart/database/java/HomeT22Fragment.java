package com.chhsiao.firebase.quickstart.database.java;

import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.os.Bundle;

import androidx.activity.result.ActivityResultLauncher;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.navigation.fragment.NavHostFragment;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Toast;

import com.google.firebase.quickstart.database.R;
import com.google.firebase.quickstart.database.databinding.FragmentHomeT22Binding;


public class HomeT22Fragment extends BaseFragment {
    public static String proLocation;
    private FragmentHomeT22Binding binding;
    Context context;

    public HomeT22Fragment() {
        // Required empty public constructor
    }



    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        context = getContext();
        proLocation = requireArguments().getString("location");
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        binding = FragmentHomeT22Binding.inflate(inflater, container, false);
        return binding.getRoot();
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        binding.btnStartInventory.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                Bundle args = new Bundle();
                args.putString("location", proLocation);
                NavHostFragment.findNavController(HomeT22Fragment.this)
                        .navigate(R.id.action_HomeT22Fragment_to_InventoryFragment,args);
            }
        });
    }


}