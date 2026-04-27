package com.abhay.bloodradar;

import android.os.Bundle;
import android.view.View;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.TextView;
import androidx.appcompat.app.AppCompatActivity;
import com.bumptech.glide.Glide;
import com.bumptech.glide.load.engine.DiskCacheStrategy;
import com.bumptech.glide.request.RequestOptions;
import com.bumptech.glide.load.resource.bitmap.BitmapTransitionOptions;
import com.bumptech.glide.request.target.BitmapImageViewTarget;
import com.bumptech.glide.request.transition.Transition;

public class ImageViewerActivity extends AppCompatActivity {

    private com.github.chrisbanes.photoview.PhotoView photoView;
    private ProgressBar progressBar;
    private ImageView btnBack;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_image_viewer);

        // Initialize views
        photoView = findViewById(R.id.photoView);
        progressBar = findViewById(R.id.progressBar);
        btnBack = findViewById(R.id.btnBack);
        TextView tvTitle = findViewById(R.id.tvTitle);

        // Get image URL from intent
        String imageUrl = getIntent().getStringExtra("IMAGE_URL");
        String title = getIntent().getStringExtra("TITLE");

        if (title != null) {
            tvTitle.setText(title);
        }

        // Back button click
        btnBack.setOnClickListener(v -> finish());

        // Load image with Glide
        if (imageUrl != null && !imageUrl.isEmpty()) {
            Glide.with(this)
                    .asBitmap()
                    .load(imageUrl)
                    .apply(new RequestOptions()
                            .diskCacheStrategy(DiskCacheStrategy.ALL)
                            .placeholder(R.drawable.user_circle_bg)
                            .error(R.drawable.user_circle_bg))
                    .into(new BitmapImageViewTarget(photoView) {
                        @Override
                        public void onLoadStarted(android.graphics.drawable.Drawable placeholder) {
                            super.onLoadStarted(placeholder);
                            progressBar.setVisibility(View.VISIBLE);
                        }

                        @Override
                        public void onResourceReady(android.graphics.Bitmap resource, Transition<? super android.graphics.Bitmap> transition) {
                            super.onResourceReady(resource, transition);
                            progressBar.setVisibility(View.GONE);
                        }

                        @Override
                        public void onLoadFailed(android.graphics.drawable.Drawable errorDrawable) {
                            super.onLoadFailed(errorDrawable);
                            progressBar.setVisibility(View.GONE);
                        }
                    });
        } else {
            progressBar.setVisibility(View.GONE);
            finish();
        }
    }

    @Override
    public void onBackPressed() {
        super.onBackPressed();
        finish();
    }
}
