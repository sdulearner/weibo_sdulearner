package com.example.weibo_sunzhenyu.component;

import android.content.Context;
import android.os.Handler;
import android.util.AttributeSet;
import android.widget.TextView;

import androidx.appcompat.widget.AppCompatButton;

import com.example.weibo_sunzhenyu.R;

public class CountDownButton extends androidx.appcompat.widget.AppCompatTextView {
    /**
     * 实现发送验证码60秒倒计时
     */
    private long timeDuration = 60;
    private String textBeforeCountdown, textAfterCountdown;
    private boolean isCountingDown = false;

    public boolean isCountingDown() {
        return isCountingDown;
    }

    private Handler handler = new Handler();
    private Runnable runnable = new Runnable() {
        @Override
        public void run() {
            if (timeDuration > 0) {
                setText("获取验证码" + "(" + timeDuration + ")");
                timeDuration--;
                handler.postDelayed(this, 1000);
            } else {
                setText(textAfterCountdown);
                isCountingDown = false;
                // todo: 2024/6/17 如果此时电话为11位则改颜色为蓝色并且可点击
//                setTextColor(getResources().getColor(R.color.link));
//                setEnabled(true);
            }
        }
    };

    public CountDownButton(Context context) {
        super(context);
    }

    public CountDownButton(Context context, AttributeSet attrs) {
        super(context, attrs);
    }

    public CountDownButton(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
    }

    public void setCountDownText(String textBeforeCountdown, String textAfterCountdown) {
        this.textBeforeCountdown = textBeforeCountdown;
        this.textAfterCountdown = textAfterCountdown;
    }

    public void startCountDown() {
        if (!isCountingDown) {
            isCountingDown = true;
            setText(textBeforeCountdown);
            handler.removeCallbacks(runnable);
            handler.post(runnable);
        }
    }
}