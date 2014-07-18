package eu.geoknow.subnottesting.services;

import java.net.MalformedURLException;

public interface SubscriptiionNotificationService {

  public void setServiceUrl(String url) throws MalformedURLException;

  /**
   * Performs subscriptions to the system.
   * 
   * @return the number of registered subscriptions
   */
  public int registerSubscriptions(String dir);

}
