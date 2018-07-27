package first.modyt.AlarmServices;

import android.Manifest;
import android.app.IntentService;
import android.app.Notification;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.database.Cursor;
import android.location.Location;
import android.location.LocationListener;
import android.location.LocationManager;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.app.ActivityCompat;
import android.support.v4.app.NotificationCompat;
import android.support.v4.app.TaskStackBuilder;
import android.util.Log;

import java.util.List;

import first.first.R;
import first.modyt.AddTaskActivity;
import first.modyt.Broadcost.BackendService;
import first.modyt.Broadcost.TaskDetail;
import first.modyt.MainActivity;
import first.modyt.data.Actions;
import first.modyt.data.TaskContract;

/**
 * Created by Khan on 29-Apr-17.
 */

public class LocationSensor extends IntentService implements LocationListener
{
    private Context mContext;
    protected LocationManager locationManager;
    protected NotificationManager notificationManager;

    boolean isGPSEnabled = false;
    boolean isNetworkEnabled = false;
    private static int mId=1;
    double displacement;

    Notification myNotification;
    Location target, current;


    public LocationSensor()
    {
        super(LocationSensor.class.getName());

        this.mContext = LocationSensor.this;
        target = new Location("Target Location");
        current = new Location("Current Location");
        displacement = 2000.00;


    }

    @Override
    protected void onHandleIntent(@Nullable Intent intent)
    {
        current = getLocation();

        Cursor tasks =  getContentResolver().query(TaskContract.TaskEntry.CONTENT_URI,
                null,
                null,
                null,
                TaskContract.TaskEntry.COLUMN_PRIORITY);

        tasks.moveToFirst();

        for (int i = 0; i<tasks.getCount(); i++) {
            String userArray[] = tasks.getString(tasks.getColumnIndex(TaskContract.TaskEntry.COLUMN_USERNAME)).split(",");
            String taskDescription = tasks.getString(tasks.getColumnIndex(TaskContract.TaskEntry.COLUMN_DESCRIPTION));
            String targetLocationArray[] = tasks.getString(tasks.getColumnIndex(TaskContract.TaskEntry.COLUMN_LOCATION)).split(",");
            String taskLatituteLongitudeArray[] = targetLocationArray[1].split("\\*");
            double lat = Double.parseDouble(taskLatituteLongitudeArray[0]);
            double lon = Double.parseDouble(taskLatituteLongitudeArray[1]);

            target.setLatitude(lat);
            target.setLongitude(lon);

            displacement = current.distanceTo(target);

            String newMessage [] = {taskDescription,userArray[0],targetLocationArray[0]};


            if(displacement < 1000.00)
            {
                final int notification_ID = mId;
                intent.putExtra("mId", notification_ID);

                myNotification = new NotificationCompat.Builder(getApplicationContext())
                        .setContentTitle(userArray[0])
                        .setContentText("Task Due in current Location")
                        //.setTicker("dist: " + l.distanceTo(target))
                        .setWhen(System.currentTimeMillis())
                        .setDefaults(Notification.DEFAULT_SOUND)
                        .addAction(taskDone(getApplicationContext(),intent))
                        .addAction(remindLatorTask(getApplicationContext(),intent))
                        .addAction(skipTask(getApplicationContext(),intent))
                        .setContentIntent(contentIntent(getApplicationContext()))
                        .setNumber(mId)
                        .setAutoCancel(true)
                        .setSmallIcon(R.drawable.notification_icon)
                        .setStyle(getInboxText(newMessage))
                        .build();

                notificationManager.notify(mId, myNotification);
            }
        tasks.moveToNext();
        }
    }
    private NotificationCompat.InboxStyle getInboxText(String[] taskDescription)
    {
        NotificationCompat.InboxStyle inboxStyle =
                new NotificationCompat.InboxStyle();


        //TaskDetail taskDetail = new TaskDetail(taskDescription);

        // Sets a title for the Inbox in expanded layout
        inboxStyle.setBigContentTitle("Task Location Reached");

        inboxStyle.addLine("Do this you are on location");
        inboxStyle.addLine("TASK : " + taskDescription[0]);
        inboxStyle.addLine("FROM : " + taskDescription[1]);
        inboxStyle.addLine(("Location : " + taskDescription[2]));

        // Moves events into the expanded layout
        /*for (int i=0; i < taskDetail.length; i++) {

            inboxStyle.addLine(taskDetail[i]);
        }*/
        // Moves the expanded layout object into the notification object.
        return inboxStyle;


    }
    private PendingIntent contentIntent(Context context) {
        Intent resultIntent = new Intent(context, AddTaskActivity.class);
        resultIntent.setAction(Actions.NOTIFICATION_TO_ADD_TASK);
        // The stack builder object will contain an artificial back stack for the
        // started Activity.
        // This ensures that navigating backward from the Activity leads out of
        // your application to the Home screen.
        TaskStackBuilder stackBuilder = TaskStackBuilder.create(context);
        // Adds the back stack for the Intent (but not the Intent itself)
        stackBuilder.addParentStack(MainActivity.class);
        // Adds the Intent that starts the Activity to the top of the stack
        stackBuilder.addNextIntent(resultIntent);
        PendingIntent resultPendingIntent =
                stackBuilder.getPendingIntent(
                        mId,
                        PendingIntent.FLAG_UPDATE_CURRENT
                );
        /*PendingIntent resultPendingIntent =
                PendingIntent.getActivity(context,mId,resultIntent,PendingIntent.FLAG_UPDATE_CURRENT);*/
        return resultPendingIntent;
    }

    public Location getLocation()
    {
        Location location = new Location("CurrentLocation");
        locationManager = (LocationManager) mContext.getSystemService(LOCATION_SERVICE);
        notificationManager = (NotificationManager) mContext.getSystemService(NOTIFICATION_SERVICE);
        isGPSEnabled = locationManager.isProviderEnabled(LocationManager.GPS_PROVIDER);
        isNetworkEnabled = locationManager.isProviderEnabled(LocationManager.NETWORK_PROVIDER);
        String provider = isGPSEnabled == true ? LocationManager.GPS_PROVIDER : LocationManager.NETWORK_PROVIDER;

        switch (provider)
        {
            case LocationManager.GPS_PROVIDER:

                //Log.d("GPS Enabled", "GPS Enabled");

                if ( ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) == PackageManager.PERMISSION_GRANTED)
                {
                    locationManager.requestLocationUpdates(LocationManager.GPS_PROVIDER, 0, 0, (LocationListener) this);

                    if (locationManager != null) {
                        location = locationManager.getLastKnownLocation(LocationManager.GPS_PROVIDER);
                    }
                } else
                {
                    //request permissions
                }
                break;
            case LocationManager.NETWORK_PROVIDER:

                //Log.d("Network Enabled", "Network Enabled");


                if ( ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) == PackageManager.PERMISSION_GRANTED &&
                        ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_COARSE_LOCATION) == PackageManager.PERMISSION_GRANTED)
                {
                    locationManager.requestLocationUpdates(LocationManager.NETWORK_PROVIDER, 0, 0, (LocationListener) this);

                    if (locationManager != null) {
                        location = locationManager.getLastKnownLocation(LocationManager.NETWORK_PROVIDER);
                    }
                } else
                {
                    //request permissions
                }

                break;
        }

        return location;
    }
    @Override
    public void onLocationChanged(Location location)
    {

    }

    @Override
    public void onStatusChanged(String provider, int status, Bundle extras)
    {

    }

    @Override
    public void onProviderEnabled(String provider)
    {

    }

    @Override
    public void onProviderDisabled(String provider)
    {

    }
    private NotificationCompat.Action skipTask(Context context, Intent intent) {
        //Create an Intent to launch WaterReminderIntentService
        Intent ignoreReminderIntent = new
                Intent(context, BackendService.class);
        ignoreReminderIntent.putExtra("mId", intent.getIntExtra("mId", 0));


        /*int notifyId = intent.getIntExtra("mId",-1);
        String  s = Context.NOTIFICATION_SERVICE;
        NotificationManager mNM = (NotificationManager) context.getSystemService(s);
        mNM.cancel(notifyId);*/
        //Set the action of the intent to designate you want to dismiss the notification
        ignoreReminderIntent.setAction(Actions.NOTIFICATION_SKIP_TASK);
        //Create a PendingIntent from the intent to launch WaterReminderIntentService
        PendingIntent ignoreReminderPendingIntent =
                PendingIntent.getService(
                        context,
                        1,
                        ignoreReminderIntent,
                        PendingIntent.FLAG_UPDATE_CURRENT);
        //Create an Action for the user to ignore the notification (and dismiss it)
        NotificationCompat.Action ignoreReminderAction = new
                NotificationCompat.Action(android.R.drawable.ic_notification_clear_all,
                "Skip",
                ignoreReminderPendingIntent);
        return ignoreReminderAction;
    }
    private NotificationCompat.Action remindLatorTask(Context context, Intent intent) {
        //Create an Intent to launch WaterReminderIntentService
        Intent ignoreReminderIntent = new
                Intent(context, BackendService.class);
        ignoreReminderIntent.putExtra("mId", intent.getIntExtra("mId", 0));


        /*int notifyId = intent.getIntExtra("mId",-1);
        String  s = Context.NOTIFICATION_SERVICE;
        NotificationManager mNM = (NotificationManager) context.getSystemService(s);
        mNM.cancel(notifyId);*/
        //Set the action of the intent to designate you want to dismiss the notification
        ignoreReminderIntent.setAction(Actions.NOTIFICATION_REMIND_TASK_LATOR);
        //Create a PendingIntent from the intent to launch WaterReminderIntentService
        PendingIntent ignoreReminderPendingIntent =
                PendingIntent.getService(
                        context,
                        1,
                        ignoreReminderIntent,
                        PendingIntent.FLAG_UPDATE_CURRENT);
        //Create an Action for the user to ignore the notification (and dismiss it)
        NotificationCompat.Action ignoreReminderAction = new
                NotificationCompat.Action(android.R.drawable.ic_popup_reminder,
                "Later",
                ignoreReminderPendingIntent);
        return ignoreReminderAction;
    }

    private NotificationCompat.Action taskDone(Context context, Intent intent) {
        //Create an Intent to launch WaterReminderIntentService
        Intent doTaskIntent = new
                Intent(context, BackendService.class);
        doTaskIntent.putExtra("mId",intent.getIntExtra("mId",0));
        doTaskIntent.putExtra("userName", intent.getStringExtra("userName"));
        doTaskIntent.putExtra("message", intent.getStringExtra("message"));
        doTaskIntent.putExtra("userNumber",intent.getStringExtra("userNumber"));


        //Set the action of the intent to designate you want to dismiss the notification
        doTaskIntent.setAction(Actions.NOTIFICATION_TASK_DONE);
        //Create a PendingIntent from the intent to launch WaterReminderIntentService
        PendingIntent doTaskPendingIntent =
                PendingIntent.getService(
                        context,
                        1,
                        doTaskIntent,
                        PendingIntent.FLAG_UPDATE_CURRENT);
        //Create an Action for the user to ignore the notification (and dismiss it)
        NotificationCompat.Action ignoreReminderAction = new
                NotificationCompat.Action(android.R.drawable.ic_input_add,
                "Done",
                doTaskPendingIntent);
        return ignoreReminderAction;
    }

}
