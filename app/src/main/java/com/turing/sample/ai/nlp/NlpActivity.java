package com.turing.sample.ai.nlp;

import android.os.Bundle;
import android.text.TextUtils;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;

import androidx.annotation.Nullable;

import com.turing.os.client.TuringOSClient;
import com.turing.os.client.TuringOSClientListener;
import com.turing.os.init.UserData;
import com.turing.os.request.bean.AppStateBean;
import com.turing.os.request.bean.NlpRequestConfig;
import com.turing.os.request.bean.ResponBean;
import com.turing.sample.R;
import com.turing.sample.app.base.BaseActivity;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import butterknife.BindView;
import butterknife.ButterKnife;
import butterknife.OnClick;

public class NlpActivity extends BaseActivity {
    private final static String TAG = "ChatActivity";
    @BindView(R.id.et_input)
    EditText etInput;
    @BindView(R.id.btn_start)
    Button btnStart;
    @BindView(R.id.tv_result)
    TextView tvResult;
    @BindView(R.id.et_semantic_robot_id)
    EditText etSemanticRobotId;
    @BindView(R.id.et_semantic_robot_data)
    EditText etSemanticRobotData;
    @BindView(R.id.et_semantic_request_extra_codes)
    EditText etSemanticRequestExtraCodes;
    @BindView(R.id.btn_request_first)
    Button btnRequestFirst;
    @BindView(R.id.btn_request_auto)
    Button btnRequestAuto;

    private TuringOSClient client;
    private AppStateBean appStateBean;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_nlp);
        ButterKnife.bind(this);
        init();
    }


    private void init() {
        userData = (UserData) getIntent().getSerializableExtra("userdata");
        client = TuringOSClient.getInstance(mContext, userData);
        btnStart.setText(getString(R.string.nlp_start));
        appStateBean = new AppStateBean();
        appStateBean.setOperateState(0);
        appStateBean.setCode(0);
    }


    private void startNlp() {
        String text = etInput.getText().toString();
        if (TextUtils.isEmpty(text)) {
            showTost("输入为空！");
            return;
        }
        NlpRequestConfig.Builder builder = new NlpRequestConfig.Builder();
        builder.appStateBean(appStateBean);

        String requestCode = etSemanticRequestExtraCodes.getText().toString();
        if (!TextUtils.isEmpty(requestCode)) {
            String[] codes = requestCode.split(";");
            List<Integer> userCodes = new ArrayList<>();
            for (String code : codes) {
                userCodes.add(Integer.valueOf(code));
            }
            builder.codes(userCodes);
        }

        String robotId = etSemanticRobotId.getText().toString();
        String robotData = etSemanticRobotData.getText().toString();
        if (!TextUtils.isEmpty(robotId) && !TextUtils.isEmpty(robotData)) {
            String[] codes = robotId.split(";");
            String[] datas = robotData.split("\\|");
            if (codes.length == datas.length) {
                Map<String, Map<String, Object>> customParams = new HashMap<>();
                for (int i = 0; i < codes.length; i++) {
                    Map<String, Object> dataMap = new HashMap<>();
                    String[] data = datas[i].split(";");
                    for (String d : data) {
                        String[] values = d.split(":");
                        dataMap.put(values[0], values[1]);
                    }
                    customParams.put(codes[i], dataMap);
                }
                builder.robotSkills(customParams);
            }
        }
        NlpRequestConfig requestConfig = builder.build();

        client.actionNlp(text, requestConfig, new TuringOSClientListener() {
            @Override
            public void onResult(int code, String result, ResponBean responBean) {
                mUIHandler.postRunnable(new Runnable() {
                    @Override
                    public void run() {
                        if (responBean != null && responBean.getNlpResponse() != null && responBean.getNlpResponse().getIntent() != null) {
                            ResponBean.NlpResponseBean.IntentBean intentBean = responBean.getNlpResponse().getIntent();
                            appStateBean.setCode(intentBean.getCode());
                            appStateBean.setOperateState(intentBean.getOperateState());
                        }
                        tvResult.setText(result);
                    }
                });

            }

            @Override
            public void onError(int code, String msg) {

            }
        });
    }


    @OnClick({R.id.btn_request_first, R.id.btn_request_auto, R.id.btn_start})
    public void onViewClicked(View view) {
        switch (view.getId()) {
            case R.id.btn_start:
                startNlp();
                break;
            case R.id.btn_request_first:
                if(client != null){
                    client.actionFirstConversion(new TuringOSClientListener() {
                        @Override
                        public void onResult(int code, String result, ResponBean responBean) {
                            mUIHandler.postRunnable(new Runnable() {
                                @Override
                                public void run() {
                                    tvResult.setText(result);
                                }
                            });
                        }

                        @Override
                        public void onError(int code, String msg) {
                            mUIHandler.postRunnable(new Runnable() {
                                @Override
                                public void run() {
                                    tvResult.setText(code + msg);
                                }
                            });
                        }
                    });
                }
                break;
            case R.id.btn_request_auto:
                if(client != null){
                    client.actionAutoConversion(new TuringOSClientListener() {
                        @Override
                        public void onResult(int code, String result, ResponBean responBean) {
                            mUIHandler.postRunnable(new Runnable() {
                                @Override
                                public void run() {
                                    tvResult.setText(result);
                                }
                            });
                        }

                        @Override
                        public void onError(int code, String msg) {
                            mUIHandler.postRunnable(new Runnable() {
                                @Override
                                public void run() {
                                    tvResult.setText(code + msg);
                                }
                            });
                        }
                    });
                }
                break;
        }
    }
}
