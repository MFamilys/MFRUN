package com.example.mfamilys.mrun.Util;

import android.content.Context;
import android.util.SparseBooleanArray;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.SimpleAdapter;
import android.widget.TextView;

import com.example.mfamilys.mrun.R;
import com.example.mfamilys.mrun.ViewActivity.CircleImageView;
import com.example.mfamilys.mrun.ViewActivity.ExpandTextView;
import com.example.mfamilys.mrun.ViewActivity.NoScollGridView;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;


public class SampleListItemAdapter extends BaseAdapter {
    //内容
    private final Context mContext;
    //稀疏数组
    private final SparseBooleanArray mCollapsedStatus;
    //字符串数组
    private final String[] sampleStrings;
    //图像数组
    private int[] ImageIds;
    private final List<Map<String,Object>> mlist;

    //结果数组
    List<HashMap<String,Object>> data;
    //布局资源
    int resource;
    //来源数组
    String[] from;
    //目标数组
    int[] to;

    public SampleListItemAdapter(Context context,List<HashMap<String,Object>> data,
                                 int resource,String[] from,int[] to) {
        this.mContext = context;
        this.data = data;
        this.resource=resource;
        this.from=from;
        this.to=to;
        mCollapsedStatus = new SparseBooleanArray();
        //测试数据
        sampleStrings = context.getResources().getStringArray(R.array.context);

        ImageIds=new int[]{
                R.drawable.test1,R.drawable.test2,R.drawable.test3,
                R.drawable.test4,R.drawable.test5,R.drawable.test6
        };
        mlist=new ArrayList<Map<String, Object>>();
        for(int i=0;i<ImageIds.length;i++){
            Map<String,Object> listitem=new HashMap<String, Object>();
            listitem.put("image",ImageIds[i]);
            mlist.add(listitem);
        }

    }
    //列表项的个数有字符串数组的个数决定
    @Override
    public int getCount() {
        return sampleStrings.length;
    }
    //根据位置返回View
    @Override
    public Object getItem(int position) {
        return null;
    }

    @Override
    public long getItemId(int position) {
        return 0;
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        final ViewHolder viewHolder;
        //组件的加载,用viewholder实例化保存,方便下次重载
        if (convertView == null) {
            convertView = LayoutInflater.from(mContext).inflate(R.layout.view_item, parent, false);
            viewHolder = new ViewHolder();
           // viewHolder.userHeader=(CircleImageView)convertView.findViewById(R.id.user_header);
         //   viewHolder.userName=(TextView)convertView.findViewById(R.id.user_name);
            viewHolder.expandTextView = (ExpandTextView) convertView.findViewById(R.id.expand_text_view);
            viewHolder.noScollGridView=(NoScollGridView)convertView.findViewById(R.id.user_Image);
            convertView.setTag(viewHolder);
        } else {
            viewHolder = (ViewHolder) convertView.getTag();
        }
        viewHolder.expandTextView.setText(sampleStrings[position], mCollapsedStatus, position);
      //  viewHolder.userName.setText(data.get(position).get("account").toString());
       // viewHolder.expandTextView.setText(data.get(position).get("content").toString(),mCollapsedStatus,position);


        //动态图的信息保存适配器
        SimpleAdapter simpleAdapter=new SimpleAdapter(this.mContext,mlist,R.layout.gradview_item,
               new String[]{"image"},new int[]{R.id.GradView_item});
        viewHolder.noScollGridView.setAdapter(simpleAdapter);
        return convertView;
    }


    private static class ViewHolder{
       //用户头像
     //  CircleImageView userHeader;
       //用户姓名
       TextView userName;
       //动态内容
       ExpandTextView expandTextView;
       //动态图片
       NoScollGridView noScollGridView;
    }
}