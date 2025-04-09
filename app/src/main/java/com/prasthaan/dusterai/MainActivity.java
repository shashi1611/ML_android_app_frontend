package com.prasthaan.dusterai;

import android.Manifest;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.os.Build;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.view.WindowManager;
import android.widget.Toast;

import androidx.activity.EdgeToEdge;
import androidx.activity.result.ActivityResult;
import androidx.activity.result.ActivityResultCallback;
import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.IntentSenderRequest;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.content.ContextCompat;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.denzcoskun.imageslider.ImageSlider;
import com.denzcoskun.imageslider.constants.ScaleTypes;
import com.denzcoskun.imageslider.models.SlideModel;
import com.google.android.gms.tasks.Task;
import com.google.android.play.core.appupdate.AppUpdateInfo;
import com.google.android.play.core.appupdate.AppUpdateManager;
import com.google.android.play.core.appupdate.AppUpdateManagerFactory;
import com.google.android.play.core.appupdate.AppUpdateOptions;
import com.google.android.play.core.install.model.AppUpdateType;
import com.google.android.play.core.install.model.UpdateAvailability;
import com.google.firebase.messaging.FirebaseMessaging;
import com.prasthaan.dusterai.Adapters.FeatListModelAdapter;
import com.prasthaan.dusterai.Adapters.FeatListModelAdapter2;
import com.prasthaan.dusterai.Models.FeatListModel;
import com.prasthaan.dusterai.Models.FeatListModel2;

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


    ActivityResultLauncher<IntentSenderRequest> activityResultLauncher;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        getWindow().setFlags(WindowManager.LayoutParams.FLAG_LAYOUT_NO_LIMITS, WindowManager.LayoutParams.FLAG_LAYOUT_NO_LIMITS);
        if (!isConnected()) {
            startActivity(new Intent(this, NoInternetActivity.class));
            finish();
            return;
        }
        setContentView(R.layout.activity_main);

        requestStoragePermissions();
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

//        firebase cloud messaging
        FirebaseMessaging.getInstance().getToken()
                .addOnCompleteListener(task -> {
                    if (!task.isSuccessful()) {
                        Log.w("FCM_Token", "Fetching FCM registration token failed", task.getException());
                        return;
                    }

                    // Get new FCM token
                    String token = task.getResult();
//                    Log.d("FCM_Token", "FCM Token: " + token);
                });

        activityResultLauncher = registerForActivityResult(
                new ActivityResultContracts.StartIntentSenderForResult(),
                new ActivityResultCallback<ActivityResult>() {
                    @Override
                    public void onActivityResult(ActivityResult result) {
                        // handle callback
                        if (result.getResultCode() != RESULT_OK) {
//                            log(Double.parseDouble("Update flow failed! Result code: " + result.getResultCode()));
                            // If the update is canceled or fails,
                            // you can request to start the update again.
                        }
                    }
                });

        CheckForAppUpdate();


    }

    private void CheckForAppUpdate() {
        AppUpdateManager appUpdateManager = AppUpdateManagerFactory.create(this);

// Returns an intent object that you use to check for an update.
        Task<AppUpdateInfo> appUpdateInfoTask = appUpdateManager.getAppUpdateInfo();

// Checks that the platform will allow the specified type of update.
        appUpdateInfoTask.addOnSuccessListener(appUpdateInfo -> {
            if (appUpdateInfo.updateAvailability() == UpdateAvailability.UPDATE_AVAILABLE
                    // This example applies an immediate update. To apply a flexible update
                    // instead, pass in AppUpdateType.FLEXIBLE
                    && appUpdateInfo.isUpdateTypeAllowed(AppUpdateType.IMMEDIATE)) {


                appUpdateManager.startUpdateFlowForResult(
                        // Pass the intent that is returned by 'getAppUpdateInfo()'.
                        appUpdateInfo,
                        // an activity result launcher registered via registerForActivityResult
                        activityResultLauncher,
                        // Or pass 'AppUpdateType.FLEXIBLE' to newBuilder() for
                        // flexible updates.
                        AppUpdateOptions.newBuilder(AppUpdateType.IMMEDIATE).build());
                // Request the update.
            }
        });
    }

    private boolean isConnected() {
        ConnectivityManager cm = (ConnectivityManager) getSystemService(Context.CONNECTIVITY_SERVICE);
        if (cm != null) {
            NetworkInfo activeNetwork = cm.getActiveNetworkInfo();
            return activeNetwork != null && activeNetwork.isConnected();
        }
        return false;
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