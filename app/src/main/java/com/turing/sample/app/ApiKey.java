package com.turing.sample.app;

import com.turing.os.init.UserData;
import com.turing.sample.app.base.BaseActivity;

/**
 * @Author yihuapeng
 * @Date 2019/12/27 14:52
 **/
public class ApiKey {
    private int type;
    private String apikey;
    private String secert;


    //图灵申请的AppKey和Secert
    private final static String ALPHA_KEY = "0b6c2dd6687a4c8cb73a3ca49ed9b3ca";
    private final static String ALPHA_SECERT = "xjX32402Uq1Vb001";

    private final static String BETA_KEY = "220bae7fe4ce46dcaa5fb70c950b9246";
    private final static String BETA_SECERT = "E56K05028vt36H98";

    private final static String PRO_KEY = "4279dab51f634bc5a4635133f2a8d71e";
    private final static String PRO_SECERT = "396Qt9qF072596o8";

    public ApiKey(int type) {
        this.type = type;
    }

    public String getApikey(){
        if(type == UserData.SERVER_ALPHA){
            return ALPHA_KEY;
        }else if(type == UserData.SERVER_BETA){
            return BETA_KEY;
        }else{
            return PRO_SECERT;
        }
    }

    public String getApiSecert(){
        if(type == UserData.SERVER_ALPHA){
            return ALPHA_SECERT;
        }else if(type == UserData.SERVER_BETA){
            return BETA_SECERT;
        }else{
            return PRO_SECERT;
        }
    }

}
