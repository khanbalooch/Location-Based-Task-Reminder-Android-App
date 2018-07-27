package first.modyt.AlarmServices;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;

/**
 * Created by Khan on 30-Apr-17.
 */

public class AlarmReceiver extends BroadcastReceiver
{
    public static final int REQUEST_CODE = 9010;
    public static final String ACTION = "khan.alarmservices.background.LocaationSensor";

    @Override
    public void onReceive(Context context, Intent intent)
    {
        Intent i = new Intent(context, LocationSensor.class);
        i.putExtra("foo", "bar");
        context.startService(i);

    }
}
