package com.example.weibo_sunzhenyu.activity;

import android.annotation.SuppressLint;
import android.os.Build;
import android.os.Bundle;
import android.view.MenuItem;
import android.view.View;
import android.view.Window;
import android.view.WindowManager;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentManager;
import androidx.fragment.app.FragmentTransaction;

import com.example.weibo_sunzhenyu.R;
import com.example.weibo_sunzhenyu.adapter.MyFragmentAdapter;
import com.example.weibo_sunzhenyu.fragment.HomeFragment;
import com.example.weibo_sunzhenyu.fragment.MyPageFragment;
import com.google.android.material.bottomnavigation.BottomNavigationView;

import java.util.ArrayList;
import java.util.List;

public class MainActivity extends AppCompatActivity {
    private BottomNavigationView bottomNavigationView;
    private FragmentManager fragmentManager;
    private HomeFragment fragment1;
    private MyPageFragment fragment2;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
//        // 设置全屏模式
//        getWindow().setFlags(
//                WindowManager.LayoutParams.FLAG_LAYOUT_NO_LIMITS,
//                WindowManager.LayoutParams.FLAG_LAYOUT_NO_LIMITS
//        );
        setContentView(R.layout.activity_main);
        // 设置状态栏的颜色
        Window window = getWindow();
        window.addFlags(WindowManager.LayoutParams.FLAG_DRAWS_SYSTEM_BAR_BACKGROUNDS);
        window.setStatusBarColor(getColor(R.color.deep_blue));

        Toolbar toolbar = findViewById(R.id.toolbar);
        TextView title = findViewById(R.id.title);
        setSupportActionBar(toolbar);

//        ViewPager2 viewPager = findViewById(R.id.view_pager);
        bottomNavigationView = findViewById(R.id.tab_layout);

        // 初始化 BottomNavigationView 和 FragmentManager
        bottomNavigationView = findViewById(R.id.tab_layout);
        fragmentManager = getSupportFragmentManager();

        // 初始时选择第一个 Fragment
        fragment1 = new HomeFragment();
        fragmentManager.beginTransaction()
                .setReorderingAllowed(true)
                .add(R.id.fragment_container, fragment1, null)
                .commit();

        // 设置ToolBar
        getSupportActionBar().setTitle(null);
        getSupportActionBar().setSubtitle(null); // 如果有副标题也一并清空
        getSupportActionBar().setDisplayShowTitleEnabled(false); // 确保标题区域不占据空间
        title.setText("推荐");

        // 设置 BottomNavigationView 的监听器
        bottomNavigationView.setOnNavigationItemSelectedListener(new BottomNavigationView.OnNavigationItemSelectedListener() {
            @SuppressLint("NonConstantResourceId")
            @Override
            public boolean onNavigationItemSelected(@NonNull MenuItem item) {
                //切换时保持Fragment
                if (item.getItemId() == R.id.navigation_home) {
                    // 切换到 fragment1
                    fragmentManager.beginTransaction()
                            .setReorderingAllowed(true)
                            .show(fragment1)
                            .commit();
                    if (fragment2 != null)
                        fragmentManager.beginTransaction()
                                .setReorderingAllowed(true)
                                .hide(fragment2)
                                .commit();
                    title.setText("推荐");
                    return true;
                } else if (item.getItemId() == R.id.navigation_my) {
                    //动态添加MyPageFragment，如果第一次点击则建立一个新的，否则直接显示出来
                    if (fragment2 == null) {
                        fragment2 = new MyPageFragment();
                        fragmentManager.beginTransaction()
                                .setReorderingAllowed(true)
                                .add(R.id.fragment_container, fragment2, null)
                                .commit();
                    } else {
                        // 切换到 fragment2
                        fragmentManager.beginTransaction()
                                .setReorderingAllowed(true)
                                .show(fragment2)
                                .commit();
                    }
                    fragmentManager.beginTransaction()
                            .setReorderingAllowed(true)
                            .hide(fragment1)
                            .commit();
                    title.setText("我的");
                    return true;
                }
                return false;
            }
        });


//        BottomNavigationMenuView menuView = (BottomNavigationMenuView) bottomNavigationView.getChildAt(0);
//
//        for (int i = 0; i < menuView.getChildCount(); i++) {
//            BottomNavigationItemView item = (BottomNavigationItemView) menuView.getChildAt(i);
//            switch (i) {
//                case 0:
//                    item.setTitle("推荐");
//                    break;
//                case 1:
//                    item.setTitle("我的");
//                    break;
//                // ... 其他情况
//            }
//        }
//        new TabLayoutMediator(bottomNavigationView, viewPager, new TabLayoutMediator.TabConfigurationStrategy() {
//            @Override
//            public void onConfigureTab(@NonNull TabLayout.Tab tab, int position) {
//                switch (position) {
//                    case 0:
//                        tab.setText("推荐");
//                        break;
//                    case 1:
//                        tab.setText("我的");
//                        break;
//                }
//            }
//        }).attach();

    }

//    private void showFragment(Fragment fragment) {
//        FragmentTransaction fragmentTransaction = fragmentManager.beginTransaction();
//        fragmentTransaction.replace(R.id.fragment_container, fragment);
//        fragmentTransaction.commit();
//    }
}