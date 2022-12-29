package com.chhsiao.firebase.quickstart.database.java;

import android.Manifest;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.content.res.AssetManager;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.net.Uri;
import android.os.Bundle;

import androidx.activity.result.ActivityResultLauncher;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.os.Environment;
import android.provider.MediaStore;
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
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.common.io.ByteStreams;
import com.google.firebase.quickstart.database.R;
import com.google.firebase.quickstart.database.databinding.FragmentInventoryMode1Binding;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;
import com.google.firebase.storage.StorageTask;
import com.google.firebase.storage.UploadTask;
import com.journeyapps.barcodescanner.ScanContract;
import com.journeyapps.barcodescanner.ScanOptions;

import org.json.JSONException;
import org.json.JSONObject;

import java.io.BufferedWriter;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.FileWriter;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Objects;


public class InventoryMode1Fragment extends BaseFragment {
    private static final String REQUIRED = "Required";
    private static final String TAG = "InventoryMode1Fragment";
    ScanOptions options = new ScanOptions();
    private static final int PICK_CAMERA_REQUEST = 100;
    private Bitmap mImageBitmap1, mImageBitmap2, mImageBitmap3;
    String mCameraFileName1, mCameraFileName2, mCameraFileName3;
    Uri imageUri1, imageUri2, imageUri3;
    private FragmentInventoryMode1Binding binding;
    ArrayList<HashMap<String, String>> arrayList = new ArrayList<>();
    private RecyclerView mRecycler;
    private StorageReference mStorageRef;
    LinearLayoutManager mManager;
    private String scannType;
    private String modeId;
    private String taskId;
    private String locationId;
    private String json_name;
    private JSONObject jsonData;
    MyListAdapter myListAdapter;
    Context context;
    private int image123;
    String[] stateList = {"正常", "堪用", "破損", "待修", "建議報廢"};
    private StorageTask mUploadTask;

    public InventoryMode1Fragment() {
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

    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        mStorageRef = FirebaseStorage.getInstance().getReference();
        binding = FragmentInventoryMode1Binding.inflate(inflater, container, false);


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


        binding.btnPhotograph1.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                image123 = 1;
                Intent intent = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);
                startActivityForResult(intent, PICK_CAMERA_REQUEST);
            }
        });
        binding.btnPhotograph2.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                image123 = 2;
                Intent intent = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);
                startActivityForResult(intent, PICK_CAMERA_REQUEST);
            }
        });
        binding.btnPhotograph3.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                image123 = 3;
                Intent intent = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);
                startActivityForResult(intent, PICK_CAMERA_REQUEST);
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

            }
        });
        submitPost();
    }
    private void submitPost() {
//        for (HashMap<String, String> onePost:arrayList){
        HashMap<String, String> onePost = arrayList.get(0);
            final String name = onePost.get("name");
            final String number = onePost.get("number");
            final String time = onePost.get("time");
            final String remark = onePost.get("remark");
            final String state = onePost.get("state");
            final String barcode = onePost.get("barcode");
            final String modeId = onePost.get("modeId");
            final String locationId = onePost.get("locationId");
            final String taskId = onePost.get("taskId");
            if (mUploadTask != null && mUploadTask.isInProgress()) {
                Toast.makeText(getContext(), "Upload in progress", Toast.LENGTH_SHORT).show();
            } else {
                File path = getActivity().getExternalFilesDir(Environment.DIRECTORY_PICTURES);
                File imgFile1 = new File(path, Objects.requireNonNull(onePost.get("uploadImage1")));

                if (imgFile1.exists()) {
                    Bitmap myBitmap = BitmapFactory.decodeFile(imgFile1.getAbsolutePath());
                    uploadImage(imgFile1);
                }
                File imgFile2 = new File(path, Objects.requireNonNull(onePost.get("uploadImage2")));
                if (imgFile2.exists()) {
                    uploadImage(imgFile2);

                }
                File imgFile3 = new File(path, Objects.requireNonNull(onePost.get("uploadImage3")));
                if (imgFile3.exists()) {
                    uploadImage(imgFile3);

                }
            }
//        }
    }
    private void uploadImage(File imgFile){
        Bitmap myBitmap = BitmapFactory.decodeFile(imgFile.getAbsolutePath());
        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        myBitmap.compress(Bitmap.CompressFormat.PNG, 100, baos);
        byte[] imageByte = baos.toByteArray();
        StorageReference fileReference = mStorageRef.child(imgFile.getName());
        mUploadTask = fileReference.putBytes(imageByte);
        // Register observers to listen for when the download is done or if it fails
        mUploadTask.addOnFailureListener(new OnFailureListener() {
            @Override
            public void onFailure(@NonNull Exception e) {
                Toast.makeText(getContext(), "image upload failure", Toast.LENGTH_SHORT).show();
            }
        }).addOnSuccessListener(new OnSuccessListener<UploadTask.TaskSnapshot>() {
            @Override
            public void onSuccess(UploadTask.TaskSnapshot taskSnapshot) {
                Log.e(TAG, "image upload success: ");
            }
        });

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

    @Override
    public void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == PICK_CAMERA_REQUEST) {
//            mImageUri = data.getData();
            if (data.getExtras().get("data") != null) {
                if (image123 == 1) {
                    mImageBitmap1 = (Bitmap) data.getExtras().get("data");
                    binding.btnPhotograph1.setImageBitmap(mImageBitmap1);
                } else if (image123 == 2) {
                    mImageBitmap2 = (Bitmap) data.getExtras().get("data");
                    binding.btnPhotograph2.setImageBitmap(mImageBitmap2);
                } else if (image123 == 3) {
                    mImageBitmap3 = (Bitmap) data.getExtras().get("data");
                    binding.btnPhotograph3.setImageBitmap(mImageBitmap3);
                }
            }
        }
    }

    private void refresshAll() {
        binding.textInputLayoutName.getEditText().setText("");
        binding.textInputLayoutNumber.getEditText().setText("");
        binding.textInputLayoutBarcode.getEditText().setText("");
        binding.textInputLayoutRemark.getEditText().setText("");
        mImageBitmap1 = null;
        binding.btnPhotograph1.setImageResource(R.drawable.ic_baseline_camera_alt_30);
        mImageBitmap2 = null;
        binding.btnPhotograph2.setImageResource(R.drawable.ic_baseline_camera_alt_30);
        mImageBitmap3 = null;
        binding.btnPhotograph3.setImageResource(R.drawable.ic_baseline_camera_alt_30);

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
            File path = getActivity().getExternalFilesDir(Environment.DIRECTORY_PICTURES);
            File imgFile = new File(path, Objects.requireNonNull(arrayList.get(position).get("uploadImage1")));
            if (imgFile.exists()) {
                Bitmap myBitmap = BitmapFactory.decodeFile(imgFile.getAbsolutePath());
                holder.imageItem1.setImageBitmap(myBitmap);
            }
            File imgFile2 = new File(path, Objects.requireNonNull(arrayList.get(position).get("uploadImage2")));
            if (imgFile2.exists()) {
                Bitmap myBitmap = BitmapFactory.decodeFile(imgFile2.getAbsolutePath());
                holder.imageItem2.setImageBitmap(myBitmap);
            }
            File imgFile3 = new File(path, Objects.requireNonNull(arrayList.get(position).get("uploadImage3")));
            if (imgFile3.exists()) {
                Bitmap myBitmap = BitmapFactory.decodeFile(imgFile3.getAbsolutePath());
                holder.imageItem3.setImageBitmap(myBitmap);
            }


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
            private ImageView imageItem1, imageItem2, imageItem3;
            private View mView;

            public ViewHolder(@NonNull View itemView) {
                super(itemView);
                barcode = itemView.findViewById(R.id.barcode);
                name = itemView.findViewById(R.id.itemName);
                number = itemView.findViewById(R.id.number);
                uploadTime = itemView.findViewById(R.id.uploadTime);
                remarks = itemView.findViewById(R.id.remarks);
                imageItem1 = itemView.findViewById(R.id.imageItem1);
                imageItem2 = itemView.findViewById(R.id.imageItem2);
                imageItem3 = itemView.findViewById(R.id.imageItem3);
                mView = itemView;
            }
        }
    }
}