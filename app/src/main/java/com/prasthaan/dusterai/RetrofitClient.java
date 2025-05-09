package com.prasthaan.dusterai;

//public class RetrofitClient {
//}


import java.util.concurrent.TimeUnit;

import okhttp3.OkHttpClient;
import retrofit2.Retrofit;
import retrofit2.converter.gson.GsonConverterFactory;

public class RetrofitClient {
    private static final String BASE_URL = "https://ognjrl54krqatgsns5butrbksa0kqfhu.lambda-url.ap-south-1.on.aws/";
    private static final String BASE_URL_IMAGE_RESTORE = "https://u3dvih2namg3i5sukskixp2dza0afuep.lambda-url.ap-south-1.on.aws/";
    private static final String BASE_URL_IMAGE_ENHANCE = "https://orshv44xnu3ebwzwtt3khdhl6y0mggye.lambda-url.ap-south-1.on.aws/";
    private static final String BASE_URL_FACE_SWAP = "https://pusyqcspflfddakepsb435c2fy0qwuzb.lambda-url.ap-south-1.on.aws/";
    private static final String BASE_URL_FACE_DETECTION = "https://ludgv6k2plzwj3vlm3vilaycia0kvspr.lambda-url.ap-south-1.on.aws/";


    private static final OkHttpClient okHttpClientFaceDetection = new OkHttpClient.Builder()
            .connectTimeout(15, TimeUnit.MINUTES) // Increase connect timeout
            .readTimeout(15, TimeUnit.MINUTES) // Increase read timeout
            .writeTimeout(15, TimeUnit.MINUTES) // Increase write timeout
            .build();

    private static final Retrofit retrofitFaceDetection = new Retrofit.Builder()
            .baseUrl(BASE_URL_FACE_DETECTION)
            .client(okHttpClientFaceDetection) // Use the custom OkHttpClient
            .addConverterFactory(GsonConverterFactory.create())
            .build();
    private static final OkHttpClient okHttpClientFaceSwap = new OkHttpClient.Builder()
            .connectTimeout(15, TimeUnit.MINUTES) // Increase connect timeout
            .readTimeout(15, TimeUnit.MINUTES) // Increase read timeout
            .writeTimeout(15, TimeUnit.MINUTES) // Increase write timeout
            .build();

    private static final Retrofit retrofitFaceSwap = new Retrofit.Builder()
            .baseUrl(BASE_URL_FACE_SWAP)
            .client(okHttpClientFaceSwap) // Use the custom OkHttpClient
            .addConverterFactory(GsonConverterFactory.create())
            .build();


    private static final OkHttpClient okHttpClientImageEnhance = new OkHttpClient.Builder()
            .connectTimeout(15, TimeUnit.MINUTES) // Increase connect timeout
            .readTimeout(15, TimeUnit.MINUTES) // Increase read timeout
            .writeTimeout(15, TimeUnit.MINUTES) // Increase write timeout
            .build();

    private static final Retrofit retrofitImageEnhance = new Retrofit.Builder()
            .baseUrl(BASE_URL_IMAGE_ENHANCE)
            .client(okHttpClientImageEnhance) // Use the custom OkHttpClient
            .addConverterFactory(GsonConverterFactory.create())
            .build();

    private static final OkHttpClient okHttpClientImageRestore = new OkHttpClient.Builder()
            .connectTimeout(15, TimeUnit.MINUTES) // Increase connect timeout
            .readTimeout(15, TimeUnit.MINUTES) // Increase read timeout
            .writeTimeout(15, TimeUnit.MINUTES) // Increase write timeout
            .build();

    private static final Retrofit retrofitImageRestore = new Retrofit.Builder()
            .baseUrl(BASE_URL_IMAGE_RESTORE)
            .client(okHttpClientImageRestore) // Use the custom OkHttpClient
            .addConverterFactory(GsonConverterFactory.create())
            .build();
    private static final OkHttpClient okHttpClient = new OkHttpClient.Builder()
            .connectTimeout(15, TimeUnit.MINUTES) // Increase connect timeout
            .readTimeout(15, TimeUnit.MINUTES) // Increase read timeout
            .writeTimeout(15, TimeUnit.MINUTES) // Increase write timeout
            .build();
    private static final Retrofit retrofit = new Retrofit.Builder()
            .baseUrl(BASE_URL)
            .client(okHttpClient) // Use the custom OkHttpClient
            .addConverterFactory(GsonConverterFactory.create())
            .build();


    public static ApiService getApiServiceFaceDetection() {
        return retrofitFaceDetection.create(ApiService.class);
    }

    public static ApiService getApiServiceFaceSwap() {
        return retrofitFaceSwap.create(ApiService.class);
    }

    public static ApiService getApiServiceImageEnhance() {
        return retrofitImageEnhance.create(ApiService.class);
    }

    public static ApiService getApiServiceImageRestore() {
        return retrofitImageRestore.create(ApiService.class);
    }

    public static ApiService getApiService() {
        return retrofit.create(ApiService.class);
    }
}

