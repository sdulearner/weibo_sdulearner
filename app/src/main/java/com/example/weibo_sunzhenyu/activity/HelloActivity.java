package com.example.weibo_sunzhenyu.activity;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.util.Log;

import com.example.weibo_sunzhenyu.R;
import com.example.weibo_sunzhenyu.fragment.HelloFragment;

public class HelloActivity extends AppCompatActivity {
    private static final String TAG = "HelloActivity";
    /**
     * 延时2000ms
     * Activity全屏主题
     */

    //闪屏业延时
    private static final int HANDLER_SPLASH = 1001;
    private final Handler handler = new Handler(new Handler.Callback() {
        @Override
        public boolean handleMessage(@NonNull Message msg) {
            if (isUserAgreed(HelloActivity.this)) {
                // 用户已经同意并使用，直接跳转到首页
                Log.i(TAG, "onCreate: 已同意并使用");
                startActivity(new Intent(HelloActivity.this, MainActivity.class));
                finish();
//                动画
//                overridePendingTransition(R.anim.slide_in_right, R.anim.slide_out_left);
            } else {
                // 启动Fragment
                HelloFragment helloFragment = new HelloFragment();
                helloFragment.show(getSupportFragmentManager(), "hello_dialog");
            }
            return true;
        }

    });

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_hello);
        // 启动页展示500ms
        handler.sendEmptyMessageDelayed(HANDLER_SPLASH, 500);

    }

    // 检查用户是否已经同意
    public static boolean isUserAgreed(Context context) {
        SharedPreferences preferences = context.getSharedPreferences("user_pref", Context.MODE_PRIVATE);
        return preferences.getBoolean("user_agreed", false);
    }
}