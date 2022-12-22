/*
 * Copyright 2015 Google Inc. All Rights Reserved.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.chhsiao.firebase.quickstart.database.java;

import android.os.Bundle;
import android.view.View;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.navigation.NavController;
import androidx.navigation.NavDestination;
import androidx.navigation.Navigation;

import com.google.android.material.floatingactionbutton.FloatingActionButton;
import com.google.firebase.quickstart.database.R;
import com.google.firebase.quickstart.database.databinding.ActivityMainBinding;

public class  MainActivity extends AppCompatActivity {

    private FloatingActionButton fab;
    private NavController navController;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        ActivityMainBinding binding = ActivityMainBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());
        setSupportActionBar(binding.toolbar);

        fab = binding.fab;
        navController = Navigation.findNavController(this, R.id.nav_host_fragment);
        navController.setGraph(R.navigation.nav_graph_java);
        navController.addOnDestinationChangedListener(new NavController.OnDestinationChangedListener() {
            @Override
            public void onDestinationChanged(@NonNull NavController controller, @NonNull NavDestination destination, @Nullable Bundle arguments) {
                boolean hideFAB;
                if (arguments != null && arguments.containsKey("hideFAB")) {
                    hideFAB = arguments.getBoolean("hideFAB");
                }
                else {
                    hideFAB = false;
                }
                if (destination.getId() == R.id.MainFragment) {
                    if(hideFAB){
                        fab.setVisibility(View.GONE);
                    }
                    else{
                        fab.setVisibility(View.VISIBLE);
                        fab.setOnClickListener(new View.OnClickListener() {
                            @Override
                            public void onClick(View view) {
                                navController.navigate(R.id.action_MainFragment_to_NewPostFragment);
                            }
                        });
                    }
                } else {
                    fab.setVisibility(View.GONE);
                }
                if(destination.getId() == R.id.InventoryModeFragment){
                    binding.toolbar.setTitle("專案");
                    setSupportActionBar(binding.toolbar);
                }
                if(destination.getId() == R.id.InventoryTaskFragment){
                    if (arguments != null && arguments.containsKey("name") && arguments.containsKey("id")) {
                        String name = arguments.getString("name");
                        String id = arguments.getString("id");
                        binding.toolbar.setTitle(name);
                        binding.toolbar.setSubtitle(id);
                        setSupportActionBar(binding.toolbar);
                    }
                }
                if(destination.getId() == R.id.InventoryLocationFragment){
                    binding.toolbar.setTitle("盤點位置");
                    binding.toolbar.setSubtitle(null);
                    setSupportActionBar(binding.toolbar);
                }

            }
        });
    }
}
