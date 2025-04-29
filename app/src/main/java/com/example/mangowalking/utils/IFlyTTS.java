package com.example.mangowalking.utils;

import static com.iflytek.cloud.SpeechSynthesizer.createSynthesizer;

import android.content.Context;
import android.media.AudioManager;
import android.os.Bundle;

import com.iflytek.cloud.ErrorCode;
import com.iflytek.cloud.InitListener;
import com.iflytek.cloud.SpeechConstant;
import com.iflytek.cloud.SpeechError;
import com.iflytek.cloud.SpeechSynthesizer;
import com.iflytek.cloud.SpeechUtility;
import com.iflytek.cloud.SynthesizerListener;

public class IFlyTTS implements TTS, SynthesizerListener, AudioManager.OnAudioFocusChangeListener {
    private static IFlyTTS iflyTTS = null;
    Context mContext = null;
    private boolean isPlaying = false;
    private AudioManager mAm = null;
    ICallBack callBack = null;



    /**
     * 请务必替换为您自己申请的ID。
     */
    private final String appId = "1342cc62"; //这个是我自己的ID

    public static IFlyTTS getInstance(Context context) {
        if (iflyTTS == null) {
            iflyTTS = new IFlyTTS(context);
        }
        return iflyTTS;
    }

    private IFlyTTS(Context context) {
        mContext = context;
        SpeechUtility.createUtility(mContext, SpeechConstant.APPID + "="
                + appId);
        createSynthesizer();
        mAm = (AudioManager) mContext.getSystemService(Context.AUDIO_SERVICE);
    }

    private void createSynthesizer() {
        mTts = SpeechSynthesizer.createSynthesizer(mContext,
                new InitListener() {
                    @Override
                    public void onInit(int errorcode) {
                        if (ErrorCode.SUCCESS == errorcode) {
                            //初始化成功
                        }
                    }
                });
    }

    private SpeechSynthesizer mTts;
    @Override
    public void onAudioFocusChange(int focusChange) {

    }

    @Override
    public void init() {
        if (mTts!=null){
            //设置发音人
            mTts.setParameter(SpeechConstant.VOICE_NAME, "xiaoyan");
            //设置语速,值范围：[0, 100],默认值：50
            mTts.setParameter(SpeechConstant.SPEED, "55");
            //设置音量
            mTts.setParameter(SpeechConstant.VOLUME, "tts_volume");
            //设置语调
            mTts.setParameter(SpeechConstant.PITCH, "tts_pitch");
            //设置与其他音频软件冲突的时候是否暂停其他音频
            mTts.setParameter(SpeechConstant.KEY_REQUEST_FOCUS, "false");
            //女生仅vixy支持多音字播报
            mTts.setParameter(SpeechConstant.VOICE_NAME, "vixy");
        }
    }

    @Override
    public void playText(String playText) {
        //多音字处理举例
        if (playText != null && playText.contains("京藏")) {
            playText = playText.replace("京藏", "京藏[=zang4]");
        }
        if (playText != null && playText.length() > 0) {
            int result = mAm.requestAudioFocus(this,
                    AudioManager.STREAM_MUSIC,
                    AudioManager.AUDIOFOCUS_GAIN_TRANSIENT_MAY_DUCK);
            if (result == AudioManager.AUDIOFOCUS_REQUEST_GRANTED) {
                int code = mTts.startSpeaking(playText, this);
                isPlaying = true;
            }
        }
    }

    @Override
    public void stopSpeak() {
        if(mTts != null){
            mTts.stopSpeaking();
        }
        isPlaying = false;
    }

    @Override
    public void destroy() {
        stopSpeak();
        if(mTts != null){
            mTts.destroy();
        }
        iflyTTS = null;
    }

    @Override
    public boolean isPlaying() {
        return isPlaying;
    }



    @Override
    public void onSpeakBegin() {
        isPlaying = true;
    }

    @Override
    public void onBufferProgress(int i, int i1, int i2, String s) {

    }

    @Override
    public void onSpeakPaused() {
        isPlaying = false;
    }

    @Override
    public void onSpeakResumed() {

    }

    @Override
    public void onSpeakProgress(int i, int i1, int i2) {

    }

    @Override
    public void onCompleted(SpeechError arg0) {
        isPlaying = false;
        if (mAm != null) {
            mAm.abandonAudioFocus(this);
        }
        if (callBack != null) {
            if (arg0 == null) {
                callBack.onCompleted(0);
            }
        }
    }

    @Override
    public void onEvent(int i, int i1, int i2, Bundle bundle) {

    }

    @Override
    public void setCallback(ICallBack callback) {
        callBack = callback;
    }
}
