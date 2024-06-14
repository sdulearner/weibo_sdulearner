package com.example.weibo_sunzhenyu.utils;

import android.content.Context;
import android.content.SharedPreferences;

public class Utils {
    // 使用SharedPreferences保存用户登录状态
    public static void saveUserLoginToken(Context context, String token) {
        SharedPreferences preferences = context.getSharedPreferences("user_pref", Context.MODE_PRIVATE);
        SharedPreferences.Editor editor = preferences.edit();
        editor.putString("login_token", token);
        editor.apply();
    }

    public static String getBearerToken(Context context) {
        SharedPreferences preferences = context.getSharedPreferences("user_pref", Context.MODE_PRIVATE);
        String token = preferences.getString("login_token", "");
        return "Bearer " + token;
    }
}
