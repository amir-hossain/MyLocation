package com.example.barath.mylocation;

import android.content.Context;
import android.content.Intent;
import android.databinding.DataBindingUtil;
import android.net.ConnectivityManager;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.View;
import android.widget.Toast;

import com.example.barath.mylocation.databinding.ActivityLoginBinding;

import okhttp3.ResponseBody;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class LoginActivity extends AppCompatActivity {

    private ActivityLoginBinding binding;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding= DataBindingUtil. setContentView(this,R.layout.activity_login);

    }

    public void login(View view){
        if(isNetworkConnected()){
            checkLogin();
        }else {
            showToast("Not connected to a network");
        }



    }

    private void checkLogin() {
        String userName=binding.userName.getText().toString();
        String password=binding.password.getText().toString();
        if(userName.isEmpty() || password.isEmpty()){
            showToast("Field is empty");
        }else {
            RetrofitInstance.getApi(userName,password).login()
                    .enqueue(new Callback<ResponseBody>() {
                        @Override
                        public void onResponse(Call<ResponseBody> call, Response<ResponseBody> response) {
                            if(response.body()==null){
                                showToast("wrong credential");
                            }else {
                                startActivity(new Intent(LoginActivity.this,MapsActivity.class));
                            }

                        }

                        @Override
                        public void onFailure(Call<ResponseBody> call, Throwable t) {
                            showToast("internal error");
                        }
                    });
        }
    }





    private void showToast(String s) {
        Toast.makeText(this,s,Toast.LENGTH_LONG).show();
    }

    private boolean isNetworkConnected() {
        ConnectivityManager cm = (ConnectivityManager) getSystemService(Context.CONNECTIVITY_SERVICE);

        return cm.getActiveNetworkInfo() != null;
    }
}
