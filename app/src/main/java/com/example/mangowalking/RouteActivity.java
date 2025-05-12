package com.example.mangowalking;

import static android.content.pm.PackageManager.PERMISSION_GRANTED;
import static com.example.mangowalking.utils.MapUtil.convertToLatLng;
import static com.example.mangowalking.utils.MapUtil.convertToLatLonPoint;

import android.annotation.SuppressLint;
import android.app.Activity;

import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.bluetooth.BluetoothGatt;
import android.bluetooth.BluetoothGattCallback;
import android.bluetooth.BluetoothGattCharacteristic;
import android.bluetooth.BluetoothGattDescriptor;
import android.bluetooth.BluetoothGattService;
import android.bluetooth.BluetoothManager;
import android.bluetooth.BluetoothProfile;
import android.bluetooth.le.BluetoothLeScanner;
import android.bluetooth.le.ScanCallback;
import android.bluetooth.le.ScanFilter;
import android.bluetooth.le.ScanResult;
import android.bluetooth.le.ScanSettings;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.os.Bundle;
import android.os.ParcelUuid;
import android.util.Log;
import android.view.KeyEvent;
import android.view.View;
import android.view.inputmethod.InputMethodManager;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.RelativeLayout;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;

import androidx.activity.EdgeToEdge;
import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;

import com.amap.api.location.AMapLocation;
import com.amap.api.location.AMapLocationClient;
import com.amap.api.location.AMapLocationClientOption;
import com.amap.api.location.AMapLocationListener;
import com.amap.api.maps.AMap;
import com.amap.api.maps.LocationSource;
import com.amap.api.maps.UiSettings;
import com.amap.api.maps.model.BitmapDescriptorFactory;
import com.amap.api.maps.model.LatLng;
import com.amap.api.maps.model.MarkerOptions;
import com.amap.api.maps.model.MyLocationStyle;
import com.amap.api.navi.AMapNavi;
import com.amap.api.services.busline.BusLineItem;
import com.amap.api.services.core.AMapException;
import com.amap.api.services.core.LatLonPoint;
import com.amap.api.services.geocoder.GeocodeAddress;
import com.amap.api.services.geocoder.GeocodeQuery;
import com.amap.api.services.geocoder.GeocodeResult;
import com.amap.api.services.geocoder.GeocodeSearch;
import com.amap.api.services.geocoder.RegeocodeResult;
import com.amap.api.services.route.BusPath;
import com.amap.api.services.route.BusRouteResult;
import com.amap.api.services.route.BusStep;
import com.amap.api.services.route.DriveRouteResult;
import com.amap.api.services.route.RideRouteResult;
import com.amap.api.services.route.RouteBusLineItem;
import com.amap.api.services.route.RouteSearch;
import com.amap.api.services.route.WalkPath;
import com.amap.api.services.route.WalkRouteResult;
import com.amap.api.services.route.WalkStep;
import com.example.mangowalking.databinding.ActivityRouteBinding;
import com.example.mangowalking.overlay.BusRouteOverlay;
import com.example.mangowalking.overlay.WalkRouteOverlay;
import com.example.mangowalking.utils.MapUtil;

import java.io.UnsupportedEncodingException;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

public class RouteActivity extends AppCompatActivity implements
        AMapLocationListener, LocationSource, AMap.OnMapClickListener, RouteSearch.OnRouteSearchListener, GeocodeSearch.OnGeocodeSearchListener, View.OnKeyListener {


    private static final String TAG = "RouteActivity";
    private ActivityRouteBinding binding;
    //地图控制器
    private AMap aMap = null;
    //声明AMapLocationClient类对象
    public AMapLocationClient mLocationClient = null;
    //声明AMapLocationClientOption对象
    public AMapLocationClientOption mLocationOption = null;
    //位置更改监听
    private LocationSource.OnLocationChangedListener mListener;
    //定义一个UiSettings对象
    private UiSettings mUiSettings;
    //定位样式
    private MyLocationStyle myLocationStyle = new MyLocationStyle();

    //起点
    private LatLonPoint mStartPoint;
    //终点
    private LatLonPoint mEndPoint;
    //路线搜索对象
    private RouteSearch routeSearch;

    //出行方式数组
    private static final String[] travelModeArray = {"步行出行", "公交出行"};

    //出行方式值
    private static int TRAVEL_MODE = 0;

    //数组适配器
    private ArrayAdapter<String> arrayAdapter;
    //地理编码搜索
    private GeocodeSearch geocodeSearch;
    //解析成功标识码
    private static final int PARSE_SUCCESS_CODE = 1000;
    //定位地址
    private String locationAddress;
    //城市
    private String city;
    //起点地址转坐标标识   1
    private int tag = -1;

    //路线规划详情
    private RelativeLayout bottomLayout;
    //花费时间
    private TextView tvTime;

    //添加关于蓝牙的变量
    private BluetoothLeScanner scanner;

    private BluetoothDevice device;

    private BluetoothGatt bluetoothGatt;

    private boolean isBluetoothConnected = false;

    private String SERVICE_UUID = "0000FFE0-0000-1000-8000-00805F9B34FB";
    private String READ_UUID = "0000FFE0-0000-1000-8000-00805F9B34FB";

    private String READ_DEDSCRIPTION_UUID = "00002902-0000-1000-8000-00805f9b34fb";

    private BluetoothGattCharacteristic writeCharacteristic;

    private ArrayList<LatLng> poiListForGuideMap = new ArrayList<>();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        binding = ActivityRouteBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main), (v, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);
            return insets;
        });

        //添加蓝牙开启   蓝牙部分是成功开启了
        if (!getPackageManager().hasSystemFeature(PackageManager.FEATURE_BLUETOOTH_LE)) {
            Toast.makeText(RouteActivity.this, "没有蓝牙", Toast.LENGTH_SHORT).show();
        } else {
            Toast.makeText(RouteActivity.this, "该设备支持蓝牙", Toast.LENGTH_SHORT).show();
        }

        final BluetoothManager bluetoothManager = (BluetoothManager) getSystemService(Context.BLUETOOTH_SERVICE);
        BluetoothAdapter bluetoothAdapter = bluetoothManager.getAdapter();

        ActivityResultLauncher<Intent> launcher = registerForActivityResult(new ActivityResultContracts.StartActivityForResult(), result -> {
            if (result.getResultCode() == RESULT_OK) {
                //处理返回结果
                Toast.makeText(RouteActivity.this, "已成功开启蓝牙", Toast.LENGTH_SHORT).show();
            }
        });
        @SuppressLint("MissingPermission")
        Intent enableBtIntent = new Intent(BluetoothAdapter.ACTION_REQUEST_ENABLE); //创建一个蓝牙启动的意图
        launcher.launch(enableBtIntent);//使用launcer启动这个意图就可以了。


        //此处可行
        //初始化定位
        initLocation();
        //初始化地图
        initMap(savedInstanceState);
        //启动定位
        mLocationClient.startLocation();
        initRoute();
        //初始化出行方式
        initTravelMode();

        // 源码  --->
        //binding.buttonmy.setOnClickListener(v->startActivity(new Intent(this,GuideMap.class)));
        binding.buttonmy.setOnClickListener(v -> {
            // 检查终点坐标是否存在
            if (mEndPoint != null) {
                Intent intent = new Intent(this, GuideMap.class);
                // 将终点坐标放入Intent
                intent.putExtra("end_lat", mEndPoint.getLatitude());
                intent.putExtra("end_lon", mEndPoint.getLongitude());
                Log.d("RouteActivity", "准备跳转 GuideMap，终点：" + mEndPoint.getLatitude() + "," + mEndPoint.getLongitude());
                startActivity(intent);
            } else {
                Toast.makeText(this, "请先选择目的地", Toast.LENGTH_SHORT).show();
            }
        });

        // 新加入的
        scanner = bluetoothAdapter.getBluetoothLeScanner();
        //不进行权限验证
        ScanCallback callback = new ScanCallback() {
            @Override
            public void onScanResult(int callbackType, ScanResult result) {
                device = result.getDevice();//得到设备
                //     Log.e(TAG, "发现设备" + device.getName());

            }

            @Override
            public void onScanFailed(int errorCode) {
                super.onScanFailed(errorCode);
                Log.e(TAG, "搜索错误" + errorCode);
            }
        };
        ScanFilter sn = new ScanFilter.Builder().setDeviceName("蓝牙设备的名称").setServiceUuid(ParcelUuid.fromString("0000FFE0-0000-1000-8000-00805F9B34FB")).build();
        List<ScanFilter> scanFilters = new ArrayList<>();
        scanFilters.add(sn);
        if (ActivityCompat.checkSelfPermission(this, android.Manifest.permission.BLUETOOTH_SCAN) != PERMISSION_GRANTED) {
            // TODO: Consider calling
            //    ActivityCompat#requestPermissions
            return;
        }
        scanner.startScan(scanFilters, new ScanSettings.Builder().build(), callback);
        bluetoothGatt = device.connectGatt(this, false, gattCallback);

//        Button btn_1 = findViewById(R.id.btn_ble);
//
//        // 重写一个内部类
//        btn_1.setOnClickListener(new View.OnClickListener() {
//            @Override
//            public void onClick(View v) {
//                Log.d("RouteActivity", "点击了跳转按钮");
////                ArrayList<LatLng> poiList = new ArrayList<>();
//                //这只是一个意图
//                Intent intent = new Intent(RouteActivity.this,GuideMap.class);
////                intent.putParcelableArrayListExtra("poi_list",poiList);
//                startActivity(intent);
//            }
//        });
// RouteActivity.java 中修改点击事件


    }

    BluetoothGattCallback gattCallback = new BluetoothGattCallback() {
        //GATT的链接状态回调
        @Override
        public void onConnectionStateChange(BluetoothGatt gatt, int status, int newState) {
            super.onConnectionStateChange(gatt, status, newState);
            if (newState == BluetoothProfile.STATE_CONNECTED) {
                if (ActivityCompat.checkSelfPermission(RouteActivity.this, android.Manifest.permission.BLUETOOTH_CONNECT) != PERMISSION_GRANTED) {
                    return;
                }
                gatt.discoverServices();
                Log.v(TAG, "连接成功");
            } else if (newState == BluetoothProfile.STATE_DISCONNECTED) {
                Log.e(TAG, "连接断开");
            } else if (newState == BluetoothProfile.STATE_CONNECTING) {
                //TODO 在实际过程中，该方法并没有调用
                Log.e(TAG, "连接中....");
            }
        }

        //获取GATT服务发现后的回调
        @Override
        public void onServicesDiscovered(BluetoothGatt gatt, int status) {
            if (status == BluetoothGatt.GATT_SUCCESS) {
                Log.e(TAG, "GATT_SUCCESS"); //服务发现
                for (BluetoothGattService bluetoothGattService : gatt.getServices()) {
                    Log.e(TAG, "Service_UUID" + bluetoothGattService.getUuid()); // 我们可以遍历到该蓝牙设备的全部Service对象。然后通过比较Service的UUID，我们可以区分该服务是属于什么业务的
                    if (SERVICE_UUID.equals(bluetoothGattService.getUuid().toString())) {

                        for (BluetoothGattCharacteristic characteristic : bluetoothGattService.getCharacteristics()) {
                            prepareBroadcastDataNotify(gatt, characteristic); //给满足条件的属性配置上消息通知
                        }
                        return;//结束循环操作
                    }
                }
            } else {
                Log.e(TAG, "onServicesDiscovered received: " + status);
            }
        }

        //蓝牙设备发送消息后的自动监听
        @Override
        public void onCharacteristicChanged(BluetoothGatt gatt, BluetoothGattCharacteristic characteristic) {
            // readUUID 是我要链接的蓝牙设备的消息读UUID值，跟通知的特性的UUID比较。这样可以避免其他消息的污染。
            if (READ_UUID.equals(characteristic.getUuid().toString())) {
                try {
                    String chara = new String(characteristic.getValue(), "UTF-8");
                    Log.e(TAG, "消息内容:" + chara);
                } catch (UnsupportedEncodingException e) {
                    e.printStackTrace();
                }
            }
        }
    };

    @SuppressLint("MissingPermission")
    private void prepareBroadcastDataNotify(BluetoothGatt mBluetoothGatt, BluetoothGattCharacteristic characteristic) {
        Log.e(TAG, "CharacteristicUUID:" + characteristic.getUuid().toString());
        int charaProp = characteristic.getProperties();
        //判断属性是否支持消息通知
        if ((charaProp | BluetoothGattCharacteristic.PROPERTY_NOTIFY) > 0) {
            BluetoothGattDescriptor descriptor =
                    characteristic.getDescriptor(UUID.fromString(READ_DEDSCRIPTION_UUID));
            if (descriptor != null) {
                //注册消息通知
                mBluetoothGatt.setCharacteristicNotification(characteristic, true);
                descriptor.setValue(BluetoothGattDescriptor.ENABLE_NOTIFICATION_VALUE);
                mBluetoothGatt.writeDescriptor(descriptor);
            }
        }
    }

    public void onServicesDiscovered(BluetoothGatt gatt, int status) {
        if (status == BluetoothGatt.GATT_SUCCESS) {
            BluetoothGattService service = gatt.getService(UUID.fromString(SERVICE_UUID));
            if (service != null) {
                // 假设写特征的 UUID 就是 READ_UUID（或者你需要再定义一个 WRITE_UUID）
                writeCharacteristic = service.getCharacteristic(UUID.fromString(READ_UUID));
                // 如果该特征需要先 enableNotification，也可在这里设置：
                if (ActivityCompat.checkSelfPermission(this, android.Manifest.permission.BLUETOOTH_CONNECT) != PERMISSION_GRANTED) {
                    return;
                }
                gatt.setCharacteristicNotification(writeCharacteristic, true);
            }
        }
    }

    /**
     * 通过 BLE 将 poiList 中的每个坐标点逐条发送到从机。
     * 由于单包 MTU 默认为 20 字节，必要时需要分包。
     */
    private void sendPoiList(List<LatLng> poiList) {
        if (writeCharacteristic == null || bluetoothGatt == null) {
            showMsg("蓝牙特征或连接未准备好");
            return;
        }

        for (LatLng p : poiList) {
            // 1) 将坐标格式化为字符串，比如："31.2304,121.4737\n"
            String str = p.latitude + "," + p.longitude + "\n";
            byte[] data = str.getBytes(StandardCharsets.UTF_8);

            // 2) 如果 data.length > MTU，需要分包；这里简单示例不分包
            writeCharacteristic.setValue(data);

            // 3) 发写命令，异步执行
            if (ActivityCompat.checkSelfPermission(this, android.Manifest.permission.BLUETOOTH_CONNECT) != PERMISSION_GRANTED) {
                return;
            }
            boolean success = bluetoothGatt.writeCharacteristic(writeCharacteristic);
            if (!success) {
                Log.e(TAG, "写入特征失败：" + str);
            }

            // 4) 为避免短时间内连续写多包导致丢包，可在这里短暂停一下（示例用 Handler）
            try {
                Thread.sleep(50);
            } catch (InterruptedException ignored) {}
        }
    }

    /**
     * 初始化出行方式
     */
    private void initTravelMode() {
        Spinner spinner = findViewById(R.id.spinner);

        //将可选内容与ArrayAdapter连接起来
        arrayAdapter = new ArrayAdapter<>(this, android.R.layout.simple_spinner_item, travelModeArray);
        //设置下拉列表的风格
        arrayAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        //将adapter 添加到spinner中
        spinner.setAdapter(arrayAdapter);
        //添加事件Spinner事件监听
        spinner.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                TRAVEL_MODE = position;
            }

            @Override
            public void onNothingSelected(AdapterView<?> parent) {
            }
        });
        //键盘按键监听
        //起点 键盘按键监听
        binding.etStartAddress.setOnKeyListener(this);
        //终点 键盘按键监听
        binding.etEndAddress.setOnKeyListener(this);


    }

    /**
     * 初始化路线
     */
    private void initRoute() {
        try {
            routeSearch = new RouteSearch(this);
        } catch (AMapException e) {
            e.printStackTrace();
        }
        routeSearch.setRouteSearchListener(this);
    }

    /**
     * 初始化定位
     */
    private void initLocation() {
        //初始化定位
        try {
            mLocationClient = new AMapLocationClient(getApplicationContext());
        } catch (Exception e) {
            e.printStackTrace();
        }
        if (mLocationClient != null) {
            mLocationClient.setLocationListener(this);
            mLocationOption = new AMapLocationClientOption();
            mLocationOption.setLocationMode(AMapLocationClientOption.AMapLocationMode.Hight_Accuracy);
            // 设置定位间隔   2000ms更新一次
            mLocationOption.setInterval(2000);
            //
            mLocationOption.setOnceLocationLatest(false);
            mLocationOption.setNeedAddress(true);
            mLocationOption.setHttpTimeOut(20000);
            mLocationOption.setLocationCacheEnable(false);
            mLocationClient.setLocationOption(mLocationOption);
        }
    }



    /**
     * 初始化地图
     *
     * @param savedInstanceState
     */
    private void initMap(Bundle savedInstanceState) {
        binding.mapView.onCreate(savedInstanceState);
        //初始化地图控制器对象
        aMap = binding.mapView.getMap();
        //设置最小缩放等级为12 ，缩放级别范围为[3, 20]
        aMap.setMinZoomLevel(12);
        //开启室内地图
        aMap.showIndoorMap(true);
        //实例化UiSettings类对象
        mUiSettings = aMap.getUiSettings();
        //隐藏缩放按钮 默认显示
        mUiSettings.setZoomControlsEnabled(false);
        //显示比例尺 默认不显示
        mUiSettings.setScaleControlsEnabled(true);
        // 自定义定位蓝点图标
        myLocationStyle.myLocationIcon(BitmapDescriptorFactory.fromResource(R.drawable.gps_point));
        //设置定位蓝点的Style
        aMap.setMyLocationStyle(myLocationStyle);
        // 设置定位监听
        aMap.setLocationSource(this);
        // 设置为true表示显示定位层并可触发定位，false表示隐藏定位层并不可触发定位，默认是false
        aMap.setMyLocationEnabled(true);
        //地图点击监听
        aMap.setOnMapClickListener(this);
        //构造 GeocodeSearch 对象
        try {
            geocodeSearch = new GeocodeSearch(this);
        } catch (AMapException e) {
            throw new RuntimeException(e);
        }
        //设置监听
        geocodeSearch.setOnGeocodeSearchListener(this);

    }

    @Override
    public void onLocationChanged(AMapLocation aMapLocation) {
        if (aMapLocation != null) {
            if (aMapLocation.getErrorCode() == 0) {
                //地址
                String address = aMapLocation.getAddress();
                //获取纬度
                double latitude = aMapLocation.getLatitude();
                //获取经度
                double longitude = aMapLocation.getLongitude();
                //设置当前所在地
                //地址
                locationAddress = aMapLocation.getAddress();
                //设置当前所在地
                binding.etStartAddress.setText(locationAddress);
                //binding.etStartAddress.setEnabled(false);//禁用输入
                //城市赋值
                city = aMapLocation.getCity();
                Log.d(TAG, address);
                //设置起点
                mStartPoint = convertToLatLonPoint(new LatLng(latitude, longitude));
                //停止定位后，本地定位服务并不会被销毁
                mLocationClient.stopLocation();

                //显示地图定位结果
                if (mListener != null) {
                    // 显示系统图标
                    mListener.onLocationChanged(aMapLocation);
                }

            } else {
                //定位失败时，可通过ErrCode（错误码）信息来确定失败的原因，errInfo是错误信息，详见错误码表。
                Log.e("AmapError", "location Error, ErrCode:"
                        + aMapLocation.getErrorCode() + ", errInfo:"
                        + aMapLocation.getErrorInfo());
            }
        }
    }

    /**
     * 开始路线搜索
     */
    private void startRouteSearch() {
        //在地图上添加起点Marker
        aMap.addMarker(new MarkerOptions()
                .position(convertToLatLng(mStartPoint))
                .icon(BitmapDescriptorFactory.fromResource(R.drawable.start)));
        //在地图上添加终点Marker
        aMap.addMarker(new MarkerOptions()
                .position(convertToLatLng(mEndPoint))
                .icon(BitmapDescriptorFactory.fromResource(R.drawable.end)));

        //搜索路线 构建路径的起终点
        final RouteSearch.FromAndTo fromAndTo = new RouteSearch.FromAndTo(
                mStartPoint, mEndPoint);
        //出行方式判断
        switch (TRAVEL_MODE) {
            case 0://步行
                //构建步行路线搜索对象
                RouteSearch.WalkRouteQuery query = new RouteSearch.WalkRouteQuery(fromAndTo, RouteSearch.WalkDefault);
                // 异步路径规划步行模式查询
                routeSearch.calculateWalkRouteAsyn(query);
                break;
            case 1://骑行
                //构建驾车路线搜索对象 第三个参数表示公交查询城市区号，第四个参数表示是否计算夜班车，0表示不计算,1表示计算
                RouteSearch.BusRouteQuery busQuery = new RouteSearch.BusRouteQuery(fromAndTo, RouteSearch.BusLeaseWalk, city, 1);
                //公交规划路径计算
                routeSearch.calculateBusRouteAsyn(busQuery);
                break;
            default:
                break;
        }

    }


    @Override
    public void activate(LocationSource.OnLocationChangedListener onLocationChangedListener) {
        mListener = onLocationChangedListener;
        if (mLocationClient == null) {
            mLocationClient.startLocation();//启动定位
        }
    }

    @Override
    public void deactivate() {
        mListener = null;
        if (mLocationClient != null) {
            mLocationClient.stopLocation();
            mLocationClient.onDestroy();
        }
        mLocationClient = null;
    }


    @Override
    protected void onResume() {
        super.onResume();
        binding.mapView.onResume();
    }

    @Override
    protected void onPause() {
        super.onPause();
        binding.mapView.onPause();
    }

    @Override
    protected void onSaveInstanceState(Bundle outState) {
        super.onSaveInstanceState(outState);
        binding.mapView.onSaveInstanceState(outState);
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        //销毁定位客户端，同时销毁本地定位服务。
        if (mLocationClient != null) {
            mLocationClient.onDestroy();
        }
        binding.mapView.onDestroy();

        if (bluetoothGatt != null) {
            if (ActivityCompat.checkSelfPermission(this, android.Manifest.permission.BLUETOOTH_CONNECT) != PERMISSION_GRANTED) {
                return;
            }
            bluetoothGatt.close();
            bluetoothGatt.disconnect();
            bluetoothGatt = null;
        }

    }

    private void showMsg(CharSequence llw) {
        Toast.makeText(this, llw, Toast.LENGTH_SHORT).show();
    }

    /**
     * 点击地图
     */
    @Override
    public void onMapClick(LatLng latLng) {
        //终点
        mEndPoint = convertToLatLonPoint(latLng);
        //开始路线搜索
        startRouteSearch();
    }

    /**
     * 公交规划路径结果
     *
     * @param busRouteResult 结果
     * @param code           结果码
     */
    @Override
    public void onBusRouteSearched(BusRouteResult busRouteResult, int code) {
        aMap.clear();// 清理地图上的所有覆盖物
        if (code != AMapException.CODE_AMAP_SUCCESS) {
            showMsg("错误码；" + code);
            return;
        }
        if (busRouteResult == null || busRouteResult.getPaths() == null) {
            showMsg("对不起，没有搜索到相关数据！");
            return;
        }
        if (busRouteResult.getPaths().isEmpty()) {
            showMsg("对不起，没有搜索到相关数据！");
            return;
        }
        final BusPath busPath = busRouteResult.getPaths().get(0);
        if (busPath == null) {
            return;
        }
        // 绘制路线
        BusRouteOverlay busRouteOverlay = new BusRouteOverlay(
                this, aMap, busPath,
                busRouteResult.getStartPos(),
                busRouteResult.getTargetPos());
        busRouteOverlay.removeFromMap();
        busRouteOverlay.addToMap();
        busRouteOverlay.zoomToSpan();

        int dis = (int) busPath.getDistance();
        int dur = (int) busPath.getDuration();
        String des = MapUtil.getFriendlyTime(dur) + "(" + MapUtil.getFriendlyLength(dis) + ")";
        Log.d(TAG, des);
        //显示公交花费时间
        //显示公交花费时间
        binding.tvTime.setText(des);
        binding.layBottom.setVisibility(View.VISIBLE);
        //跳转到路线详情页面
        binding.tvDetail.setOnClickListener(v -> {
            Intent intent = new Intent(RouteActivity.this,
                    RouteDetailActivity.class);
            intent.putExtra("type",1);
            intent.putExtra("path", busPath);
            startActivity(intent);
        });
        List<LatLng> allPoiPoints = new ArrayList<>();

        List<BusStep> steps = busPath.getSteps();
        for (BusStep step : steps) {

            // 🟢 步行部分
            if (step.getWalk() != null && step.getWalk().getSteps() != null) {
                List<WalkStep> walkSteps = step.getWalk().getSteps();
                for (WalkStep walkStep : walkSteps) {
                    List<LatLonPoint> polyline = walkStep.getPolyline();
                    for (LatLonPoint point : polyline) {
                        allPoiPoints.add(new LatLng(point.getLatitude(), point.getLongitude()));
                        Log.d("Walk:",point.getLatitude() + "," + point.getLongitude());
                    }
                }
            }

            // 🟠 公交部分
            if (step.getBusLines() != null && !step.getBusLines().isEmpty()) {
                BusLineItem busLine = step.getBusLines().get(0); // 取当前段第一条公交车
                List<LatLonPoint> busPoints = ((RouteBusLineItem) busLine).getPolyline(); // 🚍 公交线路坐标
                for (LatLonPoint point : busPoints) {
                    allPoiPoints.add(new LatLng(point.getLatitude(), point.getLongitude()));
                    Log.d("Bus:",point.getLatitude() + "," + point.getLongitude());
                }
            }
        }
        poiListForGuideMap.clear();
        poiListForGuideMap.addAll(allPoiPoints);

//        // 🧪 测试打印
//        for (LatLng p : poiList) {
//            Log.d("POI_POINT", p.latitude + "," + p.longitude);
//        }


    }


    @Override
    public void onDriveRouteSearched(DriveRouteResult driveRouteResult, int i) {

    }

    /**
     * 步行规划路径结果
     *
     * @param walkRouteResult 结果
     * @param code            结果码
     */
    @Override
    public void onWalkRouteSearched(WalkRouteResult walkRouteResult, int code) {
        aMap.clear();// 清理地图上的所有覆盖物
        if (code != AMapException.CODE_AMAP_SUCCESS) {
            showMsg("错误码；" + code);
            return;
        }
        if (walkRouteResult == null || walkRouteResult.getPaths() == null) {
            showMsg("对不起，没有搜索到相关数据！");
            return;
        }
        if (walkRouteResult.getPaths().isEmpty()) {
            showMsg("对不起，没有搜索到相关数据！");
            return;
        }
        final WalkPath walkPath = walkRouteResult.getPaths().get(0);
        if (walkPath == null) {
            return;
        }
        //绘制路线
        WalkRouteOverlay walkRouteOverlay = new WalkRouteOverlay(
                this, aMap, walkPath,
                walkRouteResult.getStartPos(),
                walkRouteResult.getTargetPos());
        walkRouteOverlay.removeFromMap();
        walkRouteOverlay.addToMap();
        walkRouteOverlay.zoomToSpan();

        int dis = (int) walkPath.getDistance();
        int dur = (int) walkPath.getDuration();
        String des = MapUtil.getFriendlyTime(dur) + "(" + MapUtil.getFriendlyLength(dis) + ")";
        Log.d(TAG, des);
        //显示步行花费时间
        binding.tvTime.setText(des);
        binding.layBottom.setVisibility(View.VISIBLE);
        //跳转到路线详情页面
        binding.tvDetail.setOnClickListener(v -> {
            Intent intent = new Intent(RouteActivity.this,
                    RouteDetailActivity.class);
            intent.putExtra("type",0);
            intent.putExtra("path", walkPath);
            startActivity(intent);
        });
        List<LatLng> poiList = new ArrayList<>();

        List<WalkStep> steps = walkPath.getSteps();
        for (WalkStep step : steps) {
            List<LatLonPoint> polyline = step.getPolyline();
            for (LatLonPoint point : polyline) {
                LatLng latLng = new LatLng(point.getLatitude(), point.getLongitude());
                poiList.add(latLng);
            }
        }

// 🧪 测试打印
        for (LatLng p : poiList) {
            Log.d("POI_POINT", p.latitude + "," + p.longitude);
        }
        sendPoiList(poiList);
        poiListForGuideMap.clear();
        poiListForGuideMap.addAll(poiList);
    }


    @Override
    public void onRideRouteSearched(RideRouteResult rideRouteResult, int i) {

    }

    @Override
    public void onRegeocodeSearched(RegeocodeResult regeocodeResult, int i) {

    }

    /**
     * 地址转坐标
     *
     * @param geocodeResult
     * @param rCode
     */
    @Override
    public void onGeocodeSearched(GeocodeResult geocodeResult, int rCode) {
        if (rCode != PARSE_SUCCESS_CODE) {
            showMsg("获取坐标失败，错误码：" + rCode);
            return;
        }
        List<GeocodeAddress> geocodeAddressList = geocodeResult.getGeocodeAddressList();
        if (geocodeAddressList != null && !geocodeAddressList.isEmpty()) {
            //判断是不是起点的搜索
            if (tag == 1) {
                //起点
                mStartPoint = geocodeAddressList.get(0).getLatLonPoint();
            } else {
                //终点
                mEndPoint = geocodeAddressList.get(0).getLatLonPoint();
            }
            if (mStartPoint != null && mEndPoint != null) {
                //开始路线搜索
                startRouteSearch();
            }
        }
    }


    @Override
    public boolean onKey(View v, int keyCode, KeyEvent event) {
        if (keyCode == KeyEvent.KEYCODE_ENTER && event.getAction() == KeyEvent.ACTION_UP) {
            //获取输入框的值 出发地（起点）
            String startAddress = binding.etStartAddress.getText().toString().trim();
            //获取输入框的值 目的地（终点）
            String endAddress = binding.etEndAddress.getText().toString().trim();

            //判断出发地是否有值  不管这个值是定位还是手动输入
            if (startAddress.isEmpty()) {
                showMsg("请输入当前的出发地");
                return false;
            }
            //判断目的地是否有值
            if (endAddress.isEmpty()) {
                showMsg("请输入要前往的目的地");
                return false;
            }

            //当出发地输入框有值的时候，判断这个值是否是定位的地址，是则说明你没有更改过，则不需要进行地址转坐标，不是则需要转换。
            if (!locationAddress.equals(startAddress)) {
                tag = 1;
                GeocodeQuery startQuery = new GeocodeQuery(startAddress, city);
                geocodeSearch.getFromLocationNameAsyn(startQuery);
            } else {
                tag = -1;
            }

            InputMethodManager imm = (InputMethodManager) getSystemService(Context.INPUT_METHOD_SERVICE);
            //隐藏软键盘
            imm.hideSoftInputFromWindow(getWindow().getDecorView().getWindowToken(), 0);

            //通过输入的目的地转为经纬度，然后进行地图上添加标点，最后计算出行路线规划

            // name表示地址，第二个参数表示查询城市，中文或者中文全拼，citycode、adcode
            GeocodeQuery endQuery = new GeocodeQuery(endAddress, city);
            geocodeSearch.getFromLocationNameAsyn(endQuery);
            return true;
        }
        return false;
    }

}
