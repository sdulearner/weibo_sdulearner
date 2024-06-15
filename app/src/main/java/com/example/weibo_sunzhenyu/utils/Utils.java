package com.example.weibo_sunzhenyu.utils;

import android.content.Context;
import android.content.SharedPreferences;

import com.bumptech.glide.Glide;

public class Utils {
    // 使用SharedPreferences保存用户登录状态
    public static void saveUserLoginToken(Context context, String token) {
        SharedPreferences preferences = context.getSharedPreferences("user_pref", Context.MODE_PRIVATE);
        SharedPreferences.Editor editor = preferences.edit();
        editor.putString("login_token", token);
        editor.apply();
    }

    // 退出登录或token过期时清除token
    public static void clearToken(Context context) {
        SharedPreferences preferences = context.getSharedPreferences("user_pref", Context.MODE_PRIVATE);
        SharedPreferences.Editor editor = preferences.edit();
        editor.putString("login_token", "");
        editor.apply();
        // 清除Glide缓存: 感觉没有必要，先不做这一步了
        // Glide.get(context).clearMemory();
    }

    // 获取Bearer Token
    public static String getBearerToken(Context context) {
        SharedPreferences preferences = context.getSharedPreferences("user_pref", Context.MODE_PRIVATE);
        String token = preferences.getString("login_token", "");
        return "Bearer " + token;
    }
}
