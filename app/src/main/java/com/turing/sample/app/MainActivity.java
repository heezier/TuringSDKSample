package com.turing.sample.app;

import android.Manifest;
import android.content.Intent;
import android.os.Bundle;
import android.os.Environment;
import android.util.Log;
import android.view.View;
import android.widget.AdapterView;
import android.widget.Button;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;

import com.tbruyelle.rxpermissions2.RxPermissions;
import com.turing.os.init.SdkInitializer;
import com.turing.os.init.SdkInitializerListener;
import com.turing.os.init.UserData;
import com.turing.os.util.SPUtils;
import com.turing.sample.R;
import com.turing.sample.ai.Ea.EaActivity;
import com.turing.sample.ai.asr.AsrActivity;
import com.turing.sample.ai.book.BookActivity;
import com.turing.sample.ai.chat.ChatActivity;
import com.turing.sample.ai.nlp.NlpActivity;
import com.turing.sample.ai.tts.TtsActivity;
import com.turing.sample.app.base.BaseActivity;

import java.io.Serializable;

import butterknife.BindView;
import butterknife.ButterKnife;
import butterknife.OnClick;
import io.reactivex.annotations.NonNull;
import io.reactivex.functions.Consumer;

public class MainActivity extends BaseActivity {

    private final static String TAG = "MainActivity";
    @BindView(R.id.tv_initState)
    TextView tvInitState;
    @BindView(R.id.tv_deviceID)
    TextView tvDeviceID;
    @BindView(R.id.btn_chat)
    Button btnChat;
    @BindView(R.id.btn_tts)
    Button btnTts;
    @BindView(R.id.btn_asr)
    Button btnAsr;
    @BindView(R.id.btn_nlp)
    Button btnNlp;
    @BindView(R.id.btn_book)
    Button btnBook;
    @BindView(R.id.spinner_server)
    Spinner spinnerServer;
    @BindView(R.id.btn_ea)
    Button btnEa;


    private UserData mUserData;
    private final static String SERVER_KEY = "server";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        ButterKnife.bind(this);
        RxPermissions rxPermissions = new RxPermissions(this);
        rxPermissions
                .request(Manifest.permission.READ_PHONE_STATE,
                        Manifest.permission.WRITE_EXTERNAL_STORAGE,
                        Manifest.permission.WRITE_EXTERNAL_STORAGE,
                        Manifest.permission.CAMERA,
                        Manifest.permission.RECORD_AUDIO,
                        Manifest.permission.READ_PHONE_STATE)
                .subscribe(new Consumer<Boolean>() {
                    @Override
                    public void accept(@NonNull Boolean aBoolean) throws Exception {
                        if (!aBoolean) {
                            Toast.makeText(MainActivity.this, "有权限没有授权成功哦", Toast.LENGTH_SHORT).show();
                        }
                        init();
                    }
                });


        //选择环境,默认为正式环境
        spinnerServer.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> adapterView, View view, int i, long l) {
                switch (i) {
                    case 0:
                        SPUtils.put(mContext, SERVER_KEY, "alpha");
                        SdkInitializer.setServer(UserData.SERVER_ALPHA);
                        init();
                        break;
                    case 1:
                        SPUtils.put(mContext, SERVER_KEY, "beta");
                        SdkInitializer.setServer(UserData.SERVER_BETA);
                        init();
                        break;
                    case 2:
                        SPUtils.put(mContext, SERVER_KEY, "product");
                        SdkInitializer.setServer(UserData.SERVER_PRODUCT);
                        init();
                        break;
                }
            }

            @Override
            public void onNothingSelected(AdapterView<?> adapterView) {

            }
        });

    }


    private void updateBtnStates(boolean isSuccess) {
        btnAsr.setEnabled(isSuccess);
        btnTts.setEnabled(isSuccess);
        btnBook.setEnabled(isSuccess);
        btnNlp.setEnabled(isSuccess);
        btnChat.setEnabled(isSuccess);
        btnEa.setEnabled(isSuccess);
    }

    private void updateUI(final boolean isSuccess, final String text, final String deviceID) {
        mUIHandler.postRunnable(new Runnable() {
            @Override
            public void run() {
                tvInitState.setText(text);
                tvDeviceID.setText(deviceID);
                updateBtnStates(isSuccess);
            }
        });
    }

    private void init() {
        String server = SPUtils.getString(mContext, SERVER_KEY, "alpha");
        ApiKey apiKey = null;
        if (server.equals("alpha")) {
            apiKey = new ApiKey(UserData.SERVER_ALPHA);
            spinnerServer.setSelection(0);
        } else if (server.equals("beta")) {
            apiKey = new ApiKey(UserData.SERVER_BETA);
            spinnerServer.setSelection(1);
        } else {
            apiKey = new ApiKey(UserData.SERVER_PRODUCT);
            spinnerServer.setSelection(2);
        }

        //只是为了方便查看文件所以获取getExternalStorageDirectory()
        String path = Environment.getExternalStorageDirectory().getAbsolutePath() + "/turingos/";
        /**
         * 打开后可看到SDK内部日志输出，用于调试环境
         */
        SdkInitializer.setDebug(path, true);
        /**
         * 默认读取AndroidManifest.xml中配置的key
         */
        if (apiKey == null) return;
        SdkInitializer.init(this, apiKey.getApikey(), apiKey.getApiSecert(), new SdkInitializerListener() {
            @Override
            public void onSuccess(String type, UserData userData) {
                Log.v(TAG, "type:  " + type + "deviceID： " + userData.getDeviceID());
                updateUI(true, mContext.getResources().getString(R.string.welecome), userData.getDeviceID());
                mUserData = userData;
            }

            @Override
            public void onError(int errorCode, String errorMsg) {
                Log.e(TAG, "errorCode:  " + errorCode + "errorMsg： " + errorMsg);
                updateUI(false, errorMsg, "获取失败");
            }
        });
    }

    @OnClick({R.id.btn_chat, R.id.btn_tts, R.id.btn_asr, R.id.btn_nlp, R.id.btn_book, R.id.btn_ea})
    public void onViewClicked(View view) {
        switch (view.getId()) {
            case R.id.btn_chat:
                Intent intentChat = new Intent(mActivity, ChatActivity.class);
                intentChat.putExtra("userdata", (Serializable) mUserData);
                mActivity.startActivity(intentChat);
                break;
            case R.id.btn_tts:
                Intent intentTts = new Intent(mActivity, TtsActivity.class);
                intentTts.putExtra("userdata", (Serializable) mUserData);
                mActivity.startActivity(intentTts);
                break;
            case R.id.btn_asr:
                Intent intent = new Intent(mActivity, AsrActivity.class);
                intent.putExtra("userdata", (Serializable) mUserData);
                mActivity.startActivity(intent);
                break;
            case R.id.btn_nlp:
                Intent intentNlp = new Intent(mActivity, NlpActivity.class);
                intentNlp.putExtra("userdata", (Serializable) mUserData);
                mActivity.startActivity(intentNlp);
                break;
            case R.id.btn_book:
                Intent intentBook = new Intent(mActivity, BookActivity.class);
                intentBook.putExtra("userdata", (Serializable) mUserData);
                mActivity.startActivity(intentBook);
                break;
            case R.id.btn_ea:
                Intent intentEa = new Intent(mActivity, EaActivity.class);
                intentEa.putExtra("userdata", (Serializable) mUserData);
                mActivity.startActivity(intentEa);
                break;
        }
    }

    @OnClick(R.id.btn_ea)
    public void onViewClicked() {
    }
}
