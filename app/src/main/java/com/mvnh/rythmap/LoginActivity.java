package com.mvnh.rythmap;

import android.content.Intent;
import android.os.Bundle;

import androidx.activity.EdgeToEdge;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;

import android.util.Log;
import android.view.View;
import android.widget.Toast;

import com.mvnh.rythmap.databinding.ActivityLoginBinding;
import com.mvnh.rythmap.responses.ServiceGenerator;
import com.mvnh.rythmap.responses.account.AccountApi;
import com.mvnh.rythmap.responses.account.entities.AccountLogin;
import com.mvnh.rythmap.responses.account.entities.AccountRegister;
import com.mvnh.rythmap.responses.account.entities.AuthResponse;

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

        binding.loginButton.setOnClickListener(v -> performAuth(
                binding.usernameField.getText().toString(),
                binding.passwordField.getText().toString(),
                binding.emailField.getText().toString()));

        binding.registerButton.setOnClickListener(v -> {
            if (binding.registerButton.getText().toString().equals(getString(R.string.dont_have_an_account))) {
                binding.emailFieldLayout.setVisibility(View.VISIBLE);
                binding.usernameFieldLayout.setHint(R.string.username);
                binding.loginButton.setText(R.string.register);
                binding.registerButton.setText(R.string.i_have_an_account);
            } else {
                binding.emailField.setText("");
                binding.emailFieldLayout.setVisibility(View.GONE);
                binding.usernameFieldLayout.setHint(R.string.username_or_email);
                binding.loginButton.setText(R.string.login);
                binding.registerButton.setText(R.string.dont_have_an_account);
            }
        });

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

    private void performAuth(String username, String password, String email) {
        AccountApi accountApi = ServiceGenerator.createService(AccountApi.class);
        Call<AuthResponse> call;
        if (!email.isEmpty()) {
            call = accountApi.register(new AccountRegister(username, password, email));
        } else {
            call = accountApi.login(new AccountLogin(username, password));
        }

        if (!email.isEmpty()) { binding.emailField.setEnabled(false); }
        binding.usernameField.setEnabled(false);
        binding.passwordField.setEnabled(false);
        binding.loginButton.setEnabled(false);

        new Thread(() -> {
            try {
                Response<AuthResponse> response = call.execute();
                Log.d("Rythmap", String.valueOf(response.body()));
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
                    Log.e("Rythmap", response.message() + errorMessage);
                    runOnUiThread(() -> {
                        Toast.makeText(LoginActivity.this, errorMessage, Toast.LENGTH_SHORT).show();
                        enableFields(!email.isEmpty());
                    });
                }
            } catch (IOException e) {
                Log.e("Rythmap", String.valueOf(e));
                runOnUiThread(() -> {
                    Toast.makeText(LoginActivity.this, e.getMessage(), Toast.LENGTH_SHORT).show();
                    enableFields(!email.isEmpty());
                });
            }
        }).start();
    }

    private void enableFields(boolean email) {
        if (email) { binding.emailField.setEnabled(true); }
        binding.usernameField.setEnabled(true);
        binding.passwordField.setEnabled(true);
        binding.loginButton.setEnabled(true);
    }
}
