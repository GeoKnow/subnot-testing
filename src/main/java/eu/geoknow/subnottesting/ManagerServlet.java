package eu.geoknow.subnottesting;

import java.io.IOException;
import java.io.PrintWriter;
import java.net.MalformedURLException;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.Date;

import javax.servlet.ServletConfig;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.apache.commons.lang3.time.DurationFormatUtils;
import org.apache.log4j.Logger;

import eu.geoknow.subnottesting.metrics.ChangeSetNotifications;
import eu.geoknow.subnottesting.metrics.Queries;
import eu.geoknow.subnottesting.services.RsineService;
import eu.geoknow.subnottesting.services.SubscriptiionNotificationService;
import eu.geoknow.subnottesting.sparqlclientssimulators.SparqlSimulator;
import eu.geoknow.subnottesting.sparqlclientssimulators.SupplyChainSimulator;

public class ManagerServlet extends HttpServlet {

  /**
   * 
   */
  private static final long serialVersionUID = 1L;
  private static final Logger LOGGER = Logger.getLogger(ManagerServlet.class);
  private static SparqlSimulator data_sim;
  private static SubscriptiionNotificationService sub_not;
  private long start = 0, end = 0;
  private int n_subscriptions = 0;
  private boolean running = false;

  public void init(final ServletConfig config) throws ServletException {
    super.init(config);

    try {
      // data_sim = new
      // SimpleSparqlSimulator(config.getServletContext().getInitParameter(
      // "proxy-endpoint"));

      data_sim = new SupplyChainSimulator(config.getServletContext().getInitParameter("scd"));
      sub_not = new RsineService();
      sub_not.setServiceUrl(config.getServletContext().getInitParameter("rsine"));

    } catch (MalformedURLException e) {
      // TODO Auto-generated catch block
      e.printStackTrace();
    }

  }

  protected void doPost(HttpServletRequest req, HttpServletResponse resp) throws ServletException,
      IOException {

    PrintWriter output = resp.getWriter();

    DateFormat fdate = new SimpleDateFormat("EEE, d MMM yyyy HH:mm:ss Z");

    String action = req.getParameter("action");

    if (action == null)
      resp.sendError(HttpServletResponse.SC_BAD_REQUEST, "action parameter is missing");

    if ("run".equals(action) && !running) {
      running = true;
      // clean static variables
      Queries.getInstance().getQueries().clear();
      ChangeSetNotifications.getInstance().getChangeSetNotifications().clear();
      // perform subscriptions
      n_subscriptions = sub_not.registerSubscriptions(data_sim.getClass().getName());
      output.write("\nRegistered subscriptions: " + n_subscriptions);
      // get time stamp
      start = System.currentTimeMillis();
      // start simulation
      data_sim.run();
      output.write("\nSimulation started: " + fdate.format(new Date(start)));
      resp.setStatus(HttpServletResponse.SC_OK);

    } else if ("stop".equals(action) && running) {
      running = false;
      // stop simulation and get time stamp
      data_sim.stop();
      end = System.currentTimeMillis();
      output.write("\nSimulation started: " + fdate.format(new Date(start)));
      output.write("\nSimulation ended: " + fdate.format(new Date(end)));

      // compute Query statistics
      Queries queries = Queries.getInstance();
      long duration = end - start;
      output.write("\nSimulation duration: "
          + DurationFormatUtils.formatDuration(duration, "HH:mm:ss,SSS") + "");
      output.write("\n# queries: " + queries.getQueries().size());
      output.write("\nquery average time: "
          + DurationFormatUtils.formatDuration(queries.getQueryRunTimeAverage(), "HH:mm:ss,SSS"));
      output.write("\nquery max duration time: "
          + DurationFormatUtils.formatDuration(queries.getQueryMaxDuration(), "HH:mm:ss,SSS"));
      output.write("\nquery min duration time: "
          + DurationFormatUtils.formatDuration(queries.getQueryMinDuration(), "HH:mm:ss,SSS"));
      // compute Notifications statistics
      output.write("\nRegistered subscriptions: " + n_subscriptions);

      ChangeSetNotifications csns = ChangeSetNotifications.getInstance();
      output.write("\n# changeset notifications: " + csns.getChangeSetNotifications().size());
      output.write("\nchangeset notification average time: "
          + DurationFormatUtils.formatDuration(csns.getChangeSetNotificationAverageTime(),
              "HH:mm:ss,SSS"));

      resp.setStatus(HttpServletResponse.SC_OK);

    }

    resp.flushBuffer();
  }

  protected void doGet(HttpServletRequest req, HttpServletResponse resp) throws ServletException,
      IOException {
    doPost(req, resp);
  }

}
