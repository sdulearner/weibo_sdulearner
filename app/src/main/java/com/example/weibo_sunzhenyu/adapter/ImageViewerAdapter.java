package com.example.weibo_sunzhenyu.adapter;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.Glide;
import com.example.weibo_sunzhenyu.R;

import java.util.List;

public class ImageViewerAdapter extends RecyclerView.Adapter<ImageViewerAdapter.ViewHolder> {

    private final List<String> imageUrls;
    private View.OnClickListener onClickListener;

    public ImageViewerAdapter(List<String> imageUrls) {
        this.imageUrls = imageUrls;
    }

    public void setOnClickListener(View.OnClickListener onClickListener) {
        this.onClickListener = onClickListener;
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_image_viewer, parent, false);
        return new ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
        // TODO: 2024/6/16 图片以宽度为基础，高度自适应；高度小于屏幕高度则上下居中
        Glide.with(holder.itemView.getContext()).load(imageUrls.get(position)).into(holder.imageView);
        
        // 设置图片的点击事件监听器
        holder.itemView.setOnClickListener(onClickListener);
    }

    @Override
    public int getItemCount() {
        return imageUrls.size();
    }

    static class ViewHolder extends RecyclerView.ViewHolder {
        ImageView imageView;

        ViewHolder(@NonNull View itemView) {
            super(itemView);
            imageView = itemView.findViewById(R.id.imageView);
        }
    }
}
