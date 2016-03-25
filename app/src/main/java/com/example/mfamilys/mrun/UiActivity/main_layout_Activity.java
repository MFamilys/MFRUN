package com.example.mfamilys.mrun.UiActivity;

import android.app.Activity;
import android.content.Context;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.GridView;
import android.widget.ListView;
import android.widget.Toast;

import com.daimajia.slider.library.Animations.DescriptionAnimation;
import com.daimajia.slider.library.SliderLayout;
import com.daimajia.slider.library.SliderTypes.BaseSliderView;
import com.daimajia.slider.library.SliderTypes.TextSliderView;
import com.daimajia.slider.library.Tricks.ViewPagerEx;
import com.example.mfamilys.mrun.R;
import com.example.mfamilys.mrun.Util.HttpHelper;
import com.example.mfamilys.mrun.Util.SampleListItemAdapter;

import org.apache.http.NameValuePair;
import org.apache.http.message.BasicNameValuePair;
import org.json.JSONArray;
import org.json.JSONStringer;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Objects;

public class main_layout_Activity extends Activity implements BaseSliderView.OnSliderClickListener,
        ViewPagerEx.OnPageChangeListener{
     private Context mContext;
     private SliderLayout mSlideLayout;
     private ListView mListView;
     private SampleListItemAdapter sampleListItemAdapter;
     private GridView mgradview;
     private int[] ImageIds;
     private View mview;
     private HttpHelper httpHelper;
     private Handler handler;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.main_layout);
        mContext=main_layout_Activity.this;
        initSlideLayout();
        initListView();


    }
    //自动循环推荐栏的初始化
    private void initSlideLayout(){
        mSlideLayout=(SliderLayout)findViewById(R.id.silder);
        //网络连接图片
        HashMap<String,String> url_maps=new HashMap<String,String>();
        //本地连接图片
        HashMap<String,Integer>file_maps=new HashMap<String,Integer>();
        file_maps.put("What's the younger",R.drawable.run0);
        //file_maps.put("Beyond the default",R.drawable.run1);
        //file_maps.put("The song of time",R.drawable.run3);
        //file_maps.put("Just begin Now",R.drawable.run4);
        //file_maps.put("Life's Great",R.drawable.run5);
        //file_maps.put("Enjoy run,enjoy life",R.drawable.run2);
        //初始化组件
        for(String name:file_maps.keySet()){
            //初始化布局
            TextSliderView textSliderView=new TextSliderView(this);
            //初始化标签
            textSliderView
                    .description(name)
                    .image(file_maps.get(name))
                    .setScaleType(BaseSliderView.ScaleType.Fit)
                    .setOnSliderClickListener(this);
            //增加传送信息
            textSliderView.bundle(new Bundle());
            textSliderView.getBundle()
                    .putString("extra",name);
            //将文本标签加入布局
            mSlideLayout.addSlider(textSliderView);
        }
        //初始化动画
        mSlideLayout.setPresetTransformer(SliderLayout.Transformer.Accordion);
        //初始化指示器
        mSlideLayout.setPresetIndicator(SliderLayout.PresetIndicators.Center_Bottom);
        //初始化装载指示器的动画
        mSlideLayout.setCustomAnimation(new DescriptionAnimation());
        mSlideLayout.setDuration(7000);
        mSlideLayout.addOnPageChangeListener(this);
    }

    private void initListView(){
        mListView=(ListView)findViewById(R.id.list);
        AsynchronousData();
        handler=new Handler(){
            @Override
            public void handleMessage(Message msg) {
                switch (msg.what){
                    case 1:{
                        List<HashMap<String,Object>> result=null;
                        try{
                            result=(List<HashMap<String,Object>>)msg.obj;
                        }catch(Exception e){
                            e.printStackTrace();
                        }
                    sampleListItemAdapter = new SampleListItemAdapter(mContext,result , R.layout.view_item,
                            new String[]{"account", "content"}, new int[]{R.id.user_name, R.id.expandable_text});
                    mListView.setAdapter(sampleListItemAdapter);
                    }break;
                    default:
                        break;
                }
            }
        };

    }

    private void AsynchronousData(){
        new Thread() {
            @Override
            public void run() {
                List<NameValuePair> params = new ArrayList<NameValuePair>();
                params.add(new BasicNameValuePair("page", "1"));
                params.add(new BasicNameValuePair("pagesize", "3"));
                ArrayList<HashMap<String, Object>> result = HttpHelper.getData(params, "http://mfamilys.imwork.net/MFRUN/list.php");
                Log.e("MFRUN","数据获取成功"+result.toString());
                List<HashMap<String, Object>> data = new ArrayList<HashMap<String, Object>>();
                    for (HashMap<String, Object> news : result) {
                        HashMap<String, Object> item = new HashMap<String, Object>();
                        item.put("id", news.get("id"));
                        item.put("account", news.get("account"));
                        item.put("content", news.get("content"));
                        item.put("createTime", news.get("createTime"));
                        item.put("upvoteCount", news.get("upvoteCount"));
                        data.add(item);
                    }
                //异步线程回调,消息传回
                Message message= handler.obtainMessage(1,data);
            }
        }.start();

    }


    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.menu_main_layout_activity, menu);
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

    @Override
    protected void onStop() {
        mSlideLayout.stopAutoCycle();
        super.onStop();
    }

    @Override
    public void onSliderClick(BaseSliderView baseSliderView) {
        Toast.makeText(this, baseSliderView.getBundle().get("extra")+"",Toast.LENGTH_SHORT).show();
    }

    @Override
    public void onPageScrolled(int i, float v, int i1) {
            }
    @Override
    public void onPageSelected(int position){

    }

    @Override
    public void onPageScrollStateChanged(int i) {

    }
}
