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
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;

import com.bumptech.glide.Glide;
import com.bumptech.glide.load.engine.DiskCacheStrategy;
import com.example.weibo_sunzhenyu.R;
import com.example.weibo_sunzhenyu.activity.LoginActivity;
import com.example.weibo_sunzhenyu.entity.CommonData;
import com.example.weibo_sunzhenyu.entity.LoginEvent;
import com.example.weibo_sunzhenyu.entity.LogoutEvent;
import com.example.weibo_sunzhenyu.entity.UserInfoItem;
import com.example.weibo_sunzhenyu.utils.Utils;
import com.google.gson.Gson;

import org.greenrobot.eventbus.EventBus;
import org.greenrobot.eventbus.Subscribe;
import org.greenrobot.eventbus.ThreadMode;

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
    private boolean login = false;
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

    private ImageView my_page_avatar;
    private TextView text_username;
    private TextView text_loginStatus;
    private TextView logout;
    // 检查用户是否已经登录

    private boolean isUserLogin(Context context) {
        // 登录状态
        String authorizationHeader = Utils.getBearerToken(requireActivity());
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
                            if (body.getData() == null && body.getCode() == 403) {
                                // token过期，未登录状态
                                Utils.clearToken(requireActivity());
                                Log.e(TAG, "token过期");
                                Intent intent = new Intent(requireActivity(), LoginActivity.class);
                                startActivity(intent);
                            } else {
                                // 请求成功将用户赋给变量
                                user = body.getData();
                                // 用户名不为空表示登录状态
                                if (user.getUsername() != null) {
//                                Log.i(TAG, user.getUsername());
                                    // 登录则加载用户信息
                                    Log.i(TAG, "onCreateView: " + my_page_avatar.getContext());
                                    Glide.with(my_page_avatar.getContext())
                                            .load(user.getAvatar())
                                            .diskCacheStrategy(DiskCacheStrategy.ALL)
                                            .into(my_page_avatar);
                                    text_username.setText(user.getUsername());
                                    // TODO: 2024/6/17 用户名下面的粉丝数暂时用phone代替
                                    text_loginStatus.setText(user.getPhone());
                                    // 并显示退出登录按钮
                                    logout.setVisibility(View.VISIBLE);
                                    login = true;
                                } else {// 用户名为空表示未登录状态
                                    Log.e(TAG, "用户未登录");
                                }
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
        return login;
    }

    // 注册和取消注册EventBus
    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        isUserLogin(requireActivity());
        EventBus.getDefault().register(this);
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        EventBus.getDefault().unregister(this);
    }

    // 登录成功时用EventBus传递消息实现更新
    @Subscribe(threadMode = ThreadMode.MAIN)
    public void onMsgEvent(LoginEvent loginEvent) {
        Log.e(TAG, "onMsgEvent: " + loginEvent.isLogin());
        if (loginEvent.isLogin()) isUserLogin(requireActivity());
    }

    private void logout() {
        Utils.clearToken(requireActivity());
        login = false;
        Toast.makeText(requireActivity(), "退出登录", Toast.LENGTH_SHORT).show();
        // 展示未登录信息
        Glide.with(my_page_avatar.getContext())
                .load(R.drawable.weibo)
                .diskCacheStrategy(DiskCacheStrategy.ALL)
                .into(my_page_avatar);
        text_username.setText("请先登录");
        text_loginStatus.setText("点击头像登录");
        // 并隐藏退出登录按钮
        logout.setVisibility(View.INVISIBLE);
        // 退出登录后刷新首页
        EventBus.getDefault().post(new LogoutEvent(false));
    }

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup
            container, @Nullable Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        View view = inflater.inflate(R.layout.my_page_fragment, container, false);
        my_page_avatar = view.findViewById(R.id.my_page_avatar);
        text_username = view.findViewById(R.id.text_username);
        text_loginStatus = view.findViewById(R.id.text_loginStatus);
        logout = view.findViewById(R.id.logout);

        logout.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                logout();
            }
        });

        text_username.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                isUserLogin(requireActivity());
            }
        });
        text_loginStatus.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

            }
        });
        my_page_avatar.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(requireActivity(), LoginActivity.class);
                startActivity(intent);
            }
        });
//        isUserLogin(requireActivity());
        if (login) {
        } else {
            // 否则展示未登录信息
            Glide.with(my_page_avatar.getContext())
                    .load(R.drawable.weibo)
                    .diskCacheStrategy(DiskCacheStrategy.ALL)
                    .into(my_page_avatar);
            text_username.setText("请先登录");
            text_loginStatus.setText("点击头像去登录");
            // 并隐藏退出登录按钮
            logout.setVisibility(View.INVISIBLE);
        }
        return view;
    }
}