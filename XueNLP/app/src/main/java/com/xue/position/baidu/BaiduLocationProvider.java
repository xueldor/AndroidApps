package com.xue.position.baidu;

import android.content.Context;
import android.location.Location;
import android.os.Handler;
import android.os.Looper;
import android.os.Message;
import android.os.SystemClock;
import android.os.WorkSource;
import android.util.Log;

import androidx.annotation.NonNull;

import com.android.location.provider.LocationProviderBase;
import com.android.location.provider.ProviderPropertiesUnbundled;
import com.android.location.provider.ProviderRequestUnbundled;
import com.baidu.location.BDAbstractLocationListener;
import com.baidu.location.BDLocation;
import com.baidu.location.LocationClient;
import com.baidu.location.LocationClientOption;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.TimeZone;

public class BaiduLocationProvider extends LocationProviderBase {
    private static final String TAG = "BaiduLocationProvider";
    private static BaiduLocationProvider instance;
    private static ProviderPropertiesUnbundled mProperties = ProviderPropertiesUnbundled.create(true, false, true, false, false, false, false, 1, 1);
    private static Context sContext;

    private LocationClientOption option = new LocationClientOption();
    private LocationClient mBaiduClient;
    private Location mLocation = new Location("network");

    public static BaiduLocationProvider getInstance(Context context) {
        sContext = context;
        if (instance == null) {
            instance = new BaiduLocationProvider("BaiduNetworkLocationProvider", mProperties);
        }
        return instance;
    }
    public BaiduLocationProvider(String tag, ProviderPropertiesUnbundled properties) {
        super(tag, properties);
//        initBaidu();
    }

    private void initBaidu(){
        mBaiduClient = new LocationClient(sContext.getApplicationContext());
        option.setLocationMode(LocationClientOption.LocationMode.Battery_Saving); // 可选，默认高精度，设置定位模式，高精度，低功耗，仅设备
        option.setCoorType("wgs84"); // 可选，默认gcj02，设置返回的定位结果坐标系
        option.setScanSpan(0); // 可选，默认0，即仅定位一次，设置发起定位请求的间隔需要大于等于1000ms才是有效的
        option.setIsNeedAddress(true); // 可选，设置是否需要地址信息，默认不需要
        option.setOpenGps(false); // 可选，默认false,设置是否使用gps
        option.setLocationNotify(false); // 可选，默认false，设置是否当gps有效时按照1S1次频率输出GPS结果
        option.setIsNeedLocationDescribe(false); // 可选，默认false，设置是否需要位置语义化结果，可以在BDLocation.getLocationDescribe里得到，结果类似于“在北京天安门附近”
        option.setIsNeedLocationPoiList(false); // 可选，默认false，设置是否需要POI结果，可以在BDLocation.getPoiList里得到
        option.setIgnoreKillProcess(false); // 可选，默认true，定位SDK内部是一个SERVICE，并放到了独立进程，设置是否在stop的时候杀死这个进程，默认不杀死
        option.SetIgnoreCacheException(true); // 可选，默认false，设置是否收集CRASH信息，默认收集
        option.setEnableSimulateGps(false); // 可选，默认false，设置是否需要过滤gps仿真结果，默认需要
        option.setWifiCacheTimeOut(5 * 60 * 1000); // 可选，如果设置了该接口，首次启动定位时，会先判断当前WiFi是否超出有效期，若超出有效期，会先重新扫描WiFi，然后定位

        mBaiduClient.setLocOption(option);
        mBaiduClient.registerLocationListener(mBaiduListener);
    }

    @Override
    protected void onSetRequest(ProviderRequestUnbundled request, WorkSource source) {
        Message obtain = Message.obtain(mHandler, ON_SET_REQUEST);
        obtain.obj = new SetRequest(request, source);
        obtain.sendToTarget();
    }

    int i = 0;
    private final BDAbstractLocationListener mBaiduListener = new BDAbstractLocationListener() {
        @Override
        public void onReceiveLocation(BDLocation bdLocation) {
            if(option.scanSpan == 0) {
                cancel();
            }
            switch (bdLocation.getLocType()) {
                case BDLocation.TypeGpsLocation:
                case BDLocation.TypeNetWorkLocation:
                    Log.i(TAG, "(" + bdLocation.getLatitude() + "," + bdLocation.getLongitude() + ")");
                    mLocation.setLatitude(bdLocation.getLatitude());
                    mLocation.setLongitude(bdLocation.getLongitude());
                    mLocation.setAccuracy(bdLocation.getRadius());
                    SimpleDateFormat simpleDateFormat = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
                    try {
                        simpleDateFormat.setTimeZone(TimeZone.getTimeZone("GMT+8"));
                        Date parse = simpleDateFormat.parse(bdLocation.getTime());
                        mLocation.setTime(parse.getTime());
                        mLocation.setElapsedRealtimeNanos(SystemClock.elapsedRealtimeNanos());
                    } catch (Exception e) {
                        e.printStackTrace();
                        mLocation.setTime(System.currentTimeMillis());
                        mLocation.setElapsedRealtimeNanos(System.currentTimeMillis());
                    }
                    i++;
                    Log.i(TAG, "xue count " + i);
                    //上报location
                    reportLocation(mLocation);
                    break;
                default:
                    Log.e(TAG, "BDLocation: " + bdLocation.getLocType());
                    break;
            }
        }
    };

    public void requestLocation(){
        Log.i(TAG, "requestLocation");
        initBaidu();
        mBaiduClient.start();
    }

    //TODO 因为目前没有申请百度证书，此方法暂注释
//    private void setScanSpan(long interval) {
//        int i = 0;//默认0，即仅定位一次
//        if (interval > 86400000) {
//            i = 86400000;
//        } else if (interval >= 1000) {
//            i = (int) interval;
//        }
//        option.setScanSpan(i);
//        mBaiduClient.setLocOption(option);
//    }

    public void cancel() {
        if (mBaiduClient != null) {
            mBaiduClient.stop();
            mBaiduClient = null;
        }
    }


    private static final int ON_SET_REQUEST = 1;
    private Handler mHandler = new Handler(Looper.getMainLooper(), new Handler.Callback() {
        @Override
        public boolean handleMessage(@NonNull Message message) {
            switch (message.what){
                case ON_SET_REQUEST:
                    if(mHandler.hasMessages(ON_SET_REQUEST)) {
                        mHandler.removeMessages(ON_SET_REQUEST);
                    }
                    SetRequest set = (SetRequest)message.obj;
                    ProviderRequestUnbundled request = set.request;
                    long interval = request.getInterval();
                    Log.i(TAG, "ignore interval " + interval + "ms");
//                    setScanSpan(interval);
                    requestLocation();
                default:
                    break;
            }
            return false;
        }
    });


    class SetRequest{
        public SetRequest(ProviderRequestUnbundled request, WorkSource source) {
            this.request = request;
            this.source = source;
        }
        ProviderRequestUnbundled request;
        WorkSource source;
    }
}
