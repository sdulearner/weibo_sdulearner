package com.example.weibo_sunzhenyu.fragment;

import android.annotation.SuppressLint;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.os.Message;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.LinearLayout;
import android.widget.SearchView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.RecyclerView;
import androidx.recyclerview.widget.StaggeredGridLayoutManager;
import androidx.swiperefreshlayout.widget.SwipeRefreshLayout;

import com.bumptech.glide.Glide;
import com.chad.library.adapter.base.listener.OnLoadMoreListener;
import com.example.weibo_sunzhenyu.R;
import com.example.weibo_sunzhenyu.adapter.HomeFragmentAdapter;
import com.example.weibo_sunzhenyu.entity.CommonData;
import com.example.weibo_sunzhenyu.entity.DataItem;
import com.example.weibo_sunzhenyu.entity.LoginEvent;
import com.example.weibo_sunzhenyu.entity.UserInfoItem;
import com.example.weibo_sunzhenyu.entity.WeiboInfoItem;
import com.example.weibo_sunzhenyu.utils.Utils;
import com.google.gson.Gson;

import org.greenrobot.eventbus.EventBus;
import org.greenrobot.eventbus.Subscribe;
import org.greenrobot.eventbus.ThreadMode;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.concurrent.TimeUnit;

import okhttp3.MediaType;
import okhttp3.OkHttpClient;
import retrofit2.Call;
import retrofit2.Response;
import retrofit2.Retrofit;
import retrofit2.converter.gson.GsonConverterFactory;
import retrofit2.http.GET;
import retrofit2.http.Header;
import retrofit2.http.Headers;
import retrofit2.http.Path;
import retrofit2.http.Query;

public class HomeFragment extends Fragment {
    private final OkHttpClient okHttpClient = new OkHttpClient.Builder()
            .connectTimeout(10, TimeUnit.SECONDS) // 设置连接超时时间
            .readTimeout(10, TimeUnit.SECONDS) // 设置读取超时时间
            .writeTimeout(10, TimeUnit.SECONDS) // 设置写入超时时间
            .build();
    private final Gson gson = new Gson();
    private final Retrofit retrofit = new Retrofit.Builder()
            .baseUrl("https://hotfix-service-prod.g.mi.com")
            .client(okHttpClient)
            .addConverterFactory(GsonConverterFactory.create(gson))
            .build();
    private final Handler handler = new Handler(Looper.getMainLooper()) {
        @Override
        public void handleMessage(@NonNull Message msg) {
            super.handleMessage(msg);
        }
    };

    public interface ApiService {
        // 根据token确定登录状态
        @GET("/weibo/api/user/info")
        retrofit2.Call<CommonData<UserInfoItem>> queryLogin(@Header("Authorization") String authorizationHeader);

        // 查找微博
        @GET("/weibo/homePage")
        retrofit2.Call<CommonData<DataItem>> queryWeibo(
                @Header("Authorization") String authorizationHeader,
                @Query("current") int current, // 当前页，默认1
                @Query("size") int size // 当前页大小，默认10
        );
    }

    private final ApiService apiService = retrofit.create(ApiService.class);

    private static final String TAG = "HomeFragment";
    private TextView textBody;
    private String token;// 保存的token
    private int current = 1;// 当前页，默认1
    private int size = 10;// 当前页大小，默认10

    // 下面的变量用于更新RecyclerView
    private SwipeRefreshLayout swipeRefreshLayout;
    private HomeFragmentAdapter mAdapter;
    private List<WeiboInfoItem> data;
    private boolean mIsLoadEnd = false;
    private RecyclerView mRecyclerView;
    private LinearLayout loadingLayout;
    private LinearLayout errorLayout;
    private BroadcastReceiver networkReceiver;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        View view = inflater.inflate(R.layout.home_fragment, container, false);

        // 加载数据显示在RecyclerView中
        mRecyclerView = view.findViewById(R.id.recyclerview);
        mRecyclerView.setLayoutManager(new StaggeredGridLayoutManager(1 // 显示一列
                , StaggeredGridLayoutManager.VERTICAL));// 垂直布局
        data = new ArrayList<>();
        mAdapter = new HomeFragmentAdapter(R.layout.big_button, data);
        swipeRefreshLayout = view.findViewById(R.id.swipe_layout);
        loadingLayout = view.findViewById(R.id.loading_layout);
        errorLayout = view.findViewById(R.id.error_layout);
        Button retryButton = view.findViewById(R.id.retry_button);

        // 监听刷新事件

        swipeRefreshLayout.setOnRefreshListener(new SwipeRefreshLayout.OnRefreshListener() {
            @Override
            public void onRefresh() {
                refreshData();
            }
        });

        retryButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                // 重新刷新数据
                isUserLogin(false);
            }
        });
        // 检查是否登录，根据登录状态选择请求参数
        isUserLogin(false);
        // 注册网络变化的监听器
        registerNetworkReceiver();
        return view;
    }

    private void showLoading() {
        loadingLayout.setVisibility(View.VISIBLE);
        errorLayout.setVisibility(View.GONE);
        swipeRefreshLayout.setVisibility(View.GONE);
    }

    private void showError() {
        loadingLayout.setVisibility(View.GONE);
        errorLayout.setVisibility(View.VISIBLE);
        swipeRefreshLayout.setVisibility(View.GONE);
    }

    private void showContent() {
        loadingLayout.setVisibility(View.GONE);
        errorLayout.setVisibility(View.GONE);
        swipeRefreshLayout.setVisibility(View.VISIBLE);
    }

    // 首次打开或下拉刷新时检查用户是否已经登录
    private void isUserLogin(boolean refresh) {
        if (!refresh) showLoading();// 第一次进入首页显示加载中
        final UserInfoItem[] user = {new UserInfoItem()};
        // 登录状态
        final String[] authorizationHeader = {Utils.getBearerToken(requireActivity())};
        retrofit2.Call<CommonData<UserInfoItem>> isUserLoginCall = apiService.queryLogin(authorizationHeader[0]);
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
                                retrofitGet(false, refresh);
                            } else {
                                // 请求成功将用户赋给变量
                                user[0] = body.getData();
                                // 用户名不为空表示登录状态
                                if (user[0].getUsername() != null) {
//                                Log.i(TAG, user.getUsername());
                                    // 登录则
                                    retrofitGet(true, refresh);
                                } else {// 用户名为空表示未登录状态
                                    Log.e(TAG, "用户未登录");
                                    retrofitGet(false, refresh);
                                }
                            }
                        } else {
                            showError();
                        }
                    }
                });
            }

            @Override
            public void onFailure(Call<CommonData<UserInfoItem>> call, Throwable t) {
                Log.e(TAG, "queryLogin onFailure.");
                showError();
            }
        });
    }

    // 搜索和刷新时调用，会先清空data再加进去，
    // 若已登录则login为true
    // 若为下拉刷新时调用则refresh为true
    private void retrofitGet(boolean login, boolean refresh) {
        swipeRefreshLayout.setRefreshing(true);
        data = new ArrayList<>();
        mAdapter = new HomeFragmentAdapter(R.layout.big_button, data);
        //加载更多2:在setAdapter之前 loadMore
        mAdapter.getLoadMoreModule().setAutoLoadMore(true);
        mAdapter.getLoadMoreModule().setEnableLoadMoreIfNotFullPage(true);
        mAdapter.getLoadMoreModule().setOnLoadMoreListener(new OnLoadMoreListener() {
            @Override
            public void onLoadMore() {
                loadMore();
            }
        });
        mRecyclerView.setAdapter(mAdapter);
        mAdapter.getLoadMoreModule().setAutoLoadMore(true);
        // 重置上拉刷新和current
        mIsLoadEnd = false;
        current = 1;
        if (login) token = Utils.getBearerToken(requireActivity());
        else token = null;
//        token = "";
        retrofit2.Call<CommonData<DataItem>> queryWeibo = apiService.queryWeibo(token, current, size);

        // 请求微博
        queryWeibo.enqueue(new retrofit2.Callback<CommonData<DataItem>>() {
            @Override
            public void onResponse(retrofit2.Call<CommonData<DataItem>> call, retrofit2.Response<CommonData<DataItem>> response) {
                CommonData<DataItem> body = response.body();
                // 使用Handler可以实现异步更新View
                handler.post(new Runnable() {
                    @Override
                    public void run() {
                        // 获取data字段，data中含有微博列表records
                        DataItem dataItem = body.getData();
                        Log.i(TAG, "run: " + body);
                        if (body != null) {
                            List<WeiboInfoItem> weiboInfoItems = dataItem.getRecords();
//                            Log.i(TAG, "weboInfoItems: " + weboInfoItems);
                            data.clear();//清空data
                            if (weiboInfoItems.size() == 0)
                                Toast.makeText(requireActivity(), "一条微博都没有", Toast.LENGTH_SHORT).show();
                            // 将列表需要展示的信息加入HomeItem
                            data.addAll(weiboInfoItems);
                            // 下拉刷新时打乱
                            if (refresh) Collections.shuffle(data);
                            showContent();
                        } else {
                            Toast.makeText(requireActivity(), "网络请求结果为空", Toast.LENGTH_SHORT).show();
                            showError();
                        }
                        // 更新列表
                        mAdapter.notifyDataSetChanged();
                        // 加载完数据设置为不刷新状态，将下拉进度收起来
                        swipeRefreshLayout.setRefreshing(false);
                    }
                });
            }

            @Override
            public void onFailure(retrofit2.Call<CommonData<DataItem>> call, Throwable t) {
                Log.e(TAG, "retrofitGet onFailure.");
                showError();
            }
        });
    }

    private void retrofitAdd() {
        token = Utils.getBearerToken(requireActivity());
        retrofit2.Call<CommonData<DataItem>> queryGamesCall = apiService.queryWeibo(token, ++current, size);

        // 加载更多
        queryGamesCall.enqueue(new retrofit2.Callback<CommonData<DataItem>>() {
            @Override
            public void onResponse(retrofit2.Call<CommonData<DataItem>> call, retrofit2.Response<CommonData<DataItem>> response) {
                CommonData<DataItem> body = response.body();
                // 使用Handler可以实现异步更新View
                handler.post(new Runnable() {
                    @Override
                    public void run() {
                        // 获取data字段，data中含有微博列表records
                        DataItem dataItem = body.getData();
                        List<WeiboInfoItem> weiboInfoItems = dataItem.getRecords();
                        // 将列表需要展示的信息加入HomeItem
                        data.addAll(weiboInfoItems);
                        // 更新列表
                        mAdapter.notifyDataSetChanged();
                        // 全部加载完则停止上拉加载
                        if (dataItem.getTotal() < current * size)
                            mIsLoadEnd = true;
                    }
                });
            }

            @Override
            public void onFailure(retrofit2.Call<CommonData<DataItem>> call, Throwable t) {
                Log.e(TAG, "retrofitAdd onFailure.");
            }
        });

    }

    //上拉加载
    private void loadMore() {
        Log.e(TAG, "loadMore");
        if (mIsLoadEnd) {
            mAdapter.getLoadMoreModule().loadMoreEnd();
            Toast.makeText(requireActivity(), "无更多内容", Toast.LENGTH_SHORT).show();
        } else {
            mRecyclerView.postDelayed(() -> {
                Log.e(TAG, "loadMore success");
                retrofitAdd();
                swipeRefreshLayout.setRefreshing(false);
                mAdapter.getLoadMoreModule().loadMoreComplete();
            }, 500);
        }
    }

    //下拉刷新
    private void refreshData() {
        new Handler().postDelayed(new Runnable() {
            @SuppressLint("NotifyDataSetChanged")
            @Override
            public void run() {
                isUserLogin(true);
            }
        }, 200);
    }

    private void registerNetworkReceiver() {
        // 注册网络变化的监听器
        networkReceiver = new BroadcastReceiver() {
            @Override
            public void onReceive(Context context, Intent intent) {
                ConnectivityManager cm = (ConnectivityManager) context.getSystemService(Context.CONNECTIVITY_SERVICE);
                NetworkInfo activeNetwork = cm.getActiveNetworkInfo();
                boolean isConnected = activeNetwork != null && activeNetwork.isConnectedOrConnecting();
                if (isConnected) {
                    Log.i(TAG, "Network is connected");
                    // 重新刷新数据
                    isUserLogin(false);
                } else {
                    Log.i(TAG, "Network is disconnected");
                }
            }
        };
        IntentFilter filter = new IntentFilter(ConnectivityManager.CONNECTIVITY_ACTION);
        requireActivity().registerReceiver(networkReceiver, filter);
    }


    // 注册和取消注册EventBus
    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EventBus.getDefault().register(this);
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        EventBus.getDefault().unregister(this);
        // 注销网络变化的监听器
        if (networkReceiver != null) {
            requireActivity().unregisterReceiver(networkReceiver);
        }
    }

    // 登录成功时用EventBus传递消息实现更新
    @Subscribe(threadMode = ThreadMode.MAIN)
    public void onMsgEvent(LoginEvent loginEvent) {
        Log.e(TAG, "onMsgEvent: " + loginEvent.isLogin());
        if (loginEvent.isLogin()) retrofitGet(true, false);
    }
}
