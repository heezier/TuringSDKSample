package com.turing.sample.media;

import android.content.Context;
import android.media.MediaPlayer;
import android.util.Log;


import java.io.File;
import java.io.IOException;


public class MediaPlayerManager {

    private final static String TAG = "MediaPlayerManager";
    private MediaPlayer mediaPlayer;
    /**
     * 是否调用了停止
     */
    private boolean isStop = false;
    private IMediaPlayPositionListener iMediaPlayPositionListener;
    private IMediaPlayListener iMediaPlayListener;
    /**
     * 是否处于暂停状态
     */
    private boolean isPause = false;
    private boolean isError = false;
    private String curPathUrl;

    private static class ClassHolder {
        private final static MediaPlayerManager instance = new MediaPlayerManager();
    }

    public static MediaPlayerManager getInstance() {
        return ClassHolder.instance;
    }


    private MediaPlayerManager() {
        mediaPlayer = new MediaPlayer();
    }

    public void setiMediaPlayPositionListener(IMediaPlayPositionListener iMediaPlayPositionListener) {
        this.iMediaPlayPositionListener = iMediaPlayPositionListener;
    }

    public void setiMediaPlayListener(IMediaPlayListener iMediaPlayListener) {
        this.iMediaPlayListener = iMediaPlayListener;
    }

    /**
     * 开始播放
     */
    public void startPlay() {
        Log.d(TAG, "startPlay");
        if (mediaPlayer != null) {
            mediaPlayer.start();
            StateMachine.getInstance().setCurMediaState(StateMachine.MEDIA_STATE_MEDIAING);
            isPause = false;
//        mHandler.sendEmptyMessageDelayed(MessageConstants.MSG_MEDIA_POSITION, 1000);
            if (iMediaPlayListener != null) {
                iMediaPlayListener.onStart();
            }
        }
    }

    public String getCurPathUrl() {
        return curPathUrl;
    }

    /**
     * 准备播放
     *
     * @param pathURL
     */
    public void preparePlay(String pathURL) {
        Log.d(TAG, "Music URL:" + pathURL);
        curPathUrl = pathURL;
        try {
            isError = false;
            isStop = false;
            if (mediaPlayer != null) {
                mediaPlayer.release();
                mediaPlayer = null;
            }
            mediaPlayer = new MediaPlayer();
            setMediaPlayerListener();
            Log.d(TAG, "is URL LIKE:" + !StringUtil.isUrlLike(pathURL));
            if (!StringUtil.isUrlLike(pathURL)) {
                File file = new File(pathURL);
                if (file.exists()) {
                    mediaPlayer.setDataSource(pathURL);
                    mediaPlayer.prepare();
                }
            } else {
                mediaPlayer.setDataSource(pathURL);
                mediaPlayer.prepareAsync();
            }
        } catch (IOException e) {
            if (iMediaPlayListener != null) {
                iMediaPlayListener.onError(ErrorConstants.ERROR_CODE_MEDIA_URL_ERROR, "Music URL parse error");
            }
            Log.d(TAG, ErrorConstants.ERROR_CODE_MEDIA_URL_ERROR + "|Music URL parse error");
            e.printStackTrace();
        }
    }

    public int getDuration() {
        return mediaPlayer.getDuration();
    }

    public void startPlay(Context context, int resID) {
        Log.d(TAG, "Music resID:" + resID + "|name:" + context.getResources().getResourceEntryName(resID));
        isError = false;
        //在再次播放时重置是否停止的状态
        isStop = false;
        if (mediaPlayer != null) {
            try {
                if (mediaPlayer.isPlaying()) {
                    Log.d(TAG, "isPlaying");
                    mediaPlayer.stop();
                }
            } catch (Exception e) {
                e.printStackTrace();
            }

            mediaPlayer.release();
            mediaPlayer = null;
        }
        mediaPlayer = MediaPlayer.create(context, resID);
        if (mediaPlayer != null) {
            mediaPlayer.start();
            setMediaPlayerListener();
            mediaPlayer.start();
            if (iMediaPlayListener != null) {
                iMediaPlayListener.onStart();
            }
            isPause = false;
            StateMachine.getInstance().setCurMediaState(StateMachine.MEDIA_STATE_MEDIAING);
        } else {

            mediaPlayer = MediaPlayer.create(context, resID);
            if (mediaPlayer != null) {
                mediaPlayer.start();
                setMediaPlayerListener();
                mediaPlayer.start();
                if (iMediaPlayListener != null) {
                    iMediaPlayListener.onStart();
                }
                isPause = false;
                StateMachine.getInstance().setCurMediaState(StateMachine.MEDIA_STATE_MEDIAING);
            } else {
                Log.e(TAG, "MediaPlayer创建失败，请排查原因");
            }


        }

    }

    private void setMediaPlayerListener() {
        if (mediaPlayer != null) {
            mediaPlayer.setOnPreparedListener(new MediaPlayer.OnPreparedListener() {
                @Override
                public void onPrepared(MediaPlayer mediaPlayer) {
                    Log.d(TAG, "Music isStop:" + isStop);
                    //如果在准备的过程中,已经调用了停止,则处于停止状态,不再开始
                    if (iMediaPlayListener != null && !isStop) {
                        iMediaPlayListener.onPrepared();
                    }
                }
            });
            mediaPlayer.setOnCompletionListener(new MediaPlayer.OnCompletionListener() {
                @Override
                public void onCompletion(MediaPlayer mediaPlayer) {
                    Log.d(TAG, "onCompletion");
                    StateMachine.getInstance().setCurMediaState(StateMachine.MEDIA_STATE_IDLE);
                    if (iMediaPlayListener != null && !isError) {
                        iMediaPlayListener.onComplete();
                        isPause = false;
                    }
//                    mHandler.removeCallbacksAndMessages(null);
                }
            });
            mediaPlayer.setOnErrorListener(new MediaPlayer.OnErrorListener() {
                @Override
                public boolean onError(MediaPlayer mediaPlayer, int i, int i1) {
                    Log.d(TAG, ErrorConstants.ERROR_CODE_MEDIA_PLAY_ERROR + "|code:" + i + "|code2:" + i1);
                    isError = true;
                    if (iMediaPlayListener != null) {
                        iMediaPlayListener.onError(ErrorConstants.ERROR_CODE_MEDIA_PLAY_ERROR, "play error|code:" + i + "|code2:" + i1);
                    }
                    StateMachine.getInstance().setCurMediaState(StateMachine.MEDIA_STATE_IDLE);
                    stopPlay();
                    isPause = false;
//                    mHandler.removeCallbacksAndMessages(null);
                    return false;
                }
            });
        }
    }

    /**
     * 停止播放
     */
    public void stopPlay() {
        isStop = true;
        if (mediaPlayer != null) {
            try {
                if (mediaPlayer.isPlaying()) {
                    mediaPlayer.stop();
                }
            } catch (Exception e) {
                e.printStackTrace();
            }


            mediaPlayer.reset();
            Log.d(TAG, "stopPlay");
            isPause = false;
            if (iMediaPlayListener != null) {
                iMediaPlayListener.onStop();
            }
            StateMachine.getInstance().setCurMediaState(StateMachine.MEDIA_STATE_IDLE);
        }

//        mHandler.removeCallbacksAndMessages(null);
    }

    /**
     * 暂停播放
     */
    public void pausePlay() {
        if (mediaPlayer.isPlaying()) {
            Log.d(TAG, "pausePlay");
            mediaPlayer.pause();
//            mHandler.removeCallbacksAndMessages(null);
            isPause = true;
            StateMachine.getInstance().setCurMediaState(StateMachine.MEDIA_STATE_IDLE);
        }
    }

    /**
     * 恢复播放
     */
    public void resumePlay() {
        if (!mediaPlayer.isPlaying()) {
            Log.d(TAG, "resumeplay");
            mediaPlayer.start();
            isPause = false;
//            mHandler.sendEmptyMessageDelayed(MessageConstants.MSG_MEDIA_POSITION, 1000);
            StateMachine.getInstance().setCurMediaState(StateMachine.MEDIA_STATE_MEDIAING);
        }
    }

    public void releasePlayer() {
        if (mediaPlayer != null) {
            mediaPlayer.release();
        }
    }

    public boolean isPlaying() {
        return mediaPlayer.isPlaying();
    }

    public boolean isPause() {
        return isPause;
    }
}
