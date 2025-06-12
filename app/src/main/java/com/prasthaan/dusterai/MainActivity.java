package com.prasthaan.dusterai;

import android.Manifest;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.os.Build;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.view.WindowManager;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.Toast;

import androidx.activity.EdgeToEdge;
import androidx.activity.result.ActivityResult;
import androidx.activity.result.ActivityResultCallback;
import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.IntentSenderRequest;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.annotation.NonNull;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.PagerSnapHelper;
import androidx.recyclerview.widget.RecyclerView;
import androidx.recyclerview.widget.SnapHelper;

import com.facebook.FacebookSdk;
import com.facebook.appevents.AppEventsLogger;
import com.google.android.gms.tasks.Task;
import com.google.android.material.snackbar.Snackbar;
import com.google.android.play.core.appupdate.AppUpdateInfo;
import com.google.android.play.core.appupdate.AppUpdateManager;
import com.google.android.play.core.appupdate.AppUpdateManagerFactory;
import com.google.android.play.core.appupdate.AppUpdateOptions;
import com.google.android.play.core.install.InstallStateUpdatedListener;
import com.google.android.play.core.install.model.AppUpdateType;
import com.google.android.play.core.install.model.InstallStatus;
import com.google.android.play.core.install.model.UpdateAvailability;
import com.google.firebase.messaging.FirebaseMessaging;
import com.prasthaan.dusterai.Adapters.FeatListModalAdapterFaceSwap;
import com.prasthaan.dusterai.Adapters.FeatListModalAdapterImageRestoration;
import com.prasthaan.dusterai.Adapters.FeatListModalAdapterPencilSketchGeneration;
import com.prasthaan.dusterai.Adapters.FeatListModelAdapter;
import com.prasthaan.dusterai.Adapters.FeatListModelAdapter2;
import com.prasthaan.dusterai.Adapters.carouselModelAdapter;
import com.prasthaan.dusterai.Models.FeatListModalFaceSwap;
import com.prasthaan.dusterai.Models.FeatListModalImageRestoration;
import com.prasthaan.dusterai.Models.FeatListModel;
import com.prasthaan.dusterai.Models.FeatListModel2;
import com.prasthaan.dusterai.Models.FeatListModelPencilSketchGeneration;
import com.prasthaan.dusterai.Models.carouselModel;

import java.util.ArrayList;
import java.util.List;

public class MainActivity extends AppCompatActivity {
    private static final int REQUEST_NOTIFICATION_PERMISSION = 1001;
    private static final String TAG = "MainActivity";
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
    AppUpdateManager appUpdateManager;
    ActivityResultLauncher<IntentSenderRequest> activityResultLauncherForInAppUpdate;
    InstallStateUpdatedListener listener = state -> {
        if (state.installStatus() == InstallStatus.DOWNLOADED) {
            // After the update is downloaded, show a notification
            // and request user confirmation to restart the app.
            popupSnackbarForCompleteUpdate();
        }
    };
    int updateType = 0;
    private LinearLayout dotContainer;
    private int totalItems;

//    private void setupDots(int total, int selectedPosition) {
//        dotContainer.removeAllViews();
//
//        for (int i = 0; i < total; i++) {
//            ImageView dot = new ImageView(this);
//            LinearLayout.LayoutParams params = new LinearLayout.LayoutParams(
//                    ViewGroup.LayoutParams.WRAP_CONTENT,
//                    ViewGroup.LayoutParams.WRAP_CONTENT);
//            params.setMargins(8, 0, 8, 0);
//            dot.setLayoutParams(params);
//
//            if (i == selectedPosition) {
//                dot.setImageDrawable(ContextCompat.getDrawable(this, R.drawable.dot_active));
//            } else {
//                dot.setImageDrawable(ContextCompat.getDrawable(this, R.drawable.dot_inactive));
//            }
//
//            dotContainer.addView(dot);
//        }
//    }

    private void setupDots(int totalItems, int activePosition) {
        dotContainer.removeAllViews();

        for (int i = 0; i < totalItems; i++) {
            View dot = new View(this);
            int size = 25;
            LinearLayout.LayoutParams params = new LinearLayout.LayoutParams(size, size);
            params.setMargins(8, 0, 8, 0);
            dot.setLayoutParams(params);
            dot.setBackgroundResource(R.drawable.dot_selector); // selector with active/inactive states

            // Initial scale for animation
            dot.setScaleX(i == activePosition ? 1.4f : 1f);
            dot.setScaleY(i == activePosition ? 1.4f : 1f);

            dotContainer.addView(dot);

            // Animate dot scale
            dot.animate()
                    .scaleX(i == activePosition ? 1.4f : 1f)
                    .scaleY(i == activePosition ? 1.4f : 1f)
                    .setDuration(250)
                    .start();
        }
    }


    // Displays the snackbar notification and call to action.
    private void popupSnackbarForCompleteUpdate() {
        Snackbar snackbar =
                Snackbar.make(
                        findViewById(R.id.recyclerView_feat_list),
                        "An update has just been downloaded.",
                        Snackbar.LENGTH_INDEFINITE);
        snackbar.setAction("RESTART", view -> appUpdateManager.completeUpdate());
        snackbar.setActionTextColor(
                getResources().getColor(android.R.color.holo_blue_dark));
        snackbar.show();
    }

    @Override
    protected void onStop() {
        super.onStop();
        appUpdateManager.unregisterListener(listener);
    }

    @Override
    protected void onResume() {
        super.onResume();
        appUpdateManager
                .getAppUpdateInfo()
                .addOnSuccessListener(appUpdateInfo -> {
                    // If the update is downloaded but not installed,
                    // notify the user to complete the update.
                    if (appUpdateInfo.installStatus() == InstallStatus.DOWNLOADED) {
                        popupSnackbarForCompleteUpdate();
                    }
                });
    }

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
        FacebookSdk.sdkInitialize(getApplicationContext());
        AppEventsLogger.activateApp(this.getApplication());
        AppEventsLogger logger = AppEventsLogger.newLogger(this);

        requestStoragePermissions();
        // Request notification permission if Android 13 or above (paste it inside onCreate method)
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            requestNotificationPermission();
        } else {
            // For earlier versions, directly initialize FCM
            initializeFCM();
        }

//        <<<<<<<<<<<<<<<<<<<<<<<<<<all the elements and their IDs >>>>>>>>>>>>>>>>>>>>>>>>>>>
        ImageView arrowIconImageUpscaling = findViewById(R.id.swipeRightArrowImageUpscaling);
        ImageView arrowIconPencilSketchGeneration = findViewById(R.id.swipeRightArrowPencilSketchGeneration);
        ImageView arrowIconFaceSwap = findViewById(R.id.swipeRightArrowFaceSwap);
        ImageView arrowIconImageToPainting = findViewById(R.id.swipeRightArrowImageToPainting);
        ImageView arrowIconSeasonChanger = findViewById(R.id.swipeRightArrowSeasonChanger);

//        <<<<<<<<<<<<<<<<<<<recyclerview for carousel >>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>

        RecyclerView recyclerViewCarousel = findViewById(R.id.recyclerViewCarousel);
        ArrayList<carouselModel> listCarousel = new ArrayList<carouselModel>();
        listCarousel.add(new carouselModel(R.drawable.pexelspixabay161097));
        listCarousel.add(new carouselModel(R.drawable.cor_s2w));
        listCarousel.add(new carouselModel(R.drawable.pexelspixabay459225));
        listCarousel.add(new carouselModel(R.drawable.cor_w2s));
        carouselModelAdapter carouselModelAdapter = new carouselModelAdapter(listCarousel, this);
        recyclerViewCarousel.setAdapter(carouselModelAdapter);
        LinearLayoutManager layoutManagerCarousel = new LinearLayoutManager(this, LinearLayoutManager.HORIZONTAL, false);
        recyclerViewCarousel.setLayoutManager(layoutManagerCarousel);
        recyclerViewCarousel.setNestedScrollingEnabled(false);
        recyclerViewCarousel.setOverScrollMode(View.OVER_SCROLL_NEVER);
        SnapHelper snapHelper = new PagerSnapHelper();
        snapHelper.attachToRecyclerView(recyclerViewCarousel);

        dotContainer = findViewById(R.id.dot_container);

        totalItems = carouselModelAdapter.getItemCount();

        recyclerViewCarousel.post(() -> {
            int initialPosition = ((LinearLayoutManager) recyclerViewCarousel.getLayoutManager()).findFirstCompletelyVisibleItemPosition();
            if (initialPosition == RecyclerView.NO_POSITION) {
                initialPosition = 0;
            }
            setupDots(totalItems, initialPosition);
        });
        recyclerViewCarousel.addOnScrollListener(new RecyclerView.OnScrollListener() {
            @Override
            public void onScrolled(@NonNull RecyclerView recyclerView, int dx, int dy) {
                super.onScrolled(recyclerView, dx, dy);

                View snapView = snapHelper.findSnapView(recyclerView.getLayoutManager());
                if (snapView != null) {
                    int position = recyclerView.getLayoutManager().getPosition(snapView);
                    setupDots(totalItems, position);
                }
            }
        });
//<<<<<<<<<<<<<<<<<<<<<<<this code for auto scroll dont remove it >>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>
//        final Handler handler = new Handler();
//        final int scrollDelay = 2000; // 2 seconds delay between scrolls
//        final int scrollBy = 1; // scroll 1 item at a time
//
//        final Runnable runnable = new Runnable() {
//            int count = 0;
//
//            @Override
//            public void run() {
//                int itemCount = recyclerViewCarousel.getAdapter().getItemCount();
//
//                if (count < itemCount) {
//                    recyclerViewCarousel.smoothScrollToPosition(count);
//                    count++;
//                } else {
//                    // Reset back to first item
//                    count = 0;
//                    recyclerViewCarousel.scrollToPosition(count);
//                    count++;
//                }
//                handler.postDelayed(this, scrollDelay);
//            }
//        };
//        // Start auto-scrolling
//        handler.postDelayed(runnable, scrollDelay);
//        // Optional: Pause auto-scrolling when user touches RecyclerView
//        recyclerViewCarousel.addOnItemTouchListener(new RecyclerView.OnItemTouchListener() {
//            @Override
//            public boolean onInterceptTouchEvent(@NonNull RecyclerView rv, @NonNull MotionEvent e) {
//                switch (e.getAction()) {
//                    case MotionEvent.ACTION_DOWN:
//                        handler.removeCallbacks(runnable); // Pause
//                        break;
//                    case MotionEvent.ACTION_UP:
//                        handler.postDelayed(runnable, scrollDelay); // Resume
//                        break;
//                }
//                return false;
//            }
//
//            @Override
//            public void onTouchEvent(@NonNull RecyclerView rv, @NonNull MotionEvent e) {
//            }
//
//            @Override
//            public void onRequestDisallowInterceptTouchEvent(boolean disallowIntercept) {
//            }
//        });
//
//        SnapHelper snapHelper = new LinearSnapHelper();
//        snapHelper.attachToRecyclerView(recyclerViewCarousel);


//        <<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<recycler view for image upscaling feature>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>
        RecyclerView recyclerViewImageRestoration = findViewById(R.id.recyclerView_feat_list_image_restoration);
        ArrayList<FeatListModalImageRestoration> listImageRestoration = new ArrayList<>();
        listImageRestoration.add(new FeatListModalImageRestoration(R.drawable.restore_image_feat_card, "Restore image"));
        listImageRestoration.add(new FeatListModalImageRestoration(R.drawable.feat_card_two_x, "Enhance 2X"));
        listImageRestoration.add(new FeatListModalImageRestoration(R.drawable.feat_card_four_x, "Enhance 4X"));
        FeatListModalAdapterImageRestoration featListModalAdapterImageRestoration = new FeatListModalAdapterImageRestoration(listImageRestoration, this);
        recyclerViewImageRestoration.setAdapter(featListModalAdapterImageRestoration);
        LinearLayoutManager layoutManagerImageRestoratiion = new LinearLayoutManager(this, LinearLayoutManager.HORIZONTAL, false);
        recyclerViewImageRestoration.setLayoutManager(layoutManagerImageRestoratiion);
        recyclerViewImageRestoration.setNestedScrollingEnabled(false);
        recyclerViewImageRestoration.setOverScrollMode(View.OVER_SCROLL_NEVER);


        arrowIconImageUpscaling.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                LinearLayoutManager layoutManager = (LinearLayoutManager) recyclerViewImageRestoration.getLayoutManager();
                int lastVisibleItem = layoutManager.findLastVisibleItemPosition();
                int totalItems = recyclerViewImageRestoration.getAdapter().getItemCount();

                int nextPosition = lastVisibleItem + 1;
//                if (nextPosition < totalItems) {
                recyclerViewImageRestoration.smoothScrollToPosition(nextPosition);
//                }


//                logger.logEvent("photoRestored");

            }
        });


//        <<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<RecyclerView for pencil sketch generation>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>
        RecyclerView recyclerViewPencilSketchGeneration = findViewById(R.id.recyclerView_feat_list_pencil_sketch_generation);
        ArrayList<FeatListModelPencilSketchGeneration> listModelPencilSketchGenerations = new ArrayList<>();
        listModelPencilSketchGenerations.add(new FeatListModelPencilSketchGeneration(R.drawable.feat_card_small_pencil, "✨ Soft Sketch"));
        listModelPencilSketchGenerations.add(new FeatListModelPencilSketchGeneration(R.drawable.feat_card_medium_pencil, "✏\uFE0F Classic Sketch"));
        listModelPencilSketchGenerations.add(new FeatListModelPencilSketchGeneration(R.drawable.feat_card_large_pencil, "\uD83D\uDD8C\uFE0F Bold Sketch"));
        FeatListModalAdapterPencilSketchGeneration featListModalAdapterPencilSketchGeneration = new FeatListModalAdapterPencilSketchGeneration(listModelPencilSketchGenerations, this);
        recyclerViewPencilSketchGeneration.setAdapter(featListModalAdapterPencilSketchGeneration);
        LinearLayoutManager layoutManagerPencilSketchGeneration = new LinearLayoutManager(this, LinearLayoutManager.HORIZONTAL, false);
        recyclerViewPencilSketchGeneration.setLayoutManager(layoutManagerPencilSketchGeneration);
        recyclerViewPencilSketchGeneration.setNestedScrollingEnabled(false);
        recyclerViewPencilSketchGeneration.setOverScrollMode(View.OVER_SCROLL_NEVER);

        arrowIconPencilSketchGeneration.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                LinearLayoutManager layoutManager = (LinearLayoutManager) recyclerViewPencilSketchGeneration.getLayoutManager();
                int lastVisibleItem = layoutManager.findLastVisibleItemPosition();
                int totalItems = recyclerViewPencilSketchGeneration.getAdapter().getItemCount();

                int nextPosition = lastVisibleItem + 1;
//                if (nextPosition < totalItems) {
                recyclerViewPencilSketchGeneration.smoothScrollToPosition(nextPosition);
//                }
            }
        });


//        <<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<Recycler view for face swap>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>
        RecyclerView recyclerViewImageFaceSwap = findViewById(R.id.recyclerView_feat_list_face_swap);
        ArrayList<FeatListModalFaceSwap> listFaceSwap = new ArrayList<>();
//        listFaceSwap.add(new FeatListModalFaceSwap(R.drawable.restore_image_feat_card, "Single face"));
        listFaceSwap.add(new FeatListModalFaceSwap(R.drawable.single_card_f, "Single face"));

        listFaceSwap.add(new FeatListModalFaceSwap(R.drawable.multi_card_fi, "Multiple faces"));
        FeatListModalAdapterFaceSwap featListModalAdapterFaceSwap = new FeatListModalAdapterFaceSwap(listFaceSwap, this);
        recyclerViewImageFaceSwap.setAdapter(featListModalAdapterFaceSwap);
        LinearLayoutManager layoutManagerFaceSwap = new LinearLayoutManager(this, LinearLayoutManager.HORIZONTAL, false);
        recyclerViewImageFaceSwap.setLayoutManager(layoutManagerFaceSwap);
        recyclerViewImageFaceSwap.setNestedScrollingEnabled(false);
        recyclerViewImageFaceSwap.setOverScrollMode(View.OVER_SCROLL_NEVER);

        arrowIconFaceSwap.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Toast.makeText(MainActivity.this, "No items present", Toast.LENGTH_SHORT).show();

//                LinearLayoutManager layoutManager = (LinearLayoutManager) recyclerViewImageFaceSwap.getLayoutManager();
//                int lastVisibleItem = layoutManager.findLastVisibleItemPosition();
//                int totalItems = recyclerViewImageFaceSwap.getAdapter().getItemCount();
//
//                int nextPosition = lastVisibleItem;
//                if (nextPosition < totalItems) {
//                    recyclerViewImageFaceSwap.smoothScrollToPosition(nextPosition);
//                }
            }
        });


//        <<<<<<<<<<<<<<<<<<<<<<< recycler view for image to painting>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>
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
        arrowIconImageToPainting.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                LinearLayoutManager layoutManager = (LinearLayoutManager) recyclerView.getLayoutManager();
                int lastVisibleItem = layoutManager.findLastVisibleItemPosition();
                int totalItems = recyclerView.getAdapter().getItemCount();

                int nextPosition = lastVisibleItem + 1;
//                if (nextPosition < totalItems) {
                recyclerView.smoothScrollToPosition(nextPosition);
//                }
            }
        });


//        <<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<recyclerview for season changer>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>
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

        arrowIconSeasonChanger.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Toast.makeText(MainActivity.this, "No items present", Toast.LENGTH_SHORT).show();

//                LinearLayoutManager layoutManager = (LinearLayoutManager) recyclerView2.getLayoutManager();
//                int lastVisibleItem = layoutManager.findLastVisibleItemPosition();
//                int totalItems = recyclerView2.getAdapter().getItemCount();
//
//                int nextPosition = lastVisibleItem + 1;
//                recyclerView2.smoothScrollToPosition(nextPosition);
            }
        });


//<<<<<<<<<<<<<<<<<<<<<<<<<<<in app update code >>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>
        activityResultLauncherForInAppUpdate = registerForActivityResult(
                new ActivityResultContracts.StartIntentSenderForResult(),
                new ActivityResultCallback<ActivityResult>() {
                    @Override
                    public void onActivityResult(ActivityResult result) {
                        // handle callback
                        if (result.getResultCode() != RESULT_OK) {
                            if (updateType == 1) {
                                AlertDialog.Builder builder = new AlertDialog.Builder(MainActivity.this);
                                builder.setTitle("Update Required")
                                        .setMessage("You must update the app to continue using it.")
                                        .setCancelable(false)
                                        .setPositiveButton("Exit", (dialog, which) -> {
                                            finishAffinity();
                                        })
                                        .show();


                            }

                        }
                    }
                });

        CheckForInAppUpdate();


    }


    private void CheckForInAppUpdate() {

        appUpdateManager = AppUpdateManagerFactory.create(getApplicationContext());

        Task<AppUpdateInfo> appUpdateInfoTask = appUpdateManager.getAppUpdateInfo();

        appUpdateInfoTask.addOnSuccessListener(appUpdateInfo -> {
            if (appUpdateInfo.updateAvailability() == UpdateAvailability.UPDATE_AVAILABLE) {
                SharedPreferences prefs = getSharedPreferences("update_prefs", MODE_PRIVATE);
                long firstSeen = prefs.getLong("first_seen_update", -1);

                if (firstSeen == -1) {
                    firstSeen = System.currentTimeMillis();
                    prefs.edit().putLong("first_seen_update", firstSeen).apply();
                }

                long daysSinceFirstSeen = (System.currentTimeMillis() - firstSeen) / (1000 * 60 * 60 * 24);
//                long minutesSinceFirstSeen = (System.currentTimeMillis() - firstSeen) / (1000 * 60);

                if (daysSinceFirstSeen >= 5) {
                    updateType = AppUpdateType.IMMEDIATE;
                } else {
                    updateType = AppUpdateType.FLEXIBLE;
                }

                if (appUpdateInfo.isUpdateTypeAllowed(updateType)) {
                    AppUpdateOptions options = AppUpdateOptions.newBuilder(updateType).build();

                    appUpdateManager.startUpdateFlowForResult(
                            appUpdateInfo,
                            activityResultLauncherForInAppUpdate,
                            options
                    );

                    // Show reminder on day 3 and 4
                    if (daysSinceFirstSeen == 3 || daysSinceFirstSeen == 4) {
                        showUpdateReminderDialog();
                    }
                }
            } else {
                // Clear first seen time if no update is available (app updated)
                getSharedPreferences("update_prefs", MODE_PRIVATE).edit().remove("first_seen_update").apply();
            }
        });
    }

    private void showUpdateReminderDialog() {
        new AlertDialog.Builder(this)
                .setTitle("Update Available")
                .setMessage("A new version of Duster AI is available. Please update soon, as after 5 days this update will be mandatory.")
                .setPositiveButton("Update Now", (dialog, which) -> {
                    CheckForInAppUpdate(); // Re-trigger update flow
                })
                .setNegativeButton("Later", null)
                .show();
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


    //<<<<<<<<<<<<<<<<<<<<<<<<    cloud mesaaging permission and stuffs and FCM >>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>
    private void requestNotificationPermission() {
        if (ContextCompat.checkSelfPermission(this, Manifest.permission.POST_NOTIFICATIONS) != PackageManager.PERMISSION_GRANTED) {
            ActivityCompat.requestPermissions(this, new String[]{Manifest.permission.POST_NOTIFICATIONS}, REQUEST_NOTIFICATION_PERMISSION);
        } else {
            // Permission already granted
            initializeFCM();
        }
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        if (requestCode == REQUEST_NOTIFICATION_PERMISSION) {
            if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                // Permission granted
                initializeFCM();
            } else {
                // Permission denied
                Log.e(TAG, "Notification permission denied.");
            }
        }
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
    }


//for initialization of FCM TOKEN

    private void initializeFCM() {
        FirebaseMessaging.getInstance().getToken()
                .addOnCompleteListener(task -> {
                    if (!task.isSuccessful()) {
                        Log.w(TAG, "Fetching FCM registration token failed", task.getException());
                        return;
                    }
                    String token = task.getResult();
                    Log.d(TAG, "FCM Token: " + token);
                });
    }


}