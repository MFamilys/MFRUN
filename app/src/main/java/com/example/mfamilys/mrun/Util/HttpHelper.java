package com.example.mfamilys.mrun.Util;

import android.content.Entity;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.util.Log;

import org.apache.http.HttpEntity;
import org.apache.http.HttpInetConnection;
import org.apache.http.HttpResponse;
import org.apache.http.NameValuePair;
import org.apache.http.client.HttpClient;
import org.apache.http.client.entity.UrlEncodedFormEntity;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.impl.client.DefaultHttpClient;
import org.apache.http.protocol.HTTP;
import org.apache.http.util.EntityUtils;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;
import org.json.JSONTokener;

import java.io.IOException;
import java.io.InputStream;
import java.io.UnsupportedEncodingException;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

/**
 * 利用HttpClient方式向服务端获取参数
 * @return json形式的字符串
 */

public class HttpHelper {

    public static  String doPost(List<NameValuePair> params,String url)
    {
        String result=null;
        //获取HttpClient对象
        HttpClient httpClient=new DefaultHttpClient();
        //新建HttpPost对象
        HttpPost httpPost=new HttpPost(url);
        if(params!=null){
            //设置字符集
            try {
                HttpEntity entity = new UrlEncodedFormEntity(params, HTTP.UTF_8);
                //设置参数实体
                httpPost.setEntity(entity);
            }catch (UnsupportedEncodingException e){
                Log.e("MFRUN","不支持UTF-8编码");
            }
        }
        //获取HttpResponse实例
        try{
             HttpResponse httpResponse=httpClient.execute(httpPost);
             //判断是否能够请求成功
            if(httpResponse.getStatusLine().getStatusCode()==200){
            //获取返回的数据
            result= EntityUtils.toString(httpResponse.getEntity(),"UTF-8");
            }else{Log.w("MFRUN", "HttpPost方式请求失败");
            }
        }catch (IOException e){e.printStackTrace();}
            return result;
    }

    /**
     *json解析
     *@return ArrayList
    */
    private static ArrayList<HashMap<String,Object>> Analysis(String jsonStr){
        JSONObject jsonObject=null;
        //初始化list数组对象
        ArrayList<HashMap<String,Object>> list=new ArrayList<HashMap<String, Object>>();
            try {
                jsonObject =new JSONObject(jsonStr);
            }catch(JSONException e){
                Log.e("MFRUN","数据申请异常: "+e.getMessage());
            }
            try{
                //初始化数组对象
                HashMap<String,Object> map=new HashMap<String, Object>();
                //初始化数据对象
                if(jsonObject!=null) {
                    map.put("code", jsonObject.getString("code"));
                    map.put("message", jsonObject.getString("message"));
                    JSONArray jsonArray = jsonObject.getJSONArray("data");
                    for (int i = 0; i < (jsonArray != null ? jsonArray.length() : 0); i++) {
                        JSONObject jsondata = jsonArray.getJSONObject(i);
                        map.put("id", jsondata.getString("id"));
                        map.put("account", jsondata.getString("account"));
                        map.put("content", jsondata.getString("content"));
                        map.put("createTime", jsondata.getString("createTime"));
                        map.put("upvoteCount", jsondata.getString("upvoteCount"));
                        map.put("imgArray", jsondata.getString("imgArray"));
                        list.add(map);
                    }
                }else{
                    Log.e("MFRUN","数据申请异常: ");
                }
            }catch (NullPointerException e){
                Log.e("MFRUN","数据长度为空");
            }
            catch(JSONException e){
                Log.e("MFRUN", "数据获取异常: " + e.getMessage());
            }

        return list;
    }
    public static ArrayList<HashMap<String,Object>> getData(List<NameValuePair> params,String url) {

            return HttpHelper.Analysis(HttpHelper.doPost(params, url));
    }
    /*
    *网络图像获取函数
    */
    public static Bitmap getImage(String path){
        Bitmap bitmap=null;
        try {
            URL url = new URL(path);
            //生成HTTP连接对象
            HttpURLConnection conn = (HttpURLConnection) url.openConnection();
            conn.setConnectTimeout(5000);
            conn.setRequestMethod("POST");
            if (conn.getResponseCode() == 200) {
                InputStream inputStream = conn.getInputStream();
                bitmap = BitmapFactory.decodeStream(inputStream);
            } else {
                Log.e("MFRUN", "图像获取失败");
            }
        }catch(IOException e){
            //Todo
        }catch(Exception e){
            //Todo
        }
       return bitmap;
    }
}
