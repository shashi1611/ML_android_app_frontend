package com.prasthaan.dusterai;

import android.Manifest;
import android.app.Dialog;
import android.app.PendingIntent;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.ImageDecoder;
import android.graphics.Matrix;
import android.media.ExifInterface;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.os.CountDownTimer;
import android.provider.MediaStore;
import android.view.View;
import android.view.ViewGroup;
import android.view.WindowManager;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.Toast;

import androidx.activity.EdgeToEdge;
import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.PickVisualMediaRequest;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.core.app.ActivityCompat;
import androidx.core.app.NotificationCompat;
import androidx.core.app.NotificationManagerCompat;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;

import com.bumptech.glide.Glide;
import com.google.android.material.bottomnavigation.BottomNavigationView;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;

import okhttp3.MediaType;
import okhttp3.MultipartBody;
import okhttp3.RequestBody;
import okhttp3.ResponseBody;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class FaceSwapProcessing extends BaseMenuActivity {

    private static final int TOTAL_TIME = 30;
    ActivityResultLauncher<PickVisualMediaRequest> imagePickerLauncherTargetFace;
    ActivityResultLauncher<PickVisualMediaRequest> imagePickerLauncherSourceFace;
    TextView textView1;
    TextView textView2;
    Button btnFaceSwap;
    boolean flagNewTargetImage = false;
    boolean flagNewSrcImage = false;
    String presignedUrl = "";
    private File imageFileFromTargetFace;
    private File imageFileFromSourceFace;
    private Dialog progressDialog;
    private TextView timerText;
    private CountDownTimer countDownTimer;
    private boolean isAppInForeground = true;
    private String pendingPresignedUrl = null;

    @Override
    protected void onDestroy() {
        super.onDestroy();
        if (countDownTimer != null) {
            countDownTimer.cancel();
        }
    }

    private void requestPermissionIfNeeded() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            if (checkSelfPermission(android.Manifest.permission.READ_MEDIA_IMAGES) != PackageManager.PERMISSION_GRANTED) {
                requestPermissions(new String[]{android.Manifest.permission.READ_MEDIA_IMAGES}, 1001);
            }
        } else {
            if (checkSelfPermission(android.Manifest.permission.READ_EXTERNAL_STORAGE) != PackageManager.PERMISSION_GRANTED) {
                requestPermissions(new String[]{Manifest.permission.READ_EXTERNAL_STORAGE}, 1001);
            }
        }
    }

    @Override
    protected void onResume() {
        super.onResume();
        isAppInForeground = true;

        if (pendingPresignedUrl != null) {
            goToProcessedActivity(pendingPresignedUrl);
            pendingPresignedUrl = null;
        }

    }

    @Override
    protected void onPause() {
        super.onPause();
        isAppInForeground = false;
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        getWindow().setFlags(WindowManager.LayoutParams.FLAG_LAYOUT_NO_LIMITS, WindowManager.LayoutParams.FLAG_LAYOUT_NO_LIMITS);
        setContentView(R.layout.activity_face_swap_processing);
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main), (v, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);
            return insets;
        });
        RelativeLayout relativeLayoutImageTemplate = findViewById(R.id.imageTemplateContainer);
        RelativeLayout relativeLayoutUploadFace = findViewById(R.id.uploadFaceBtn);
        textView1 = findViewById(R.id.uploadTextSingleFace);
        textView2 = findViewById(R.id.uploadImageTextSingleFace);
        TextView textViewTitle = findViewById(R.id.featureNameTextView);


        ImageView imageViewTargetFace = findViewById(R.id.uploadImageTargetFace);
        ImageView imageViewSourceFace = findViewById(R.id.uploadImageSourceFace);
        btnFaceSwap = findViewById(R.id.swapFaceButton);

        BottomNavigationView bottomNavigationView = findViewById(R.id.bottom_navigation);
        setupBottomNavigation(bottomNavigationView, -1);

        relativeLayoutImageTemplate.setOnClickListener(view -> openGalleryTargetFace());
        relativeLayoutUploadFace.setOnClickListener(view -> openGallerySourceFace());
        btnFaceSwap.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (presignedUrl.isEmpty() || flagNewTargetImage || flagNewSrcImage) {
                    if (imageFileFromTargetFace != null && imageFileFromSourceFace != null) {
                        showProcessingDialog("processing");
                        processImage(imageFileFromSourceFace, imageFileFromTargetFace);
                    }
                } else {
                    Intent intent = new Intent(FaceSwapProcessing.this, ProcessedActivity.class);
                    intent.putExtra("PRESIGNED_URL", presignedUrl);
                    startActivity(intent);
                }
            }
        });

        String receivedText = getIntent().getStringExtra("text_key");
        textViewTitle.setText(receivedText);


        imagePickerLauncherTargetFace =
                registerForActivityResult(new ActivityResultContracts.PickVisualMedia(), uri -> {
                    if (uri != null) {
                        flagNewTargetImage = true;

                        RelativeLayout.LayoutParams params = new RelativeLayout.LayoutParams(
                                RelativeLayout.LayoutParams.MATCH_PARENT,
                                RelativeLayout.LayoutParams.MATCH_PARENT
                        );
                        params.addRule(RelativeLayout.CENTER_IN_PARENT, RelativeLayout.TRUE);
                        imageViewTargetFace.setLayoutParams(params);
                        imageViewTargetFace.setImageTintList(null);
                        imageViewTargetFace.setScaleType(ImageView.ScaleType.CENTER_CROP);
                        textView1.setVisibility(View.GONE);
                        Glide.with(this)
                                .load(uri)
                                .override(1024, 1024) // limit size
                                .into(imageViewTargetFace);
                        imageFileFromTargetFace = copyUriToFile(uri);
                        checkEnableButton();

                    } else {
                        Toast.makeText(this, "No image selected", Toast.LENGTH_SHORT).show();
                    }
                });

        imagePickerLauncherSourceFace =
                registerForActivityResult(new ActivityResultContracts.PickVisualMedia(), uri -> {
                    if (uri != null) {
                        flagNewSrcImage = true;

                        imageViewSourceFace.setImageTintList(null);
                        imageViewSourceFace.setScaleType(ImageView.ScaleType.CENTER_CROP);

                        ViewGroup.LayoutParams params = imageViewSourceFace.getLayoutParams();
                        params.width = ViewGroup.LayoutParams.MATCH_PARENT;
                        params.height = ViewGroup.LayoutParams.MATCH_PARENT;
                        imageViewSourceFace.setLayoutParams(params);
                        textView2.setVisibility(View.GONE);
                        Glide.with(this)
                                .load(uri)
                                .override(1024, 1024) // limit size
                                .into(imageViewSourceFace);
                        imageFileFromSourceFace = copyUriToFile(uri);
                        checkEnableButton();

                    } else {
                        Toast.makeText(this, "No image selected", Toast.LENGTH_SHORT).show();
                    }
                });


    }

    private void checkEnableButton() {
        if (imageFileFromTargetFace != null && imageFileFromTargetFace.length() > 0 &&
                imageFileFromSourceFace != null && imageFileFromSourceFace.length() > 0) {
            btnFaceSwap.setEnabled(true);
        } else {
            btnFaceSwap.setEnabled(false);
        }
    }

    private void processImage(File file1, File file2) {
        RequestBody requestFile1 = RequestBody.create(MediaType.parse("image/*"), file1);
        RequestBody requestFile2 = RequestBody.create(MediaType.parse("image/*"), file2);
        MultipartBody.Part image1 = MultipartBody.Part.createFormData("image1", file1.getName(), requestFile1);
        MultipartBody.Part image2 = MultipartBody.Part.createFormData("image2", file2.getName(), requestFile2);
        ApiService apiService = RetrofitClient.getApiServiceFaceSwap();
        apiService.executeProcessingFaceSwap(image1, image2).enqueue(new Callback<ResponseBody>() {
            @Override
            public void onResponse(Call<ResponseBody> call, Response<ResponseBody> response) {
                if (response.isSuccessful()) {
                    dismissDialog();
                    try {
                        presignedUrl = response.body().string();
                        flagNewSrcImage = false;
                        flagNewTargetImage = false;
                        Intent intent = new Intent(FaceSwapProcessing.this, ProcessedActivity.class);
                        intent.putExtra("PRESIGNED_URL", presignedUrl);
                        startActivity(intent);
                        if (isAppInForeground) {
                            goToProcessedActivity(presignedUrl);
                        } else {
                            pendingPresignedUrl = presignedUrl;
                            sendProcessingCompletedNotification();
                        }

                    } catch (IOException e) {
                        dismissDialog();
                        e.printStackTrace();
                    }
                } else {
                    dismissDialog();
                    Toast.makeText(FaceSwapProcessing.this, "Processing Failed", Toast.LENGTH_SHORT).show();
                }
            }

            @Override
            public void onFailure(Call<ResponseBody> call, Throwable t) {
                dismissDialog();
                Toast.makeText(FaceSwapProcessing.this, "Error: " + t.getMessage(), Toast.LENGTH_SHORT).show();
            }
        });
    }

    private void openGalleryTargetFace() {
        requestPermissionIfNeeded();  // Always check before picker
        imagePickerLauncherTargetFace.launch(new PickVisualMediaRequest.Builder()
                .setMediaType(ActivityResultContracts.PickVisualMedia.ImageOnly.INSTANCE)
                .build());

    }

    private void openGallerySourceFace() {
        requestPermissionIfNeeded();  // Always check before picker
        imagePickerLauncherSourceFace.launch(new PickVisualMediaRequest.Builder()
                .setMediaType(ActivityResultContracts.PickVisualMedia.ImageOnly.INSTANCE)
                .build());

    }

    private File copyUriToFile(Uri uri) {
//        File finalFile = new File(getCacheDir(), "original_image.jpg");
        String uniqueName = "image_" + System.currentTimeMillis() + ".jpg";
        File finalFile = new File(getCacheDir(), uniqueName);

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
//            File compressedFile = new File(getCacheDir(), "compressed_image.jpg");
            String uniqueName = "compressed_" + System.currentTimeMillis() + ".jpg";
            File compressedFile = new File(getCacheDir(), uniqueName);

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
        startCountdown(process);
    }

    private void dismissDialog() {
        if (progressDialog != null && progressDialog.isShowing()) {
            countDownTimer.cancel();
            progressDialog.dismiss();
        }
    }

    private void startCountdown(String process) {
        int total_time;
        total_time = TOTAL_TIME;
        countDownTimer = new CountDownTimer(total_time * 1000, 1000) {
            @Override
            public void onTick(long millisUntilFinished) {
                int secondsLeft = (int) (millisUntilFinished / 1000);
                timerText.setText("Generating: " + secondsLeft + "s remaining");
            }

            @Override
            public void onFinish() {
                timerText.setText("Still processing please wait!");
            }
        }.start();
    }

    private void goToProcessedActivity(String presignedUrl) {
        Intent intent = new Intent(FaceSwapProcessing.this, ProcessedActivity.class);
        intent.putExtra("PRESIGNED_URL", presignedUrl);
        startActivity(intent);
    }

    private void sendProcessingCompletedNotification() {
        Intent intent = new Intent(this, ProcessedActivity.class);
        intent.putExtra("PRESIGNED_URL", pendingPresignedUrl);
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

}