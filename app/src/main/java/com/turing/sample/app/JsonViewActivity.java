package com.turing.sample.app;

import android.os.Bundle;
import android.text.TextUtils;

import com.turing.sample.R;
import com.turing.sample.app.base.BaseActivity;
import com.yuyh.jsonviewer.library.JsonRecyclerView;

import androidx.annotation.Nullable;

public class JsonViewActivity extends BaseActivity {

    private JsonRecyclerView mRecyclewView;

    @Override
    protected void onDestroy() {
        super.onDestroy();
    }

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_jsonview);
        mRecyclewView = findViewById(R.id.rv_json);
        mRecyclewView.setTextSize(14);
        String json = getIntent().getStringExtra("json");
        if(!TextUtils.isEmpty(json)){
            mRecyclewView.bindJson(json);
        }
    }
}
