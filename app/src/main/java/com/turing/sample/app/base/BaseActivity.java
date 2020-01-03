package com.turing.sample.app.base;

import android.app.Activity;
import android.content.Context;
import android.os.Bundle;
import android.os.Handler;
import android.util.Log;
import android.view.View;
import android.widget.Toast;

import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;

import com.turing.os.init.UserData;
import com.turing.sample.R;

import java.lang.ref.WeakReference;

/**
 * created by yihuapeng
 * on 2019/11/7
 */
public class BaseActivity extends AppCompatActivity {

    protected Context mContext;
    protected Activity mActivity;
    protected UserData userData;
    protected UIHandler mUIHandler = null;
    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        mContext = this;
        mActivity = this;
        mUIHandler = new UIHandler(mActivity);
    }

    public void showTost(String text){
        Toast.makeText(this, text, Toast.LENGTH_SHORT).show();

    }
    public void fullScreen(){
        setTheme(R.style.FullScreenTheme);
        hideNavigationBar();
    }
    public void hideNavigationBar() {
        int uiFlags = View.SYSTEM_UI_FLAG_LAYOUT_STABLE
                | View.SYSTEM_UI_FLAG_LAYOUT_HIDE_NAVIGATION
                | View.SYSTEM_UI_FLAG_LAYOUT_FULLSCREEN
                | View.SYSTEM_UI_FLAG_HIDE_NAVIGATION // hide nav bar
                | View.SYSTEM_UI_FLAG_FULLSCREEN; // hide status bar

        if (android.os.Build.VERSION.SDK_INT >= 19) {
            uiFlags |= View.SYSTEM_UI_FLAG_IMMERSIVE;//0x00001000; // SYSTEM_UI_FLAG_IMMERSIVE_STICKY: hide
        } else {
            uiFlags |= View.SYSTEM_UI_FLAG_LOW_PROFILE;
        }

        try {
            getWindow().getDecorView().setSystemUiVisibility(uiFlags);
        } catch (Exception e) {
            // TODO: handle exception
        }

    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        mActivity = null;
    }

    public static class UIHandler extends Handler {
        private final WeakReference<Activity> mActivity;
        public UIHandler(Activity activity) {
            mActivity = new WeakReference<Activity>(activity);
        }
        public void postRunnable(Runnable runnable){
            if(mActivity != null){
                this.post(runnable);
            }
        }
    }
}
