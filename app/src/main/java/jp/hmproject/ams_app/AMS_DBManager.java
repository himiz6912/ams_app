package jp.hmproject.ams_app;

import android.content.Context;
import android.content.SharedPreferences;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;
import android.preference.PreferenceManager;
import android.util.Log;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * Created by hm on 12/23/2016.
 */
public class AMS_DBManager {
    private SQLiteDatabase mDB;
    private Context context;
    private SharedPreferences sp;
    private dbHelper dh;

    public AMS_DBManager(Context ctx) {
        this.context = ctx;
        sp = PreferenceManager.getDefaultSharedPreferences(context);
        dh = new dbHelper(context);
        mDB = dh.getWritableDatabase();
    }

    public void close(){
        if(dh != null){
            dh.close();
        }
    }

    private static class dbHelper extends SQLiteOpenHelper {
        private static final String DATABASE_NAME = "ams";
        private static final int DATABASE_VERSION = 3;
        dbHelper(Context context){
            super(context,DATABASE_NAME,null,DATABASE_VERSION);
        }

        @Override
        public void onCreate(SQLiteDatabase db){
            db.execSQL(
                    "create table ams_table(" +
                            "id INTEGER PRIMARY KEY AUTOINCREMENT," +
                            "flag INTEGER NOT NULL," +
                            "event INTEGER NOT NULL," +
                            "accuracy REAL," +
                            "generation TEXT NOT NULL," +
                            "registration TEXT," +
                            "user NUMERIC," +
                            "location TEXT" +
                            "system TEXT" +
                            "operation TEXT" +
                            "remarks TEXT" +
                            "twopeople INTEGER" +
                            "dailysubcon INTEGER" +
                            "sending TEXT" +
                            "revise TEXT" +
                            ");"


            );
            db.execSQL("create index ams_index on ams_table(id);");
        }

        @Override
        public void onUpgrade(SQLiteDatabase db,int oldVersion, int newVersion){
            Log.w("AMS_DBManager/dbHelper","onUpgrade:" + oldVersion + " > " + newVersion);
            db.execSQL("drop table if exsists ams_table");
            onCreate(db);
        }
    }

    private String now(){
        SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss", Locale.getDefault());
        return sdf.format(new Date());
    }

    private String[] mSplit(String s){
        String REGEX_CSV_COMMA = ",(?=(([^\"]*\"){2})*[^\"]*$)";
        String REGEX_SURROUND_DOUBLEQUATATION = "^\"|\"$";
        String REGEX_DOUBLEQUOATATION = "\"\"";
        String[] res = null;
        try {
            Pattern cPattern = Pattern.compile(REGEX_CSV_COMMA);
            String[] cols = cPattern.split(s, -1);
            res	= new String[cols.length];
            for(int i=0,len=cols.length;i<len;i++) {
                String col = cols[i].trim();
                Pattern sdqPattern =	Pattern.compile(REGEX_SURROUND_DOUBLEQUATATION);
                Matcher matcher = sdqPattern.matcher(col);
                col = matcher.replaceAll("");
                Pattern dqPattern = Pattern.compile(REGEX_DOUBLEQUOATATION);
                matcher = dqPattern.matcher(col);
                col = matcher.replaceAll("\"");
                res[i] = col;
            }
        }catch(Exception e){
            e.printStackTrace();
        }
        return res;
    }

}
