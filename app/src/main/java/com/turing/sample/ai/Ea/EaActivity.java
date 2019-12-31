package com.turing.sample.ai.Ea;

import android.app.AlertDialog;
import android.os.Bundle;
import android.text.TextUtils;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;

import androidx.annotation.Nullable;

import com.turing.os.client.TuringOSClient;
import com.turing.os.client.TuringOSClientListener;
import com.turing.os.init.UserData;
import com.turing.os.request.bean.ResponBean;
import com.turing.sample.R;
import com.turing.sample.app.base.BaseActivity;

import butterknife.BindView;
import butterknife.ButterKnife;
import butterknife.OnClick;

/**
 * @Author yihuapeng
 * @Date 2019/12/30 19:31
 **/
public class EaActivity extends BaseActivity {
    private final static String TAG = "EaActivity";

    @BindView(R.id.et_input)
    EditText etInput;
    @BindView(R.id.btn_start)
    Button btnStart;
    @BindView(R.id.tv_result)
    TextView tvResult;

    private AlertDialog alertDialog;
    private TuringOSClient client;
    private TextView tv_word;


    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_ea);
        ButterKnife.bind(this);
        userData = (UserData) getIntent().getSerializableExtra("userdata");
        client = TuringOSClient.getInstance(this, userData);
    }

    @OnClick(R.id.btn_start)
    public void onViewClicked() {
        if (!TextUtils.isEmpty(etInput.getText().toString())) {
            showAlertDialog(etInput.getText().toString());
            client.startEAWithRecorder(etInput.getText().toString(), new TuringOSClientListener() {
                @Override
                public void onResult(int code, String result, ResponBean responBean, String asrResult) {
                    mUIHandler.postRunnable(new Runnable() {
                        @Override
                        public void run() {
                            closeAlertDialog();
                            tvResult.setText(result);
                        }
                    });


                }

                @Override
                public void onError(int code, String result) {
                    Log.e(TAG, "code: " + code + " result: " + result);
                    mUIHandler.postRunnable(new Runnable() {
                        @Override
                        public void run() {
                            closeAlertDialog();
                        }
                    });
                }
            });
        }
    }

    private void showAlertDialog(String word) {
        if (alertDialog == null) {
            View view = LayoutInflater.from(mContext).inflate(R.layout.layout_oral_evaluation, null);
            alertDialog = new AlertDialog.Builder(mContext)
                    .setView(view)
                    .create();
            tv_word = view.findViewById(R.id.tv_word);
        }
        tv_word.setText(word);
        alertDialog.show();
    }

    private void closeAlertDialog() {
        if (alertDialog != null) {
            alertDialog.dismiss();
        }
    }
}
