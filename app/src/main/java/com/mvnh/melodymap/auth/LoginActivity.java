package com.mvnh.melodymap.auth;

import static com.mvnh.melodymap.auth.AuthActivity.errorDescriptions;

import android.content.Intent;
import android.os.Bundle;

import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;

import android.util.Log;
import android.view.View;
import android.widget.Toast;

import com.mvnh.melodymap.MainActivity;
import com.mvnh.melodymap.R;
import com.mvnh.melodymap.TokenManager;
import com.mvnh.melodymap.databinding.ActivityAuthBinding;
import com.mvnh.melodymap.databinding.ActivityLoginBinding;
import com.mvnh.melodymap.responses.ServiceGenerator;
import com.mvnh.melodymap.responses.account.AccountApi;
import com.mvnh.melodymap.responses.account.AccountLogin;
import com.mvnh.melodymap.responses.account.AuthResponse;

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
                Log.d("Melodymap", String.valueOf(response));
                if (response.isSuccessful()) {
                    Log.d("Melodymap", "response code " + response.code());
                    AuthResponse authResponse = response.body();
                    String accessToken = authResponse.getAccessToken();
                    tokenManager.saveToken(accessToken);
                    Log.d("Melodymap", "access token " + accessToken);
                    runOnUiThread(() -> {
                        Log.d("Melodymap", "running main activity after successful response");
                        startActivity(new Intent(LoginActivity.this, MainActivity.class));
                        finishAffinity();
                    });
                } else {
                    String errorMessage = errorDescriptions.containsKey(response.code())
                            ? errorDescriptions.get(response.code())
                            : getString(R.string.unknown_error) + response.code();
                    Log.e("Melodymap", response.code() + errorMessage);
                    runOnUiThread(() -> {
                        Toast.makeText(LoginActivity.this, errorMessage, Toast.LENGTH_SHORT).show();
                        binding.usernameField.setEnabled(true);
                        binding.passwordField.setEnabled(true);
                        binding.loginButton.setEnabled(true);
                    });
                }
            } catch (IOException e) {
                Log.e("Melodymap", String.valueOf(e));
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
