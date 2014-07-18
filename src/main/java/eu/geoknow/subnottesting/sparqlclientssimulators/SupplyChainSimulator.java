package eu.geoknow.subnottesting.sparqlclientssimulators;

import java.io.IOException;
import java.net.MalformedURLException;

import org.apache.commons.httpclient.HttpClient;
import org.apache.commons.httpclient.HttpException;
import org.apache.commons.httpclient.methods.PostMethod;
import org.apache.log4j.Logger;

public class SupplyChainSimulator implements SparqlSimulator {

  private static final Logger LOGGER = Logger.getLogger(SupplyChainSimulator.class);

  String hostService;

  public SupplyChainSimulator(String hostService) throws MalformedURLException {

    this.hostService = hostService;

  }

  public void run() throws HttpException, IOException {

    String uri = this.hostService + "/simulator/run";

    HttpClient client = new HttpClient();
    PostMethod method = new PostMethod(uri);

    client.executeMethod(method);
    method.releaseConnection();

    LOGGER.info("simulation started");
  }

  public void stop() throws HttpException, IOException {

    String uri = this.hostService + "/simulator/stop";
    HttpClient client = new HttpClient();
    PostMethod method = new PostMethod(uri);
    client.executeMethod(method);
    method.releaseConnection();
    LOGGER.info("simulation stoped");
  }
}
