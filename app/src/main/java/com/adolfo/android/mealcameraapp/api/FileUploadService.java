package com.adolfo.android.mealcameraapp.api;

import com.adolfo.android.mealcameraapp.api.responses.LoginResponse;
import com.adolfo.android.mealcameraapp.api.responses.ResponseJSON;

import okhttp3.MultipartBody;
import retrofit2.Call;
import retrofit2.http.Body;
import retrofit2.http.Headers;
import retrofit2.http.Multipart;
import retrofit2.http.POST;
import retrofit2.http.Part;

public interface FileUploadService {

    @Multipart
    @POST("neuronalNetwork/upload")
    Call<ResponseJSON> upload(@Part MultipartBody.Part file);

    @Headers("Content-type: application/json")
    @POST("auth/loginFromOutside")
    Call<LoginResponse> loginFromOutside(@Body UserRegistrationRequest body);
}
