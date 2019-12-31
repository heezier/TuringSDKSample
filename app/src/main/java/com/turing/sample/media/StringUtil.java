package com.turing.sample.media;

import java.util.regex.Pattern;


public class StringUtil {
    public static final String strRegUrl = "^((http|https)).*";
    /**
     * 判断是否为Url的字符串
     *
     * @param str 目标字符
     * @return
     */
    public static boolean isUrlLike(String str) {
        if (str == null){
            return false;
        }
        return Pattern.compile(strRegUrl).matcher(str).matches();
    }

    /**
     * 去掉字符串中的字母和符号
     *
     * @param s 需要处理的字符串
     * @return
     */
    public static String format(String s) {

        String str = s.replaceAll("[`qwertyuiopasdfghjklzxcvbnmQWERTYUIOPASDFGHJKLZXCVBNM~!@#$%^&*()+=|{}':;',\\[\\].<>/?~！@#￥%……& amp;*（）——+|{}【】‘；：”“’。，、？|-]", "");
        return str;
    }
    public static void main(String[] args){
        System.out.println("isUrlLike:"+isUrlLike("http://universe-file-limit.tuling123.com/201907291729/1c1eb289e5e6875c6320e59fa95c63f1/title/12960.mp3"));
        System.out.println("isUrlLike:"+isUrlLike("http://universe-file-limit.tuling123.com/201907291732/73a3f874b919b4fda8cce63a83831882/title/7026.mp3"));
    }
}
