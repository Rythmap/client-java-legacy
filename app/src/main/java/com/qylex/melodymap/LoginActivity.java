package com.qylex.melodymap;

import androidx.appcompat.app.AppCompatActivity;
import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import com.qylex.melodymap.responses.account.AccountApi;
import com.qylex.melodymap.responses.account.AccountLogin;
import com.qylex.melodymap.responses.account.AccountRegister;
import com.qylex.melodymap.responses.account.AuthResponse;

import java.io.IOException;

import java.util.HashMap;
import java.util.Map;

import retrofit2.Call;
import retrofit2.Response;
import retrofit2.Retrofit;
import retrofit2.converter.gson.GsonConverterFactory;

public class LoginActivity extends AppCompatActivity {

    private TokenManager tokenManager;

    private boolean isSignUp = true;

    private EditText editEmail;
    private EditText editUsername;
    private EditText editPassword;

    private TextView signUpButton;
    private TextView status;
    private TextView recoverPassword;

    private Button loginRegister;

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
        setContentView(R.layout.activity_login);

        editUsername = findViewById(R.id.editTextUsername);
        editUsername.setSingleLine(true);
        editPassword = findViewById(R.id.editTextPassword);
        editEmail = findViewById(R.id.editTextEmail);
        signUpButton = findViewById(R.id.textViewSignUp);
        loginRegister = findViewById(R.id.buttonLogin);
        status = findViewById(R.id.textViewStatus);
        recoverPassword = findViewById(R.id.recoverPassView);
        tokenManager = new TokenManager(this);

        String token = tokenManager.getToken();
        if (token != null && !token.isEmpty()) {
            runOnUiThread(() -> {
                Intent intent = new Intent(LoginActivity.this, MainActivity.class);
                startActivity(intent);
                finish();
            });
        }

        signUpButton.setText(getString(R.string.dont_have_an_account));
        editEmail.setVisibility(View.INVISIBLE);
        editUsername.setHint(getString(R.string.username_or_email));

        signUpButton.setOnClickListener(v -> {
            if (isSignUp) {
                signUpButton.setText(getString(R.string.i_have_an_account));
                loginRegister.setText(getString(R.string.register));
                loginRegister.setOnClickListener(registerClickListener);
                isSignUp = false;

                editEmail.setVisibility(View.VISIBLE);

                editUsername.setHint(getString(R.string.username));

            } else {
                signUpButton.setText(getString(R.string.dont_have_an_account));
                loginRegister.setText(getString(R.string.login));
                loginRegister.setOnClickListener(loginClickListener);
                isSignUp = true;

                editEmail.setVisibility(View.INVISIBLE);

                editUsername.setHint(getString(R.string.username_or_email));
            }
        });

        loginRegister.setOnClickListener(loginClickListener);
    }

    private final View.OnClickListener registerClickListener = v -> performAuthentication("account.register");
    private final View.OnClickListener loginClickListener = v -> performAuthentication("account.login");

    private void performAuthentication(String action) {
        String username = editUsername.getText().toString();
        String password = editPassword.getText().toString();
        String email = editEmail.getText().toString();

        Retrofit retrofit = new Retrofit.Builder()
                .baseUrl("https://melomap-production.up.railway.app/")
                .addConverterFactory(GsonConverterFactory.create())
                .build();

        AccountApi accountApi = retrofit.create(AccountApi.class);

        Call<AuthResponse> call;
        if (action.equals("account.register")) {
            call = accountApi.register(new AccountRegister(username, password, email));
        } else if (action.equals("account.login")) {
            call = accountApi.login(new AccountLogin(username, password));
        } else {
            return;
        }

        loginRegister.setEnabled(false);
        signUpButton.setEnabled(false);
        new Thread(() -> {
            try {
                Response<AuthResponse> response = call.execute();
                if (response.isSuccessful()) {
                    AuthResponse authResponse = response.body();
                    if (action.equals("account.register")) {
                        runOnUiThread(() -> {
                            Toast.makeText(LoginActivity.this, errorDescriptions.get(response.code()), Toast.LENGTH_SHORT).show();
                            loginRegister.setEnabled(true);
                            signUpButton.setEnabled(true);
                        });
                    } else if (action.equals("account.login")) {
                        runOnUiThread(() -> {
                            tokenManager.saveToken(authResponse.getAccessToken());
                            Intent intent = new Intent(LoginActivity.this, MainActivity.class);
                            startActivity(intent);
                            finish();
                            Toast.makeText(LoginActivity.this, "successful login", Toast.LENGTH_SHORT).show();
                        });
                    } else {
                        runOnUiThread(() -> {
                            Toast.makeText(LoginActivity.this, errorDescriptions.get(response.code()), Toast.LENGTH_SHORT).show();
                            loginRegister.setEnabled(true);
                            signUpButton.setEnabled(true);
                        });
                    }
                } else {
                    runOnUiThread(() -> {
                        if (errorDescriptions.containsKey(response.code())) {
                            Toast.makeText(LoginActivity.this, errorDescriptions.get(response.code()), Toast.LENGTH_SHORT).show();
                            loginRegister.setEnabled(true);
                            signUpButton.setEnabled(true);
                        } else {
                            Toast.makeText(LoginActivity.this, R.string.unknown_error + response.code(), Toast.LENGTH_SHORT).show();
                            loginRegister.setEnabled(true);
                            signUpButton.setEnabled(true);
                        }
                    });
                }
            } catch (IOException e) {
                runOnUiThread(() -> {
                    Toast.makeText(LoginActivity.this, e.getMessage(), Toast.LENGTH_SHORT).show();
                    loginRegister.setEnabled(true);
                    signUpButton.setEnabled(true);
                });
            }
        }).start();
    }
}