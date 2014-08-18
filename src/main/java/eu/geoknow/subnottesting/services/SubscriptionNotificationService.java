package eu.geoknow.subnottesting.services;

import java.net.MalformedURLException;

public abstract class SubscriptionNotificationService {

    private static SubscriptionNotificationService instance;

    public abstract void setServiceUrl(String url) throws MalformedURLException;

    /**
     * Performs subscriptions to the system.
     * 
     * @return the number of registered subscriptions
     */
    public abstract int registerSubscriptions(String dir);

}
