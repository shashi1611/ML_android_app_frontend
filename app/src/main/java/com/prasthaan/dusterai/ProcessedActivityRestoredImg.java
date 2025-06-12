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
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.content.FileProvider;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.Glide;
import com.prasthaan.dusterai.Adapters.AdapterResultRestoImg;
import com.prasthaan.dusterai.Models.ModalResultRestoImg;

import java.io.File;
import java.util.ArrayList;

public class ProcessedActivityRestoredImg extends AppCompatActivity {
    private Button btnDownload, btnShare;
    private String downloadedImageName;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_processed_restored_img);
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main), (v, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);
            return insets;
        });

        ImageView imageViewRestoImg = findViewById(R.id.download_img_result_resto);
        RecyclerView recyclerViewImageRestorationRes = findViewById(R.id.recyclerView_result_list_image_restoration);
        TextView textView = findViewById(R.id.feature_banner_image_restoratiion);
        btnDownload = findViewById(R.id.download_img_vid_button_resto_img);
        btnShare = findViewById(R.id.share_img_vid_button_resto_img);

        ArrayList<ModalResultRestoImg> listImageRestorationRes = new ArrayList<>();

        Intent intent = getIntent();

        String restoredImageUrl = intent.getStringExtra("RESTORED_IMAGE_URL");

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

        btnDownload.setOnClickListener(v -> {
            try {
                // Directly pass the URL string to downloadImage
                downloadImage(restoredImageUrl);
                ReviewHelper.launchReviewIfEligible(this);
                Toast.makeText(this, "Download started, see the notification", Toast.LENGTH_SHORT).show();
            } catch (Exception e) {
                e.printStackTrace();
                Toast.makeText(this, "Error starting download", Toast.LENGTH_SHORT).show();
            }
        });

        btnShare.setOnClickListener(v -> shareImage());


    }

    public void downloadImage(String imageUrl) {
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

    private void shareImage() {
        if (downloadedImageName == null) {
            Toast.makeText(this, "No image found to share! Please download first.", Toast.LENGTH_SHORT).show();
            return;
        }

        File imageFile = new File(Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DOWNLOADS), downloadedImageName);

        if (!imageFile.exists()) {
            Toast.makeText(this, "Image not found!", Toast.LENGTH_SHORT).show();
            return;
        }


        try {
            Uri imageUri = FileProvider.getUriForFile(this, getPackageName() + ".fileprovider", imageFile);

            grantUriPermission(getPackageName(), imageUri, Intent.FLAG_GRANT_READ_URI_PERMISSION);

            Intent shareIntent = new Intent(Intent.ACTION_SEND);
            shareIntent.setType("image/*");
            shareIntent.putExtra(Intent.EXTRA_STREAM, imageUri);
            shareIntent.addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION);

            startActivity(Intent.createChooser(shareIntent, "Share Image via"));
        } catch (Exception e) {
            e.printStackTrace();
            Toast.makeText(this, "Failed to share image: " + e.getMessage(), Toast.LENGTH_SHORT).show();
        }
    }
}