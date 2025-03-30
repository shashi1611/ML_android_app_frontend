package com.example.myapplication;

import android.Manifest;
import android.annotation.SuppressLint;
import android.app.DownloadManager;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.database.Cursor;
import android.net.Uri;
import android.os.Bundle;
import android.os.Environment;
import android.util.Log;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.Toast;

import androidx.activity.EdgeToEdge;
import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;
import androidx.core.content.FileProvider;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;

import com.bumptech.glide.Glide;

import org.json.JSONException;
import org.json.JSONObject;

import java.io.File;

public class ProcessedActivity extends AppCompatActivity {

    private static final int STORAGE_PERMISSION_CODE = 100;
    private ImageView imageView;
    private Button btnDownload, btnShare;
    private String presignedUrl;
    private File imageFile; // Store downloaded image file
    private String downloadedImageName;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_processed);
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main), (v, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);
            return insets;
        });

        imageView = findViewById(R.id.download_img);
        btnDownload = findViewById(R.id.download_img_vid_button);
        btnShare = findViewById(R.id.share_img_vid_button);


        // Get the presigned URL from intent
        presignedUrl = getIntent().getStringExtra("PRESIGNED_URL");
        if (presignedUrl != null) {
            Log.d("PresignedURL", "Received URL: " + presignedUrl);
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
            if (checkPermission()) {

                try {
                    JSONObject jsonObject = new JSONObject(presignedUrl);
                    String imageUrl = jsonObject.getString("output"); // Extract URL
                    downloadImage(imageUrl);
                    Toast.makeText(this, "Download started see the notification", Toast.LENGTH_SHORT).show();
                } catch (JSONException e) {
                    e.printStackTrace();
                    Toast.makeText(this, "Invalid URL format", Toast.LENGTH_SHORT).show();
                }
            } else {
                requestStoragePermission();
            }
        });

        btnShare.setOnClickListener(v -> shareImage());

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
//                String imageUrl = presignedUrl; // Make sure presignedUrl is available
//                downloadImage(imageUrl);
            } else {
                Toast.makeText(this, "Permission Denied!", Toast.LENGTH_SHORT).show();
            }
        }
    }

//    private void downloadImage(String imageUrl) {
//        try {
//            // Create Download Manager Request
//            DownloadManager.Request request = new DownloadManager.Request(Uri.parse(imageUrl));
//
//            // Set Notification details
//            request.setTitle("Downloading Image");
//            request.setDescription("Saving image to Downloads folder...");
//            request.setNotificationVisibility(DownloadManager.Request.VISIBILITY_VISIBLE_NOTIFY_COMPLETED);
//
//            // Set the download destination (Downloads folder)
//            request.setDestinationInExternalPublicDir(Environment.DIRECTORY_DOWNLOADS, "downloaded_image_" + System.currentTimeMillis() + ".jpg");
//
//            // Get the system Download Manager
//            DownloadManager downloadManager = (DownloadManager) getSystemService(Context.DOWNLOAD_SERVICE);
//
//            // Enqueue the request (start downloading)
//            long downloadId = downloadManager.enqueue(request);
//
//            // Check Download Status (Optional)
//            new Thread(() -> {
//                boolean downloading = true;
//                while (downloading) {
//                    DownloadManager.Query query = new DownloadManager.Query();
//                    query.setFilterById(downloadId);
//                    Cursor cursor = downloadManager.query(query);
//                    if (cursor.moveToFirst()) {
//                        @SuppressLint("Range") int status = cursor.getInt(cursor.getColumnIndex(DownloadManager.COLUMN_STATUS));
//                        if (status == DownloadManager.STATUS_SUCCESSFUL) {
//                            downloading = false;
//                            runOnUiThread(() -> Toast.makeText(this, "Download Complete!", Toast.LENGTH_SHORT).show());
//                        }
//                    }
//                    cursor.close();
//                }
//            }).start();
//        } catch (Exception e) {
//            e.printStackTrace();
//            Toast.makeText(this, "Download failed: " + e.getMessage(), Toast.LENGTH_SHORT).show();
//            Log.d("hariom mre bhai ", "downloadImage failed error: " + e.getMessage());
//        }
//    }


    private void downloadImage(String imageUrl) {
        try {
            // Generate a unique filename
            downloadedImageName = "downloaded_image_" + System.currentTimeMillis() + ".jpg";

            // Create Download Manager Request
            DownloadManager.Request request = new DownloadManager.Request(Uri.parse(imageUrl));

            // Set Notification details
            request.setTitle("Downloading Image");
            request.setDescription("Saving image to Downloads folder...");
            request.setNotificationVisibility(DownloadManager.Request.VISIBILITY_VISIBLE_NOTIFY_COMPLETED);

            // Set the download destination
            request.setDestinationInExternalPublicDir(Environment.DIRECTORY_DOWNLOADS, downloadedImageName);

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
//        if (imageFile == null || !imageFile.exists()) {
//            Toast.makeText(this, "Image not found. Please download first!", Toast.LENGTH_SHORT).show();
////            Log.d("hariom mre bhai", "shareImage: ");
//            return;
//        }
//
//        Uri imageUri = FileProvider.getUriForFile(this, getPackageName() + ".provider", imageFile);
//
//        Intent shareIntent = new Intent(Intent.ACTION_SEND);
//        shareIntent.setType("image/*");
//        shareIntent.putExtra(Intent.EXTRA_STREAM, imageUri);
//        shareIntent.addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION);
//
//        try {
//            startActivity(Intent.createChooser(shareIntent, "Share Image via"));
//        } catch (Exception e) {
//            e.printStackTrace();
//            Toast.makeText(this, "No app found to share the image", Toast.LENGTH_SHORT).show();
//        }
//    }

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
//        } else {
//            Toast.makeText(this, "Image found!", Toast.LENGTH_SHORT).show();
//        }
//
////        Uri imageUri = FileProvider.getUriForFile(this, getPackageName() + ".provider", imageFile);
//        Uri imageUri = FileProvider.getUriForFile(this, getApplicationContext().getPackageName() + ".provider", imageFile);
////
////        Intent shareIntent = new Intent(Intent.ACTION_SEND);
////        shareIntent.setType("image/*");
////        shareIntent.putExtra(Intent.EXTRA_STREAM, imageUri);
////        shareIntent.addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION);
////
////        try {
////            startActivity(Intent.createChooser(shareIntent, "Share Image via"));
////        } catch (Exception e) {
////            e.printStackTrace();
////            Toast.makeText(this, "No app found to share the image", Toast.LENGTH_SHORT).show();
////        }
//    }

    private void shareImage() {
        if (downloadedImageName == null) {
            Toast.makeText(this, "No image found to share! Please download first.", Toast.LENGTH_SHORT).show();
            return;
        }

        File imageFile = new File(Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DOWNLOADS), downloadedImageName);

        if (!imageFile.exists()) {
            Log.e("Share Error", "File not found: " + imageFile.getAbsolutePath());
            Toast.makeText(this, "Image not found!", Toast.LENGTH_SHORT).show();
            return;
        }

        Log.d("Share Success", "File found: " + imageFile.getAbsolutePath());

        try {
            Uri imageUri = FileProvider.getUriForFile(this, getPackageName() + ".fileprovider", imageFile);

            grantUriPermission(getPackageName(), imageUri, Intent.FLAG_GRANT_READ_URI_PERMISSION);

            Intent shareIntent = new Intent(Intent.ACTION_SEND);
            shareIntent.setType("image/*");
            shareIntent.putExtra(Intent.EXTRA_STREAM, imageUri);
            shareIntent.addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION);

            startActivity(Intent.createChooser(shareIntent, "Share Image via"));
        } catch (Exception e) {
            Log.d("hariom mre bhai", "share image error : " + e.getMessage());
            e.printStackTrace();
            Toast.makeText(this, "Failed to share image: " + e.getMessage(), Toast.LENGTH_SHORT).show();
//            Log.d("Share Error", "Exception: " + e.getMessage());
        }
    }


}