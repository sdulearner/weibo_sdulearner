package com.example.weibo_sunzhenyu.fragment;

import android.annotation.SuppressLint;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.os.Message;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.SearchView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.RecyclerView;
import androidx.recyclerview.widget.StaggeredGridLayoutManager;
import androidx.swiperefreshlayout.widget.SwipeRefreshLayout;

import com.chad.library.adapter.base.listener.OnLoadMoreListener;
import com.example.weibo_sunzhenyu.R;
import com.example.weibo_sunzhenyu.adapter.HomeFragmentAdapter;
import com.example.weibo_sunzhenyu.entity.CommonData;
import com.example.weibo_sunzhenyu.entity.DataItem;
import com.example.weibo_sunzhenyu.entity.WeiboInfoItem;
import com.example.weibo_sunzhenyu.utils.Utils;
import com.google.gson.Gson;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import okhttp3.MediaType;
import okhttp3.OkHttpClient;
import retrofit2.Retrofit;
import retrofit2.converter.gson.GsonConverterFactory;
import retrofit2.http.GET;
import retrofit2.http.Header;
import retrofit2.http.Headers;
import retrofit2.http.Path;
import retrofit2.http.Query;

public class HomeFragment extends Fragment {
    private final OkHttpClient okHttpClient = new OkHttpClient.Builder().build();
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
//            if (msg.what == 1) textBody.setText((CharSequence) msg.obj);
        }
    };

    public interface ApiService {
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

        //加载更多2:在setAdapter之前 loadMore
        mAdapter.getLoadMoreModule().setAutoLoadMore(true);
        mAdapter.getLoadMoreModule().setEnableLoadMoreIfNotFullPage(true);
        mAdapter.getLoadMoreModule().setOnLoadMoreListener(new OnLoadMoreListener() {
            @Override
            public void onLoadMore() {
//                loadMore();
            }
        });
        mRecyclerView.setAdapter(mAdapter);

        //3.监听刷新事件
        swipeRefreshLayout.setOnRefreshListener(new SwipeRefreshLayout.OnRefreshListener() {
            @Override
            public void onRefresh() {
                refreshData();
            }
        });
        retrofitGet();
        return view;
    }

    // 搜索和刷新时调用，会先清空data再加进去
    private void retrofitGet() {
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
        //这里是主线程
        //一些比较耗时的操作，比如联网获取数据，需要放到子线程执行
        mAdapter.getLoadMoreModule().setAutoLoadMore(true);
        // 重置上拉刷新和current
        mIsLoadEnd = false;
        current = 1;
        token = Utils.getBearerToken(requireActivity());
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
//                        textBody.setText(body.toString());
//                        Log.i(TAG, "queryGames: " + body);
                        // 获取data字段，data中含有游戏列表records
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
                            // 刷新时打乱
//                            Collections.shuffle(data);
                        } else {
                            Toast.makeText(requireActivity(), "网络请求结果为空", Toast.LENGTH_SHORT).show();
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
                        // 获取data字段，data中含有游戏列表records
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
                retrofitGet();
            }
        }, 200);
    }


}
