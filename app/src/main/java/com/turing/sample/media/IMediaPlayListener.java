package com.turing.sample.media;



public interface IMediaPlayListener {
    void onError(int code, String errorMsg);

    void onComplete();

    void onPrepared();

    void onStart();

    void onStop();
}
