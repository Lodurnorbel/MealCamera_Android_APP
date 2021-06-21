package com.adolfo.android.mealcameraapp.ui;

import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.Gravity;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import com.adolfo.android.mealcameraapp.api.FileUploadService;
import com.adolfo.android.mealcameraapp.R;
import com.adolfo.android.mealcameraapp.api.ServiceGenerator;
import com.adolfo.android.mealcameraapp.api.UserRegistrationRequest;
import com.adolfo.android.mealcameraapp.api.responses.LoginResponse;

import androidx.appcompat.app.AppCompatActivity;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class LoginActivity  extends AppCompatActivity {

    private EditText username;
    private EditText password;
    private Button loginButton;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_login);

        username = findViewById(R.id.username);
        password = findViewById(R.id.password);
        loginButton = findViewById(R.id.login);

        loginButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                FileUploadService service = ServiceGenerator.createService(FileUploadService.class);

                UserRegistrationRequest userRegistrationRequest =
                        new UserRegistrationRequest(username.getText().toString(), password.getText().toString());
                Call<LoginResponse> call = service.loginFromOutside(userRegistrationRequest);
                call.enqueue(new Callback<LoginResponse>() {
                    @Override
                    public void onResponse(Call<LoginResponse> call, Response<LoginResponse> response) {
                        Log.v("Log in", "success");
                        if(response.body().msg.equals("correct")){
                            correctLogin();
                        }else{
                            Toast errorToast = Toast.makeText(getApplicationContext(), response.body().msg, Toast.LENGTH_SHORT);
                            errorToast.setGravity(Gravity.CENTER, 0, 0);
                            errorToast.show();
                        }
                    }
                    @Override
                    public void onFailure(Call<LoginResponse> call, Throwable t) {
                        Log.e("Log in:", t.getMessage());
                    }
                });

            }
        });
    }

    @Override
    protected void onStart(){
        super.onStart();
    }

    @Override
    protected void onStop() {
        super.onStop();
    }

    public void correctLogin(){
        Intent myIntent = new Intent(this, MainActivity.class);
        startActivity(myIntent);
    }
}
