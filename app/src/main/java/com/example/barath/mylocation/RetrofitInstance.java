package com.example.barath.mylocation;

import okhttp3.OkHttpClient;
import retrofit2.Retrofit;

public class RetrofitInstance {

    public static API getApi(String userName,String password){
        OkHttpClient client =getClient(userName,password);
        return new Retrofit.Builder()
                .baseUrl("http://test.selliscope.com/api/v1/")
                .client(client)
                .build()
                .create(API.class);
    }

    private static OkHttpClient getClient(String userName,String password) {
        return new OkHttpClient.Builder()
                .addInterceptor(new BasicAuthInterceptor(userName, password))
                .build();
    }


}
