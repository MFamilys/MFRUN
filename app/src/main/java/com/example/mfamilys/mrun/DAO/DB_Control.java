package com.example.mfamilys.mrun.DAO;

import android.content.Context;
import android.database.SQLException;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;
import android.util.Log;

import java.io.File;
import java.util.logging.Logger;

/**
 * Created by mfamilys on 15-8-23.
 */
public class DB_Control {
    public static final int MODE_READ_ONLY=0x001;
    public static final int MODE_READ_WRITER=0x000;
    public static final String DB_Name="meta";
    protected SQLiteDatabase database;
    protected DB_OpenHelper db_openHelper;
    protected Context context;
    protected String name;
    protected int mode;
    private DB_Meta meta;

    public DB_Control(Context context){
        this.context=context;
    }
    public DB_Control(Context context,String name){
        this.context=context;
        this.name=name;
        open(name,MODE_READ_ONLY);
    }
    public DB_Control(Context context,String name,int mode){
        this.context=context;
        this.name=name;
        this.mode=mode;
        this.open(name,mode);
    }
    public String getName(){
        return name;
    }
    public void open(String name,int mode){
        //防止重新打开数据库
        if(db_openHelper!=null)
        {
            this.close();
        }
        this.name=name;
        this.mode=mode;
        this.db_openHelper=new DB_OpenHelper(context,this.name);
        this.getDB(this.mode);
        this.meta=new DB_Meta(this);

    }
    public void getDB(int mode){
        switch(mode){
            case MODE_READ_ONLY:
                this.database=db_openHelper.getReadableDatabase();
                break;
            case MODE_READ_WRITER:
                this.database=db_openHelper.getWritableDatabase();
                break;
        }
    }
    public boolean delete(){
        if(db_openHelper!=null){
            close();
        }
        File file= new File(name);
        return (file!=null)&&file.delete();
    }
    public boolean exists(){
        File file= new File(name);
        return (file!=null)&&file.exists();
    }
    public void close(){
        if(db_openHelper!=null){
            db_openHelper.close();
            db_openHelper=null;
        }
    }
    public DB_Meta getMeta(){
        return  meta;
    }


    //数据库创建助手
    protected class DB_OpenHelper extends SQLiteOpenHelper{
        protected static final int version =1;
        protected static final String SQL_CREATE_TABLE="Create table "+DB_Name+"("
                +"id integer primary key autoincrement,"
                +"meta varchar(255) not null unique,"
                +"value varchar(255) default null"
                + ");";
        DB_OpenHelper(Context context,String name){
            super(context,name,null,version);
        }
        @Override
        public void onCreate(SQLiteDatabase sqLiteDatabase) {
            try{
                sqLiteDatabase.execSQL(SQL_CREATE_TABLE);
            }catch(SQLException e){
                Log.w("MRUN",e.getMessage());
            }
        }

        @Override
        public void onUpgrade(SQLiteDatabase sqLiteDatabase, int i, int i1) {

        }
    }
}
