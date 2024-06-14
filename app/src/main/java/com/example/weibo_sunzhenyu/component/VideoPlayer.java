package com.example.weibo_sunzhenyu.component;

import android.content.Context;
import android.graphics.SurfaceTexture;
import android.media.MediaPlayer;
import android.net.Uri;
import android.os.Handler;
import android.util.AttributeSet;
import android.util.Log;
import android.view.Surface;
import android.view.TextureView;
import android.view.View;
import android.widget.FrameLayout;
import android.widget.ImageView;
import android.widget.MediaController;
import android.widget.ProgressBar;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;


import com.bumptech.glide.Glide;
import com.example.weibo_sunzhenyu.R;

import java.io.IOException;

public class VideoPlayer extends FrameLayout {
    private static final String TAG = "VideoPlayer";
    private TextureView textureView;
    private ImageView coverImage;
    private ProgressBar progressBar;
    private TextView timeTextView;
    private MediaPlayer mediaPlayer;
    private Handler handler = new Handler();

    private boolean isPlaying = false;
    private boolean isPrepared = false;

    public VideoPlayer(@NonNull Context context, String coverUri, Uri videoUri) {
        super(context);
        init(context, coverUri, videoUri);
    }

    private void init(Context context, String coverUri, Uri videoUri) {
        View.inflate(context, R.layout.view_video_player, this);

        // Set up MediaController for playback controls
        MediaController mediaController = new MediaController(context);
        mediaController.setAnchorView(this);
//        this.setMediaController(mediaController);
//        textureView = findViewById(R.id.textureView);
//        coverImage = findViewById(R.id.coverImage);
//        progressBar = findViewById(R.id.progressBar);
//        timeTextView = findViewById(R.id.timeTextView);
        setCoverUri(coverUri);
        setVideoUri(videoUri);
        textureView.setSurfaceTextureListener(new TextureView.SurfaceTextureListener() {
            @Override
            public void onSurfaceTextureAvailable(SurfaceTexture surface, int width, int height) {
                setupMediaPlayer(surface);
            }

            @Override
            public void onSurfaceTextureSizeChanged(SurfaceTexture surface, int width, int height) {
            }

            @Override
            public boolean onSurfaceTextureDestroyed(SurfaceTexture surface) {
                if (mediaPlayer != null) {
                    mediaPlayer.release();
                    mediaPlayer = null;
                }
                return true;
            }

            @Override
            public void onSurfaceTextureUpdated(SurfaceTexture surface) {
            }
        });

        coverImage.setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View v) {
                if (isPlaying) {
                    pauseVideo();
                    coverImage.setVisibility(View.VISIBLE);
                } else {
                    startVideo();
                    coverImage.setVisibility(View.INVISIBLE);
                }
            }
        });

        // Handler to update progress bar and time text view
        handler.post(updateProgressRunnable);
    }

    private void setupMediaPlayer(SurfaceTexture surface) {
        mediaPlayer = new MediaPlayer();
        mediaPlayer.setSurface(new Surface(surface));
        mediaPlayer.setLooping(true);
        mediaPlayer.setOnPreparedListener(new MediaPlayer.OnPreparedListener() {
            @Override
            public void onPrepared(MediaPlayer mp) {
                Log.i(TAG, "onPrepared: 视频准备完毕");
                isPrepared = true;
                progressBar.setMax(mediaPlayer.getDuration());
            }
        });
    }

    public void setCoverUri(String coverUri) {
        if (mediaPlayer == null) return;
        Glide.with(getContext()).load(coverUri).into(coverImage);
    }

    public void setVideoUri(Uri videoUri) {
        if (mediaPlayer == null) return;
        try {
            mediaPlayer.setDataSource(getContext(), videoUri);
            mediaPlayer.prepareAsync();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private void startVideo() {
        if (isPrepared) {
            coverImage.setVisibility(View.GONE);
            mediaPlayer.start();
            isPlaying = true;
        }
    }

    private void pauseVideo() {
        if (mediaPlayer != null && mediaPlayer.isPlaying()) {
            mediaPlayer.pause();
            coverImage.setVisibility(View.VISIBLE);
            isPlaying = false;
        }
    }

    private final Runnable updateProgressRunnable = new Runnable() {
        @Override
        public void run() {
            if (mediaPlayer != null && isPlaying) {
                int currentPosition = mediaPlayer.getCurrentPosition();
                progressBar.setProgress(currentPosition);

                int minutes = (currentPosition / 1000) / 60;
                int seconds = (currentPosition / 1000) % 60;
                int duration = mediaPlayer.getDuration();
                int durationMinutes = (duration / 1000) / 60;
                int durationSeconds = (duration / 1000) % 60;
                String timeText = String.format("%02d:%02d / %02d:%02d", minutes, seconds, durationMinutes, durationSeconds);
                timeTextView.setText(timeText);

                handler.postDelayed(this, 1000);
            }
        }
    };

    // Call this method in Activity's onPause or onStop to handle the video going out of screen
    public void pauseIfNotVisible() {
        if (isPlaying) {
            pauseVideo();
        }
    }
}
