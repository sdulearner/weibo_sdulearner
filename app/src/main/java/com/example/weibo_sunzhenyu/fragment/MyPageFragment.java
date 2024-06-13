package com.example.weibo_sunzhenyu.fragment;

import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.os.Message;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;

import com.bumptech.glide.Glide;
import com.example.weibo_sunzhenyu.R;
import com.example.weibo_sunzhenyu.activity.LoginActivity;
import com.example.weibo_sunzhenyu.entity.CommonData;
import com.example.weibo_sunzhenyu.entity.UserInfoItem;
import com.google.gson.Gson;

import okhttp3.MediaType;
import okhttp3.OkHttpClient;
import retrofit2.Call;
import retrofit2.Response;
import retrofit2.Retrofit;
import retrofit2.converter.gson.GsonConverterFactory;
import retrofit2.http.GET;
import retrofit2.http.Header;

public class MyPageFragment extends Fragment {
    private static final String TAG = "MyPageFragment";
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
    }

    // 检查用户是否已经登录
    private boolean isUserLogin(Context context) {
        // 登录状态
        final boolean[] login = {false};
        SharedPreferences preferences = context.getSharedPreferences("user_pref", Context.MODE_PRIVATE);
        String token = preferences.getString("login_token", "");
        String authorizationHeader = "Bearer " + token;
        retrofit2.Call<CommonData<UserInfoItem>> isUserLoginCall = apiService.queryLogin(authorizationHeader);
        isUserLoginCall.enqueue(new retrofit2.Callback<CommonData<UserInfoItem>>() {
            @Override
            public void onResponse(Call<CommonData<UserInfoItem>> call, Response<CommonData<UserInfoItem>> response) {
                CommonData<UserInfoItem> body = response.body();
                // 使用Handler可以实现异步更新View
                handler.post(new Runnable() {
                    @Override
                    public void run() {
                        if (body != null) {
                            // 请求成功将用户赋给变量
                            user = body.getData();
                            // 用户名不为空表示登录状态
                            if (user.getUsername() != null) {
                                login[0] = true;
                            } else {// 用户名为空表示未登录状态
                                Log.e(TAG, "用户未登录");
                            }
                        }
                    }
                });
            }

            @Override
            public void onFailure(Call<CommonData<UserInfoItem>> call, Throwable t) {
                Log.e(TAG, "queryLogin onFailure.");
            }
        });
        return login[0];
    }

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup
            container, @Nullable Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        View view = inflater.inflate(R.layout.my_page_fragment, container, false);
        ImageView my_page_avatar = view.findViewById(R.id.my_page_avatar);
        TextView text_username = view.findViewById(R.id.text_username);
        TextView text_loginStatus = view.findViewById(R.id.text_loginStatus);
        my_page_avatar.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(requireActivity(), LoginActivity.class);
                startActivity(intent);
            }
        });

        if (isUserLogin(requireActivity())) {
            // 登录则加载用户信息
            Glide.with(my_page_avatar.getContext())
                    .load(user.getAvatar())
                    .into(my_page_avatar);
            text_username.setText(user.getUsername());
            // TODO: 2024/6/13 用户名下面的粉丝数暂时用phone代替
            text_loginStatus.setText(user.getPhone());
            // TODO: 2024/6/13 并显示退出登录按钮
        } else {
            // 否则展示未登录信息
            Glide.with(my_page_avatar.getContext())
                    .load(R.drawable.weibo)
                    .into(my_page_avatar);
            text_username.setText("请先登录");
            text_loginStatus.setText("点击头像去登录");
            // TODO: 2024/6/13 并隐藏退出登录按钮
        }
        return view;
    }

    // 使用SharedPreferences保存用户登录状态
    private void saveUserLoginToken(String token) {
        SharedPreferences preferences = requireActivity().getSharedPreferences("user_pref", Context.MODE_PRIVATE);
        SharedPreferences.Editor editor = preferences.edit();
        editor.putString("login_token", token);
        editor.apply();
    }
}