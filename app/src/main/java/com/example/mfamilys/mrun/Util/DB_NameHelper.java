package com.example.mfamilys.mrun.Util;

import android.content.Context;
import android.content.SharedPreferences;
import android.location.Location;
import android.os.Environment;
import android.preference.Preference;
import android.preference.PreferenceManager;

import com.example.mfamilys.mrun.DAO.DB_Control;

import java.io.File;
import java.io.FilenameFilter;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Comparator;
import java.util.Date;

/**
 * Created by mfamilys on 15-8-24.
 */
    //文件名助手
    public class DB_NameHelper {
        private Context context;
        //配置信息格式数据
        private SharedPreferences sharedPreferences;
        //最后打开的文件名
        private static final String LAST_OPENED_ARCHIVE_FILE_NAME = "lastOpenedArchiveFileName";
        private SharedPreferences.Editor editor;
        //数据库文件名后缀
        public static final String SQLITE_DATABASE_FILENAME_EXT = ".sqlite";
        //保存的外部目录
        public static final String SAVED_EXTERNAL_DIRECTORY = "MFRUN";
        //按照每个月分组格式
        public static final String GROUP_BY_EACH_MONTH = "yyyyMM";
        //按照每天分组格式　
        public static final String GROUP_BY_EACH_DAY = "yyyyMMdd";
        //文件名分组格式
        public static final int RECORD_BY_DAY=0x001;
        public static final int RECORD_BY_TIMES=0x002;

        public DB_NameHelper(Context context) {
            this.context = context;
            this.sharedPreferences = PreferenceManager.getDefaultSharedPreferences(context);
            this.editor = sharedPreferences.edit();
        }
        //判断是否存在外部存储卡
        public static boolean isExternalStoragePresent() {
        //判断是否存在外部存储卡并正确挂载
            return Environment.getExternalStorageState().equals(Environment.MEDIA_MOUNTED);
        }
        //获取外部内存卡路径
        public static File getExternalStoragePath() {
            if (isExternalStoragePresent()) {
                return Environment.getExternalStorageDirectory();
            }
            return null;
        }
        //获取存储路径
        public static File getStorageDirectory(Date date) {
            //保存路径为："外部存储路径/tracker/yyyymm"
            String saveDirectory = getExternalStoragePath() + File.separator + SAVED_EXTERNAL_DIRECTORY
                    + File.separator + new SimpleDateFormat(GROUP_BY_EACH_MONTH).format(date);

            // 如果保存目录不存在，则自动创建
            File saveDirectoryFile = new File(saveDirectory);
            if (!saveDirectoryFile.isDirectory()) {
                saveDirectoryFile.mkdirs();
            }

            return saveDirectoryFile;
        }
        //获取当前的外部存储路径
        public static File getCurrentStorageDirectory() {
            return getStorageDirectory(new Date());
        }

        //按月获取文件名
        public ArrayList<String> getArchiveFilesNameByMonth(Date date) {
            ArrayList<String> result = new ArrayList<String>();

            File storageDirectory = getStorageDirectory(date);
            File[] DBFiles = storageDirectory.listFiles(new FilenameFilter() {
                @Override
                public boolean accept(File file, String s) {
                    //文件名过滤，通过数据库后缀
                    return s.endsWith(SQLITE_DATABASE_FILENAME_EXT);
                }
            });

            if (DBFiles != null) {
                /**
                 * 第一次记录的时候排序(sort的原理：基本数据快速排序，对象数据堆排序)
                 */
                Arrays.sort(DBFiles, new Comparator<File>() {
                    public int compare(File f1, File f2) {
                        try {
                            DB_Control db_control1 = new DB_Control(context, f1.getAbsolutePath(), DB_Control.MODE_READ_ONLY);
                            DB_Control db_control2 = new DB_Control(context, f2.getAbsolutePath(), DB_Control.MODE_READ_ONLY);
                            Long time1=db_control1.getMeta().getStartTime().getTime();
                            Long time2=db_control2.getMeta().getStartTime().getTime();
                            db_control1.close();
                            db_control2.close();
                          //对象比较．将time2的值与time1比较，返回1,0,-1
                            return Long.valueOf(time2).compareTo(time1);
                        } catch (NullPointerException e) {
                            return 0;
                        }
                    }
                });

                for (int i = 0; i <DBFiles.length; i++) {
                    result.add((DBFiles[i]).getAbsolutePath());
                }
            }

            return result;
        }
        //获取直到当前月份的数据
        public ArrayList<String> getArchiveFilesFormCurrentMonth() {
            return getArchiveFilesNameByMonth(new Date());
        }
        //设置当前日期的名字
        public String getNewName() {
            String RECORD_BY = sharedPreferences.getString("RECORD_BY", "RECORD_BY_TIMES");
            String databaseFileName = System.currentTimeMillis() + SQLITE_DATABASE_FILENAME_EXT;
            if (RECORD_BY.equals("RECORD_BY_DAY")) {
                databaseFileName = (new SimpleDateFormat(GROUP_BY_EACH_DAY).format(new Date())) + SQLITE_DATABASE_FILENAME_EXT;
            }

            File databaseFile = new File(getCurrentStorageDirectory().getAbsolutePath() + File.separator + databaseFileName);
            return databaseFile.getAbsolutePath();
        }


        /**
         * 获得已经存在过的未清理的文件
         *
         * @return
         */
        public String getResumeName() {
            //判断是否存在最后打开文件名字的键值对
            if (sharedPreferences.contains(LAST_OPENED_ARCHIVE_FILE_NAME)) {
                //是则返回名字
                return sharedPreferences.getString(LAST_OPENED_ARCHIVE_FILE_NAME, "");
            }

            return null;
        }
        //清理已存在过的文件
        public boolean clearLastOpenedName() {
            if (sharedPreferences.contains(LAST_OPENED_ARCHIVE_FILE_NAME)) {
                //如果配置文件中存在文件，则清除
                editor.remove(LAST_OPENED_ARCHIVE_FILE_NAME);
                return editor.commit();
            }

            return false;
        }
        //设置最后打开的文件
        public boolean setLastOpenedName(String name) {
            editor.putString(LAST_OPENED_ARCHIVE_FILE_NAME, name);
            return editor.commit();
        }
        //设置文件名的格式
        public boolean setFilesRecordBy(int RecordBy){
           switch (RecordBy){
            case RECORD_BY_DAY:
              editor.putString("RECORD_BY","RECORD_BY_DAY");
              break;
            case RECORD_BY_TIMES:
              editor.putString("RECORD_BY","RECORD_BY_TIMES");
              break;
        }
          return   editor.commit();
        }
        //若存在可重写的文件，返回是．
        public boolean hasResumeName() {
            String resumeArchiveFileName = getResumeName();
            if (resumeArchiveFileName != null) {
                File resumeFile = new File(resumeArchiveFileName);
                return (resumeFile.exists() && resumeFile.isFile() && resumeFile.canWrite()) ? true : false;
            } else {
                return false;
            }
        }
    }
