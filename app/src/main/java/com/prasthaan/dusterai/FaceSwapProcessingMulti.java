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
import androidx.core.app.ActivityCompat;
import androidx.core.app.NotificationCompat;
import androidx.core.app.NotificationManagerCompat;
import androidx.core.content.FileProvider;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.Glide;
import com.prasthaan.dusterai.Adapters.AdapterFaceSwapMulti;
import com.prasthaan.dusterai.Models.ModalFaceSwapMulti;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.stream.Collectors;
import java.util.zip.ZipEntry;
import java.util.zip.ZipInputStream;

import okhttp3.MediaType;
import okhttp3.MultipartBody;
import okhttp3.RequestBody;
import okhttp3.ResponseBody;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class FaceSwapProcessingMulti extends AppCompatActivity {

    private static final int TOTAL_TIME = 45;
    RecyclerView recyclerView;
    ArrayList<ModalFaceSwapMulti> listFaceSwapMulti = new ArrayList<>();
    AdapterFaceSwapMulti adapterFaceSwapMulti;
    Map<Integer, File> dst_faces = new HashMap<>();
    List<File> fsrc_faces = new ArrayList<>();
    List<File> src_faces = new ArrayList<>();
    List<Integer> dst_position = new ArrayList<>();
    Button btnFaceSwapMulti;
    TextView textView1;

    boolean flagNewTargetImage = false;
    boolean flagNewSrcImage = false;
    String presignedUrl = "";
    private CountDownTimer countDownTimer;
    private ActivityResultLauncher<PickVisualMediaRequest> imagePickerLauncherSourceImages;
    private ActivityResultLauncher<PickVisualMediaRequest> imagePickerLauncherTargetFaceMulti;
    private int currentPickPosition = -1;
    private File imageFileTargetImage;
    private Dialog progressDialog;
    private TextView timerText;

    private boolean isAppInForeground = true;
    private String pendingPresignedUrl = null;

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
        setContentView(R.layout.activity_face_swap_processing_multi);
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main), (v, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);
            return insets;
        });

        btnFaceSwapMulti = findViewById(R.id.swapFaceButton);
        RelativeLayout relativeLayoutImageTemplate = findViewById(R.id.imageTemplateContainerMultiFace);
        ImageView imageViewUploadTargetImg = findViewById(R.id.uploadimageFaceSwapMulti);
        textView1 = findViewById(R.id.uploadTextMulti);

        relativeLayoutImageTemplate.setOnClickListener(view -> openGalleryTargetFaceMulti());

        String receivedText = getIntent().getStringExtra("text_key");


        adapterFaceSwapMulti = new AdapterFaceSwapMulti(
                listFaceSwapMulti,
                this, // <-- this is the context
                new AdapterFaceSwapMulti.OnImagePickRequested() {
                    @Override
                    public void onImagePick(int adapterPosition) {
                        currentPickPosition = adapterPosition;
//                        Log.d("current position = ", "onImagePick: the current position =  " + currentPickPosition);
//                        dst_position.add(currentPickPosition);
                        if (!dst_position.contains(currentPickPosition)) {
                            dst_position.add(currentPickPosition);
                        }
//                        Log.d("current position = ", "onImagePick: the current length of dst_position =  " + dst_position.size());

                        imagePickerLauncherSourceImages.launch(new PickVisualMediaRequest.Builder()
                                .setMediaType(ActivityResultContracts.PickVisualMedia.ImageOnly.INSTANCE)
                                .build());
                    }
                }
        );


// <<<<<<<<<<<<<<<<<<<<<<<image picker for recycler view source images>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>
        imagePickerLauncherSourceImages =
                registerForActivityResult(new ActivityResultContracts.PickVisualMedia(), uri -> {
                    if (uri != null && currentPickPosition != -1) {
                        flagNewTargetImage = true;
                        listFaceSwapMulti.get(currentPickPosition).setSelectedImageUri(uri);
                        adapterFaceSwapMulti.notifyItemChanged(currentPickPosition);
                        File dst_imageFile = copyUriToFile(uri);
                        dst_faces.put(currentPickPosition, dst_imageFile);
                        currentPickPosition = -1;
                        checkEnableButton();

                    } else {
                        Toast.makeText(this, "No image selected", Toast.LENGTH_SHORT).show();
                    }
                });


        recyclerView = findViewById(R.id.recyclerView_feat_list_face_swap_multi);


        recyclerView.setAdapter(adapterFaceSwapMulti);
        LinearLayoutManager layoutManagerFaceSwapMulti = new LinearLayoutManager(this, LinearLayoutManager.HORIZONTAL, false);
        recyclerView.setLayoutManager(layoutManagerFaceSwapMulti);
        recyclerView.setNestedScrollingEnabled(false);
        recyclerView.setOverScrollMode(View.OVER_SCROLL_NEVER);

//   <<<<<<<<<<<<<<<<<<<<<<<<<main image picker for source image bada wala >>>>>>>>>>>>>>>>>>>>>>>>>>
        imagePickerLauncherTargetFaceMulti =
                registerForActivityResult(new ActivityResultContracts.PickVisualMedia(), uri -> {
                    if (uri != null) {
                        flagNewSrcImage = true;
                        listFaceSwapMulti.clear();
                        src_faces.clear();
                        dst_faces.clear();
                        dst_position.clear();
                        fsrc_faces.clear();
                        showProcessingDialog("detect");
                        RelativeLayout.LayoutParams params = new RelativeLayout.LayoutParams(
                                RelativeLayout.LayoutParams.MATCH_PARENT,
                                RelativeLayout.LayoutParams.MATCH_PARENT
                        );
                        params.addRule(RelativeLayout.CENTER_IN_PARENT, RelativeLayout.TRUE);
                        imageViewUploadTargetImg.setLayoutParams(params);
                        imageViewUploadTargetImg.setImageTintList(null);
                        imageViewUploadTargetImg.setScaleType(ImageView.ScaleType.CENTER_CROP);
                        textView1.setVisibility(View.GONE);

                        Glide.with(this)
                                .load(uri)
                                .override(1024, 1024) // limit size
                                .into(imageViewUploadTargetImg);

                        imageFileTargetImage = copyUriToFile(uri);
//                        flagNewImage = true;
                        detectFace(imageFileTargetImage);
                        checkEnableButton();


                    } else {
                        Toast.makeText(this, "No image selected", Toast.LENGTH_SHORT).show();
                    }
                });
        btnFaceSwapMulti.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (presignedUrl.isEmpty() || flagNewTargetImage || flagNewSrcImage) {
                    showProcessingDialog("processing");
                    List<File> dstFilesForApi = dst_faces.entrySet()
                            .stream()
                            .sorted(Map.Entry.comparingByKey()) // Sort by position (key)
                            .map(Map.Entry::getValue)           // Extract the file (value)
                            .collect(Collectors.toList());
                    Collections.sort(dst_position);
                    fsrc_faces.clear();
                    for (int i = 0; i < dst_position.size(); i++) {
                        fsrc_faces.add(src_faces.get(dst_position.get(i)));
                    }
                    if (fsrc_faces.size() == dstFilesForApi.size()) {
                        processImage(imageFileTargetImage, fsrc_faces, dstFilesForApi);
                    }
                } else {
                    Intent intent = new Intent(FaceSwapProcessingMulti.this, ProcessedActivity.class);
                    intent.putExtra("PRESIGNED_URL", presignedUrl);
                    startActivity(intent);
                }

            }

        });


    }

    private void checkEnableButton() {
        if (imageFileTargetImage != null && imageFileTargetImage.length() > 0 &&
                dst_faces != null && dst_faces.size() > 0) {
            btnFaceSwapMulti.setEnabled(true);
        } else {
            btnFaceSwapMulti.setEnabled(false);
        }
    }

    private void processImage(File file1, List<File> src, List<File> dst) {

        RequestBody requestFile1 = RequestBody.create(MediaType.parse("image/*"), file1);
        MultipartBody.Part target_image = MultipartBody.Part.createFormData("target_image", file1.getName(), requestFile1);

        List<MultipartBody.Part> src_image_list = new ArrayList<>();
        for (int i = 0; i < src.size(); i++) {
            File srcFile = src.get(i);
            RequestBody srcBody = RequestBody.create(MediaType.parse("image/*"), srcFile);
            MultipartBody.Part srcPart = MultipartBody.Part.createFormData("src_image_list", srcFile.getName(), srcBody);
            src_image_list.add(srcPart);
        }

        // Prepare dst_image_list
        List<MultipartBody.Part> dst_image_list = new ArrayList<>();
        for (int i = 0; i < dst.size(); i++) {
            File dstFile = dst.get(i);
            RequestBody dstBody = RequestBody.create(MediaType.parse("image/*"), dstFile);
            MultipartBody.Part dstPart = MultipartBody.Part.createFormData("dst_image_list", dstFile.getName(), dstBody);
            dst_image_list.add(dstPart);
        }


        ApiService apiService = RetrofitClient.getApiServiceFaceSwap();
        apiService.executeProcessingMultiFaceSwap(target_image, src_image_list, dst_image_list).enqueue(new Callback<ResponseBody>() {
            @Override
            public void onResponse(Call<ResponseBody> call, Response<ResponseBody> response) {
                if (response.isSuccessful()) {
                    dismissDialog("multi");
                    try {
                        presignedUrl = response.body().string();
                        flagNewSrcImage = false;
                        flagNewTargetImage = false;
                        Intent intent = new Intent(FaceSwapProcessingMulti.this, ProcessedActivity.class);
                        intent.putExtra("PRESIGNED_URL", presignedUrl);
                        startActivity(intent);
                        if (isAppInForeground) {
                            goToProcessedActivity(presignedUrl);
                        } else {
                            pendingPresignedUrl = presignedUrl;
                            sendProcessingCompletedNotification();
                        }
                    } catch (IOException e) {
                        dismissDialog("multi");
                        e.printStackTrace();
                    }
                } else {
                    dismissDialog("multi");
                    Toast.makeText(FaceSwapProcessingMulti.this, "Processing Failed", Toast.LENGTH_SHORT).show();
                }
            }

            @Override
            public void onFailure(Call<ResponseBody> call, Throwable t) {
                dismissDialog("multi");
                Toast.makeText(FaceSwapProcessingMulti.this, "Error: " + t.getMessage(), Toast.LENGTH_SHORT).show();
            }
        });
    }


    private void detectFace(File imageFileTargetImage) {
        RequestBody requestFile = RequestBody.create(MediaType.parse("image/*"), imageFileTargetImage);
        MultipartBody.Part file = MultipartBody.Part.createFormData("file", imageFileTargetImage.getName(), requestFile);
        ApiService apiService = RetrofitClient.getApiServiceFaceDetection();
        apiService.executeProcessingDetectFace(file).enqueue(new Callback<ResponseBody>() {
            @Override
            public void onResponse(Call<ResponseBody> call, Response<ResponseBody> response) {
                if (response.isSuccessful()) {
                    dismissDialog("detect");
                    try {
                        File zipFile = new File(FaceSwapProcessingMulti.this.getCacheDir(), "faces.zip");
                        FileOutputStream fos = new FileOutputStream(zipFile);
                        fos.write(response.body().bytes());
                        fos.close();

                        // ✅ Extract the zip
                        List<Bitmap> faceBitmaps = unzipAndLoadBitmaps(zipFile);
                        for (Bitmap bitmap : faceBitmaps) {

                            // 1. Save bitmap to cache
                            File faceFile = new File(getCacheDir(), "face_" + System.currentTimeMillis() + ".jpg");
                            FileOutputStream out = new FileOutputStream(faceFile);
                            bitmap.compress(Bitmap.CompressFormat.JPEG, 100, out);
                            out.flush();
                            out.close();
                            src_faces.add(faceFile);

                            Uri imageUri = FileProvider.getUriForFile(FaceSwapProcessingMulti.this, "com.prasthaan.dusterai.fileprovider", faceFile);


                            listFaceSwapMulti.add(new ModalFaceSwapMulti(imageUri, R.drawable.baseline_add_24));
                        }
                        adapterFaceSwapMulti.notifyDataSetChanged();
                    } catch (IOException e) {
                        dismissDialog("detect");
                        e.printStackTrace();
                    }
                } else {
                    dismissDialog("detect");
                    Toast.makeText(FaceSwapProcessingMulti.this, "Processing Failed", Toast.LENGTH_SHORT).show();
                }
            }

            @Override
            public void onFailure(Call<ResponseBody> call, Throwable t) {
                dismissDialog("detect");
                Toast.makeText(FaceSwapProcessingMulti.this, "Error: " + t.getMessage(), Toast.LENGTH_SHORT).show();
            }
        });
    }

    private List<Bitmap> unzipAndLoadBitmaps(File zipFile) throws IOException {
        List<Bitmap> faceBitmaps = new ArrayList<>();
        ZipInputStream zis = new ZipInputStream(new FileInputStream(zipFile));
        ZipEntry zipEntry;

        while ((zipEntry = zis.getNextEntry()) != null) {
            if (!zipEntry.isDirectory() && zipEntry.getName().endsWith(".jpg")) {
                ByteArrayOutputStream baos = new ByteArrayOutputStream();
                byte[] buffer = new byte[1024];
                int count;
                while ((count = zis.read(buffer)) != -1) {
                    baos.write(buffer, 0, count);
                }
                byte[] imageBytes = baos.toByteArray();
                Bitmap bitmap = BitmapFactory.decodeByteArray(imageBytes, 0, imageBytes.length);
                faceBitmaps.add(bitmap);
            }
            zis.closeEntry();
        }
        zis.close();
        return faceBitmaps;
    }


    private void openGalleryTargetFaceMulti() {
        requestPermissionIfNeeded();  // Always check before picker
        imagePickerLauncherTargetFaceMulti.launch(new PickVisualMediaRequest.Builder()
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
        if (Objects.equals(process, "detect")) {
            timerText = progressDialog.findViewById(R.id.timer_text);
            timerText.setText("Detecting Faces...");
        } else {
            // Initialize countdown
            timerText = progressDialog.findViewById(R.id.timer_text);
            startCountdown(process);
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
                timerText.setText("Still processing please wait ...");
//                dismissDialog();
            }
        }.start();
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        if (countDownTimer != null) {
            countDownTimer.cancel();
        }
    }

    private void dismissDialog(String process) {
        if (Objects.equals(process, "multi")) {
            if (progressDialog != null && progressDialog.isShowing()) {
                countDownTimer.cancel();
                progressDialog.dismiss();
            }
        } else {
            if (progressDialog != null && progressDialog.isShowing()) {
                progressDialog.dismiss();
            }
        }
    }

    private void goToProcessedActivity(String presignedUrl) {
        Intent intent = new Intent(FaceSwapProcessingMulti.this, ProcessedActivity.class);
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