package com.turing.sample.media;



public class StateMachine {
    /**
     * 没有初始化
     */
    public final static int STATE_NOT_INIT = 0;
    /**
     * 校验失败
     */
    public final static int STATE_AUTHORITY_ERROR = STATE_NOT_INIT + 1;
    /**
     * 正在初始化
     */
    public final static int STATE_INITING = STATE_AUTHORITY_ERROR + 1;
    /**
     * 空闲状态
     */
    public final static int STATE_IDLE = STATE_INITING + 1;
    /**
     * 正在ASR
     */
    public final static int STATE_ASRING = STATE_IDLE + 1;
    /**
     * 正在TTS
     */
    public final static int STATE_TTSING = STATE_ASRING + 1;
    /**
     * 正在网络请求
     */
    public final static int STATE_SEMANTICING = STATE_TTSING + 1;

    /**
     * 音乐状态空闲
     */
    public final static int MEDIA_STATE_IDLE = 0;
    /**
     * 正在播放音乐
     */
    public final static int MEDIA_STATE_MEDIAING = MEDIA_STATE_IDLE + 1;

    /**
     * 当前机器人状态
     */
    private int curState = STATE_NOT_INIT;
    /**
     * 当前机器人媒体播放状态
     */
    private int curMediaState = MEDIA_STATE_IDLE;

    /**
     * 状态附加消息
     */
    private String stateMessage = "";

    private StateMachine() {

    }

    private final static class HolderClass {
        private final static StateMachine INSTANCE = new StateMachine();
    }

    public static StateMachine getInstance() {
        return HolderClass.INSTANCE;
    }

    /**
     * 获取当前的状态
     *
     * @return
     */
    public int getCurState() {
        return curState;
    }

    /**
     * 设置当前状态
     *
     * @param curState
     */
    public void setCurState(int curState) {
        this.curState = curState;
    }

    /**
     * 获取当前的媒体状态
     *
     * @return
     */
    public int getCurMediaState() {
        return curMediaState;
    }

    /**
     * 设置当前的媒体状态
     *
     * @param curMediaState
     */
    public void setCurMediaState(int curMediaState) {
        this.curMediaState = curMediaState;
    }

    public String getStateMessage() {
        return stateMessage;
    }

    public void setStateMessage(String stateMessage) {
        this.stateMessage = stateMessage;
    }

}
