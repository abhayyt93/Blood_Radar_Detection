package com.abhay.bloodradar.repository;

import android.util.Log;
import com.abhay.bloodradar.api.ApiService;
import org.json.JSONObject;

public class AuthRepository {
    private static final String TAG = "AuthRepository";

    public interface AuthCallback {
        void onRegistrationSuccess(JSONObject response);
        void onRegistrationError(String error);
        void onLoginSuccess(JSONObject response);
        void onLoginError(String error);
    }

    public static void registerUser(
            String name,
            String phone,
            String password,
            String city,
            String bloodGroup,
            String bio,
            AuthCallback callback) {

        if (!validateRegistration(name, phone, password, city, bloodGroup, bio, callback)) {
            return;
        }

        ApiService.register(name, phone, password, city, bloodGroup, bio,
                new ApiService.ApiCallback<JSONObject>() {
                    @Override
                    public void onSuccess(JSONObject data) {
                        Log.d(TAG, "Registration Success");
                        callback.onRegistrationSuccess(data);
                    }

                    @Override
                    public void onError(String error) {
                        Log.e(TAG, "Registration Error: " + error);
                        callback.onRegistrationError(error);
                    }
                });
    }

    public static void loginUser(
            String phone,
            String password,
            AuthCallback callback) {

        if (!validateLogin(phone, password, callback)) {
            return;
        }

        ApiService.login(phone, password,
                new ApiService.ApiCallback<JSONObject>() {
                    @Override
                    public void onSuccess(JSONObject data) {
                        Log.d(TAG, "Login Success");
                        callback.onLoginSuccess(data);
                    }

                    @Override
                    public void onError(String error) {
                        Log.e(TAG, "Login Error: " + error);
                        callback.onLoginError(error);
                    }
                });
    }

    private static boolean validateRegistration(
            String name,
            String phone,
            String password,
            String city,
            String bloodGroup,
            String bio,
            AuthCallback callback) {

        if (name == null || name.trim().isEmpty()) {
            callback.onRegistrationError("Name is required");
            return false;
        }
        if (phone == null || phone.trim().isEmpty()) {
            callback.onRegistrationError("Phone is required");
            return false;
        }
        if (phone.length() != 10) {
            callback.onRegistrationError("Phone must be 10 digits");
            return false;
        }
        if (password == null || password.trim().isEmpty()) {
            callback.onRegistrationError("Password is required");
            return false;
        }
        if (password.length() < 6) {
            callback.onRegistrationError("Password must be at least 6 characters");
            return false;
        }
        if (city == null || city.trim().isEmpty()) {
            callback.onRegistrationError("City is required");
            return false;
        }
        if (bloodGroup == null || bloodGroup.trim().isEmpty()) {
            callback.onRegistrationError("Blood Group is required");
            return false;
        }

        return true;
    }

    private static boolean validateLogin(
            String phone,
            String password,
            AuthCallback callback) {

        if (phone == null || phone.trim().isEmpty()) {
            callback.onLoginError("Phone is required");
            return false;
        }
        if (phone.length() != 10) {
            callback.onLoginError("Phone must be 10 digits");
            return false;
        }
        if (password == null || password.trim().isEmpty()) {
            callback.onLoginError("Password is required");
            return false;
        }

        return true;
    }
}

