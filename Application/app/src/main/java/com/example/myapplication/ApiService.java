package com.example.myapplication;

import okhttp3.MultipartBody;
import okhttp3.ResponseBody;
import retrofit2.Call;
import retrofit2.http.*;

public interface ApiService {
    @Multipart
    @POST("/upload")
    Call<ResponseBody> uploadFile(@Part MultipartBody.Part file);

    @GET("/files")
    Call<ResponseBody> listFiles();

    @Streaming
    @GET("/download/{filename}")
    Call<ResponseBody> downloadFile(@Path("filename") String filename);

    @DELETE("/delete/{filename}")
    Call<ResponseBody> deleteFile(@Path("filename") String filename);
}
