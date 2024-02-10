package com.mvnh.melodymap.auth;

import static com.mvnh.melodymap.auth.AuthActivity.errorDescriptions;

import android.content.Intent;
import android.os.Bundle;

import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;

import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.material.textfield.TextInputEditText;
import com.mvnh.melodymap.MainActivity;
import com.mvnh.melodymap.R;
import com.mvnh.melodymap.TokenManager;
import com.mvnh.melodymap.responses.account.AccountApi;
import com.mvnh.melodymap.responses.account.AccountLogin;
import com.mvnh.melodymap.responses.account.AuthResponse;

import java.io.IOException;

import retrofit2.Call;
import retrofit2.Response;
import retrofit2.Retrofit;
import retrofit2.converter.gson.GsonConverterFactory;

public class LoginActivity extends AppCompatActivity {
    private TextInputEditText editUsername;
    private TextInputEditText editPassword;
    private Button loginButton;
    private TextView recoverPswdButton;

    private TokenManager tokenManager;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_login);

        editUsername = findViewById(R.id.usernameField);
        editPassword = findViewById(R.id.passwordField);
        loginButton = findViewById(R.id.loginButton);

        tokenManager = new TokenManager(LoginActivity.this);

        loginButton.setOnClickListener(v -> performLogin(editUsername.getText().toString(), editPassword.getText().toString()));
    }

    private void performLogin(String username, String password) {
        Retrofit retrofit = new Retrofit.Builder()
                .baseUrl("https://melomap-production.up.railway.app/")
                .addConverterFactory(GsonConverterFactory.create())
                .build();

        AccountApi accountApi = retrofit.create(AccountApi.class);

        Call<AuthResponse> call = accountApi.login(new AccountLogin(username, password));

        editUsername.setEnabled(false);
        editPassword.setEnabled(false);
        loginButton.setEnabled(false);

        new Thread(() -> {
            try {
                Response<AuthResponse> response = call.execute();
                if (response.isSuccessful()) {
                    AuthResponse authResponse = response.body();
                    tokenManager.saveToken(authResponse.getAccessToken());
                    runOnUiThread(() -> {
                        startActivity(new Intent(LoginActivity.this, MainActivity.class));
                        finish();
                        Toast.makeText(LoginActivity.this, "Successful login", Toast.LENGTH_SHORT).show();
                    });
                } else {
                    int errorCode = response.code();
                    String errorMessage = errorDescriptions.containsKey(errorCode)
                            ? errorDescriptions.get(errorCode)
                            : getString(R.string.unknown_error) + errorCode;
                    runOnUiThread(() -> {
                        Toast.makeText(LoginActivity.this, errorMessage, Toast.LENGTH_SHORT).show();
                        editUsername.setEnabled(true);
                        editPassword.setEnabled(true);
                        loginButton.setEnabled(true);
                    });
                }
            } catch (IOException e) {
                runOnUiThread(() -> {
                    Toast.makeText(LoginActivity.this, e.getMessage(), Toast.LENGTH_SHORT).show();
                    editUsername.setEnabled(true);
                    editPassword.setEnabled(true);
                    loginButton.setEnabled(true);
                });
            }
        }).start();
    }
}
