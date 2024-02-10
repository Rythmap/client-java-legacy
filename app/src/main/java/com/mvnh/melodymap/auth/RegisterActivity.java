package com.mvnh.melodymap.auth;

import static com.mvnh.melodymap.auth.AuthActivity.errorDescriptions;

import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.os.Bundle;
import android.widget.Button;
import android.widget.Toast;

import com.google.android.material.textfield.TextInputEditText;
import com.mvnh.melodymap.MainActivity;
import com.mvnh.melodymap.R;
import com.mvnh.melodymap.TokenManager;
import com.mvnh.melodymap.responses.account.AccountApi;
import com.mvnh.melodymap.responses.account.AccountRegister;
import com.mvnh.melodymap.responses.account.AuthResponse;

import java.io.IOException;

import retrofit2.Call;
import retrofit2.Response;
import retrofit2.Retrofit;
import retrofit2.converter.gson.GsonConverterFactory;

public class RegisterActivity extends AppCompatActivity {

    private TextInputEditText editEmail;
    private TextInputEditText editUsername;
    private TextInputEditText editPassword;
    private Button registerButton;
    private TokenManager tokenManager;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_register);

        editEmail = findViewById(R.id.emailField);
        editUsername = findViewById(R.id.usernameField);
        editPassword = findViewById(R.id.passwordField);
        registerButton = findViewById(R.id.registerButton);
        tokenManager = new TokenManager(RegisterActivity.this);

        registerButton.setOnClickListener(v -> performRegister(
                editUsername.getText().toString(),
                editPassword.getText().toString(),
                editEmail.getText().toString()));
    }

    private void performRegister(String username, String password, String email) {
        Retrofit retrofit = new Retrofit.Builder()
                .baseUrl("https://melomap-production.up.railway.app/")
                .addConverterFactory(GsonConverterFactory.create())
                .build();

        AccountApi accountApi = retrofit.create(AccountApi.class);

        Call<AuthResponse> call = accountApi.register(new AccountRegister(username, password, email));

        editEmail.setEnabled(false);
        editUsername.setEnabled(false);
        editPassword.setEnabled(false);
        registerButton.setEnabled(false);

        new Thread(() -> {
           try {
               Response<AuthResponse> response = call.execute();
               if (response.isSuccessful()) {
                   AuthResponse authResponse = response.body();
                   tokenManager.saveToken(authResponse.getAccessToken());
                   runOnUiThread(() -> {
                       startActivity(new Intent(RegisterActivity.this, MainActivity.class));
                       finish();
                       Toast.makeText(RegisterActivity.this, errorDescriptions.get(response.code()), Toast.LENGTH_SHORT).show();
                   });
               } else {
                   int errorCode = response.code();
                   String errorMessage = errorDescriptions.containsKey(errorCode)
                           ? errorDescriptions.get(errorCode)
                           : getString(R.string.unknown_error) + errorCode;
                   runOnUiThread(() -> {
                       Toast.makeText(RegisterActivity.this, errorMessage, Toast.LENGTH_SHORT).show();
                       editUsername.setEnabled(true);
                       editUsername.setEnabled(true);
                       editPassword.setEnabled(true);
                       registerButton.setEnabled(true);
                   });
               }
           } catch (IOException e) {
               runOnUiThread(() -> {
                   Toast.makeText(RegisterActivity.this, e.getMessage(), Toast.LENGTH_SHORT).show();
                   editEmail.setEnabled(true);
                   editUsername.setEnabled(true);
                   editPassword.setEnabled(true);
                   registerButton.setEnabled(true);
               });
           }
        }).start();
    }
}