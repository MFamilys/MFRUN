package com.example.mfamilys.mrun.DAO;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.CursorIndexOutOfBoundsException;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteException;
import android.util.Log;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.SimpleTimeZone;

/**
 * Created by mfamilys on 15-8-24.
 */
//NoSQL键值对数据模型
public class DB_Meta {
    public static final String DESCRIPTION="description";
    public static final String START_TIME="start_time";
    public static final String END_TIME="end_time";
    public static final String DISTANCE="distance";
    public static final String SPEED="speed";
    public static final Double KM_PRE_HOUR_CNT=3.597;
    public static final int TO_KILIMETER=1000;
    public static final String TIME_FORMAT="%02d:%02d:%02d";
    public static final String TABLE_NAME="meta";
    protected DB_Control db_control;
    private SQLiteDatabase sqLiteDatabase;

    public DB_Meta(DB_Control db_control){
        this.db_control=db_control;
        this.sqLiteDatabase=db_control.database;
    }
    protected boolean set(String name,String values){
        ContentValues contentValues=new ContentValues();
        contentValues.put("meta",name);
        contentValues.put("value",values);
        long result=0;
        try{
            if(isExists(name)){
                result=sqLiteDatabase.update(TABLE_NAME,contentValues,"meta='"+name+"'",null);
            }else{
                result=sqLiteDatabase.insert(TABLE_NAME,null,contentValues);
            }
        }catch (Exception e){
           e.printStackTrace();
        }
        return result>0;
    }
    protected String  get(String name) {
        Cursor cursor;
        String result = "";
        try {
            cursor = sqLiteDatabase.rawQuery("SELECT value From "
                    + TABLE_NAME + " where meta ='" + name + "'", null);
            cursor.moveToFirst();
            result = cursor.getString(cursor.getColumnIndex("value"));
            cursor.close();
        } catch (SQLiteException e) {
            Log.w("MFRUN", e.getMessage());
        } catch (CursorIndexOutOfBoundsException e) {
            Log.w("MFRUN", e.getMessage());
        } catch (IllegalStateException e) {
            Log.w("MFRUN", e.getMessage());
        }
        return result;
    }
    //为空时返回默认值
    protected String get(String name,String defaultValue){
        String value=get(name);
        if(value.equals("")&&defaultValue.length()>0){
            return defaultValue;
        }
        return value;
    }


    protected  boolean isExists(String name){
        Cursor cursor;
        int count=0;
        try{
            cursor=sqLiteDatabase.rawQuery("SELECT count(id) AS count" +
                    " From "+TABLE_NAME+" Where meta ='"+name+"'",null);
            cursor.moveToFirst();
            count=cursor.getInt(cursor.getColumnIndex("count"));
            cursor.close();
        }catch (Exception e){
            Log.w("MFUN",e.getMessage());
        }
         return count>0;
    }
    public Date getStartTime(){
        try{
            long startTime=Long.parseLong(get(START_TIME));
            return  new Date(startTime);
        }catch (Exception e){
            return  null;
        }
    }
    public Date getEndTime(){
        try{
            long endtime =Long.parseLong(get(END_TIME));
            return  new Date(endtime);
        }catch (Exception e){
            return null;
        }
    }
    public boolean setStartTime(Date date){
        //long time =date.getTime();
        String stime=new SimpleDateFormat("yyyy/MM/dd HH:mm:ss").format(date);
        return  set(START_TIME,String.valueOf(stime));
    }
    public boolean setEndTime(Date date){
       // long time=date.getTime();
        String etime=new SimpleDateFormat("yyyy/MM/dd HH:mm:ss").format(date);
        return set(END_TIME,String.valueOf(etime));
    }
    public boolean setDescription(String description){
        return set(DESCRIPTION,description);
    }
    public long getCount(){
        Cursor cursor;
        long count=0;
        try{
            cursor=sqLiteDatabase.rawQuery("SELECT count(id) AS count FROM "
            +db_control.DB_Name+" LIMIT 1",null);
            cursor.moveToFirst();
            count=cursor.getInt(cursor.getColumnIndex("count"));
            cursor.close();
        }catch (Exception e){
            Log.w("MFRUN",e.getMessage());
        }
        return count;
    }
    public boolean setRawDistance(double distance){
        return set(DISTANCE,String.valueOf(distance));
    }
    public boolean setCostTime(String time){
        return  set("time",time);
    }
    public double getDistance(){
        return Double.parseDouble(get(DISTANCE,"0.0"));
    }
    public double getSpeed(){
        double rawdistance=Double.parseDouble(get(DISTANCE,"0.0"));
        long costtime=Long.parseLong(get("time","00:00:00"));
        return rawdistance/costtime;
    }

}
