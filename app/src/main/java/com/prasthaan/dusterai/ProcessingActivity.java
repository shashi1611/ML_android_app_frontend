package com.prasthaan.dusterai;

import android.Manifest;
import android.app.Dialog;
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
import android.widget.Button;
import android.widget.FrameLayout;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.activity.EdgeToEdge;
import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.PickVisualMediaRequest;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.content.ContextCompat;
import androidx.core.content.FileProvider;

import com.bumptech.glide.Glide;
import com.google.android.gms.ads.AdRequest;
import com.google.android.gms.ads.AdSize;
import com.google.android.gms.ads.AdView;
import com.google.android.gms.ads.MobileAds;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.text.SimpleDateFormat;
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


public class ProcessingActivity extends AppCompatActivity {
    private static final int REQUEST_IMAGE_CAPTURE = 1;
    private static final int REQUEST_PERMISSIONS = 101;
    private static final int TOTAL_TIME = 90; // 120 seconds
    private static final String AD_UNIT_ID = "ca-app-pub-3940256099942544/6300978111";
    String receivedText;
    ActivityResultLauncher<PickVisualMediaRequest> imagePickerLauncher;
    private ImageView imageView;
    private Uri imageUri;
    private File imageFileFromCamera = null;
    private File imageFileFromFileUploader;
    private Button btnUpload, btnProcess, open_cam_btn;
    private Dialog progressDialog;
    private TextView timerText;
    private CountDownTimer countDownTimer;
    private AdView adView;
    private FrameLayout adContainerView;

    private void showProcessingDialog() {
        progressDialog = new Dialog(this);
        progressDialog.show();
        progressDialog.setContentView(R.layout.progress_dialog);
        progressDialog.setCancelable(false);
        progressDialog.getWindow().setBackgroundDrawableResource(
                android.R.color.transparent
        );

        // Initialize countdown
        timerText = progressDialog.findViewById(R.id.timer_text);
        startCountdown();
    }

    private void startCountdown() {
        countDownTimer = new CountDownTimer(TOTAL_TIME * 1000, 1000) {
            @Override
            public void onTick(long millisUntilFinished) {
                int secondsLeft = (int) (millisUntilFinished / 1000);
                timerText.setText("Generating: " + secondsLeft + "s remaining");
            }

            @Override
            public void onFinish() {
                timerText.setText("Processing Complete!");
                dismissDialog();
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
        if (!isConnected()) {
            startActivity(new Intent(this, NoInternetActivity.class));
            finish();
            return;
        }
        setContentView(R.layout.activity_processing);
        imageView = findViewById(R.id.uploaded_img);
        btnUpload = findViewById(R.id.upload_img_vid_button);
        btnProcess = findViewById(R.id.process_img_button);
        open_cam_btn = findViewById(R.id.open_camera_button);
        receivedText = getIntent().getStringExtra("text_key");
        TextView textViewFeatname = findViewById(R.id.featureNameTextView);

        if (receivedText.equals("To winter") || receivedText.equals("To Summer")) {
            textViewFeatname.setText("Image " + receivedText);
        } else {
            textViewFeatname.setText("Image to " + receivedText);
        }

        if (!isCameraPermissionGranted()) {
            requestPermissions();
        }

        open_cam_btn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {


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

            if (imageFileFromFileUploader != null) {
                showProcessingDialog();
                processImage(imageFileFromFileUploader);
            } else if (imageFileFromCamera != null) {
                showProcessingDialog();
                processImage(imageFileFromCamera);

            } else {
                Toast.makeText(this, "Select an image first", Toast.LENGTH_SHORT).show();
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
                        Glide.with(this)
                                .load(uri)
                                .override(1024, 1024) // limit size
                                .into(imageView);
                        imageFileFromFileUploader = copyUriToFile(uri);

                    } else {
                        Toast.makeText(this, "No image selected", Toast.LENGTH_SHORT).show();
                    }
                });
    }


    //    <<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<camera image capture on activity result>>>>>>>>>>>>>>>>>>>>>>>>>>
    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == REQUEST_IMAGE_CAPTURE) {
            if (imageFileFromCamera != null) {
                imageUri = Uri.fromFile(imageFileFromCamera);
                Glide.with(this)
                        .load(imageUri)
                        .override(1024, 1024) // limit size
                        .into(imageView);
                imageFileFromCamera = copyUriToFile(imageUri);


            }
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
        adView.setAdUnitId(AD_UNIT_ID);

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
        if (Objects.equals(receivedText, "Ukiyo-e style")) {
            apiService.executeProcessingUkiyoe(body).enqueue(new Callback<ResponseBody>() {
                @Override
                public void onResponse(Call<ResponseBody> call, Response<ResponseBody> response) {
                    if (response.isSuccessful()) {
                        dismissDialog();
                        try {


                            String presignedUrl = response.body().string();
                            Intent intent = new Intent(ProcessingActivity.this, ProcessedActivity.class);
                            intent.putExtra("PRESIGNED_URL", presignedUrl);
                            startActivity(intent);
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
                    ;
                }
            });
        } else if (Objects.equals(receivedText, "Monet style")) {
            apiService.executeProcessingMonet(body).enqueue(new Callback<ResponseBody>() {
                @Override
                public void onResponse(Call<ResponseBody> call, Response<ResponseBody> response) {
                    if (response.isSuccessful()) {
                        dismissDialog();
                        try {
                            String presignedUrl = response.body().string();
                            Intent intent = new Intent(ProcessingActivity.this, ProcessedActivity.class);
                            intent.putExtra("PRESIGNED_URL", presignedUrl);
                            startActivity(intent);
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
                            String presignedUrl = response.body().string();
                            Intent intent = new Intent(ProcessingActivity.this, ProcessedActivity.class);
                            intent.putExtra("PRESIGNED_URL", presignedUrl);
                            startActivity(intent);
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
                            String presignedUrl = response.body().string();
                            Intent intent = new Intent(ProcessingActivity.this, ProcessedActivity.class);
                            intent.putExtra("PRESIGNED_URL", presignedUrl);
                            startActivity(intent);
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
                            String presignedUrl = response.body().string();
                            Intent intent = new Intent(ProcessingActivity.this, ProcessedActivity.class);
                            intent.putExtra("PRESIGNED_URL", presignedUrl);
                            startActivity(intent);
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
                            String presignedUrl = response.body().string();
                            Intent intent = new Intent(ProcessingActivity.this, ProcessedActivity.class);
                            intent.putExtra("PRESIGNED_URL", presignedUrl);
                            startActivity(intent);
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

}

