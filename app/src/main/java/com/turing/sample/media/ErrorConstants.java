package com.turing.sample.media;



public class ErrorConstants {

    public final static int ERROR_CODE_AUTHORITY_INIT = 1000;


    public final static int ERROR_CODE_ASR_INIT = 2000;


    public final static int ERROR_CODE_TTS_INIT = 3000;


    public final static int ERROR_CODE_SEMANTIC_SERVER_NULL = 4000;
    public final static int ERROR_CODE_SEMANTIC_SERVER_FAIL = ERROR_CODE_SEMANTIC_SERVER_NULL + 1;

    public final static int ERROR_CODE_MEDIA_URL_ERROR = 5000;
    public final static int ERROR_CODE_MEDIA_PLAY_ERROR = ERROR_CODE_MEDIA_URL_ERROR + 1;

    /**
     * 封面查询服务器异常
     */
    public final static int ERROR_CODE_COVER_SERVER = 10000;
    /**
     * 封面查询 检测图片没有移动、变化
     */
    public final static int ERROR_CODE_COVER_NOTMOVE = ERROR_CODE_COVER_SERVER + 1;
    /**
     * 封面查询 检测图片为空
     */
    public final static int ERROR_CODE_COVER_BITMAP_NULL = ERROR_CODE_COVER_SERVER + 2;
    /**
     * 封面查询结果中音频URL为空
     */
    public final static int ERROR_CODE_COVER_URL_NULL = ERROR_CODE_COVER_SERVER + 3;
    /**
     * 封面查询 请求失败
     */
    public final static int ERROR_CODE_COVER_NET = ERROR_CODE_COVER_SERVER + 4;
    /**
     * 封面查询时参数为空
     */
    public final static int ERROR_CODE_COVER_PAMAM_NULL = ERROR_CODE_COVER_SERVER + 5;

    /**
     * 内页查询服务器异常
     */
    public final static int ERROR_CODE_PAGE_SERVER = 20000;
    /**
     * 内页查询 检测图片没有移动、变化
     */
    public final static int ERROR_CODE_PAGE_NOTMOVE = ERROR_CODE_PAGE_SERVER + 1;
    /**
     * 内页查询 检测图片为空
     */
    public final static int ERROR_CODE_PAGE_BITMAP_NULL = ERROR_CODE_PAGE_SERVER + 2;
    /**
     * 封面查询 请求失败
     */
    public final static int ERROR_CODE_PAGE_NET = ERROR_CODE_PAGE_SERVER + 3;
    /**
     * 封面查询时参数为空
     */
    public final static int ERROR_CODE_PAGE_PAMAM_NULL = ERROR_CODE_PAGE_SERVER + 4;
}
