package com.example.weibo_sunzhenyu.layout;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.content.res.TypedArray;
import android.graphics.drawable.ColorDrawable;
import android.graphics.drawable.Drawable;
import android.util.AttributeSet;
import android.util.Log;
import android.view.View;
import android.widget.FrameLayout;
import android.widget.ImageView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.core.app.ActivityCompat;
import androidx.core.app.ActivityOptionsCompat;

import com.bumptech.glide.Glide;
import com.bumptech.glide.load.DataSource;
import com.bumptech.glide.load.engine.DiskCacheStrategy;
import com.bumptech.glide.load.engine.GlideException;
import com.bumptech.glide.request.RequestListener;
import com.bumptech.glide.request.target.Target;
import com.example.weibo_sunzhenyu.R;
import com.example.weibo_sunzhenyu.activity.ImageViewerActivity;
import com.example.weibo_sunzhenyu.component.CustomVideoPlayer;

import java.util.ArrayList;
import java.util.List;

public class MediaView extends FrameLayout {


    private static final String TAG = "MediaView";
    private float mHorizontalMargin = 10;
    private float mVerticalMargin = 10;
    private ImageView singleImageView;
    private List<String> mTags = null;
    private CustomVideoPlayer customVideoPlayer;

    private boolean isSingleImage = false;
    private boolean isVideo = false;
    private int singleImageWidth = 0;
    private int singleImageHeight = 0;

    public MediaView(@NonNull Context context) {
        super(context);
    }

    public MediaView(@NonNull Context context, @Nullable AttributeSet attrs) {
        super(context, attrs);
        init(attrs);
    }

    public MediaView(@NonNull Context context, @Nullable AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        init(attrs);
    }

    private void init(@Nullable AttributeSet attrs) {
        if (attrs != null) {
            TypedArray a = getContext().obtainStyledAttributes(attrs, R.styleable.MediaView);
            mHorizontalMargin = a.getDimension(R.styleable.MediaView_hMargin, 10);
            mVerticalMargin = a.getDimension(R.styleable.MediaView_vMargin, 10);
            a.recycle();
        }
    }

    private void loadVideo(String coverUrl, String videoUrl) {
        // 视频
        isVideo = true;
        customVideoPlayer = new CustomVideoPlayer(getContext());
        Log.i(TAG, "loadVideo: Start loading video from URL: " + coverUrl);
        customVideoPlayer.setVideo(coverUrl, videoUrl);
        // 默认显示横图样式
        requestLayout();
        addView(customVideoPlayer);
    }


    // 加载MediaView中的信息
    public void setTags(List<String> tags, boolean isPhoto, String userAvatarUrl, String userName) {
        removeAllViews();
        isSingleImage = false;
        isVideo = false;
        mTags = tags;
        if (isPhoto) {
            if (tags != null && !tags.isEmpty()) {
                displayImages(userAvatarUrl, userName);
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
    }


    private void displayImages(String userAvatarUrl, String userName) {
        int size = mTags.size();
        if (size == 1) {
            addSingleImage(mTags.get(0), userAvatarUrl, userName);
        } else {
            addGridImages(mTags, userAvatarUrl, userName);
        }
    }

    // 调整Layout的方法
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

    private void addSingleImage(String url, String userAvatarUrl, String userName) {
        isSingleImage = true;
        singleImageView = new ImageView(getContext());
        singleImageView.setScaleType(ImageView.ScaleType.FIT_CENTER);
        singleImageView.setImageDrawable(new ColorDrawable(0xFFCCCCCC)); // 灰色方框作为占位符

        // 设置点击跳转到大图浏览
        singleImageView.setOnClickListener(v -> {
            Intent intent = new Intent(getContext(), ImageViewerActivity.class);
            intent.putStringArrayListExtra("imageUrls", new ArrayList<>(mTags));
            intent.putExtra("currentIndex", 0); // 单张图片索引为0
            intent.putExtra("userAvatarUrl", userAvatarUrl); // 用户头像
            intent.putExtra("userName", userName); // 用户名

            ActivityOptionsCompat options = ActivityOptionsCompat.makeScaleUpAnimation(singleImageView, 0, 0, singleImageView.getWidth(), singleImageView.getHeight());
            ActivityCompat.startActivity(getContext(), intent, options.toBundle());
        });

        Log.i(TAG, "addSingleImage: ");
        Glide.with(getContext())
                .load(url)
                .diskCacheStrategy(DiskCacheStrategy.ALL)
                .listener(new RequestListener<Drawable>() {
                    @Override
                    public boolean onLoadFailed(@Nullable GlideException e, Object model, Target<Drawable> target, boolean isFirstResource) {
                        Log.i(TAG, "onLoadFailed: ");
                        return false;
                    }

                    @Override
                    public boolean onResourceReady(@NonNull Drawable resource, Object model, @NonNull Target<Drawable> target, @NonNull DataSource dataSource, boolean isFirstResource) {
                        singleImageWidth = resource.getIntrinsicWidth();
                        singleImageHeight = resource.getIntrinsicHeight();
                        Log.i(TAG, "onResourceReady: " + "width: " + singleImageWidth + " height:" + singleImageHeight);
                        requestLayout(); // 重新布局
                        return false;
                    }
                })
                .into(singleImageView);
        addView(singleImageView);
    }

    private void addGridImages(List<String> urls, String userAvatarUrl, String userName) {
        int imageSize = (getWidth() - (int) mHorizontalMargin * 2) / 3;
        int gridSize = Math.min(urls.size(), 9); // Limit to 9 images

        for (int i = 0; i < gridSize; i++) {
            ImageView imageView = new ImageView(getContext());
            LayoutParams params = new LayoutParams(imageSize, imageSize);
            imageView.setLayoutParams(params);
            imageView.setScaleType(ImageView.ScaleType.CENTER_CROP);
            imageView.setBackgroundColor(getResources().getColor(R.color.gray2)); // 灰色方框作为未加载时的占位

            // 设置点击跳转到大图浏览
            int finalI = i;
            imageView.setOnClickListener(v -> {
                Intent intent = new Intent(getContext(), ImageViewerActivity.class);
                intent.putStringArrayListExtra("imageUrls", new ArrayList<>(mTags));
                intent.putExtra("currentIndex", finalI);
                intent.putExtra("userAvatarUrl", userAvatarUrl); // 用户头像
                intent.putExtra("userName", userName); // 用户名

                ActivityOptionsCompat options = ActivityOptionsCompat.makeScaleUpAnimation(imageView, 0, 0, imageView.getWidth(), imageView.getHeight());
                ActivityCompat.startActivity(getContext(), intent, options.toBundle());
            });

            Glide.with(getContext()).load(urls.get(i)).diskCacheStrategy(DiskCacheStrategy.ALL).into(imageView);
            addView(imageView);
        }
    }

    @Override
    protected void onMeasure(int widthMeasureSpec, int heightMeasureSpec) {
        super.onMeasure(widthMeasureSpec, heightMeasureSpec);
        int totalWidth = MeasureSpec.getSize(widthMeasureSpec);
        if (isSingleImage && singleImageWidth > 0 && singleImageHeight > 0) {
            // 单张图片或视频显示横图样式
            int childWidth = totalWidth;
            // 横图时高为父View的宽*图像的高/图像的宽，竖图时高为父View的宽
            int childHeight = (singleImageWidth > singleImageHeight) ? totalWidth * singleImageHeight / singleImageWidth : totalWidth;
            setMeasuredDimension(totalWidth, childHeight);
            int childWidthSpec = MeasureSpec.makeMeasureSpec(childWidth, MeasureSpec.EXACTLY);
            int childHeightSpec = MeasureSpec.makeMeasureSpec(childHeight, MeasureSpec.EXACTLY);
            singleImageView.measure(childWidthSpec, childHeightSpec);
        } else if (isVideo) {
            // 视频显示规则：默认显示横图样式
            int childWidth = totalWidth;
            int childHeight = totalWidth * customVideoPlayer.getVideoHeight() / customVideoPlayer.getVideoWidth();
            int childWidthSpec = MeasureSpec.makeMeasureSpec(childWidth, MeasureSpec.EXACTLY);
            int childHeightSpec = MeasureSpec.makeMeasureSpec(childHeight, MeasureSpec.EXACTLY);
            setMeasuredDimension(childWidth, childHeight);
            customVideoPlayer.measure(childWidthSpec, childHeightSpec);
        } else {
            int childWidth = (totalWidth - (int) mHorizontalMargin * 2) / 3;
            int totalHeight = (childWidth + (int) mVerticalMargin) * ((getChildCount() + 2) / 3);
            setMeasuredDimension(totalWidth, totalHeight);

            int childWidthSpec = MeasureSpec.makeMeasureSpec(childWidth, MeasureSpec.EXACTLY);
            int childHeightSpec = MeasureSpec.makeMeasureSpec(childWidth, MeasureSpec.EXACTLY);
            for (int i = 0; i < getChildCount(); i++) {
                View child = getChildAt(i);
                if (child.getVisibility() != GONE) {
                    child.measure(childWidthSpec, childHeightSpec);
                }
            }
        }
    }

    @Override
    protected void onLayout(boolean changed, int left, int top, int right, int bottom) {
        int totalWidth = getWidth();
        if (isSingleImage && singleImageWidth > 0 && singleImageHeight > 0) {
            // 布局单张图片
            int childWidth = totalWidth;
            int childHeight = (singleImageWidth > singleImageHeight) ? singleImageView.getMeasuredHeight() : totalWidth;
            singleImageView.layout(0, 0, childWidth, childHeight);
        } else if (isVideo) {
            // 布局视频
            customVideoPlayer.layout(0, 0, customVideoPlayer.getMeasuredWidth(), customVideoPlayer.getMeasuredHeight());
        } else {
            int childWidth = (getWidth() - (int) mHorizontalMargin * 2) / 3;
            int childHeight = childWidth;
            int childLeft;
            int childTop;
            for (int i = 0; i < getChildCount(); i++) {
                View child = getChildAt(i);
                if (child.getVisibility() != GONE) {
                    childLeft = (i % 3) * (childWidth + (int) mHorizontalMargin / 2);
                    childTop = (i / 3) * (childHeight + (int) mVerticalMargin / 2);
                    child.layout(childLeft, childTop, childLeft + childWidth, childTop + childHeight);
                }
            }
        }
    }
}
//        else if (isVideo) {
//            // 布局视频
//            int childWidth = customVideoPlayer.getMeasuredWidth();
//            int childHeight = customVideoPlayer.getMeasuredHeight();
//            customVideoPlayer.layout(0, 0, childWidth, childHeight);
//        } else {
//            // 布局九宫格图片
//            int childWidth = (totalWidth - (int) mHorizontalMargin * 2) / 3;
//            int childHeight = childWidth;
//            int childLeft;
//            int childTop;
//            for (int i = 0; i < getChildCount(); i++) {
//                View child = getChildAt(i);
//                if (child.getVisibility() != GONE) {
//                    childLeft = (i % 3) * (childWidth + (int) mHorizontalMargin / 2);
//                    childTop = (i / 3) * (childHeight + (int) mVerticalMargin / 2);
//                    child.layout(childLeft, childTop, childLeft + childWidth, childTop + childHeight);
//                }
//            }
//        }

//    private void addSingleImage(String url) {
//        singleImageView = new ImageView(getContext());
////        LayoutParams params = new LayoutParams(LayoutParams.MATCH_PARENT, LayoutParams.MATCH_PARENT);
////        imageView.setLayoutParams(params);
////        singleImageView.setScaleType(ImageView.ScaleType.CENTER_CROP);
//        LayoutParams params = new LayoutParams(LayoutParams.MATCH_PARENT, 500);
//        singleImageView.setScaleType(ImageView.ScaleType.FIT_CENTER);
//        singleImageView.setLayoutParams(params);
//
//        Log.i(TAG, "addSingleImage: ");
//        Glide.with(getContext())
//                .load(url)
//                .diskCacheStrategy(DiskCacheStrategy.ALL)
////                .listener(new RequestListener<Drawable>() {
////                    @Override
////                    public boolean onLoadFailed(@Nullable GlideException e, Object model, @NonNull Target<Drawable> target, boolean isFirstResource) {
////                        Log.i(TAG, "addSingleImage onLoadFailed: ");
////                        return false;
////                    }
////
////                    @Override
////                    public boolean onResourceReady(@NonNull Drawable resource, @NonNull Object model, Target<Drawable> target, @NonNull DataSource dataSource, boolean isFirstResource) {
////                        int width = resource.getIntrinsicWidth();
////                        int height = resource.getIntrinsicHeight();
////                        Log.i(TAG, "onResourceReady: " + "width: " + width + " height:" + height);
////                        adjustSingleImageViewLayout(width, height);
////                        return false;
////                    }
////                })
//                .into(singleImageView);
//        addView(singleImageView);
//    }

//    private void addGridImages(List<String> urls) {
//        int imageSize = (getWidth() - (int) mHorizontalMargin * 2) / 3;
//        int gridSize = Math.min(urls.size(), 9); // Limit to 9 images
//
//        for (int i = 0; i < gridSize; i++) {
//            ImageView imageView = new ImageView(getContext());
//            LayoutParams params = new LayoutParams(imageSize, imageSize);
//            params.leftMargin = (i % 3) * (imageSize + (int) mHorizontalMargin / 2);
//            params.topMargin = (i / 3) * (imageSize + (int) mVerticalMargin / 2);
//            imageView.setLayoutParams(params);
//            imageView.setScaleType(ImageView.ScaleType.CENTER_CROP);
//            Glide.with(getContext()).load(urls.get(i)).diskCacheStrategy(DiskCacheStrategy.ALL).into(imageView);
//            addView(imageView);
//        }
//    }

// 加载MediaView中的信息
//    public void setTags(List<String> tags, boolean isPhoto) {
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
//                Glide.with(child.getContext())
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
//    }