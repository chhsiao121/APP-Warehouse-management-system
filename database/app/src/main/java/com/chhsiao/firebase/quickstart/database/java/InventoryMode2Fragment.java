package com.chhsiao.firebase.quickstart.database.java;

import android.Manifest;
import android.app.AlertDialog;
import android.app.ProgressDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.pm.PackageManager;
import android.content.res.AssetManager;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.net.Uri;
import android.os.Bundle;

import androidx.activity.result.ActivityResultLauncher;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;
import androidx.fragment.app.Fragment;
import androidx.navigation.fragment.NavHostFragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.os.Environment;
import android.text.TextUtils;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.chhsiao.firebase.quickstart.database.java.capture.CaptureAct;
import com.chhsiao.firebase.quickstart.database.java.models.Location;
import com.chhsiao.firebase.quickstart.database.java.models.T22;
import com.chhsiao.firebase.quickstart.database.java.models.Task;
import com.chhsiao.firebase.quickstart.database.java.models.User;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.common.io.ByteStreams;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.google.firebase.quickstart.database.R;
import com.google.firebase.quickstart.database.databinding.FragmentInventoryMode2Binding;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;
import com.google.firebase.storage.StorageTask;
import com.google.firebase.storage.UploadTask;
import com.journeyapps.barcodescanner.ScanContract;
import com.journeyapps.barcodescanner.ScanOptions;

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
import java.util.Map;


public class InventoryMode2Fragment extends BaseFragment {
    private static final String REQUIRED = "Required";
    private static final String TAG = "InventoryMode2Fragment";
    ScanOptions options = new ScanOptions();
    private static final int PICK_CAMERA_REQUEST = 100;
    private FragmentInventoryMode2Binding binding;
    ArrayList<HashMap<String, String>> arrayList = new ArrayList<>();
    LinearLayoutManager mManager;
    private String scannType;
    private String modeId;
    private String taskId;
    private String locationId;
    private String json_name;
    private JSONObject jsonData;
    private Bitmap mImageBitmap1, mImageBitmap2, mImageBitmap3;
    Context context;
    MyListAdapter myListAdapter;
    String[] stateList = {"正常", "堪用", "破損", "待修", "建議報廢"};
    private StorageReference mStorageRef;
    private DatabaseReference mDatabase;
    private StorageTask mUploadTask;
    ProgressDialog progressDialog;
    public InventoryMode2Fragment() {
        // Required empty public constructor
    }


    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        context = getContext();
        if (ContextCompat.checkSelfPermission(getActivity(), Manifest.permission.CAMERA) != PackageManager.PERMISSION_GRANTED) {
            ActivityCompat.requestPermissions(getActivity(), new String[]{Manifest.permission.CAMERA}, PICK_CAMERA_REQUEST);
        }
        modeId = InventoryModeFragment.mode;
        taskId = InventoryTaskFragment.taskId;
        locationId = InventoryLocationFragment.locationId;
        json_name = modeId + "_" + taskId + "_" + locationId + ".json";
        jsonData = new JSONObject();
        checkJsonFile(json_name);
        Iterator<String> keys = (jsonData.keys());
        while (keys.hasNext()) {
            String currentDynamicKey = (String) keys.next();
            try {
                JSONObject currentDynamicValue = jsonData.getJSONObject(currentDynamicKey);
                HashMap<String, String> hashMap = new HashMap<>();
                hashMap.put("name", (String) currentDynamicValue.get("name"));
                hashMap.put("number", (String) currentDynamicValue.get("number"));
                hashMap.put("barcode", (String) currentDynamicValue.get("barcode"));
                hashMap.put("state", (String) currentDynamicValue.get("state"));
                hashMap.put("remark", (String) currentDynamicValue.get("remark"));
                hashMap.put("time", (String) currentDynamicValue.get("time"));
                hashMap.put("modeId", (String) currentDynamicValue.get("modeId"));
                hashMap.put("taskId", (String) currentDynamicValue.get("taskId"));
                hashMap.put("locationId", (String) currentDynamicValue.get("locationId"));
                hashMap.put("uploadImage1", (String) currentDynamicValue.get("uploadImage1"));
                hashMap.put("uploadImage2", (String) currentDynamicValue.get("uploadImage2"));
                hashMap.put("uploadImage3", (String) currentDynamicValue.get("uploadImage3"));
                arrayList.add(hashMap);
            } catch (JSONException e) {
                e.printStackTrace();
            }

        }
        progressDialog = new ProgressDialog(context);
        progressDialog.setMessage("Uploading Images\nPlease Wait....");
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        mStorageRef = FirebaseStorage.getInstance().getReference();
        mDatabase = FirebaseDatabase.getInstance().getReference();
        binding = FragmentInventoryMode2Binding.inflate(inflater, container, false);
        binding.listT22View.setHasFixedSize(true);
        mManager = new LinearLayoutManager(context);
        binding.listT22View.setLayoutManager(mManager);
        myListAdapter = new MyListAdapter();
        binding.listT22View.setAdapter(myListAdapter);

        return binding.getRoot();
    }
    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        options.setDesiredBarcodeFormats(ScanOptions.ALL_CODE_TYPES);
        options.setCaptureActivity(CaptureAct.class);
//        options.setPrompt("Scan a barcode");
        options.setCameraId(0);  // Use a specific camera of the device
        options.setOrientationLocked(false);
        options.setBeepEnabled(false);
        options.setBarcodeImageEnabled(false);
        ArrayAdapter<String> adapterState = new ArrayAdapter<String>(context, R.layout.list_item, stateList);
        binding.ItemStateTxt.setAdapter(adapterState);
        binding.scannerNumber.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                scannType = "number";
                barcodeLauncher.launch(options);
            }
        });

        binding.scannerBarcode.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                scannType = "barcode";
                barcodeLauncher.launch(options);
            }
        });
        binding.btnok.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                HashMap<String, String> hashMap = new HashMap<>();

                final String strName = binding.textInputLayoutName.getEditText().getText().toString();
                final String strNumber = binding.textInputLayoutNumber.getEditText().getText().toString();
                final String strBarcode = binding.textInputLayoutBarcode.getEditText().getText().toString();
                final String strState = binding.ItemStateTxt.getText().toString();
                String strRemark = binding.textInputLayoutRemark.getEditText().getText().toString();
                if (TextUtils.isEmpty(strName)) {
                    binding.textInputLayoutName.setError(REQUIRED);
                    return;
                } else {
                    binding.textInputLayoutName.setError(null);
                }
                if (TextUtils.isEmpty(strNumber)) {
                    binding.textInputLayoutNumber.setError(REQUIRED);
                    return;
                } else {
                    binding.textInputLayoutNumber.setError(null);
                }
                if (TextUtils.isEmpty(strBarcode)) {
                    binding.textInputLayoutBarcode.setError(REQUIRED);
                    return;
                } else {
                    binding.textInputLayoutBarcode.setError(null);
                }
                if (TextUtils.isEmpty(strState)) {
                    Toast.makeText(context, "請先選擇狀態!!", Toast.LENGTH_SHORT).show();
                    return;
                }
                if (TextUtils.isEmpty(strRemark)) {
                    strRemark = "";
                }
                Date dNow = new Date();
                SimpleDateFormat ft = new SimpleDateFormat("yyyy/MM/dd HH:mm");
                final String strTime = ft.format(dNow).toString();


                hashMap.put("name", strName);
                hashMap.put("number", strNumber);
                hashMap.put("barcode", strBarcode);
                hashMap.put("state", strState);
                hashMap.put("remark", strRemark);
                hashMap.put("time", strTime);
                hashMap.put("modeId", modeId);
                hashMap.put("taskId", taskId);
                hashMap.put("locationId", locationId);
                if (mImageBitmap1 != null) {
                    String newPicFile = System.currentTimeMillis() + ".png";
                    saveBitmap2Png(newPicFile, mImageBitmap1);
                    hashMap.put("uploadImage1", newPicFile);
                } else {
                    hashMap.put("uploadImage1", "");
                }
                if (mImageBitmap2 != null) {

                    String newPicFile = System.currentTimeMillis() + ".png";
                    saveBitmap2Png(newPicFile, mImageBitmap2);
                    hashMap.put("uploadImage2", newPicFile);
                } else {
                    hashMap.put("uploadImage2", "");
                }
                if (mImageBitmap3 != null) {
                    String newPicFile = System.currentTimeMillis() + ".png";
                    saveBitmap2Png(newPicFile, mImageBitmap3);
                    hashMap.put("uploadImage3", newPicFile);
                } else {
                    hashMap.put("uploadImage3", "");
                }
                arrayList.add(hashMap);
                JSONObject jsonObjectNewTask = new JSONObject(hashMap);
                try {
                    jsonData.put(strBarcode, jsonObjectNewTask);
                } catch (JSONException e) {
                    e.printStackTrace();
                }
                saveJson2File(json_name, jsonData);

                myListAdapter.notifyDataSetChanged();
                refresshAll();

            }
        });
        binding.refreshLayout.setColorSchemeColors(getResources().getColor(R.color.material_cyan_a700));
        binding.refreshLayout.setOnRefreshListener(() -> {
            myListAdapter.notifyDataSetChanged();
            binding.refreshLayout.setRefreshing(false);
        });
        binding.btnUpload.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if (!networkIsConnect()) {
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
                } else {
                    submitPost();
                    submitLocation();
                    submitTask();
                    NavHostFragment.findNavController(InventoryMode2Fragment.this)
                            .navigate(R.id.action_InventoryMode2Fragment_to_InventoryModeFragment);
                }
            }
        });
    }
    private void submitPost() {
        for (HashMap<String, String> onePost : arrayList) {
            final String name = onePost.get("name");
            final String number = onePost.get("number");
            final String time = onePost.get("time");
            final String remark = onePost.get("remark");
            final String state = onePost.get("state");
            final String barcode = onePost.get("barcode");
            final String modeId = onePost.get("modeId");
            final String locationId = onePost.get("locationId");
            final String taskId = onePost.get("taskId");
            final String uploadFileName1 = onePost.get("uploadImage1");
            final String uploadFileName2 = onePost.get("uploadImage2");
            final String uploadFileName3 = onePost.get("uploadImage3");


//            Toast.makeText(getContext(), "Posting...", Toast.LENGTH_SHORT).show();
            final String userId = getUid();
            mDatabase.child("users").child(userId).addListenerForSingleValueEvent(
                    new ValueEventListener() {
                        @Override
                        public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                            // Get user value
                            User user = dataSnapshot.getValue(User.class);
                            if (user == null) {
                                // User is null, error out
                                Log.e(TAG, "User " + userId + " is unexpectedly null");
                                Toast.makeText(getContext(),
                                        "Error: could not fetch user.",
                                        Toast.LENGTH_SHORT).show();
                            } else {
                                // Write new post
                                writeNewT22Post(userId, user.username, name, number, time, remark, state, barcode, modeId, locationId, taskId, uploadFileName1, uploadFileName2, uploadFileName3);
//
                            }
                        }
                        @Override
                        public void onCancelled(@NonNull DatabaseError databaseError) {
                            Log.w(TAG, "getUser:onCancelled", databaseError.toException());
                        }
                    });
        }
    }
    private void writeNewT22Post(String userId, String username, String name, String number, String time, String remarks, String state, String barcode,
                                 String modeId, String taskId, String locationId, String uploadFileName1, String uploadFileName2, String uploadFileName3) {
        // Create new post at /user-posts/$userid/$postid and at
        // /posts/$postid simultaneously
        String key = mDatabase.child("posts").child(modeId).child(taskId).child(locationId).push().getKey();
        T22 t22 = new T22(userId, username, name, number, time, remarks, state, barcode, modeId, taskId, locationId, uploadFileName1, uploadFileName2, uploadFileName3);
        Map<String, Object> postValues = t22.toMap();
        Map<String, Object> childUpdates = new HashMap<>();
        childUpdates.put("/posts/" + modeId + "/" + taskId + "/" + locationId + "/" + key, postValues);
        childUpdates.put("/user-posts/" +userId+"/"+ modeId + "/" + taskId + "/" + locationId  + "/" + key, postValues);
        mDatabase.updateChildren(childUpdates);
    }
    private void submitLocation() {
        String jsonLocationName = getLocationJson();
        JSONObject jsonLocationData = null;
        try {
            jsonLocationData = new JSONObject(readJsonFromPhone(jsonLocationName));
        } catch (JSONException e) {
            e.printStackTrace();
        }
        Iterator<String> keys = jsonLocationData.keys();
        while (keys.hasNext()) {
            String currentDynamicKey = (String) keys.next();
            try {
                if (currentDynamicKey.equals(locationId)) {
                    JSONObject currentDynamicValue = jsonLocationData.getJSONObject(currentDynamicKey);
                    String id = (String) currentDynamicValue.get("id");
                    String name = (String) currentDynamicValue.get("name");
                    String time = (String) currentDynamicValue.get("time");
                    writeNewLocation(id, name, time);
                }
            } catch (JSONException e) {
                e.printStackTrace();
            }
        }
    }
    private void writeNewLocation(String locationId, String name, String time) {
        Location location = new Location(locationId, name, time);
        mDatabase.child("locations").child(locationId).setValue(location);
    }
    private void submitTask() {
        String jsonTaskName = getTaskJson();
        JSONObject jsonTaskData = null;
        try {
            jsonTaskData = new JSONObject(readJsonFromPhone(jsonTaskName));
        } catch (JSONException e) {
            e.printStackTrace();
        }
        Iterator<String> keys = jsonTaskData.keys();
        while (keys.hasNext()) {
            String currentDynamicKey = (String) keys.next();
            try {
                if (currentDynamicKey.equals(taskId)) {
                    JSONObject currentDynamicValue = jsonTaskData.getJSONObject(currentDynamicKey);
                    String id = (String) currentDynamicValue.get("id");
                    String name = (String) currentDynamicValue.get("name");
                    String time = (String) currentDynamicValue.get("time");
                    writeNewTask(id, name, time);
                }
            } catch (JSONException e) {
                e.printStackTrace();
            }
        }
    }

    private void writeNewTask(String taskId, String name, String time) {
        Task task = new Task(taskId, name, time);
        mDatabase.child("tasks").child(taskId).setValue(task);
    }

    private void refresshAll() {
        binding.textInputLayoutName.getEditText().setText("");
        binding.textInputLayoutNumber.getEditText().setText("");
        binding.textInputLayoutBarcode.getEditText().setText("");
        binding.textInputLayoutRemark.getEditText().setText("");
        mImageBitmap1 = null;
        mImageBitmap2 = null;
        mImageBitmap3 = null;
    }

    private final ActivityResultLauncher<ScanOptions> barcodeLauncher = registerForActivityResult(new ScanContract(),
            result -> {
                if (result.getContents() != null) {
                    if (scannType.equals("number")) {
                        binding.textInputLayoutNumber.getEditText().setText(result.getContents());
                    } else if (scannType.equals("barcode")) {
                        binding.textInputLayoutBarcode.getEditText().setText(result.getContents());
                    }
                } else {
                    Toast.makeText(getActivity(), "No Results", Toast.LENGTH_SHORT).show();
                }
            });


    private void saveBitmap2Png(String bitName, Bitmap mBitmap) {
        File path = getActivity().getExternalFilesDir(Environment.DIRECTORY_PICTURES);
        File f = new File(path, bitName);
        try (FileOutputStream out = new FileOutputStream(f)) {
            mBitmap.compress(Bitmap.CompressFormat.PNG, 100, out); // bmp is your Bitmap instance
            // PNG is a lossless format, the compression factor (100) is ignored
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
    private void checkJsonFile(String target_name) {//1.建立json檔(如果json檔不存在)
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

    private boolean networkIsConnect() {
        ConnectivityManager connectivityManager = (ConnectivityManager) getActivity().getSystemService(Context.CONNECTIVITY_SERVICE);
        NetworkInfo networkInfo = connectivityManager.getActiveNetworkInfo();
        if (networkInfo != null) {
            return networkInfo.isConnected();
        } else {
            return false;
        }
    }
    private class MyListAdapter extends RecyclerView.Adapter<MyListAdapter.ViewHolder> {

        @NonNull
        @Override
        public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
            View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_post_t22, parent, false);
            return new ViewHolder(view);
        }

        @Override
        public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
            holder.barcode.setText(arrayList.get(position).get("barcode"));
            holder.name.setText(arrayList.get(position).get("name"));
            holder.number.setText(arrayList.get(position).get("number"));
            String time = arrayList.get(position).get("time");
            time = time.split("/")[1] + "/" + time.split("/")[2];
            holder.uploadTime.setText(time);
            holder.remarks.setText(arrayList.get(position).get("remark"));
            holder.mView.setOnClickListener((v) -> {
//                Toast.makeText(context,holder.name.getText(),Toast.LENGTH_SHORT).show();
//                locationId = holder.id.getText().toString();
            });
        }

        @Override
        public int getItemCount() {
            return arrayList.size();
        }
        class ViewHolder extends RecyclerView.ViewHolder {
            private TextView barcode, name, number, uploadTime, remarks;

            private View mView;

            public ViewHolder(@NonNull View itemView) {
                super(itemView);
                barcode = itemView.findViewById(R.id.barcode);
                name = itemView.findViewById(R.id.itemName);
                number = itemView.findViewById(R.id.number);
                uploadTime = itemView.findViewById(R.id.uploadTime);
                remarks = itemView.findViewById(R.id.remarks);
                mView = itemView;
            }
        }
    }
}