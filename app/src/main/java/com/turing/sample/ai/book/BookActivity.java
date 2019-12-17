package com.turing.sample.ai.book;

import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
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

import com.turing.os.request.bean.BookRequestConfig;
import com.turing.os.request.bean.ResponBean;
import com.turing.os.util.SPUtils;
import com.turing.sample.R;
import com.turing.sample.ai.chat.ResultAdapter;
import com.turing.sample.app.JsonViewActivity;
import com.turing.sample.app.SettingsActivity;
import com.turing.sample.app.base.BaseActivity;
import com.turingapi.turingstory.BookRecognizer;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import butterknife.BindView;
import butterknife.ButterKnife;
import butterknife.OnClick;

public class BookActivity extends BaseActivity implements View.OnClickListener {
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

    private String vag;
    private String vagType;

    /**
     * 为了预览不变形，固定为4/3
     */
    private float rate = (float) 1.333;

    private List<String> resultList = new ArrayList<>();
    private RecyclerView resultRecyclerView;
    private ResultAdapter resultAdapter;

    private volatile int bookID = -1;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_book);
        ButterKnife.bind(this);
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

    private void useByte(String path){
        byte[] fileByte = getBytes(path);
        if(fileByte != null){
            if(bookID == -1){
                client.actionBook(fileByte, bookRequestConfig, booklistener);
            }else{
                client.actionBook(fileByte, bookID, bookRequestConfig, booklistener);
            }
        }
    }

    public static byte[] getBytes(String filePath){
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
                    client.actionBook(filePath, bookID, bookRequestConfig, booklistener);

                    //JEPG图片字节数据方式识别
//                    useByte(filePath);
                } else {
                    //文件的方式识别
                    client.actionBook(filePath, bookRequestConfig, booklistener);

                    //JEPG图片字节数据方式识别
//                    useByte(filePath);
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

    @Override
    public void onClick(View view) {
        switch (view.getId()) {
            case R.id.btn_reset_cover:
                bookID = -1;
                tvResultsHint.setText(" ");
                break;
            case R.id.btn_setting:
                Intent intent = new Intent(mActivity, SettingsActivity.class);
                mContext.startActivity(intent);
                break;
        }
    }

    @OnClick({R.id.btn_reset_cover, R.id.btn_setting})
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
        }
    }


    class Booklistener implements TuringOSClientListener {

        @Override
        public void onResult(int code, String result, ResponBean responBean) {
            mUIHandler.postRunnable(new Runnable() {
                @Override
                public void run() {
                    isIdle = true;
                    if (responBean != null && responBean.getNlpResponse() != null && responBean.getNlpResponse().getIntent() != null
                            && responBean.getNlpResponse().getIntent().getParameters() != null) {
                        int intentCode = responBean.getNlpResponse().getIntent().getParameters().getIntentCode();
                        if (intentCode == 200) {
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
}
