package com.mvnh.rythmap.auth;

import android.content.Intent;
import android.os.Bundle;

import androidx.activity.EdgeToEdge;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;

import android.util.Log;
import android.widget.Toast;

import com.mvnh.rythmap.MainActivity;
import com.mvnh.rythmap.R;
import com.mvnh.rythmap.TokenManager;
import com.mvnh.rythmap.databinding.ActivityLoginBinding;
import com.mvnh.rythmap.responses.ServiceGenerator;
import com.mvnh.rythmap.responses.account.AccountApi;
import com.mvnh.rythmap.responses.account.AccountLogin;
import com.mvnh.rythmap.responses.account.AuthResponse;

import java.io.IOException;
import java.util.HashMap;
import java.util.Map;

import retrofit2.Call;
import retrofit2.Response;

public class LoginActivity extends AppCompatActivity {
    private ActivityLoginBinding binding;
    private TokenManager tokenManager;
    public static Map<Integer, String> errorDescriptions = new HashMap<>();
    static {
        errorDescriptions.put(400, "invalid username or password");
        errorDescriptions.put(401, "username must contain only English characters and numbers");
        errorDescriptions.put(402, "username length must be between 3 and 32 characters");
        errorDescriptions.put(403, "password length must be between 6 and 64 characters");
        errorDescriptions.put(404, "there is no user with this nickname");
        errorDescriptions.put(405, "invalid token");
        errorDescriptions.put(406, "username already registered");
        errorDescriptions.put(407, "username change failed");
        errorDescriptions.put(411, "invalid data");
        errorDescriptions.put(200, "all good");
    }

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = ActivityLoginBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());
        EdgeToEdge.enable(this);

        tokenManager = new TokenManager(LoginActivity.this);

        binding.loginButton.setOnClickListener(v -> performLogin(
                binding.usernameField.getText().toString(),
                binding.passwordField.getText().toString()));

        String token = tokenManager.getToken();
        if (token != null && !token.isEmpty()) {
            Log.d("Rythmap", "token is not empty, running MainActivity");
            runOnUiThread(() -> {
                Intent intent = new Intent(LoginActivity.this, MainActivity.class);
                startActivity(intent);
                finish();
            });
        }
    }

    private void performLogin(String username, String password) {
        AccountApi accountApi = ServiceGenerator.createService(AccountApi.class);
        Call<AuthResponse> call = accountApi.login(new AccountLogin(username, password));

        binding.usernameField.setEnabled(false);
        binding.passwordField.setEnabled(false);
        binding.loginButton.setEnabled(false);

        new Thread(() -> {
            try {
                Response<AuthResponse> response = call.execute();
                Log.d("Rythmap", String.valueOf(response));
                if (response.isSuccessful()) {
                    Log.d("Rythmap", "response code " + response.code());
                    AuthResponse authResponse = response.body();
                    String accessToken = authResponse.getAccessToken();
                    tokenManager.saveToken(accessToken);
                    Log.d("Rythmap", "access token " + accessToken);
                    runOnUiThread(() -> {
                        Log.d("Rythmap", "running main activity after successful response");
                        startActivity(new Intent(LoginActivity.this, MainActivity.class));
                        finishAffinity();
                    });
                } else {
                    String errorMessage = errorDescriptions.containsKey(response.code())
                            ? errorDescriptions.get(response.code())
                            : getString(R.string.unknown_error) + response.code();
                    Log.e("Rythmap", response.code() + errorMessage);
                    runOnUiThread(() -> {
                        Toast.makeText(LoginActivity.this, errorMessage, Toast.LENGTH_SHORT).show();
                        binding.usernameField.setEnabled(true);
                        binding.passwordField.setEnabled(true);
                        binding.loginButton.setEnabled(true);
                    });
                }
            } catch (IOException e) {
                Log.e("Rythmap", String.valueOf(e));
                runOnUiThread(() -> {
                    Toast.makeText(LoginActivity.this, e.getMessage(), Toast.LENGTH_SHORT).show();
                    binding.usernameField.setEnabled(true);
                    binding.passwordField.setEnabled(true);
                    binding.loginButton.setEnabled(true);
                });
            }
        }).start();
    }
}
