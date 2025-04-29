package com.example.mangowalking;

import android.annotation.SuppressLint;
import android.content.pm.PackageManager;
import android.os.Bundle;
import android.util.Log;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;
import androidx.core.content.ContextCompat;

import com.amap.api.location.AMapLocationClient;
import com.amap.api.location.AMapLocationClientOption;
import com.amap.api.location.AMapLocationListener;
import com.amap.api.maps.AMapException;
import com.amap.api.navi.AMapNavi;
import com.amap.api.navi.AMapNaviListener;
import com.amap.api.navi.AMapNaviView;
import com.amap.api.navi.AMapNaviViewListener;
import com.amap.api.navi.ParallelRoadListener;
import com.amap.api.navi.enums.AMapNaviParallelRoadStatus;
import com.amap.api.navi.enums.NaviType;
import com.amap.api.navi.enums.TransportType;
import com.amap.api.navi.model.*;
import com.example.mangowalking.utils.TTSController;
import com.google.android.filament.View;


import java.util.ArrayList;
import java.util.List;

public class GuideMap extends AppCompatActivity implements AMapNaviListener, AMapNaviViewListener, ParallelRoadListener {
    private static final String TAG = "GuideMap";
    private AMapNaviView mNaviView; // 对应DEMO mAMapNaviView;
    private AMapNavi mNavi;  // mAMapNavi;

    protected TTSController mTtsManager;
    private NaviLatLng mStartPoint; // 起点
    private NaviLatLng mEndPoint;   // 终点
    private List<NaviLatLng> mWayPoints = new ArrayList<>();                // 途经点

    protected final List<NaviLatLng> sList = new ArrayList<NaviLatLng>();
    protected final List<NaviLatLng> eList = new ArrayList<NaviLatLng>();
    protected List<NaviLatLng> mWayPointList = new ArrayList<NaviLatLng>();
    //与定位相关
    private AMapLocationClient mapLocationClient;
    private AMapLocationClientOption mLocationOption;
    private boolean isAutoStartLocation = false;

    @SuppressLint("MissingInflatedId")
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
//        mNavi.setIsNaviTravelView(true);
        setContentView(R.layout.activity_guide_map);
        // 1. 初始化导航组件
        mNaviView = findViewById(R.id.naviView);
        mNaviView.onCreate(savedInstanceState);
        try {
            mNavi = AMapNavi.getInstance(getApplicationContext());
        } catch (AMapException e) {
            throw new RuntimeException(e);
        }

        // 2. 设置导航监听器
        mNavi.addAMapNaviListener(this);
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
        mNaviView.setNaviMode(mNaviView.NORTH_UP_MODE);
        initLocation();
        // 3. 添加途经点（可选）
//        mWayPoints.add(new NaviLatLng(39.993706, 116.400865));
        //初始化定位
//        initLocation();
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
        super.onDestroy();
        mNaviView.onDestroy();
        //since 1.6.0 不再在naviview destroy的时候自动执行AMapNavi.stopNavi();请自行执行
        if (mNavi!=null){
            mNavi.stopNavi();
            mNavi.destroy();
        }

    }


    @Override
    public void onInitNaviFailure() {
        Toast.makeText(this, "init navi Failed", Toast.LENGTH_SHORT).show();
    }

    @Override
    public void onInitNaviSuccess() {
        //这里无需super  因为我是直接在父类上直接写
        mNavi.setTravelInfo(new AMapTravelInfo(TransportType.Walk));
        mNavi.calculateWalkRoute(new NaviLatLng(39.925846, 116.435765), new NaviLatLng(39.925846, 116.532765));
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
        mNavi.startNavi(NaviType.GPS);
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