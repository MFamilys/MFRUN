package com.example.mfamilys.mrun.UiActivity;

import android.app.ActionBar;
import android.app.Activity;
import android.app.AlarmManager;
import android.app.AlertDialog;
import android.content.Context;
import android.database.SQLException;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteException;
import android.graphics.Color;
import android.location.Location;
import android.os.Bundle;
import android.os.SystemClock;
import android.util.Log;
import android.view.Display;
import android.view.Menu;
import android.view.MenuItem;
import android.view.MotionEvent;
import android.view.View;
import android.view.Window;
import android.view.WindowManager;
import android.widget.Chronometer;
import android.widget.FrameLayout;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;
import com.amap.api.location.AMapLocalWeatherForecast;
import com.amap.api.location.AMapLocalWeatherListener;
import com.amap.api.location.AMapLocalWeatherLive;
import com.amap.api.location.AMapLocation;
import com.amap.api.location.AMapLocationListener;
import com.amap.api.location.LocationManagerProxy;
import com.amap.api.location.LocationProviderProxy;
import com.amap.api.maps2d.model.Marker;
import com.amap.api.maps2d.CameraUpdateFactory;
import com.amap.api.maps2d.CameraUpdate;
import com.amap.api.maps2d.AMapOptions;
import com.amap.api.maps2d.UiSettings;
import com.amap.api.maps2d.LocationSource;
import com.amap.api.maps2d.AMap;
import com.amap.api.maps2d.MapView;
import com.amap.api.maps2d.model.BitmapDescriptorFactory;
import com.amap.api.maps2d.model.LatLng;
import com.amap.api.maps2d.model.MarkerOptions;
import com.amap.api.maps2d.model.MyLocationStyle;
import com.amap.api.maps2d.model.Polyline;
import com.amap.api.maps2d.model.PolylineOptions;
import com.example.mfamilys.mrun.DAO.DB_Control;
import com.example.mfamilys.mrun.DAO.DB_Meta;
import com.example.mfamilys.mrun.R;
import com.example.mfamilys.mrun.Util.DB_NameHelper;
import com.example.mfamilys.mrun.ViewActivity.SlideButton;

import java.io.File;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.logging.Logger;

public class adess_layout_Activity extends Activity
        implements LocationSource,AMapLocationListener,AMapLocalWeatherListener{
    //地图控件
    private AMap aMap;
    private MapView mapView;
    private UiSettings mUiSettings;
    //地图监听器
    private OnLocationChangedListener mListener;
    private LocationManagerProxy mAMapLocationManager;
    //地图显示控制对象
    private CameraUpdate cameraUpdate;
    //地图绘画折线对象
    private Polyline polyline;
    private PolylineOptions mPolylineOptions;
    //地图标记对象
    private Marker marker;
    private MarkerOptions SMarkerOptions;
    private MarkerOptions EMarkerOptions;
    //位置坐标(经纬度范围-180-180)
    private double SgeoLat=360.0;
    private double SgeoLng=360.0;
    private double geoLat;
    private double geoLng;
    private double TgeoLat;
    private double TgeoLng;
    private AMapLocation lastComputeLocation;
    //天气组件
    private ImageView mImgWeather;
    private TextView mTextWeather;
    private String SWeather;
    //系统总距离差
    private double Dlatlng=0.0;
    //布局按钮
    private ImageButton Run_StartOrStop;
    private ImageButton Run_Continue;
    private ImageButton Run_Stop;
    //是否点击标志
    private boolean Run_start_isclick=false;
    //跑步是否结束标志
    private boolean Is_Over=false;
    //提示框组件
    private SlideButton mSlideButton;
    private AlertDialog mAlertDialog;
    //计时器组件
    private Chronometer mChronometer;
    private long recordtime=0;
    private long costtime=0;
    private String Stime="0.0";
    //数据文本组件
    private TextView run_distance_text;
    private TextView run_speed_text;
    private TextView run_coluli_text;
    //闹钟管理
    private AlarmManager malarmManager;
    //导航条组件
    private ActionBar actionBar;
    //数据库组件
    private DB_Control db_control;
    private DB_Meta  meta;
    private DB_NameHelper nameHelper;
    private String dbName;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.adess_layout);
        //加载地图组件
        mapView = (MapView) findViewById(R.id.map);
        mapView.onCreate(savedInstanceState);//不能省略
        initDB();
        initMapView();
        initWeatherView();
        initPolyline();
        initDataText();
        initButtonView();
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.menu_adess, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        int id = item.getItemId();
        if (id == R.id.action_settings) {
            return true;
        }
        return super.onOptionsItemSelected(item);
    }


    //初始化AMap对象
    private void initMapView() {
        if (aMap == null) {
            aMap = mapView.getMap();
            setupMap();
        }
        //设置不响应父组件(ViewPager)的触摸,避免滑动冲突
        mapView.getParent().requestDisallowInterceptTouchEvent(true);
    }

    //设置AMap的属性
    private void setupMap() {
        //自定义系统定位小蓝点对象
        MyLocationStyle myLocationStyle = new MyLocationStyle();
        //自定义系统定位图标
        myLocationStyle.myLocationIcon(BitmapDescriptorFactory.fromResource(R.drawable.location_marker));
        //定义边界框颜色
        myLocationStyle.strokeColor(Color.parseColor("#ffffffff"));
        //定义圆形填充颜色
        myLocationStyle.radiusFillColor(Color.argb(0, 0, 0, 0));
        //定义圆形的边框粗细
        myLocationStyle.strokeWidth(1);
        //加载自定义风格对象
        aMap.setMyLocationStyle(myLocationStyle);
        //设置定位监听
        aMap.setLocationSource(this);
        //设置定位按钮显示
        aMap.getUiSettings().setMyLocationButtonEnabled(true);
        //设置显示定位层
        aMap.setMyLocationEnabled(true);
        //设置地图模式
        aMap.setMapType(AMap.MAP_TYPE_NORMAL);//矢量地图
        //aMap.setMapType(AMap.MAP_TYPE_SATELLITE);//卫星地图
        // 设置地图logo显示在左下方
        mUiSettings = aMap.getUiSettings();
        mUiSettings.setLogoPosition(AMapOptions.LOGO_POSITION_BOTTOM_LEFT);
        //禁止显示缩放按钮
        //mUiSettings.setZoomControlsEnabled(false);
        //设置初始缩放等级(4-20)
        cameraUpdate= CameraUpdateFactory.zoomTo(18);
        aMap.moveCamera(cameraUpdate);
    }

    //初始化天气组件
    private void initWeatherView(){
        mImgWeather=(ImageView)findViewById(R.id.ImgWeather);
        mTextWeather=(TextView)findViewById(R.id.TextWeather);
    }

    //初始化折线类
    private void initPolyline(){
        //创建折线选项类
        mPolylineOptions=new PolylineOptions();
        //设置折线的颜色
        mPolylineOptions.color(Color.parseColor("#ffcc0000"));
        //设置折线的边框大小
        mPolylineOptions.width(10);
        //设置折线的可见性
        mPolylineOptions.visible(true);

    }

    //初始化标志对象
    private MarkerOptions initMarker(double geoLat,double geoLng,String title,int Imgid){
        MarkerOptions mMarkerOptions = new MarkerOptions();
        mMarkerOptions.position(new LatLng(geoLat, geoLng))
                .title(title)
                .snippet(getDate())
                .icon(BitmapDescriptorFactory.fromResource(Imgid))
                .visible(true);
        return mMarkerOptions;
    }

    //初始化数据组件和文本
    private void initDataText(){
        //初始化公里数
        run_distance_text=(TextView)findViewById(R.id.run_distance);
        run_distance_text.setText("0.00公里");
        //初始化速度
        run_speed_text=(TextView)findViewById(R.id.run_speed);
        run_speed_text.setText("0.00");
        //初始化卡路里
        run_coluli_text=(TextView)findViewById(R.id.run_coluli);
        run_coluli_text.setText("0.00");
        //初始化计时器
        mChronometer=(Chronometer)findViewById(R.id.run_time);
        mChronometer.setText("00:00:00");
        //重写系统检测时间,转换为时分秒格式
        mChronometer.setOnChronometerTickListener(new Chronometer.OnChronometerTickListener() {
            @Override
            public void onChronometerTick(Chronometer chronometer) {
                costtime = SystemClock.elapsedRealtime() - chronometer.getBase();
                int h =(int)(costtime /3600000);
                int m =(int)(costtime - h*3600000)/60000;
                int s= (int)(costtime - h*3600000- m*60000)/1000 ;
                String hh = h < 10 ? "0"+h: h+"";
                String mm = m < 10 ? "0"+m: m+"";
                String ss = s < 10 ? "0"+s: s+"";
                //记录时间格式
                Stime=hh+":"+mm+":"+ss;
                chronometer.setText(Stime);
            }
        });

    }

    //初始化布局按钮
    private void initButtonView(){
        //加载跑步开始按钮
        Run_StartOrStop=(ImageButton)findViewById(R.id.Run_StartOrStop);
        Run_StartOrStop.setOnClickListener(new View.OnClickListener(){
            @Override
            public void onClick(View view) {
                //开启计时器
                onRecordStart();
                if(!Run_start_isclick) {
                   run_Start();
                }else{
                   run_N_Start();
                }
            }
        });
        //加载跑步继续和结束按钮
        Run_Continue=(ImageButton)findViewById(R.id.Run_continue);
        Run_Continue.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
              run_Continue();
            }
        });
        Run_Stop=(ImageButton)findViewById(R.id.Run_stop);
        Run_Stop.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
               run_Stop();
            }
        });
    }

    private void initDB(){
        db_control=new DB_Control(this);
        nameHelper=new DB_NameHelper(this);
    }
    //跑步开始
    private void run_Start() {
        //如果程序第一次开始
        if (!Is_Over) {

            if(!DB_NameHelper.isExternalStoragePresent()){
                Toast.makeText(this,"没有外部内存卡",Toast.LENGTH_LONG).show();
                return;
            }
            boolean hasResumeName=nameHelper.hasResumeName();
            if(hasResumeName){
                dbName=nameHelper.getResumeName();
                Toast.makeText(this,"恢复异常关闭文件",Toast.LENGTH_SHORT).show();
            }else{
                //设置文件名按日来分
                nameHelper.setFilesRecordBy(0x001);
                dbName=nameHelper.getNewName();
            }
            try {
                db_control.open(dbName, DB_Control.MODE_READ_WRITER);
                //第一次打开时设置时间(美国时间?)
                if (!hasResumeName) {
                    db_control.getMeta().setStartTime(new Date());
                }
                nameHelper.setLastOpenedName(dbName);
            }catch (SQLiteException e){
                Log.w("MFRUN",e.getMessage());
            }
            Toast.makeText(adess_layout_Activity.this, "开始跑步", Toast.LENGTH_SHORT).show();
            Run_StartOrStop.setImageResource(R.drawable.run_stop);
            Run_start_isclick = true;
            ShowViewDialog();
         //如果程序重新开始
        } else {
            //清除地图上显示所有的覆盖物并重置
            aMap.clear();
            setupMap();
            //重置折线对象
            initPolyline();
            aMap.invalidate();
            //清除计时
            initDataText();
            onRecordStop();
            //激活定位
            mapView.onResume();
            Is_Over = false;
        }
    }
    //跑步结束,在锁屏框被Back键取消时点击
    private void run_N_Start(){
        Toast.makeText(adess_layout_Activity.this, "跑步结束", Toast.LENGTH_LONG).show();
        //直接引用停止逻辑
        run_Stop();
    }

    //跑步继续
    private void run_Continue(){
        Run_StartOrStop.setImageResource(R.drawable.run_stop);
        //重新唤醒,激活定位
        mapView.onResume();
        //重新开始定时
        onRecordStart();
        Run_StartOrStop.setClickable(true);
        Toast.makeText(adess_layout_Activity.this,"继续跑步",Toast.LENGTH_LONG).show();
        //锁住屏幕
        ShowViewDialog();
        //重新隐藏按钮
        Run_Continue.setVisibility(View.INVISIBLE);
        Run_Stop.setVisibility(View.INVISIBLE);
    }

    //跑步结束
    private void run_Stop(){
        db_control.getMeta().setRawDistance((Dlatlng/1000));
        db_control.getMeta().setCostTime(Stime);
        long totalcount=db_control.getMeta().getCount();
        double RawDistance=db_control.getMeta().getDistance();
        if(RawDistance<=0){
             (new File(dbName)).delete();
            Toast.makeText(this,"没有记录任何事情",Toast.LENGTH_SHORT).show();
        }else{
            db_control.getMeta().setEndTime(new Date());
            Toast.makeText(this,"已经记录"+totalcount+"条记录",Toast.LENGTH_SHORT).show();
        }
        //正常结束时清理最近未关闭文件
        nameHelper.clearLastOpenedName();
        db_control.close();
        onRecordStop();
        //重置图标
        Run_StartOrStop.setImageResource(R.drawable.run_start);
        //停止定位
        deactivate();
        //重新隐藏按钮
        Run_Continue.setVisibility(View.INVISIBLE);
        Run_Stop.setVisibility(View.INVISIBLE);
        Toast.makeText(adess_layout_Activity.this,"跑步结束",Toast.LENGTH_LONG).show();
        //切换按钮状态
        Run_StartOrStop.setImageResource(R.drawable.run_start);
        Run_StartOrStop.setClickable(true);
        Run_start_isclick=false;
        //利用停止定位前的最后一个坐标来设立终点
        //初始化终点标志
        EMarkerOptions=initMarker(geoLat,geoLng,"终点",R.drawable.end);
        //添加终点标志
        marker = aMap.addMarker(EMarkerOptions);
        //把终点加入轨迹折线
        mPolylineOptions.add(new LatLng(geoLat, geoLng));
        aMap.invalidate();
        //重新设置起点标志
        SgeoLat=360;
        SgeoLng=360;
        //重新设置公里数
        Dlatlng=0;
        //设置跑步结束标志
        Is_Over=true;
    }


    @Override
    protected void onSaveInstanceState(Bundle outState) {
        super.onSaveInstanceState(outState);
        mapView.onSaveInstanceState(outState);
    }

    @Override
    protected void onResume() {
        super.onResume();
        mapView.onResume();

    }

    @Override
    protected void onPause() {
        super.onPause();
        mapView.onPause();
    }

    @Override
    protected void onDestroy() {
        //如果消息款没点掉,则强行关闭
        if(mAlertDialog!=null) {
            mAlertDialog.dismiss();
        }
        super.onDestroy();
        mapView.onDestroy();

    }

    //激活定位
    @Override
    public void activate(OnLocationChangedListener listener) {
        mListener = listener;
        if (mAMapLocationManager == null) {
            mAMapLocationManager = LocationManagerProxy.getInstance(this);
            //获取定位信息
            mAMapLocationManager.requestLocationData(
                    LocationProviderProxy.AMapNetwork, 500, 5,this );
            //获取当前天气信息
            mAMapLocationManager.requestWeatherUpdates(
                    LocationManagerProxy.WEATHER_TYPE_LIVE,this);
        }

    }

    //停止定位
    @Override
    public void deactivate() {
        mListener = null;
        if (mAMapLocationManager != null) {
            mAMapLocationManager.removeUpdates(this);
            mAMapLocationManager.destroy();
        }
        mAMapLocationManager = null;
    }

    @Override
    public void onLocationChanged(Location location) {
    }

    @Override
    public void onProviderDisabled(String provider) {
    }

    @Override
    public void onProviderEnabled(String provider) {
    }

    @Override
    public void onStatusChanged(String provider, int status, Bundle extras) {
    }

    //定位成功后的回调函数
    @Override
    public void onLocationChanged(AMapLocation aMapLocation) {
        //显示系统定位图标
        if (mListener != null && aMapLocation != null) {
            mListener.onLocationChanged(aMapLocation);
        }
        //当点击的标志为true的时候才启动定位画图
        if(Run_start_isclick){
          //定位成功时获取坐标
          if(aMapLocation != null && aMapLocation.getAMapException().getErrorCode() == 0) {
            //获取位置信息
             TgeoLat = aMapLocation.getLatitude();  TgeoLng = aMapLocation.getLongitude();
            //定义起点
            if (SgeoLat == 360.0 || SgeoLng == 360.0) {
                //修改起点坐标
                SgeoLat = TgeoLat;  SgeoLng = TgeoLng;
                //将第一次定位的坐标保存下来
                geoLat = SgeoLat;  geoLng = SgeoLng;
                //初始化起点标志
                SMarkerOptions=initMarker(SgeoLat,SgeoLng,"起点",R.drawable.start);
                //添加起点标志
                marker = aMap.addMarker(SMarkerOptions);
                //把起点加入轨迹折线
                mPolylineOptions.add(new LatLng(SgeoLat, SgeoLng));
                aMap.invalidate();
            }
            //起点之后,每当与上次定位相差距离超过1.11米时,才绘制(经纬度１度等于111KM)
            else if(Math.abs(TgeoLat - geoLat) >= 0.00001 || Math.abs(TgeoLng - geoLng) >= 0.00001){

                //将经纬度转换成米
                double Dlat =Math.abs(TgeoLat - geoLat) * 111000;
                double Dlng =Math.abs(TgeoLng - geoLng) * 111000;
                //计算每秒距离差(较小),利用勾股定理
                Dlatlng = Dlatlng + Math.sqrt(Math.pow(Dlat,2) + Math.pow(Dlng,2));
                //将每次定位的坐标保存下来以便与下一次比较
                geoLat = TgeoLat; geoLng = TgeoLng;
                //每次定位成功就把折线添加到地图上
                mPolylineOptions.add(new LatLng(TgeoLat, TgeoLng));
                polyline = aMap.addPolyline(mPolylineOptions);
                //刷新地图
                aMap.invalidate();
                //修改数据
                setDataText(Dlatlng);
            }
          }

        }
    }



    //设置文本
    private void setDataText(double Dlatlng){
        //接受的是距离差,以米位单位
        //距离差文本－－将距离差转换成以公里为单位字符串
        double distance =Dlatlng/1000;
        run_distance_text.setText(String.format(getResources().getString(R.string.run_distance),distance));
        //速度文本
        double speed=(Dlatlng*1000/costtime);//米/秒
        run_speed_text.setText(String.format(getResources().getString(R.string.run_speed),speed));
        //消耗卡路里文本(利用60公斤的人做标本)(体重＊距离＊1.036)
        double coluli=((Dlatlng/1000.0)*60*1.036);
        run_coluli_text.setText(String.format(getResources().getString(R.string.run_coluli),coluli));
    }


    //当前天气查询
    @Override
    public void onWeatherLiveSearched(AMapLocalWeatherLive aMapLocalWeatherLive){
       //实况天气查询成功回调，设置天气信息
        if(aMapLocalWeatherLive!=null&&aMapLocalWeatherLive.getAMapException().getErrorCode()==0){
           //设置温度
            SWeather=aMapLocalWeatherLive.getWeather();
           //设置天气图片
            mImgWeather.setImageResource(getWeatherImg(SWeather));
            mTextWeather.setText(aMapLocalWeatherLive.getTemperature()+"℃");
        }
        else{
            //获取天气失败
            Toast.makeText(this,"获取天气预报失败"+aMapLocalWeatherLive.getAMapException().getErrorMessage(),Toast.LENGTH_SHORT).show();
        }
    }

    //未来天气查询
    @Override
    public void onWeatherForecaseSearched(AMapLocalWeatherForecast arg0){

    }

    //获取系统当前日期时间
    public String getDate(){
        SimpleDateFormat formater=new SimpleDateFormat("yyyy年MM月dd日 HH:mm:ss");
        Date curdate= new Date();
        String Scurdate= formater.format(curdate);
        return Scurdate;
    }

    //返回天气图片
    public int getWeatherImg(String SWeather){
        if(SWeather.equals("晴"))  return R.drawable.weather_sun;
        if(SWeather.endsWith("阴天")) return R.drawable.weather_overcast;
        if(SWeather.equals("多云")) return R.drawable.weather_cloudy;
        if(SWeather.equals("雾")||SWeather.equals("轻霾")||SWeather.equals("霾") ||
           SWeather.equals("浮尘")||SWeather.equals("扬沙"))
            return R.drawable.weather_fog_haze;
        if(SWeather.equals("小雨")||SWeather.equals("阵雨")||SWeather.equals("小雨-中雨"))
            return R.drawable.weather_s_rain;
        if(SWeather.equals("中雨")||SWeather.equals("冻雨")||SWeather.equals("中雨-大雨"))
            return R.drawable.weather_m_rain;
        if(SWeather.equals("大雨")||SWeather.equals("暴雨")||SWeather.equals("大暴雨")|| SWeather.equals("特大暴雨")||
           SWeather.equals("中雨-暴雨")||SWeather.equals("暴雨-大暴雨")||SWeather.equals("大暴雨-特大暴雨"))
            return R.drawable.weather_l_rain;
        if(SWeather.equals("小雪")||SWeather.equals("阵雪")||SWeather.equals("小雪-中雪"))
            return R.drawable.weather_s_snow;
        if(SWeather.equals("中雪")||SWeather.equals("中雪-大雪"))
            return R.drawable.weather_m_snow;
        if(SWeather.equals("大雪")||SWeather.equals("暴雪")||SWeather.equals("大雪-暴雪"))
            return R.drawable.weather_l_snow;
        if(SWeather.equals("雨夹雪"))
            return R.drawable.weather_rain_snow;
        if(SWeather.equals("雷阵雨")||SWeather.equals("雷阵雨并带有冰雹"))
            return R.drawable.weather_thunder_rain;
        if(SWeather.equals("强沙尘暴")) return R.drawable.weather_sandstorm;
        if(SWeather.equals("龙卷风")||SWeather.equals("飑"))
            return R.drawable.weayher_tornado;
        return 0;
    }

    //自定义锁屏对话框
    public void ShowViewDialog(){
        //如果之前的通知框没有被点掉的情况下,强行关闭
        if(mAlertDialog!=null) {
            mAlertDialog.dismiss();
        }
        //加载SlideButton布局和SlideButton按钮
        FrameLayout SlideButtonLy=(FrameLayout)getLayoutInflater().inflate(R.layout.slide_button_layout,null);
        mSlideButton=(SlideButton)SlideButtonLy.findViewById(R.id.SlideButton);
        if(mAlertDialog==null){
        mAlertDialog=new AlertDialog.Builder(adess_layout_Activity.this)
                .setView(SlideButtonLy)
                .create();
        }
        mAlertDialog.show();
       //设置对话框的宽度和高度
        WindowManager m = getWindowManager();
        Window dialogWindow=mAlertDialog.getWindow();
        Display d = m.getDefaultDisplay(); // 获取屏幕宽、高用
         WindowManager.LayoutParams p = dialogWindow.getAttributes(); // 获取对话框当前的参数值
        p.width =dip2px(this,250); // 宽度设置为dp205（与布局一致）
        p.y=220;//垂直偏移量
        dialogWindow.setAttributes(p);

        //设置滑动按钮监听器
        mSlideButton.setOnTouchListener(new View.OnTouchListener() {
            @Override
            public boolean onTouch(View view, MotionEvent motionEvent) {
                if(mSlideButton.handleActivityEvent(motionEvent)){
                    mAlertDialog.dismiss();
                    runWait();
                }
                return false;
            }
        });
    }

    //dp转px函数
    public static int dip2px(Context context, float dpValue) {
        final float scale = context.getResources().getDisplayMetrics().density;
        return (int) (dpValue * scale + 0.5f);
    }

    //定位暂停
    public void runWait(){
        Run_StartOrStop.setImageResource(R.drawable.run_wait);
        //停止定位
        mapView.onPause();
        deactivate();
        //停止定时
        onRecordPause();
        Run_StartOrStop.setClickable(false);
        //显示继续和结束按钮
        Run_Continue.setVisibility(View.VISIBLE);
        Run_Stop.setVisibility(View.VISIBLE);
    }

    //计时器开始函数重写
    private void onRecordStart(){
        //开始时间加上之前记录的时间，实现继续功能
        mChronometer.setBase(SystemClock.elapsedRealtime()-recordtime);
        mChronometer.start();
    }

    //计时器暂停函数重写
    private void onRecordPause(){
        mChronometer.stop();
        //记录下时间，才可以重新开始
        recordtime=SystemClock.elapsedRealtime()-mChronometer.getBase();
    }

    //计时器结束函数重写
    private void onRecordStop(){
        recordtime=0;
        mChronometer.setBase(SystemClock.elapsedRealtime());
    }


        
}

