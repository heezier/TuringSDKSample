package com.turing.sample.ai.asr;

import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;

import androidx.annotation.Nullable;

import com.turing.opus.OpusEncoder;
import com.turing.opus.OpusHelper;
import com.turing.os.client.TuringOSClient;
import com.turing.os.client.TuringOSClientAsrListener;
import com.turing.os.init.UserData;
import com.turing.os.request.bean.AsrRequestConfig;
import com.turing.os.request.bean.ResponBean;
import com.turing.sample.R;
import com.turing.sample.app.base.BaseActivity;

import java.io.InputStream;
import java.util.Arrays;

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
    @BindView(R.id.tv_results)
    TextView tvResults;
    @BindView(R.id.btn_code_start)
    Button btnCodeStart;


    private final static int MODE_PCM = 0;
    private final static int MODE_RECORD = 1;
    private final static int MODE_CODE = 2;
    private final static int MODE_NONE = 3;
    @BindView(R.id.btn_stop_pcm)
    Button btnStopPcm;
    @BindView(R.id.btn_stop_opus)
    Button btnStopOpus;


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
    }

    private void startEncodeStream() {
        AsrRequestConfig.Builder builder = new AsrRequestConfig.Builder();
        builder.asrLanguageEnum(AsrRequestConfig.CHINESE);
        //必须与传入可支持的格式对应，比如你将要传入opus格式，则该参数必须设置为OPUS
        builder.asrFormatEnum(AsrRequestConfig.OPUS);
        builder.asrRateEnum(AsrRequestConfig.RATE_16000);
        builder.enablePunctuation(false);
        builder.maxEndSilence(3000);
        /**
         * CHANNEL_IN_MONO或者CHANNEL_IN_STEREO
         */
        builder.channel(AsrRequestConfig.CHANNEL_IN_MONO);

        AsrRequestConfig asrRequestConfig = builder.build();
        turingOSClient = TuringOSClient.getInstance(this, userData);
        turingOSClient.initAsrEncodeStream(asrRequestConfig, new TuringOSClientAsrListener() {

            @Override
            public void onRecorderStart() {

            }

            @Override
            public void onStop() {
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
                startStreamEncodeInput();
            }


            @Override
            public void onResult(int code, String result, boolean isLast, ResponBean responBean) {
                mUIHandler.postRunnable(new Runnable() {
                    @Override
                    public void run() {
                        tvResults.setText("code:" + code + "result: " + result);
                    }
                });
            }

            @Override
            public void onTimer(int second) {

            }

            @Override
            public void onError(int code, String msg) {
                mUIHandler.postRunnable(new Runnable() {
                    @Override
                    public void run() {
                        updateUI(MODE_NONE);
                    }
                });
            }
        });
        updateUI(MODE_CODE);
    }

    private void startPCMStream() {
        AsrRequestConfig.Builder builder = new AsrRequestConfig.Builder();
        builder.asrLanguageEnum(AsrRequestConfig.CHINESE);
        //默认是OPUS，这里传入pcm数据，会经过opus编码再请求，如果设置PCM，则不会编码,而是直接请求
        builder.asrFormatEnum(AsrRequestConfig.PCM);
        builder.asrRateEnum(AsrRequestConfig.RATE_16000);
        builder.enablePunctuation(false);
        builder.intermediateResult(true);
        builder.maxEndSilence(3000);
        /**
         * CHANNEL_IN_MONO或者CHANNEL_IN_STEREO
         */
        builder.channel(AsrRequestConfig.CHANNEL_IN_MONO);
        builder.enableVoiceDetection(true);

        AsrRequestConfig asrRequestConfig = builder.build();
        turingOSClient = TuringOSClient.getInstance(this, userData);
        turingOSClient.initAsrPcmStream(asrRequestConfig, new TuringOSClientAsrListener() {

            @Override
            public void onRecorderStart() {

            }

            @Override
            public void onStop() {
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
                        tvResults.setText("code:" + code + "result: " + result);
                    }
                });

            }

            @Override
            public void onTimer(int second) {
                if (second <= 0) {
                    if (turingOSClient != null) {
                        turingOSClient.stopAsr();
                    }
                }
            }

            @Override
            public void onError(int code, String msg) {
                mUIHandler.postRunnable(new Runnable() {
                    @Override
                    public void run() {
                        updateUI(MODE_NONE);
                    }
                });
            }
        });

        updateUI(MODE_PCM);
    }

    private void startInnerRecord() {

        turingOSClient = TuringOSClient.getInstance(this, userData);
        turingOSClient.startAsrWithRecorder(new TuringOSClientAsrListener() {
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
                        tvResults.setText("code:" + code + "result: " + result);
                    }
                });

            }

            @Override
            public void onTimer(int second) {

            }

            @Override
            public void onError(int code, String msg) {
                mUIHandler.postRunnable(new Runnable() {
                    @Override
                    public void run() {
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
            case MODE_CODE:
                btnStart.setEnabled(false);
                btnStop.setEnabled(false);
                btnStreamStart.setEnabled(false);
                btnCodeStart.setEnabled(false);
                btnStopOpus.setEnabled(true);
                btnStopPcm.setEnabled(false);
                break;
            case MODE_PCM:
                btnStart.setEnabled(false);
                btnStop.setEnabled(false);
                btnStreamStart.setEnabled(false);
                btnCodeStart.setEnabled(false);
                btnStopOpus.setEnabled(false);
                btnStopPcm.setEnabled(true);
                break;
            case MODE_RECORD:
                btnStart.setEnabled(false);
                btnStop.setEnabled(true);
                btnStreamStart.setEnabled(false);
                btnCodeStart.setEnabled(false);
                btnStopOpus.setEnabled(false);
                btnStopPcm.setEnabled(false);
                break;
            case MODE_NONE:
                btnStart.setEnabled(true);
                btnStop.setEnabled(false);
                btnStreamStart.setEnabled(true);
                btnCodeStart.setEnabled(true);
                btnStopOpus.setEnabled(false);
                btnStopPcm.setEnabled(false);
                isEncodeStop = false;
                isPcmStop = false;
                break;
        }
    }

    @OnClick({R.id.btn_start, R.id.btn_stop, R.id.btn_stream_start,
            R.id.btn_code_start, R.id.btn_stop_pcm, R.id.btn_stop_opus})
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
                startEncodeStream();
                break;
            case R.id.btn_stop_pcm:
                isPcmStop = true;
                if(turingOSClient != null){
                    turingOSClient.stopAsr();
                }
                break;
            case R.id.btn_stop_opus:
                isEncodeStop = true;
                if(turingOSClient != null){
                    turingOSClient.stopAsr();
                }
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
                        turingOSClient.sendPcmData(buffer);
                    } else {
                        turingOSClient.sendPcmData(Arrays.copyOf(buffer, length));
                    }
                    Thread.sleep(20);
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
                InputStream inputStream = getAssets().open("record_.pcm");
                byte[] buffer = new byte[OpusHelper.getByteSizePerFrame(FRAME_SIZE, CHANNELS)];
                int bufferSize = OpusHelper.getByteSizePerFrame(FRAME_SIZE, CHANNELS);
                Log.e(TAG, FRAME_SIZE + "length" + bufferSize);
                while (!isEncodeStop) {
                    int length = inputStream.read(buffer);
                    if (length == -1) {
                        turingOSClient.stopAsr();
                        break;
                    }
                    if (length == bufferSize) {
                        byte[] enBytes = opusEncoder.encodeBytes(buffer, FRAME_SIZE);
                        Log.i(TAG, "testFile: encodeLen=" + enBytes.length);
                        turingOSClient.sendEncodeData(enBytes, AsrRequestConfig.OPUS);
                    } else {
                        byte[] enBytes = opusEncoder.encodeBytes(buffer, FRAME_SIZE);
                        turingOSClient.sendEncodeData(Arrays.copyOf(enBytes, enBytes.length), AsrRequestConfig.OPUS);
                    }
                }
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
    };

    private void startStreamEncodeInput() {
        new Thread(encodeRun).start();
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        if (turingOSClient != null) {
            turingOSClient.release();
        }
    }

}
