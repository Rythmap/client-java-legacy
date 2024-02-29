package com.mvnh.melodymap.auth;

import android.content.Intent;
import android.os.Bundle;
import android.util.Log;

import androidx.appcompat.app.AppCompatActivity;

import com.mvnh.melodymap.MainActivity;
import com.mvnh.melodymap.R;
import com.mvnh.melodymap.TokenManager;
import com.mvnh.melodymap.databinding.ActivityAuthBinding;

import java.util.HashMap;
import java.util.Map;

public class AuthActivity extends AppCompatActivity {
    private ActivityAuthBinding binding;
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
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = ActivityAuthBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());
        tokenManager = new TokenManager(AuthActivity.this);

        binding.loginButton.setOnClickListener(v -> startActivity(new Intent(AuthActivity.this, LoginActivity.class)));
        binding.registerButton.setOnClickListener(v -> startActivity(new Intent(AuthActivity.this, RegisterActivity.class)));

        String token = tokenManager.getToken();
        if (token != null && !token.isEmpty()) {
            Log.d("Melodymap", "token is not empty, running MainActivity");
            runOnUiThread(() -> {
                Intent intent = new Intent(AuthActivity.this, MainActivity.class);
                startActivity(intent);
                finish();
            });
        }
    }
}