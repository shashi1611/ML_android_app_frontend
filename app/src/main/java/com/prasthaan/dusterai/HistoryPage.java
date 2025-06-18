package com.prasthaan.dusterai;

import android.os.Bundle;
import android.os.Environment;
import android.text.format.DateFormat;
import android.util.Log;
import android.view.View;
import android.view.WindowManager;
import android.widget.TextView;
import android.widget.Toast;

import androidx.activity.EdgeToEdge;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.airbnb.lottie.LottieAnimationView;
import com.google.android.material.bottomnavigation.BottomNavigationView;
import com.prasthaan.dusterai.Adapters.AdapterHistory;
import com.prasthaan.dusterai.Models.ModalHistory;

import java.io.File;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.Date;
import java.util.List;

public class HistoryPage extends BaseMenuActivity {
    RecyclerView recyclerView;
    AdapterHistory adapterHistory;
    List<ModalHistory> historyList;

    @Override
    public void onRequestPermissionsResult(int requestCode, String[] permissions, int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        if (PermissionUtils.handlePermissionResult(requestCode, permissions, grantResults)) {
            // Permission granted ✅
            Toast.makeText(this, "Storage permission granted", Toast.LENGTH_SHORT).show();
        } else {
            // Permission denied ❌
            if (PermissionUtils.shouldShowRationale(this)) {
                Toast.makeText(this, "Storage permission is required", Toast.LENGTH_SHORT).show();
            } else {
                PermissionUtils.openAppSettings(this);
            }
        }
    }


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        getWindow().setFlags(WindowManager.LayoutParams.FLAG_LAYOUT_NO_LIMITS, WindowManager.LayoutParams.FLAG_LAYOUT_NO_LIMITS);
        setContentView(R.layout.activity_history_page);
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main), (v, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);
            return insets;
        });
        BottomNavigationView bottomNavigationView = findViewById(R.id.bottom_navigation);
        setupBottomNavigation(bottomNavigationView, R.id.nav_history);
        TextView noHistoryText = findViewById(R.id.no_history_text);
        LottieAnimationView noDataAnimation = findViewById(R.id.no_data_animation);

        recyclerView = findViewById(R.id.recyclerViewHistory);
        recyclerView.setLayoutManager(new LinearLayoutManager(this));

        historyList = new ArrayList<>();

        adapterHistory = new AdapterHistory((ArrayList<ModalHistory>) historyList, this);
        recyclerView.setAdapter(adapterHistory);

        if (!PermissionUtils.isStoragePermissionGranted(this)) {
            PermissionUtils.requestStoragePermission(this);
        }
        loadHistoryImages();

        if (historyList.isEmpty()) {
            recyclerView.setVisibility(View.GONE);
            noDataAnimation.setVisibility(View.VISIBLE);        // for animation (optional)
        } else {
            recyclerView.setVisibility(View.VISIBLE);
            noHistoryText.setVisibility(View.GONE);
            noDataAnimation.setVisibility(View.GONE);
        }


    }

    private void loadHistoryImages() {
        File downloadsDir = new File(Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DOWNLOADS), "Duster AI");
        Log.d("HistoryDebug", "Looking in: " + downloadsDir.getAbsolutePath());
        Log.d("HistoryDebug", "Exists: " + downloadsDir.exists() + ", CanRead: " + downloadsDir.canRead());

        if (!downloadsDir.exists()) {
            Log.e("HistoryDebug", "Duster AI directory does not exist.");
            return;
        }

        File[] files = downloadsDir.listFiles();
        Log.d("HistoryDebug", "Files: " + Arrays.toString(files));

        if (files != null) {
            for (File file : files) {
                if (file.getName().startsWith("duster_ai_") && file.getName().endsWith(".jpg")) {
                    String fileName = file.getName();
                    String filePath = file.getAbsolutePath();
                    long lastModified = file.lastModified();
                    String date = DateFormat.format("dd MMM yyyy, hh:mm a", new Date(lastModified)).toString();
                    historyList.add(new ModalHistory(filePath, fileName, date));
                }
            }
            Collections.sort(historyList, (a, b) -> b.getDateTime().compareTo(a.getDateTime()));
            adapterHistory.notifyDataSetChanged();
        } else {
            Log.e("HistoryDebug", "listFiles() returned null.");
        }
    }


}