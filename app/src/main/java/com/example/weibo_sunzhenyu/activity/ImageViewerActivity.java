package com.example.weibo_sunzhenyu.activity;

import android.animation.Animator;
import android.animation.AnimatorListenerAdapter;
import android.animation.AnimatorSet;
import android.animation.ArgbEvaluator;
import android.animation.ObjectAnimator;
import android.content.Context;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Color;
import android.graphics.drawable.ColorDrawable;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.Environment;
import android.transition.Fade;
import android.transition.Transition;
import android.transition.TransitionManager;
import android.view.View;
import android.view.Window;
import android.view.WindowManager;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.constraintlayout.widget.ConstraintLayout;
import androidx.viewpager2.widget.ViewPager2;

import com.bumptech.glide.Glide;
import com.example.weibo_sunzhenyu.R;
import com.example.weibo_sunzhenyu.adapter.ImageViewerAdapter;

import java.io.File;
import java.io.FileOutputStream;
import java.io.InputStream;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.List;

public class ImageViewerActivity extends AppCompatActivity {

    private List<String> imageUrls;
    private int currentIndex;
    private TextView textViewPageIndicator;
    private ViewPager2 viewPager;
    private ImageView userAvatar;
    private TextView userName;
    private ConstraintLayout rootLayout;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_image_viewer);
        // 设置Activity背景为透明色
        getWindow().setBackgroundDrawable(new ColorDrawable(Color.TRANSPARENT));
        // 设置状态栏的颜色
        Window window = getWindow();
        window.addFlags(WindowManager.LayoutParams.FLAG_DRAWS_SYSTEM_BAR_BACKGROUNDS);
        window.setStatusBarColor(getColor(R.color.black));

        imageUrls = getIntent().getStringArrayListExtra("imageUrls");
        currentIndex = getIntent().getIntExtra("currentIndex", 0);
        String userAvatarUrl = getIntent().getStringExtra("userAvatarUrl");
        String userNameStr = getIntent().getStringExtra("userName");

        textViewPageIndicator = findViewById(R.id.textViewPageIndicator);
        viewPager = findViewById(R.id.viewPager);
        userAvatar = findViewById(R.id.userAvatar);
        userName = findViewById(R.id.userName);
        rootLayout = findViewById(R.id.rootLayout);

        ImageViewerAdapter adapter = new ImageViewerAdapter(imageUrls);
        adapter.setOnClickListener(v -> closeWithAnimation()); // 设置点击事件监听器
        viewPager.setAdapter(adapter);
        viewPager.setCurrentItem(currentIndex, false); // 取消滑动效果

        textViewPageIndicator.setText((currentIndex + 1) + "/" + imageUrls.size());
        userName.setText(userNameStr);
        Glide.with(this).load(userAvatarUrl).circleCrop().into(userAvatar);

        viewPager.registerOnPageChangeCallback(new ViewPager2.OnPageChangeCallback() {
            @Override
            public void onPageSelected(int position) {
                super.onPageSelected(position);
                textViewPageIndicator.setText((position + 1) + "/" + imageUrls.size());
            }
        });

        findViewById(R.id.buttonDownload).setOnClickListener(v -> downloadImage(imageUrls.get(viewPager.getCurrentItem())));

        // 设置打开动画
        rootLayout.post(this::startOpenAnimation);
    }

    private void downloadImage(String url) {
        // 启动异步任务下载图片
        new DownloadImageTask(this).execute(url);
    }
    // 异步任务类，用于在后台下载图片
    private static class DownloadImageTask extends AsyncTask<String, Void, String> {
        private Context context;

        public DownloadImageTask(Context context) {
            this.context = context;
        }

        @Override
        protected String doInBackground(String... urls) {
            String url = urls[0];
            Bitmap bitmap = null;
            try {
                // 从URL下载图片
                URL imageUrl = new URL(url);
                HttpURLConnection connection = (HttpURLConnection) imageUrl.openConnection();
                connection.connect();
                InputStream input = connection.getInputStream();
                bitmap = BitmapFactory.decodeStream(input);
            } catch (Exception e) {
                e.printStackTrace();
            }
            if (bitmap != null) {
                // 保存图片到图库
                return saveImageToGallery(context, bitmap);
            } else {
                return null;
            }
        }

        @Override
        protected void onPostExecute(String result) {
            if (result != null) {
                Toast.makeText(context, "图片下载完成，请相册查看", Toast.LENGTH_SHORT).show();
            } else {
                Toast.makeText(context, "图片下载失败", Toast.LENGTH_SHORT).show();
            }
        }

        private String saveImageToGallery(Context context, Bitmap bitmap) {
            // 定义保存路径 DCIM/weibo_sunzhenyu
            String storePath = Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DCIM).getAbsolutePath() + File.separator + "weibo_sunzhenyu";
            File appDir = new File(storePath);
            // 如果目录不存在则创建
            if (!appDir.exists()) {
                appDir.mkdirs();
            }

            String fileName = System.currentTimeMillis() + ".jpg";// 定义文件名
            File file = new File(appDir, fileName);
            try {
                // 保存图片到文件
                FileOutputStream fos = new FileOutputStream(file);
                bitmap.compress(Bitmap.CompressFormat.JPEG, 100, fos);
                fos.flush();
                fos.close();
            } catch (Exception e) {
                e.printStackTrace();
                return null;
            }
            // 通知图库更新
            context.sendBroadcast(new Intent(Intent.ACTION_MEDIA_SCANNER_SCAN_FILE, Uri.fromFile(file)));
            return file.getAbsolutePath();
        }
    }

    private void startOpenAnimation() {
        Transition fade = new Fade();
        fade.setDuration(500);
        TransitionManager.beginDelayedTransition(rootLayout, fade);
        rootLayout.setVisibility(View.VISIBLE);
    }

    private void closeWithAnimation() {
        int startColor = Color.BLACK;
        int endColor = Color.TRANSPARENT;

        ObjectAnimator scaleX = ObjectAnimator.ofFloat(rootLayout, "scaleX", 1f, 0f);
        ObjectAnimator scaleY = ObjectAnimator.ofFloat(rootLayout, "scaleY", 1f, 0f);
        ObjectAnimator alpha = ObjectAnimator.ofFloat(rootLayout, "alpha", 1f, 0f);
        ObjectAnimator colorFade = ObjectAnimator.ofObject(rootLayout, "backgroundColor", new ArgbEvaluator(), startColor, endColor);

        scaleX.setDuration(150);
        scaleY.setDuration(150);

        // 使用时间插值器使透明度变为透明的速度一开始很快，动画的后面变慢
        alpha.setDuration(150);
        alpha.setInterpolator(new android.view.animation.AccelerateDecelerateInterpolator());
        colorFade.setDuration(150);

        AnimatorSet animatorSet = new AnimatorSet();
        animatorSet.playTogether(scaleX, scaleY, alpha, colorFade);
        animatorSet.addListener(new AnimatorListenerAdapter() {
            @Override
            public void onAnimationEnd(Animator animation) {
                finish();
                overridePendingTransition(0, 0); // 清除默认的Activity转场动画
            }
        });
        animatorSet.start();
    }

    @Override
    public void finish() {
        super.finish();
        overridePendingTransition(0, 0); // 清除默认的Activity转场动画
    }
}
