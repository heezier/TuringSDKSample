package com.turingapi.turingstory;


import android.util.Log;

/**
 * 识别管理类
 *
 *
 */

public class BookRecognizer {
    private static final String TAG = "BookRecognizer";
    private int ocrFlag = 0;
    private int fingerFlag = 0;

    private int minThread = 30;
    private int maxThread = 100;
    private int threadWaitTime = 100;
    private int dstHeight = 100;
    private int cropRate = 33;
    private int serverWaitTime = 0;
    private int timer = 100;

    public BookRecognizer() {
    }

    public BookRecognizer(int minThread, int maxThread, int threadWaitTime, int dstHeight, int cropRate, int serverWaitTime, int timer) {
        this.minThread = minThread;
        this.maxThread = maxThread;
        this.threadWaitTime = threadWaitTime;
        this.dstHeight = dstHeight;
        this.cropRate = cropRate;
        this.serverWaitTime = serverWaitTime;
        this.timer = timer;
        moveDetectInit();
    }

    private void moveDetectInit() {
//        SDKLog.d(TAG, "moveDetectInit: minThread:" + minThread + "|maxThread:" + maxThread + "|threadWaitTime:" + threadWaitTime + "|dstHeight:" + dstHeight + "|cropRate:" + cropRate + "|server_wait_time:" + serverWaitTime + "|timer:" + timer);
        int isMoveDetectInitSuccess = jniMoveDetectInit(minThread, maxThread, threadWaitTime, dstHeight, cropRate, serverWaitTime, timer, System.currentTimeMillis());
//        SDKLog.d(TAG, "isMoveDetectInitSuccess:" + isMoveDetectInitSuccess);
    }

    public byte[] moveDetect(byte[] nv21Yuv, int width, int height, int ocrFlag) {
        if (nv21Yuv != null && nv21Yuv.length != 0) {
            long nowTime = System.currentTimeMillis();
//        Log.v(TAG, "move detect start:data length:" + nv21Yuv.length + "|width:" + width + "|height:" + height + "|ocrFlag:" + ocrFlag + "|fingerFlag:" + fingerFlag);
//        Log.v(TAG, "nowTime:" + nowTime + "|yuv100:" + nv21Yuv[100] + "|yuv200:" + nv21Yuv[200] + "|yuv300:" + nv21Yuv[300]);
            byte[] moveValue = jniMoveDetect(nv21Yuv, width, height, 2, 106, nowTime, ocrFlag, fingerFlag, 0);
            if (moveValue != null) {
                Log.d(TAG, "move detect end:---isMove:true" + "|currentTime:" + nowTime);
                return moveValue;
            }
//        Log.v(TAG, "move detect end:---isMove:false" + "|currentTime:" + nowTime);
        }
        return null;
    }

    /**
     * 释放内存
     */
    public void detectFree() {
        jniMoveDetectFree();
    }

    static {
        System.loadLibrary("turing_story_test");
    }

    /**
     * @param minThr         最小阈值，和上一张图片对比的差异，默认30，调整识别翻页的灵敏度
     * @param maxThr         最大阈值，和上一张图片对比的差异，默认100，调整识别翻页的灵敏度
     * @param minThrWaitTime 最小的阈值等待时间，默认100ms
     * @param dstHeight      缩放图片的高度，默认100，调整识别翻页的灵敏度
     * @param cropRate       裁切图片大小，这里是比例大小，默认33，一般裁切1/3，调整识别翻页的灵敏度
     * @param serverWaitTime 服务器等待时间，服务端对前端的时间限制，调整识别速度，默认0
     * @param timer          定时对比，默认100ms
     * @param nowTime        当前的时间戳
     * @return
     */
    native int jniMoveDetectInit(int minThr, int maxThr, int minThrWaitTime, int dstHeight, int cropRate, int serverWaitTime, int timer, long nowTime);

    /**
     * @param image      图像内容，像素值，支持格式 RGB BGR YUV422 YUV420，要求大小640*480
     * @param width      图像宽度
     * @param height     图像高度
     * @param channels   图像通道数
     * @param imgType    图像类型
     * @param nowTime    当前时间戳
     * @param ocrFlag    OCR模式 1- OCR模式；0- 非OCR模式
     * @param fingerFlag 指读模式 1- 指读模式；0- 非指读模式
     * @param checkCode  测试使用，传0即可
     * @return 缩放后图像内容，图像类型与输入相同，NULL- 未翻页，非NULL - 翻页，ocrFlag==1时，输出图片尺寸640*480；ocrFlag==0时，输出图片尺寸320*240
     */
    native byte[] jniMoveDetect(byte[] image, int width, int height, int channels, int imgType, long nowTime, int ocrFlag, int fingerFlag, int checkCode);

    native int jniMoveDetectFree();

}
