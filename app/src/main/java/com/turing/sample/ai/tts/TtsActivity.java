package com.turing.sample.ai.tts;

import android.os.Bundle;
import android.text.TextUtils;
import android.util.Log;
import android.util.SparseArray;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;

import androidx.annotation.Nullable;

import com.turing.os.client.TuringOSClient;
import com.turing.os.client.TuringOSClientListener;
import com.turing.os.init.UserData;

import com.turing.os.player.TtsPlayerPool;
import com.turing.os.request.bean.ResponBean;

import com.turing.sample.R;
import com.turing.sample.app.base.BaseActivity;

import java.util.List;

import butterknife.BindView;
import butterknife.ButterKnife;
import butterknife.OnClick;

public class TtsActivity extends BaseActivity {
    private final static String TAG = "ChatActivity";
    @BindView(R.id.et_input)
    EditText etInput;
    @BindView(R.id.btn_start_nlp)
    Button btnStartNlp;
    @BindView(R.id.btn_start_play)
    Button btnStartPlay;
    @BindView(R.id.btn_stop)
    Button btnStop;
    @BindView(R.id.tv_result)
    TextView tvResult;


    private TuringOSClient client;
    private TtsPlayerPool ttsPlayerPool;
    private StringBuilder results;


    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_tts);
        ButterKnife.bind(this);
        init();
    }
    private void init() {
        userData = (UserData) getIntent().getSerializableExtra("userdata");
        client = TuringOSClient.getInstance(mContext, userData);
        btnStartNlp.setText(getString(R.string.tts_start));
    }


    private void startTts() {
        String text = etInput.getText().toString();
        if (TextUtils.isEmpty(text)) {
            showTost("输入为空！");
            return;
        }
        //针对较长的字符串，将会切割返回
        SparseArray<String> list = client.split(text);
        for(int i = 0; i < list.size(); i++){
            Log.d(TAG, list.get(i).length() + "==========" + list.get(i));
        }
        ttsPlayerPool = TtsPlayerPool.create(list.size());
        results =  new StringBuilder();

        client.actionTts(text, new TuringOSClientListener() {
            @Override
            public void onResult(int code, String result, ResponBean responBean) {
                if(responBean != null && responBean.getNlpResponse() != null
                        && responBean.getNlpResponse().getResults() != null){
                    List<ResponBean.NlpResponseBean.ResultsBean> resultsBeanList = responBean.getNlpResponse().getResults();
                    ResponBean.NlpResponseBean.ResultsBean.ValuesBean valuesBean = resultsBeanList.get(0).getValues();
                    if(valuesBean != null){
                        String url = valuesBean.getTtsUrl().get(0);
                        if(!TextUtils.isEmpty(url)){
                            ttsPlayerPool.addPlayUrl(url);
                        }
                        results.append(cutUrl(url));
                    }
                }
                mUIHandler.postRunnable(new Runnable() {
                    @Override
                    public void run() {
                        tvResult.setText(results);
                    }
                });
            }

            @Override
            public void onError(int code, String msg) {

            }
        });


    }


    @OnClick({R.id.btn_start_nlp, R.id.btn_start_play, R.id.btn_stop})
    public void onViewClicked(View view) {
        switch (view.getId()) {
            case R.id.btn_start_nlp:
                startTts();
                break;
            case R.id.btn_start_play:
                startPlay();
                break;
            case R.id.btn_stop:
                stopPlay();
                break;
        }
    }

    private void startPlay(){
        if(ttsPlayerPool != null){
            ttsPlayerPool.start();
            btnStop.setEnabled(true);
        }
    }

    private void stopPlay(){
        if(ttsPlayerPool != null){
            ttsPlayerPool.stop();
            btnStartPlay.setEnabled(true);
        }
    }

    private String cutUrl(String url){
        if(!TextUtils.isEmpty(url)){
            return url.substring(0, 40) + "...\n";
        }
        return null;
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        stopPlay();
    }
}
