package com.turing.sample.ai.book;

import android.app.AlertDialog;
import android.content.Intent;
import android.os.Bundle;
import android.text.TextUtils;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.Button;
import android.widget.FrameLayout;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

import androidx.annotation.Nullable;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.Glide;
import com.bumptech.glide.load.engine.DiskCacheStrategy;
import com.bumptech.glide.request.RequestOptions;
import com.turing.os.client.TuringOSClient;
import com.turing.os.client.TuringOSClientListener;
import com.turing.os.init.SdkInitializer;
import com.turing.os.init.UserData;
import com.turing.os.player.TtsPlayerCallback;
import com.turing.os.player.TtsPlayerPool;
import com.turing.os.request.bean.BookRequestConfig;
import com.turing.os.request.bean.ResponBean;
import com.turing.os.util.SPUtils;
import com.turing.sample.R;
import com.turing.sample.ai.chat.ResultAdapter;
import com.turing.sample.app.JsonViewActivity;
import com.turing.sample.app.SettingsActivity;
import com.turing.sample.app.base.BaseActivity;
import com.turing.sample.media.IMediaPlayListener;
import com.turing.sample.media.MediaPlayerManager;
import com.turingapi.turingstory.BookRecognizer;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Queue;

import butterknife.BindView;
import butterknife.ButterKnife;
import butterknife.OnClick;

public class BookActivity extends BaseActivity {
    private final static String TAG = "BookActivity";

    @BindView(R.id.tv_camera_hint)
    TextView tvCameraHint;
    @BindView(R.id.tv_img_result_hint)
    TextView tvImgResultHint;
    @BindView(R.id.frame_layer)
    FrameLayout frameLayout;
    @BindView(R.id.iv_image_result)
    ImageView ivImageResult;
    @BindView(R.id.tv_results_hint)
    TextView tvResultsHint;


    @BindView(R.id.ll_imgview)
    LinearLayout llImgview;
    @BindView(R.id.btn_reset_cover)
    Button btnResetCover;
    @BindView(R.id.btn_setting)
    Button btnSetting;
    @BindView(R.id.result_recycleview)
    RecyclerView resultRecycleview;
    @BindView(R.id.bottom)
    LinearLayout bottom;
    @BindView(R.id.tv_mode)
    TextView tvMode;
    @BindView(R.id.btn_qa)
    Button btnQa;


    private CameraVideo cameraVideo;

    private TuringOSClient client;
    private UserData userData;
    private BookRecognizer bookRecognizer;
    private BookRequestConfig bookRequestConfig;
    private Booklistener booklistener;
    private boolean isIdle = false;
    private String imgFilePath;

    private int ocrFlag = 0;
    private int fingerFlag = 0;
    private QAState curQaState = QAState.None;

    private List<ResponBean.NlpResponseBean.ResultsBean> curQuestions;

    /**
     * 为了预览不变形，固定为4/3
     */
    private float rate = (float) 1.333;

    private List<String> resultList = new ArrayList<>();
    private RecyclerView resultRecyclerView;
    private ResultAdapter resultAdapter;

    private volatile int bookID = -1;

    private TextView tv_word;
    private TextView tv_questing;
    private ImageView iv_icon;
    private AlertDialog alertDialog;
    private TtsPlayerPool ttsPlayerPool;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_book);
        ButterKnife.bind(this);
        btnQa.setEnabled(false);
        btnQa.setBackgroundResource(R.drawable.bg_press_true);
        init();
        initResultView();
    }

    private void initResultView() {

        resultRecyclerView = (RecyclerView) findViewById(R.id.result_recycleview);
        LinearLayoutManager resultlayoutManager = new LinearLayoutManager(this);
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
    }

    private void init() {
        //第一步：获取TuringOSClient实例
        userData = (UserData) getIntent().getSerializableExtra("userdata");
        client = TuringOSClient.getInstance(mContext, userData);

        //第二步：①创建绘本识别参数，cameraID为必须参数
        BookRequestConfig.Builder builder = new BookRequestConfig.Builder();
        builder.cameraID(1)
                .enableUseQa(true)
                .enableDebug(true)
                .type(1)
                .enableInnerUrlFlag(true)
                .phoneModle("iPhone 8 Plus");
        bookRequestConfig = builder.build();
        //②需要上传识别的图片保存地址
        imgFilePath = SdkInitializer.getFilePath() + "book_image.jpg";
        Log.d(TAG, "Book Image File Path is " + imgFilePath);
        //③创建回调接口
        booklistener = new Booklistener();
        isIdle = true;

        //第三步：翻页检测初始化
        initBookRecognizer();


        this.ocrFlag = Integer.parseInt(SPUtils.getStringPreference(mContext, mContext.getString(R.string.pre_key_mode_ocr), "0"));
        this.fingerFlag = Integer.parseInt(SPUtils.getStringPreference(mContext, mContext.getString(R.string.pre_key_mode_finger), "0"));

        //第四步：初始化相机和预览界面
        frameLayout = (FrameLayout) findViewById(R.id.frame_layer);
        frameLayout.post(new Runnable() {
            @Override
            public void run() {
                int height = frameLayout.getMeasuredHeight();
                int width = (int) (height / rate);
                initCamera(height, width);
            }
        });

    }

    private void initCamera(int height, int width) {
        cameraVideo = new CameraVideo(this, this, frameLayout, ocrFlag == 1, height, width);
        //第五步：接受相机的yuv数据
        cameraVideo.setOnFrameListener(new CameraVideo.VideoOnFrameListener() {
            @Override
            public void onFrameData(byte[] videodata, int length) {
                if (videodata != null && videodata.length != 0) {
                    //第六步：翻页检测
                    moveDetect(videodata, length);
                }
            }
        });
    }

    private void initBookRecognizer() {
        int minThread = Integer.parseInt(SPUtils.getStringPreference(mContext, mContext.getString(R.string.pre_key_motion_min_thread), "30"));
        int maxThread = Integer.parseInt(SPUtils.getStringPreference(mContext, mContext.getString(R.string.pre_key_motion_max_thread), "100"));
        int threadWaitTime = Integer.parseInt(SPUtils.getStringPreference(mContext, mContext.getString(R.string.pre_key_motion_min_thread_wait_time), "100"));
        int dstHeight = Integer.parseInt(SPUtils.getStringPreference(mContext, mContext.getString(R.string.pre_key_motion_dst_height), "100"));
        int cropRate = Integer.parseInt(SPUtils.getStringPreference(mContext, mContext.getString(R.string.pre_key_motion_crop_rate), "33"));
        int serverWaitTime = Integer.parseInt(SPUtils.getStringPreference(mContext, mContext.getString(R.string.pre_key_motion_server_wait_time), "0"));
        int timer = Integer.parseInt(SPUtils.getStringPreference(mContext, mContext.getString(R.string.pre_key_motion_timer), "100"));
        bookRecognizer = new BookRecognizer(minThread, maxThread, threadWaitTime, dstHeight, cropRate, serverWaitTime, timer);
    }

    private void moveDetect(byte[] yuvData, int length) {
        byte[] moveValue = bookRecognizer.moveDetect(yuvData, cameraVideo.getWidth(), cameraVideo.getHeight(), ocrFlag);
        if (moveValue != null) {
            if (isIdle) {
                isIdle = false;
                saveImage(moveValue, ocrFlag == 1);
            }
        }
    }

    /**
     * 图片预览
     */
    private void updateImg() {
        mUIHandler.postRunnable(new Runnable() {
            @Override
            public void run() {
                RequestOptions options = new RequestOptions()
                        .diskCacheStrategy(DiskCacheStrategy.NONE)
                        .skipMemoryCache(true);
                Glide.with(mContext.getApplicationContext())
                        .load(imgFilePath)
                        .apply(options)
                        .into(ivImageResult);

            }
        });

    }

    private void useByte(String path) {
        byte[] fileByte = getBytes(path);
        if (fileByte != null) {
            if (bookID == -1) {
                client.actionBook(fileByte, bookRequestConfig, booklistener);
            } else {
                client.actionBook(fileByte, bookID, bookRequestConfig, booklistener);
            }
        }
    }

    public static byte[] getBytes(String filePath) {
        File file = new File(filePath);
        ByteArrayOutputStream out = null;
        try {
            FileInputStream in = new FileInputStream(file);
            out = new ByteArrayOutputStream();
            byte[] b = new byte[1024];
            int i = 0;
            while ((i = in.read(b)) != -1) {
                out.write(b, 0, b.length);
            }
            out.close();
            in.close();
        } catch (FileNotFoundException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        } catch (IOException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        }
        byte[] s = out.toByteArray();
        return s;

    }

    private void saveImage(byte[] data, boolean ocr) {
        //第七步：将翻页检测的结果保存为图片
        BitmapUtil.saveYuvTpJpg(imgFilePath, data, ocr, new BitmapUtil.OnSaveImgListener() {
            @Override
            public void onSuccess(String filePath) {
                //第八步：请求绘本识别
                if (bookID != -1) {
                    //文件的方式识别
//                    client.actionBook(filePath, bookID, bookRequestConfig, booklistener);

                    //JEPG图片字节数据方式识别
                    useByte(filePath);
                } else {
                    //文件的方式识别
//                    client.actionBook(filePath, bookRequestConfig, booklistener);

                    //JEPG图片字节数据方式识别
                    useByte(filePath);
                }
                isIdle = true;
                updateImg();
            }

            @Override
            public void onFiled(String meaasge) {
                isIdle = true;
            }
        });
    }



    @OnClick({R.id.btn_reset_cover, R.id.btn_setting, R.id.btn_qa})
    public void onViewClicked(View view) {
        switch (view.getId()) {
            case R.id.btn_reset_cover:
                bookID = -1;
                tvMode.setText(mContext.getString(R.string.cover));
                btnResetCover.setEnabled(false);
                tvResultsHint.setText(" ");
                break;
            case R.id.btn_setting:
                Intent intent = new Intent(mActivity, SettingsActivity.class);
                mContext.startActivity(intent);
                break;
            case R.id.btn_qa:
                handQA();
                break;
        }
    }


    class Booklistener implements TuringOSClientListener {

        @Override
        public void onResult(int code, String result, ResponBean responBean, String extension) {
            mUIHandler.postRunnable(new Runnable() {
                @Override
                public void run() {
                    isIdle = true;
                    if (responBean != null && responBean.getNlpResponse() != null && responBean.getNlpResponse().getIntent() != null
                            && responBean.getNlpResponse().getIntent().getParameters() != null) {
                        int intentCode = responBean.getNlpResponse().getIntent().getParameters().getIntentCode();
                        if (intentCode == 200) {
                            btnQa.setEnabled(false);
                            btnQa.setBackgroundResource(R.drawable.bg_press_true);
                            ResponBean.NlpResponseBean.IntentBean.ParametersBean.TitleDataBean titleDataBean =
                                    responBean.getNlpResponse().getIntent().getParameters().getTitleData();
                            if (titleDataBean != null) {
                                String successResult = intentCode + " BookID: " + titleDataBean.getBookId() + "BookName: " + titleDataBean.getBookName();
                                String hint = "封面识别成功，BookID:" + titleDataBean.getBookId();
                                tvResultsHint.setText(hint);
                                tvMode.setText(mContext.getString(R.string.page));
                                btnResetCover.setEnabled(true);
                                bookID = titleDataBean.getBookId();

                            }
                        }else if(intentCode == 201){
                            int operateState = responBean.getNlpResponse().getIntent().getOperateState();
                            if(operateState == 3000){
                                curQuestions = responBean.getNlpResponse().getResults();
                                btnQa.setEnabled(true);
                                btnQa.setBackgroundResource(R.drawable.selector_send_bg);
                            }else{
                                btnQa.setEnabled(false);
                                btnQa.setBackgroundResource(R.drawable.bg_press_true);
                            }


                        }
                    }
                    resultList.add(result);
                    resultAdapter.updateList(resultList);
                    resultRecyclerView.smoothScrollToPosition(resultList.size() - 1);
                }
            });
        }

        @Override
        public void onError(int code, String msg) {
            isIdle = true;
            Log.e(TAG, "code: " + code + "mag: " + msg);
        }
    }

    private void handQA(){
        if(curQuestions != null){
            showAlertDialog();
        }
    }


    private void showAlertDialog() {
        List<String> questions = new ArrayList<>();
        List<String> questionsText = new ArrayList<>();
        for(int i = 0; i < curQuestions.size(); i++){
            if(curQuestions.get(i).getValues() != null && curQuestions.get(i).getValues().getTtsUrl() != null){
                questions.add(curQuestions.get(i).getValues().getTtsUrl().get(0));
                questionsText.add(curQuestions.get(i).getValues().getText());
            }
        }
        if(questions.size() <= 0){
            Log.e(TAG, "No Qa data !");
            return;
        }

        if (alertDialog == null) {
            View view = LayoutInflater.from(mContext).inflate(R.layout.layout_oral_evaluation, null);
            alertDialog = new AlertDialog.Builder(mContext)
                    .setView(view)
                    .create();
            tv_word = view.findViewById(R.id.tv_word);
            tv_questing = view.findViewById(R.id.tv_questing);
            iv_icon = view.findViewById(R.id.iv_icon);
        }
        updateDialog(questionsText.toString(), " ");
        alertDialog.show();
        playQuestions(questions);
    }

    private void playQuestions(List<String> questions){
        ttsPlayerPool = TtsPlayerPool.create(questions.size());
        ttsPlayerPool.addPlayUrl(questions);
        ttsPlayerPool.setCallback(new TtsPlayCallback());
        ttsPlayerPool.start();
    }
    private void closeAlertDialog() {
        if (alertDialog != null) {
            alertDialog.dismiss();
        }
    }

    private void updateDialog(String questionsText, String asr){
        if(alertDialog != null && alertDialog.isShowing()){
            if(tv_questing != null && !TextUtils.isEmpty(questionsText)){
                tv_questing.setText(questionsText);
            }
            if(tv_word != null && !TextUtils.isEmpty(asr)){
                tv_word.setText(asr);
            }
        }
    }
    class TtsPlayCallback implements TtsPlayerCallback{

        @Override
        public void onPlayStart() {
            mUIHandler.postRunnable(new Runnable() {
                @Override
                public void run() {
                    if(iv_icon != null){
                        iv_icon.setBackgroundResource(R.mipmap.voice);
                    }
                }
            });
        }

        @Override
        public void onPlayStop() {

        }

        @Override
        public void onPlayComplete() {
            mUIHandler.postRunnable(new Runnable() {
                @Override
                public void run() {
                    if(iv_icon != null){
                        iv_icon.setBackgroundResource(R.mipmap.microphone);
                    }
                    playTip(R.raw.tip_start_answer, null);
                }
            });
        }

        @Override
        public void onPlayError(int code, String msg) {

        }
    }

    private void startRecordQA(){
        client.startQAWithRecorder(new TuringOSClientListener() {
            @Override
            public void onResult(int code, String result, ResponBean responBean, String asrResult) {
                if(code == 200){
                    if(responBean.getNlpResponse() != null && responBean.getNlpResponse().getIntent() != null){
                        if(responBean.getNlpResponse().getIntent().getOperateState() == 3000){
                            List<ResponBean.NlpResponseBean.ResultsBean> resultsBean =
                                    responBean.getNlpResponse().getResults();
                            if (curQaState == QAState.Answering) {
                                startQA(resultsBean, QAState.ReQuesting, asrResult);
                            }
                        }else if (responBean.getNlpResponse().getIntent().getOperateState() == 3100) {
                            ResponBean.NlpResponseBean.IntentBean.ParametersBean parametersBean =
                                    responBean.getNlpResponse().getIntent().getParameters();
                            if(parametersBean != null){
                                boolean success = parametersBean.isSuccess();
                                List<ResponBean.NlpResponseBean.ResultsBean> resultsBean =
                                        responBean.getNlpResponse().getResults();
                                if (curQaState == QAState.Answering || curQaState == QAState.ReAnswering) {
                                    if (success) {
                                        startQA(resultsBean, QAState.AnswerCorrect, asrResult);
                                    } else {
                                        startQA(resultsBean,  QAState.AnswerWrong, asrResult);
                                    }
                                }
                            }
                        }
                    }
                }else{
                    Log.e(TAG, "code : " + code + " result: " + result);
                    if(curQaState == QAState.None) return;
                    playTip(R.raw.tip_answer_wrong, null);
                    cancelQA();
                }
            }

            @Override
            public void onError(int code, String result) {
                if(curQaState == QAState.None) return;
                playTip(R.raw.tip_answer_wrong, null);
                Log.e(TAG, "code : " + code + " result: " + result);
                cancelQA();
            }
        });
    }


    /**
     * 播放提示语
     *
     * @param audioResId         待播放的音频资源
     * @param iMediaPlayListener {@link IMediaPlayListener}
     */
    private void playTip(int audioResId, IMediaPlayListener iMediaPlayListener) {

        try {
            while (MediaPlayerManager.getInstance().isPlaying()) {
                try {
                    Thread.sleep(200);
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
        }

        if (iMediaPlayListener == null) {
            MediaPlayerManager.getInstance().setiMediaPlayListener(new IMediaPlayListener() {
                @Override
                public void onError(int code, String errorMsg) {

                }

                @Override
                public void onComplete() {
                    startRecordQA();
                }

                @Override
                public void onPrepared() {

                }

                @Override
                public void onStart() {

                }

                @Override
                public void onStop() {

                }
            });
        } else {
            MediaPlayerManager.getInstance().setiMediaPlayListener(iMediaPlayListener);
        }
        MediaPlayerManager.getInstance().startPlay(this, audioResId);
    }

    private void startQA(List<ResponBean.NlpResponseBean.ResultsBean> resultsBeans, QAState qaState, String asr) {
        List<String> questions = new ArrayList<>();
        List<String> questionsText = new ArrayList<>();
        for(int i = 0; i < resultsBeans.size(); i++){
            if(resultsBeans.get(i).getValues() != null && resultsBeans.get(i).getValues().getTtsUrl() != null){
                questions.add(resultsBeans.get(i).getValues().getTtsUrl().get(0));
                questionsText.add(resultsBeans.get(i).getValues().getText());
            }
        }
        if(questions.size() > 0){
            handleQA(questionsText, questions, qaState, asr);
        }else{
            Log.e(TAG, "No Qa data !");
        }

    }

    private void handleQA(List<String> questionsText, List<String> questions, QAState qaState, String asr) {
        curQaState = qaState;
        switch (curQaState) {
            case Questing:
            case ReQuesting:
                updateDialog(questionsText.toString(), asr);
                playQuestions(questions);
                break;
            case Answering:
            case ReAnswering:
                playTip(R.raw.tip_start_answer, null);
                updateDialog(questionsText.toString(), asr);
                break;
            case AnswerWrong:
            case AnswerCorrect:
                updateDialog(questionsText.toString(), asr);
                playQuestions(questions);
                break;
            case None:
                closeAlertDialog();
                break;
        }
    }

    private void cancelQA() {
        if (curQaState != QAState.None) {
            closeAlertDialog();
            switch (curQaState) {
                case Questing:
                case ReQuesting:
                case AnswerWrong:
                case AnswerCorrect:
                    if(ttsPlayerPool != null){
                        ttsPlayerPool.stop();
                    }
                    break;
                case Answering:
                case ReAnswering:
                    client.stopAsr();
                    break;
            }
            curQaState = QAState.None;
        }
    }
}
