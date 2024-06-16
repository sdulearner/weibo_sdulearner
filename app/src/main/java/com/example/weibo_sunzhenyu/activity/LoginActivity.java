package com.example.weibo_sunzhenyu.activity;

import static androidx.core.view.ViewCompat.setBackgroundTintList;

import android.content.Context;
import android.content.SharedPreferences;
import android.content.res.ColorStateList;
import android.graphics.PorterDuff;
import android.graphics.PorterDuffColorFilter;
import android.graphics.drawable.ColorDrawable;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.os.Message;
import android.text.Editable;
import android.text.TextWatcher;
import android.util.Log;
import android.view.View;
import android.view.Window;
import android.view.WindowManager;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.content.ContextCompat;

import com.example.weibo_sunzhenyu.R;
import com.example.weibo_sunzhenyu.component.CountDownButton;
import com.example.weibo_sunzhenyu.entity.CommonData;
import com.example.weibo_sunzhenyu.entity.LoginEvent;
import com.example.weibo_sunzhenyu.entity.UserInfoItem;
import com.example.weibo_sunzhenyu.utils.Utils;
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;

import org.greenrobot.eventbus.EventBus;

import java.util.HashMap;
import java.util.Map;

import okhttp3.MediaType;
import okhttp3.OkHttpClient;
import retrofit2.Call;
import retrofit2.Response;
import retrofit2.Retrofit;
import retrofit2.converter.gson.GsonConverterFactory;
import retrofit2.http.Body;
import retrofit2.http.GET;
import retrofit2.http.Header;
import retrofit2.http.POST;

public class LoginActivity extends AppCompatActivity {
    private static final String TAG = "NetworkActivity";
    private UserInfoItem user = new UserInfoItem();
    private final OkHttpClient okHttpClient = new OkHttpClient.Builder().build();
    private static final MediaType jsonType = MediaType.parse("application/json;charset=utf-8");
    private final Gson gson = new Gson();
    private final Handler handler = new Handler(Looper.getMainLooper()) {
        @Override
        public void handleMessage(@NonNull Message msg) {
            super.handleMessage(msg);
//            if (msg.what == 1) textBody.setText((CharSequence) msg.obj);
        }
    };
    private final Retrofit retrofit = new Retrofit.Builder()
            .baseUrl("https://hotfix-service-prod.g.mi.com")
            .client(okHttpClient)
            .addConverterFactory(GsonConverterFactory.create(gson))
            .build();
    private ApiService apiService = retrofit.create(ApiService.class);

    public interface ApiService {
        // 根据token确定登录状态
        @GET("/weibo/api/user/info")
        retrofit2.Call<CommonData<UserInfoItem>> queryLogin(@Header("Authorization") String authorizationHeader);

        @POST("/weibo/api/auth/sendCode")
        retrofit2.Call<CommonData> sendSmsCode(@Body Map<String, String> requestBody);

        @POST("/weibo/api/auth/login")
        retrofit2.Call<CommonData> login(@Body Map<String, String> requestBody);
    }

    private static class SendCodeRequestBody {
        private String phone;

        public SendCodeRequestBody(String phone) {
            this.phone = phone;
        }

        public String getPhone() {
            return phone;
        }


        public void setPhone(String phone) {
            this.phone = phone;
        }
    }


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        // TODO: 2024/6/15 登录界面左上角toolbar添加返回文字按钮
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_login);
        // 设置状态栏的颜色
        Window window = getWindow();
        window.addFlags(WindowManager.LayoutParams.FLAG_DRAWS_SYSTEM_BAR_BACKGROUNDS);
        window.setStatusBarColor(getColor(R.color.white));
        // 设置Activity背景为很浅的灰色
        getWindow().setBackgroundDrawable(new ColorDrawable(getResources().getColor(R.color.very_light_gray)));

        EditText input_phone = findViewById(R.id.input_phone);
        final boolean[] sendCodeEnabled = {false};
        boolean loginEnabled = false;

        EditText input_smsCode = findViewById(R.id.input_smsCode);

        CountDownButton count_down = findViewById(R.id.count_down);
        // 设置倒计时文本
        count_down.setCountDownText("获取验证码", "获取验证码");


        count_down.setEnabled(false);
        count_down.setTextColor(getColor(R.color.gray1));

        Button btn_login = findViewById(R.id.btn_login);

        int sky_blue = ContextCompat.getColor(this, R.color.sky_blue);
        int link = ContextCompat.getColor(this, R.color.link);
        ColorStateList colorStateList = ColorStateList.valueOf(sky_blue);
        btn_login.setBackgroundTintList(colorStateList);
        btn_login.setEnabled(false);

        input_phone.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {
            }

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
                // 判断未尾输入
                if (s.length() > 11) {
                    s = s.subSequence(0, s.length() - 1);
                    input_phone.setText(s);//设置新内容
                    input_phone.setSelection(s.length());//设置光标
                } else if (s.length() == 11) {
                    // 设置获取验证码样式
                    // 需要设置一个bool变量，当倒计时的时候不能发送验证码
                    if (!count_down.isCountingDown()) {
                        count_down.setEnabled(true);
                        count_down.setTextColor(getColor(R.color.link));
                        sendCodeEnabled[0] = true;// 能够发送验证码
                    }
                    // 如果验证码同时为6位则能够登录
                    if (String.valueOf(input_smsCode.getText()).length() == 6) {
                        btn_login.setEnabled(true);
                        ColorStateList colorStateList = ColorStateList.valueOf(link);
                        btn_login.setBackgroundTintList(colorStateList);
                    }
                } else {
                    count_down.setEnabled(false);
                    count_down.setTextColor(getColor(R.color.gray1));
                    ColorStateList colorStateList = ColorStateList.valueOf(sky_blue);
                    btn_login.setBackgroundTintList(colorStateList);
                    btn_login.setEnabled(false);
                }
            }

            @Override
            public void afterTextChanged(Editable s) {
            }
        });

        input_smsCode.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {
            }

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
                // 判断验证码输入
                btn_login.setEnabled(false);
                ColorStateList colorStateList = ColorStateList.valueOf(sky_blue);
                btn_login.setBackgroundTintList(colorStateList);
                if (s.length() > 6) {
                    s = s.subSequence(0, s.length() - 1);
                    input_smsCode.setText(s);//设置新内容
                    input_smsCode.setSelection(s.length());//设置光标
                }
                if (s.length() == 6 && sendCodeEnabled[0]) {
                    // 设置登录按钮样式
                    btn_login.setEnabled(true);
                    colorStateList = ColorStateList.valueOf(link);
                    btn_login.setBackgroundTintList(colorStateList);
                }
            }

            @Override
            public void afterTextChanged(Editable s) {
            }
        });
        count_down.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
//                Toast.makeText(LoginActivity.this, "获取验证码", Toast.LENGTH_SHORT).show();
                // TODO: 2024/6/16 点击获取验证码将光标移动到输入验证码的Edittext
                // 使用Gson直接构造请求体（虽然这里没直接用到，但确保Gson存在以解释如何构造复杂对象）
                Gson gson = new GsonBuilder().create();
                Map<String, String> requestBody = new HashMap<>();
                requestBody.put("phone", String.valueOf(input_phone.getText()));
                retrofit2.Call<CommonData> call = apiService.sendSmsCode(requestBody);
                call.enqueue(new retrofit2.Callback<CommonData>() {
                    @Override
                    public void onResponse(Call<CommonData> call, Response<CommonData> response) {
                        CommonData body = response.body();
                        if ((Boolean) body.getData())
                            Toast.makeText(LoginActivity.this, "发送成功", Toast.LENGTH_SHORT).show();
                        else
                            Toast.makeText(LoginActivity.this, "发送失败", Toast.LENGTH_SHORT).show();
                        count_down.startCountDown(); // 开始倒计时
                        count_down.setEnabled(false);
                        count_down.setTextColor(getColor(R.color.gray1));
                    }

                    @Override
                    public void onFailure(Call<CommonData> call, Throwable t) {
                        Log.e(TAG, "sendSmsCode onFailure.");
                        Toast.makeText(LoginActivity.this, "Network Error:sendSmsCode onFailure.", Toast.LENGTH_SHORT).show();
                    }
                });
            }
        });

        btn_login.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Gson gson = new GsonBuilder().create();
                Map<String, String> requestBody = new HashMap<>();
                requestBody.put("phone", String.valueOf(input_phone.getText()));
                requestBody.put("smsCode", String.valueOf(input_smsCode.getText()));
                retrofit2.Call<CommonData> call = apiService.login(requestBody);

                call.enqueue(new retrofit2.Callback<CommonData>() {
                    @Override
                    public void onResponse(Call<CommonData> call, Response<CommonData> response) {
                        CommonData<UserInfoItem> body = response.body();
                        if (body != null) {
                            if (body.getData() != null) {
                                Toast.makeText(LoginActivity.this, "登录成功", Toast.LENGTH_SHORT).show();
                                // 请求成功将token保存
                                String token = String.valueOf(body.getData());
                                Utils.saveUserLoginToken(LoginActivity.this, token);
                                // 登录成功后调用EventBus修改上一页信息并返回上一页
                                EventBus.getDefault().post(new LoginEvent(true));
                                onBackPressed();
                            } else
                                Toast.makeText(LoginActivity.this, "登录失败，" + body.getMsg(), Toast.LENGTH_SHORT).show();
                        }
                    }

                    @Override
                    public void onFailure(Call<CommonData> call, Throwable t) {
                        Log.e(TAG, "login onFailure.");
                        Toast.makeText(LoginActivity.this, "Network Error:login onFailure.", Toast.LENGTH_SHORT).show();
                    }
                });
            }
        });
    }


}