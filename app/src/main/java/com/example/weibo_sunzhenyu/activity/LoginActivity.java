package com.example.weibo_sunzhenyu.activity;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.os.Message;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import com.example.weibo_sunzhenyu.R;
import com.example.weibo_sunzhenyu.entity.CommonData;
import com.example.weibo_sunzhenyu.entity.UserInfoItem;
import com.example.weibo_sunzhenyu.fragment.MyPageFragment;
import com.google.gson.Gson;

import okhttp3.MediaType;
import okhttp3.OkHttpClient;
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
    private MyPageFragment.ApiService apiService = retrofit.create(MyPageFragment.ApiService.class);

    public interface ApiService {
        // 根据token确定登录状态
        @GET("/weibo/api/user/info")
        retrofit2.Call<CommonData<UserInfoItem>> queryLogin(@Header("Authorization") String authorizationHeader);

        @POST("/weibo/api/auth/sendCode")
        retrofit2.Call<Void> sendSmsCode(@Body SendCodeRequestBody requestBody);
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
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_login);
        EditText input_phone = findViewById(R.id.input_phone);

        EditText input_smsCode = findViewById(R.id.input_smsCode);

        TextView count_down = findViewById(R.id.count_down);
        count_down.setEnabled(false);
        count_down.setTextColor(getColor(R.color.gray1));

        Button btn_login = findViewById(R.id.btn_login);


        input_phone.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {
            }

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
                // 判断未尾输入
                count_down.setEnabled(false);
                count_down.setTextColor(getColor(R.color.gray1));
                if (s.length() > 11) {
                    s = s.subSequence(0, s.length() - 1);
                    input_phone.setText(s);//设置新内容
                    input_phone.setSelection(s.length());//设置光标
                }
                if (s.length() == 11) {
                    // 设置获取验证码样式
                    count_down.setEnabled(true);
                    count_down.setTextColor(getColor(R.color.link));
                }
            }

            @Override
            public void afterTextChanged(Editable s) {
            }
        });
        count_down.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Toast.makeText(LoginActivity.this, "获取验证码", Toast.LENGTH_SHORT).show();

            }
        });


        // TODO: 2024/6/13 登录界面左上角toolbar添加返回文字按钮
    }
}