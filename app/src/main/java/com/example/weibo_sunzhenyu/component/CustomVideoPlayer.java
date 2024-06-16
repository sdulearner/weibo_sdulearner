package com.example.weibo_sunzhenyu.component;

import android.content.Context;
import android.media.MediaPlayer;
import android.net.Uri;
import android.util.AttributeSet;
import android.util.Log;
import android.view.SurfaceHolder;
import android.view.SurfaceView;
import android.view.View;
import android.widget.FrameLayout;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import com.bumptech.glide.Glide;
import com.bumptech.glide.load.engine.DiskCacheStrategy;
import com.example.weibo_sunzhenyu.R;

import java.io.IOException;
import java.util.Timer;
import java.util.TimerTask;

public class CustomVideoPlayer extends FrameLayout implements SurfaceHolder.Callback {
    // TODO: 2024/6/16 实现视频缓存
    // TODO: 2024/6/18 视频必须全部加载完才可播放，怎样解决这个问题？

    private static final String TAG = "CustomVideoPlayer";

    private ImageView videoCover;
    private SurfaceView surfaceView;
    private ProgressBar progressBar;
    private TextView timeText;

    private MediaPlayer mediaPlayer;
    private Timer timer;
    private TimerTask timerTask;

    private String videoUrl;
    // 默认视频的宽高
    private int videoWidth = 1920;
    private int videoHeight = 1082;

    public int getVideoWidth() {
        return videoWidth;
    }

    public int getVideoHeight() {
        return videoHeight;
    }

    public CustomVideoPlayer(@NonNull Context context) {
        super(context);
        init(context);
    }

    public CustomVideoPlayer(@NonNull Context context, @Nullable AttributeSet attrs) {
        super(context, attrs);
        init(context);
    }

    public CustomVideoPlayer(@NonNull Context context, @Nullable AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        init(context);
    }

    private void init(Context context) {
        inflate(context, R.layout.view_video_player, this);

        videoCover = findViewById(R.id.video_cover);
        surfaceView = findViewById(R.id.surface_view);
        progressBar = findViewById(R.id.progress_bar);
        timeText = findViewById(R.id.time_text);

        surfaceView.getHolder().addCallback(this);

        this.setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View v) {
                togglePlayPause();
            }
        });
    }

    public void setVideo(String coverUrl, String videoUrl) {
        this.videoUrl = videoUrl;
        Glide.with(getContext())
                .load(coverUrl)
                .diskCacheStrategy(DiskCacheStrategy.ALL)
                .into(videoCover);

        setupMediaPlayer();
    }

    private void setupMediaPlayer() {
        mediaPlayer = new MediaPlayer();
        mediaPlayer.setOnPreparedListener(new MediaPlayer.OnPreparedListener() {
            @Override
            public void onPrepared(MediaPlayer mp) {
                mediaPlayer = mp;
                // 获取视频宽高
                mediaPlayer.setOnVideoSizeChangedListener(new MediaPlayer.OnVideoSizeChangedListener() {
                    @Override
                    public void onVideoSizeChanged(MediaPlayer mp, int width, int height) {
                        // 设置视频宽高
                        videoWidth = width;
                        videoHeight = height;
                    }
                });
                mp.setLooping(true);
                updateProgress();
            }
        });

        mediaPlayer.setOnCompletionListener(new MediaPlayer.OnCompletionListener() {
            @Override
            public void onCompletion(MediaPlayer mp) {
                stopProgressUpdate();
                resetPlayer();
            }
        });

        try {
            mediaPlayer.setDataSource(getContext(), Uri.parse(videoUrl));
            mediaPlayer.prepareAsync();
        } catch (IOException e) {
            Log.e(TAG, "Error setting data source", e);
        }
    }

    private void togglePlayPause() {
        if (mediaPlayer.isPlaying()) {
            // TODO: 2024/6/17  暂停时显示视频图标
            videoCover.setImageResource(R.drawable.video);
            videoCover.setVisibility(View.VISIBLE);
            mediaPlayer.pause();
            stopProgressUpdate();
        } else {
            videoCover.setVisibility(View.GONE);
            surfaceView.setVisibility(View.VISIBLE);
            mediaPlayer.start();
            updateProgress();
        }
    }

    // TODO: 2024/6/17 更改ProgressBar样式
    private void updateProgress() {
        if (timer == null) {
            timer = new Timer();
            timerTask = new TimerTask() {
                @Override
                public void run() {
                    if (mediaPlayer != null && mediaPlayer.isPlaying()) {
                        int currentPosition = mediaPlayer.getCurrentPosition();
                        int duration = mediaPlayer.getDuration();
                        progressBar.setProgress(currentPosition * 100 / duration);
                        post(new Runnable() {
                            @Override
                            public void run() {
                                timeText.setText(formatTime(currentPosition) + "/" + formatTime(duration));
                            }
                        });
                    }
                }
            };
            timer.schedule(timerTask, 0, 1000);
        }
    }

    private void stopProgressUpdate() {
        if (timer != null) {
            timer.cancel();
            timer = null;
        }
        if (timerTask != null) {
            timerTask.cancel();
            timerTask = null;
        }
    }

    private void resetPlayer() {
        videoCover.setVisibility(View.VISIBLE);
        surfaceView.setVisibility(View.GONE);
        progressBar.setProgress(0);
        timeText.setText("00:00/00:00");// TODO: 2024/6/17 设置初始时间
    }

    private String formatTime(int millis) {
        int seconds = millis / 1000;
        int minutes = seconds / 60;
        seconds = seconds % 60;
        return String.format("%02d:%02d", minutes, seconds);
    }

    @Override
    public void surfaceCreated(SurfaceHolder holder) {
        mediaPlayer.setDisplay(holder);
    }

    @Override
    public void surfaceChanged(SurfaceHolder holder, int format, int width, int height) {
    }

    @Override
    public void surfaceDestroyed(SurfaceHolder holder) {
        if (mediaPlayer != null) {
            if (mediaPlayer.isPlaying()) {
                mediaPlayer.pause();
                stopProgressUpdate();
            }
            // TODO: 2024/6/17 在刷新时释放资源
//            mediaPlayer.release();
//            mediaPlayer = null;
        }
    }
}
