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
import eu.geoknow.subnottesting.metrics.Notification;
import eu.geoknow.subnottesting.metrics.Notifications;

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

    public void init(final ServletConfig config) throws ServletException {
	super.init(config);

    }

    protected void doPost(HttpServletRequest req, HttpServletResponse resp)
	    throws ServletException, IOException {

	// stop hearing notifications if manager is not testing
	if (!Manager.isTesting())
	    return;

	Map<String, String[]> requestParameters = req.getParameterMap();

	Notifications notifications = Notifications.getInstance();

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
		    LOGGER.debug("notification:" + subscription);
		    Notification notification = new Notification(subscription);
		    // get the changesets
		    p = Pattern.compile(changeset_pattern);
		    m = p.matcher(value);
		    while (m.find()) {
			String[] bindings = m.group(1).split(";");
			ChangeSetNotification csnotification = new ChangeSetNotification();

			csnotification
				.setNotificationTimeStamp(notificationTimeStamp);
			changes_count++;
			for (int i = 0; i < bindings.length; i++) {
			    String binding = bindings[i];
			    String[] kv = binding.split("=");
			    // it is required a property called csdate that
			    // contains the date
			    // of the change
			    LOGGER.debug(kv[0] + " - " + kv[1]);
			    if ("csdate".equals(kv[0])) {
				Pattern pdate = Pattern.compile("\"(.*?)\"");
				Matcher mdate = pdate.matcher(kv[1]);
				if (mdate.find()) {
				    try {
					XMLGregorianCalendar date = DatatypeFactory
						.newInstance()
						.newXMLGregorianCalendar(
							mdate.group(1));

					csnotification
						.setChangeSetTimeStamp(date
							.toGregorianCalendar()
							.getTimeInMillis());
					notification
						.addChangeSetNotification(csnotification);

				    } catch (DatatypeConfigurationException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				    }
				}

			    }

			}

		    }
		    LOGGER.info(subscription + " ==> " + changes_count
			    + " changes");
		    notifications.geNotifications().add(notification);
		}
	    }
	}
	resp.setStatus(HttpServletResponse.SC_OK);
    }
}
