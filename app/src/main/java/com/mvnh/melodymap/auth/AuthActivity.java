package com.mvnh.melodymap.auth;

import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.widget.Button;

import androidx.appcompat.app.AppCompatActivity;

import com.mvnh.melodymap.MainActivity;
import com.mvnh.melodymap.R;
import com.mvnh.melodymap.TokenManager;

import java.util.HashMap;
import java.util.Map;

public class AuthActivity extends AppCompatActivity {

    private Button loginButton;
    private Button registerButton;
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
        errorDescriptions.put(200, "account registered successfully!");
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_auth);
        loginButton = findViewById(R.id.loginButton);
        registerButton = findViewById(R.id.registerButton);
        tokenManager = new TokenManager(AuthActivity.this);

        loginButton.setOnClickListener(v -> startActivity(new Intent(AuthActivity.this, LoginActivity.class)));
        registerButton.setOnClickListener(v -> startActivity(new Intent(AuthActivity.this, RegisterActivity.class)));

        String token = tokenManager.getToken();
        if (token != null && !token.isEmpty()) {
            Log.d("Auth token validation", "token is not empty, running MainActivity");
            runOnUiThread(() -> {
                Intent intent = new Intent(AuthActivity.this, MainActivity.class);
                startActivity(intent);
                finish();
            });
        }
    }
}