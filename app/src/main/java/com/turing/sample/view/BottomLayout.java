package com.turing.sample.view;

import android.content.Context;
import android.text.TextUtils;
import android.util.AttributeSet;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.View;
import android.view.inputmethod.InputMethodManager;
import android.widget.Button;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;
import android.widget.Toast;

import com.turing.sample.R;


/**
 * @author：licheng@uzoo.com
 */

public class BottomLayout extends LinearLayout implements View.OnClickListener {


    private View bottomLayout;//底部整体布局
    private RelativeLayout rel_input, rel_asr;
    private EditText ed_input;
    private Button btn_select_asr, btn_start_asr;
    private Button btn_select_input, btn_send_tts;
    private ActionCallback callback;
    private Context context;


    public BottomLayout(Context context) {
        super(context);
        this.context = context;
        initView(context);
    }

    public BottomLayout(Context context, AttributeSet attrs) {
        super(context, attrs);
        this.context = context;
        initView(context);
    }

    public BottomLayout(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        this.context = context;
        initView(context);
    }

    private void initView(Context context) {
        bottomLayout = LayoutInflater.from(context).inflate(R.layout.base_bottom, null);
        //输入的整体
        rel_input = (RelativeLayout) bottomLayout.findViewById(R.id.rel_input);
        ed_input = (EditText) bottomLayout.findViewById(R.id.ed_input);
        btn_select_asr = (Button) bottomLayout.findViewById(R.id.btn_select_asr);
        btn_select_asr.setOnClickListener(this);
        btn_send_tts = (Button) bottomLayout.findViewById(R.id.btn_send);
        btn_send_tts.setOnClickListener(this);
        //asr的整体
        rel_asr = (RelativeLayout) bottomLayout.findViewById(R.id.rel_asr);
        btn_start_asr = (Button) bottomLayout.findViewById(R.id.btn_start_asr);
        btn_start_asr.setOnTouchListener(new OnTouchListener() {
            @Override
            public boolean onTouch(View v, MotionEvent event) {
                switch (event.getAction()) {
                    case MotionEvent.ACTION_DOWN:
                        startVoice();
                        btn_start_asr.setText("松开停止");
                        btn_start_asr.setBackgroundResource(R.drawable.bg_press_true);
                        break;
                    case MotionEvent.ACTION_CANCEL:
                        btn_start_asr.setText("按住说话");
                        btn_start_asr.setBackgroundResource(R.drawable.bg_press_false);
                        stopVoice();
                        break;
                    case MotionEvent.ACTION_UP:
                        btn_start_asr.setText("按住说话");
                        btn_start_asr.setBackgroundResource(R.drawable.bg_press_false);
                        stopVoice();
                        break;
                    default:
                }
                return true;
            }
        });
        btn_select_input = (Button) bottomLayout.findViewById(R.id.btn_select_input);
        btn_select_input.setOnClickListener(this);
        addView(bottomLayout);
    }

    @Override
    public void onClick(View v) {
        switch (v.getId()) {
            case R.id.btn_select_input://选择键盘，切换到输入框
                rel_input.setVisibility(VISIBLE);
                rel_asr.setVisibility(GONE);
                break;
            case R.id.btn_select_asr:
                hiddenKeyboard();
                rel_input.setVisibility(GONE);
                rel_asr.setVisibility(VISIBLE);
                break;
            case R.id.btn_send:
                onClickSendText();
                break;

        }
    }

    public interface ActionCallback{
        void onActionRecordStart();
        void onActionRecordStop();
        void onActionText(String str);
    }
    public void setCallback(ActionCallback callback) {
        this.callback = callback;
    }

    private void hideKeyboard() {
        InputMethodManager imm = (InputMethodManager) getContext().getSystemService(Context.INPUT_METHOD_SERVICE);
        if (imm.isActive()) {
            if (this.getWindowToken() != null) {
                imm.hideSoftInputFromWindow(this.getWindowToken(), InputMethodManager.HIDE_NOT_ALWAYS);
            }
        }
    }

    /**
     * 开始asr
     **/
    private void startVoice() {
        callback.onActionRecordStart();

    }

    private void stopVoice() {
        callback.onActionRecordStop();

    }

    private void onClickSendText() {
        String str = ed_input.getText().toString();
        if (TextUtils.isEmpty(str)) {
            Toast.makeText(context, R.string.warn_content_isnull, Toast.LENGTH_SHORT).show();
            return;
        }
        callback.onActionText(str);

        hiddenKeyboard();
        clearInputDate();
    }



    private void hiddenKeyboard() {
        InputMethodManager imm = (InputMethodManager) getContext().getSystemService(Context.INPUT_METHOD_SERVICE);
        imm.showSoftInput(ed_input, InputMethodManager.SHOW_FORCED);
        imm.hideSoftInputFromWindow(ed_input.getWindowToken(), 0); //强制隐藏键盘
    }

    /**
     * 设置asr按钮的文本
     **/
    public void setTextBtnASR(String str) {
        btn_start_asr.setText(str);
    }

    /**
     * 获得asr按钮的文本
     **/
    public String getTextBtnASR() {
        return btn_start_asr.getText().toString();
    }

    /**
     * 清空edit里面的数据
     **/
    public void clearInputDate() {
        ed_input.setText("");
    }
}
