package com.mvnh.rythmap.auth;

import static com.mvnh.rythmap.auth.AuthActivity.errorDescriptions;

import android.content.Intent;
import android.os.Bundle;

import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;

import android.util.Log;
import android.widget.Toast;

import com.mvnh.rythmap.MainActivity;
import com.mvnh.rythmap.R;
import com.mvnh.rythmap.TokenManager;
import com.mvnh.rythmap.databinding.ActivityAuthBinding;
import com.mvnh.rythmap.databinding.ActivityLoginBinding;
import com.mvnh.rythmap.responses.ServiceGenerator;
import com.mvnh.rythmap.responses.account.AccountApi;
import com.mvnh.rythmap.responses.account.AccountLogin;
import com.mvnh.rythmap.responses.account.AuthResponse;

import java.io.IOException;

import retrofit2.Call;
import retrofit2.Response;

public class LoginActivity extends AppCompatActivity {
    private ActivityLoginBinding binding;
    private TokenManager tokenManager;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = ActivityLoginBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());

        tokenManager = new TokenManager(LoginActivity.this);

        binding.loginButton.setOnClickListener(v -> performLogin(
                binding.usernameField.getText().toString(),
                binding.passwordField.getText().toString()));
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
