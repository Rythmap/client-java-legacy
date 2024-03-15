package com.mvnh.melodymap.auth;

import static com.mvnh.melodymap.auth.AuthActivity.errorDescriptions;

import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import com.mvnh.melodymap.MainActivity;
import com.mvnh.melodymap.R;
import com.mvnh.melodymap.TokenManager;
import com.mvnh.melodymap.databinding.ActivityRegisterBinding;
import com.mvnh.melodymap.responses.ServiceGenerator;
import com.mvnh.melodymap.responses.account.AccountApi;
import com.mvnh.melodymap.responses.account.AccountRegister;
import com.mvnh.melodymap.responses.account.AuthResponse;

import java.io.IOException;

import retrofit2.Call;
import retrofit2.Response;

public class RegisterActivity extends AppCompatActivity {
    private ActivityRegisterBinding binding;
    private TokenManager tokenManager;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = ActivityRegisterBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());

        tokenManager = new TokenManager(RegisterActivity.this);

        binding.registerButton.setOnClickListener(v -> performRegister(
                binding.usernameField.getText().toString(),
                binding.passwordField.getText().toString(),
                binding.emailField.getText().toString()));
    }

    private void performRegister(String username, String password, String email) {
        AccountApi accountApi = ServiceGenerator.createService(AccountApi.class);
        Call<AuthResponse> call = accountApi.register(new AccountRegister(username, password, email));

        binding.emailField.setEnabled(false);
        binding.usernameField.setEnabled(false);
        binding.passwordField.setEnabled(false);
        binding.registerButton.setEnabled(false);

        new Thread(() -> {
           try {
               Response<AuthResponse> response = call.execute();
               if (response.isSuccessful()) {
                   Log.d("Melodymap", "response code" + response.code());
                   AuthResponse authResponse = response.body();
                   String accessToken = authResponse.getAccessToken();
                   tokenManager.saveToken(accessToken);
                   Log.d("Melodymap", "access token" + accessToken);
                   runOnUiThread(() -> {
                       startActivity(new Intent(RegisterActivity.this, MainActivity.class));
                       finish();
                       Toast.makeText(RegisterActivity.this, errorDescriptions.get(response.code()), Toast.LENGTH_SHORT).show();
                   });
               } else {
                   String errorMessage = errorDescriptions.containsKey(response.code())
                           ? errorDescriptions.get(response.code())
                           : getString(R.string.unknown_error) + response.code();
                   Log.e("Melodymap", errorMessage);
                   runOnUiThread(() -> {
                       Toast.makeText(RegisterActivity.this, errorMessage, Toast.LENGTH_SHORT).show();
                       binding.emailField.setEnabled(true);
                       binding.usernameField.setEnabled(true);
                       binding.passwordField.setEnabled(true);
                       binding.registerButton.setEnabled(true);
                   });
               }
           } catch (IOException e) {
               Log.e("Melodymap", String.valueOf(e));
               runOnUiThread(() -> {
                   Toast.makeText(RegisterActivity.this, e.getMessage(), Toast.LENGTH_SHORT).show();
                   binding.emailField.setEnabled(true);
                   binding.usernameField.setEnabled(true);
                   binding.passwordField.setEnabled(true);
                   binding.registerButton.setEnabled(true);
               });
           }
        }).start();
    }
}