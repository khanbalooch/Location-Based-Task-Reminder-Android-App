package first.modyt.Broadcost;


import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.BroadcastReceiver;
import android.content.ContentResolver;
import android.content.Context;
import android.content.Intent;
import android.content.res.Resources;
import android.database.Cursor;
import android.net.Uri;
import android.os.Bundle;
import android.provider.ContactsContract;
import android.support.v4.app.NotificationCompat;
import android.support.v4.app.TaskStackBuilder;
import android.support.v4.content.ContextCompat;
import android.telephony.SmsMessage;
import android.util.Log;

import first.first.R;
import first.modyt.AddTaskActivity;
import first.modyt.MainActivity;
import first.modyt.data.Actions;


public class smsReceiver extends BroadcastReceiver {

    private static int mId=100;
    private Context context;
    private Intent intent;

    public String getContactDisplayNameByNumber(String number, Context context) {
        Uri uri = Uri.withAppendedPath(ContactsContract.PhoneLookup.CONTENT_FILTER_URI, Uri.encode(number));
        String name = "Incoming call from";

        ContentResolver contentResolver = context.getContentResolver();
        Cursor contactLookup = contentResolver.query(uri, null, null, null, null);

        try {
            if (contactLookup != null && contactLookup.getCount() > 0) {
                contactLookup.moveToNext();
                name = contactLookup.getString(contactLookup.getColumnIndex(ContactsContract.Data.DISPLAY_NAME));
                // this.id =
                // contactLookup.getString(contactLookup.getColumnIndex(ContactsContract.Data.CONTACT_ID));
                // String contactId =
                // contactLookup.getString(contactLookup.getColumnIndex(BaseColumns._ID));
            } else {
                name = "Unknown number";
            }
        } finally {
            if (contactLookup != null) {
                contactLookup.close();
            }
        }

        return name;
    }

    private NotificationCompat.Action ignoreReminderAction(Context context, Intent intent) {
        //Create an Intent to launch WaterReminderIntentService
        Intent ignoreReminderIntent = new
                Intent(context, BackendService.class);
        ignoreReminderIntent.putExtra("mId", intent.getIntExtra("mId", 0));


        /*int notifyId = intent.getIntExtra("mId",-1);
        String  s = Context.NOTIFICATION_SERVICE;
        NotificationManager mNM = (NotificationManager) context.getSystemService(s);
        mNM.cancel(notifyId);*/
        //Set the action of the intent to designate you want to dismiss the notification
        ignoreReminderIntent.setAction(Actions.IGNORE_TASK);
        //Create a PendingIntent from the intent to launch WaterReminderIntentService
        PendingIntent ignoreReminderPendingIntent =
                PendingIntent.getService(
                        context,
                        mId,
                        ignoreReminderIntent,
                        PendingIntent.FLAG_UPDATE_CURRENT);
        //Create an Action for the user to ignore the notification (and dismiss it)
        NotificationCompat.Action ignoreReminderAction = new
                NotificationCompat.Action(android.R.drawable.ic_notification_clear_all,
                "Ignore",
                ignoreReminderPendingIntent);
        return ignoreReminderAction;
    }

    private NotificationCompat.Action sayOkToTask(Context context, Intent intent) {
        //Create an Intent to launch WaterReminderIntentService
        Intent doTaskIntent = new
                Intent(context, BackendService.class);
        doTaskIntent.putExtra("mId",intent.getIntExtra("mId",0));
        doTaskIntent.putExtra("userName", intent.getStringExtra("userName"));
        doTaskIntent.putExtra("message", intent.getStringExtra("message"));
        doTaskIntent.putExtra("userNumber",intent.getStringExtra("userNumber"));


        //Set the action of the intent to designate you want to dismiss the notification
        doTaskIntent.setAction(Actions.DO_TASK);
        //Create a PendingIntent from the intent to launch WaterReminderIntentService
        PendingIntent doTaskPendingIntent =
                PendingIntent.getService(
                        context,
                        mId,
                        doTaskIntent,
                        PendingIntent.FLAG_UPDATE_CURRENT);
        //Create an Action for the user to ignore the notification (and dismiss it)
        NotificationCompat.Action ignoreReminderAction = new
                NotificationCompat.Action(android.R.drawable.ic_input_add,
                "Ok, I will",
                doTaskPendingIntent);
        return ignoreReminderAction;
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

    public void sendNotification(Context context, Intent intent) {
        Log.i("cs.fsu", "smsReceiver: SMS Received");

        Bundle bundle = intent.getExtras();
        if (bundle != null) {
            Object[] pdus = (Object[]) bundle.get("pdus");
            SmsMessage[] msgs = new SmsMessage[pdus.length];
            for (int i = 0; i < msgs.length; i++) {
                msgs[i] = SmsMessage.createFromPdu((byte[]) pdus[i]);

                final String sender = msgs[i].getOriginatingAddress();
                final String message = msgs[i].getMessageBody().toString();
                final String userName = getContactDisplayNameByNumber(sender, context.getApplicationContext());
                final int notification_ID = mId;
                intent.putExtra("mId", notification_ID);
                intent.putExtra("userName", userName);
                intent.putExtra("message", message);
                intent.putExtra("userNumber",sender);

                NotificationCompat.Builder mBuilder =
                        new NotificationCompat.Builder(context.getApplicationContext())
                                .setColor(ContextCompat.getColor(context, R.color.colorPrimary))
                                .setSmallIcon(R.drawable.notification_icon)
                                .setContentTitle(userName)
                                .setContentText("Swipe down to open")
                                .setContentIntent(contentIntent(context))
                                .addAction(sayOkToTask(context, intent))
                                .addAction(ignoreReminderAction(context, intent))
                                .setNumber(mId)
                                .setAutoCancel(true)
                                .setStyle(getInboxText(new String[]{userName, sender, message}));

                NotificationManager mNotificationManager =
                        (NotificationManager) context.getSystemService(Context.NOTIFICATION_SERVICE);
                // mId allows you to update the notification later on.
                mNotificationManager.notify(mId++, mBuilder.build());


            }
        }
    }


    private String getRequiredPriorityString(String priority) {

        switch (priority) {
            case "1":
                return "HIGH";

            case "2":
                return "MEDIUM";

            case "3":
                return "LOW";


        }
        return "no Priority";
    }

    NotificationCompat.InboxStyle getInboxText(String[] taskDescription) {
        NotificationCompat.InboxStyle inboxStyle =
                new NotificationCompat.InboxStyle();


        TaskDetail taskDetail = new TaskDetail(taskDescription);

        // Sets a title for the Inbox in expanded layout
        inboxStyle.setBigContentTitle("Task Details : ");

        inboxStyle.addLine("TASK : " + taskDetail.getTask());
        inboxStyle.addLine("FROM : " + taskDetail.getUserName());
        inboxStyle.addLine(("Location : " + taskDetail.getLocationName()));
        inboxStyle.addLine("Expiry : "+taskDetail.getExpiryTimeDate());
        inboxStyle.addLine("With  " +getRequiredPriorityString(taskDetail.getPriority())+" Priority");

        // Moves events into the expanded layout
        /*for (int i=0; i < taskDetail.length; i++) {

            inboxStyle.addLine(taskDetail[i]);
        }*/
        // Moves the expanded layout object into the notification object.
        return inboxStyle;


    }

    @Override
    public void onReceive(Context context, Intent intent) {


        this.intent = intent;
        this.context = context;
        sendNotification(smsReceiver.this.context, smsReceiver.this.intent);


    }
}