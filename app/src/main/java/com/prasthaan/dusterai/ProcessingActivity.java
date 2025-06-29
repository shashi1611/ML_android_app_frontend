package com.prasthaan.dusterai;

import android.Manifest;
import android.app.Dialog;
import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.ImageDecoder;
import android.graphics.Matrix;
import android.media.ExifInterface;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.os.CountDownTimer;
import android.os.Environment;
import android.provider.MediaStore;
import android.util.DisplayMetrics;
import android.view.View;
import android.view.WindowManager;
import android.widget.Button;
import android.widget.FrameLayout;
import android.widget.ImageView;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.Toast;

import androidx.activity.EdgeToEdge;
import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.PickVisualMediaRequest;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.core.app.ActivityCompat;
import androidx.core.app.NotificationCompat;
import androidx.core.app.NotificationManagerCompat;
import androidx.core.content.ContextCompat;
import androidx.core.content.FileProvider;

import com.bumptech.glide.Glide;
import com.google.android.gms.ads.AdRequest;
import com.google.android.gms.ads.AdSize;
import com.google.android.gms.ads.AdView;
import com.google.android.gms.ads.MobileAds;
import com.google.android.material.bottomnavigation.BottomNavigationView;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.Locale;
import java.util.Objects;

import okhttp3.MediaType;
import okhttp3.MultipartBody;
import okhttp3.RequestBody;
import okhttp3.ResponseBody;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;


public class ProcessingActivity extends BaseMenuActivity {
    private static final int REQUEST_IMAGE_CAPTURE = 1;
    private static final int REQUEST_PERMISSIONS = 101;
    private static final int TOTAL_TIME = 90; // 120 seconds
    private static final int TOTAL_TIME_SMALL_PENCIL = 120; // 120 seconds
    private static final int TOTAL_TIME_RESTORE_IMAGE = 90; // 120 seconds
    private static final int TOTAL_TIME_ENHANCE_IMAGE_2X = 90; // 120 seconds
    private static final int TOTAL_TIME_ENHANCE_IMAGE_4X = 180; // 120 seconds
    private static final String AD_UNIT_ID = "ca-app-pub-4827086355311757/2017201353111458692";
    String development_test_ad = "ca-app-pub-3940256099942544/9214589741";
    String receivedText;
    ActivityResultLauncher<PickVisualMediaRequest> imagePickerLauncher;
    String presignedUrl = "";
    boolean flagNewImage = false;
    ArrayList<String> faceUrls;
    TextView minimizeAppTextView;
    TextView textView1;
    ArrayList<String> resultUrl = null;
    String videoUrl = "";
    ImageView open_cam_btn;
    RelativeLayout btnUpload;
    private ImageView imageView;
    private Uri imageUri;
    private File imageFileFromCamera = null;
    private File imageFileFromFileUploader;
    private Button btnProcess;
    private Dialog progressDialog;
    private TextView timerText;
    private CountDownTimer countDownTimer;
    private AdView adView;
    private FrameLayout adContainerView;
    private boolean isAppInForeground = true;
    private String pendingPresignedUrl = null;
    private String pendingRestoredImageUrl = null;
    private ArrayList<String> pendingRestoredFaceUrls = null;
    private ArrayList<String> pendingSketchResultUrl = null;
    private String pendingSketchVideoUrls = null;

    @Override
    protected void onResume() {
        super.onResume();
        isAppInForeground = true;

        if (pendingPresignedUrl != null) {
            goToProcessedActivity(pendingPresignedUrl);
            pendingPresignedUrl = null;
        }
        if (pendingRestoredImageUrl != null && pendingRestoredFaceUrls != null) {
            goToProcessedActivityRestored(pendingRestoredImageUrl, pendingRestoredFaceUrls);
            pendingRestoredImageUrl = null;
            pendingRestoredFaceUrls = null;
        }
    }

    @Override
    protected void onPause() {
        super.onPause();
        isAppInForeground = false;
    }

    private void showProcessingDialog(String process) {
        progressDialog = new Dialog(this);
        progressDialog.show();
        progressDialog.setContentView(R.layout.progress_dialog);
        progressDialog.setCancelable(false);
        progressDialog.getWindow().setBackgroundDrawableResource(
                android.R.color.transparent
        );

        // Initialize countdown
        timerText = progressDialog.findViewById(R.id.timer_text);
        minimizeAppTextView = progressDialog.findViewById(R.id.minimize_app_textview);
        startCountdown(process);
        if (Objects.equals(process, "Restore image") || Objects.equals(process, "Enhance resolution 2X")) {
            minimizeAppTextView.setVisibility(View.VISIBLE);
        }
    }

    private void startCountdown(String process) {
        int total_time;
        if (Objects.equals(process, "Restore image")) {
            total_time = TOTAL_TIME_RESTORE_IMAGE;
        } else if (Objects.equals(process, "Enhance 2X")) {
            total_time = TOTAL_TIME_ENHANCE_IMAGE_2X;
        } else if (Objects.equals(process, "Enhance 4X")) {
            total_time = TOTAL_TIME_ENHANCE_IMAGE_4X;
        } else if (Objects.equals(process, "✨ Soft Sketch")) {
            total_time = TOTAL_TIME_SMALL_PENCIL;
        } else {
            total_time = TOTAL_TIME;
        }
        countDownTimer = new CountDownTimer(total_time * 1000, 1000) {
            @Override
            public void onTick(long millisUntilFinished) {
                int secondsLeft = (int) (millisUntilFinished / 1000);
                timerText.setText("Generating: " + secondsLeft + "s remaining");
            }

            @Override
            public void onFinish() {
                timerText.setText("Still processing your image is heavy please keep patience!");
//                dismissDialog();
            }
        }.start();
    }

    private void dismissDialog() {
        if (progressDialog != null && progressDialog.isShowing()) {
            countDownTimer.cancel();
            progressDialog.dismiss();
        }
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        if (countDownTimer != null) {
            countDownTimer.cancel();
        }
    }

    private void requestPermissions() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            if (checkSelfPermission(Manifest.permission.CAMERA) != PackageManager.PERMISSION_GRANTED ||
                    checkSelfPermission(Manifest.permission.READ_EXTERNAL_STORAGE) != PackageManager.PERMISSION_GRANTED) {

                requestPermissions(new String[]{
                        Manifest.permission.CAMERA,
                        Manifest.permission.READ_EXTERNAL_STORAGE
                }, REQUEST_PERMISSIONS);
            }
        }
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        if (requestCode == REQUEST_PERMISSIONS) {
            if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                Toast.makeText(this, "Permissions granted!", Toast.LENGTH_SHORT).show();
            } else {
                Toast.makeText(this, "Permissions denied! Camera and gallery won't work.", Toast.LENGTH_SHORT).show();
            }
        }
    }

    private boolean isCameraPermissionGranted() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            return ContextCompat.checkSelfPermission(this, Manifest.permission.CAMERA)
                    == PackageManager.PERMISSION_GRANTED;
        } else {
            // Permissions are granted at install time for Android 5.1 and below
            return true;
        }
    }

    private void requestPermissionIfNeeded() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            if (checkSelfPermission(Manifest.permission.READ_MEDIA_IMAGES) != PackageManager.PERMISSION_GRANTED) {
                requestPermissions(new String[]{Manifest.permission.READ_MEDIA_IMAGES}, 1001);
            }
        } else {
            if (checkSelfPermission(Manifest.permission.READ_EXTERNAL_STORAGE) != PackageManager.PERMISSION_GRANTED) {
                requestPermissions(new String[]{Manifest.permission.READ_EXTERNAL_STORAGE}, 1001);
            }
        }
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
        setContentView(R.layout.activity_processing);
        imageView = findViewById(R.id.uploaded_img);
        btnUpload = findViewById(R.id.imageTemplateContainer);
        btnProcess = findViewById(R.id.process_img_button);
        open_cam_btn = findViewById(R.id.open_camera_button);

        textView1 = findViewById(R.id.uploadTextSingleFace);

        BottomNavigationView bottomNavigationView = findViewById(R.id.bottom_navigation);
        setupBottomNavigation(bottomNavigationView, -1);


        receivedText = getIntent().getStringExtra("text_key");
        TextView textViewFeatname = findViewById(R.id.featureNameTextView);

        createNotificationChannel();

        if (receivedText.equals("To winter") || receivedText.equals("To Summer")) {
            textViewFeatname.setText(receivedText);
        } else {
            textViewFeatname.setText(receivedText);
        }

        if (!isCameraPermissionGranted()) {
            requestPermissions();
        }

        open_cam_btn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (ContextCompat.checkSelfPermission(ProcessingActivity.this, Manifest.permission.CAMERA)
                        == PackageManager.PERMISSION_GRANTED) {


                    Intent intent = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);
                    imageFileFromCamera = null;
                    try {
                        imageFileFromCamera = createImageFile();
                    } catch (IOException e) {
                        throw new RuntimeException(e);
                    }
                    if (imageFileFromCamera != null) {

                        imageUri = FileProvider.getUriForFile(ProcessingActivity.this, "com.prasthaan.dusterai.fileprovider", imageFileFromCamera);
                        intent.putExtra(MediaStore.EXTRA_OUTPUT, imageUri);
                        intent.addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION);
                        startActivityForResult(intent, REQUEST_IMAGE_CAPTURE);
                    }
                } else {
                    Toast.makeText(ProcessingActivity.this, "Camera permission is required", Toast.LENGTH_SHORT).show();
                }

            }

            private File createImageFile() throws IOException {
                String timestamp = new SimpleDateFormat("yyyyMMdd_HHmmss", Locale.getDefault()).format(new Date());
                String imageFileName = "IMG_" + timestamp + "_";
                File storageDir = getExternalFilesDir(Environment.DIRECTORY_PICTURES);
                return File.createTempFile(imageFileName, ".jpg", storageDir);
            }
        });

        // Select Image
        btnUpload.setOnClickListener(view -> openGallery());

        btnProcess.setOnClickListener(view -> {
            if (Objects.equals(receivedText, "✨ Soft Sketch") || Objects.equals(receivedText, "✏\uFE0F Classic Sketch") || Objects.equals(receivedText, "\uD83D\uDD8C\uFE0F Bold Sketch")) {
                if (videoUrl.isEmpty() || flagNewImage) {
                    if (imageFileFromFileUploader != null) {
                        showProcessingDialog(receivedText);
                        processImage(imageFileFromFileUploader);
                    } else if (imageFileFromCamera != null) {
                        showProcessingDialog(receivedText);
                        processImage(imageFileFromCamera);

                    } else {
                        Toast.makeText(this, "Select an image first", Toast.LENGTH_SHORT).show();
                    }
                } else {
                    Intent intent = new Intent(ProcessingActivity.this, ProcessedActivityPencilSketchGeneration.class);
                    intent.putStringArrayListExtra("resultUrls", resultUrl);
                    intent.putExtra("videoUrl", videoUrl);
                    startActivity(intent);
                }

            } else if (presignedUrl.isEmpty() || flagNewImage) {


                if (imageFileFromFileUploader != null) {
                    showProcessingDialog(receivedText);
                    processImage(imageFileFromFileUploader);
                } else if (imageFileFromCamera != null) {
                    showProcessingDialog(receivedText);
                    processImage(imageFileFromCamera);

                } else {
                    Toast.makeText(this, "Select an image first", Toast.LENGTH_SHORT).show();
                }
            } else {
                if (Objects.equals(receivedText, "Restore image")) {
                    Intent intent = new Intent(ProcessingActivity.this, ProcessedActivityRestoredImg.class);
                    intent.putExtra("RESTORED_IMAGE_URL", presignedUrl);
                    intent.putStringArrayListExtra("RESTORED_FACE_URLS", faceUrls);
                    startActivity(intent);
                } else {
                    Intent intent = new Intent(ProcessingActivity.this, ProcessedActivity.class);
                    intent.putExtra("PRESIGNED_URL", presignedUrl);
                    startActivity(intent);
                }
            }


        });


//        <<<<<<<<<<<<<<<<<<<<<<<<<<<<<ad part >>>>>>>>>>>>>>>>>>>>>>>>>>>>>

        MobileAds.initialize(this, initializationStatus -> {
        });

        // Find Ad Container
        adContainerView = findViewById(R.id.ad_view_container);

        // Create a new AdView and load an ad
        loadAdaptiveBannerAd();

        imagePickerLauncher =
                registerForActivityResult(new ActivityResultContracts.PickVisualMedia(), uri -> {
                    if (uri != null) {
                        RelativeLayout.LayoutParams params = new RelativeLayout.LayoutParams(
                                RelativeLayout.LayoutParams.MATCH_PARENT,
                                RelativeLayout.LayoutParams.MATCH_PARENT
                        );
                        params.addRule(RelativeLayout.CENTER_IN_PARENT, RelativeLayout.TRUE);
                        imageView.setLayoutParams(params);
                        imageView.setImageTintList(null);
                        imageView.setScaleType(ImageView.ScaleType.FIT_CENTER);
                        textView1.setVisibility(View.GONE);
                        Glide.with(this)
                                .load(uri)
                                .override(1024, 1024) // limit size
                                .into(imageView);
                        imageFileFromFileUploader = copyUriToFile(uri);
                        flagNewImage = true;

                    } else {
                        Toast.makeText(this, "No image selected", Toast.LENGTH_SHORT).show();
                    }
                });
    }


    //    <<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<camera image capture on activity result>>>>>>>>>>>>>>>>>>>>>>>>>>
//    @Override
//    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
//        super.onActivityResult(requestCode, resultCode, data);
//        if (requestCode == REQUEST_IMAGE_CAPTURE) {
//            if (imageFileFromCamera != null) {
//                RelativeLayout.LayoutParams params = new RelativeLayout.LayoutParams(
//                        RelativeLayout.LayoutParams.MATCH_PARENT,
//                        RelativeLayout.LayoutParams.MATCH_PARENT
//                );
//                params.addRule(RelativeLayout.CENTER_IN_PARENT, RelativeLayout.TRUE);
//                imageView.setLayoutParams(params);
//                imageView.setImageTintList(null);
//                imageView.setScaleType(ImageView.ScaleType.FIT_CENTER);
//                textView1.setVisibility(View.GONE);
//                imageUri = Uri.fromFile(imageFileFromCamera);
//                Glide.with(this)
//                        .load(imageUri)
//                        .override(1024, 1024) // limit size
//                        .into(imageView);
//                imageFileFromCamera = copyUriToFile(imageUri);
//                flagNewImage = true;
//
//
//            }
//        }
//
//    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        if (requestCode == REQUEST_IMAGE_CAPTURE && resultCode == RESULT_OK) {
            if (imageFileFromCamera != null && imageFileFromCamera.exists()) {
                RelativeLayout.LayoutParams params = new RelativeLayout.LayoutParams(
                        RelativeLayout.LayoutParams.MATCH_PARENT,
                        RelativeLayout.LayoutParams.MATCH_PARENT
                );
                params.addRule(RelativeLayout.CENTER_IN_PARENT, RelativeLayout.TRUE);
                imageView.setLayoutParams(params);
                imageView.setImageTintList(null);
                imageView.setScaleType(ImageView.ScaleType.FIT_CENTER);
                textView1.setVisibility(View.GONE);
                imageUri = Uri.fromFile(imageFileFromCamera);
                Glide.with(this)
                        .load(imageUri)
                        .override(1024, 1024)
                        .into(imageView);
                imageFileFromCamera = copyUriToFile(imageUri);
                flagNewImage = true;
            }
        } else {
            // Handle cancel or failure here if needed
            imageFileFromCamera = null;
//            Toast.makeText(this, "No image Taken", Toast.LENGTH_SHORT).show();
        }
    }


    private File copyUriToFile(Uri uri) {
        File finalFile = new File(getCacheDir(), "original_image.jpg");

        try {
            Bitmap bitmap;

            // Decode HEIC/HEIF using ImageDecoder (for Android 9+)
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.P) {
                ImageDecoder.Source source = ImageDecoder.createSource(getContentResolver(), uri);
                bitmap = ImageDecoder.decodeBitmap(source);
            } else {
                // Fallback for older devices: try using MediaStore
                bitmap = MediaStore.Images.Media.getBitmap(getContentResolver(), uri);
            }

            // Save bitmap as JPG
            try (OutputStream out = new FileOutputStream(finalFile)) {
                bitmap.compress(Bitmap.CompressFormat.JPEG, 100, out);
            }

        } catch (IOException e) {
            e.printStackTrace();

            // fallback: try regular copy if decode fails
            try (InputStream in = getContentResolver().openInputStream(uri);
                 OutputStream out = new FileOutputStream(finalFile)) {

                byte[] buffer = new byte[1024];
                int len;
                while ((len = in.read(buffer)) > 0) {
                    out.write(buffer, 0, len);
                }

            } catch (IOException ex) {
                ex.printStackTrace();
            }
        }

        long fileSizeInMB = finalFile.length() / (1024 * 1024);
        if (fileSizeInMB > 4) {
            return compressImage(finalFile);
        }

        return finalFile;
    }


    private File compressImage(File inputFile) {
        try {
            // Step 1: Decode bitmap
            BitmapFactory.Options options = new BitmapFactory.Options();
            Bitmap bitmap = BitmapFactory.decodeFile(inputFile.getAbsolutePath(), options);

            // Step 2: Read EXIF orientation
            ExifInterface exif = new ExifInterface(inputFile.getAbsolutePath());
            int orientation = exif.getAttributeInt(ExifInterface.TAG_ORIENTATION, ExifInterface.ORIENTATION_NORMAL);

            // Step 3: Rotate bitmap if needed
            Bitmap rotatedBitmap = rotateBitmapIfRequired(bitmap, orientation);

            // Step 4: Save compressed image
            File compressedFile = new File(getCacheDir(), "compressed_image.jpg");
            FileOutputStream out = new FileOutputStream(compressedFile);
            if (inputFile.length() / (1024 * 1024) > 10) {
                rotatedBitmap.compress(Bitmap.CompressFormat.JPEG, 50, out);
            } else {
                rotatedBitmap.compress(Bitmap.CompressFormat.JPEG, 75, out);
            }

            out.flush();
            out.close();

            // Cleanup
            bitmap.recycle();
            rotatedBitmap.recycle();

            return compressedFile;

        } catch (IOException e) {
            e.printStackTrace();
            return inputFile; // fallback
        }
    }

    private Bitmap rotateBitmapIfRequired(Bitmap bitmap, int orientation) {
        Matrix matrix = new Matrix();

        switch (orientation) {
            case ExifInterface.ORIENTATION_ROTATE_90:
                matrix.postRotate(90);
                break;
            case ExifInterface.ORIENTATION_ROTATE_180:
                matrix.postRotate(180);
                break;
            case ExifInterface.ORIENTATION_ROTATE_270:
                matrix.postRotate(270);
                break;
            case ExifInterface.ORIENTATION_FLIP_HORIZONTAL:
                matrix.postScale(-1, 1);
                break;
            case ExifInterface.ORIENTATION_FLIP_VERTICAL:
                matrix.postScale(1, -1);
                break;
            default:
                return bitmap; // no transformation needed
        }

        return Bitmap.createBitmap(bitmap, 0, 0, bitmap.getWidth(), bitmap.getHeight(), matrix, true);
    }

    //    <<<<<<<<<<<<<<<<<<<<<<<<<<<<<ad functions>>>>>>>>>>>>>>>>>>>>>>>>>
    private void loadAdaptiveBannerAd() {
        // Create a new AdView dynamically
        adView = new AdView(this);
        adView.setAdUnitId(AD_UNIT_ID); //prod ad
//        adView.setAdUnitId(development_test_ad); //test ad

        // Set adaptive ad size
        AdSize adSize = getAdSize();
        adView.setAdSize(adSize);

        // Add AdView to container
        adContainerView.removeAllViews();
        adContainerView.addView(adView);

        // Load the ad
        AdRequest adRequest = new AdRequest.Builder().build();
        adView.loadAd(adRequest);
    }

    //    <<<<<<<<<<<<<<<<<<<<<<<<<<<<<get image from gallery and pass to the api >>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>

    private AdSize getAdSize() {
        // Get screen width in dp
        DisplayMetrics displayMetrics = getResources().getDisplayMetrics();
        float widthPixels = displayMetrics.widthPixels;
        float density = displayMetrics.density;
        int adWidth = (int) (widthPixels / density);

        // Get adaptive ad size
        return AdSize.getCurrentOrientationAnchoredAdaptiveBannerAdSize(this, adWidth);
    }

    private void openGallery() {
        requestPermissionIfNeeded();  // Always check before picker
        imagePickerLauncher.launch(new PickVisualMediaRequest.Builder()
                .setMediaType(ActivityResultContracts.PickVisualMedia.ImageOnly.INSTANCE)
                .build());

    }

    private void processImage(File file) {
        RequestBody requestFile = RequestBody.create(MediaType.parse("image/*"), file);
        MultipartBody.Part body = MultipartBody.Part.createFormData("file", file.getName(), requestFile);


        ApiService apiService = RetrofitClient.getApiService();
        if (Objects.equals(receivedText, "Enhance 2X")) {
            ApiService apiServiceImageEnhancer2x = RetrofitClient.getApiServiceImageEnhance();
            apiServiceImageEnhancer2x.executeProcessingEnhanceImage2x(body).enqueue(new Callback<ResponseBody>() {
                @Override
                public void onResponse(Call<ResponseBody> call, Response<ResponseBody> response) {
                    if (response.isSuccessful()) {
                        dismissDialog();
                        try {
                            presignedUrl = response.body().string();
                            flagNewImage = false;
                            if (isAppInForeground) {
                                goToProcessedActivity(presignedUrl);
//                                goToProcessedActivity(presignedUrl, imageUpscaling);
                            } else {
                                pendingPresignedUrl = presignedUrl;
                                sendProcessingCompletedNotification();
//                                sendProcessingCompletedNotification(imageUpscaling);
                            }
                            flagNewImage = false;
                        } catch (IOException e) {
                            dismissDialog();
                            e.printStackTrace();
                        }
                    } else {
                        dismissDialog();
                        Toast.makeText(ProcessingActivity.this, "Processing Failed", Toast.LENGTH_SHORT).show();
                    }
                }

                @Override
                public void onFailure(Call<ResponseBody> call, Throwable t) {
                    dismissDialog();
                    Toast.makeText(ProcessingActivity.this, "Error: " + t.getMessage(), Toast.LENGTH_SHORT).show();
                }
            });

        } else if (Objects.equals(receivedText, "Enhance 4X")) {
            ApiService apiServiceImageEnhancer4x = RetrofitClient.getApiServiceImageEnhance();
            apiServiceImageEnhancer4x.executeProcessingEnhanceImage4x(body).enqueue(new Callback<ResponseBody>() {
                @Override
                public void onResponse(Call<ResponseBody> call, Response<ResponseBody> response) {
                    if (response.isSuccessful()) {
                        dismissDialog();
                        try {
                            presignedUrl = response.body().string();
                            flagNewImage = false;
                            if (isAppInForeground) {
                                goToProcessedActivity(presignedUrl);
//                                goToProcessedActivity(presignedUrl, imageUpscaling);
                            } else {
                                pendingPresignedUrl = presignedUrl;
                                sendProcessingCompletedNotification();
//                                sendProcessingCompletedNotification(imageUpscaling);
                            }
                            flagNewImage = false;
                        } catch (IOException e) {
                            dismissDialog();
                            e.printStackTrace();
                        }
                    } else {
                        dismissDialog();
                        Toast.makeText(ProcessingActivity.this, "Processing Failed", Toast.LENGTH_SHORT).show();
                    }
                }

                @Override
                public void onFailure(Call<ResponseBody> call, Throwable t) {
                    dismissDialog();
                    Toast.makeText(ProcessingActivity.this, "Error: " + t.getMessage(), Toast.LENGTH_SHORT).show();
                }
            });

        } else if (Objects.equals(receivedText, "Restore image")) {

            ApiService apiServiceRestoreImage = RetrofitClient.getApiServiceImageRestore();
            apiServiceRestoreImage.executeProcessingRestoreImage(body).enqueue(new Callback<RestoreImageResponse>() {
                @Override
                public void onResponse(Call<RestoreImageResponse> call, Response<RestoreImageResponse> response) {
                    dismissDialog();
                    if (response.isSuccessful() && response.body() != null) {

                        RestoreImageResponse result = response.body();
                        faceUrls = new ArrayList<>(result.getRestoredFaces());
                        presignedUrl = result.getRestoredImage();
                        flagNewImage = false;

                        if (isAppInForeground) {
                            goToProcessedActivityRestored(presignedUrl, faceUrls);
                        } else {
                            pendingRestoredImageUrl = presignedUrl;
                            pendingRestoredFaceUrls = faceUrls;
                            sendRestoreCompletedNotification();

                        }

                    } else {
                        Toast.makeText(ProcessingActivity.this, "Processing Failed", Toast.LENGTH_SHORT).show();
                    }
                }

                @Override
                public void onFailure(Call<RestoreImageResponse> call, Throwable t) {
                    dismissDialog();
                    Toast.makeText(ProcessingActivity.this, "Error: " + t.getMessage(), Toast.LENGTH_LONG).show();
                }
            });


        } else if (Objects.equals(receivedText, "✨ Soft Sketch")) {
            ApiService apiServicePencilSketchGeneration = RetrofitClient.getApiServicePencilSketchGeneration();
            apiServicePencilSketchGeneration.executeProcessingGenerateSketchSmallPencil(body).enqueue(new Callback<PencilSketchGenerationResponse>() {
                @Override
                public void onResponse(Call<PencilSketchGenerationResponse> call, Response<PencilSketchGenerationResponse> response) {
                    dismissDialog();
                    if (response.isSuccessful() && response.body() != null) {

                        PencilSketchGenerationResponse result = response.body();
                        resultUrl = new ArrayList<>(result.getResultsImage());
                        videoUrl = result.getResultsVideo();
                        Intent intent = new Intent(ProcessingActivity.this, ProcessedActivityPencilSketchGeneration.class);
                        intent.putStringArrayListExtra("resultUrls", resultUrl);
                        intent.putExtra("videoUrl", videoUrl);
                        startActivity(intent);

                        flagNewImage = false;

                        if (isAppInForeground) {
                            goToProcessedActivityPencilSketchGeneration(videoUrl, resultUrl);
//                            goToProcessedActivityPencilSketchGeneration(videoUrl, resultUrl, sketch);
                        } else {
                            pendingSketchResultUrl = resultUrl;
                            pendingSketchVideoUrls = videoUrl;
                            sendPencilSketchGenerationNotification();
//                            sendPencilSketchGenerationNotification(sketch);
                        }

                    } else {
                        Toast.makeText(ProcessingActivity.this, "Processing Failed", Toast.LENGTH_SHORT).show();
                    }
                }

                @Override
                public void onFailure(Call<PencilSketchGenerationResponse> call, Throwable t) {
                    dismissDialog();
                    Toast.makeText(ProcessingActivity.this, "Error: " + t.getMessage(), Toast.LENGTH_LONG).show();
                }
            });

        } else if (Objects.equals(receivedText, "✏\uFE0F Classic Sketch")) {

            ApiService apiServicePencilSketchGeneration = RetrofitClient.getApiServicePencilSketchGeneration();
            apiServicePencilSketchGeneration.executeProcessingGenerateSketchMediumPencil(body).enqueue(new Callback<PencilSketchGenerationResponse>() {
                @Override
                public void onResponse(Call<PencilSketchGenerationResponse> call, Response<PencilSketchGenerationResponse> response) {
                    dismissDialog();
                    if (response.isSuccessful() && response.body() != null) {
                        PencilSketchGenerationResponse result = response.body();
                        resultUrl = new ArrayList<>(result.getResultsImage());
                        videoUrl = result.getResultsVideo();
                        Intent intent = new Intent(ProcessingActivity.this, ProcessedActivityPencilSketchGeneration.class);
                        intent.putStringArrayListExtra("resultUrls", resultUrl);
                        intent.putExtra("videoUrl", videoUrl);
//                        intent.putExtra("featName", sketch);
                        startActivity(intent);

                        flagNewImage = false;
                        if (isAppInForeground) {
                            goToProcessedActivityPencilSketchGeneration(videoUrl, resultUrl);
//                            goToProcessedActivityPencilSketchGeneration(videoUrl, resultUrl, sketch);
                        } else {
                            pendingSketchResultUrl = resultUrl;
                            pendingSketchVideoUrls = videoUrl;
                            sendPencilSketchGenerationNotification();
//                            sendPencilSketchGenerationNotification(sketch);
                        }
                    } else {
                        Toast.makeText(ProcessingActivity.this, "Processing Failed", Toast.LENGTH_SHORT).show();
                    }
                }

                @Override
                public void onFailure(Call<PencilSketchGenerationResponse> call, Throwable t) {
                    dismissDialog();
                    Toast.makeText(ProcessingActivity.this, "Error: " + t.getMessage(), Toast.LENGTH_LONG).show();
                }
            });

        } else if (Objects.equals(receivedText, "\uD83D\uDD8C\uFE0F Bold Sketch")) {

            ApiService apiServicePencilSketchGeneration = RetrofitClient.getApiServicePencilSketchGeneration();
            apiServicePencilSketchGeneration.executeProcessingGenerateSketchLargePencil(body).enqueue(new Callback<PencilSketchGenerationResponse>() {
                @Override
                public void onResponse(Call<PencilSketchGenerationResponse> call, Response<PencilSketchGenerationResponse> response) {
                    dismissDialog();
                    if (response.isSuccessful() && response.body() != null) {
                        PencilSketchGenerationResponse result = response.body();
                        resultUrl = new ArrayList<>(result.getResultsImage());
                        videoUrl = result.getResultsVideo();
                        Intent intent = new Intent(ProcessingActivity.this, ProcessedActivityPencilSketchGeneration.class);
                        intent.putStringArrayListExtra("resultUrls", resultUrl);
                        intent.putExtra("videoUrl", videoUrl);
//                        intent.putExtra("featName", sketch);
                        startActivity(intent);

                        flagNewImage = false;
                        if (isAppInForeground) {
                            goToProcessedActivityPencilSketchGeneration(videoUrl, resultUrl);
//                            goToProcessedActivityPencilSketchGeneration(videoUrl, resultUrl, sketch);
                        } else {
                            pendingSketchResultUrl = resultUrl;
                            pendingSketchVideoUrls = videoUrl;
                            sendPencilSketchGenerationNotification();
//                            sendPencilSketchGenerationNotification(sketch);
                        }
                    } else {
                        Toast.makeText(ProcessingActivity.this, "Processing Failed", Toast.LENGTH_SHORT).show();
                    }
                }

                @Override
                public void onFailure(Call<PencilSketchGenerationResponse> call, Throwable t) {
                    dismissDialog();
                    Toast.makeText(ProcessingActivity.this, "Error: " + t.getMessage(), Toast.LENGTH_LONG).show();
                }
            });

        } else if (Objects.equals(receivedText, "Ukiyo-e style")) {
            apiService.executeProcessingUkiyoe(body).enqueue(new Callback<ResponseBody>() {
                @Override
                public void onResponse(Call<ResponseBody> call, Response<ResponseBody> response) {
                    if (response.isSuccessful()) {
                        dismissDialog();
                        try {


                            presignedUrl = response.body().string();
                            flagNewImage = false;
                            Intent intent = new Intent(ProcessingActivity.this, ProcessedActivity.class);
                            intent.putExtra("PRESIGNED_URL", presignedUrl);
//                            intent.putExtra("featName", imagePainting);
                            startActivity(intent);
                            if (isAppInForeground) {
                                goToProcessedActivity(presignedUrl);
//                                goToProcessedActivity(presignedUrl, imagePainting);
                            } else {
                                pendingPresignedUrl = presignedUrl;
                                sendProcessingCompletedNotification();
//                                sendProcessingCompletedNotification(imagePainting);
                            }
                        } catch (IOException e) {
                            dismissDialog();
                            e.printStackTrace();
                        }
                    } else {
                        dismissDialog();
                        Toast.makeText(ProcessingActivity.this, "Processing Failed", Toast.LENGTH_SHORT).show();
                    }
                }

                @Override
                public void onFailure(Call<ResponseBody> call, Throwable t) {
                    dismissDialog();
                    Toast.makeText(ProcessingActivity.this, "Error: " + t.getMessage(), Toast.LENGTH_SHORT).show();
                }
            });
        } else if (Objects.equals(receivedText, "Monet style")) {
            apiService.executeProcessingMonet(body).enqueue(new Callback<ResponseBody>() {
                @Override
                public void onResponse(Call<ResponseBody> call, Response<ResponseBody> response) {
                    if (response.isSuccessful()) {
                        dismissDialog();
                        try {
                            presignedUrl = response.body().string();
                            flagNewImage = false;
                            Intent intent = new Intent(ProcessingActivity.this, ProcessedActivity.class);
                            intent.putExtra("PRESIGNED_URL", presignedUrl);
//                            intent.putExtra("featName", imagePainting);
                            startActivity(intent);
                            if (isAppInForeground) {
                                goToProcessedActivity(presignedUrl);
//                                goToProcessedActivity(presignedUrl, imagePainting);
                            } else {
                                pendingPresignedUrl = presignedUrl;
                                sendProcessingCompletedNotification();
//                                sendProcessingCompletedNotification(imagePainting);
                            }
                        } catch (IOException e) {
                            dismissDialog();
                            e.printStackTrace();
                        }
                    } else {
                        dismissDialog();
                        Toast.makeText(ProcessingActivity.this, "Processing Failed", Toast.LENGTH_SHORT).show();
                    }
                }

                @Override
                public void onFailure(Call<ResponseBody> call, Throwable t) {
                    dismissDialog();
                    Toast.makeText(ProcessingActivity.this, "Error: " + t.getMessage(), Toast.LENGTH_SHORT).show();
                }
            });

        } else if (Objects.equals(receivedText, "Van Gogh style")) {
            apiService.executeProcessingVangogh(body).enqueue(new Callback<ResponseBody>() {
                @Override
                public void onResponse(Call<ResponseBody> call, Response<ResponseBody> response) {
                    if (response.isSuccessful()) {
                        dismissDialog();
                        try {
                            presignedUrl = response.body().string();
                            flagNewImage = false;
                            Intent intent = new Intent(ProcessingActivity.this, ProcessedActivity.class);
                            intent.putExtra("PRESIGNED_URL", presignedUrl);
//                            intent.putExtra("featName", imagePainting);
                            startActivity(intent);
                            if (isAppInForeground) {
                                goToProcessedActivity(presignedUrl);
//                                goToProcessedActivity(presignedUrl, imagePainting);
                            } else {
                                pendingPresignedUrl = presignedUrl;
                                sendProcessingCompletedNotification();
//                                sendProcessingCompletedNotification(imagePainting);
                            }
                        } catch (IOException e) {
                            dismissDialog();
                            e.printStackTrace();
                        }
                    } else {
                        dismissDialog();
                        Toast.makeText(ProcessingActivity.this, "Processing Failed", Toast.LENGTH_SHORT).show();
                    }
                }

                @Override
                public void onFailure(Call<ResponseBody> call, Throwable t) {
                    dismissDialog();
                    Toast.makeText(ProcessingActivity.this, "Error: " + t.getMessage(), Toast.LENGTH_SHORT).show();
                }
            });

        } else if (Objects.equals(receivedText, "Cezanne style")) {
            apiService.executeProcessingCezanne(body).enqueue(new Callback<ResponseBody>() {
                @Override
                public void onResponse(Call<ResponseBody> call, Response<ResponseBody> response) {
                    if (response.isSuccessful()) {
                        dismissDialog();
                        try {
                            presignedUrl = response.body().string();
                            flagNewImage = false;
                            Intent intent = new Intent(ProcessingActivity.this, ProcessedActivity.class);
                            intent.putExtra("PRESIGNED_URL", presignedUrl);
//                            intent.putExtra("featName", imagePainting);
                            startActivity(intent);
                            if (isAppInForeground) {
                                goToProcessedActivity(presignedUrl);
//                                goToProcessedActivity(presignedUrl, imagePainting);
                            } else {
                                pendingPresignedUrl = presignedUrl;
                                sendProcessingCompletedNotification();
//                                sendProcessingCompletedNotification(imagePainting);
                            }
                        } catch (IOException e) {
                            dismissDialog();
                            e.printStackTrace();
                        }
                    } else {
                        dismissDialog();
                        Toast.makeText(ProcessingActivity.this, "Processing Failed", Toast.LENGTH_SHORT).show();
                    }
                }

                @Override
                public void onFailure(Call<ResponseBody> call, Throwable t) {
                    dismissDialog();
                    Toast.makeText(ProcessingActivity.this, "Error: " + t.getMessage(), Toast.LENGTH_SHORT).show();
                }
            });

        } else if (Objects.equals(receivedText, "To winter")) {
            apiService.executeProcessingSummer2Winter(body).enqueue(new Callback<ResponseBody>() {
                @Override
                public void onResponse(Call<ResponseBody> call, Response<ResponseBody> response) {
                    if (response.isSuccessful()) {
                        dismissDialog();
                        try {
                            presignedUrl = response.body().string();
                            flagNewImage = false;
                            Intent intent = new Intent(ProcessingActivity.this, ProcessedActivity.class);
                            intent.putExtra("PRESIGNED_URL", presignedUrl);
//                            intent.putExtra("featName", seasonChanger);
                            startActivity(intent);
                            if (isAppInForeground) {
                                goToProcessedActivity(presignedUrl);
//                                goToProcessedActivity(presignedUrl, seasonChanger);
                            } else {
                                pendingPresignedUrl = presignedUrl;
                                sendProcessingCompletedNotification();
//                                sendProcessingCompletedNotification(seasonChanger);
                            }
                        } catch (IOException e) {
                            dismissDialog();
                            e.printStackTrace();
                        }
                    } else {
                        dismissDialog();
                        Toast.makeText(ProcessingActivity.this, "Processing Failed", Toast.LENGTH_SHORT).show();
                    }
                }

                @Override
                public void onFailure(Call<ResponseBody> call, Throwable t) {
                    dismissDialog();
                    Toast.makeText(ProcessingActivity.this, "Error: " + t.getMessage(), Toast.LENGTH_SHORT).show();
                }
            });

        } else if (Objects.equals(receivedText, "To Summer")) {
            apiService.executeProcessingWinter2Summer(body).enqueue(new Callback<ResponseBody>() {
                @Override
                public void onResponse(Call<ResponseBody> call, Response<ResponseBody> response) {
                    dismissDialog();
                    if (response.isSuccessful()) {
                        try {
                            presignedUrl = response.body().string();
                            flagNewImage = false;
                            Intent intent = new Intent(ProcessingActivity.this, ProcessedActivity.class);
                            intent.putExtra("PRESIGNED_URL", presignedUrl);
//                            intent.putExtra("featName", seasonChanger);
                            startActivity(intent);
                            if (isAppInForeground) {
                                goToProcessedActivity(presignedUrl);
//                                goToProcessedActivity(presignedUrl, seasonChanger);
                            } else {
                                pendingPresignedUrl = presignedUrl;
                                sendProcessingCompletedNotification();
//                                sendProcessingCompletedNotification(seasonChanger);
                            }
                        } catch (IOException e) {
                            dismissDialog();
                            e.printStackTrace();
                        }
                    } else {
                        dismissDialog();
                        Toast.makeText(ProcessingActivity.this, "Processing Failed", Toast.LENGTH_SHORT).show();
                    }
                }

                @Override
                public void onFailure(Call<ResponseBody> call, Throwable t) {
                    dismissDialog();
                    Toast.makeText(ProcessingActivity.this, "Error: " + t.getMessage(), Toast.LENGTH_SHORT).show();
                }
            });

        } else {
            Toast.makeText(this, "something went wrong go back", Toast.LENGTH_SHORT).show();
        }

    }

    private boolean isConnected() {
        ConnectivityManager cm = (ConnectivityManager) getSystemService(Context.CONNECTIVITY_SERVICE);
        if (cm != null) {
            NetworkInfo activeNetwork = cm.getActiveNetworkInfo();
            return activeNetwork != null && activeNetwork.isConnected();
        }
        return false;
    }


    private void createNotificationChannel() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            CharSequence name = "ProcessingChannel";
            String description = "Channel for Processing Completion Notifications";
            int importance = NotificationManager.IMPORTANCE_DEFAULT;
            NotificationChannel channel = new NotificationChannel("processing_channel", name, importance);
            channel.setDescription(description);

            NotificationManager notificationManager = getSystemService(NotificationManager.class);
            notificationManager.createNotificationChannel(channel);
        }
    }

    //<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<background process for normal one >>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>
    private void sendProcessingCompletedNotification() {
        Intent intent = new Intent(this, ProcessedActivity.class);
        intent.putExtra("PRESIGNED_URL", pendingPresignedUrl);
//        intent.putExtra("featName", featName);
        intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
        PendingIntent pendingIntent = PendingIntent.getActivity(this, 0, intent, PendingIntent.FLAG_UPDATE_CURRENT | PendingIntent.FLAG_IMMUTABLE);

        NotificationCompat.Builder builder = new NotificationCompat.Builder(this, "processing_channel")
                .setSmallIcon(R.drawable.app_logo__icon) // <-- Use your app's icon here
                .setContentTitle("Processing Completed")
                .setContentText("Tap to view your image")
                .setPriority(NotificationCompat.PRIORITY_DEFAULT)
                .setContentIntent(pendingIntent)
                .setAutoCancel(true);

        NotificationManagerCompat notificationManager = NotificationManagerCompat.from(this);
        if (ActivityCompat.checkSelfPermission(this, Manifest.permission.POST_NOTIFICATIONS) != PackageManager.PERMISSION_GRANTED) {
            // TODO: Consider calling
            //    ActivityCompat#requestPermissions
            // here to request the missing permissions, and then overriding
            //   public void onRequestPermissionsResult(int requestCode, String[] permissions,
            //                                          int[] grantResults)
            // to handle the case where the user grants the permission. See the documentation
            // for ActivityCompat#requestPermissions for more details.
            Toast.makeText(this, "Notification permission is required", Toast.LENGTH_SHORT).show();
            return;
        }
        notificationManager.notify(1001, builder.build());
    }

    private void goToProcessedActivity(String presignedUrl) {
        Intent intent = new Intent(ProcessingActivity.this, ProcessedActivity.class);
        intent.putExtra("PRESIGNED_URL", presignedUrl);
//        intent.putExtra("featName", featName);
        startActivity(intent);
    }


    //    <<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<background process for restore image>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>
    private void goToProcessedActivityRestored(String imageUrl, ArrayList<String> faceUrls) {
        Intent intent = new Intent(ProcessingActivity.this, ProcessedActivityRestoredImg.class);
        intent.putExtra("RESTORED_IMAGE_URL", imageUrl);
        intent.putStringArrayListExtra("RESTORED_FACE_URLS", faceUrls);
//        intent.putExtra("featName", featName);
        startActivity(intent);
    }

    private void sendRestoreCompletedNotification() {
        Intent intent = new Intent(this, ProcessedActivityRestoredImg.class);
        intent.putExtra("RESTORED_IMAGE_URL", pendingRestoredImageUrl);
        intent.putStringArrayListExtra("RESTORED_FACE_URLS", pendingRestoredFaceUrls);
//        intent.putExtra("featName", featName);
        intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);

        PendingIntent pendingIntent = PendingIntent.getActivity(
                this, 1, intent,
                PendingIntent.FLAG_UPDATE_CURRENT | PendingIntent.FLAG_IMMUTABLE
        );

        NotificationCompat.Builder builder = new NotificationCompat.Builder(this, "processing_channel")
                .setSmallIcon(R.drawable.app_logo__icon) // Your icon
                .setContentTitle("Restoration Complete")
                .setContentText("Tap to view the restored image")
                .setPriority(NotificationCompat.PRIORITY_DEFAULT)
                .setContentIntent(pendingIntent)
                .setAutoCancel(true);

        NotificationManagerCompat manager = NotificationManagerCompat.from(this);
        if (ActivityCompat.checkSelfPermission(this, Manifest.permission.POST_NOTIFICATIONS) != PackageManager.PERMISSION_GRANTED) {
            // TODO: Consider calling
            //    ActivityCompat#requestPermissions
            // here to request the missing permissions, and then overriding
            //   public void onRequestPermissionsResult(int requestCode, String[] permissions,
            //                                          int[] grantResults)
            // to handle the case where the user grants the permission. See the documentation
            // for ActivityCompat#requestPermissions for more details.
            Toast.makeText(this, "Permission is required", Toast.LENGTH_SHORT).show();
            return;
        }
        manager.notify(1002, builder.build());
    }


    //    <<<<<<<<<<<<<<<<<<<<<<<<<<<<<background processing for sketch generation>>>>>>>>>>>>>>>>>>>>>>
    private void goToProcessedActivityPencilSketchGeneration(String videoUrl, ArrayList<String> resultUrl) {
        Intent intent = new Intent(ProcessingActivity.this, ProcessedActivityPencilSketchGeneration.class);
        intent.putExtra("videoUrl", videoUrl);
        intent.putStringArrayListExtra("resultUrls", resultUrl);
//        intent.putExtra("featName", featName);
        startActivity(intent);
    }

    private void sendPencilSketchGenerationNotification() {
        Intent intent = new Intent(this, ProcessedActivityPencilSketchGeneration.class);
        intent.putExtra("videoUrl", pendingSketchVideoUrls);
        intent.putStringArrayListExtra("resultUrls", pendingSketchResultUrl);
//        intent.putExtra("featName", featName);
        intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);

        PendingIntent pendingIntent = PendingIntent.getActivity(
                this, 1, intent,
                PendingIntent.FLAG_UPDATE_CURRENT | PendingIntent.FLAG_IMMUTABLE
        );

        NotificationCompat.Builder builder = new NotificationCompat.Builder(this, "processing_channel")
                .setSmallIcon(R.drawable.app_logo__icon) // Your icon
                .setContentTitle("Your sketch is ready")
                .setContentText("Tap to view the sketch")
                .setPriority(NotificationCompat.PRIORITY_DEFAULT)
                .setContentIntent(pendingIntent)
                .setAutoCancel(true);

        NotificationManagerCompat manager = NotificationManagerCompat.from(this);
        if (ActivityCompat.checkSelfPermission(this, Manifest.permission.POST_NOTIFICATIONS) != PackageManager.PERMISSION_GRANTED) {
            // TODO: Consider calling
            //    ActivityCompat#requestPermissions
            // here to request the missing permissions, and then overriding
            //   public void onRequestPermissionsResult(int requestCode, String[] permissions,
            //                                          int[] grantResults)
            // to handle the case where the user grants the permission. See the documentation
            // for ActivityCompat#requestPermissions for more details.
            Toast.makeText(this, "Permission is required", Toast.LENGTH_SHORT).show();
            return;
        }
        manager.notify(1002, builder.build());
    }


}





