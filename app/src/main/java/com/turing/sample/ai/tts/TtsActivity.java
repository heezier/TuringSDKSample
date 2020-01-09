package com.turing.sample.ai.tts;

import android.content.Intent;
import android.os.Bundle;
import android.text.TextUtils;
import android.util.Log;
import android.util.SparseArray;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;

import androidx.annotation.Nullable;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.turing.os.client.TuringOSClient;
import com.turing.os.client.TuringOSClientListener;
import com.turing.os.init.UserData;
import com.turing.os.player.TtsPlayerPool;
import com.turing.os.request.bean.ResponBean;
import com.turing.sample.R;
import com.turing.sample.ai.asr.AsrResultAdapter;
import com.turing.sample.ai.chat.ResultAdapter;
import com.turing.sample.app.JsonViewActivity;
import com.turing.sample.app.base.BaseActivity;

import java.util.ArrayList;
import java.util.List;

import butterknife.BindView;
import butterknife.ButterKnife;
import butterknife.OnClick;

public class TtsActivity extends BaseActivity {
    private final static String TAG = "TtsActivity";
    @BindView(R.id.et_input)
    EditText etInput;
    @BindView(R.id.btn_start_nlp)
    Button btnStartNlp;
    @BindView(R.id.btn_start_play)
    Button btnStartPlay;
    @BindView(R.id.btn_stop)
    Button btnStop;
    @BindView(R.id.result_recycleview)
    RecyclerView resultRecycleview;


    private String str = "新华社北京11月22日电（记者白洁）国家主席习近平22日在人民大会堂会见国际货币基金组织总裁格奥尔基耶娃。\n" +
            "\n" +
            "　　习近平欢迎格奥尔基耶娃女士作为国际货币基金组织总裁首次访华。习近平指出，当前全球经济增长趋缓，下行风险加大，保护主义抬头，多边主义和自由贸易面临严峻挑战，国际社会对国际货币基金组织的作用有着更高期待。希望在你领导下，国际货币基金组织进一步完善国际货币及其治理体系，提高新兴市场国家和发展中国家代表性和发言权。\n" +
            "\n" +
            "　　习近平指出，中方积极倡导共商共建共享的全球治理观，坚决反对保护主义，维护以世界贸易组织为核心的多边贸易体制。希望国际货币基金组织继续在全球贸易议程中发挥积极作用，维护公平开放的全球金融市场，推动国际秩序朝着更加公正合理的方向发展。近年来，中国和国际货币基金组织在加强共建“一带一路”国家能力建设、提升软环境方面开展了很好合作。中方愿同国际货币基金组织不断深化合作。\n" +
            "\n" +
            "　　习近平强调，中国经济发展有着巨大韧性、潜力和回旋余地，经济长期向好的态势不会改变。中国将坚持新发展理念，推动经济高质量发展，持续推进更高水平的对外开放，为世界经济增长带来更多机遇。我对中国的发展充满信心。\n" +
            "\n" +
            "　　格奥尔基耶娃祝贺中华人民共和国成立70周年，表示明年中国将实现全面脱贫，这对于中国和整个世界都具有里程碑意义。中国通过改革开放实现了经济持续强劲增长，相信中国未来在包括金融、资本等各个领域都将继续保持开放。国际货币基金组织高度重视并将继续致力于深化同中国合作。当前，个别国家挑起贸易争端，世界经济处于艰难时期，国际货币基金组织坚定支持维护自由开放贸易，努力实现和平的贸易关系，愿对国际货币基金组织进行与时俱进的改革，提升新兴经济体的分量。" +
            "愿同中方积极推进构建人类命运共同体，加强共建“一带一路”合作，帮助发展中国家实现更好发展。";


    private TuringOSClient client;
    private TtsPlayerPool ttsPlayerPool;

    private List<String> resultList = new ArrayList<>();
    private ResultAdapter resultAdapter;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_tts);
        ButterKnife.bind(this);
        init();
        initResultView();
    }

    private void init() {
        userData = (UserData) getIntent().getSerializableExtra("userdata");
        client = TuringOSClient.getInstance(mContext, userData);
        btnStartNlp.setText(getString(R.string.tts_start));
    }

    private void initResultView() {
        resultRecycleview = (RecyclerView) findViewById(R.id.result_recycleview);
        LinearLayoutManager resultlayoutManager = new LinearLayoutManager(this);
        resultRecycleview.setLayoutManager(resultlayoutManager);
        resultAdapter = new ResultAdapter(resultList);
        resultRecycleview.setAdapter(resultAdapter);
        resultAdapter.setOnItemClickListener(new ResultAdapter.OnItemClickListener() {
            @Override
            public void onItemClick(View view, int position) {
                Intent intent = new Intent(mContext, JsonViewActivity.class);
                intent.putExtra("json", resultList.get(position));
                mContext.startActivity(intent);
            }
        });
    }
    private void startTts() {
        String text = etInput.getText().toString();
        if (TextUtils.isEmpty(text)) {
            showTost("输入为空！");
            return;
        }
        //针对较长的字符串，将会切割返回
        SparseArray<String> list = client.split(text);
        for (int i = 0; i < list.size(); i++) {
            Log.d(TAG, list.get(i).length() + "==========" + list.get(i));
        }
        ttsPlayerPool = TtsPlayerPool.create(list.size());

        client.actionTts(text, new TuringOSClientListener() {
            @Override
            public void onResult(int code, String result, ResponBean responBean, String extension) {
                if (responBean != null && responBean.getNlpResponse() != null
                        && responBean.getNlpResponse().getResults() != null) {
                    List<ResponBean.NlpResponseBean.ResultsBean> resultsBeanList = responBean.getNlpResponse().getResults();
                    ResponBean.NlpResponseBean.ResultsBean.ValuesBean valuesBean = resultsBeanList.get(0).getValues();
                    if (valuesBean != null) {
                        if (valuesBean.getTtsUrl() != null) {
                            String url = valuesBean.getTtsUrl().get(0);
                            if (!TextUtils.isEmpty(url)) {
                                ttsPlayerPool.addPlayUrl(url);
                            }
                        }
                    }
                }
                mUIHandler.postRunnable(new Runnable() {
                    @Override
                    public void run() {
                        resultList.add(result);
                        resultAdapter.updateList(resultList);
                        resultRecycleview.smoothScrollToPosition(resultList.size() - 1);
                    }
                });
            }

            @Override
            public void onError(int code, String msg) {
                Log.e(TAG, "onError code: " + code + "  msg: " + msg);
            }
        });


    }


    @OnClick({R.id.btn_start_nlp, R.id.btn_start_play, R.id.btn_stop})
    public void onViewClicked(View view) {
        switch (view.getId()) {
            case R.id.btn_start_nlp:
                startTts();
                break;
            case R.id.btn_start_play:
                startPlay();
                break;
            case R.id.btn_stop:
                stopPlay();
                break;
        }
    }

    private void startPlay() {
        if (ttsPlayerPool != null) {
            ttsPlayerPool.start();
            btnStop.setEnabled(true);
        }
    }

    private void stopPlay() {
        if (ttsPlayerPool != null) {
            ttsPlayerPool.stop();
            btnStartPlay.setEnabled(true);
        }
    }

    private String cutUrl(String url) {
        if (!TextUtils.isEmpty(url)) {
            return url.substring(0, 40) + "...\n";
        }
        return null;
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        stopPlay();
    }
}
