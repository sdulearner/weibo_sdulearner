package com.example.weibo_sunzhenyu.adapter;

import static androidx.core.content.ContextCompat.startActivity;

import android.animation.Animator;
import android.animation.AnimatorSet;
import android.animation.ObjectAnimator;
import android.content.Intent;
import android.os.Handler;
import android.os.Looper;
import android.os.Message;
import android.text.TextUtils;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.Glide;
import com.bumptech.glide.load.engine.DiskCacheStrategy;
import com.chad.library.adapter.base.BaseQuickAdapter;
import com.chad.library.adapter.base.module.BaseLoadMoreModule;
import com.chad.library.adapter.base.module.LoadMoreModule;
import com.chad.library.adapter.base.viewholder.BaseViewHolder;
import com.example.weibo_sunzhenyu.R;
import com.example.weibo_sunzhenyu.activity.LoginActivity;
import com.example.weibo_sunzhenyu.entity.CommonData;
import com.example.weibo_sunzhenyu.entity.LoginEvent;
import com.example.weibo_sunzhenyu.entity.UserInfoItem;
import com.example.weibo_sunzhenyu.entity.WeiboInfoItem;
import com.example.weibo_sunzhenyu.fragment.MyPageFragment;
import com.example.weibo_sunzhenyu.layout.MediaView;
import com.example.weibo_sunzhenyu.utils.Utils;
import com.google.gson.Gson;

import org.greenrobot.eventbus.EventBus;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import okhttp3.OkHttpClient;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;
import retrofit2.Retrofit;
import retrofit2.converter.gson.GsonConverterFactory;
import retrofit2.http.Body;
import retrofit2.http.GET;
import retrofit2.http.Header;
import retrofit2.http.POST;

public class HomeFragmentAdapter extends BaseQuickAdapter<WeiboInfoItem, BaseViewHolder> implements LoadMoreModule {
    private static final String TAG = "HomeFragmentAdapter";
    private final OkHttpClient okHttpClient = new OkHttpClient.Builder().build();
    private final Gson gson = new Gson();
    private boolean login = false;
    private final Handler handler = new Handler(Looper.getMainLooper()) {
        @Override
        public void handleMessage(@NonNull Message msg) {
            super.handleMessage(msg);
        }
    };

    private final Retrofit retrofit = new Retrofit.Builder()
            .baseUrl("https://hotfix-service-prod.g.mi.com")
            .client(okHttpClient)
            .addConverterFactory(GsonConverterFactory.create(gson))
            .build();
    private HomeFragmentAdapter.ApiService apiService = retrofit.create(HomeFragmentAdapter.ApiService.class);


    public interface ApiService {
        // 根据token确定登录状态
        @GET("/weibo/api/user/info")
        retrofit2.Call<CommonData<UserInfoItem>> queryLogin(@Header("Authorization") String authorizationHeader);

        // 点赞
        @POST("/weibo/like/up")
        retrofit2.Call<CommonData> likeUp(@Header("Authorization") String authorizationHeader, @Body Map<String, String> requestBody);

        // 取消点赞
        @POST("/weibo/like/down")
        retrofit2.Call<CommonData> likeDown(@Header("Authorization") String authorizationHeader, @Body Map<String, String> requestBody);

    }

    private void retrofitLike(boolean like, long weibo_id) {
        final String[] authorizationHeader = {Utils.getBearerToken(getContext())};
        Map<String, String> requestBody = new HashMap<>();
        requestBody.put("id", String.valueOf(weibo_id));
        if (like) {
            retrofit2.Call<CommonData> likeUpCall = apiService.likeUp(authorizationHeader[0], requestBody);
            likeUpCall.enqueue(new Callback<CommonData>() {
                @Override
                public void onResponse(Call<CommonData> call, Response<CommonData> response) {
                    CommonData<UserInfoItem> body = response.body();
                    if (body != null) {
                        if (body.getCode() == 200) {
                            Log.i(TAG, "retrofitLike: 点赞成功");
                        } else
                            Toast.makeText(getContext(), "点赞失败，" + body.getMsg(), Toast.LENGTH_SHORT).show();
                    }
                }

                @Override
                public void onFailure(Call<CommonData> call, Throwable t) {
                    Log.e(TAG, "retrofitLike likeUpCall onFailure: ");
                }
            });
        } else {
            retrofit2.Call<CommonData> likeDownCall = apiService.likeDown(authorizationHeader[0], requestBody);
            likeDownCall.enqueue(new Callback<CommonData>() {
                @Override
                public void onResponse(Call<CommonData> call, Response<CommonData> response) {
                    CommonData<UserInfoItem> body = response.body();
                    if (body != null) {
                        if (body.getCode() == 200) {
                            Log.i(TAG, "retrofitLike: 取消点赞成功");
                        } else
                            Toast.makeText(getContext(), "取消点赞失败，" + body.getMsg(), Toast.LENGTH_SHORT).show();
                    }
                }

                @Override
                public void onFailure(Call<CommonData> call, Throwable t) {
                    Log.e(TAG, "retrofitLike likeDownCall onFailure: ");
                }
            });
        }
    }

    private void isUserLogin(boolean init, WeiboInfoItem weiboInfoItem, TextView big_likeIcon, TextView big_likeText) {
        final boolean[] isLogin = {false};
        final UserInfoItem[] user = {new UserInfoItem()};
        // 登录状态
        final String[] authorizationHeader = {Utils.getBearerToken(getContext())};
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
                                Utils.clearToken(getContext());
                                Log.e(TAG, "token过期");
                                Intent intent = new Intent(getContext(), LoginActivity.class);
                                getContext().startActivity(intent);
                            } else {
                                // 请求成功将用户赋给变量
                                user[0] = body.getData();
                                // 用户名不为空表示登录状态
                                if (user[0].getUsername() != null) {
//                                Log.i(TAG, user.getUsername());
                                    // 如果已登录则设置登录状态
                                    login = true;
                                } else {// 用户名为空表示未登录状态
                                    Log.e(TAG, "用户未登录");
                                    if (!init) {
                                        // 如果不是在初始化时调用此方法说明是在点赞或取消点赞时调用的此方法，需要打开登录页面
                                        Toast.makeText(getContext(), "请先登录", Toast.LENGTH_SHORT).show();
                                        Intent intent = new Intent(getContext(), LoginActivity.class);
                                        getContext().startActivity(intent);
                                    }
                                }
                            }
                        } else {
//                            showError();
                        }
                    }
                });
            }

            @Override
            public void onFailure(Call<CommonData<UserInfoItem>> call, Throwable t) {
                Log.e(TAG, "queryLogin onFailure.");
            }
        });

    }

    public HomeFragmentAdapter(int layoutResId, @Nullable List<WeiboInfoItem> data) {
        super(layoutResId, data);
    }

    public HomeFragmentAdapter(int layoutResId) {
        super(layoutResId);
    }

    @Override
    public void onBindViewHolder(@NonNull BaseViewHolder holder, int position) {
        super.onBindViewHolder(holder, position);
        // FIXME: 2024/6/16 在这里设置监听会有Bug
//        FATAL EXCEPTION: main
//        Process: com.example.weibo_sunzhenyu, PID: 11906
//        java.lang.IllegalStateException: No view found with id 2131230818
//        at com.chad.library.adapter.base.viewholder.BaseViewHolder.getView(BaseViewHolder.kt:38)
//        at com.example.weibo_sunzhenyu.adapter.HomeFragmentAdapter.onBindViewHolder(HomeFragmentAdapter.java:41)
//        // 关闭按钮的监听
//        TextView big_close = holder.getView(R.id.big_close);
//        big_close.setOnClickListener(v -> {
//            // 删除当前item
//            getData().remove(position);
//            notifyItemRemoved(position);
//            notifyItemRangeChanged(position, getItemCount());
//        });
//        // 评论的监听
//        TextView big_commentIcon = holder.getView(R.id.big_commentIcon);
//        TextView big_commentText = holder.getView(R.id.big_commentText);
//        String text = "点击第" + (position + 1) + "条数据评论按钮";
//        big_commentIcon.setOnClickListener(v -> {
//            Toast.makeText(getContext(), text, Toast.LENGTH_SHORT).show();
//        });
//        big_commentText.setOnClickListener(v -> {
//            Toast.makeText(getContext(), text, Toast.LENGTH_SHORT).show();
//        });
    }

    @NonNull
    @Override
    public BaseLoadMoreModule addLoadMoreModule(@NonNull BaseQuickAdapter<?, ?>
                                                        baseQuickAdapter) {
        return LoadMoreModule.super.addLoadMoreModule(baseQuickAdapter);
    }

    @Override
    protected void convert(@NonNull BaseViewHolder baseViewHolder, WeiboInfoItem weiboInfoItem) {
        //加载更多1:实现LoadMoreModule
        // 1.加载用户头像
        ImageView big_avatar = baseViewHolder.getView(R.id.big_avatar);
        Glide.with(big_avatar.getContext())
                .load(weiboInfoItem.getAvatar())
                .diskCacheStrategy(DiskCacheStrategy.ALL)
                .into(big_avatar);

        // 2.加载用户名
        TextView big_username = baseViewHolder.getView(R.id.big_username);
        big_username.setText(weiboInfoItem.getUsername());

        // 加载title
        // 设置为最大六行
        TextView big_title = baseViewHolder.getView(R.id.big_title);
        big_title.setText(weiboInfoItem.getTitle());
        // Set maximum lines to 6
        big_title.setMaxLines(6);
        // Set ellipsize to end
        big_title.setEllipsize(TextUtils.TruncateAt.END);

        // 加载MediaView
        boolean isPhoto = true;
        List<String> mTags = new ArrayList<>();
        MediaView mediaView = baseViewHolder.getView(R.id.mediaView);
        String videoUrl = weiboInfoItem.getVideoUrl();
        List<String> images = weiboInfoItem.getImages();
        if (videoUrl != null) {
            isPhoto = false;
            mTags.add(weiboInfoItem.getPoster());//视频封面
            mTags.add(weiboInfoItem.getVideoUrl());//视频链接
        } else if (images != null) {
            mTags.addAll(images);
        }
        mediaView.setTags(mTags, isPhoto);

        // 加载点赞评论
        TextView big_likeIcon = baseViewHolder.getView(R.id.big_likeIcon);
        TextView big_likeText = baseViewHolder.getView(R.id.big_likeText);
        isUserLogin(true, weiboInfoItem, big_likeIcon, big_likeText);// 判断是否登录
        big_likeText.setText(String.valueOf(weiboInfoItem.getLikeCount()));
        if (weiboInfoItem.isLikeFlag()) {//已点赞
            big_likeIcon.setBackgroundResource(R.drawable.like_fill);
            big_likeText.setTextColor(getContext().getColor(R.color.red));
        } else {//未点赞
            big_likeIcon.setBackgroundResource(R.drawable.like);
            big_likeText.setTextColor(getContext().getColor(R.color.gray1));
        }
        // 关闭按钮的监听
        TextView big_close = baseViewHolder.getView(R.id.big_close);
        big_close.setOnClickListener(v -> {
            // 删除当前item
            int position = baseViewHolder.getAbsoluteAdapterPosition();
//            int position = baseViewHolder.getBindingAdapterPosition();
            // 防止越界闪退
            if (position != RecyclerView.NO_POSITION && position < getData().size()) {
                if (getData().size() == 1) {
                    // 最后一项数据
                    getData().clear();
                    notifyDataSetChanged();
                } else {
                    getData().remove(position);
                    notifyItemRemoved(position);
                    notifyItemRangeChanged(position, getItemCount());
                }
            }
        });
        // 设置点赞和评论的点击监听
        big_likeIcon.setOnClickListener(v -> {
            // 如果已经登录则handleLikeClick，否则检查登录状态
            if (login) {
                handleLikeClick(weiboInfoItem, big_likeIcon, big_likeText);
                return;
            }
            isUserLogin(false, weiboInfoItem, big_likeIcon, big_likeText);
        });
        big_likeText.setOnClickListener(v -> {
            // 如果已经登录则handleLikeClick，否则检查登录状态
            if (login) {
                handleLikeClick(weiboInfoItem, big_likeIcon, big_likeText);
                return;
            }
            isUserLogin(false, weiboInfoItem, big_likeIcon, big_likeText);
        });

        // 评论的监听
        TextView big_commentIcon = baseViewHolder.getView(R.id.big_commentIcon);
        TextView big_commentText = baseViewHolder.getView(R.id.big_commentText);
        String text = "点击第" + (baseViewHolder.getAdapterPosition() + 1) + "条数据评论按钮";
        big_commentIcon.setOnClickListener(v -> {
            Toast.makeText(getContext(), text, Toast.LENGTH_SHORT).show();
        });
        big_commentText.setOnClickListener(v -> {
            Toast.makeText(getContext(), text, Toast.LENGTH_SHORT).show();
        });
    }

    // 当可以点赞或取消点赞时调用
    private void handleLikeClick(WeiboInfoItem weiboInfoItem, TextView big_likeIcon, TextView
            big_likeText) {
        retrofitLike(!weiboInfoItem.isLikeFlag(), weiboInfoItem.getId());
        // 禁用点击事件，等动画播放完才可点击
        big_likeIcon.setClickable(false);
        big_likeText.setClickable(false);

        if (weiboInfoItem.isLikeFlag()) {
            // 取消点赞
            weiboInfoItem.setLikeFlag(false);
            weiboInfoItem.setLikeCount(weiboInfoItem.getLikeCount() - 1);
            big_likeIcon.setBackgroundResource(R.drawable.like);
            big_likeText.setTextColor(getContext().getColor(R.color.gray1));
            animateDislike(big_likeIcon, () -> {
                // 动画结束后重新启用点击事件
                big_likeIcon.setClickable(true);
                big_likeText.setClickable(true);
            });
        } else {
            // 点赞
            weiboInfoItem.setLikeFlag(true);
            weiboInfoItem.setLikeCount(weiboInfoItem.getLikeCount() + 1);
            big_likeIcon.setBackgroundResource(R.drawable.like_fill);
            big_likeText.setTextColor(getContext().getColor(R.color.red));
            animateLike(big_likeIcon, () -> {
                // 动画结束后重新启用点击事件
                big_likeIcon.setClickable(true);
                big_likeText.setClickable(true);
            });
        }
        big_likeText.setText(String.valueOf(weiboInfoItem.getLikeCount()));
    }

    private void animateLike(View view, Runnable endAction) {
        ObjectAnimator scaleX = ObjectAnimator.ofFloat(view, "scaleX", 1.0f, 1.2f, 1.0f);
        ObjectAnimator scaleY = ObjectAnimator.ofFloat(view, "scaleY", 1.0f, 1.2f, 1.0f);
        ObjectAnimator rotationY = ObjectAnimator.ofFloat(view, "rotationY", 0f, 360f);

        AnimatorSet animatorSet = new AnimatorSet();
        animatorSet.playTogether(scaleX, scaleY, rotationY);
        animatorSet.setDuration(1000);
        animatorSet.addListener(new AnimatorSet.AnimatorListener() {
            @Override
            public void onAnimationStart(@NonNull Animator animation) {
            }

            @Override
            public void onAnimationEnd(Animator animation) {
                endAction.run();
            }

            @Override
            public void onAnimationCancel(Animator animation) {
                endAction.run();
            }

            @Override
            public void onAnimationRepeat(Animator animation) {
            }
        });
        animatorSet.start();
    }

    private void animateDislike(View view, Runnable endAction) {
        ObjectAnimator scaleX = ObjectAnimator.ofFloat(view, "scaleX", 1.0f, 0.8f, 1.0f);
        ObjectAnimator scaleY = ObjectAnimator.ofFloat(view, "scaleY", 1.0f, 0.8f, 1.0f);

        AnimatorSet animatorSet = new AnimatorSet();
        animatorSet.playTogether(scaleX, scaleY);
        animatorSet.setDuration(1000);
        animatorSet.addListener(new AnimatorSet.AnimatorListener() {
            @Override
            public void onAnimationStart(Animator animation) {
            }

            @Override
            public void onAnimationEnd(Animator animation) {
                endAction.run();
            }

            @Override
            public void onAnimationCancel(Animator animation) {
                endAction.run();
            }

            @Override
            public void onAnimationRepeat(Animator animation) {
            }
        });
        animatorSet.start();
    }
}
