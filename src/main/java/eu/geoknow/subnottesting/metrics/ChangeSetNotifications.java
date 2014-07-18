package eu.geoknow.subnottesting.metrics;

import java.util.ArrayList;
import java.util.List;

public class ChangeSetNotifications {

  private static List<ChangeSetNotification> notifications;
  private static ChangeSetNotifications instance = null;

  public static ChangeSetNotifications getInstance() {

    if (instance == null) {
      instance = new ChangeSetNotifications();
      notifications = new ArrayList<ChangeSetNotification>();
    }
    return instance;
  }

  public List<ChangeSetNotification> getChangeSetNotifications() {
    return notifications;
  }

  public ChangeSetNotification addChangeSetNotification() {
    ChangeSetNotification n = new ChangeSetNotification();
    notifications.add(n);
    return n;
  }

  public long getChangeSetNotificationAverageTime() {
    if (notifications.size() == 0)
      return 0;
    long sum = 0;
    for (int i = 0; i < notifications.size(); i++) {
      sum += notifications.get(i).getChangeSetNotificationTime();
    }
    return sum / (long) notifications.size();
  }
}
