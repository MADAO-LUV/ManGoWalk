package com.example.mangowalking.utils;


import android.content.Context;
import android.os.Message;
import android.os.Handler;

import com.amap.api.navi.AMapNaviListener;
import com.amap.api.navi.model.AMapCalcRouteResult;
import com.amap.api.navi.model.AMapLaneInfo;
import com.amap.api.navi.model.AMapModelCross;
import com.amap.api.navi.model.AMapNaviCameraInfo;
import com.amap.api.navi.model.AMapNaviCross;
import com.amap.api.navi.model.AMapNaviLocation;
import com.amap.api.navi.model.AMapNaviRouteNotifyData;
import com.amap.api.navi.model.AMapNaviTrafficFacilityInfo;
import com.amap.api.navi.model.AMapServiceAreaInfo;
import com.amap.api.navi.model.AimLessModeCongestionInfo;
import com.amap.api.navi.model.AimLessModeStat;
import com.amap.api.navi.model.NaviInfo;

import java.util.LinkedList;
import java.util.logging.LogRecord;

public class TTSController implements AMapNaviListener,ICallBack{

    @Override
    public void onCompleted(int code) {
        if (handler != null) {
            handler.obtainMessage(1).sendToTarget();
        }
    }
    public static enum TTSType {
        /**
         * 讯飞语音
         */
        IFLYTTS,
        /**
         * 系统语音
         */
        SYSTEMTTS;
    }

    public static TTSController ttsManager;
    private Context mContext;
    private TTS tts = null;
    private SystemTTS systemTTS;
    private IFlyTTS iflyTTS = null;
    private LinkedList<String> wordList = new LinkedList<String>();
    private final int TTS_PLAY = 1;
    private final int CHECK_TTS_PLAY = 2;

    private Handler handler = new Handler() {
        @Override
        public void handleMessage(Message msg) {
            super.handleMessage(msg);
            switch (msg.what) {
                case TTS_PLAY:
                    if (tts != null && wordList.size() > 0) {
                        tts.playText(wordList.removeFirst());
                    }
                    break;
                case CHECK_TTS_PLAY:
                    if (!tts.isPlaying()) {
                        handler.obtainMessage(1).sendToTarget();
                    }
                    break;
                default:
            }

        }
    };

    public void setTTSType(TTSType type) {
        if (type == TTSType.SYSTEMTTS) {
            tts = systemTTS;
        } else {
            tts = iflyTTS;
        }
        tts.setCallback(this);
    }

    private TTSController(Context context) {
        mContext = context.getApplicationContext();
        systemTTS = SystemTTS.getInstance(mContext);
        iflyTTS = IFlyTTS.getInstance(mContext);
        tts = iflyTTS;
    }

    public void init() {
        if (systemTTS != null) {
            systemTTS.init();
        }
        if (iflyTTS != null) {
            iflyTTS.init();
        }
        tts.setCallback(this);
    }

    public static TTSController getInstance(Context context) {
        if (ttsManager == null) {
            ttsManager = new TTSController(context);
        }
        return ttsManager;
    }

    public void stopSpeaking() {
        if (systemTTS != null) {
            systemTTS.stopSpeak();
        }
        if (iflyTTS != null) {
            iflyTTS.stopSpeak();
        }
        wordList.clear();
    }

    public void destroy() {
        if (systemTTS != null) {
            systemTTS.destroy();
        }
        if (iflyTTS != null) {
            iflyTTS.destroy();
        }
        ttsManager = null;
    }

    /****************************************************************************
     * 以下都是导航相关接口
     ****************************************************************************/

    @Override
    public void onInitNaviFailure() {
        
    }

    @Override
    public void onInitNaviSuccess() {

    }

    @Override
    public void onStartNavi(int i) {

    }

    @Override
    public void onTrafficStatusUpdate() {

    }

    @Override
    public void onLocationChange(AMapNaviLocation aMapNaviLocation) {

    }

    @Override
    public void onGetNavigationText(int i, String s) {

    }

    @Override
    public void onGetNavigationText(String s) {

    }

    @Override
    public void onEndEmulatorNavi() {

    }

    @Override
    public void onArriveDestination() {

    }

    @Override
    public void onCalculateRouteFailure(int i) {

    }

    @Override
    public void onReCalculateRouteForYaw() {
        if (wordList != null) {
            wordList.addLast("路线重新规划");
        }
    }

    @Override
    public void onReCalculateRouteForTrafficJam() {
        if (wordList != null) {
            wordList.addLast("前方路线拥堵，路线重新规划");
        }
    }

    @Override
    public void onArrivedWayPoint(int i) {

    }

    @Override
    public void onGpsOpenStatus(boolean b) {

    }

    @Override
    public void onNaviInfoUpdate(NaviInfo naviInfo) {

    }

    @Override
    public void updateCameraInfo(AMapNaviCameraInfo[] aMapNaviCameraInfos) {

    }

    @Override
    public void updateIntervalCameraInfo(AMapNaviCameraInfo aMapNaviCameraInfo, AMapNaviCameraInfo aMapNaviCameraInfo1, int i) {

    }

    @Override
    public void onServiceAreaUpdate(AMapServiceAreaInfo[] aMapServiceAreaInfos) {

    }

    @Override
    public void showCross(AMapNaviCross aMapNaviCross) {

    }

    @Override
    public void hideCross() {

    }

    @Override
    public void showModeCross(AMapModelCross aMapModelCross) {

    }

    @Override
    public void hideModeCross() {

    }

    @Override
    public void showLaneInfo(AMapLaneInfo[] aMapLaneInfos, byte[] bytes, byte[] bytes1) {

    }

    @Override
    public void showLaneInfo(AMapLaneInfo aMapLaneInfo) {

    }

    @Override
    public void hideLaneInfo() {

    }

    @Override
    public void onCalculateRouteSuccess(int[] ints) {

    }

    @Override
    public void notifyParallelRoad(int i) {

    }

    @Override
    public void OnUpdateTrafficFacility(AMapNaviTrafficFacilityInfo[] aMapNaviTrafficFacilityInfos) {

    }

    @Override
    public void OnUpdateTrafficFacility(AMapNaviTrafficFacilityInfo aMapNaviTrafficFacilityInfo) {

    }

    @Override
    public void updateAimlessModeStatistics(AimLessModeStat aimLessModeStat) {

    }

    @Override
    public void updateAimlessModeCongestionInfo(AimLessModeCongestionInfo aimLessModeCongestionInfo) {

    }

    @Override
    public void onPlayRing(int i) {

    }

    @Override
    public void onCalculateRouteSuccess(AMapCalcRouteResult aMapCalcRouteResult) {
        if(wordList != null)
        {
            wordList.addLast("主人你好棒呀");
        }
    }

    @Override
    public void onCalculateRouteFailure(AMapCalcRouteResult aMapCalcRouteResult) {
        if (wordList != null) {
            wordList.addLast("路线规划失败");
        }
    }

    @Override
    public void onNaviRouteNotify(AMapNaviRouteNotifyData aMapNaviRouteNotifyData) {

    }

    @Override
    public void onGpsSignalWeak(boolean b) {

    }


}