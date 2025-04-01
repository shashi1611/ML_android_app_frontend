package com.example.myapplication;

import android.Manifest;
import android.app.Dialog;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.os.CountDownTimer;
import android.os.Environment;
import android.provider.MediaStore;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.activity.EdgeToEdge;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.content.FileProvider;

import com.bumptech.glide.Glide;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
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

    private static final int PICK_IMAGE_REQUEST = 1;
    private static final int REQUEST_IMAGE_CAPTURE = 1;
    private static final int REQUEST_PERMISSIONS = 101;
    private static final int TOTAL_TIME = 90; // 120 seconds
    String receivedText;
    private ImageView imageView;
    private Uri imageUri;
    private File imageFile;
    private Button btnUpload, btnProcess, open_cam_btn;
    private Dialog progressDialog;
    private TextView timerText;
    private CountDownTimer countDownTimer;

    private void showProcessingDialog() {
        progressDialog = new Dialog(this);
        progressDialog.show();
//        progressDialog.requestWindowFeature(Window.FEATURE_NO_TITLE);
        progressDialog.setContentView(R.layout.progress_dialog);
        progressDialog.setCancelable(false);
//        progressDialog.getWindow().setLayout(WindowManager.LayoutParams.MATCH_PARENT, WindowManager.LayoutParams.MATCH_PARENT);
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
                timerText.setText("Processing: " + secondsLeft + "s remaining");
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

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_processing);
        requestPermissions();

        imageView = findViewById(R.id.uploaded_img);
        btnUpload = findViewById(R.id.upload_img_vid_button);
        btnProcess = findViewById(R.id.process_img_button);
        open_cam_btn = findViewById(R.id.open_camera_button);
        receivedText = getIntent().getStringExtra("text_key");
//        Toast.makeText(this, "text recieved is = " + receivedText, Toast.LENGTH_SHORT).show();

        open_cam_btn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {


                Intent intent = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);

//                 Create a file for the image
                imageFile = null;
                try {
                    imageFile = createImageFile();
                } catch (IOException e) {
                    throw new RuntimeException(e);
                }
                Log.d("hariom mre bhai", "onClick: photofile = " + imageFile);
                if (imageFile != null) {

                    imageUri = FileProvider.getUriForFile(ProcessingActivity.this, "com.example.myapplication.fileprovider", imageFile);
                    intent.putExtra(MediaStore.EXTRA_OUTPUT, imageUri);
                    intent.addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION);
//                    captureImageLauncher.launch(intent);
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

            if (imageUri != null) {
                showProcessingDialog();
                processImage(imageUri);
            } else {
                Toast.makeText(this, "Select an image first", Toast.LENGTH_SHORT).show();
            }

        });


    }


    //    <<<<<<<<<<<<<<<<<<<<<<<<<<<<<get image from gallery and pass to the api >>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>
    private void openGallery() {
        Intent intent = new Intent(Intent.ACTION_PICK, MediaStore.Images.Media.EXTERNAL_CONTENT_URI);
        startActivityForResult(intent, PICK_IMAGE_REQUEST);
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == PICK_IMAGE_REQUEST && resultCode == RESULT_OK && data != null) {
            imageUri = data.getData();
            imageView.setImageURI(imageUri);
        } else if (requestCode == REQUEST_IMAGE_CAPTURE) {
            // 📌 Image captured from camera
            if (imageFile != null) {
                imageUri = Uri.fromFile(imageFile);
                imageView.setImageURI(imageUri);
//                uploadImage(imageFile);
            }
        }
    }


    private File getFileFromUri(Uri uri) {
        try {
            InputStream inputStream = getContentResolver().openInputStream(uri);
            if (inputStream == null) return null;

            // Create a temporary file in app's cache directory
            File tempFile = new File(getCacheDir(), "temp_image.jpg");
            FileOutputStream outputStream = new FileOutputStream(tempFile);

            byte[] buffer = new byte[4096];
            int bytesRead;
            while ((bytesRead = inputStream.read(buffer)) != -1) {
                outputStream.write(buffer, 0, bytesRead);
            }

            outputStream.close();
            inputStream.close();
            return tempFile;
        } catch (IOException e) {
            e.printStackTrace();
            return null;
        }
    }

    private void processImage(Uri fileUri) {
//        File file = new File(FileUtils.getPath(this, fileUri));
        File file = getFileFromUri(fileUri);
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
//                        displayImage(presignedUrl);
//                        Toast.makeText(ProcessingActivity.this, "Processing succesful " + presignedUrl, Toast.LENGTH_SHORT).show();
//                        Log.d("hariom mre bhai ", "onResponse: " + presignedUrl);
                        } catch (IOException e) {
                            e.printStackTrace();
                        }
                    } else {
                        Toast.makeText(ProcessingActivity.this, "Processing Failed", Toast.LENGTH_SHORT).show();
                    }
                }

                @Override
                public void onFailure(Call<ResponseBody> call, Throwable t) {
                    dismissDialog();
                    Toast.makeText(ProcessingActivity.this, "Error: " + t.getMessage(), Toast.LENGTH_SHORT).show();
                    Log.d("hariom mre bhai ", "onError: " + t.getMessage());
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
//                        displayImage(presignedUrl);
//                        Toast.makeText(ProcessingActivity.this, "Processing succesful " + presignedUrl, Toast.LENGTH_SHORT).show();
//                        Log.d("hariom mre bhai ", "onResponse: " + presignedUrl);
                        } catch (IOException e) {
                            e.printStackTrace();
                        }
                    } else {
                        Toast.makeText(ProcessingActivity.this, "Processing Failed", Toast.LENGTH_SHORT).show();
                    }
                }

                @Override
                public void onFailure(Call<ResponseBody> call, Throwable t) {
                    dismissDialog();
                    Toast.makeText(ProcessingActivity.this, "Error: " + t.getMessage(), Toast.LENGTH_SHORT).show();
                    Log.d("hariom mre bhai ", "onError: " + t.getMessage());
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
//                        displayImage(presignedUrl);
//                        Toast.makeText(ProcessingActivity.this, "Processing succesful " + presignedUrl, Toast.LENGTH_SHORT).show();
//                        Log.d("hariom mre bhai ", "onResponse: " + presignedUrl);
                        } catch (IOException e) {
                            e.printStackTrace();
                        }
                    } else {
                        Toast.makeText(ProcessingActivity.this, "Processing Failed", Toast.LENGTH_SHORT).show();
                    }
                }

                @Override
                public void onFailure(Call<ResponseBody> call, Throwable t) {
                    dismissDialog();
                    Toast.makeText(ProcessingActivity.this, "Error: " + t.getMessage(), Toast.LENGTH_SHORT).show();
                    Log.d("hariom mre bhai ", "onError: " + t.getMessage());
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
//                        displayImage(presignedUrl);
//                        Toast.makeText(ProcessingActivity.this, "Processing succesful " + presignedUrl, Toast.LENGTH_SHORT).show();
//                        Log.d("hariom mre bhai ", "onResponse: " + presignedUrl);
                        } catch (IOException e) {
                            e.printStackTrace();
                        }
                    } else {
                        Toast.makeText(ProcessingActivity.this, "Processing Failed", Toast.LENGTH_SHORT).show();
                    }
                }

                @Override
                public void onFailure(Call<ResponseBody> call, Throwable t) {
                    dismissDialog();
                    Toast.makeText(ProcessingActivity.this, "Error: " + t.getMessage(), Toast.LENGTH_SHORT).show();
                    Log.d("hariom mre bhai ", "onError: " + t.getMessage());
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
//                        displayImage(presignedUrl);
//                        Toast.makeText(ProcessingActivity.this, "Processing succesful " + presignedUrl, Toast.LENGTH_SHORT).show();
//                        Log.d("hariom mre bhai ", "onResponse: " + presignedUrl);
                        } catch (IOException e) {
                            e.printStackTrace();
                        }
                    } else {
                        Toast.makeText(ProcessingActivity.this, "Processing Failed", Toast.LENGTH_SHORT).show();
                    }
                }

                @Override
                public void onFailure(Call<ResponseBody> call, Throwable t) {
                    dismissDialog();
                    Toast.makeText(ProcessingActivity.this, "Error: " + t.getMessage(), Toast.LENGTH_SHORT).show();
                    Log.d("hariom mre bhai ", "onError: " + t.getMessage());
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
//                        displayImage(presignedUrl);
//                        Toast.makeText(ProcessingActivity.this, "Processing succesful " + presignedUrl, Toast.LENGTH_SHORT).show();
//                        Log.d("hariom mre bhai ", "onResponse: " + presignedUrl);
                        } catch (IOException e) {
                            e.printStackTrace();
                        }
                    } else {
                        Toast.makeText(ProcessingActivity.this, "Processing Failed", Toast.LENGTH_SHORT).show();
                    }
                }

                @Override
                public void onFailure(Call<ResponseBody> call, Throwable t) {
                    dismissDialog();
                    Toast.makeText(ProcessingActivity.this, "Error: " + t.getMessage(), Toast.LENGTH_SHORT).show();
                    Log.d("hariom mre bhai ", "onError: " + t.getMessage());
                }
            });

        } else {
            Toast.makeText(this, "something went wrong go back", Toast.LENGTH_SHORT).show();
        }

    }


    private void displayImage(String imageUrl) {
        Glide.with(this).load(imageUrl).into(imageView);
    }
}

