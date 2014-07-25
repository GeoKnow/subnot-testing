package eu.geoknow.subnottesting.metrics;

public class ChangeSetNotification {

  private long notificationTimeStamp;
  private long changeSetTimeStamp;
  private String changSet;

  public long getNotificationTimeStamp() {
    return notificationTimeStamp;
  }

  public void setNotificationTimeStamp(long notificationTimeStamp) {
    this.notificationTimeStamp = notificationTimeStamp;
  }

  public long getChangeSetTimeStamp() {
    return changeSetTimeStamp;
  }

  public void setChangeSetTimeStamp(long changeSetTimeStamp) {
    this.changeSetTimeStamp = changeSetTimeStamp;
  }

  public String getChangSet() {
    return changSet;
  }

  public void setChangSet(String changSet) {
    this.changSet = changSet;
  }

  public long getChangeSetNotificationTime() {
    return notificationTimeStamp - changeSetTimeStamp;
  }

}
