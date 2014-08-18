package eu.geoknow.subnottesting.metrics;

import java.util.ArrayList;
import java.util.List;

import org.apache.log4j.Logger;

public class Notification {

    private static List<ChangeSetNotification> changeSetNotifications;
    private String subscription;

    private static final Logger LOGGER = Logger.getLogger(Notification.class);

    public String getSubscription() {
	return subscription;
    }

    public void setSubscription(String subscription) {
	this.subscription = subscription;
    }

    public Notification(String subscription) {

	changeSetNotifications = new ArrayList<ChangeSetNotification>();
	this.setSubscription(subscription);
    }

    public synchronized List<ChangeSetNotification> getChangeSetNotifications() {
	return changeSetNotifications;
    }

    public void addChangeSetNotification(ChangeSetNotification n) {
	changeSetNotifications.add(n);
    }

    public synchronized long getChangeSetNotificationAverageTime() {
	if (changeSetNotifications.size() == 0)
	    return 0;
	long sum = 0;
	for (int i = 0; i < changeSetNotifications.size(); i++) {
	    sum += changeSetNotifications.get(i).getChangeSetNotificationTime();
	}
	return sum / (long) changeSetNotifications.size();
    }
}
