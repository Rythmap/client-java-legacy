package com.mvnh.melodymap.auth;

import static com.mvnh.melodymap.auth.AuthActivity.errorDescriptions;

import android.content.Intent;
import android.os.Bundle;

import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;

import android.util.Log;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.material.textfield.TextInputEditText;
import com.mvnh.melodymap.MainActivity;
import com.mvnh.melodymap.R;
import com.mvnh.melodymap.TokenManager;
import com.mvnh.melodymap.responses.ServiceGenerator;
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
        AccountApi accountApi = ServiceGenerator.createService(AccountApi.class);
        Call<AuthResponse> call = accountApi.login(new AccountLogin(username, password));

        editUsername.setEnabled(false);
        editPassword.setEnabled(false);
        loginButton.setEnabled(false);

        new Thread(() -> {
            try {
                Response<AuthResponse> response = call.execute();
                if (response.isSuccessful()) {
                    Log.d("Response", String.valueOf(response.code()));
                    AuthResponse authResponse = response.body();
                    Log.d("Auth response", String.valueOf(authResponse));
                    String accessToken = authResponse.getAccessToken();
                    Log.d("Access token", accessToken);
                    tokenManager.saveToken(accessToken);
                    runOnUiThread(() -> {
                        Log.d("Successful response intent", "running main activity after successful response");
                        startActivity(new Intent(LoginActivity.this, MainActivity.class));
                        finishAffinity();
                        Toast.makeText(LoginActivity.this, "successful login", Toast.LENGTH_SHORT).show();
                    });
                } else {
                    int responseCode = response.code();
                    Log.e("Not successful response", String.valueOf(responseCode));
                    String errorMessage = errorDescriptions.containsKey(responseCode)
                            ? errorDescriptions.get(responseCode)
                            : getString(R.string.unknown_error) + responseCode;
                    Log.e("Error message", errorMessage);
                    runOnUiThread(() -> {
                        Toast.makeText(LoginActivity.this, errorMessage, Toast.LENGTH_SHORT).show();
                        editUsername.setEnabled(true);
                        editPassword.setEnabled(true);
                        loginButton.setEnabled(true);
                    });
                }
            } catch (IOException e) {
                Log.e("Exception occured", String.valueOf(e));
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
