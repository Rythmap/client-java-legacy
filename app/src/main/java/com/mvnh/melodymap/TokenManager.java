package com.mvnh.melodymap;

import android.content.Context;
import android.content.SharedPreferences;
import android.util.Log;

import androidx.security.crypto.EncryptedSharedPreferences;
import androidx.security.crypto.MasterKeys;

public class TokenManager {

    private static final String ENCRYPTED_PREFS_FILE_NAME = "encrypted_prefs";
    private static final String ACCESS_TOKEN_KEY = "access_token";

    private SharedPreferences encryptedSharedPreferences;

    public TokenManager(Context context) {
        try {
            String masterKeyAlias = MasterKeys.getOrCreate(MasterKeys.AES256_GCM_SPEC);

            encryptedSharedPreferences = EncryptedSharedPreferences.create(
                    ENCRYPTED_PREFS_FILE_NAME,
                    masterKeyAlias,
                    context,
                    EncryptedSharedPreferences.PrefKeyEncryptionScheme.AES256_SIV,
                    EncryptedSharedPreferences.PrefValueEncryptionScheme.AES256_GCM
            );
        } catch (Exception e) {
        }
    }

    public void saveToken(String token) {
        SharedPreferences.Editor editor = encryptedSharedPreferences.edit();
        editor.putString(ACCESS_TOKEN_KEY, token);
        editor.apply();
    }

    public String getToken() {
        return encryptedSharedPreferences.getString(ACCESS_TOKEN_KEY, null);
    }

    public void clearToken() {
        SharedPreferences.Editor editor = encryptedSharedPreferences.edit();
        editor.remove(ACCESS_TOKEN_KEY);
        editor.apply();
    }
}
