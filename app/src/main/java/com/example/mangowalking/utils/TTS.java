package com.example.mangowalking.utils;

public interface TTS {
    public void init();
    public void playText(String playText);
    public void stopSpeak();
    public void destroy();
    public boolean isPlaying();
    public void setCallback(ICallBack callback);
}
