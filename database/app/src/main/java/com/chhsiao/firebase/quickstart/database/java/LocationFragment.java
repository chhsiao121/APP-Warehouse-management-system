package com.chhsiao.firebase.quickstart.database.java;

import android.content.Context;
import android.os.Bundle;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.navigation.NavController;
import androidx.navigation.Navigation;
import androidx.navigation.fragment.NavHostFragment;

import android.text.TextUtils;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.Toast;

import com.google.firebase.quickstart.database.R;
import com.google.firebase.quickstart.database.databinding.FragmentLocationBinding;

import java.util.ArrayList;


public class LocationFragment extends BaseFragment {
    private FragmentLocationBinding binding;
    private ArrayList<String> arrayList_localtion;
    private ArrayAdapter<String> arrayAdapter_localtion;
    private static final String REQUIRED = "Required";
    Context context;
    String location;
    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        binding = FragmentLocationBinding.inflate(inflater, container, false);
        context = getContext();
        return binding.getRoot();
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        arrayList_localtion = new ArrayList<>();

        binding.btnNewlocation.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                location = binding.textInputLayoutLocation.getEditText().getText().toString();
                if (TextUtils.isEmpty(location)) {
                    binding.textInputLayoutLocation.setError(REQUIRED);
                    return;
                }
                for(String i : arrayList_localtion){
                    if(i.equals(location)){
                        Toast.makeText(context,"請勿輸入已存在地點",Toast.LENGTH_SHORT).show();
                    }
                }
                arrayList_localtion.add(location);
                arrayAdapter_localtion = new ArrayAdapter<>(context,
                        R.layout.support_simple_spinner_dropdown_item,arrayList_localtion);
                binding.actLocations.setAdapter(arrayAdapter_localtion);
                binding.actLocations.setThreshold(1);

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
    }
}