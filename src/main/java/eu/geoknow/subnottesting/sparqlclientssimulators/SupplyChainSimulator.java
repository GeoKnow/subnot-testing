package eu.geoknow.subnottesting.sparqlclientssimulators;

import java.io.IOException;
import java.net.MalformedURLException;

import org.apache.commons.httpclient.HttpClient;
import org.apache.commons.httpclient.methods.PostMethod;
import org.apache.log4j.Logger;

public class SupplyChainSimulator implements SparqlSimulator {

  private static final Logger LOGGER = Logger.getLogger(SupplyChainSimulator.class);

  String hostService;

  public SupplyChainSimulator(String hostService) throws MalformedURLException {

    this.hostService = hostService;

  }

  public void generateSubsciptionFiles() {

    String suppliers_query = " PREFIX sc: <http://www.xybermotive.com/ontology/> select ?s"
        + " from <subnot-test>  { ?s a sc:Supplier}";

  }

  public void run() {

    String uri = this.hostService + "/simulator/run";

    HttpClient client = new HttpClient();
    PostMethod method = new PostMethod(uri);

    try {
      client.executeMethod(method);
    } catch (IOException e) {
      LOGGER.error(e.getMessage());
    }
    method.releaseConnection();

    LOGGER.info("simulation started");
  }

  public void stop() {

    String uri = this.hostService + "/simulator/stop";
    HttpClient client = new HttpClient();
    PostMethod method = new PostMethod(uri);
    try {
      client.executeMethod(method);
    } catch (IOException e) {
      LOGGER.error(e.getMessage());
    }
    method.releaseConnection();
    LOGGER.info("simulation stoped");
  }
}
