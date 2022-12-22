package com.chhsiao.firebase.quickstart.database.java;

import android.Manifest;
import android.content.Context;
import android.content.pm.PackageManager;
import android.content.res.AssetManager;
import android.os.Bundle;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;
import androidx.fragment.app.Fragment;

import android.os.Environment;
import android.text.TextUtils;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.material.bottomsheet.BottomSheetDialog;
import com.google.android.material.textfield.TextInputLayout;
import com.google.firebase.quickstart.database.R;
import com.google.firebase.quickstart.database.databinding.FragmentInventoryLocationBinding;
import com.google.common.io.ByteStreams;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.FileWriter;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.Iterator;


public class InventoryLocationFragment extends BaseFragment {

    private static final String REQUIRED = "Required";
    Context context;
    FragmentInventoryLocationBinding binding;
    MyListAdapter myListAdapter;
    RecyclerView mRecyclerView;
    LinearLayoutManager mManager;
    private JSONObject jsonData;
    private String mode;
    private String taskID;
    ArrayList<HashMap<String,String>> arrayList = new ArrayList<>();
    String json_name;
    public InventoryLocationFragment() {
        // Required empty public constructor
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        context = getContext();
        jsonData = new JSONObject();
        json_name = getLocationJson();
        checkJsonFile(json_name);
        Iterator<String> keys = jsonData.keys();
        while(keys.hasNext()) {
            String currentDynamicKey = (String)keys.next();
            try {
                JSONObject currentDynamicValue = jsonData.getJSONObject(currentDynamicKey);
                HashMap<String,String> hashMap = new HashMap<>();
                hashMap.put("id", (String) currentDynamicValue.get("id"));
                hashMap.put("name", (String) currentDynamicValue.get("name"));
                hashMap.put("time", (String) currentDynamicValue.get("time"));

                arrayList.add(hashMap);
            } catch (JSONException e) {
                e.printStackTrace();
            }

        }
    }
    private class MyListAdapter extends RecyclerView.Adapter<MyListAdapter.ViewHolder>{
        class ViewHolder extends RecyclerView.ViewHolder{
            private TextView id,name,time;
            private View mView;
            public ViewHolder(@NonNull View itemView) {
                super(itemView);
                id = itemView.findViewById(R.id.locationID);
                name = itemView.findViewById(R.id.locationName);
                time = itemView.findViewById(R.id.locationTime);
                mView = itemView;
            }
        }
        @NonNull
        @Override
        public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
            View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_location,parent,false);
            return new ViewHolder(view);
        }

        @Override
        public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
            holder.id.setText(arrayList.get(position).get("id"));
            holder.name.setText(arrayList.get(position).get("name"));
            holder.time.setText(arrayList.get(position).get("time"));
            holder.mView.setOnClickListener((v)->{
//                Toast.makeText(context,holder.name.getText(),Toast.LENGTH_SHORT).show();
                Toast.makeText(context,"mode:"+mode+"\n"+"taskID:"+taskID+"\n",Toast.LENGTH_SHORT).show();
            });
        }

        @Override
        public int getItemCount() {
            return arrayList.size();
        }
    }
    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        mode = requireArguments().getString("mode");
        taskID = requireArguments().getString("task_id");
        binding = FragmentInventoryLocationBinding.inflate(inflater, container, false);
        //設置RecycleView
        mManager = new LinearLayoutManager(context);
        binding.recyclerviewLocation.setLayoutManager(mManager);
        myListAdapter = new MyListAdapter();
        binding.recyclerviewLocation.setAdapter(myListAdapter);
        return binding.getRoot();
    }
    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        binding.btnNewLocation.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                BottomSheetDialog bottomSheetDialog = new BottomSheetDialog(context,R.style.BottomSheetDialog);
                View view = LayoutInflater.from(context).inflate(R.layout.bottom_sheet_location,null);
                TextInputLayout textID = view.findViewById(R.id.textInputLayoutID);
                TextInputLayout textName = view.findViewById(R.id.textInputLayoutName);
                TextInputLayout textTime = view.findViewById(R.id.textInputLayoutTime);
                Button btnOK = view.findViewById(R.id.btnOK);
                bottomSheetDialog.setContentView(view);
                ViewGroup parent = (ViewGroup) view.getParent();//取得BottomSheet介面設定
                parent.setBackgroundResource(android.R.color.transparent);//將背景設為透明，否則預設白底
                bottomSheetDialog.show();
                textTime.setEnabled(false);
                Date dNow = new Date( );
                SimpleDateFormat ft = new SimpleDateFormat ("yyyy/MM/dd HH:mm");
                textTime.getEditText().setText(ft.format(dNow).toString());
                btnOK.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        final String strID = textID.getEditText().getText().toString();
                        final String strName = textName.getEditText().getText().toString();
                        final String strTime = textTime.getEditText().getText().toString();
                        if (TextUtils.isEmpty(strID)) {
                            textID.setError(REQUIRED);
                        }
                        if (TextUtils.isEmpty(strName)) {
                            textName.setError(REQUIRED);
                            return;
                        }
                        if (TextUtils.isEmpty(strTime)) {
                            textTime.setError(REQUIRED);
                        }
                        else{
                            HashMap<String,String> hashMap = new HashMap<>();
                            hashMap.put("id",strID);
                            hashMap.put("time",strTime);
                            hashMap.put("name",strName);
                            arrayList.add(hashMap);
                            JSONObject jsonObjectNewLocation = new JSONObject(hashMap);
                            try {
                                jsonData.put(strID,jsonObjectNewLocation);
                            } catch (JSONException e) {
                                e.printStackTrace();
                            }
                            saveJson2File(json_name,jsonData);
                            bottomSheetDialog.dismiss();
//                            Toast.makeText(getContext(), "添加", Toast.LENGTH_SHORT).show();
                            myListAdapter.notifyDataSetChanged();

                        }
                    }
                });
            }
        });
        binding.refreshLayout.setColorSchemeColors(getResources().getColor(R.color.colorPrimary));
        binding.refreshLayout.setOnRefreshListener(()->{
            myListAdapter.notifyDataSetChanged();
            binding.refreshLayout.setRefreshing(false);

        });
    }
    private void checkJsonFile(String target_name){//1.建立json檔(如果json檔不存在)
        int READ_EXTERNAL_STORAGE = 100;
        if (ContextCompat.checkSelfPermission(getActivity(), Manifest.permission.READ_EXTERNAL_STORAGE) != PackageManager.PERMISSION_GRANTED)
            ActivityCompat.requestPermissions(getActivity(), new String[]{Manifest.permission.READ_EXTERNAL_STORAGE}, READ_EXTERNAL_STORAGE);

        if (!hasExternalStoragePrivateJson(target_name)) {
            createExternalStoragePrivateJson(target_name);
        } else {
            try {
                jsonData = new JSONObject(readJsonFromPhone(target_name));
            } catch (JSONException e) {
                e.printStackTrace();
            }
        }
    }
    public String readJsonFromPhone(String filename) {
        File path = getActivity().getExternalFilesDir(Environment.DIRECTORY_DOCUMENTS);
        File file = new File(path, filename);
        String line = "";
        try {
            FileInputStream fileInputStream = new FileInputStream(file);
            line = new String(ByteStreams.toByteArray(fileInputStream));
        } catch (IOException e) {
            e.printStackTrace();
        }
        return line;
    }
    boolean hasExternalStoragePrivateJson(String filename) {
        // Create a path where we will place our picture in the user's
        // public pictures directory and check if the file exists.  If
        // external storage is not currently mounted this will think the
        // picture doesn't exist.
        File path = requireActivity().getExternalFilesDir(Environment.DIRECTORY_DOCUMENTS);
        if (path != null) {
            File file = new File(path, filename);
            return file.exists();
        }
        return false;
    }
    void createExternalStoragePrivateJson(String filename) {
        File path = getActivity().getExternalFilesDir(Environment.DIRECTORY_DOCUMENTS);
        AssetManager assetManager = context.getAssets();
        File file = new File(path, filename);
        try {
            InputStream is = assetManager.open("new.json");
            OutputStream os = new FileOutputStream(file);
            byte[] data = new byte[is.available()];
            is.read(data);
            os.write(data);
            is.close();
            os.close();
        } catch (IOException ignored) {
        }
    }
    public void saveJson2File(String filename, @NonNull JSONObject JsonObject) {
        String userString = JsonObject.toString();
        File path = getActivity().getExternalFilesDir(Environment.DIRECTORY_DOCUMENTS);
        File file = new File(path, filename);
        FileWriter fileWriter;
        try {
            fileWriter = new FileWriter(file);
            BufferedWriter bufferedWriter = new BufferedWriter(fileWriter);
            bufferedWriter.write(userString);
            bufferedWriter.close();
            Toast.makeText(context.getApplicationContext(), "新增保存成功", Toast.LENGTH_LONG).show();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}