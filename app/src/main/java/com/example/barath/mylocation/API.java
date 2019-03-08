package com.example.barath.mylocation;

import okhttp3.Response;
import okhttp3.ResponseBody;
import retrofit2.Call;
import retrofit2.http.POST;

public interface API {
    @POST("login")
    Call<ResponseBody> login();
}
