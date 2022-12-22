package com.chhsiao.firebase.quickstart.database.java;

import android.os.Bundle;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.navigation.fragment.NavHostFragment;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.google.firebase.quickstart.database.R;
import com.google.firebase.quickstart.database.databinding.FragmentInventoryModeBinding;


public class InventoryModeFragment extends BaseFragment {

    FragmentInventoryModeBinding binding;


    public InventoryModeFragment() {
        // Required empty public constructor
    }


    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        binding = FragmentInventoryModeBinding.inflate(inflater, container, false);

        return binding.getRoot();
    }
    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        binding.btnMode1.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                String id = binding.mode1ID.getText().toString();
                String name = binding.mode1Name.getText().toString();
                Bundle args = new Bundle();
                args.putString("id", id);
                args.putString("name", name);
                NavHostFragment.findNavController(InventoryModeFragment.this)
                        .navigate(R.id.action_InventoryModeFragment_to_InventoryTaskFragment,args);
            }
        });
        binding.btnMode2.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                NavHostFragment.findNavController(InventoryModeFragment.this)
                        .navigate(R.id.action_InventoryModeFragment_to_LocationFragment);
            }
        });
    }
}