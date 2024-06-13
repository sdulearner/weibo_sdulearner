package com.example.weibo_sunzhenyu.fragment;

import android.app.Dialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.text.SpannableString;
import android.text.Spanned;
import android.text.method.LinkMovementMethod;
import android.text.style.ClickableSpan;
import android.view.KeyEvent;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AlertDialog;
import androidx.fragment.app.DialogFragment;

import com.example.weibo_sunzhenyu.R;
import com.example.weibo_sunzhenyu.activity.MainActivity;

public class HelloFragment extends DialogFragment {
    public static String TAG = "HelloFragment";

//    @Nullable
//    @Override
//    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
//        // 加载布局并设置视图
//        View view = inflater.inflate(R.layout.hello_fragment, container, false);
////        getDialog().getWindow().setLayout(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT);
//        // TODO: 2024/6/12 编写DialogFragment的样式
//        // 设置按钮点击监听器
//        view.findViewById(R.id.btn_agree).setOnClickListener(v -> {
//            // 保存用户同意状态
//            saveUserAgreement();
//            dismiss(); // 关闭弹窗
//            // 跳转到首页
//            Intent intent = new Intent(view.getContext(), MainActivity.class);
//            startActivity(intent);
//        });
//        view.findViewById(R.id.btn_disagree).setOnClickListener(v -> {
//            // 用户不同意，退出应用
//            requireActivity().finishAffinity(); // 如果在Activity中，可以结束当前Activity及其上的所有Activity
//            dismiss(); // 关闭弹窗
//        });
//
//        TextView tv_title = view.findViewById(R.id.tv_title);
//        TextView tv_message = view.findViewById(R.id.tv_message);
//        SpannableString spannableString = new SpannableString("欢迎使用iH微博，" +
//                "我们将严格遵守相关法律和隐私政策保护您的个人隐私，请您阅读并同意《用户协议》与《隐私政策》");
//        ClickableSpan userAgreementSpan = new ClickableSpan() {
//            @Override
//            public void onClick(View widget) {
//                Toast.makeText(getContext(), "查看⽤⼾协议", Toast.LENGTH_SHORT).show();
//            }
//        };
//        ClickableSpan privacyPolicySpan = new ClickableSpan() {
//            @Override
//            public void onClick(@NonNull View widget) {
//                Toast.makeText(getContext(), "查看隐私政策", Toast.LENGTH_SHORT).show();
//            }
//        };
//
//        // 设置用户协议的Span
//        int start = 40;//文本中“用户协议”的起始位置
//        int end = 46;//文本中“用户协议”的结束位置
//        spannableString.setSpan(userAgreementSpan, start, end, Spanned.SPAN_EXCLUSIVE_EXCLUSIVE);
//
//        // 同样设置“隐私政策”的Span
//        start = 48;//文本中“用户协议”的起始位置
//        end = 54;//文本中“用户协议”的结束位置
//        spannableString.setSpan(privacyPolicySpan, start, end, Spanned.SPAN_EXCLUSIVE_EXCLUSIVE);
//
//        //在点击链接时，有执行的动作，都必须设置 MovementMethod对象
//        tv_message.setMovementMethod(LinkMovementMethod.getInstance());
//        tv_message.setText(spannableString);
//
//
//        return view;
//    }

    private void saveUserAgreement() {
        // 使用SharedPreferences保存用户同意状态
        SharedPreferences preferences = requireActivity().getSharedPreferences("user_pref", Context.MODE_PRIVATE);
        SharedPreferences.Editor editor = preferences.edit();
        editor.putBoolean("user_agreed", true);
        editor.apply();
    }


    // 设置自定义动画（可选）
    @Override
    public void onStart() {
        super.onStart();
        Dialog dialog = getDialog();
        if (dialog != null) {
//            dialog.getWindow().getAttributes().windowAnimations = R.style.DialogAnimation;
        }
    }

    @NonNull
    @Override
    public Dialog onCreateDialog(@Nullable Bundle savedInstanceState) {

        View view = LayoutInflater.from(requireActivity()).inflate(R.layout.hello_fragment, null);
        // 设置按钮点击监听器
        view.findViewById(R.id.btn_agree).setOnClickListener(v -> {
            // 保存用户同意状态
            saveUserAgreement();
            dismiss(); // 关闭弹窗
            // 跳转到首页
            Intent intent = new Intent(view.getContext(), MainActivity.class);
            startActivity(intent);
            // 销毁HelloActivity
            requireActivity().finish();
        });
        view.findViewById(R.id.btn_disagree).setOnClickListener(v -> {
            // 用户不同意，退出应用
            requireActivity().finishAffinity(); // 如果在Activity中，可以结束当前Activity及其上的所有Activity
            dismiss(); // 关闭弹窗
        });

        TextView tv_title = view.findViewById(R.id.tv_title);
        TextView tv_message = view.findViewById(R.id.tv_message);
        SpannableString spannableString = new SpannableString("欢迎使用iH微博，" +
                "我们将严格遵守相关法律和隐私政策保护您的个人隐私，请您阅读并同意《用户协议》与《隐私政策》");
        ClickableSpan userAgreementSpan = new ClickableSpan() {
            @Override
            public void onClick(View widget) {
                Toast.makeText(getContext(), "查看⽤⼾协议", Toast.LENGTH_SHORT).show();
            }
        };
        ClickableSpan privacyPolicySpan = new ClickableSpan() {
            @Override
            public void onClick(@NonNull View widget) {
                Toast.makeText(getContext(), "查看隐私政策", Toast.LENGTH_SHORT).show();
            }
        };
        // TODO: 2024/6/13 设置链接字体为蓝色， 设置两个按钮点击改变颜色
        // 设置用户协议的Span
        int start = 41;//文本中“用户协议”的起始位置
        int end = 47;//文本中“用户协议”的结束位置
        spannableString.setSpan(userAgreementSpan, start, end, Spanned.SPAN_EXCLUSIVE_EXCLUSIVE);

        // 同样设置“隐私政策”的Span
        start = 48;//文本中“用户协议”的起始位置
        end = 54;//文本中“用户协议”的结束位置
        spannableString.setSpan(privacyPolicySpan, start, end, Spanned.SPAN_EXCLUSIVE_EXCLUSIVE);

        //在点击链接时，有执行的动作，都必须设置 MovementMethod对象
        tv_message.setMovementMethod(LinkMovementMethod.getInstance());
        tv_message.setText(spannableString);

        AlertDialog.Builder builder = new AlertDialog.Builder(requireActivity());
        builder.setView(view);
        Dialog dialog = builder.create();
        // 当用户按下返回键时，你可以根据需要处理，比如弹出提示或者直接不关闭对话框
        dialog.setOnKeyListener(new DialogInterface.OnKeyListener() {
            @Override
            public boolean onKey(DialogInterface dialog, int keyCode, KeyEvent event) {
                if (keyCode == KeyEvent.KEYCODE_BACK && event.getAction() == KeyEvent.ACTION_UP) {
                    Toast.makeText(getActivity(), "请先做出选择！", Toast.LENGTH_SHORT).show();
                    return true; // 返回true表示消费了这个事件，即不关闭对话框
                }
                return false;
            }
        });

        // 设置对话框背景为圆角矩形
        dialog.getWindow().setBackgroundDrawableResource(R.drawable.rounded_background);
        // todo:设置宽度为屏幕宽度的80%，你可以根据需要调整这个比例
//        int width = (int) (getResources().getDisplayMetrics().widthPixels * 0.5f); // 80%的屏幕宽度
//        dialog.getWindow().setLayout(width, ViewGroup.LayoutParams.WRAP_CONTENT); // 设置宽度，高度根据内容自适应

        return dialog;
//        return new AlertDialog.Builder(requireContext())
//                .setTitle("声明与条款")
//                .setMessage("欢迎使用iH微博，" +
//                        "我们将严格遵守相关法律和隐私政策保护您的个人隐私，请您阅读并同意《用户协议》与《隐私政策》")
//                .setNegativeButton("不同意", (dialog, which) -> {
//                    // 用户不同意，退出应用
//                    requireActivity().finishAffinity(); // 如果在Activity中，可以结束当前Activity及其上的所有Activity
//                    dismiss(); // 关闭弹窗
//                })
//                .setPositiveButton("同意并使用", new DialogInterface.OnClickListener() {
//                    @Override
//                    public void onClick(DialogInterface dialog, int which) {
//                        // 保存用户同意状态
//                        saveUserAgreement();
//                        dismiss(); // 关闭弹窗
//                        // 跳转到首页
//                        Intent intent = new Intent(getActivity(), MainActivity.class);
//                        startActivity(intent);
//                        // 销毁HelloActivity
//                        requireActivity().finish();
//                    }
//                })
//                .create();
    }
}
