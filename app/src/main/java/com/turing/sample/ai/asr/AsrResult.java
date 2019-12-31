package com.turing.sample.ai.asr;

import java.io.Serializable;

/**
 * @Author yihuapeng
 * @Date 2019/12/27 10:58
 **/
public class AsrResult implements Serializable {
    private String result;
    private String asrResult;

    public AsrResult(String result, String asrResult) {
        this.result = result;
        this.asrResult = asrResult;
    }

    public String getResult() {
        return result;
    }

    public void setResult(String result) {
        this.result = result;
    }

    public String getAsrResult() {
        return asrResult;
    }

    public void setAsrResult(String asrResult) {
        this.asrResult = asrResult;
    }
}
