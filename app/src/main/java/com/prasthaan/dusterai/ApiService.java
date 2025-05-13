package com.prasthaan.dusterai;

//public interface ApiService {
//
//
//}

import java.util.List;

import okhttp3.MultipartBody;
import okhttp3.ResponseBody;
import retrofit2.Call;
import retrofit2.http.Multipart;
import retrofit2.http.POST;
import retrofit2.http.Part;

public interface ApiService {

    @Multipart
//    @POST("execute_ukiyoe")
//        // Ensure the correct endpoint
//    Call<ResponseBody> executeProcessing(@Part MultipartBody.Part file);

//        @Multipart
    @POST("/upload")
    Call<ResponseBody> uploadImage(@Part MultipartBody.Part file);

    @Multipart
    @POST("/detect_faces")
    Call<ResponseBody> executeProcessingDetectFace(@Part MultipartBody.Part file);

    @Multipart
    @POST("/single_face")
    Call<ResponseBody> executeProcessingFaceSwap(
            @Part MultipartBody.Part image1,
            @Part MultipartBody.Part image2
    );

    @Multipart
    @POST("/multi_face")
    Call<ResponseBody> executeProcessingMultiFaceSwap(
            @Part MultipartBody.Part target_image,
            @Part List<MultipartBody.Part> src_image_list,
            @Part List<MultipartBody.Part> dst_image_list
    );

    @Multipart
    @POST("/scale_image_2x")
    Call<ResponseBody> executeProcessingEnhanceImage2x(@Part MultipartBody.Part file);


    @Multipart
    @POST("/scale_image_4x")
    Call<ResponseBody> executeProcessingEnhanceImage4x(@Part MultipartBody.Part file);

    @Multipart
    @POST("/restore_images")
    Call<RestoreImageResponse> executeProcessingRestoreImage(@Part MultipartBody.Part file);

    @Multipart
    @POST("/execute_ukiyoe")
    Call<ResponseBody> executeProcessingUkiyoe(@Part MultipartBody.Part file);

    @Multipart
    @POST("/execute_monet")
    Call<ResponseBody> executeProcessingMonet(@Part MultipartBody.Part file);

    @Multipart
    @POST("/execute_van_gogh")
    Call<ResponseBody> executeProcessingVangogh(@Part MultipartBody.Part file);

    @Multipart
    @POST("/execute_cezanne")
    Call<ResponseBody> executeProcessingCezanne(@Part MultipartBody.Part file);

    @Multipart
    @POST("/execute_summer2winter")
    Call<ResponseBody> executeProcessingSummer2Winter(@Part MultipartBody.Part file);

    @Multipart
    @POST("/execute_winter2summer")
    Call<ResponseBody> executeProcessingWinter2Summer(@Part MultipartBody.Part file);
}
