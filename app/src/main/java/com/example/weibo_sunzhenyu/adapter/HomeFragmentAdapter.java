package com.example.weibo_sunzhenyu.adapter;

import android.text.TextUtils;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import com.bumptech.glide.Glide;
import com.chad.library.adapter.base.BaseQuickAdapter;
import com.chad.library.adapter.base.module.BaseLoadMoreModule;
import com.chad.library.adapter.base.module.LoadMoreModule;
import com.chad.library.adapter.base.viewholder.BaseViewHolder;
import com.example.weibo_sunzhenyu.R;
import com.example.weibo_sunzhenyu.entity.WeiboInfoItem;
import com.example.weibo_sunzhenyu.layout.MediaView;

import java.util.ArrayList;
import java.util.List;

public class HomeFragmentAdapter extends BaseQuickAdapter<WeiboInfoItem, BaseViewHolder> implements LoadMoreModule {
    public HomeFragmentAdapter(int layoutResId, @Nullable List<WeiboInfoItem> data) {
        super(layoutResId, data);
    }

    public HomeFragmentAdapter(int layoutResId) {
        super(layoutResId);
    }

    @Override
    public void onBindViewHolder(@NonNull BaseViewHolder holder, int position) {
        super.onBindViewHolder(holder, position);
    }

    @NonNull
    @Override
    public BaseLoadMoreModule addLoadMoreModule(@NonNull BaseQuickAdapter<?, ?> baseQuickAdapter) {
        return LoadMoreModule.super.addLoadMoreModule(baseQuickAdapter);
    }

    @Override
    protected void convert(@NonNull BaseViewHolder baseViewHolder, WeiboInfoItem weiboInfoItem) {
        //加载更多1:实现LoadMoreModule
        // 1.加载用户头像
        ImageView big_avatar = baseViewHolder.getView(R.id.big_avatar);
        Glide.with(big_avatar.getContext())
                .load(weiboInfoItem.getAvatar())
                .into(big_avatar);

        // 2.加载用户名
        TextView big_username = baseViewHolder.getView(R.id.big_username);
        big_username.setText(weiboInfoItem.getUsername());

        // 加载title
        // TODO: 2024/6/14 设置为最大六行
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
        TextView big_commentIcon = baseViewHolder.getView(R.id.big_commentIcon);
        TextView big_commentText = baseViewHolder.getView(R.id.big_commentText);
        big_likeText.setText(String.valueOf(weiboInfoItem.getLikeCount()));
        if (weiboInfoItem.isLikeFlag()) {//已点赞
            big_likeIcon.setText("♥");
            big_likeText.setTextColor(getContext().getColor(R.color.red));
        } else {//未点赞
            big_likeIcon.setText("♡");
            big_likeText.setTextColor(getContext().getColor(R.color.gray1));
        }


    }
}
