package com.prasthaan.dusterai;

import android.Manifest;
import android.app.Dialog;
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
import android.util.Log;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.Toast;

import androidx.activity.EdgeToEdge;
import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.PickVisualMediaRequest;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;

import com.bumptech.glide.Glide;

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

public class FaceSwapProcessing extends AppCompatActivity {

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
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
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


        ImageView imageViewTargetFace = findViewById(R.id.uploadImageTargetFace);
        ImageView imageViewSourceFace = findViewById(R.id.uploadImageSourceFace);
        btnFaceSwap = findViewById(R.id.swapFaceButton);

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


        imagePickerLauncherTargetFace =
                registerForActivityResult(new ActivityResultContracts.PickVisualMedia(), uri -> {
                    if (uri != null) {
                        flagNewTargetImage = true;
//                        relativeLayoutImageTemplate.setBackground(null);

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
//                        flagNewImage = true;

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
//                        flagNewImage = true;

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
        Log.d("uploading image", "processImage: file1" + file1);
        Log.d("uploading image", "processImage:  file2" + file2);
        RequestBody requestFile1 = RequestBody.create(MediaType.parse("image/*"), file1);
        RequestBody requestFile2 = RequestBody.create(MediaType.parse("image/*"), file2);
        Log.d("uploading image", "processImage: requestfile1 " + requestFile1);
        Log.d("uploading image", "processImage:  requestfile2 " + requestFile2);
        MultipartBody.Part image1 = MultipartBody.Part.createFormData("image1", file1.getName(), requestFile1);
        MultipartBody.Part image2 = MultipartBody.Part.createFormData("image2", file2.getName(), requestFile2);
        Log.d("uploading image", "processImage: uploading image1" + image1);
        Log.d("uploading image", "processImage: uploading image2" + image2);

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
                        Log.d("Face swap response", "onResponse: the response from face swap is" + presignedUrl);
//                        flagNewImage = false;
                        Intent intent = new Intent(FaceSwapProcessing.this, ProcessedActivity.class);
                        intent.putExtra("PRESIGNED_URL", presignedUrl);
                        startActivity(intent);
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
//                dismissDialog();
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
//        if (Objects.equals(process, "Restore image")) {
//            total_time = TOTAL_TIME_RESTORE_IMAGE;
//        } else if (Objects.equals(process, "Enhance resolution 2X")) {
//            total_time = TOTAL_TIME_ENHANCE_IMAGE_2X;
//        } else if (Objects.equals(process, "Enhance resolution 4X")) {
//            total_time = TOTAL_TIME_ENHANCE_IMAGE_4X;
//        } else {
        total_time = TOTAL_TIME;
//        }
        countDownTimer = new CountDownTimer(total_time * 1000, 1000) {
            @Override
            public void onTick(long millisUntilFinished) {
                int secondsLeft = (int) (millisUntilFinished / 1000);
                timerText.setText("Generating: " + secondsLeft + "s remaining");
            }

            @Override
            public void onFinish() {
                timerText.setText("Still processing please keep patience!");
//                dismissDialog();
            }
        }.start();
    }

}