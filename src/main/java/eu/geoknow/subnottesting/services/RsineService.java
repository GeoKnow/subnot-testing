package eu.geoknow.subnottesting.services;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.net.MalformedURLException;
import java.net.URL;

import javax.activation.MimetypesFileTypeMap;

import org.apache.commons.httpclient.HttpClient;
import org.apache.commons.httpclient.methods.PostMethod;
import org.apache.commons.httpclient.methods.StringRequestEntity;
import org.apache.commons.io.IOUtils;
import org.apache.log4j.Logger;

public class RsineService implements SubscriptiionNotificationService {

  private static final Logger LOGGER = Logger.getLogger(RsineService.class);

  public static final MimetypesFileTypeMap mimetypes_map;
  static {
    mimetypes_map = new MimetypesFileTypeMap();
    mimetypes_map.addMimeTypes("text/turtle ttl");
    mimetypes_map.addMimeTypes("text/n3 n3");
    mimetypes_map.addMimeTypes("application/rdf+xml rdf");
  }

  HttpClient client;
  URL url;

  public void setServiceUrl(String url_p) throws MalformedURLException {
    url = new URL(url_p);
    client = new HttpClient();
    client.getHostConfiguration().setHost(url.getHost());
    LOGGER.info("Rsine service at " + url_p);
  }

  public void notificationListener() {
    // TODO Auto-generated method stub

  }

  /**
   * This function will registered subscriptions that are in the resources directory
   */
  public void registerSubscriptions() {

    URL url = getClass().getResource("/subscriptions");

    LOGGER.info("Registering subscriptions...");
    File dir = new File(url.getPath());
    int n_subs = 0;
    if (dir.isDirectory()) {
      n_subs = subscribeRdfFiles(dir);
      LOGGER.info(n_subs + " registered subscriptions");
    } else
      LOGGER.error("Not a directory");

  }

  private int subscribeRdfFiles(final File folder) {
    int count = 0;
    for (final File fileEntry : folder.listFiles()) {
      if (fileEntry.isDirectory()) {
        subscribeRdfFiles(fileEntry);
      } else {
        String regex = "([^\\s]+(\\.(?i)(ttl|rdf|n3))$)";
        if (fileEntry.getName().matches(regex)) {
          System.out.println(fileEntry.getName());

          String content_type = mimetypes_map.getContentType(fileEntry);

          FileInputStream fis;
          try {
            fis = new FileInputStream(fileEntry);
            String content = IOUtils.toString(fis, "UTF-8");
            PostMethod subscription = new PostMethod(url.toString() + "/register");
            subscription.setRequestEntity(new StringRequestEntity(content, content_type, "UTF-8"));

            LOGGER.info("Subscribing :" + subscription.getPath());
            client.executeMethod(subscription);
            subscription.releaseConnection();
            count++;
          } catch (IOException e) {
            e.printStackTrace();
          }

        }
      }
    }
    return count;
  }
}
