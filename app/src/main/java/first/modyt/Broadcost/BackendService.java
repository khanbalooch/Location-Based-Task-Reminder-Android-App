package first.modyt.Broadcost;

import android.app.NotificationManager;
import android.app.Service;
import android.content.ContentValues;
import android.content.Context;
import android.content.Intent;
import android.net.Uri;
import android.os.IBinder;
import android.widget.Toast;

import first.modyt.data.Actions;
import first.modyt.data.TaskContract;

public class BackendService extends Service {
    public BackendService() {
    }

    @Override
    public IBinder onBind(Intent intent) {
        // TODO: Return the communication channel to the service.


        throw new UnsupportedOperationException("Not yet implemented");
    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {

        if(intent.getAction().equals(Actions.IGNORE_TASK)){


            NotificationManager notifManager= (NotificationManager) getApplicationContext().getSystemService(Context.NOTIFICATION_SERVICE);
            notifManager.cancel(intent.getIntExtra("mId",0));

        }
        if(intent.getAction().equals(Actions.DO_TASK)){

            Toast.makeText(getApplicationContext(),"Do Task Part",Toast.LENGTH_LONG);
            String [] description = {intent.getStringExtra("userName"),intent.getStringExtra("userNumber"),intent.getStringExtra("message")};
            TaskDetail taskDetail = new TaskDetail(description);

            if(taskDetail.getTask().length()==0 || taskDetail.getLocation().length()==0 ){

            }

            // Insert new task data via a ContentResolver
            // Create new empty ContentValues object
            ContentValues contentValues = new ContentValues();
            // Put the task description and selected mPriority into the ContentValues
            contentValues.put(TaskContract.TaskEntry.COLUMN_DESCRIPTION,taskDetail.getTask());
            contentValues.put(TaskContract.TaskEntry.COLUMN_PRIORITY, taskDetail.getPriority());
            contentValues.put(TaskContract.TaskEntry.COLUMN_EXPIRY, taskDetail.getExpiryTimeDate());
            contentValues.put(TaskContract.TaskEntry.COLUMN_LOCATION, taskDetail.getLocation());
            contentValues.put(TaskContract.TaskEntry.COLUMN_USERNAME, taskDetail.getUserName()+","+taskDetail.getUserNumber());
            contentValues.put(TaskContract.TaskEntry.COLUMN_IsYourTask, "no");
            contentValues.put(TaskContract.TaskEntry.COLUMN_STATUS, 0);

            // Insert the content values via a ContentResolver
            Uri uri = getContentResolver().insert(TaskContract.TaskEntry.CONTENT_URI, contentValues);

            // Display the URI that's returned with a Toast
            // [Hint] Don't forget to call finish() to return to MainActivity after this insert is complete
            if (uri != null) {
                Toast.makeText(getBaseContext(), uri.toString(), Toast.LENGTH_LONG).show();
            }


            NotificationManager notifManager= (NotificationManager) getApplicationContext().getSystemService(Context.NOTIFICATION_SERVICE);
            notifManager.cancel(intent.getIntExtra("mId",0));


        }
        if(intent.getAction().equals(Actions.NOTIFICATION_REMIND_TASK_LATOR)){


            NotificationManager notifManager= (NotificationManager) getApplicationContext().getSystemService(Context.NOTIFICATION_SERVICE);
            notifManager.cancel(intent.getIntExtra("mId",0));

        }
        if(intent.getAction().equals(Actions.NOTIFICATION_TASK_DONE)){


            NotificationManager notifManager= (NotificationManager) getApplicationContext().getSystemService(Context.NOTIFICATION_SERVICE);
            notifManager.cancel(intent.getIntExtra("mId",0));

        }
        if(intent.getAction().equals(Actions.NOTIFICATION_SKIP_TASK)){


            NotificationManager notifManager= (NotificationManager) getApplicationContext().getSystemService(Context.NOTIFICATION_SERVICE);
            notifManager.cancel(intent.getIntExtra("mId",0));

        }

        return super.onStartCommand(intent, flags, startId);
    }
}
