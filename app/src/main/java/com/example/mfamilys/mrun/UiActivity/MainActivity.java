package com.example.mfamilys.mrun.UiActivity;

import android.app.ActionBar;
import android.app.Activity;
import android.app.LocalActivityManager;
import android.content.Intent;
import android.graphics.Color;
import android.os.Bundle;
import android.support.v4.view.PagerAdapter;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewConfiguration;
import android.support.v4.view.ViewPager;
import android.view.ViewGroup;
import android.widget.ImageButton;
import android.widget.LinearLayout;

import com.example.mfamilys.mrun.R;

import java.lang.reflect.Field;
import java.util.ArrayList;
import java.util.List;


public class MainActivity extends Activity implements
        android.view.View.OnClickListener {

    private ViewPager mViewPager;//页面切换中介容器
    private PagerAdapter mPageAdapter;//用来包装view
    public  static List<View> mViews =new ArrayList<View>();//页面列表容器
    //五个按钮布局
    private LinearLayout mTabMain;
    private LinearLayout mTabAderss;
    private LinearLayout mTabTotal;
    private LinearLayout mTabSetting;
    private LinearLayout mTabFriend;
    //五个按钮
    private ImageButton mStart;
    private ImageButton Run_StartOrStop;
    //活动管理类对象
    LocalActivityManager manager=null;
    //通信
    private Intent intent;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        manager=new LocalActivityManager(this,true);
        manager.dispatchCreate(savedInstanceState); //用来将主Activity的触摸事件信息分发到子Activity里
        ActionBar actionBar=getActionBar();

        //设置返回按钮可点击并加上一个小箭头
       try{

            actionBar.setDisplayHomeAsUpEnabled(true);
           }
        catch (NullPointerException e) {
            System.out.print("That is the exception caused by actionbar");
        }
        //设置overflow始终可见　不受物理按钮影响
        try {
            ViewConfiguration mconfig = ViewConfiguration.get(this);
            Field menuKeyField = ViewConfiguration.class.getDeclaredField("sHasPermanentMenuKey");
            if(menuKeyField != null) {
                menuKeyField.setAccessible(true);
                menuKeyField.setBoolean(mconfig, false);
            }
        } catch (Exception ex) {
            System.out.print("That is Exception caused by menu");
        }
        initView();
        initViewPage();
        initEvent();

    }
    @Override
    public void onClick(View v){
      switch (v.getId()){
          case R.id.id_tabMain:
              mViewPager.setCurrentItem(0);
              break;
          case R.id.id_tabFriend:
              mViewPager.setCurrentItem(1);
              break;
          case R.id.id_tabTotal:
              mViewPager.setCurrentItem(2);
              break;
          case R.id.id_tabAderss:
              mViewPager.setCurrentItem(3);
              break;
          case R.id.id_tabSetting:
              mViewPager.setCurrentItem(4);
              break;
          case R.id.Start:
              mViewPager.setCurrentItem(3);
              Run_StartOrStop.callOnClick();

              break;
      }
    }
    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.menu_main, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        int id = item.getItemId();

        //noinspection SimplifiableIfStatement
        if (id == R.id.action_settings) {
            return true;
        }

        return super.onOptionsItemSelected(item);
    }

    private void initEvent() {
        mStart.setOnClickListener(this);
        mTabMain.setOnClickListener(this);
        mTabFriend.setOnClickListener(this);
        mTabAderss.setOnClickListener(this);
        mTabTotal.setOnClickListener(this);
        mTabSetting.setOnClickListener(this);
        mTabMain.setBackgroundColor(Color.parseColor("#aaaaaa00"));//初始化时主页按钮是选中的
        mViewPager.setOnPageChangeListener(new ViewPager.OnPageChangeListener() {
            @Override
            public void onPageSelected(int arg0) {
                int currentItem = mViewPager.getCurrentItem();
                switch (currentItem) {
                    case 0:
                        resetImg();
                        mTabMain.setBackgroundColor(Color.parseColor("#aaaaaa00"));
                        break;
                    case 1:
                        resetImg();
                        mTabFriend.setBackgroundColor(Color.parseColor("#aaaaaa00"));
                        break;
                    case 2:
                        resetImg();
                        mTabTotal.setBackgroundColor(Color.parseColor("#aaaaaa00"));
                        break;
                    case 3:
                        resetImg();
                        mTabAderss.setBackgroundColor(Color.parseColor("#aaaaaa00"));
                        break;
                    case 4:
                        resetImg();
                        mTabSetting.setBackgroundColor(Color.parseColor("#aaaaaa00"));
                        break;
                    default:
                        break;
                }
            }

            public void onPageScrolled(int arg0, float arg1, int arg2) {

            }

            @Override
            public void onPageScrollStateChanged(int arg0) {
            }

        });
    }
        //初始化设置
        private void initView(){
          //初始化五个LinearLayout
          mViewPager=(ViewPager)findViewById(R.id.viewpager);
          mTabMain=(LinearLayout)findViewById(R.id.id_tabMain);
          mTabAderss=(LinearLayout)findViewById(R.id.id_tabAderss);
          mTabFriend=(LinearLayout)findViewById(R.id.id_tabFriend);
          mTabTotal=(LinearLayout)findViewById(R.id.id_tabTotal);
          mTabSetting=(LinearLayout)findViewById(R.id.id_tabSetting);
        }

        private void initViewPage(){
        //初始化四个页面
         LayoutInflater mlayoutInflater = LayoutInflater.from(this);
            //加载主页的activity
            intent=new Intent(MainActivity.this,main_layout_Activity.class);
            View Main=manager.startActivity("main",intent).getDecorView();
            View Friend =mlayoutInflater.inflate(R.layout.friend_layout,null);
            View Total=mlayoutInflater.inflate(R.layout.total_layout,null);
            View Setting=mlayoutInflater.inflate(R.layout.setting_layout,null);
            //加载地图的activity
            intent=new Intent(MainActivity.this,adess_layout_Activity.class);
            View Aderss=manager.startActivity("adess",intent).getDecorView();
            mViews.add(Main);
            mViews.add(Friend);
            mViews.add(Total);
            mViews.add(Aderss);
            mViews.add(Setting);

         //向viewPage提供内容
         mPageAdapter = new PagerAdapter() {
            @Override
            public int getCount() {
                return mViews.size();
            }

            @Override
            public boolean isViewFromObject(View view, Object o) {
                return view==o;
            }

            @Override
            public void destroyItem(ViewGroup container,int position,Object object){
                container.removeView(mViews.get(position));
            }

            @Override
            public Object instantiateItem(ViewGroup container,int position){
                View view = mViews.get(position);
                ViewGroup parent = (ViewGroup) view.getParent();
                if (parent != null) {
                    parent.removeAllViews();
                }
                container.addView(view);
                return view;
            }

        };
        mViewPager.setAdapter(mPageAdapter);
        mViewPager.setOffscreenPageLimit(4);
       //初始化开始按钮
       mStart=(ImageButton)MainActivity.mViews.get(2).findViewById(R.id.Start);
       Run_StartOrStop=(ImageButton)MainActivity.mViews.get(3).findViewById(R.id.Run_StartOrStop);
    }
    //背景初始化
    private  void resetImg(){
        mTabMain.setBackgroundColor(Color.parseColor("#ffcc0000"));
        mTabFriend.setBackgroundColor(Color.parseColor("#ffcc0000"));
        mTabAderss.setBackgroundColor(Color.parseColor("#ffcc0000"));
        mTabTotal.setBackgroundColor(Color.parseColor("#ffcc0000"));
        mTabSetting.setBackgroundColor(Color.parseColor("#ffcc0000"));
    }

}


