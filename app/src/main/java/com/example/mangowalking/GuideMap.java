package com.example.mangowalking;

import android.annotation.SuppressLint;
import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import com.amap.api.location.AMapLocationClient;
import com.amap.api.location.AMapLocationClientOption;
import com.amap.api.location.AMapLocationListener;
import com.amap.api.maps.AMapException;
import com.amap.api.maps.model.LatLng;
import com.amap.api.maps.model.Poi;
import com.amap.api.navi.AMapNavi;
import com.amap.api.navi.AMapNaviListener;
import com.amap.api.navi.AMapNaviView;
import com.amap.api.navi.AMapNaviViewListener;
import com.amap.api.navi.AmapNaviPage;
import com.amap.api.navi.AmapNaviParams;
import com.amap.api.navi.AmapNaviType;
import com.amap.api.navi.AmapPageType;
import com.amap.api.navi.ParallelRoadListener;
import com.amap.api.navi.enums.AMapNaviParallelRoadStatus;
import com.amap.api.navi.enums.NaviType;
import com.amap.api.navi.enums.TransportType;
import com.amap.api.navi.enums.TravelStrategy;
import com.amap.api.navi.model.*;
import com.example.mangowalking.utils.TTSController;


import java.util.ArrayList;
import java.util.List;

public class GuideMap extends AppCompatActivity implements AMapNaviListener, AMapNaviViewListener, ParallelRoadListener {
    private static final String TAG = "GuideMap";
    private AMapNaviView mNaviView; // 对应DEMO mAMapNaviView;
    private AMapNavi mAMapNavi;  // mAMapNavi;

    protected TTSController mTtsManager;

    //终点坐标
    private double endLat,endLon; //终点坐标

    private List<NaviLatLng> mWayPoints = new ArrayList<>();                // 途经点

    //与定位相关
    private AMapLocationClient mapLocationClient;
    private AMapLocationClientOption mLocationOption;

    private AmapNaviType currentNaviType = AmapNaviType.WALK;
    private NaviLatLng end;
    private TextView tvRouteInfo;
    private Button btnStartNavi;
    private Button btnSwitchMode;

    @SuppressLint("MissingInflatedId")
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        // 获取传入的终点经纬度
        Intent intent = getIntent();
        endLat = intent.getDoubleExtra("end_lat", 0);
        endLon = intent.getDoubleExtra("end_lon", 0);
        Log.d("GuideMap", "收到终点坐标: lat=" + endLat + ", lon=" + endLon);
        Log.d("GuideMap", "接收到坐标：" + endLat + "," + endLon);

        if (endLat < 1 || endLon < 1) {
            Toast.makeText(this, "终点坐标可能异常", Toast.LENGTH_LONG).show();
        }
        end = new NaviLatLng(endLat,endLon);
        startNaviToDestination(currentNaviType);
//
//        mNaviView = findViewById(R.id.naviView);
//        mNaviView.onCreate(savedInstanceState);
//
//        mTtsManager = TTSController.getInstance(getApplicationContext());
//        mTtsManager.init();
//        mTtsManager.setTTSType(TTSController.TTSType.SYSTEMTTS);
//        try {
//            AMapNavi.getInstance(this).calculateWalkRoute(null,end);
//        } catch (AMapException e) {
//            throw new RuntimeException(e);
//        }

        try {
            AMapNavi.getInstance(this).calculateWalkRoute(end);
            mAMapNavi = AMapNavi.getInstance(getApplicationContext());
            mAMapNavi.setUseInnerVoice(true,false);
            AMapNavi.setTtsPlaying(true);
        } catch (AMapException e) {
            throw new RuntimeException(e);
        }
        mAMapNavi.addAMapNaviListener(mTtsManager);
        mAMapNavi.playTTS("主人你好呀", false);

//        mAMapNavi.startNavi(NaviType.EMULATOR);
        //以上为新添加的 不知道怎么处理

        //        try {
//            mAMapNavi = AMapNavi.getInstance(getApplicationContext());
//        } catch (AMapException e) {
//            throw new RuntimeException(e);
//        }
//        mAMapNavi.addAMapNaviListener(this);
//        mAMapNavi.addAMapNaviListener(mTtsManager);
//
//        // 🚩 设置为骑/步行视图（重要）
//        mAMapNavi.setIsNaviTravelView(true);
//
//        // 🚶 步行路线规划（起点为空）——无起点规划
//        NaviLatLng end = new NaviLatLng(endLat, endLon);
//        mAMapNavi.calculateWalkRoute(end);  // 或 mAMapNavi.calculateRideRoute(end);
//
//        // 初始化界面控件
//        tvRouteInfo = findViewById(R.id.tvRouteInfo);
//        btnStartNavi = findViewById(R.id.btnStartNavi);
//        btnSwitchMode = findViewById(R.id.btnSwitchMode);
//        // 切换模式按钮
//        btnSwitchMode.setOnClickListener(new View.OnClickListener() {
//            @Override
//            public void onClick(View view) {
//                if (currentNaviType == AmapNaviType.WALK) {
//                    currentNaviType = AmapNaviType.DRIVER;
//                } else {
//                    currentNaviType = AmapNaviType.WALK;
//                }
//                updateRouteInfoText();
//            }
//        });
//
//        // 开始导航按钮
//        btnStartNavi.setOnClickListener(new View.OnClickListener() {
//            @Override
//            public void onClick(View view) {
//                startNaviToDestination(currentNaviType);
//            }
//        });

        mNaviView.setAMapNaviViewListener(new AMapNaviViewListener() {
            @Override
            public void onNaviSetting() {

            }

            @Override
            public void onNaviCancel() {

            }

            @Override
            public boolean onNaviBackClick() {
                return false;
            }

            @Override
            public void onNaviMapMode(int i) {

            }

            @Override
            public void onNaviTurnClick() {

            }

            @Override
            public void onNextRoadClick() {

            }

            @Override
            public void onScanViewButtonClick() {

            }

            @Override
            public void onLockMap(boolean b) {

            }

            @Override
            public void onNaviViewLoaded() {
                Log.d("wlx","导航页面加载成功");
                Log.d("wlx","请不要使用AMapNaviView.getMap().setOnMapLoadedListener();会overwrite导航SDK内部画线逻辑");
            }

            @Override
            public void onMapTypeChanged(int i) {

            }

            @Override
            public void onNaviViewShowMode(int i) {

            }
            // 处理导航视图事件...
        });

        // 3. 添加途经点（可选）
//        mWayPoints.add(new NaviLatLng(39.993706, 116.400865));
        //初始化定位
//        initLocation();
//        mNaviView.setNaviMode(mNaviView.NORTH_UP_MODE);
    }
    @Override
    public void onNaviSetting() {

    }

    @Override
    public void onNaviCancel() {

    }

    @Override
    public boolean onNaviBackClick() {
        return false;
    }

    @Override
    public void onNaviMapMode(int i) {

    }

    @Override
    public void onNaviTurnClick() {

    }

    @Override
    public void onNextRoadClick() {

    }

    @Override
    public void onScanViewButtonClick() {

    }

    @Override
    public void onLockMap(boolean b) {

    }

    @Override
    public void onNaviViewLoaded() {

    }

    @Override
    public void onMapTypeChanged(int i) {

    }

    @Override
    public void onNaviViewShowMode(int i) {

    }

    @Override
    public void notifyParallelRoad(AMapNaviParallelRoadStatus aMapNaviParallelRoadStatus) {
        if (aMapNaviParallelRoadStatus.getmElevatedRoadStatusFlag() == 1) {
            Toast.makeText(this, "当前在高架上", Toast.LENGTH_SHORT).show();
            Log.d("wlx", "当前在高架上");
        } else if (aMapNaviParallelRoadStatus.getmElevatedRoadStatusFlag() == 2) {
            Toast.makeText(this, "当前在高架下", Toast.LENGTH_SHORT).show();
            Log.d("wlx", "当前在高架下");
        }

        if (aMapNaviParallelRoadStatus.getmParallelRoadStatusFlag() == 1) {
            Toast.makeText(this, "当前在主路", Toast.LENGTH_SHORT).show();
            Log.d("wlx", "当前在主路");
        } else if (aMapNaviParallelRoadStatus.getmParallelRoadStatusFlag() == 2) {
            Toast.makeText(this, "当前在辅路", Toast.LENGTH_SHORT).show();
            Log.d("wlx", "当前在辅路");
        }
    }

    public enum NaviMode {WALK,BUS};
    private NaviMode currentNaviMode = NaviMode.WALK;



    // 初始化定位配置
    private void initLocation() {
        try {
            mapLocationClient = new AMapLocationClient(getApplicationContext());
            mLocationOption = new AMapLocationClientOption();
            mLocationOption.setLocationMode(AMapLocationClientOption.AMapLocationMode.Hight_Accuracy);
            mLocationOption.setInterval(2000);
            mapLocationClient.setLocationListener((AMapLocationListener) this);
            mapLocationClient.setLocationOption(mLocationOption);
        } catch (Exception e) {
            throw new RuntimeException(e);
        }

    }

    private void updateRouteInfoText() {
        String modeText = currentNaviType == AmapNaviType.WALK ? "步行模式" : "公交模式";
        tvRouteInfo.setText("当前导航模式：" + modeText);
    }

    private void startNaviToDestination(AmapNaviType naviType) {
        Poi end = new Poi("目的地", new LatLng(endLat, endLon), null);
        AmapNaviParams params = new AmapNaviParams(null, null, end, naviType, AmapPageType.ROUTE);
        params.setUseInnerVoice(true);
        AmapNaviPage.getInstance().showRouteActivity(getApplicationContext(), params, null);
    }






    @Override
    protected void onResume() {
        super.onResume();
        mNaviView.onResume();
    }

    @Override
    protected void onPause() {
        super.onPause();
        mNaviView.onPause();
    }

    @Override
    protected void onDestroy() {
//        super.onDestroy();
//        mNaviView.onDestroy();
//        //since 1.6.0 不再在naviview destroy的时候自动执行AMapNavi.stopNavi();请自行执行
//        if (mAMapNavi!=null){
//            mAMapNavi.stopNavi();
//            mAMapNavi.destroy();
//        }
        super.onDestroy();
//        if(mTtsManager != null)
//        {
//            mTtsManager.stopSpeaking();
//            mTtsManager.destroy();
//        }
        if(mAMapNavi != null)
        {
//            mAMapNavi.removeAMapNaviListener(mTtsManager);
            mAMapNavi.stopNavi();
        }
        mNaviView.onDestroy();
    }


    @Override
    public void onInitNaviFailure() {
        Toast.makeText(this, "init navi Failed", Toast.LENGTH_SHORT).show();
    }

    @Override
    public void onInitNaviSuccess() {
        //这里无需super  因为我是直接在父类上直接写
        mAMapNavi.setTravelInfo(new AMapTravelInfo(TransportType.Walk));
        mAMapNavi.calculateWalkRoute(null, end);
    }

    @Override
    public void onStartNavi(int i) {

    }

    @Override
    public void onTrafficStatusUpdate() {

    }

    @Override
    public void onLocationChange(AMapNaviLocation aMapNaviLocation) {
        aMapNaviLocation.getCoord();
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

    }

    @Override
    public void onReCalculateRouteForTrafficJam() {

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
        try {
            AMapNavi.getInstance(this).startNavi(NaviType.GPS);
            AMapNavi.getInstance(this.getApplicationContext()).setIsNaviTravelView(true);
        } catch (AMapException e) {
            throw new RuntimeException(e);
        }
    }

    @Override
    public void onCalculateRouteFailure(AMapCalcRouteResult aMapCalcRouteResult) {
        //路线计算失败
        Log.e("dm", "--------------------------------------------");
        Log.i("dm", "路线计算失败：错误码=" + aMapCalcRouteResult.getErrorCode() + ",Error Message= " + aMapCalcRouteResult.getErrorDescription());
        Log.i("dm", "错误码详细链接见：http://lbs.amap.com/api/android-navi-sdk/guide/tools/errorcode/");
        Log.e("dm", "--------------------------------------------");
        Toast.makeText(this, "errorInfo：" + aMapCalcRouteResult.getErrorDetail() + ", Message：" + aMapCalcRouteResult.getErrorDescription(), Toast.LENGTH_LONG).show();
    }

    @Override
    public void onNaviRouteNotify(AMapNaviRouteNotifyData aMapNaviRouteNotifyData) {

    }

    @Override
    public void onGpsSignalWeak(boolean b) {

    }
}