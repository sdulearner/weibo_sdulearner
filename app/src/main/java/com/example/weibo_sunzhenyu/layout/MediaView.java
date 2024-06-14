package com.example.weibo_sunzhenyu.layout;

import android.content.Context;
import android.content.res.TypedArray;
import android.graphics.drawable.Drawable;
import android.net.Uri;
import android.util.AttributeSet;
import android.util.Log;
import android.view.View;
import android.widget.FrameLayout;
import android.widget.ImageView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import com.bumptech.glide.Glide;
import com.bumptech.glide.load.DataSource;
import com.bumptech.glide.load.engine.GlideException;
import com.bumptech.glide.request.RequestListener;
import com.bumptech.glide.request.target.Target;
import com.example.weibo_sunzhenyu.R;
import com.example.weibo_sunzhenyu.component.CustomVideoPlayer;
import com.example.weibo_sunzhenyu.component.VideoPlayer;

import java.util.List;

public class MediaView extends FrameLayout {


    private static final String TAG = "MediaView";
    private float mHorizontalMargin = 10;
    private float mVerticalMargin = 10;
    private ImageView singleImageView;
    private List<String> mTags = null;
    private CustomVideoPlayer customVideoPlayer;

    public MediaView(@NonNull Context context) {
        super(context);
        init(context);
    }

    public MediaView(@NonNull Context context, @Nullable AttributeSet attrs) {
        super(context, attrs);
        init(context);
    }

    public MediaView(@NonNull Context context, @Nullable AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        TypedArray a = context.obtainStyledAttributes(attrs, R.styleable.MediaView);
        mHorizontalMargin = a.getDimension(R.styleable.MediaView_hMargin, 10);
        mVerticalMargin = a.getDimension(R.styleable.MediaView_vMargin, 10);
        a.recycle();
        init(context);
    }

    private void init(Context context) {
        customVideoPlayer = new CustomVideoPlayer(context);
        // Add CustomVideoPlayer to the FrameLayout
//        addView(customVideoPlayer, new FrameLayout.LayoutParams(LayoutParams.MATCH_PARENT, LayoutParams.WRAP_CONTENT));
        // TODO: 2024/6/14  
        addView(customVideoPlayer, new FrameLayout.LayoutParams(LayoutParams.MATCH_PARENT, 500));
    }


    private void loadVideo(String coverUrl, String videoUrl) {
        Log.i(TAG, "loadVideo: Start loading video from URL: " + coverUrl);
        customVideoPlayer.setVideo(coverUrl, videoUrl);
        addView(customVideoPlayer);
    }

    // 加载MediaView中的信息
    public void setTags(List<String> tags, boolean isPhoto) {
        removeAllViews();
        mTags = tags;
        if (isPhoto) {
            if (tags != null && !tags.isEmpty()) {
                displayImages();
//                videoPlayer.setVisibility(View.GONE);
            }
        } else {
            if (tags != null && tags.size() >= 2) {
                String videoCoverUrl = tags.get(0);
                String videoUrl = tags.get(1);
                loadVideo(videoCoverUrl, videoUrl);
                customVideoPlayer.setVisibility(View.VISIBLE);
            }
        }
//        if (this.mTags == tags) {
//            Log.i(TAG, "setTags: " + tags.toString());
//            this.removeAllViews();
//        }
//        mTags = tags;
//        int tagCount = mTags != null ? mTags.size() : 0;//获取总标签数
//        int childCount = getChildCount();// 获取现有的子View数
//
//        if (tagCount == 0) {
//            // 普通文本(没有图片也没有视频)
//            removeAllViews();
//        } else if (tagCount == 1) {
//            // 一张图片或一个视频
//            if (isPhoto) {
//                ImageView child = new ImageView(getContext());
//                // 单张图片的宽高使用Glide动态获取，根据宽>高，显示横图样式，否则显示竖图样式
//                Glide.with(child.getContext()) // TODO: 2024/6/14 改为直接getContext()行不行？
//                        .load(mTags.get(0))
//                        .listener(new RequestListener<Drawable>() {
//                            @Override
//                            public boolean onLoadFailed(@Nullable GlideException e, Object model, Target<Drawable> target, boolean isFirstResource) {
//                                // 处理加载失败的情况
////                                Toast.makeText(getContext(), "图片加载失败", Toast.LENGTH_SHORT).show();
//                                return false;
//                            }
//
//                            @Override
//                            public boolean onResourceReady(Drawable resource, Object model, Target<Drawable> target, DataSource dataSource, boolean isFirstResource) {
//                                // 获取图片的Bitmap（注意：这可能会消耗额外的内存）
//                                Bitmap bitmap = ((BitmapDrawable) resource).getBitmap();
//                                int width = bitmap.getWidth();
//                                int height = bitmap.getHeight();
//                                // 根据宽高设置样式
//                                if (width < height)
//                                    // 显示竖图样式
//                                    horizontalMode = false;
//                                return false;
//                            }
//                        })
//                        .into(child);
//            } else {
//                // 一个视频
//                this.isPhoto = false;
//            }
//        } else {
//            // 其他情况为两张及以上图片
//            if (tagCount > childCount) {// 总标签数大于子View数，向后追加
//                for (int i = childCount; i < tagCount; i++) {
//                    ImageView child = new ImageView(getContext());
//                    Glide.with(child.getContext())
//                            .load(mTags.get(i))
//                            .into(child);
//                    addView(child, i);//添加子View
//                }
//            } else if (tagCount < childCount) {//标签数量小于子View数量，移出
//                for (int i = childCount; i > tagCount; i--) {
//                    removeViewAt(childCount);
//                }
//            }
//        }
    }


    private void displayImages() {
        int size = mTags.size();
        if (size == 1) {
            addSingleImage(mTags.get(0));
        } else {
            addGridImages(mTags);
        }
    }

    private void addSingleImage(String url) {
        singleImageView = new ImageView(getContext());
//        LayoutParams params = new LayoutParams(LayoutParams.MATCH_PARENT, LayoutParams.MATCH_PARENT);
//        imageView.setLayoutParams(params);
//        singleImageView.setScaleType(ImageView.ScaleType.CENTER_CROP);
        LayoutParams params = new LayoutParams(LayoutParams.MATCH_PARENT, 500);
        singleImageView.setScaleType(ImageView.ScaleType.FIT_CENTER);
        singleImageView.setLayoutParams(params);

        Log.i(TAG, "addSingleImage: ");
        Glide.with(getContext())
                .load(url)// TODO: 2024/6/14 单张图片的宽高使用Glide动态获取，根据宽>高，显示横图样式，否则显示竖图样式
//                .listener(new RequestListener<Drawable>() {
//                    @Override
//                    public boolean onLoadFailed(@Nullable GlideException e, Object model, @NonNull Target<Drawable> target, boolean isFirstResource) {
//                        Log.i(TAG, "addSingleImage onLoadFailed: ");
//                        return false;
//                    }
//
//                    @Override
//                    public boolean onResourceReady(@NonNull Drawable resource, @NonNull Object model, Target<Drawable> target, @NonNull DataSource dataSource, boolean isFirstResource) {
//                        int width = resource.getIntrinsicWidth();
//                        int height = resource.getIntrinsicHeight();
//                        Log.i(TAG, "onResourceReady: " + "width: " + width + " height:" + height);
//                        adjustSingleImageViewLayout(width, height);
//                        return false;
//                    }
//                })
                .into(singleImageView);
        addView(singleImageView);
    }

    private void adjustSingleImageViewLayout(int width, int height) {
        LayoutParams params;
        Log.i(TAG, "adjustImageViewLayout: " + "width: " + width + " height:" + height);
        if (width > height) {
            // Horizontal image
            params = new LayoutParams(LayoutParams.MATCH_PARENT, LayoutParams.WRAP_CONTENT);
            singleImageView.setScaleType(ImageView.ScaleType.FIT_CENTER);
        } else {
            // Vertical image
            params = new LayoutParams(LayoutParams.WRAP_CONTENT, LayoutParams.MATCH_PARENT);
            singleImageView.setScaleType(ImageView.ScaleType.FIT_CENTER);
        }
        singleImageView.setLayoutParams(params);
    }

    private void addGridImages(List<String> urls) {
        int imageSize = (getWidth() - (int) mHorizontalMargin * 2) / 3;
        int gridSize = Math.min(urls.size(), 9); // Limit to 9 images

        for (int i = 0; i < gridSize; i++) {
            ImageView imageView = new ImageView(getContext());
            LayoutParams params = new LayoutParams(imageSize, imageSize);
            params.leftMargin = (i % 3) * (imageSize + (int) mHorizontalMargin / 2);
            params.topMargin = (i / 3) * (imageSize + (int) mVerticalMargin / 2);
            imageView.setLayoutParams(params);
            imageView.setScaleType(ImageView.ScaleType.CENTER_CROP);
            Glide.with(getContext()).load(urls.get(i)).into(imageView);
            addView(imageView);
        }
    }

}
