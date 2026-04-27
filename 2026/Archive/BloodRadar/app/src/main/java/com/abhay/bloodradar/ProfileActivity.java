package com.abhay.bloodradar;

import android.content.Intent;
import android.content.SharedPreferences;
import android.net.Uri;
import android.os.Bundle;
import android.provider.MediaStore;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;
import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import com.abhay.bloodradar.api.AppConfig;
import com.bumptech.glide.Glide;
import com.yalantis.ucrop.UCrop;
import okhttp3.*;
import org.json.JSONObject;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;

public class ProfileActivity extends AppCompatActivity {
    private ImageView ivProfilePic;
    private TextView tvProfileInitial;
    private String authToken;
    private String currentUserId;
    private final OkHttpClient client = new OkHttpClient();
    private Uri croppedImageUri;

    private final ActivityResultLauncher<Intent> imagePickerLauncher = registerForActivityResult(
            new ActivityResultContracts.StartActivityForResult(),
            result -> {
                if (result.getResultCode() == RESULT_OK && result.getData() != null) {
                    Uri imageUri = result.getData().getData();
                    if (imageUri != null) {
                        startCrop(imageUri);
                    }
                }
            }
    );

    private final ActivityResultLauncher<Intent> cropLauncher = registerForActivityResult(
            new ActivityResultContracts.StartActivityForResult(),
            result -> {
                if (result.getResultCode() == RESULT_OK && result.getData() != null) {
                    Uri croppedUri = UCrop.getOutput(result.getData());
                    if (croppedUri != null) {
                        android.util.Log.d("ProfileActivity", "Cropped URI: " + croppedUri.toString());
                        uploadProfilePic(croppedUri);
                    } else {
                        android.util.Log.e("ProfileActivity", "Cropped URI is null!");
                        runOnUiThread(() -> Toast.makeText(ProfileActivity.this, "Crop failed", Toast.LENGTH_SHORT).show());
                    }
                } else {
                    android.util.Log.d("ProfileActivity", "Crop cancelled or failed. ResultCode: " + result.getResultCode());
                }
            }
    );

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_profile);

        // Setup Toolbar with back button
        Toolbar toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        if (getSupportActionBar() != null) {
            getSupportActionBar().setDisplayHomeAsUpEnabled(true);
            getSupportActionBar().setDisplayShowHomeEnabled(true);
        }

        // Set navigation icon (back arrow) to white
        if (toolbar.getNavigationIcon() != null) {
            toolbar.getNavigationIcon().setTint(getResources().getColor(R.color.white));
        }

        TextView tvProfileInitial = findViewById(R.id.tvProfileInitial);
        TextView tvName = findViewById(R.id.tvName);
        TextView tvPhone = findViewById(R.id.tvPhone);
        TextView tvCity = findViewById(R.id.tvCity);
        TextView tvBloodGroup = findViewById(R.id.tvBloodGroup);
        TextView tvUserType = findViewById(R.id.tvUserType);
        TextView tvBio = findViewById(R.id.tvBio);

        // Get user data from SharedPreferences
        SharedPreferences prefs = getSharedPreferences("UserData", MODE_PRIVATE);
        authToken = prefs.getString("token", "");
        String userData = prefs.getString("user", null);

        tvProfileInitial = findViewById(R.id.tvProfileInitial);
        ivProfilePic = findViewById(R.id.ivProfilePic);
        View layoutProfileImage = findViewById(R.id.layoutProfileImage);

        if (layoutProfileImage != null) {
            layoutProfileImage.setOnClickListener(v -> openImagePicker());
        }


        if (userData != null) {
            try {
                JSONObject user = new JSONObject(userData);
                String name = user.optString("name", "User");

                // Set profile picture or initial
                String profilePic = user.optString("profilePic", "");
                if (profilePic != null && !profilePic.isEmpty()) {
                    ivProfilePic.setVisibility(View.VISIBLE);
                    tvProfileInitial.setVisibility(View.GONE);
                    // Remove /api from BASE_URL for image loading since uploads is served at root
                    String baseUrlForImages = AppConfig.BASE_URL.replace("/api", "");
                    String imageUrl = baseUrlForImages + profilePic;
                    Glide.with(this)
                            .load(imageUrl)
                            .placeholder(R.drawable.user_circle_bg)
                            .circleCrop()
                            .into(ivProfilePic);
                } else {
                    ivProfilePic.setVisibility(View.GONE);
                    tvProfileInitial.setVisibility(View.VISIBLE);
                    if (!name.isEmpty()) {
                        tvProfileInitial.setText(String.valueOf(name.charAt(0)).toUpperCase());
                    }
                }

                tvName.setText(name);
                tvPhone.setText(user.optString("phone", "-"));
                tvCity.setText(user.optString("city", "-"));
                tvBloodGroup.setText(user.optString("bloodGroup", "-"));

                String role = user.optString("role", "user");
                role = role.substring(0, 1).toUpperCase() + role.substring(1).toLowerCase();
                tvUserType.setText(role);

                tvBio.setText(user.optString("bio", "-"));
            } catch (Exception e) {
                tvName.setText("Error loading profile");
            }
        } else {
            tvName.setText("No user data found");
        }

        // Contact Requests button — donor ke liye
        Button btnContactRequests = findViewById(R.id.btnContactRequests);
        btnContactRequests.setOnClickListener(v -> startActivity(
            new Intent(ProfileActivity.this, ContactRequestsActivity.class)
        ));
    }

    private void openProfileImageViewer() {
        String userData = getSharedPreferences("UserData", MODE_PRIVATE).getString("user", null);
        if (userData != null) {
            try {
                JSONObject user = new JSONObject(userData);
                String profilePic = user.optString("profilePic", "");
                if (profilePic != null && !profilePic.isEmpty()) {
                    String baseUrlForImages = AppConfig.BASE_URL.replace("/api", "");
                    String imageUrl = baseUrlForImages + profilePic;
                    
                    Intent intent = new Intent(ProfileActivity.this, ImageViewerActivity.class);
                    intent.putExtra("IMAGE_URL", imageUrl);
                    intent.putExtra("TITLE", "My Profile Photo");
                    startActivity(intent);
                }
            } catch (Exception e) {
                android.util.Log.e("ProfileActivity", "Error opening image viewer", e);
            }
        }
    }

    private void openImagePicker() {
        // Show dialog with options: View Photo, Change Photo, Remove Photo
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setTitle("Profile Photo");
        
        ArrayList<String> options = new ArrayList<>();
        options.add("View Full Photo");
        options.add("Change Photo");
        
        // Only show "Remove Photo" if there's already a profile pic
        String userData = getSharedPreferences("UserData", MODE_PRIVATE).getString("user", null);
        boolean hasProfilePic = false;
        if (userData != null) {
            try {
                JSONObject user = new JSONObject(userData);
                String profilePic = user.optString("profilePic", "");
                hasProfilePic = profilePic != null && !profilePic.isEmpty();
            } catch (Exception e) {}
        }
        
        final boolean canRemovePhoto = hasProfilePic;
        
        if (hasProfilePic) {
            options.add("Remove Photo");
        }
        
        builder.setItems(options.toArray(new String[0]), (dialog, which) -> {
            if (which == 0) {
                // View Full Photo
                if (canRemovePhoto) {
                    openProfileImageViewer();
                } else {
                    android.widget.Toast.makeText(this, "No profile photo to view", android.widget.Toast.LENGTH_SHORT).show();
                }
            } else if (which == 1) {
                // Change Photo - Open gallery
                android.util.Log.d("ProfileActivity", "Opening image picker...");
                Intent intent = new Intent(Intent.ACTION_GET_CONTENT);
                intent.setType("image/*");
                intent.addCategory(Intent.CATEGORY_OPENABLE);
                imagePickerLauncher.launch(Intent.createChooser(intent, "Select Profile Photo"));
            } else if (which == 2 && canRemovePhoto) {
                // Remove Photo
                android.util.Log.d("ProfileActivity", "Removing profile photo...");
                removeProfilePic();
            }
        });
        
        builder.setNegativeButton("Cancel", null);
        builder.show();
    }

    private void startCrop(Uri imageUri) {
        try {
            // Create destination URI for cropped image
            String destinationFileName = "cropped_" + System.currentTimeMillis() + ".jpg";
            croppedImageUri = Uri.fromFile(new File(getCacheDir(), destinationFileName));
            
            UCrop.of(imageUri, croppedImageUri)
                    .withAspectRatio(1, 1) // Square crop for profile picture
                    .withMaxResultSize(800, 800) // Max size
                    .start(this);
        } catch (Exception e) {
            Toast.makeText(this, "Error: " + e.getMessage(), Toast.LENGTH_SHORT).show();
            e.printStackTrace();
        }
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (resultCode == RESULT_OK && requestCode == UCrop.REQUEST_CROP) {
            final Uri resultUri = UCrop.getOutput(data);
            if (resultUri != null) {
                uploadProfilePic(resultUri);
            }
        } else if (resultCode == UCrop.RESULT_ERROR) {
            final Throwable cropError = UCrop.getError(data);
            if (cropError != null) {
                Toast.makeText(this, "Crop error: " + cropError.getMessage(), Toast.LENGTH_SHORT).show();
            }
        }
    }

    private void uploadProfilePic(Uri uri) {
        try {
            android.util.Log.d("ProfileActivity", "Starting upload for URI: " + uri.toString());
            runOnUiThread(() -> Toast.makeText(this, "Uploading...", Toast.LENGTH_SHORT).show());

            // Create temporary file from Uri
            File tempFile = createTempFile(uri);
            if (tempFile == null) {
                android.util.Log.e("ProfileActivity", "Failed to create temp file");
                runOnUiThread(() -> Toast.makeText(this, "Failed to process image", Toast.LENGTH_SHORT).show());
                return;
            }

            android.util.Log.d("ProfileActivity", "Temp file created: " + tempFile.getAbsolutePath() + ", Size: " + tempFile.length());

            RequestBody requestBody = new MultipartBody.Builder()
                    .setType(MultipartBody.FORM)
                    .addFormDataPart("profilePic", tempFile.getName(),
                            RequestBody.create(tempFile, MediaType.parse("image/jpeg")))
                    .build();

            String fullUrl = AppConfig.BASE_URL + "/auth/profile-pic";
            android.util.Log.d("ProfileActivity", "Uploading to URL: " + fullUrl);

            Request request = new Request.Builder()
                    .url(fullUrl)
                    .addHeader("Authorization", "Bearer " + authToken)
                    .patch(requestBody)
                    .build();

            client.newCall(request).enqueue(new Callback() {
                @Override
                public void onFailure(Call call, IOException e) {
                    android.util.Log.e("ProfileActivity", "Upload failed", e);
                    runOnUiThread(() -> {
                        Toast.makeText(ProfileActivity.this, "Upload failed: " + e.getMessage(), Toast.LENGTH_LONG).show();
                        e.printStackTrace();
                    });
                }

                @Override
                public void onResponse(Call call, Response response) throws IOException {
                    try {
                        String responseBody = response.body() != null ? response.body().string() : "";
                        android.util.Log.d("ProfileActivity", "Response Code: " + response.code());
                        android.util.Log.d("ProfileActivity", "Response Body: " + responseBody);
                        
                        if (response.isSuccessful()) {
                            JSONObject json = new JSONObject(responseBody);
                            String newPicUrl = json.getJSONObject("data").getString("profilePic");
                            android.util.Log.d("ProfileActivity", "New profile pic URL: " + newPicUrl);

                            // Update local storage (cached user data)
                            updateCachedUserPic(newPicUrl);

                            runOnUiThread(() -> {
                                Toast.makeText(ProfileActivity.this, "✅ Profile picture updated!", Toast.LENGTH_SHORT).show();
                                // Reload image
                                ivProfilePic.setVisibility(View.VISIBLE);
                                tvProfileInitial.setVisibility(View.GONE);
                                // Remove /api from BASE_URL for image loading since uploads is served at root
                                String baseUrlForImages = AppConfig.BASE_URL.replace("/api", "");
                                String imageUrl = baseUrlForImages + newPicUrl;
                                android.util.Log.d("ProfileActivity", "Loading image from: " + imageUrl);
                                Glide.with(ProfileActivity.this)
                                        .load(imageUrl)
                                        .placeholder(R.drawable.user_circle_bg)
                                        .error(R.drawable.user_circle_bg)
                                        .circleCrop()
                                        .into(ivProfilePic);
                                
                                // Clear Glide cache to force reload
                                Glide.get(ProfileActivity.this).clearMemory();
                                new Thread(() -> Glide.get(ProfileActivity.this).clearDiskCache()).start();
                            });
                        } else {
                            android.util.Log.e("ProfileActivity", "Server error: " + response.code() + " - " + responseBody);
                            runOnUiThread(() -> {
                                Toast.makeText(ProfileActivity.this, "Server error: " + response.code() + " - " + responseBody, Toast.LENGTH_LONG).show();
                            });
                        }
                    } catch (Exception e) {
                        android.util.Log.e("ProfileActivity", "Error processing response", e);
                        runOnUiThread(() -> {
                            Toast.makeText(ProfileActivity.this, "Error: " + e.getMessage(), Toast.LENGTH_LONG).show();
                            e.printStackTrace();
                        });
                    }
                }
            });

        } catch (Exception e) {
            e.printStackTrace();
            runOnUiThread(() -> Toast.makeText(this, "Error: " + e.getMessage(), Toast.LENGTH_LONG).show());
        }
    }

    private void removeProfilePic() {
        try {
            runOnUiThread(() -> Toast.makeText(this, "Removing photo...", Toast.LENGTH_SHORT).show());

            RequestBody requestBody = new MultipartBody.Builder()
                    .setType(MultipartBody.FORM)
                    .build();

            Request request = new Request.Builder()
                    .url(AppConfig.BASE_URL + "/auth/profile-pic")
                    .addHeader("Authorization", "Bearer " + authToken)
                    .delete(requestBody)
                    .build();

            client.newCall(request).enqueue(new Callback() {
                @Override
                public void onFailure(Call call, IOException e) {
                    runOnUiThread(() -> {
                        Toast.makeText(ProfileActivity.this, "Failed to remove: " + e.getMessage(), Toast.LENGTH_LONG).show();
                    });
                }

                @Override
                public void onResponse(Call call, Response response) throws IOException {
                    try {
                        if (response.isSuccessful()) {
                            // Update local storage
                            updateCachedUserPic("");
                            
                            runOnUiThread(() -> {
                                Toast.makeText(ProfileActivity.this, "✅ Photo removed!", Toast.LENGTH_SHORT).show();
                                // Show initial instead of image
                                ivProfilePic.setVisibility(View.GONE);
                                tvProfileInitial.setVisibility(View.VISIBLE);
                                
                                // Update the initial
                                String userData = getSharedPreferences("UserData", MODE_PRIVATE).getString("user", null);
                                if (userData != null) {
                                    try {
                                        JSONObject user = new JSONObject(userData);
                                        String name = user.optString("name", "User");
                                        if (!name.isEmpty()) {
                                            tvProfileInitial.setText(String.valueOf(name.charAt(0)).toUpperCase());
                                        }
                                    } catch (Exception e) {}
                                }
                            });
                        } else {
                            runOnUiThread(() -> {
                                Toast.makeText(ProfileActivity.this, "Failed to remove photo", Toast.LENGTH_SHORT).show();
                            });
                        }
                    } catch (Exception e) {
                        runOnUiThread(() -> {
                            Toast.makeText(ProfileActivity.this, "Error: " + e.getMessage(), Toast.LENGTH_LONG).show();
                        });
                    }
                }
            });
        } catch (Exception e) {
            e.printStackTrace();
            runOnUiThread(() -> Toast.makeText(this, "Error: " + e.getMessage(), Toast.LENGTH_LONG).show());
        }
    }

    private File createTempFile(Uri uri) {
        try {
            InputStream inputStream = getContentResolver().openInputStream(uri);
            File file = new File(getCacheDir(), "temp_profile.jpg");
            FileOutputStream outputStream = new FileOutputStream(file);
            byte[] buffer = new byte[1024];
            int read;
            while ((read = inputStream.read(buffer)) != -1) {
                outputStream.write(buffer, 0, read);
            }
            outputStream.close();
            inputStream.close();
            return file;
        } catch (Exception e) {
            return null;
        }
    }

    private void updateCachedUserPic(String newUrl) {
        try {
            SharedPreferences prefs = getSharedPreferences("UserData", MODE_PRIVATE);
            String userData = prefs.getString("user", null);
            if (userData != null) {
                JSONObject user = new JSONObject(userData);
                user.put("profilePic", newUrl);
                prefs.edit().putString("user", user.toString()).apply();
            }
        } catch (Exception e) { e.printStackTrace(); }
    }

    @Override
    public boolean onSupportNavigateUp() {
        finish();
        return true;
    }
}

