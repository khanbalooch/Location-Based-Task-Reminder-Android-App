package first.modyt.Broadcost;

import android.content.res.Resources;

import first.first.R;

/**
 * Created by Irshad on 5/1/2017.
 */

public class TaskDetail {
    private String userName;
    private String userNumber;
    private String task;
    private String time;
    private String date;
    private String status;
    private String priority;
    private String locationName;
    private String locationAttributes;
    private String location;
    private String expiryTimeDate;
    private String locationLatitude;
    private String locationLongitude;

    public String getLocationName() {
        return locationName;
    }

    public void setLocationName(String locationName) {
        this.locationName = locationName;
    }

    public String getLocationAttributes() {
        return locationAttributes;
    }

    public void setLocationAttributes(String locationAttributes) {
        this.locationAttributes = locationAttributes;
    }

    public String getLocation() {
        return location;
    }

    public void setLocation(String location) {
        this.location = location;
    }

    public String getExpiryTimeDate() {
        return expiryTimeDate;
    }

    public void setExpiryTimeDate(String expiryTimeDate) {
        this.expiryTimeDate = expiryTimeDate;
    }

    public String getLocationLatitude() {
        return locationLatitude;
    }

    public void setLocationLatitude(String locationLatitude) {
        this.locationLatitude = locationLatitude;
    }

    public String getLocationLongitude() {
        return locationLongitude;
    }

    public void setLocationLongitude(String locationLongitude) {
        this.locationLongitude = locationLongitude;
    }

    public TaskDetail(String [] taskDescription){
        this.userName=taskDescription[0];
        this.userNumber=taskDescription[1];

        String taskDescriptionArray[] = taskDescription[2].split("\\$");
        this.task = taskDescriptionArray[0];
        this.location=taskDescriptionArray[1];
        this.expiryTimeDate=taskDescriptionArray[2];

        this.priority=taskDescriptionArray[3];
        this.status=taskDescriptionArray[4];


        String locationDetailArray[] = taskDescriptionArray[1].split(",");
        this.locationName=locationDetailArray[0];
        this.locationAttributes=locationDetailArray[1];
        String locationLatitudeLongitude[] = this.locationAttributes.split("\\*");
        this.locationLatitude=locationLatitudeLongitude[0];
        this.locationLatitude=locationLatitudeLongitude[1];




        if(taskDescriptionArray[2].length()!=0) {
            String expiryTimeDate[] = taskDescriptionArray[2].split("\\|");
            this.date = expiryTimeDate[0];
            this.time = expiryTimeDate[1];
        }
    }


    public String getUserName() {
        return userName;
    }

    public void setUserName(String userName) {
        this.userName = userName;
    }

    public String getUserNumber() {
        return userNumber;
    }

    public void setUserNumber(String userNumber) {
        this.userNumber = userNumber;
    }

    public String getTask() {
        return task;
    }

    public void setTask(String task) {
        this.task = task;
    }

    public String getTime() {
        return time;
    }

    public void setTime(String time) {
        this.time = time;
    }

    public String getDate() {
        return date;
    }

    public void setDate(String date) {
        this.date = date;
    }

    public String getStatus() {
        return status;
    }

    public void setStatus(String status) {
        this.status = status;
    }

    public String getPriority() {
        return priority;
    }

    public void setPriority(String priority) {
        this.priority = priority;
    }

    public TaskDetail(String userName, String userNumber, String task, String time, String date, String status, String priority) {
        this.userName = userName;
        this.userNumber = userNumber;
        this.task = task;
        this.time = time;
        this.date = date;
        this.status = status;
        this.priority = priority;

    }

}
