package com.turing.sample.ai.chat;

import androidx.annotation.IntDef;

import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;

/**
 *
 * @Date 2019/12/2 19:58
 **/
public class Msg {
    public final static int RECEIVED = 0;
    public final static int SENT = 1;

    /**
     * 内容
     */
    private String content;

    @IntDef({RECEIVED, SENT})
    @Retention(RetentionPolicy.SOURCE)
    public @interface TYPE {
    }


    /**
     * 类型
     */
    private int type;


    public Msg(String content,@TYPE int type){
        this.content = content;
        this.type = type;
    }

    @TYPE
    public int getType() {
        return type;
    }

    public String getContent() {
        return content;
    }

    public void setContent(String content) {
        this.content = content;
    }
}
