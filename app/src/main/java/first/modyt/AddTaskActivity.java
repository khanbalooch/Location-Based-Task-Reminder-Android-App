/*
* Copyright (C) 2016 The Android Open Source Project
*
* Licensed under the Apache License, Version 2.0 (the "License");
* you may not use this file except in compliance with the License.
* You may obtain a copy of the License at
*
*      http://www.apache.org/licenses/LICENSE-2.0
*
* Unless required by applicable law or agreed to in writing, software
* distributed under the License is distributed on an "AS IS" BASIS,
* WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
* See the License for the specific language governing permissions and
* limitations under the License.
*/

package first.modyt;

import android.app.DatePickerDialog;
import android.app.Dialog;
import android.app.DialogFragment;
import android.app.TimePickerDialog;
import android.content.ContentValues;
import android.content.Intent;
import android.database.Cursor;
import android.location.Address;
import android.location.Geocoder;
import android.net.Uri;
import android.os.Bundle;
import android.provider.ContactsContract;
import android.support.v7.app.AppCompatActivity;
import android.telephony.SmsManager;
import android.text.format.DateFormat;
import android.util.Log;
import android.view.View;
import android.widget.DatePicker;
import android.widget.EditText;
import android.widget.RadioButton;
import android.widget.TimePicker;
import android.widget.Toast;

import java.util.Calendar;
import java.util.List;
import java.util.Locale;

import first.first.R;
import first.modyt.data.Actions;
import first.modyt.data.TaskContract;


public class AddTaskActivity extends AppCompatActivity {

    // Declare a member variable to keep track of a task's selected mPriority
    private int mPriority;
    private String currentUserName;
    private String currentUserNumber;
    private String taskDetails;
    private String locationName;
    private String locationAttributes;
    private String expiryTimeDate;
    private int taskStatus;
    private static String date;



    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_add_task);

        // Initialize to highest mPriority by default (mPriority = 1)
        ((RadioButton) findViewById(R.id.radButton1)).setChecked(true);
        mPriority = 1;

        chooseRequiredContact();
        showTimeDatePickerDialog();
        chooseLocation();


    }


    public void showTimeDatePickerDialog() {

        ((EditText) findViewById(R.id.editTextExpiry)).setOnClickListener(new EditText.OnClickListener() {
            @Override
            public void onClick(View v) {
                DialogFragment newFragment = new TimePickerFragment();
                newFragment.show(getFragmentManager(), "timePicker");

                DialogFragment dateFragment = new DatePickerFragment();
                dateFragment.show(getFragmentManager(), "datePicker");


            }
        });
    }

    public void chooseRequiredContact() {
        ((EditText) findViewById(R.id.editTextUserName)).setOnClickListener(new EditText.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent1 = new Intent(Intent.ACTION_PICK);
                intent1.setType(ContactsContract.CommonDataKinds.Phone.CONTENT_TYPE);
                startActivityForResult(intent1, Actions.RESULT_CODE_FOR_CONTRACT);

            }
        });


    }
    public void chooseLocation (){
        ((EditText)findViewById(R.id.editTextLocation)).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(AddTaskActivity.this,AddTaskMapsActivity.class);
                startActivityForResult(intent, Actions.RESULT_CODE_FOR_LOCATION);
            }
        });
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        // TODO Auto-generated method stub
        super.onActivityResult(requestCode, resultCode, data);

        if (requestCode == Actions.RESULT_CODE_FOR_LOCATION) {
            if(resultCode == RESULT_OK) {
                double lat=data.getDoubleExtra("lat",12.04);
                double lng=data.getDoubleExtra("lng",12.04);
                locationName= getCompleteAddressString(lat,lng);
                locationAttributes=lat+"*"+lng;
                ((EditText)findViewById(R.id.editTextLocation)).setText(locationAttributes);
            }
        }

        if (requestCode == Actions.RESULT_CODE_FOR_CONTRACT) {
            if(resultCode == RESULT_OK) {
                Uri contactUri = data.getData();

                String[] projection = {ContactsContract.CommonDataKinds.Phone.NUMBER, ContactsContract.CommonDataKinds.Phone.DISPLAY_NAME};


                Cursor cursor = getContentResolver()
                        .query(contactUri, projection, null, null, null);
                cursor.moveToFirst();


                int numberColumn = cursor.getColumnIndex(ContactsContract.CommonDataKinds.Phone.NUMBER);
                int nameColumn = cursor.getColumnIndex(ContactsContract.CommonDataKinds.Phone.DISPLAY_NAME);
                String number = cursor.getString(numberColumn);
                String name = cursor.getString(nameColumn);                //contactName.setText(name);
                currentUserName = name;
                currentUserNumber = number;
                ((EditText) findViewById(R.id.editTextUserName)).setText(currentUserName);
                //contactEmail.setText(email);

            }
        }



    }


    public void onClickAddTask(View view) {
        // Not yet implemented
        // Check if EditText is empty, if not retrieve input and store it in a ContentValues object
        // If the EditText input is empty -> don't create an entry
        taskDetails= ((EditText) findViewById(R.id.editTextTaskDescription)).getText().toString();
        expiryTimeDate= ((EditText) findViewById(R.id.editTextExpiry)).getText().toString();
        taskStatus=0;
        // if task,locaion or name is invalid or null then return
        if (taskDetails.length() == 0 || currentUserName.length()==0 || locationAttributes.length() ==0) {
            return;
        }



        // Insert new task data via a ContentResolver
        // Create new empty ContentValues object
        ContentValues contentValues = new ContentValues();
        // Put the task description and selected mPriority into the ContentValues
        contentValues.put(TaskContract.TaskEntry.COLUMN_DESCRIPTION, taskDetails);
        contentValues.put(TaskContract.TaskEntry.COLUMN_PRIORITY, mPriority);
        contentValues.put(TaskContract.TaskEntry.COLUMN_EXPIRY, expiryTimeDate);
        contentValues.put(TaskContract.TaskEntry.COLUMN_LOCATION, locationName+","+locationAttributes);
        contentValues.put(TaskContract.TaskEntry.COLUMN_USERNAME, currentUserName+","+currentUserNumber);
        contentValues.put(TaskContract.TaskEntry.COLUMN_IsYourTask, "yes");
        contentValues.put(TaskContract.TaskEntry.COLUMN_STATUS, taskStatus);

        // Insert the content values via a ContentResolver
        Uri uri = getContentResolver().insert(TaskContract.TaskEntry.CONTENT_URI, contentValues);

        // Display the URI that's returned with a Toast
        // [Hint] Don't forget to call finish() to return to MainActivity after this insert is complete
        if (uri != null) {
            Toast.makeText(getBaseContext(), uri.toString(), Toast.LENGTH_LONG).show();
        }

        String reminderToSend = taskDetails+"$"
                +locationName+","+locationAttributes+"$"
                +expiryTimeDate+"$"
                +mPriority+"$"
                +taskStatus;


        sendSMS(currentUserNumber, reminderToSend);

        // Finish activity (this returns back to MainActivity)
        finish();

    }

    private String getCompleteAddressString(double LATITUDE, double LONGITUDE) {
        String strAdd = "";
        Geocoder geocoder = new Geocoder(this, Locale.getDefault());
        try {
            List<Address> addresses = geocoder.getFromLocation(LATITUDE, LONGITUDE, 1);
            if (addresses != null) {
                Address returnedAddress = addresses.get(0);
                StringBuilder strReturnedAddress = new StringBuilder("");

                for (int i = 0; i < returnedAddress.getMaxAddressLineIndex(); i++) {
                    strReturnedAddress.append(returnedAddress.getAddressLine(i)).append("\n");
                }
                strAdd = strReturnedAddress.toString();
                Log.w("My Current address", "" + strReturnedAddress.toString());
            } else {
                Log.w("My Current  address", "No Address returned!");
            }
        } catch (Exception e) {
            e.printStackTrace();
            Log.w("My Current  address", "Canont get Address!");
        }
        return strAdd;
    }

    public void sendSMS(String phoneNo, String msg) {
        try {
            SmsManager smsManager = SmsManager.getDefault();
            smsManager.sendTextMessage(phoneNo, null, msg, null, null);
            Toast.makeText(getApplicationContext(), "Message Sent",
                    Toast.LENGTH_LONG).show();
        } catch (Exception ex) {
            Toast.makeText(getApplicationContext(), ex.getMessage().toString(),
                    Toast.LENGTH_LONG).show();
            ex.printStackTrace();
        }
    }

    /**
     * onPrioritySelected is called whenever a priority button is clicked.
     * It changes the value of mPriority based on the selected button.
     */
    public void onPrioritySelected(View view) {
        if (((RadioButton) findViewById(R.id.radButton1)).isChecked()) {
            mPriority = 1;
        } else if (((RadioButton) findViewById(R.id.radButton2)).isChecked()) {
            mPriority = 2;
        } else if (((RadioButton) findViewById(R.id.radButton3)).isChecked()) {
            mPriority = 3;
        }
    }

    public static class TimePickerFragment extends DialogFragment
            implements TimePickerDialog.OnTimeSetListener {

        @Override
        public Dialog onCreateDialog(Bundle savedInstanceState) {
            // Use the current time as the default values for the picker
            final Calendar c = Calendar.getInstance();
            int hour = c.get(Calendar.HOUR_OF_DAY);
            int minute = c.get(Calendar.MINUTE);

            // Create a new instance of TimePickerDialog and return it
            return new TimePickerDialog(getActivity(), this, hour, minute,
                    DateFormat.is24HourFormat(getActivity()));
        }

        public void onTimeSet(TimePicker view, int hourOfDay, int minute) {
            // Do something with the time chosen by the user
            String time = "";
            if (hourOfDay < 12) {
                time = hourOfDay + ":" + minute + "am";
            } else {
                time = (hourOfDay - 12) + ":" + minute + "pm";
            }
            ((EditText) getActivity().findViewById(R.id.editTextExpiry)).setText(date + " | " + time);

        }
    }

    public static class DatePickerFragment extends DialogFragment
            implements DatePickerDialog.OnDateSetListener {

        @Override
        public Dialog onCreateDialog(Bundle savedInstanceState) {
            // Use the current date as the default date in the picker
            final Calendar c = Calendar.getInstance();
            int year = c.get(Calendar.YEAR);
            int month = c.get(Calendar.MONTH);
            int day = c.get(Calendar.DAY_OF_MONTH);

            // Create a new instance of DatePickerDialog and return it
            return new DatePickerDialog(getActivity(), this, year, month, day);
        }

        public void onDateSet(DatePicker view, int year, int month, int day) {
            // Do something with the date chosen by the user
            date = month + "/" + day + "/" + year;
            //((EditText) getActivity().findViewById(R.id.editTextExpiry)).setText(date+" "+time);
        }
    }
}