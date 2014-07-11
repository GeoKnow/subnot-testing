package eu.geoknow.subnottesting.services;

import java.net.MalformedURLException;

public interface SubscriptiionNotificationService {

  public void setServiceUrl(String url) throws MalformedURLException;

  public void registerSubscriptions();

  public void notificationListener();
}
