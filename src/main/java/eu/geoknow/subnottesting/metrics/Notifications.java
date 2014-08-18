package eu.geoknow.subnottesting.metrics;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.apache.log4j.Logger;

public class Notifications {

    private static final Logger LOGGER = Logger.getLogger(Notifications.class);

    private static List<Notification> list;
    private static Notifications instance = null;

    public static synchronized Notifications getInstance() {
	if (instance == null) {
	    instance = new Notifications();
	    list = new ArrayList<Notification>();
	}
	return instance;
    }

    public synchronized List<Notification> geNotifications() {
	return list;
    }

    public synchronized long getNotificationTimeAverage() {
	if (list.isEmpty())
	    return (long) 0;
	long sum = 0;
	for (int index = 0; index < list.size(); index++) {
	    sum += list.get(index).getChangeSetNotificationAverageTime();
	}
	return sum / (long) list.size();
    }

    public synchronized int geNotificationsChangesetsSize() {
	if (list.isEmpty())
	    return 0;
	int sum = 0;
	for (int index = 0; index < list.size(); index++) {
	    sum += list.get(index).getChangeSetNotifications().size();
	}
	return sum;
    }

    public synchronized Map<String, Integer> getNotificationsBySubscriotions() {
	Map<String, Integer> map = new HashMap<String, Integer>();
	for (int index = 0; index < list.size(); index++) {
	    Notification n = list.get(index);
	    if (map.containsKey(n.getSubscription()))
		map.put(n.getSubscription(), map.get(n.getSubscription()) + 1);
	    else
		map.put(n.getSubscription(), 1);
	}
	return map;
    }
    // public long getQueryMaxDuration() {
    // long max = 0;
    //
    // for (int index = 0; index < list.size(); index++) {
    // if (list.get(index).getRunTime() > max)
    // max = list.get(index).getRunTime();
    // }
    //
    // return max;
    // }
    //
    // public long getQueryMinDuration() {
    //
    // long min = Long.MAX_VALUE;
    //
    // for (int index = 0; index < list.size(); index++) {
    // if (list.get(index).getRunTime() < min)
    // min = list.get(index).getRunTime();
    // }
    // return min;
    // }

}
