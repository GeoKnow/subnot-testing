package eu.geoknow.subnottesting.sparqlclientssimulators;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.MalformedURLException;
import java.net.URL;
import java.net.URLEncoder;
import java.util.ArrayList;
import java.util.List;

import org.apache.commons.httpclient.HttpClient;
import org.apache.commons.httpclient.HttpException;
import org.apache.commons.httpclient.methods.GetMethod;
import org.apache.commons.io.IOUtils;
import org.apache.log4j.Logger;

/***
 * This class reads form resources/queries all txt files and post them to the
 * endpoint
 * 
 * @author alejandragarciarojas
 * 
 */

public class SimpleSparqlSimulator implements SparqlSimulator {

  private static final Logger LOGGER = Logger.getLogger(SimpleSparqlSimulator.class);

  private URL endpoint;
  private List<File> sparql_queries;

  public SimpleSparqlSimulator(String endpoint) throws MalformedURLException {
    // read the list of files of queries
    sparql_queries = new ArrayList<File>();

    URL url = getClass().getResource("/");

    LOGGER.info("Reading queries (txt files) from " + url.getPath()
        + SimpleSparqlSimulator.class.getName());
    File dir = new File(url.getPath() + SimpleSparqlSimulator.class.getName());

    if (dir.isDirectory()) {
      for (final File fileEntry : dir.listFiles()) {
        if (fileEntry.isFile())
          if (fileEntry.getName().endsWith(".txt"))
            sparql_queries.add(fileEntry);
      }

    } else
      LOGGER.error("Not a directory");

    this.endpoint = new URL(endpoint);
  }

  public void run() throws HttpException, IOException {

    HttpClient client = new HttpClient();
    client.getHostConfiguration().setHost(endpoint.getHost());

    for (int index = 0; index < sparql_queries.size(); index++) {

      LOGGER.info("Posting " + sparql_queries.get(index).getName());

      FileInputStream fis = new FileInputStream(sparql_queries.get(index));
      String query = IOUtils.toString(fis, "UTF-8");

      String params = "?query=" + URLEncoder.encode(query, "UTF-8");
      params += "&format=" + URLEncoder.encode("application/sparql-results+json", "UTF-8");

      String uri = endpoint.toString() + params;

      LOGGER.debug("MethodGet :" + uri);

      GetMethod method = new GetMethod(uri);
      client.executeMethod(method);

      BufferedReader br = new BufferedReader(
          new InputStreamReader(method.getResponseBodyAsStream()));
      String readLine;
      LOGGER.info("Response status:  " + method.getStatusCode());
      while (((readLine = br.readLine()) != null)) {
        LOGGER.debug("  " + readLine);
      }
      method.releaseConnection();

    }
  }

  public void stop() {

  }

}
