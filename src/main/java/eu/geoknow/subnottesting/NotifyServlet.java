package eu.geoknow.subnottesting;

import java.io.IOException;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import javax.servlet.ServletConfig;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.xml.datatype.DatatypeConfigurationException;
import javax.xml.datatype.DatatypeFactory;
import javax.xml.datatype.XMLGregorianCalendar;

import org.apache.log4j.Logger;

import eu.geoknow.subnottesting.metrics.ChangeSetNotification;
import eu.geoknow.subnottesting.metrics.ChangeSetNotifications;
import eu.geoknow.subnottesting.services.SubscriptiionNotificationService;
import eu.geoknow.subnottesting.sparqlclientssimulators.SimpleSparqlSimulator;

/**
 * Handle http notifications froms subscriptions services
 * 
 * @author alejandragarciarojas
 * 
 */
public class NotifyServlet extends HttpServlet {

  private static final long serialVersionUID = 1L;
  /**
   * 
   */

  private static final Logger LOGGER = Logger.getLogger(NotifyServlet.class);
  private static SimpleSparqlSimulator data_sim;
  private static SubscriptiionNotificationService sub_not;

  public void init(final ServletConfig config) throws ServletException {
    super.init(config);

  }

  protected void doPost(HttpServletRequest req, HttpServletResponse resp) throws ServletException,
      IOException {

    Map<String, String[]> requestParameters = req.getParameterMap();

    ChangeSetNotifications notifications = ChangeSetNotifications.getInstance();

    long notificationTimeStamp = System.currentTimeMillis();

    for (String name : requestParameters.keySet()) {
      for (String value : requestParameters.get(name)) {
        String changeset_pattern = "\\[(.*?)\\]";
        String subscription_pattern = "\\[.*?You receive this notification because of subscription (.*?)\\]";
        if ("messages".equals(name)) {
          // get the subscription message
          Pattern p = Pattern.compile(subscription_pattern);
          Matcher m = p.matcher(value);
          int changes_count = 0;
          String subscription = "";
          while (m.find()) {
            subscription = m.group(1);
          }
          // get the changesets
          p = Pattern.compile(changeset_pattern);
          m = p.matcher(value);
          while (m.find()) {
            String[] bindings = m.group(1).split(";");
            ChangeSetNotification csnotification = notifications.addChangeSetNotification();
            csnotification.setNotificationTimeStamp(notificationTimeStamp);
            changes_count++;
            for (int i = 0; i < bindings.length; i++) {
              String binding = bindings[i];
              String[] kv = binding.split("=");
              // it is required a property called csdate that contains the date
              // of the change
              if ("csdate".equals(kv[0])) {
                Pattern pdate = Pattern.compile("\"(.*?)\"");
                Matcher mdate = pdate.matcher(kv[1]);
                if (mdate.find()) {
                  LOGGER.debug("csdate = " + mdate.group(1));
                  try {
                    XMLGregorianCalendar date = DatatypeFactory.newInstance()
                        .newXMLGregorianCalendar(mdate.group(1));
                    csnotification.setChangeSetTimeStamp(date.toGregorianCalendar()
                        .getTimeInMillis());

                  } catch (DatatypeConfigurationException e) {
                    // TODO Auto-generated catch block
                    e.printStackTrace();
                  }
                }

              } else
                LOGGER.debug(kv[0] + " - " + kv[1]);
            }

          }
          LOGGER.info(subscription + " ==> " + changes_count + " changes");
        }
      }
    }
    resp.setStatus(HttpServletResponse.SC_OK);
  }
}
