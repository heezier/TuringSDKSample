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
    private final static String ALPHA_KEY = "";
    private final static String ALPHA_SECERT = "";

    private final static String BETA_KEY = "";
    private final static String BETA_SECERT = "";

    private final static String PRO_KEY = "";
    private final static String PRO_SECERT = "";
    public ApiKey(int type) {
        this.type = type;
    }

    public String getApikey(){
        if(type == UserData.SERVER_ALPHA){
            return ALPHA_KEY;
        }else if(type == UserData.SERVER_BETA){
            return BETA_KEY;
        }else{
            return PRO_KEY;
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
