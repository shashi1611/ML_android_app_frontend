package com.example.myapplication;

import android.Manifest;
import android.content.pm.PackageManager;
import android.os.Build;
import android.os.Bundle;
import android.view.View;
import android.view.WindowManager;
import android.widget.Toast;

import androidx.activity.EdgeToEdge;
import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.content.ContextCompat;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.denzcoskun.imageslider.ImageSlider;
import com.denzcoskun.imageslider.constants.ScaleTypes;
import com.denzcoskun.imageslider.models.SlideModel;
import com.example.myapplication.Adapters.FeatListModelAdapter;
import com.example.myapplication.Adapters.FeatListModelAdapter2;
import com.example.myapplication.Models.FeatListModel;
import com.example.myapplication.Models.FeatListModel2;

import java.util.ArrayList;
import java.util.List;

public class MainActivity extends AppCompatActivity {
    private final ActivityResultLauncher<String[]> requestPermissionLauncher =
            registerForActivityResult(new ActivityResultContracts.RequestMultiplePermissions(), result -> {
                boolean allGranted = true;
                for (boolean granted : result.values()) {
                    if (!granted) {
                        allGranted = false;
                        break;
                    }
                }
                if (allGranted) {
                    Toast.makeText(this, "Permissions Granted", Toast.LENGTH_SHORT).show();
                } else {
                    Toast.makeText(this, "Some permissions were denied!", Toast.LENGTH_SHORT).show();
                }
            });

    @Override
    protected void onCreate(Bundle savedInstanceState) {

        requestStoragePermissions();

        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        getWindow().setFlags(WindowManager.LayoutParams.FLAG_LAYOUT_NO_LIMITS, WindowManager.LayoutParams.FLAG_LAYOUT_NO_LIMITS);
        setContentView(R.layout.activity_main);
        ImageSlider imageSlider = findViewById(R.id.image_slider);
        ArrayList<SlideModel> slideModels = new ArrayList<>();
        slideModels.add(new SlideModel(R.drawable.pexelspixabay161097, ScaleTypes.FIT));
        slideModels.add(new SlideModel(R.drawable.cor_s2w, ScaleTypes.FIT));
        slideModels.add(new SlideModel(R.drawable.pexelspixabay459225, ScaleTypes.FIT));
        slideModels.add(new SlideModel(R.drawable.cor_w2s, ScaleTypes.FIT));

        imageSlider.setImageList(slideModels);

        RecyclerView recyclerView = findViewById(R.id.recyclerView_feat_list);
        ArrayList<FeatListModel> list = new ArrayList<>();
        list.add(new FeatListModel(R.drawable.feat1c, "Ukiyo-e style"));
        list.add(new FeatListModel(R.drawable.feat2c, "Monet style"));
        list.add(new FeatListModel(R.drawable.feat3c, "Van Gogh style"));
        list.add(new FeatListModel(R.drawable.feat4c, "Cezanne style"));

        FeatListModelAdapter featListModelAdapter = new FeatListModelAdapter(list, this);
        recyclerView.setAdapter(featListModelAdapter);

        LinearLayoutManager layoutManager = new LinearLayoutManager(this, LinearLayoutManager.HORIZONTAL, false);
        recyclerView.setLayoutManager(layoutManager);


        recyclerView.setNestedScrollingEnabled(false);
        recyclerView.setOverScrollMode(View.OVER_SCROLL_NEVER);

        RecyclerView recyclerView2 = findViewById(R.id.recyclerView_feat_list2);
        ArrayList<FeatListModel2> list2 = new ArrayList<>();
        list2.add(new FeatListModel2(R.drawable.s2w, "To winter"));
        list2.add(new FeatListModel2(R.drawable.w2s, "To Summer"));

        FeatListModelAdapter2 featListModelAdapter2 = new FeatListModelAdapter2(list2, this);
        recyclerView2.setAdapter(featListModelAdapter2);

        LinearLayoutManager layoutManager2 = new LinearLayoutManager(this, LinearLayoutManager.HORIZONTAL, false);
        recyclerView2.setLayoutManager(layoutManager2);


        recyclerView2.setNestedScrollingEnabled(false);
        recyclerView2.setOverScrollMode(View.OVER_SCROLL_NEVER);


    }

    private void requestStoragePermissions() {
        List<String> permissionsNeeded = new ArrayList<>();

        // WRITE_EXTERNAL_STORAGE is needed only for Android 9 and below (API 28)
        if (Build.VERSION.SDK_INT <= Build.VERSION_CODES.P) {
            if (ContextCompat.checkSelfPermission(this, Manifest.permission.WRITE_EXTERNAL_STORAGE) != PackageManager.PERMISSION_GRANTED) {
                permissionsNeeded.add(Manifest.permission.WRITE_EXTERNAL_STORAGE);
            }
        }

        // READ_EXTERNAL_STORAGE is needed only for Android 12 and below (API 32)
        if (Build.VERSION.SDK_INT <= Build.VERSION_CODES.S_V2) {
            if (ContextCompat.checkSelfPermission(this, Manifest.permission.READ_EXTERNAL_STORAGE) != PackageManager.PERMISSION_GRANTED) {
                permissionsNeeded.add(Manifest.permission.READ_EXTERNAL_STORAGE);
            }
        }

        // For Android 13+ (API 33+), request new media permissions
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            if (ContextCompat.checkSelfPermission(this, Manifest.permission.READ_MEDIA_IMAGES) != PackageManager.PERMISSION_GRANTED) {
                permissionsNeeded.add(Manifest.permission.READ_MEDIA_IMAGES);
            }
            if (ContextCompat.checkSelfPermission(this, Manifest.permission.READ_MEDIA_VIDEO) != PackageManager.PERMISSION_GRANTED) {
                permissionsNeeded.add(Manifest.permission.READ_MEDIA_VIDEO);
            }
            if (ContextCompat.checkSelfPermission(this, Manifest.permission.READ_MEDIA_AUDIO) != PackageManager.PERMISSION_GRANTED) {
                permissionsNeeded.add(Manifest.permission.READ_MEDIA_AUDIO);
            }
        }

        // Request permissions if they are not already granted
        if (!permissionsNeeded.isEmpty()) {
            requestPermissionLauncher.launch(permissionsNeeded.toArray(new String[0]));
        } else {
            Toast.makeText(this, "All necessary permissions are already granted!", Toast.LENGTH_SHORT).show();
        }
    }
}