package com.prasthaan.dusterai;

import android.annotation.SuppressLint;
import android.app.DownloadManager;
import android.content.Context;
import android.content.Intent;
import android.database.Cursor;
import android.net.Uri;
import android.os.Bundle;
import android.os.Environment;
import android.view.View;
import android.view.WindowManager;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.activity.EdgeToEdge;
import androidx.annotation.NonNull;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.content.FileProvider;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.Glide;
import com.google.android.gms.ads.AdError;
import com.google.android.gms.ads.AdRequest;
import com.google.android.gms.ads.FullScreenContentCallback;
import com.google.android.gms.ads.LoadAdError;
import com.google.android.gms.ads.rewarded.RewardedAd;
import com.google.android.gms.ads.rewarded.RewardedAdLoadCallback;
import com.google.android.material.bottomnavigation.BottomNavigationView;
import com.prasthaan.dusterai.Adapters.AdapterResultRestoImg;
import com.prasthaan.dusterai.Models.ModalResultRestoImg;

import java.io.File;
import java.io.FileOutputStream;
import java.io.InputStream;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.ArrayList;

public class ProcessedActivityRestoredImg extends BaseMenuActivity {
    String restoredImageUrl;
    String development_test_ad = "ca-app-pub-3940256099942544/9214589741";
    String development_test_ad_rewarded_ad = "ca-app-pub-3940256099942544/5224354917";
    String production_ad_rewarded_ad_restore_img = "ca-app-pub-4827086355311757/5843507510";
    private Button btnDownload, btnShare;
    private String downloadedImageName;

    private RewardedAd rewardedAd;

    private void loadRewardedAd() {
        AdRequest adRequest = new AdRequest.Builder().build();
        RewardedAd.load(this, "ca-app-pub-4827086355311757/5843507510", adRequest, new RewardedAdLoadCallback() {
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
        setContentView(R.layout.activity_processed_restored_img);
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main), (v, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);
            return insets;
        });
        loadRewardedAd();

        ImageView imageViewRestoImg = findViewById(R.id.download_img_result_resto);
        RecyclerView recyclerViewImageRestorationRes = findViewById(R.id.recyclerView_result_list_image_restoration);
        TextView textView = findViewById(R.id.feature_banner_image_restoratiion);
        btnDownload = findViewById(R.id.download_img_vid_button_resto_img);
        btnShare = findViewById(R.id.share_img_vid_button_resto_img);
        BottomNavigationView bottomNavigationView = findViewById(R.id.bottom_navigation);
        setupBottomNavigation(bottomNavigationView, -1);

        ArrayList<ModalResultRestoImg> listImageRestorationRes = new ArrayList<>();

        Intent intent = getIntent();

        restoredImageUrl = intent.getStringExtra("RESTORED_IMAGE_URL");

        ArrayList<String> faceUrls = intent.getStringArrayListExtra("RESTORED_FACE_URLS");
        if (restoredImageUrl != null) {
            Glide.with(this)
                    .load(restoredImageUrl)
                    .placeholder(R.drawable.loadingimagepleasewait)
                    .error(R.drawable.errorloadingimage)
                    .into(imageViewRestoImg);


        } else {
            Toast.makeText(this, "Image URL not received", Toast.LENGTH_SHORT).show();
        }

        if (!faceUrls.isEmpty()) {
            for (String url : faceUrls) {
                listImageRestorationRes.add(new ModalResultRestoImg(url));
            }
            AdapterResultRestoImg adapterResultRestoImg = new AdapterResultRestoImg(listImageRestorationRes, this);
            recyclerViewImageRestorationRes.setAdapter(adapterResultRestoImg);
            LinearLayoutManager layoutManagerImageRestoration = new LinearLayoutManager(this, LinearLayoutManager.HORIZONTAL, false);
            recyclerViewImageRestorationRes.setLayoutManager(layoutManagerImageRestoration);
            recyclerViewImageRestorationRes.setNestedScrollingEnabled(false);
            recyclerViewImageRestorationRes.setOverScrollMode(View.OVER_SCROLL_NEVER);


        } else {
            textView.setText("");
        }

//        btnDownload.setOnClickListener(v -> {
//            try {
//                // Directly pass the URL string to downloadImage
//                downloadImage(restoredImageUrl);
//                ReviewHelper.launchReviewIfEligible(this);
//                Toast.makeText(this, "Download started, see the notification", Toast.LENGTH_SHORT).show();
//            } catch (Exception e) {
//                e.printStackTrace();
//                Toast.makeText(this, "Error starting download", Toast.LENGTH_SHORT).show();
//            }
//        });

        btnDownload.setOnClickListener(v -> {
            if (rewardedAd == null) {
                Toast.makeText(this, "Ad not loaded yet. Please try again shortly.", Toast.LENGTH_SHORT).show();
                loadRewardedAd(); // Load again if needed
                return;
            }

            new AlertDialog.Builder(this)
                    .setTitle("Unlock Download")
                    .setMessage("Watch a short ad to unlock this image for download.")
                    .setIcon(R.drawable.app_logo__icon)
                    .setPositiveButton("Watch Ad", (dialog, which) -> {
                        // User agreed to watch ad
                        rewardedAd.setFullScreenContentCallback(new FullScreenContentCallback() {
                            @Override
                            public void onAdDismissedFullScreenContent() {
                                rewardedAd = null;
                                loadRewardedAd(); // Preload for next time
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
                            // Reward earned, allow download
                            try {
                                downloadImage(restoredImageUrl);
                                ReviewHelper.launchReviewIfEligible(this);
                                Toast.makeText(this, "Download started, see the notification", Toast.LENGTH_SHORT).show();
                            } catch (Exception e) {
                                e.printStackTrace();
                                Toast.makeText(this, "Error starting download", Toast.LENGTH_SHORT).show();
                            }
                        });
                    })
                    .setNegativeButton("Cancel", (dialog, which) -> dialog.dismiss())
                    .show();
        });


        btnShare.setOnClickListener(v -> shareImageFromPresignedUrl(restoredImageUrl));


    }

    public void downloadImage(String imageUrl) {
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