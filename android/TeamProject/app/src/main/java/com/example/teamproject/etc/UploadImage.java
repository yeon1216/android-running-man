package com.example.teamproject.etc;

import okhttp3.MultipartBody;
import okhttp3.RequestBody;
import okhttp3.ResponseBody;
import retrofit2.Call;
import retrofit2.http.GET;
import retrofit2.http.Multipart;
import retrofit2.http.POST;
import retrofit2.http.Part;
import retrofit2.http.Query;

public interface UploadImage {

    @Multipart
    @POST("/test")
    Call<ResponseBody> uploadFile1(
            @Part("id") RequestBody id,
            @Part MultipartBody.Part file);


    @GET("/hello/")
    Call<ResponseBody> uploadFile(
            @Query("description") String body
    );
}
