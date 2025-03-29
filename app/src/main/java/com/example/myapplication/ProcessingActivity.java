package com.example.myapplication;

import android.Manifest;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.os.Environment;
import android.provider.MediaStore;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.Toast;

import androidx.activity.EdgeToEdge;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.content.FileProvider;

import com.bumptech.glide.Glide;

import java.io.File;
import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;

import okhttp3.MediaType;
import okhttp3.MultipartBody;
import okhttp3.RequestBody;
import okhttp3.ResponseBody;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

//public class ProcessingActivity extends AppCompatActivity {
//
//    private static final int REQUEST_PERMISSION_CODE = 100;
//
//    private ActivityResultLauncher<Intent> imagePickerLauncher;
//    private ActivityResultLauncher<Intent> captureImageLauncher;
//    private Uri photoUri;
//
//    @Override
//        protected void onCreate(Bundle savedInstanceState) {
//            super.onCreate(savedInstanceState);
//            EdgeToEdge.enable(this);
//            setContentView(R.layout.activity_processing);
//
//
//        imagePickerLauncher = registerForActivityResult(new ActivityResultContracts.StartActivityForResult(), result -> {
//            if (result.getResultCode() == RESULT_OK && result.getData() != null) {
//                Uri imageUri = result.getData().getData();
//                Log.d("hello world", "hello this is image uri: " + imageUri.toString());
////                Log.d("hello world", "hello the result is: " + result);
//                ImageView imageView = findViewById(R.id.uploaded_img); // Replace with your ImageView ID
//                imageView.setImageURI(imageUri);
//                String imagePath = getImagePathFromUri(imageUri);
//                Log.d("hello world", "hello the image path is =: " + imagePath);
//                uploadImageToApi(imagePath);
//            }
//        });
//
//
//        captureImageLauncher = registerForActivityResult(
//                new ActivityResultContracts.StartActivityForResult(),
//                result -> {
//                    if (result.getResultCode() == RESULT_OK) {
//                        ImageView imageView = findViewById(R.id.uploaded_img);
//                        imageView.setImageURI(photoUri);
//                    }
//                }
//        );
//
//
//        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main), (v, insets) -> {
//            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
//            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);
//            return insets;
//        });
//
//        if (ContextCompat.checkSelfPermission(this, Manifest.permission.READ_EXTERNAL_STORAGE)
//                != PackageManager.PERMISSION_GRANTED) {
//            // Request the permission
//            ActivityCompat.requestPermissions(this,
//                    new String[]{Manifest.permission.READ_EXTERNAL_STORAGE},
//                    REQUEST_PERMISSION_CODE);
//        }
//        if (ContextCompat.checkSelfPermission(this, Manifest.permission.CAMERA) != PackageManager.PERMISSION_GRANTED) {
//            ActivityCompat.requestPermissions(this, new String[]{Manifest.permission.CAMERA}, 101);
//        }
//
//
//        final ProgressBar progressBar = (ProgressBar) findViewById(R.id.progress_bar);
//        final ObjectAnimator objectAnimator = ObjectAnimator.ofInt(progressBar, "progress", progressBar.getProgress(), 100).setDuration(2000);
//        objectAnimator.addUpdateListener(new ValueAnimator.AnimatorUpdateListener() {
//            @Override
//            public void onAnimationUpdate(ValueAnimator valueAnimator) {
//                int progress = (int) valueAnimator.getAnimatedValue();
//                progressBar.setProgress(progress);
//            }
//        });
//
//        TextView btn = (TextView) findViewById(R.id.text_view_button);
//        btn.setOnClickListener(new View.OnClickListener() {
//            @Override
//            public void onClick(View view) {
//                objectAnimator.start();
//            }
//        });
//
//        Button upload_img_btn = findViewById(R.id.upload_img_vid_button);
//        Button open_cam_btn = findViewById(R.id.open_cam_button);
//
//        upload_img_btn.setOnClickListener(new View.OnClickListener() {
//            @Override
//            public void onClick(View v) {
//                Intent intent = new Intent();
//                intent.setType("image/*");
//                intent.setAction(Intent.ACTION_GET_CONTENT);
//                imagePickerLauncher.launch(Intent.createChooser(intent, "Select Image"));
//            }
//
//
//        });
//
//        open_cam_btn.setOnClickListener(new View.OnClickListener() {
//            @Override
//            public void onClick(View v) {
//
//
//                Intent intent = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);
//
////                 Create a file for the image
//                File photoFile = createImageFile();
//                if (photoFile != null) {
//
//                    photoUri = FileProvider.getUriForFile(ProcessingActivity.this, "com.example.myapplication.fileprovider", photoFile);
//                    intent.putExtra(MediaStore.EXTRA_OUTPUT, photoUri);
//                    intent.addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION);
//                    captureImageLauncher.launch(intent);
//                }
//
//            }
//
//            private File createImageFile() {
//                try {
//                    // Create an image file name
//                    String timeStamp = new SimpleDateFormat("yyyyMMdd_HHmmss", Locale.getDefault()).format(new Date());
//                    String imageFileName = "JPEG_" + timeStamp + "_";
//                    File storageDir = getExternalFilesDir(Environment.DIRECTORY_PICTURES);
//
//                    return File.createTempFile(imageFileName, ".jpg", storageDir); // Ensure this matches your file_paths.xml
//                } catch (IOException e) {
//                    e.printStackTrace();
//                    return null;
//                }
//            }
//        });
//
//    }
//
//
//    private void uploadImageToApi(String imagePath) {
//        // Define the URL of your API
//        String apiUrl = "https://ognjrl54krqatgsns5butrbksa0kqfhu.lambda-url.ap-south-1.on.aws/upload";
//
//        // Create a Volley request queue
//        RequestQueue requestQueue = Volley.newRequestQueue(this);
//
//        // Create a Multipart request
//        VolleyMultipartRequest multipartRequest = new VolleyMultipartRequest(Request.Method.POST, apiUrl,
//                new Response.Listener<NetworkResponse>() {
//                    @Override
//                    public void onResponse(NetworkResponse response) {
//                        try {
//                            String responseString = new String(response.data);
//                            JSONObject jsonResponse = new JSONObject(responseString);
//                            String message = jsonResponse.getString("message");
//                            String downloadUrl = jsonResponse.getString("download_url");
//
//                            Log.d("API_RESPONSE", "Message: " + message);
//                            Log.d("API_RESPONSE", "Download URL: " + downloadUrl);
//                        } catch (JSONException e) {
//                            e.printStackTrace();
//                            Log.e("API_RESPONSE", "Error parsing response");
//                        }
//                    }
//                },
//                new Response.ErrorListener() {
//                    @Override
//                    public void onErrorResponse(VolleyError error) {
//                        Log.e("API_ERROR", "Error: " + error.toString());
//                        if (error.networkResponse != null) {
//                            Log.e("API_ERROR", "Response Code: " + error.networkResponse.statusCode);
//                            Log.e("API_ERROR", "Response Data: " + new String(error.networkResponse.data));
//                        }
//                    }
//                }) {
//            @Override
//            protected Map<String, DataPart> getByteData() {
//                Map<String, DataPart> params = new HashMap<>();
//
//                // Ensure the file exists and is read correctly
//                File file = new File(imagePath);
//                if (file.exists()) {
//                    try {
//                        byte[] fileBytes = readFileAsBytes(file);
//                        params.put("file", new DataPart("image.jpg", fileBytes, "image/jpeg"));
//                    } catch (IOException e) {
//                        Log.e("API_ERROR", "Error reading file: " + e.getMessage());
//                        e.printStackTrace();
//                    }
//                } else {
//                    Log.e("API_ERROR", "File does not exist: " + imagePath);
//                }
//
//
//                return params;
//            }
//        };
//s
//        // Set a custom retry policy for long processing times
//        int tenMinutesInMillis = 600000; // 10 minutes
//        multipartRequest.setRetryPolicy(new DefaultRetryPolicy(
//                tenMinutesInMillis,
//                DefaultRetryPolicy.DEFAULT_MAX_RETRIES,
//                DefaultRetryPolicy.DEFAULT_BACKOFF_MULT));
//
//        // Add the request to the queue
//        requestQueue.add(multipartRequest);
//    }
//
//    // Helper method to read file as bytes
//    private byte[] readFileAsBytes(File file) throws IOException {
//        FileInputStream inputStream = new FileInputStream(file);
//        byte[] bytes = new byte[(int) file.length()];
//        inputStream.read(bytes);
//        inputStream.close();
//        return bytes;
//    }
//
//
//    private String getImagePathFromUri(Uri uri) {
//        String path = "";
//        if (uri != null) {
//            Cursor cursor = getContentResolver().query(uri, null, null, null, null);
//            if (cursor != null && cursor.moveToFirst()) {
//                int columnIndex = cursor.getColumnIndex(MediaStore.Images.Media.DATA);
//                path = cursor.getString(columnIndex);
//                cursor.close();
//            }
//        }
//        return path;
//    }
//
//    @Override
//    public void onRequestPermissionsResult(int requestCode, String[] permissions, int[] grantResults) {
//        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
//
//        if (requestCode == REQUEST_PERMISSION_CODE) {
//            if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
//                // Permission granted
//                Toast.makeText(this, "Permission granted", Toast.LENGTH_SHORT).show();
//            } else {
//                // Permission denied
//                Toast.makeText(this, "Permission denied", Toast.LENGTH_SHORT).show();
//            }
//        }
//    }
//
//
//}


public class ProcessingActivity extends AppCompatActivity {

    private static final int PICK_IMAGE_REQUEST = 1;
    private static final int REQUEST_IMAGE_CAPTURE = 1;
    private static final int REQUEST_PERMISSIONS = 101;
    private ImageView imageView;
    private Uri imageUri;
    private File imageFile;
    private Button btnUpload, btnProcess, open_cam_btn;

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
                Log.d("hariom mre bhai ", "the image uri =  " + imageUri);
                processImage(imageUri);
            } else {
                Toast.makeText(this, "Select an image first", Toast.LENGTH_SHORT).show();
            }

        });


    }


    //    <<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<capture image from camera and pass to the api >>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>
//    private void openCamera() {
//
//        Intent cameraIntent = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);
//        Log.d("hariom mre bhai", "openCamera: cameraIntent.resolveActivity(getPackageManager()) = " + cameraIntent.resolveActivity(getPackageManager()));
//        if (cameraIntent.resolveActivity(getPackageManager()) != null) {
//            try {
//                imageFile = createImageFile();
//                if (imageFile != null) {
//                    imageUri = FileProvider.getUriForFile(this, "com.example.myapplication.fileprovider", imageFile);
//                    cameraIntent.putExtra(MediaStore.EXTRA_OUTPUT, imageUri);
//                    startActivityForResult(cameraIntent, REQUEST_IMAGE_CAPTURE);
//                }
//            } catch (IOException e) {
//                e.printStackTrace();
//                Toast.makeText(this, "Failed to create image file", Toast.LENGTH_SHORT).show();
//            }
//        } else {
//            Toast.makeText(this, "give permission first", Toast.LENGTH_SHORT).show();
//        }
//    }


//    private File createImageFile() throws IOException {
//        String timestamp = new SimpleDateFormat("yyyyMMdd_HHmmss", Locale.getDefault()).format(new Date());
//        String imageFileName = "IMG_" + timestamp + "_";
//        File storageDir = getExternalFilesDir(Environment.DIRECTORY_PICTURES);
//        return File.createTempFile(imageFileName, ".jpg", storageDir);
//    }

//    @Override
//    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
//        super.onActivityResult(requestCode, resultCode, data);
//        if (requestCode == REQUEST_IMAGE_CAPTURE && resultCode == RESULT_OK) {
//            if (imageFile != null) {
//                // Display image in ImageView
//                ImageView imageView = findViewById(R.id.uploaded_img);
//                imageView.setImageURI(imageUri);
//
//                // Upload image
//                uploadImage(imageFile);
//            }
//        }
//    }

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


    private void processImage(Uri fileUri) {
        File file = new File(FileUtils.getPath(this, fileUri));
        RequestBody requestFile = RequestBody.create(MediaType.parse("image/*"), file);
        MultipartBody.Part body = MultipartBody.Part.createFormData("file", file.getName(), requestFile);


        ApiService apiService = RetrofitClient.getApiService();
        apiService.executeProcessing(body).enqueue(new Callback<ResponseBody>() {
            @Override
            public void onResponse(Call<ResponseBody> call, Response<ResponseBody> response) {
                if (response.isSuccessful()) {
                    try {
                        String presignedUrl = response.body().string();
                        displayImage(presignedUrl);
                        Toast.makeText(ProcessingActivity.this, "Processing succesful " + presignedUrl, Toast.LENGTH_SHORT).show();
                        Log.d("hariom mre bhai ", "onResponse: " + presignedUrl);
                    } catch (IOException e) {
                        e.printStackTrace();
                    }
                } else {
                    Toast.makeText(ProcessingActivity.this, "Processing Failed", Toast.LENGTH_SHORT).show();
                }
            }

            @Override
            public void onFailure(Call<ResponseBody> call, Throwable t) {
                Toast.makeText(ProcessingActivity.this, "Error: " + t.getMessage(), Toast.LENGTH_SHORT).show();
                Log.d("hariom mre bhai ", "onError: " + t.getMessage());
            }
        });
    }


    private void displayImage(String imageUrl) {
        Glide.with(this).load(imageUrl).into(imageView);
    }
}

