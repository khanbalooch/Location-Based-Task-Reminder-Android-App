package first.modyt.Broadcost;

import android.app.Service;
import android.content.Intent;
import android.os.IBinder;
import android.util.Log;
import android.widget.Toast;

import static android.content.ContentValues.TAG;

public class SmsService extends Service {


    @Override
    public void onCreate()
    {
        // Handler will get associated with the current thread,
        // which is the main thread.
        super.onCreate();


    }

    @Override
    public IBinder onBind(Intent arg0)
    {
        // TODO Auto-generated method stub

        return null;
    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId)
    {
        Log.i(TAG, "onStartCommand");
        Toast.makeText(this, "onStartCommand", Toast.LENGTH_LONG).show();

        return START_STICKY;
    }

    //launch when its closed
    @Override
    public void onDestroy()
    {
        super.onDestroy();
        Toast.makeText(this, "YouWillNeverKillMe TOAST!!", Toast.LENGTH_LONG).show();
        sendBroadcast(new Intent("YouWillNeverKillMe"));
      //startSmsService();

    }

}