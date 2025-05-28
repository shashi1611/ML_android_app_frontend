package com.prasthaan.dusterai;

import android.annotation.SuppressLint;
import android.app.DownloadManager;
import android.content.Context;
import android.content.Intent;
import android.database.Cursor;
import android.net.Uri;
import android.os.Bundle;
import android.os.Environment;
import android.widget.ImageView;
import android.widget.Toast;

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.content.FileProvider;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;
import androidx.media3.common.MediaItem;
import androidx.media3.exoplayer.ExoPlayer;
import androidx.media3.ui.PlayerView;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.prasthaan.dusterai.Adapters.AdapterResultPencilSketchGeneration;
import com.prasthaan.dusterai.Models.ModelResultPencilSketchGeneration;

import java.io.File;
import java.io.FileOutputStream;
import java.io.InputStream;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.ArrayList;

public class ProcessedActivityPencilSketchGeneration extends AppCompatActivity {
    ExoPlayer player;
    private String downloadedImageName;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_processed_pencil_sketch_generation);
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main), (v, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);
            return insets;
        });
        Intent intent = getIntent();

//        <<<<<<<<<<<<<<<<<<<<<<<<<<video player>>>>>>>>>>>>>>>>>>>>>>>>>
        PlayerView playerView = findViewById(R.id.player_view);
        player = new ExoPlayer.Builder(this).build();
        playerView.setPlayer(player);
        String videoUrl = intent.getStringExtra("videoUrl");

        MediaItem mediaItem = MediaItem.fromUri(videoUrl); // your presigned S3 video URL
        player.setMediaItem(mediaItem);
        player.prepare();
        player.play();


//<<<<<<<<<<<<<<<<<<<<<<<<<<<<image results recycler view >>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>
        RecyclerView recyclerView = findViewById(R.id.result_recyclerView_pencil_sketch_generation);
        ArrayList<ModelResultPencilSketchGeneration> listImagePencilSketch = new ArrayList<>();


        ArrayList<String> results = intent.getStringArrayListExtra("resultUrls");

        if (results != null && !results.isEmpty()) {
            for (String url : results) {
//                Log.d("response from pencil", "onCreate: the resultUrl =  " + url);
                listImagePencilSketch.add(new ModelResultPencilSketchGeneration(url, "edge"));
            }

            recyclerView.setLayoutManager(new LinearLayoutManager(this)); // vertical layout
            AdapterResultPencilSketchGeneration adapter = new AdapterResultPencilSketchGeneration(listImagePencilSketch, this);
            recyclerView.setAdapter(adapter);
        }

//        <<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<download video >>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>
        ImageView btnDownload = findViewById(R.id.btn_download_video);

        btnDownload.setOnClickListener(v -> {
            // Click effect: scale + alpha
            v.animate()
                    .scaleX(0.85f)
                    .scaleY(0.85f)
                    .alpha(0.6f)
                    .setDuration(100)
                    .withEndAction(() -> {
                        v.animate()
                                .scaleX(1f)
                                .scaleY(1f)
                                .alpha(1f)
                                .setDuration(100)
                                .start();

                        // Download code
//                        String videoUrl = getIntent().getStringExtra("videoUrl");
                        if (videoUrl != null) {
                            DownloadManager downloadManager = (DownloadManager) getSystemService(Context.DOWNLOAD_SERVICE);
                            Uri uri = Uri.parse(videoUrl);

                            DownloadManager.Request request = new DownloadManager.Request(uri);
                            request.setTitle("Downloading video");
                            request.setDescription("Saving video to Downloads");
                            request.setNotificationVisibility(DownloadManager.Request.VISIBILITY_VISIBLE_NOTIFY_COMPLETED);
                            request.setDestinationInExternalPublicDir(Environment.DIRECTORY_DOWNLOADS, "sketch_video.mp4");

                            downloadManager.enqueue(request);
                            Toast.makeText(this, "Download started...", Toast.LENGTH_SHORT).show();
                        }
                    })
                    .start();
        });


    }

    @Override
    protected void onStop() {
        super.onStop();
        if (player != null) {
            player.release();
        }
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