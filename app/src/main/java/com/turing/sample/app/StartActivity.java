package com.turing.sample.app;

import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;

import com.turing.sample.R;
import com.turing.sample.app.base.BaseActivity;

/**
 * created by yihuapeng
 * on 2019/11/7
 */
public class StartActivity extends BaseActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        fullScreen();
        setTheme(R.style.FullScreenTheme);
        setContentView(R.layout.activity_start);
        new Handler().postDelayed(new Runnable() {
            @Override
            public void run() {
                startLoginActivity();
            }
        }, 2000);
    }

    /**
     * 进入主页面
     */
    private void startLoginActivity() {
        Intent intent = new Intent(this, MainActivity.class);
        startActivity(intent);
        finish();

    }

}
