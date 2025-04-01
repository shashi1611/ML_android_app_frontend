package com.prasthaan.dusterai;

//public class RetrofitClient {
//}


import java.util.concurrent.TimeUnit;

import okhttp3.OkHttpClient;
import retrofit2.Retrofit;
import retrofit2.converter.gson.GsonConverterFactory;

public class RetrofitClient {
    private static final String BASE_URL = "https://ognjrl54krqatgsns5butrbksa0kqfhu.lambda-url.ap-south-1.on.aws/";

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

    public static ApiService getApiService() {
        return retrofit.create(ApiService.class);
    }
}

