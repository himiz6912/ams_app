package jp.hmproject.ams_app;

import android.app.AlertDialog;
import android.app.TimePickerDialog;
import android.content.ComponentName;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.ServiceConnection;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.os.Handler;
import android.os.IBinder;
import android.os.Message;
import android.os.RemoteException;
import android.support.design.widget.FloatingActionButton;
import android.support.design.widget.Snackbar;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.KeyEvent;
import android.view.View;
import android.view.Menu;
import android.view.MenuItem;
import android.widget.Button;
import android.widget.TimePicker;

import java.util.Calendar;

public class MainActivity extends AppCompatActivity {
    private static String TAG = "MainActivity";
    private Intent AMS_ServiceIntent;
    private AMS_Remote ams_remote;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        FloatingActionButton fab = (FloatingActionButton) findViewById(R.id.fab);
        fab.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Snackbar.make(view, "Replace with your own action", Snackbar.LENGTH_LONG)
                        .setAction("Action", null).show();
            }
        });

        final Button start_bt = (Button)findViewById(R.id.start);
        start_bt.setOnClickListener((new View.OnClickListener(){

            Calendar cal = Calendar.getInstance();
            int hour = cal.get(Calendar.HOUR_OF_DAY);
            int minute = cal.get(Calendar.MINUTE);
            @Override
            public void onClick(View view) {
                TimePickerDialog tpd = new TimePickerDialog(
                        getApplicationContext(), new TimePickerDialog.OnTimeSetListener() {
                    @Override
                    public void onTimeSet(TimePicker timePicker, int i, int i1) {
                        start_bt.setText(Integer.toString(i) + ":" + Integer.toString(i1));
                    }
                },hour,minute,false);
            }
        }));
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

    protected void onResume(){
        super.onResume();
        bindAMSService();
    }
    public void onPause(){
        super.onPause();
    }
    protected void onDestroy(){
        super.onDestroy();
        try {
            ams_remote.unregisterCallback(callback);
        }catch(RemoteException e){
            Log.e(TAG,"onDestroy:" + e.getMessage());
        }
        unbindService(connection);
    }

    private void endApp(){
        AlertDialog.Builder ab = new AlertDialog.Builder(this);
        ab.setTitle(getString(R.string.confirm_finish_title));
        ab.setMessage(getString(R.string.confirm_finish_message));
        ab.setPositiveButton("OK", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                doCommand("END");
            }
        });
        ab.setNegativeButton("No", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
            }
        });
        ab.create().show();
    }

    private Handler handler = new Handler(){
        public void handleMessage(Message msg){
            doCallbackTask((String) msg.obj);
        }
    };
    private AMS_Callback callback = new AMS_Callback.Stub(){

        @Override
        public void basicTypes(String msg) throws RemoteException {
            handler.sendMessage(Message.obtain(handler,0,msg));
        }
    };
    private ServiceConnection connection = new ServiceConnection() {
        @Override
        public void onServiceConnected(ComponentName name, IBinder service) {
            ams_remote = AMS_Remote.Stub.asInterface(service);
            try{
                ams_remote.registerCallback(callback);
            }catch(RemoteException e){
                Log.e(TAG,"ServiceConnection:" + e.getMessage());
            }
            if(ams_remote != null){
                doCommand("INIT");
            }
        }
        @Override
        public void onServiceDisconnected(ComponentName componentName) {
            ams_remote = null;
        }
    };
    private void startAMSService(){
        startService(AMS_ServiceIntent);
    }
    private void bindAMSService(){
        Intent intent = new Intent(AMS_Remote.class.getName());
        if(bindService(intent,connection,Context.BIND_AUTO_CREATE) != true){
            endApp();
        }
    }
    public void doCommand(String msg){
        try{
            ams_remote.command(msg);
        }catch(RemoteException e){
            Log.e(TAG,"doCommand:" + e.getMessage());
        }
    }
    private void doCallbackTask(String msg){
        if (msg.substring(0, 7).equals("status:") && !msg.equals("status:")) {
            String[] msgArray = msg.split(":");
            String status = msgArray[1];
            if (status.equals("INIT")) {

            }else if(msg.equals("END")) {
                finish();
            }
        }
    }

    @Override
    public boolean dispatchKeyEvent(KeyEvent event) {
        if(event.getAction() == KeyEvent.ACTION_DOWN){
            if(event.getKeyCode() == KeyEvent.KEYCODE_BACK){
                endApp();
                return true;
            }
        }
        return super.dispatchKeyEvent(event);
    }
}
