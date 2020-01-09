package com.turing.sample.ai.asr;

import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;

import androidx.annotation.Nullable;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.turing.opus.OpusEncoder;
import com.turing.opus.OpusHelper;
import com.turing.os.client.TuringOSClient;
import com.turing.os.client.TuringOSClientAsrListener;
import com.turing.os.init.UserData;
import com.turing.os.request.bean.AsrRequestConfig;
import com.turing.os.request.bean.ResponBean;
import com.turing.os.voiceprocessor.codec.BytesTransform;
import com.turing.sample.R;
import com.turing.sample.app.JsonViewActivity;
import com.turing.sample.app.base.BaseActivity;

import java.io.InputStream;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import butterknife.BindView;
import butterknife.ButterKnife;
import butterknife.OnClick;

public class AsrActivity extends BaseActivity {
    private final static String TAG = "AsrActivity";

    UserData userData;
    TuringOSClient turingOSClient;


    @BindView(R.id.tv_withrecord)
    TextView tvWithrecord;
    @BindView(R.id.btn_start)
    Button btnStart;
    @BindView(R.id.btn_stop)
    Button btnStop;
    @BindView(R.id.btn_stream_start)
    Button btnStreamStart;

    @BindView(R.id.tv_results_hint)
    TextView tvResultsHint;
    @BindView(R.id.btn_code_start)
    Button btnCodeStart;


    private final static int MODE_PCM = 0;
    private final static int MODE_RECORD = 1;
    private final static int MODE_CODE_OPUS = 2;
    private final static int MODE_CODE_SPEEX = 4;
    private final static int MODE_NONE = 3;
    @BindView(R.id.btn_stop_pcm)
    Button btnStopPcm;
    @BindView(R.id.btn_stop_opus)
    Button btnStopOpus;
    @BindView(R.id.result_recycleview)
    RecyclerView resultRecycleview;
    @BindView(R.id.tv_result)
    TextView tvResult;
    @BindView(R.id.btn_speex_start)
    Button btnSpeexStart;
    @BindView(R.id.btn_speex_stop)
    Button btnSpeexStop;

    private List<AsrResult> resultList = new ArrayList<>();
    private AsrResultAdapter resultAdapter;

    private volatile boolean isPcmStop = false;
    private volatile boolean isEncodeStop = false;

    private static final int SAMPLE_RATE = 16000;
    private static final int CHANNELS = 1;
    private static final int FRAME_SIZE = OpusHelper.getFrameSize(SAMPLE_RATE, OpusHelper.TimePerFrame.Time_20);
    private OpusEncoder opusEncoder = new OpusEncoder();

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_asr);
        ButterKnife.bind(this);
        userData = (UserData) getIntent().getSerializableExtra("userdata");
        updateUI(MODE_NONE);
        initResultView();
    }

    private void initResultView() {
        resultRecycleview = (RecyclerView) findViewById(R.id.result_recycleview);
        LinearLayoutManager resultlayoutManager = new LinearLayoutManager(this);
        resultRecycleview.setLayoutManager(resultlayoutManager);
        resultAdapter = new AsrResultAdapter(resultList);
        resultRecycleview.setAdapter(resultAdapter);
        resultAdapter.setOnItemClickListener(new AsrResultAdapter.OnItemClickListener() {
            @Override
            public void onItemClick(View view, int position) {
                Intent intent = new Intent(mContext, JsonViewActivity.class);
                intent.putExtra("json", resultList.get(position).getResult());
                mContext.startActivity(intent);
            }
        });
    }

    private void startEncodeStream(final @AsrRequestConfig.AsrFormatEnum  int type) {
        AsrRequestConfig.Builder builder = new AsrRequestConfig.Builder();
        builder.asrLanguageEnum(AsrRequestConfig.CHINESE);
        //必须与传入可支持的格式对应，比如你将要传入opus格式，则该参数必须设置为OPUS
        builder.asrFormatEnum(type);
        builder.asrSrcFormatEnum(type);
        builder.asrRateEnum(AsrRequestConfig.RATE_16000);
        builder.enablePunctuation(false);
        builder.maxEndSilence(3000);
        /**
         * CHANNEL_IN_MONO或者CHANNEL_IN_STEREO
         */
        builder.channel(AsrRequestConfig.CHANNEL_IN_MONO);

        AsrRequestConfig asrRequestConfig = builder.build();
        turingOSClient = TuringOSClient.getInstance(this, userData);
        // turingOSClient.initAsrStream为ASR模式  turingOSClient.initChatStream为聊天模式
        turingOSClient.initChatStream(true, asrRequestConfig, null, new TuringOSClientAsrListener() {

            @Override
            public void onRecorderStart() {

            }

            @Override
            public void onStop() {
                Log.e(TAG, "===========onStop========");
                isEncodeStop = true;
                mUIHandler.postRunnable(new Runnable() {
                    @Override
                    public void run() {
                        updateUI(MODE_NONE);
                    }
                });

            }

            @Override
            public void onStreamOpen() {
                isEncodeStop = false;
                startStreamEncodeInput(type);
            }


            @Override
            public void onResult(int code, String result, boolean isLast, ResponBean responBean) {
                mUIHandler.postRunnable(new Runnable() {
                    @Override
                    public void run() {
                        updateResult(code, result, responBean);
                    }
                });
            }

            @Override
            public void onTimer(int second) {

            }

            @Override
            public void onError(int code, String msg) {
                Log.e(TAG, "onError  code: " + code + "  msg:" + msg);
                isEncodeStop = true;
                mUIHandler.postRunnable(new Runnable() {
                    @Override
                    public void run() {
                        updateResult(code, msg, null);
                        updateUI(MODE_NONE);
                    }
                });
            }
        });
        if(type == AsrRequestConfig.OPUS){
            updateUI(MODE_CODE_OPUS);
        }else if(type == AsrRequestConfig.SPEEX){
            updateUI(MODE_CODE_SPEEX);
        }

    }

    private void startPCMStream() {
        AsrRequestConfig.Builder builder = new AsrRequestConfig.Builder();
        builder.asrLanguageEnum(AsrRequestConfig.CHINESE);
        builder.asrFormatEnum(AsrRequestConfig.PCM);
        builder.asrSrcFormatEnum(AsrRequestConfig.PCM);
        builder.asrRateEnum(AsrRequestConfig.RATE_16000);
        builder.enablePunctuation(false);
        builder.intermediateResult(true);
        builder.maxEndSilence(3000);
        /**
         * CHANNEL_IN_MONO或者CHANNEL_IN_STEREO
         */
        builder.channel(AsrRequestConfig.CHANNEL_IN_MONO);
        builder.enableVoiceDetection(false);

        AsrRequestConfig asrRequestConfig = builder.build();
        turingOSClient = TuringOSClient.getInstance(this, userData);
        turingOSClient.initAsrStream(asrRequestConfig, new TuringOSClientAsrListener() {

            @Override
            public void onRecorderStart() {

            }

            @Override
            public void onStop() {
                Log.e(TAG, " =========onStop=====");
                isPcmStop = true;
                mUIHandler.postRunnable(new Runnable() {
                    @Override
                    public void run() {
                        updateUI(MODE_NONE);
                    }
                });
            }

            @Override
            public void onStreamOpen() {
                isPcmStop = false;
                startStreamPcmInput();
            }


            @Override
            public void onResult(int code, String result, boolean isLast, ResponBean responBean) {
                mUIHandler.postRunnable(new Runnable() {
                    @Override
                    public void run() {
                        updateResult(code, result, responBean);
                    }
                });

            }

            @Override
            public void onTimer(int second) {

            }

            @Override
            public void onError(int code, String msg) {
                Log.e(TAG, "onError  code: " + code + "  msg:" + msg);
                isPcmStop = true;
                mUIHandler.postRunnable(new Runnable() {
                    @Override
                    public void run() {
                        updateResult(code, msg, null);
                        updateUI(MODE_NONE);
                    }
                });
            }
        });

        updateUI(MODE_PCM);
    }

    private void startInnerRecord() {
        turingOSClient = TuringOSClient.getInstance(this, userData);
        turingOSClient.startAsrWithRecorder(true, new TuringOSClientAsrListener() {
            @Override
            public void onRecorderStart() {
                mUIHandler.postRunnable(new Runnable() {
                    @Override
                    public void run() {
                        updateUI(MODE_RECORD);
                    }
                });
            }


            @Override
            public void onStop() {
                Log.e(TAG, " =========onStop=====");
                mUIHandler.postRunnable(new Runnable() {
                    @Override
                    public void run() {
                        updateUI(MODE_NONE);
                    }
                });
            }

            @Override
            public void onStreamOpen() {

            }

            @Override
            public void onResult(int code, String result, boolean isLast, ResponBean responBean) {
                mUIHandler.postRunnable(new Runnable() {
                    @Override
                    public void run() {
                        updateResult(code, result, responBean);
                    }
                });

            }

            @Override
            public void onTimer(int second) {

            }

            @Override
            public void onError(int code, String msg) {
                Log.e(TAG, "onError  code: " + code + "  msg:" + msg);
                mUIHandler.postRunnable(new Runnable() {
                    @Override
                    public void run() {
                        updateResult(code, msg, null);
                        updateUI(MODE_NONE);
                    }
                });
            }
        });
    }

    private void stopInnerRecord() {
        turingOSClient.stopAsr();
        updateUI(MODE_NONE);
    }

    private void updateUI(int mode) {
        switch (mode) {
            case MODE_CODE_OPUS:
                btnStart.setEnabled(false);
                btnStop.setEnabled(false);
                btnStreamStart.setEnabled(false);
                btnCodeStart.setEnabled(false);
                btnStopOpus.setEnabled(true);
                btnStopPcm.setEnabled(false);
                btnSpeexStart.setEnabled(false);
                btnSpeexStop.setEnabled(false);
                break;
            case MODE_CODE_SPEEX:
                btnStart.setEnabled(false);
                btnStop.setEnabled(false);
                btnStreamStart.setEnabled(false);
                btnCodeStart.setEnabled(false);
                btnStopOpus.setEnabled(false);
                btnStopPcm.setEnabled(false);
                btnSpeexStart.setEnabled(false);
                btnSpeexStop.setEnabled(true);
                break;
            case MODE_PCM:
                btnStart.setEnabled(false);
                btnStop.setEnabled(false);
                btnStreamStart.setEnabled(false);
                btnCodeStart.setEnabled(false);
                btnStopOpus.setEnabled(false);
                btnStopPcm.setEnabled(true);
                btnSpeexStart.setEnabled(false);
                btnSpeexStop.setEnabled(false);
                break;
            case MODE_RECORD:
                btnStart.setEnabled(false);
                btnStop.setEnabled(true);
                btnStreamStart.setEnabled(false);
                btnCodeStart.setEnabled(false);
                btnStopOpus.setEnabled(false);
                btnStopPcm.setEnabled(false);
                btnSpeexStart.setEnabled(false);
                btnSpeexStop.setEnabled(false);
                break;
            case MODE_NONE:
                btnStart.setEnabled(true);
                btnStop.setEnabled(false);
                btnStreamStart.setEnabled(true);
                btnCodeStart.setEnabled(true);
                btnStopOpus.setEnabled(false);
                btnStopPcm.setEnabled(false);
                btnSpeexStart.setEnabled(true);
                btnSpeexStop.setEnabled(false);
                break;
        }
    }

    @OnClick({R.id.btn_start, R.id.btn_stop, R.id.btn_stream_start,
            R.id.btn_code_start, R.id.btn_stop_pcm, R.id.btn_stop_opus,
            R.id.btn_speex_start, R.id.btn_speex_stop})
    public void onViewClicked(View view) {
        switch (view.getId()) {
            case R.id.btn_start:
                startInnerRecord();
                break;
            case R.id.btn_stop:
                stopInnerRecord();
                break;
            case R.id.btn_stream_start:
                startPCMStream();
                break;
            case R.id.btn_code_start:
                startEncodeStream(AsrRequestConfig.OPUS);
                break;
            case R.id.btn_stop_pcm:
                isPcmStop = true;
                if (turingOSClient != null) {
                    turingOSClient.stopAsr();
                }
                break;
            case R.id.btn_speex_stop:
            case R.id.btn_stop_opus:
                isEncodeStop = true;
                if (turingOSClient != null) {
                    turingOSClient.stopAsr();
                }
                break;
            case R.id.btn_speex_start:
                startEncodeStream(AsrRequestConfig.SPEEX);
                break;
        }
    }

    private Runnable pcmRun = new Runnable() {
        @Override
        public void run() {
            try {
                InputStream inputStream = getAssets().open("record.pcm");
                while (!isPcmStop) {
                    byte[] buffer = new byte[320];
                    int length = inputStream.read(buffer);
                    if (length == -1) {
                        turingOSClient.stopAsr();
                        break;
                    }
                    if (length == 320) {
                        turingOSClient.sendAudio(buffer, length);
                    } else {
                        turingOSClient.sendAudio(Arrays.copyOf(buffer, length), length);
                    }
                    Thread.sleep(30);
                }
                Log.i(TAG, "testFile: 流输入完成");
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
    };

    private void startStreamPcmInput() {
        new Thread(pcmRun).start();
    }

    private Runnable encodeRun = new Runnable() {
        @Override
        public void run() {
            int enState = opusEncoder.createEncoder(SAMPLE_RATE, CHANNELS);
            if (enState == 0) {
                Log.v(TAG, "opusEncoder初始化成功");
                opusEncoder.setComplexity(5);
            } else {
                Log.v(TAG, "opusEncoder初始化失败");
            }
            try {
                InputStream inputStream = getAssets().open("weather.pcm");
                int bufferSize = OpusHelper.getByteSizePerFrame(FRAME_SIZE, CHANNELS);
                Log.e(TAG, FRAME_SIZE + "length" + bufferSize);
                while (!isEncodeStop) {
                    byte[] buffer = new byte[bufferSize];
                    int length = inputStream.read(buffer);
                    if (length == -1) {
                        turingOSClient.stopAsr();
                        break;
                    }
                    byte[] enBytes = opusEncoder.encodeBytes(buffer, FRAME_SIZE);
                    byte[] opusBytes = BytesTransform.opensocketOpusAdapt(enBytes);
                    Log.i(TAG, "testFile: encodeLen=" + enBytes.length);
                    turingOSClient.sendAudio(opusBytes, opusBytes.length);
                }
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
    };

    private Runnable encodeSpeexRun = new Runnable() {
        @Override
        public void run() {
            try {
                InputStream inputStream = getAssets().open("father.spx");

                while (!isEncodeStop) {
                    byte[] buffer = new byte[1280];
                    int length = inputStream.read(buffer);
                    if (length == -1) {
                        turingOSClient.stopAsr();
                        break;
                    }
                    turingOSClient.sendAudio(buffer, buffer.length);
                }
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
    };


    private void updateResult(int code, String result, ResponBean responBean) {
        if (code == 200) {
            if (responBean != null && responBean.getAsrResponse() != null) {
                if (responBean.getAsrResponse().getState() == 210) {
                    String asrResult = responBean.getAsrResponse().getValue();
                    tvResult.setText(asrResult);
                } else if (responBean.getAsrResponse().getState() == 200) {
                    String asrResult = responBean.getAsrResponse().getValue();
                    AsrResult asrResult1Bean = new AsrResult(result, asrResult);
                    resultList.add(asrResult1Bean);
                    resultAdapter.updateList(resultList);
                    resultRecycleview.smoothScrollToPosition(resultList.size() - 1);
                }
            }
        } else {
            AsrResult asrResult1Bean = new AsrResult(result, result);
            resultList.add(asrResult1Bean);
            resultAdapter.updateList(resultList);
            resultRecycleview.smoothScrollToPosition(resultList.size() - 1);
        }

    }

    private void startStreamEncodeInput(int type) {
        if(type == AsrRequestConfig.OPUS){
            new Thread(encodeRun).start();
        }else if(type == AsrRequestConfig.SPEEX){
            new Thread(encodeSpeexRun).start();
        }
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        if (turingOSClient != null) {
            turingOSClient.release();
        }
    }

}
