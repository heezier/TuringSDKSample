# SDK 开发者文档

## 简介及运行环境

### 概述

SDK V2使用图灵websocket接口，集成了图灵云端的ASR(语音识别)、TTS(在线语音合成)、NLP（语义理解）、绘本识别、AI对话功能。

### 创建机器人

开发集成儿童版SDK前需要申请APIKey与Secret,请在平台地址 [图灵官网](http://biz.turingos.cn/login.html) 注册登录。在机器人管理界面中创建机器人,并在机器人信息界面获取APIKey与Secret

### 兼容性

| 类别      | 兼容范围                                                     |
| --------- | ------------------------------------------------------------ |
| 系统      | 支持Android 4.1 以上版本 API LEVEL 16                        |
| 支持的ABI | 使用VAD和翻页检测功能仅支持armeabi-v7a，不使用则全支持       |
| 硬件要求  | 要求设备上有麦克风                                           |
| 网络      | 支持移动网络、WIFI等网络环境                                 |
| 开发环境  | 建议使用最新版本Android Studio 进行开发（SDK开发使用Android 3.2） |

建议开发环境：

```
AndroidStudio 3.5.2
Gradle 4.6
com.android.tools.build:gradle:3.2.0
buildToolsVersion "29.0.2"
```



### SDK库文件

| 资源名称              | 资源大小 | 资源描述 |
| --------------------- | -------- | :------- |
| turingsdk-release.aar | 5.17 MB  | aar 库   |

## 集成指南

### 添加依赖库

将turingsdk-release.aar复制到您的项目的app/libs/目录下

修改您的项目的app/build.gradle文件，将下面依赖库添加到您的依赖目录中。

```xml
implementation(name: 'turingsdk-release', ext: 'aar')
//必须依赖库
implementation 'com.google.code.gson:gson:2.6.2'
implementation 'com.squareup.okhttp3:okhttp:3.9.0'
implementation "org.java-websocket:Java-WebSocket:1.4.0"

```

### AndroidManifest.xml 权限

```xml
	<!--网络-->
    <uses-permission android:name="android.permission.INTERNET" />
    <uses-permission android:name="android.permission.ACCESS_NETWORK_STATE" />
    <uses-permission android:name="android.permission.ACCESS_WIFI_STATE" />
        <uses-permission android:name="android.permission.CHANGE_WIFI_STATE" />
    <!--文件读写-->
    <uses-permission android:name="android.permission.WRITE_EXTERNAL_STORAGE" />
    <uses-permission android:name="android.permission.READ_EXTERNAL_STORAGE" />
    <!--录音 -->
    <uses-permission android:name="android.permission.RECORD_AUDIO" />
    <!--deviceID-->
    <uses-permission android:name="android.permission.READ_PHONE_STATE" />
```

备注：android 6.0 以上版本权限需动态申请。

## 功能使用

### 初始化

#### 一、代码配置方式

如果调用了带有deviceID（设备的唯一值）参数的方法初始化，请开发者确保输入的deviceID唯一且不会改变。

如果调用了不带deviceID参数的方法初始化，SDK会默认读取设备的Mac地址作为设备唯一ID，如果成功读取，将会在SdkInitializerListener中返回相应的设备ID，具体见下面SdkInitializerListener说明。

```java
	 /**
     * 
     * @param context 应用的Context
     * @param apiKey 图灵开放平台注册的key
     * @param secert 图灵开放平台注册的secert
     * @param sdkInitializerListener
     */
	public static void init(Context context, String apiKey, String secert, SdkInitializerListener sdkInitializerListener)
    
    /**
     * 
     * @param deviceID 用户设备的唯一ID
     * @param context 应用的Context
     * @param apiKey 图灵开放平台注册的key
     * @param secert 图灵开放平台注册的secert
     * @param sdkInitializerListener
     */
    public static void init(String deviceID, Context context, String apiKey,
                            String secert, SdkInitializerListener sdkInitializerListener)
        
        //调用
        SdkInitializer.init(context, sdkInitializerListener);
```



参数说明

| 参数                   | 类型                   | 是否必须 | 说明                |
| ---------------------- | ---------------------- | -------- | ------------------- |
| context                | Context                | 是       | 应用的Context       |
| turingApiKey           | String                 | 是       | 图灵授权的apikey    |
| turingSecret           | String                 | 是       | 图灵授权的secret    |
| deviceID               | String                 | 否       | 默认取设备的mac地址 |
| authenticationListener | AuthenticationListener | 是       | 鉴权回调            |

#### 二、 AndroidManifest.xml配置方式 

```
<meta-data
    android:name="TURING_APPKEY"
    android:value="图灵授权的APIKEY" />
<meta-data
    android:name="TURING_SECRET"
    android:value="图灵授权的SECRET" />

```

| 参数          | 类型   | 是否必须 | 说明             |
| ------------- | ------ | -------- | ---------------- |
| TURING_APPKEY | String | 是       | 图灵授权的apikey |
| TURING_SECRET | String | 是       | 图灵授权的secret |

调用初始化方法，根据配置的参数初始化。

如果调用了带有deviceID参数的方法初始化，请开发者确保每一台设备输入的deviceID唯一且不会改变。

如果调用了不带deviceID（设备唯一ID）的方法初始化，SDK会默认读取设备的Mac地址作为设备唯一ID，如果成功读取，将会在SdkInitializerListener中返回相应的设备ID，具体见下面SdkInitializerListener说明。

```java
	

	//方法说明
	/**
     * 
     * @param context
     * @param sdkInitializerListener
     */
    public static void init(Context context, SdkInitializerListener sdkInitializerListener)
	
    /**
     * 
     * @param deviceID
     * @param context
     * @param sdkInitializerListener
     */
    public static void init(String deviceID, Context context, SdkInitializerListener sdkInitializerListener)
```

SdkInitializerListener说明

```java
public interface SdkInitializerListener {
   /**
     * 
     * @param type "custom_id" or "mac" 传入devceID则为custom_id，否则为mac
     * @param userData 具体见UserData说明
     */
    void onSuccess(String type, UserData userData);

    /**
     * 初始化失败回调
     *
     * @param errorCode 错误码,错误码见{@link com.turing.os.log.TuringErrorCode}
     * @param errorMsg  鉴权失败信息
     */
    void onError(int errorCode, String errorMsg);
}
```

UserData说明

| 参数                                        | 类型   | 是否必须 | 说明             |
| ------------------------------------------- | ------ | -------- | ---------------- |
| key                                         | String | 是       | 图灵授权的apikey |
| secert                                      | String | 是       | 图灵授权的secret |
| deviceID                                    | String | 否       | 设备唯一ID       |
| Location{String latitude;String longitude;} | Object | 否       | 地理坐标         |

备注：Location参数为可扩展参数，具体的赋值开发者自行获取并初始化。

### 获取SDK版本号

```java
	SdkInitializer.getVersion();
```

### 打开SDK Debug日志

```java
	/**
     * 
     * @param dubugPath 日志关键文件的保存路径 不能为空，或者不合法的路径
     * @param isDebug 是否打开debug日志，默认为false
     */
	SdkInitializer.setDebug(path, true)
```

### 设置环境

设置服务器环境，便于集成SDK时调试。

```java
	/**
     * 设置服务器环境，便于集成SDK时调试
     * @param type
     */
 	public static void setServer(@UserData.ServerChannel int type);
 	
 	//调用
 	SdkInitializer.setServer(UserData.SERVER_ALPHA);
```

| 参数 | 类型                                                         | 默认值                |
| ---- | ------------------------------------------------------------ | --------------------- |
| type | UserData.ServerChannel{SERVER_ALPHA, SERVER_BETA, SERVER_PRODUCT} | UserData.SERVER_ALPHA |



### 创建TuringOSClient

TuringOSClient是SDK提供外部功能的管理类，每个模块的功能调用都需要使用TuringOSClient实例开启，TuringOSClient在应用中为单例。

```java
//创建
TuringOSClient turingOSClient = TuringOSClient.getInstance(mContext, userData);
```

| 参数     | 类型     | 说明                                                    |
| -------- | -------- | ------------------------------------------------------- |
| mContext | Context  | 应用的Context                                           |
| userData | UserData | SDK内部封装的用户信息，SdkInitializerListener中的返回值 |

### ASR调用

#### 相关参数说明

TuringOSClientAsrListener说明：SDK中使用到ASR功能（语音识别）的所有模块都会使用该接口回调结果

```java
public interface TuringOSClientAsrListener {

    /**
     * 调用startChatWithRecord之后开始录音回调
     */
    void onRecorderStart();
    
    /**
     * asr识别停止，包括请求Error或者外部调用client.stopAsr都会回调
     */
    void onStop();
    
    /**
     * 允许输入音频数据流，在该方法回调之后开始调用sendAudio
     */
    void onStreamOpen();

    /**
     *
     * 请求结果返回
     *
     * @param code 具体参考Opensocket协议中的输出说明
     * @param result 返回的json字符串
     * @param isLast Asr识别结果；last : 是否是最终结果
     * @param responBean  返回的json字符串转换的Java对象
     */
    void onResult(int code, String result, boolean isLast, ResponBean responBean);

    /**
     * Asr识别单论交互倒计时 最大值20s
     *
     * @param second
     */
    void onTimer(int second);

    /**
     * 请求发生错误的信息
     *
     * @param code
     * @param msg
     */
    void onError(int code, String msg);

}
```

AsrRequestConfig说明：Asr识别必要参数

| 参数                 | 类型                                 | 默认值          | 说明                                                 |
| -------------------- | ------------------------------------ | --------------- | ---------------------------------------------------- |
| asrSrcFormatEnum     | {PCM, OPUS, OPU, SPEEX}              | 无              | 当不使用内部录音器的时候该值为必填                   |
| asrFormatEnum        | {PCM, OPUS, OPU, SPEEX}              | PCM             | 上传至云端的音频格式，**<u>目前仅支持PCM和OPUS</u>** |
| asrLanguageEnum      | {CHINESE, ENGLISH}                   | CHINESE         | 识别语言类型                                         |
| asrRateEnum          | {RATE_8000, RATE_16000}              | RATE_16000      | 音频采样率                                           |
| channel              | {CHANNEL_IN_MONO, CHANNEL_IN_STEREO} | CHANNEL_IN_MONO | CHANNEL_IN_MONO：单声道；CHANNEL_IN_STEREO：双声道   |
| enableITN            | boolean                              | false           |                                                      |
| enablePunctuation    | boolean                              | false           |                                                      |
| enableVoiceDetection | boolean                              | fasle           | 是否开启VAD                                          |
| intermediateResult   | boolean                              | true            | 是否打开中间返回结果                                 |
| maxEndSilence        | int                                  | 2000            | VAD前端检测值，单位为毫秒                            |
| maxStartSilence      | int                                  | 800             | VAD末端检测值，单位为毫秒                            |

AsrRequestConfig参数的配置：

```java
 		AsrRequestConfig.Builder builder = new AsrRequestConfig.Builder();
        builder.asrLanguageEnum(AsrRequestConfig.CHINESE);
        builder.asrFormatEnum(AsrRequestConfig.PCM);
        builder.asrSrcFormatEnum(AsrRequestConfig.PCM);
        builder.asrRateEnum(AsrRequestConfig.RATE_16000);
        builder.enablePunctuation(false);
        builder.intermediateResult(true);
        builder.maxEndSilence(3000);
        /**
         * CHANNEL_IN_MONO或者CHANNEL_IN_STEREO
         */
        builder.channel(AsrRequestConfig.CHANNEL_IN_MONO);
        builder.enableVoiceDetection(false);

        AsrRequestConfig asrRequestConfig = builder.build();

```



#### 开启ASR调用方法

##### 一、无参输入使用SDK内部录音进行ASR识别

AsrRequestConfig会默认创建，使用默认值

```java
	/**
     * 
     * @param isLoop 是否循环录音识别
     * @param listener TuringOSClientAsrListener
     */
    public void startAsrWithRecorder(boolean isLoop, TuringOSClientAsrListener listener)
	
	//调用
	turingOSClient.startChatWithRecord(false, listener);
	
```



##### 二、有参输入使用SDK内部录音进行ASR识别

```java
	/**
     * 
     * @param isLoop 是否循环录音识别
     * @param asrRequestConfig  AsrRequestConfig
     * @param listener TuringOSClientAsrListener
     */
    public void startAsrWithRecorder(boolean isLoop, final AsrRequestConfig asrRequestConfig, final TuringOSClientAsrListener listener)
	
	//调用
	turingOSClient.startChatWithRecord(asrRequestConfig, listener);
```

注意：

调用该方法时，asrRequestConfig中的asrFormatEnum取值只能为PCM或者OPUS。在使用内部录音器时，asrFormatEnum值为PCM时，会将录音的PCM数据上传进行识别，当配置为OPUS时，则会经过SDK内部编码为OPUS再上传至云端识别。



##### 三、输入音频数据流进行识别

方法说明

```java
	
    /**
     * 
     * @param asrRequestConfig asrRequestConfig
     * @param listener TuringOSClientAsrListener
     */
    public void initAsrStream(final AsrRequestConfig asrRequestConfig, final TuringOSClientAsrListener listener)
	
	 /**
     * 
     * @param dataBuffer 音频数据
     * @param length 音频数据长度
     */
    public void sendAudio(byte[] dataBuffer, int length)
	
```

调用示例

```java
    //第一步：参数配置
    AsrRequestConfig.Builder builder = new AsrRequestConfig.Builder();
    builder.asrLanguageEnum(AsrRequestConfig.CHINESE);

    //必须参数，代表最终上传服务器请求的格式参数，必须与传入可支持的格式对应
    builder.asrFormatEnum(AsrRequestConfig.OPUS);

	//必须参数，代表音频源的格式
    builder.asrSrcFormatEnum(AsrRequestConfig.OPUS);
    builder.asrRateEnum(AsrRequestConfig.RATE_16000);
    builder.enablePunctuation(false);
    builder.maxEndSilence(3000);
    builder.channel(AsrRequestConfig.CHANNEL_IN_MONO);

    AsrRequestConfig asrRequestConfig = builder.build();
    turingOSClient = TuringOSClient.getInstance(this, userData);	


	//第二步：初始化
	/**
     *
     * @param asrRequestConfig AsrRequestConfig
     * @param listener TuringOSClientAsrListener
     */
	turingOSClient.initAsrStream(asrRequestConfig, new TuringOSClientAsrListener() {

            @Override
            public void onRecorderStart() {

            }

            @Override
            public void onStop() {
               
            }

            @Override
            public void onStreamOpen() {
            	//第二步：再数据流回调方法中开始输入数据
                startStreamPcmInput();
            }


            @Override
            public void onResult(int code, String result, boolean isLast,ResponBean 					responBean) {
                //识别结果

            }

            @Override
            public void onTimer(int second) {
            	 
            }

            @Override
            public void onError(int code, String msg) {
               
            }
        });
        
        
     
      //注意事项：在音频流关闭之后调用turingOSClient.stopAsr();
      private void startStreamPcmInput() {
            new Thread(pcmRun).start();
        }
      	//注意事项：在音频流关闭之后调用turingOSClient.stopAsr();
       private Runnable pcmRun = new Runnable() {
        @Override
        public void run() {
            try {
                InputStream inputStream = getAssets().open("record.pcm");
                while (!isPcmStop) {
                    byte[] buffer = new byte[320];
                    int length = inputStream.read(buffer);
                    if (length == -1) {
                        turingOSClient.stopAsr();
                        break;
                    }
                    if (length == 320) {
                        //第三步 发送数据
                        turingOSClient.sendAudio(buffer, length);
                    } else {
                        turingOSClient.sendAudio(Arrays.copyOf(buffer, length), length);
                    }
                }
   
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
    };
```



##### 四、流输入方式说明

在调用流输入方式时，asrFormatEnum和asrSrcFormatEnum为必须参数。当将要输入的音频数据源为PCM时，则asrSrcFormatEnum为PCM，对应的asrFormatEnum的可能值只能为PCM或者OPUS，同内部录音器，SDK内部默认支持OPUS编码。当asrSrcFormatEnum为其他值时，对应的asrFormatEnum必须与asrSrcFormatEnum相等。



#### ASR识别输出

返回的结果在TuringOSClientAsrListener的onResult方法中，其中String result为json字符串，ResponBean responBean为result转换为json后的java对象。

json示例如下：

```json
{
    "asrResponse":{
        "binarysId":"0a2071a8-2bb6-4fae-b109-32bee00c1726",
        "state":200,
        "value":"深圳明天的天气"
    },
    "code":200,
    "globalId":"98412368730000000",
    "message":"success"
}
```

**asrResponse(数组) 参数说明**

| 参数      | 类型   | 是否必须 | 取值范围 | 说明                                        |
| :-------- | :----- | :------- | :------- | :------------------------------------------ |
| binarysId | String | Y        | -        | asr二进制参数Id                             |
| value     | String | Y        | -        | asr识别内容                                 |
| state     | int    | Y        | 200,210  | 210:asr识别中间结果返回,200:asr识别完成结果 |

#### 安全停止ASR

```java
	/**
	 *方法说明
     *该方法会将音频数据池中的数据发送完毕后正常结束Asr引擎
     *
     */
     public void stopAsr()
     
     //调用示例
	turingOSClient.stopAsr();
```

#### 释放releaseAsr()

```java
	/**
     *该方法会强制停止释放Asr引擎
     *
     */
     public void release()
     
     //调用示例
	turingOSClient.release();
```

### TTS调用

#### 相关参数说明

TuringOSClientListener说明：TTS、NLP、文字类型的AI对话、绘本识别都使用该接口回调结果

```java
	
	public interface TuringOSClientListener {    
         /**
         *
         * 请求结果返回
         *
         * @param code 具体参考Opensocket协议中的输出说明
         * @param result 返回的json字符串
         * @param isLast Asr识别结果；last : 是否是最终结果
         * @param responBean  返回的json字符串转换的Java对象
         *@param extension  扩展参数
         */
        void onResult(int code, String result, ResponBean responBean, String extension);   
        
        
        /**
         * 请求发生错误的信息
         *
         * @param code
         * @param msg
         */
        void onError(int code, String msg);
    }

```

字符切割方法

```java
    //对用户输入的字符串长度进行切割，保证每一次请求的文字长度合法
    public static SparseArray<String> split(String text)
    
    //调用
    SparseArray<String> textList = turingOSClient.SparseArray(text);
```

#### TTS调用

方法说明

```java
    /** 
    * 
    * @param text 需要转语音的文字
    * @param listener TuringOSClientListener
    * @return 你输入的字符串会被切割为SparseArray<String>
    */
    public SparseArray<String> actionTts(String text, TuringOSClientListener listener)
```

调用

```java

    //返回的语音MP3地址会按照该textList对应的文字顺序返回
    SparseArray<String> textList = turingOSClient.actionTts(text, listener);

```

#### TTS输出

具体看TuringOSClientListener接口的onResult方法说明。

json示例如下：

```json
{
    "code":200,
    "globalId":"98412556156000000",
    "message":"success",
    "nlpResponse":{
        "intent":{
            "code":50101
        },
        "results":[
            {
                "groupType":0,
                "resultType":"text",
                "values":{
                    "ttsUrl":[
                        "http://turing-iot.oss-cn-beijing.aliyuncs.com/tts/tts-0b6c2dd6687a4c8cb73a3ca49ed9b3ca-ef856c768eb2495bafce0fd01eeade93.mp3"
                    ],
                    "text":"北京今天天气怎么样?"
                }
            }
        ]
    }
}
```

备注：ttsUrl为文字转语音之后的mp3播放地址。

#### TTS播放

SDK内部集成了播放器，开发者可根据自身业务逻辑选择使用内部或者自行播放。

```java

    //第一步：创建播放池
    //textList为client.SparseArray(text)切割的结果
    TtsPlayerPool ttsPlayerPool = TtsPlayerPool.create(textList.size());

    //第二步：依次将MP3播放地址传入
    ttsPlayerPool.addPlayUrl(url);

    //第三步：开始播放
    ttsPlayerPool.start();

    //停止播放
    ttsPlayerPool.stop();

```

### NLP调用

#### 相关参数说明

NlpRequestConfig

| 参数         | 类型                             | 是否必须 | 默认值 | 说明                                                         |
| ------------ | -------------------------------- | -------- | ------ | ------------------------------------------------------------ |
| appStateBean | AppStateBean                     | 否       | -      |                                                              |
| codes        | List<Integer>                    | 否       | -      | 用户此次交互仅使用该参数中的应用（[具体参考TuringOS 文档中心](http://docs.turingos.cn/api/apiV2/)） |
| robotSkills  | Map<String, Map<String, Object>> | 否       | -      |                                                              |

AppStateBean

| 参数         | 类型 | 默认值 | 说明                                                         |
| ------------ | ---- | ------ | ------------------------------------------------------------ |
| code         | int  | 0      | 应用code（[具体参考TuringOS 文档中心](http://docs.turingos.cn/api/apiV2/)） |
| operateState | int  | 0      | 应用状态值                                                   |

NlpRequestConfig参数创建（具体参数可参考TuringSDKSample）

```java
    AppStateBean appStateBean = new AppStateBean();
    appStateBean.setOperateState(0);
    appStateBean.setCode(0);

    NlpRequestConfig.Builder builder = new 									    							NlpRequestConfig.Builder();
	builder.appStateBean(appStateBean);		
	
```



#### NLP调用

方法说明

```java
    /** 
    * 
    * @param text 限定合法的最大长度为100
    * @param requestConfig NlpRequestConfig
    * @param listener TuringOSClientListener
    */
   public void actionNlp(String text, NlpRequestConfig requestConfig, 					TuringOSClientListener listener)
       
   /** 
    * 
    * @param text 限定合法的最大长度为100
    * @param listener TuringOSClientListener
    */
   public void actionNlp(String text, TuringOSClientListener listener)
       
    /**
     *请求主动交互,用于让机器人主动发起交互的场景,返回的结果可以在图灵平台进行配置
     * @param listener
     */
    public void actionAutoConversion(TuringOSClientListener listener)
       
    /**
     * 请求打招呼接口,一般用于开机或机器人的第一次交互,返回的结果可以在图灵平台进行配置
     * @param listener
     */
    public void actionFirstConversion(TuringOSClientListener listener)
```

调用示例

```java

    turingOSClient.actionNlp(text, listener);
	//or
    turingOSClient.actionNlp(text, requestConfig, listener);

```

#### NLP输出

具体返回结果请看TuringOSClientListener接口的onResult方法说明。

参数详细说明可以参考（[具体参考TuringOS 文档中心](http://docs.turingos.cn/api/apiV2/)）。

json示例如下：

```json
{
    "code":200,
    "globalId":"98412738103000000",
    "message":"success",
    "nlpResponse":{
        "intent":{
            "code":100000,
            "operateState":1010
        },
        "results":[
            {
                "groupType":0,
                "resultType":"text",
                "values":{
                    "emotionId":0,
                    "sentenceId":201,
                    "text":"你好呀，乖宝宝。"
                }
            }
        ]
    }
}
```



#### 口语评测

```java
/**
*  
* @param word 口语测评的单词
* @param listener 
*/
public void startEAWithRecorder(String word, TuringOSClientListener listener)
```



### AI对话调用

#### 参数说明

TuringOSClientListener、NlpRequestConfig、AsrRequestConfig同上。

##### 一、无参文字输入的AI对话

带有NlpRequestConfig参数的方法意指可以指定此次对话使用的技能

```java
	/**
     * 方法说明
     * @param text 对话输入的文字
     * @param listener TuringOSClientAsrListener
     */
	public void actionChat(String text, TuringOSClientListener listener)
	
	//调用
	turingOSClient.actionChat(text, listener);

	/**
     * 方法说明
     * @param text 对话输入的文字
     * @param NlpRequestConfig 同NLP中的NlpRequestConfig
     * @param TuringOSClientListener 
     */
	public void actionChat(String text, NlpRequestConfig requestConfig, 						TuringOSClientListener listener)
	
	//调用
	turingOSClient.actionChat(text, requestConfig, listener);
	
```

##### 二、无参输入使用SDK内部录音进行AI对话

AsrRequestConfig会默认创建，使用默认值。

enableTts意指是否需要将对话结果合成TTS，文字输入AI对话时默认合成TTS。

```java
	/**
     *
     * @param enableTts 是否合成TTS
     * @param listener
     */
    public void startChatWithRecord(boolean enableTts, final TuringOSClientAsrListener listener)
	
	//调用
	turingOSClient.startChatWithRecord(false, listener);
	
```

##### 三、有参输入使用SDK内部录音进行AI对话

```java
	 /**
     * 
     * @param enableTts 是否合成TTS
     * @param asrRequestConfig 不能为null
     * @param nlpRequestConfig 同NLP中的NlpRequestConfig，可以为null
     * @param listener
     */
    public void startChatWithRecord(boolean enableTts, AsrRequestConfig asrRequestConfig, NlpRequestConfig nlpRequestConfig, TuringOSClientAsrListener listener)
	
	//调用  nlpRequestConfig为可配置项，可以为null
	turingOSClient.startChatWithRecord(true, asrRequestConfig, null, listener);
```

##### 四、输入音频流进行AI对话

方法说明

```java
	 /**
     * 
     * @param enableTts 是否合成TTS
     * @param asrRequestConfig 不能为null
     * @param nlpRequestConfig 同NLP中的NlpRequestConfig，可以为空
     * @param listener
     */
	public void initChatStream(boolean enableTts, AsrRequestConfig asrRequestConfig, NlpRequestConfig nlpRequestConfig, TuringOSClientAsrListener listener)
	
	/**
     * 
     * @param dataBuffer 音频数据
     * @param length 音频数据长度
     */
    public void sendAudio(byte[] dataBuffer, int length)
	
```

调用示例

```java
	//asrRequestConfig创建规则以及参数说明同ASR中音频流输入
	//第一步 初始化
	turingOSClient.initChatStream(true，asrRequestConfig, null, new TuringOSClientAsrListener() {

            @Override
            public void onRecorderStart() {

            }

            @Override
            public void onStop() {
               
            }

            @Override
            public void onStreamOpen() {
            	//第二步：再数据流回调方法中开始输入数据
                startStreamPcmInput();
            }


            @Override
            public void onResult(int code, String result, boolean isLast,ResponBean 					responBean) {
                //识别结果

            }

            @Override
            public void onTimer(int second) {
            	
            }

            @Override
            public void onError(int code, String msg) {
               
            }
        });
        
        
     
        private void startStreamPcmInput() {
            new Thread(pcmRun).start();
        }
      	//注意事项：在音频流关闭之后调用turingOSClient.stopAsr();
       private Runnable pcmRun = new Runnable() {
        @Override
        public void run() {
            try {
                InputStream inputStream = getAssets().open("record.pcm");
                while (!isPcmStop) {
                    byte[] buffer = new byte[320];
                    int length = inputStream.read(buffer);
                    if (length == -1) {
                        turingOSClient.stopAsr();
                        break;
                    }
                    if (length == 320) {
                        //第三步 发送数据
                        turingOSClient.sendAudio(buffer, length);
                    } else {
                        turingOSClient.sendAudio(Arrays.copyOf(buffer, length), length);
                    }
                }
   
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
    };
```



#### AI对话输出

调用asr输入则会先返回asr结果，然后再返回图灵AI对话的语音MP3地址以及相应的文字。

具体看TuringOSClientListener接口的onResult方法说明。

json示例如下：

asr结果：

```json
{
    "asrResponse":{
        "binarysId":"2d456834-d82c-491d-a92d-312a1a86c300",
        "state":200,
        "value":"您好"
    },
    "code":200,
    "globalId":"98413305835000000",
    "message":"success"
}

```



AI对话结果

```json
{
    "code":200,
    "globalId":"98413305835000000",
    "message":"success",
    "nlpResponse":{
        "intent":{
            "code":100000,
            "operateState":1010
        },
        "results":[
            {
                "groupType":0,
                "resultType":"text",
                "values":{
                    "ttsUrl":[
                        "http://turing-iot.oss-cn-beijing.aliyuncs.com/tts/tts-0b6c2dd6687a4c8cb73a3ca49ed9b3ca-c63480507c8844cca59e36d7859d63ef.mp3"
                    ],
                    "emotionId":0,
                    "sentenceId":201,
                    "text":"恩恩，你也好，我好喜欢小主人哦！"
                }
            }
        ]
    }
}
```



### 绘本识别

#### 相关参数说明

BookRequestConfig

| 参数         | 类型    | 是否必须 | 取值范围 | 说明                                                         |
| ------------ | ------- | -------- | -------- | ------------------------------------------------------------ |
| cameraId     | int     | Y        | -        | 在图灵后台注册的摄像头编号，包含摄像头标定参数以及硬件平台信息 |
| bookId       | long    | N        | -        | 绘本模型ID，封面识别不需要此参数，内页识别则需要把封面识别结果中的 bookId 传入 |
| innerUrlFlag | Boolean | N        | -        | 方案商是否需要每页的音频URL链接，（非必填，默认为false）     |
| debug        | Boolean | N        | -        | 接口运行模式，false（default）：正式使用；true：调试模式     |
| useQa        | Boolean | N        | -        | 开启QA模式：false（default），使用需要管理平台开通对应题库   |
| type         | int     | Y        | -        | 请求类型。0-普通类型(默认)，1-⼿机app。设置1时，不需要传入摄像头其他参数。固定传入cameraId=1 |
| phoneModel   | String  | N        | -        | 手机型号（支架必填，非支架不需要）                           |

参数创建

```java
    BookRequestConfig.Builder builder = new BookRequestConfig.Builder();
    builder.cameraID(1)        
        .enableUseQa(true)        
        .enableDebug(true)        
        .type(1)        
        .phoneModle("");
```

#### 绘本识别调用

具体使用方式参考[TuringSDKSample](https://github.com/heezier/TuringSDKSample)。

##### 一、封面识别

```java
	/**
     * 
     * 
     * @param filePath 需要识别的图片文件路径
     * @param bookRequestConfig BookRequestConfig
     * @param listener TuringOSClientListener
     */
	public void actionBook(String filePath, BookRequestConfig bookRequestConfig, 				TuringOSClientListener listener)
        
        
    /**
     * 
     * @param data JEPG格式图片的byte[]数据
     * @param bookRequestConfig
     * @param listener
     */
    public void actionBook(byte[] data, BookRequestConfig bookRequestConfig, TuringOSClientListener listener)
        
    //调用
    turingOSClient.actionBook(filePath, bookRequestConfig, listener);  
        
```

##### 二、内页识别

```java
    /** 
     * 
     * @param fileName 
     * @param bookID  封面识别成功之后会在返回的结果中给出bookID
     * @param bookRequestConfig 
     * @param listener 
     */
	public void actionBook(String fileName, int bookID, BookRequestConfig 						bookRequestConfig, TuringOSClientListener listener)
        
    /**
     * 
     * @param data JEPG格式图片的byte[]数据
     * @param bookID
     * @param bookRequestConfig
     * @param listener
     */
    public void actionBook(byte[] data, int bookID, BookRequestConfig bookRequestConfig, TuringOSClientListener listener)
        
    //调用
    turingOSClient.actionBook(filePath, bookID, bookRequestConfig, listener);  


```

##### 三、内页QA输入

当绘本当前页支持QA问答时（operateState =3000），可以调用startQAWithRecorder打开录音器回答问题。

```java
    /**
     * 
     * @param listener
     */
    public void startQAWithRecorder(TuringOSClientListener listener)
        
        
    //调用
    turingOSClient.startQAWithRecorder(listener);  
     
```



#### 绘本输出

具体看TuringOSClientListener接口的onResult方法说明。

调用asr输入则会先返回asr结果，然后再返回图灵AI对话的语音MP3地址以及相应的文字。

```json
{
    "code":200,
    "globalId":"98411492145000000",
    "message":"success",
    "nlpResponse":{
        "intent":{
            "code":1000056,
            "operateState":2000,
            "parameters":{
                "msg":"cover recognition succeeded",
                "intentCode":200,
                "titleData":{
                    "cover":"http://universe-file-limit.tuling123.com/201912171915/9eeeaea4b6aa0cf34230ceb609e0c0f3/book_image/7b15cb80fe334d5e959c4ecc14b1a0be.jpg",
                    "bookVersion":3,
                    "zipUrl":"http://universe-file-limit.tuling123.com/201912171915/dbb3f754afe5a5038b9abea9c03f82b0/robot_story/12948/12948.zip",
                    "nameUrl":"http://universe-file-limit.tuling123.com/201912171915/14a7698950461761f5994c2214d6bee8/title/12948.mp3",
                    "name":"狼来了",
                    "bookName":"7b15cb80fe334d5e959c4ecc14b1a0be",
                    "bookId":12948
                },
                "commandUrl":"http://universe-file-limit.tuling123.com/201912171915/b5c3cdc000982ee3e55c579a8b71d13d/hint/success-1.mp3",
                "debugData":{
                    "imgOriginalUrl":"http://turing-platform-openapi.oss-cn-beijing.aliyuncs.com/openapi/image/0b6c2dd6687a4c8cb73a3ca49ed9b3ca_69412281_98411492145000000_834c386edc924aacb366def72581a2f9.png",
                    "imgProcessedUrl":"http://turing-universe.oss-cn-beijing.aliyuncs.com/recognition_img/img224/180.0_35.0_250.0_560.0_320_240_crop_/7f01feb9-70af-4feb-9fcf-b02737309168.jpg"
                }
            }
        },
        "results":[
            {
                "groupType":0,
                "resultType":"text",
                "values":{
                    "text":"我要和你一起读故事了"
                }
            }
        ]
    }
}
```

参数说明：

**intent-parameters**

| 参数       | 类型      | 是否必须 | 取值范围 | 说明                        |
| ---------- | --------- | -------- | -------- | --------------------------- |
| intentCode | int       | Y        | -        | 绘本状态                    |
| msg        | String    | N        | -        | 返回信息（英文）            |
| commandUrl | String    | N        | -        | 提示语音                    |
| titleData  | TitleData | N        | -        | 封面识别返回信息            |
| innerData  | InnerData | N        | -        | 内页识别返回信息            |
| debugData  | DebugData | N        | -        | 调试信息                    |
| funcData   | FuncData  | N        | -        | 功能参数，支持OCR和指读结果 |
| success    | boolean   | N        | -        | useQa时，用户回答是否正确   |


**TitleData**

| 参数        | 类型    | 是否必须 | 取值范围 | 说明                                 |
| ----------- | ------- | -------- | -------- | ------------------------------------ |
| name        | String  | Y        | -        | 绘本名称                             |
| zipUrl      | String  | Y        | -        | 绘本压缩包的url                      |
| bookVersion | Integer | Y        | -        | 绘本版本，音频版本，判断是否需要更新 |
| nameUrl     | String  | Y        | -        | 绘本名称的url                        |
| bookName    | String  | Y        | -        | 绘本模型标识                         |
| author      | String  | N        | -        | 绘本作者名称（版本2.0.0新增字段）    |
| publisher   | String  | N        | -        | 出版社名称（版本2.0.0新增字段）      |
| cover       | String  | N        | -        | 封面图片的url（版本2.0.0新增字段）   |
| bookId      | Long    | N        | -        | 绘本Id                               |

**InnerData**

| 参数    | 类型    | 是否必须 | 取值范围 | 说明                              |
| ------- | ------- | -------- | -------- | --------------------------------- |
| bookId  | Long    | N        | -        | 绘本Id                            |
| pageNum | Integer | Y        | -        | 绘本页数                          |
| url     | String  | Y        | -        | 绘本内页音频                      |
| pageUrl | String  | N        | -        | 绘本内页音频（版本2.0.0新增字段） |

**FuncData**

| 参数   | 类型 | 是否必须 | 取值范围 | 说明                  |
| ------ | ---- | -------- | -------- | --------------------- |
| ocr    | int  | Y        | -        | 0:普通;1:ocr          |
| finger | int  | Y        | -        | 0:无指读;1:可支持指读 |

**DebugData**

| 参数            | 类型   | 是否必须 | 取值范围 | 说明                 |
| --------------- | ------ | -------- | -------- | -------------------- |
| imgOriginalUrl  | String | Y        | -        | 原图片链接           |
| imgProcessedUrl | String | Y        | -        | 算法处理后的图片链接 |

**intent-operateState**

| operateState | 说明                                              |
| ------------ | ------------------------------------------------- |
| 2000         | 绘本识别正常                                      |
| 3000         | 正常，有附加内容输出，开启文本输入                |
| 3100         | 正常，有附加内容输出， 关闭文本输入               |
| 4000         | 正常，有附加内容输出，开启音频输入(注意音频类型!) |
| 4100         | 正常，有附加内容输出， 关闭音频输入               |
| 1001         | 绘本图片识别异常，有intentCode                    |
| 1005         | 图片内容大小非法(取值范围:[1,200]kb)              |
| 1006         | 图片内容处理异常                                  |
| 1007         | 请求Type非法                                      |
| 1008         | 请求中无图片信息                                  |

>注：`1，文本\音频输入使用通用方案输入即可(需要robotSkill中useQa设置为true)；2，文本\音频输入关闭后，继续使用文本\音频输入，接口将返回operateState=1008；3，开启文本\音频输入后，有效时间为5分钟，超过时间后继续输入该类型，接口将返回operateState=1008`



### 错误码

| code | 说明                                                         |
| :--- | :----------------------------------------------------------- |
| 200  | 正确结果返回                                                 |
| 210  | 参数初始化成功，请收到该标识后上传二进制数据(如果需要)       |
| 220  | 参数上传完成，正在请求nlp/tts                                |
| 300  | 无效数据:二进制参数已完成传输，不要发送该数据                |
| 4005 | apikey信息错误                                               |
| 4006 | deviceId信息错误                                             |
| 4007 | 解密失败，您的加密逻辑存在异常                               |
| 4008 | 数据内容格式错误                                             |
| 4009 | 机器人被禁用                                                 |
| 4010 | 试用期已过                                                   |
| 4011 | 系统不支持二进制参数                                         |
| 4012 | 今天我们已经聊了很多啦，明天再来找我聊天吧。                 |
| 4013 | 这一小时的对话次数已经超过我的极限啦，让我休息一下，待会再聊 |
| 4014 | 这一分钟里我们已经聊了很多啦，休息，休息一下吧               |
| 4015 | 二进制参数错误，请确定binarysId是否对应                      |
| 4016 | 单次交互时间过长，请查看文档！                               |
| 4017 | 二进制参数错误，有重复binarysId！                            |
| 4018 | 二进制参数输入状态异常                                       |
| 4019 | 二进制参数处理时间超时                                       |
| 4020 | ASR权限异常                                                  |
| 4021 | TTS权限异常                                                  |
| 4022 | 二进制传输内容过大！                                         |
| 4023 | 单次交互同类型的二进制参数只允许输入一个                     |
| 4025 | 上传数据失败，请稍后~                                        |
| 4026 | nlp/tts请求内容过长                                          |
| 4100 | 服务正在升级 请稍后再试                                      |
| 4101 | 请求没有正确初始化！                                         |
| 4102 | 长时间未请求业务,关闭连接                                    |
| 4200 | robot信息异常                                                |
| 4201 | nlp/tts处理异常                                              |
| 4202 | tts数据为空                                                  |
| 5001 | 音频信息参数错误                                             |
| 5002 | 未上传任何二进制数据                                         |
| 5003 | ASR音频格式不支持                                            |
| 5004 | ASR引擎链接异常                                              |
| 5005 | ASR引擎异常超时                                              |
| 5006 | 讯飞ASR引擎异常                                              |
| 5007 | ASR数据传输失败，没有初始化                                  |
| 5008 | ASR二进制数据转码失败                                        |
| 6000 | 丢弃任务：同一用户不允许同时处理一个以上的ASR请求            |
| 6001 | tts服务异常，请稍后再试                                      |