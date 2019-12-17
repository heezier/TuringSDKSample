package com.turing.sample.ai.chat;

import android.content.Intent;
import android.os.Bundle;
import android.text.TextUtils;
import android.util.Log;
import android.view.View;
import android.widget.TextView;

import androidx.annotation.Nullable;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;


import com.turing.os.client.TuringOSClient;
import com.turing.os.client.TuringOSClientAsrListener;
import com.turing.os.client.TuringOSClientListener;
import com.turing.os.init.UserData;

import com.turing.os.log.TuringErrorCode;
import com.turing.os.player.TtsPlayerPool;
import com.turing.os.request.bean.ResponBean;
import com.turing.sample.R;
import com.turing.sample.app.JsonViewActivity;
import com.turing.sample.app.base.BaseActivity;
import com.turing.sample.view.BottomLayout;

import java.util.ArrayList;
import java.util.List;

import butterknife.BindView;
import butterknife.ButterKnife;

public class ChatActivity extends BaseActivity {
    private final static String TAG = "ChatActivity";

    @BindView(R.id.base_bottom)
    BottomLayout bottomLayout;
    @BindView(R.id.tv_results_hint)
    TextView tvResultsHint;

    TuringOSClient client;
    UserData userData;
    @BindView(R.id.tv_timer)
    TextView tvTimer;
    private List<Msg> msgList = new ArrayList<>();
    private RecyclerView msgRecyclerView;
    private MsgAdapter adapter;

    private List<String> resultList = new ArrayList<>();
    private RecyclerView resultRecyclerView;
    private ResultAdapter resultAdapter;
    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_chat);
        ButterKnife.bind(this);
        init();
    }

    private void init() {
        userData = (UserData) getIntent().getSerializableExtra("userdata");
        tvTimer.setVisibility(View.GONE);
        client = TuringOSClient.getInstance(this, userData);

        msgRecyclerView =(RecyclerView)findViewById(R.id.msg_recycleview);
        LinearLayoutManager layoutManager=new LinearLayoutManager(this);
        msgRecyclerView.setLayoutManager(layoutManager);
        adapter = new MsgAdapter(msgList);
        msgRecyclerView.setAdapter(adapter);

        resultRecyclerView =(RecyclerView)findViewById(R.id.result_recycleview);
        LinearLayoutManager resultlayoutManager=new LinearLayoutManager(this);
        resultRecyclerView.setLayoutManager(resultlayoutManager);
        resultAdapter = new ResultAdapter(resultList);
        resultRecyclerView.setAdapter(resultAdapter);
        resultAdapter.setOnItemClickListener(new ResultAdapter.OnItemClickListener() {
            @Override
            public void onItemClick(View view, int position) {
                Intent intent = new Intent(mContext, JsonViewActivity.class);
                intent.putExtra("json", resultList.get(position));
                mContext.startActivity(intent);
            }
        });

        bottomLayout.setCallback(new BottomLayout.ActionCallback() {
            @Override
            public void onActionRecordStart() {
                startRecordChat();
            }

            @Override
            public void onActionRecordStop() {
                stopRecordChat();
            }

            @Override
            public void onActionText(String str) {
                startTextChat(str);
            }
        });
    }

    private void stopRecordChat() {
        tvTimer.setVisibility(View.GONE);
        client.stopChat();
    }

    private void addSendMsg(){
        mUIHandler.postRunnable(new Runnable() {
            @Override
            public void run() {
                msgList.add(new Msg("语音识别中...", Msg.SENT));
                int newSize = msgList.size() - 1;
                adapter.notifyItemInserted(newSize);
                msgRecyclerView.scrollToPosition(newSize);
            }
        });

    }
    private void startRecordChat() {
        client.startChatWithRecord(new TuringOSClientAsrListener() {


            @Override
            public void onRecorderStart() {
                addSendMsg();
            }

            @Override
            public void onStop() {

            }

            @Override
            public void onStreamOpen() {

            }


            @Override
            public void onResult(int code, String result, boolean isLast, ResponBean responBean) {
                mUIHandler.postRunnable(new Runnable() {
                    @Override
                    public void run() {
                        tvTimer.setVisibility(View.GONE);
                        resultList.add(result);
                        resultAdapter.updateList(resultList);
                        resultRecyclerView.smoothScrollToPosition(resultList.size() - 1);
                        if(code == TuringErrorCode.WEBSOCKET_200){
                            handlerResult(responBean);
                        }
                    }
                });
            }

            @Override
            public void onTimer(int second) {
                //聊天中 单轮输入交互不能超过30s
                mUIHandler.postRunnable(new Runnable() {
                    @Override
                    public void run() {
                        tvTimer.setText(second + " s");
                        tvTimer.setVisibility(View.VISIBLE);
                        if(second <= 0) {
                            tvTimer.setVisibility(View.GONE);
                            showTost("录音超市已取消！");
                        }
                    }
                });
            }

            @Override
            public void onError(int code, String msg) {
                Log.e(TAG, "code: " + code + " msg : " + msg);
                mUIHandler.postRunnable(new Runnable() {
                    @Override
                    public void run() {
                        tvTimer.setVisibility(View.GONE);
                    }
                });

            }
        });
    }

    private void startTextChat(String text) {
        long time = System.currentTimeMillis();
        client.actionChat(text, new TuringOSClientListener() {
            @Override
            public void onResult(int code, String result, ResponBean responBean) {
                long diff = System.currentTimeMillis() - time;
                Log.v(TAG, "diff:" + diff);
                mUIHandler.postRunnable(new Runnable() {
                    @Override
                    public void run() {
                        resultList.add(result);
                        resultAdapter.updateList(resultList);
                        handlerResult(responBean);
                    }
                });
            }

            @Override
            public void onError(int code, String msg) {
                Log.e(TAG, "code: " + code + " msg: " + msg);
            }
        });
        msgList.add(new Msg(text, Msg.SENT));
        int newSize = msgList.size() - 1;
        adapter.notifyItemInserted(newSize);
        msgRecyclerView.scrollToPosition(newSize);
    }

    private void handlerResult(ResponBean responBean){
        if(responBean != null && responBean.getAsrResponse() != null){
            String asrResult = responBean.getAsrResponse().getValue();
            int index = 0;
            if(msgList.size() >= 1){
                index = msgList.size() - 1;
            }
            msgList.remove(index);
            if(!TextUtils.isEmpty(asrResult)){
                msgList.add(new Msg(asrResult, Msg.SENT));
            }else{
                msgList.add(new Msg("你没有说话", Msg.SENT));
            }
            adapter.updateList(msgList);
        }
        if(responBean != null && responBean.getNlpResponse() != null
                && responBean.getNlpResponse().getResults() != null){

            List<ResponBean.NlpResponseBean.ResultsBean> resultsBeanList = responBean.getNlpResponse().getResults();
            ResponBean.NlpResponseBean.ResultsBean.ValuesBean valuesBean = resultsBeanList.get(0).getValues();
            if(valuesBean != null){
                String content = valuesBean.getText();
                String voiceUrl = valuesBean.getTtsUrl().get(0);
                playUrl(voiceUrl);
                msgList.add(new Msg(content, Msg.RECEIVED));
//                adapter.updateList(msgList);
                int newSize = msgList.size() - 1;
                adapter.notifyItemChanged(newSize);
                msgRecyclerView.scrollToPosition(newSize);
            }
        }else{

        }
    }

    private void playUrl(String url){
        if(TextUtils.isEmpty(url)) return;
        TtsPlayerPool ttsPlayerPool = TtsPlayerPool.create(1);
        ttsPlayerPool.addPlayUrl(url);
        ttsPlayerPool.start();
    }
}
