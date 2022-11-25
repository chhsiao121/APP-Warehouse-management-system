package com.chhsiao.firebase.quickstart.database.java;

import android.os.Bundle;

import androidx.activity.OnBackPressedCallback;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.navigation.fragment.NavHostFragment;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.CompoundButton;
import android.widget.Toast;


import com.google.firebase.quickstart.database.R;
import com.google.firebase.quickstart.database.databinding.FragmentNewInventoryBinding;



public class NewInventoryFragment extends BaseFragment {
    FragmentNewInventoryBinding binding;
    public static String proLocation;
    Boolean fieldName = true;
    public NewInventoryFragment() {
        // Required empty public constructor
    }



    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        //返回建功能
        OnBackPressedCallback callback = new OnBackPressedCallback(true /* enabled by default */) {
            @Override
            public void handleOnBackPressed() {
                // Handle the back button event
                Bundle args = new Bundle();
                args.putString("location", proLocation);
                args.putBoolean("fieldName", fieldName);
                NavHostFragment.findNavController(NewInventoryFragment.this)
                        .navigate(R.id.action_NewInventoryFragment_to_HomeT22Fragment,args);
            }
        };
        requireActivity().getOnBackPressedDispatcher().addCallback(this, callback);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        binding = FragmentNewInventoryBinding.inflate(inflater, container, false);
        proLocation = requireArguments().getString("location");
        // Inflate the layout for this fragment
        return binding.getRoot();

    }
    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        binding.switchName.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                fieldName = isChecked;
//                Toast.makeText(getContext(),String.valueOf(isChecked),Toast.LENGTH_SHORT).show();
            }
        });
//        if(binding.switchName.isChecked()){
//            Toast.makeText(getContext(),"ON",Toast.LENGTH_SHORT).show();
//        }
//        else{
//            Toast.makeText(getContext(),"OFF",Toast.LENGTH_SHORT).show();
//        }
    }
}