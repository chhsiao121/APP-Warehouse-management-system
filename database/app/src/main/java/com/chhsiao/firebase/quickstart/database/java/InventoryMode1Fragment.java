package com.chhsiao.firebase.quickstart.database.java;

import android.Manifest;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.content.res.AssetManager;
import android.graphics.Bitmap;
import android.graphics.drawable.BitmapDrawable;
import android.os.Bundle;

import androidx.activity.result.ActivityResultLauncher;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;
import androidx.navigation.fragment.NavHostFragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.os.Environment;
import android.provider.MediaStore;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.TextView;
import android.widget.Toast;

import com.chhsiao.firebase.quickstart.database.java.capture.CaptureAct;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.common.io.ByteStreams;
import com.google.firebase.quickstart.database.R;
import com.google.firebase.quickstart.database.databinding.FragmentInventoryMode1Binding;
import com.google.firebase.storage.StorageReference;
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


public class InventoryMode1Fragment extends BaseFragment {
    private static final String REQUIRED = "Required";
    ScanOptions options = new ScanOptions();
    private static final int PICK_CAMERA_REQUEST = 100;
    private Bitmap mImageBitmap;
    private FragmentInventoryMode1Binding binding;
    private RecyclerView mRecycler;
    LinearLayoutManager mManager;
    private String scannType;
    private String modeId;
    private String taskId;
    private String locationId;
    private String json_name;
    private JSONObject jsonData;
    Context context;
    private int image123;
    String [] stateList =   {"正常","堪用","破損","待修","建議報廢"};
    public InventoryMode1Fragment() {
        // Required empty public constructor
    }



    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        context = getContext();
        if(ContextCompat.checkSelfPermission(getActivity(), Manifest.permission.CAMERA)!= PackageManager.PERMISSION_GRANTED){
            ActivityCompat.requestPermissions(getActivity(),new String[]{Manifest.permission.CAMERA},PICK_CAMERA_REQUEST);
        }

    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        binding = FragmentInventoryMode1Binding.inflate(inflater, container, false);

        modeId = InventoryModeFragment.mode;
        taskId = InventoryTaskFragment.taskId;
        locationId = InventoryLocationFragment.locationId;
        json_name = modeId+"_"+taskId+"_"+locationId+".json";
        checkJsonFile(json_name);
        binding.listT22View.setHasFixedSize(true);
        mManager = new LinearLayoutManager(context);
        binding.listT22View.setLayoutManager(mManager);
//        TODO
//        myListAdapter = new
//        binding.listT22View.setAdapter(myListAdapter);

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
                startActivityForResult(intent,100);
            }
        });
        binding.btnPhotograph2.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                image123 = 2;
                Intent intent = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);
                startActivityForResult(intent,100);
            }
        });
        binding.btnPhotograph3.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                image123 = 3;
                Intent intent = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);
                startActivityForResult(intent,100);
            }
        });
        binding.btnok.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

            }
        });
        binding.refreshLayout.setColorSchemeColors(getResources().getColor(R.color.material_cyan_a700));
        binding.refreshLayout.setOnRefreshListener(()->{
//          TODO
//            mAdapter.notifyDataSetChanged();
            binding.refreshLayout.setRefreshing(false);
        });
    }
    private final ActivityResultLauncher<ScanOptions> barcodeLauncher = registerForActivityResult(new ScanContract(),
            result -> {
                if(result.getContents()!=null){
                    if(scannType.equals("number")){
                        binding.textInputLayoutNumber.getEditText().setText(result.getContents());
                    }else if(scannType.equals("barcode")){
                        binding.textInputLayoutBarcode.getEditText().setText(result.getContents());
                    }
                }
                else {
                    Toast.makeText(getActivity(),"No Results",Toast.LENGTH_SHORT).show();
                }
            });

    @Override
    public void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == PICK_CAMERA_REQUEST ){
//            mImageUri = data.getData();
            if(data.getExtras().get("data")!=null){
                mImageBitmap = (Bitmap)data.getExtras().get("data");
                if(image123 == 1) {
                    binding.btnPhotograph1.setImageBitmap(mImageBitmap);
                }
                else if(image123 == 2){
                    binding.btnPhotograph2.setImageBitmap(mImageBitmap);
                }
                else if(image123 == 3){
                    binding.btnPhotograph3.setImageBitmap(mImageBitmap);
                }
            }

        }
//            if(requestCode==100){
//            Bitmap bitmap = (Bitmap)data.getExtras().get("data");
//            binding.imageViewItem.setImageBitmap(bitmap);
//        }
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
    private String saveBitmap2Jpg() {
        final String fileName;
        Bitmap bitmap = ((BitmapDrawable)binding.btnPhotograph1.getDrawable()).getBitmap();
//        TODO
//        if (bitmap != null) {
//            fileName = System.currentTimeMillis() + ".jpg";
//            //                Bitmap original = MediaStore.Images.Media.getBitmap(requireActivity().getContentResolver(), mImageUri);
//            ByteArrayOutputStream stream = new ByteArrayOutputStream();
//            Bitmap resized = Bitmap.createScaledBitmap ( bitmap , 1000 , 1000 , true ) ;
//            ByteArrayOutputStream baos = new ByteArrayOutputStream();
//            resized.compress(Bitmap.CompressFormat.JPEG, 50, baos);
//            byte[] imageByte = baos.toByteArray();
//            StorageReference fileReference = mStorageRef.child(fileName);
//            mUploadTask = fileReference.putBytes(imageByte);
//
//            // Register observers to listen for when the download is done or if it fails
//            mUploadTask.addOnFailureListener(new OnFailureListener() {
//                @Override
//                public void onFailure(@NonNull Exception e) {
//                    Toast.makeText(getContext(), "image upload failure", Toast.LENGTH_SHORT).show();
//                }
//            }).addOnSuccessListener(new OnSuccessListener<UploadTask.TaskSnapshot>() {
//                @Override
//                public void onSuccess(UploadTask.TaskSnapshot taskSnapshot) {
//                    Log.e(TAG, "image upload success: ");
//                }
//            });
//        } else {
//            Toast.makeText(getContext(), "No file selected", Toast.LENGTH_SHORT).show();
//            fileName = null;
//        }
        return fileName;
    }
    private class MyListAdapter extends RecyclerView.Adapter<MyListAdapter.ViewHolder>{
        @NonNull
        @Override
        public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
            View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_post_t22,parent,false);
            return new ViewHolder(view);
        }

        @Override
        public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
//            holder.id.setText(arrayList.get(position).get("id"));
//            holder.name.setText(arrayList.get(position).get("name"));
//            holder.time.setText(arrayList.get(position).get("time"));
            holder.mView.setOnClickListener((v)->{
//                Toast.makeText(context,holder.name.getText(),Toast.LENGTH_SHORT).show();
                locationId = holder.id.getText().toString();
            });
        }

        @Override
        public int getItemCount() {
            return 0;
//            return arrayList.size();
        }

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
    }
}