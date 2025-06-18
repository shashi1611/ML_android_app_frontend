package com.prasthaan.dusterai;

import android.Manifest;
import android.annotation.SuppressLint;
import android.app.AlertDialog;
import android.app.DownloadManager;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.database.Cursor;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.os.Environment;
import android.util.DisplayMetrics;
import android.util.Log;
import android.view.WindowManager;
import android.widget.Button;
import android.widget.FrameLayout;
import android.widget.ImageView;
import android.widget.Toast;

import androidx.activity.EdgeToEdge;
import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.annotation.NonNull;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;
import androidx.core.content.FileProvider;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;

import com.bumptech.glide.Glide;
import com.google.android.gms.ads.AdError;
import com.google.android.gms.ads.AdRequest;
import com.google.android.gms.ads.AdSize;
import com.google.android.gms.ads.AdView;
import com.google.android.gms.ads.FullScreenContentCallback;
import com.google.android.gms.ads.LoadAdError;
import com.google.android.gms.ads.MobileAds;
import com.google.android.gms.ads.rewarded.RewardedAd;
import com.google.android.gms.ads.rewarded.RewardedAdLoadCallback;
import com.google.android.material.bottomnavigation.BottomNavigationView;

import org.json.JSONException;
import org.json.JSONObject;

import java.io.File;
import java.io.FileOutputStream;
import java.io.InputStream;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.ArrayList;
import java.util.List;

public class ProcessedActivity extends BaseMenuActivity {

    private static final int STORAGE_PERMISSION_CODE = 100;
    private static final String AD_UNIT_ID_DOWNLOAD_PAGE = "ca-app-pub-4827086355311757/743508911111344343434";
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
    String development_test_ad = "ca-app-pub-3940256099942544/9214589741";
    String development_test_ad_rewarded_ad = "ca-app-pub-3940256099942544/5224354917";
    String production_ad_rewarded_ad_enhance_2x = "ca-app-pub-4827086355311757/6989068229";
    String imageUrl = "";
    private ImageView imageView;
    private Button btnDownload, btnShare;
    private String presignedUrl;
    private File imageFile; // Store downloaded image file
    private String downloadedImageName;
    private AdView adViewDownloadPage;
    private FrameLayout adContainerViewDownloadPage;
    private RewardedAd rewardedAd;

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

    private void loadRewardedAd() {
        AdRequest adRequest = new AdRequest.Builder().build();
        RewardedAd.load(this, "ca-app-pub-4827086355311757/6989068229", adRequest, new RewardedAdLoadCallback() {
            @Override
            public void onAdLoaded(@NonNull RewardedAd ad) {
                rewardedAd = ad;
            }

            @Override
            public void onAdFailedToLoad(@NonNull LoadAdError adError) {
                rewardedAd = null;
            }
        });
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        getWindow().setFlags(WindowManager.LayoutParams.FLAG_LAYOUT_NO_LIMITS, WindowManager.LayoutParams.FLAG_LAYOUT_NO_LIMITS);
        setContentView(R.layout.activity_processed);
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main), (v, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);
            return insets;
        });

        imageView = findViewById(R.id.download_img);
        btnDownload = findViewById(R.id.download_img_vid_button);
        btnShare = findViewById(R.id.share_img_vid_button);
        loadRewardedAd();

        BottomNavigationView bottomNavigationView = findViewById(R.id.bottom_navigation);
        setupBottomNavigation(bottomNavigationView, -1);


        // Get the presigned URL from intent
        presignedUrl = getIntent().getStringExtra("PRESIGNED_URL");
        if (presignedUrl != null) {
            try {
                JSONObject jsonObject = new JSONObject(presignedUrl);
                String imageUrl = jsonObject.getString("output"); // Extract the actual URL

                Glide.with(this)
                        .load(imageUrl)
                        .placeholder(R.drawable.loadingimagepleasewait)
                        .error(R.drawable.errorloadingimage)
                        .into(imageView);

            } catch (JSONException e) {
                Log.e("GLIDE", "Failed to parse URL JSON: " + e.getMessage(), e);
            }


        } else {
            Toast.makeText(this, "Image URL not received", Toast.LENGTH_SHORT).show();
        }


        btnDownload.setOnClickListener(v -> {
            if (rewardedAd == null) {
                Toast.makeText(this, "Ad not loaded yet. Please try again shortly.", Toast.LENGTH_SHORT).show();
                loadRewardedAd(); // Preload if not ready
                return;
            }

            new AlertDialog.Builder(this)
                    .setTitle("Unlock Download")
                    .setMessage("Watch a short ad to unlock this image for download.")
                    .setIcon(R.drawable.app_logo__icon)
                    .setPositiveButton("Watch Ad", (dialog, which) -> {
                        // User agreed to watch the ad
                        rewardedAd.setFullScreenContentCallback(new FullScreenContentCallback() {
                            @Override
                            public void onAdDismissedFullScreenContent() {
                                rewardedAd = null;
                                loadRewardedAd(); // Preload next
                            }

                            @Override
                            public void onAdFailedToShowFullScreenContent(AdError adError) {
                                rewardedAd = null;
                                loadRewardedAd();
                            }

                            @Override
                            public void onAdShowedFullScreenContent() {
                                rewardedAd = null;
                            }
                        });

                        rewardedAd.show(this, rewardItem -> {
                            // User watched the ad fully, unlock download
                            try {
                                JSONObject jsonObject = new JSONObject(presignedUrl);
                                String imageUrl = jsonObject.getString("output");
                                downloadImage(imageUrl);
                                ReviewHelper.launchReviewIfEligible(this);
                                Toast.makeText(this, "Download started. See the notification.", Toast.LENGTH_SHORT).show();
                            } catch (JSONException e) {
                                e.printStackTrace();
                                Toast.makeText(this, "Invalid URL format", Toast.LENGTH_SHORT).show();
                            }
                        });
                    })
                    .setNegativeButton("Cancel", (dialog, which) -> {
                        dialog.dismiss(); // Do nothing if user declines
                    })
                    .show();
        });


        btnShare.setOnClickListener(v -> shareImageFromPresignedUrl(imageUrl));

//        <<<<<<<<<<<<<<<<<<<<<<<<<<<<ad part>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>

        MobileAds.initialize(this, initializationStatus -> {
        });

        // Find Ad Container
        adContainerViewDownloadPage = findViewById(R.id.ad_view_container_download_page);

        // Create a new AdView and load an ad
        loadAdaptiveBannerAd();

    }


    //    <<<<<<<<<<<<<<<<<<<<<<<<<<<<<ad functions>>>>>>>>>>>>>>>>>>>>>>>>>
    private void loadAdaptiveBannerAd() {
        // Create a new AdView dynamically
        adViewDownloadPage = new AdView(this);
        adViewDownloadPage.setAdUnitId(AD_UNIT_ID_DOWNLOAD_PAGE);  //prod ad
//        adViewDownloadPage.setAdUnitId(development_test_ad);  //test ad

        // Set adaptive ad size
        AdSize adSize = getAdSize();
        adViewDownloadPage.setAdSize(adSize);

        // Add AdView to container
        adContainerViewDownloadPage.removeAllViews();
        adContainerViewDownloadPage.addView(adViewDownloadPage);

        // Load the ad
        AdRequest adRequest = new AdRequest.Builder().build();
        adViewDownloadPage.loadAd(adRequest);
    }

    private AdSize getAdSize() {
        // Get screen width in dp
        DisplayMetrics displayMetrics = getResources().getDisplayMetrics();
        float widthPixels = displayMetrics.widthPixels;
        float density = displayMetrics.density;
        int adWidth = (int) (widthPixels / density);

        // Get adaptive ad size
        return AdSize.getCurrentOrientationAnchoredAdaptiveBannerAdSize(this, adWidth);
    }


    // Check if storage permission is granted
    private boolean checkPermission() {
        int write = ContextCompat.checkSelfPermission(this, Manifest.permission.WRITE_EXTERNAL_STORAGE);
        return write == PackageManager.PERMISSION_GRANTED;
    }

    // Request storage permission
    private void requestStoragePermission() {
        ActivityCompat.requestPermissions(this, new String[]{Manifest.permission.WRITE_EXTERNAL_STORAGE}, STORAGE_PERMISSION_CODE);
    }

    // Handle permission request result
    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        if (requestCode == STORAGE_PERMISSION_CODE) {
            if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
//                downloadImage();
                try {
                    JSONObject jsonObject = new JSONObject(presignedUrl);
                    String imageUrl = jsonObject.getString("output"); // Extract URL
                    downloadImage(imageUrl);
                } catch (JSONException e) {
                    e.printStackTrace();
                    Toast.makeText(this, "Invalid URL format", Toast.LENGTH_SHORT).show();
                }
            } else {
                Toast.makeText(this, "Permission Denied!", Toast.LENGTH_SHORT).show();
            }
        }
    }


    private void downloadImage(String imageUrl) {
        try {
            // Generate a unique filename
            downloadedImageName = "duster_ai_" + System.currentTimeMillis() + ".jpg";

            // Create Download Manager Request
            DownloadManager.Request request = new DownloadManager.Request(Uri.parse(imageUrl));

            // Set Notification details
            request.setTitle("Downloading Image");
            request.setDescription("Saving image to Downloads folder...");
            request.setNotificationVisibility(DownloadManager.Request.VISIBILITY_VISIBLE_NOTIFY_COMPLETED);

            // Set the download destination
            // Construct the full destination path manually for logging
            String downloadDir = Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DOWNLOADS).getAbsolutePath();
            String fullPath = downloadDir + "/Duster AI/" + downloadedImageName;

// Log the path
            Log.d("history page", "Image will be saved to: " + fullPath);
            request.setDestinationInExternalPublicDir(Environment.DIRECTORY_DOWNLOADS, "Duster AI/" + downloadedImageName);

            // Get the system Download Manager
            DownloadManager downloadManager = (DownloadManager) getSystemService(Context.DOWNLOAD_SERVICE);

            // Enqueue the request (start downloading)
            long downloadId = downloadManager.enqueue(request);

            // Show a toast that download started
            Toast.makeText(this, "Download started, check notification", Toast.LENGTH_SHORT).show();

            // Check Download Status (Optional)
            new Thread(() -> {
                boolean downloading = true;
                while (downloading) {
                    DownloadManager.Query query = new DownloadManager.Query();
                    query.setFilterById(downloadId);
                    Cursor cursor = downloadManager.query(query);
                    if (cursor.moveToFirst()) {
                        @SuppressLint("Range") int status = cursor.getInt(cursor.getColumnIndex(DownloadManager.COLUMN_STATUS));
                        if (status == DownloadManager.STATUS_SUCCESSFUL) {
                            downloading = false;
                            runOnUiThread(() -> Toast.makeText(this, "Download Complete!", Toast.LENGTH_SHORT).show());
                        }
                    }
                    cursor.close();
                }
            }).start();
        } catch (Exception e) {
            e.printStackTrace();
            Toast.makeText(this, "Download failed: " + e.getMessage(), Toast.LENGTH_SHORT).show();
        }
    }


//    private void shareImage() {
//        if (downloadedImageName == null) {
//            Toast.makeText(this, "No image found to share! Please download first.", Toast.LENGTH_SHORT).show();
//            return;
//        }
//
//        File imageFile = new File(Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DOWNLOADS), downloadedImageName);
//
//        if (!imageFile.exists()) {
//            Toast.makeText(this, "Image not found!", Toast.LENGTH_SHORT).show();
//            return;
//        }
//
//
//        try {
//            Uri imageUri = FileProvider.getUriForFile(this, getPackageName() + ".fileprovider", imageFile);
//
//            grantUriPermission(getPackageName(), imageUri, Intent.FLAG_GRANT_READ_URI_PERMISSION);
//
//            Intent shareIntent = new Intent(Intent.ACTION_SEND);
//            shareIntent.setType("image/*");
//            shareIntent.putExtra(Intent.EXTRA_STREAM, imageUri);
//            shareIntent.addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION);
//
//            startActivity(Intent.createChooser(shareIntent, "Share Image via"));
//        } catch (Exception e) {
//            e.printStackTrace();
//            Toast.makeText(this, "Failed to share image: " + e.getMessage(), Toast.LENGTH_SHORT).show();
//        }
//    }

    public void shareImageFromPresignedUrl(String imageUrl) {
        new Thread(() -> {
            try {
                // Download image from presigned URL
                URL url = new URL(imageUrl);
                HttpURLConnection connection = (HttpURLConnection) url.openConnection();
                connection.connect();

                if (connection.getResponseCode() != HttpURLConnection.HTTP_OK) {
                    runOnUiThread(() -> Toast.makeText(this, "Failed to download image!", Toast.LENGTH_SHORT).show());
                    return;
                }

                InputStream inputStream = connection.getInputStream();
                File cacheDir = new File(getCacheDir(), "shared_images");
                if (!cacheDir.exists()) cacheDir.mkdirs();

                File tempFile = new File(cacheDir, "shared_image.jpg");
                FileOutputStream outputStream = new FileOutputStream(tempFile);

                byte[] buffer = new byte[4096];
                int bytesRead;

                while ((bytesRead = inputStream.read(buffer)) != -1) {
                    outputStream.write(buffer, 0, bytesRead);
                }

                outputStream.close();
                inputStream.close();

                Uri contentUri = FileProvider.getUriForFile(this, getPackageName() + ".fileprovider", tempFile);

                // Share via intent
                Intent shareIntent = new Intent(Intent.ACTION_SEND);
                shareIntent.setType("image/*");
                shareIntent.putExtra(Intent.EXTRA_STREAM, contentUri);
                shareIntent.addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION);

                runOnUiThread(() -> startActivity(Intent.createChooser(shareIntent, "Share Image via")));

            } catch (Exception e) {
                e.printStackTrace();
                runOnUiThread(() -> Toast.makeText(this, "Error: " + e.getMessage(), Toast.LENGTH_SHORT).show());
            }
        }).start();
    }


}