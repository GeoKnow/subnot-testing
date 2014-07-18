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

  /**
   * This function will registered subscriptions that are in the resources
   * directory
   * 
   * @return number of subscriptions registered
   */
  public int registerSubscriptions(String directory) {

    URL url = getClass().getResource("/");

    LOGGER.info("Registering subscriptions (ttl|rdf|n3 files) from resources/" + directory);
    File dir = new File(url.getPath() + directory);
    int n_subs = 0;
    if (dir.isDirectory()) {
      n_subs = subscribeRdfFiles(dir);
      LOGGER.info(n_subs + " registered subscriptions");
    } else
      LOGGER.error("Not a directory");
    return n_subs;

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

            client.executeMethod(subscription);

            if (subscription.getStatusCode() == 201) {
              LOGGER.error(subscription.getStatusCode() + " :" + subscription.getStatusText() + " "
                  + subscription.getPath());
            } else
              LOGGER.error(subscription.getStatusCode() + " :" + subscription.getStatusText() + " "
                  + subscription.getPath());

            subscription.releaseConnection();

          } catch (IOException e) {
            e.printStackTrace();
          }

        }
      }
    }
    return count;
  }
}
