package eu.geoknow.subnottesting;

import java.io.IOException;
import java.io.PrintWriter;
import java.net.MalformedURLException;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Map.Entry;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.apache.commons.lang3.time.DurationFormatUtils;
import org.apache.log4j.Logger;

import eu.geoknow.subnottesting.metrics.Notifications;
import eu.geoknow.subnottesting.metrics.Queries;
import eu.geoknow.subnottesting.services.RsineService;
import eu.geoknow.subnottesting.services.SubscriptionNotificationService;
import eu.geoknow.subnottesting.sparqlclientssimulators.SparqlSimulator;
import eu.geoknow.subnottesting.sparqlclientssimulators.SupplyChainSimulator;

public class ManagerServlet extends HttpServlet {

    /**
   * 
   */
    private static final long serialVersionUID = 1L;
    private static final Logger LOGGER = Logger.getLogger(ManagerServlet.class);
    private SparqlSimulator data_sim;
    private SubscriptionNotificationService sub_not;
    private long start = 0, end = 0;
    private int n_subscriptions = 0;

    private long duration;

    private String rsineUrl;
    private String simulator;
    private String supplyChainSimulatorUrl;
    private String supplyChainSimulatorFrequency;
    private boolean autoStop = true;

    protected void doPost(HttpServletRequest req, HttpServletResponse resp)
	    throws ServletException, IOException {

	DateFormat fdate = new SimpleDateFormat("EEE, d MMM yyyy HH:mm:ss Z");
	String action = req.getParameter("action");
	if (action == null)
	    throw new ServletException("action parameter is missing");

	if ("run".equals(action) && !Manager.isTesting()) {

	    rsineUrl = req.getParameter("rsineUrl");
	    simulator = req.getParameter("simulator");
	    supplyChainSimulatorUrl = req
		    .getParameter("SupplyChainSimulatorUrl");
	    supplyChainSimulatorFrequency = req
		    .getParameter("SupplyChainSimulatorFrequency");

	    autoStop = req.getParameter("autoStop").equals("true") ? true
		    : false;

	    // get minutes duration to milliseconds
	    duration = Long.parseLong(req.getParameter("duration")) * 60 * 1000;

	    try {
		// data_sim = new
		// SimpleSparqlSimulator(config.getServletContext().getInitParameter(
		// "proxy-endpoint"));

		data_sim = SupplyChainSimulator
			.getInstance(supplyChainSimulatorUrl);
		SupplyChainSimulator
			.setFrequency(supplyChainSimulatorFrequency);
		sub_not = RsineService.getInstance();
		sub_not.setServiceUrl(rsineUrl);

	    } catch (MalformedURLException e) {
		throw new ServletException("Malformed URL");
	    }

	    // perform subscriptions
	    n_subscriptions = sub_not.registerSubscriptions(data_sim.getClass()
		    .getName());

	    resp.setContentType("text/html");
	    PrintWriter out = resp.getWriter();

	    out.println("<html>");
	    out.println("<head>");
	    out.println("<title>SubNot Testing</title>");
	    out.println("</head>");
	    out.println("<body>");
	    out.write("<p>Registered subscriptions: " + n_subscriptions
		    + "</p>");
	    // start simulation
	    data_sim.run();
	    // clean static variables
	    Queries.getInstance().getQueries().clear();
	    Notifications.getInstance().geNotifications().clear();
	    // get time stamp ignore initialization time
	    start = System.currentTimeMillis();
	    Manager.setTesting(true);

	    out.write("<p>Simulation started: " + fdate.format(new Date(start)));
	    out.write("<br>Queries: "
		    + Queries.getInstance().getQueries().size());
	    out.write("<br>Notifications: "
		    + Notifications.getInstance().geNotifications().size()
		    + "</p>");

	    long stop = start + duration;

	    if (autoStop) {
		out.write("<script language=\"javascript\">");
		out.write("setTimeout(function(){ document.getElementById(\"stopForm\").submit(); }, "
			+ duration + ");");
		out.write("</script>");
	    }

	    if (autoStop)
		out.write("<p>Simulation will stop at "
			+ fdate.format(new Date(stop)) + "</p>");
	    out.println("<form method=\"POST\" action=\"testing\" id=\"stopForm\">");
	    out.write("<input type=\"hidden\" name=\"action\" value=\"stop\">");
	    out.write("<input type=\"submit\" value=\"Stop\">");
	    out.println("</form>");
	    out.println("</body>");
	    out.println("</html>");

	    out.close();

	} else if ("stop".equals(action) && Manager.isTesting()) {
	    Manager.setTesting(false);
	    // stop simulation and get time stamp
	    end = System.currentTimeMillis();
	    data_sim.stop();
	    PrintWriter out = resp.getWriter();
	    out.println("<html>");
	    out.println("<head>");
	    out.println("<title>SubNot Testing</title>");
	    out.println("</head>");
	    out.println("<body>");
	    out.write("<p>Simulation started: " + fdate.format(new Date(start)));
	    out.write("<br>Simulation ended: " + fdate.format(new Date(end)));
	    out.write("<br>Simulation duration: "
		    + DurationFormatUtils.formatDuration(end - start,
			    "HH:mm:ss,SSS"));
	    out.write("<br>supplyChainSimulatorFrequency: "
		    + supplyChainSimulatorFrequency + "</p>");

	    // compute Query statistics

	    out.write("<p># queries: "
		    + Queries.getInstance().getQueries().size());
	    out.write("<br>query average time: "
		    + Queries.getInstance().getQueryRunTimeAverage()
		    + "ms => "
		    + DurationFormatUtils.formatDuration(Queries.getInstance()
			    .getQueryRunTimeAverage(), "HH:mm:ss,SSS"));
	    out.write("<br>query max duration time: "
		    + Queries.getInstance().getQueryMaxDuration()
		    + "ms => "
		    + DurationFormatUtils.formatDuration(Queries.getInstance()
			    .getQueryMaxDuration(), "HH:mm:ss,SSS"));
	    out.write("<br>query min duration time: "
		    + Queries.getInstance().getQueryMinDuration()
		    + "ms => "
		    + DurationFormatUtils.formatDuration(Queries.getInstance()
			    .getQueryMinDuration(), "HH:mm:ss,SSS") + "</p>");
	    // compute Notifications statistics
	    out.write("<p>Registered subscriptions: " + n_subscriptions);
	    out.write("<br>#  notifications: "
		    + Notifications.getInstance().geNotifications().size());
	    out.write("<br>#  changesets: "
		    + Notifications.getInstance()
			    .geNotificationsChangesetsSize());
	    out.write("<br>changeset notification average time: "
		    + Notifications.getInstance().getNotificationTimeAverage()
		    + "ms => "
		    + DurationFormatUtils.formatDuration(Notifications
			    .getInstance().getNotificationTimeAverage(),
			    "HH:mm:ss,SSS") + "</p>");
	    out.write("<p>");
	    for (Entry<String, Integer> e : Notifications.getInstance()
		    .getNotificationsBySubscriotions().entrySet()) {
		out.write("<br>" + e.getKey() + " - " + e.getValue());

	    }
	    out.write("</p>");
	    out.close();
	}

    }

    protected void doGet(HttpServletRequest req, HttpServletResponse resp)
	    throws ServletException, IOException {
	// doPost(req, resp);
    }

}
