package eu.geoknow.subnottesting;

import java.io.IOException;
import java.io.PrintWriter;
import java.net.MalformedURLException;

import javax.servlet.ServletConfig;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.apache.log4j.Logger;

import eu.geoknow.subnottesting.services.RsineService;
import eu.geoknow.subnottesting.services.SubscriptiionNotificationService;
import eu.geoknow.subnottesting.sparqlclientssimulators.SampleSparqlClientSimulator;

public class SubNotTestingServlet extends HttpServlet {

  private static final Logger LOGGER = Logger.getLogger(SubNotTestingServlet.class);
  private static SampleSparqlClientSimulator data_sim;
  private static SubscriptiionNotificationService sub_not;

  public void init(final ServletConfig config) throws ServletException {
    super.init(config);

    try {
      data_sim = new SampleSparqlClientSimulator(config.getServletContext().getInitParameter(
          "proxy-endpoint"));

      sub_not = new RsineService();
      sub_not.setServiceUrl(config.getServletContext().getInitParameter("rsine"));
      sub_not.registerSubscriptions();

    } catch (MalformedURLException e) {
      // TODO Auto-generated catch block
      e.printStackTrace();
    }

  }

  protected void doPost(HttpServletRequest req, HttpServletResponse resp) throws ServletException,
      IOException {
    PrintWriter output = resp.getWriter();
    String action = req.getParameter("action");
    if ("run".equals(action)) {
      output.write("run started");
      data_sim.run();
      output.write("run finished");
      resp.setStatus(HttpServletResponse.SC_OK);

    } else if ("stop".equals(action)) {
      data_sim.stop();
      output.write("run stopped");
      resp.setStatus(HttpServletResponse.SC_OK);
    } else if ("report".equals(action)) {
      Queries queries = Queries.getInstance();
      output.write("query average time: " + queries.getQueryRunTimeAverage());
      resp.setStatus(HttpServletResponse.SC_OK);
    } else
      resp.sendError(HttpServletResponse.SC_BAD_REQUEST, "action parameter is missing");
    resp.flushBuffer();
  }

  protected void doGet(HttpServletRequest req, HttpServletResponse resp) throws ServletException,
      IOException {
    doPost(req, resp);
  }
}
