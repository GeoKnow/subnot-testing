package eu.geoknow.subnottesting.sparqlclientssimulators;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.UnsupportedEncodingException;
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

public class SimpleSparqlSimulator implements SparqlSimulator {

  private static final Logger LOGGER = Logger.getLogger(SimpleSparqlSimulator.class);

  private URL endpoint;
  private boolean exit;
  private List<File> sparql_queries;

  private SparqlSimulator instance;

  public SimpleSparqlSimulator(String endpoint) throws MalformedURLException {
    // read the list of files of queries
    sparql_queries = new ArrayList<File>();

    URL url = getClass().getResource("/queries");

    LOGGER.info("Reading queries txt files from resources...");
    File dir = new File(url.getPath());

    if (dir.isDirectory()) {
      for (final File fileEntry : dir.listFiles()) {
        if (fileEntry.isFile())
          sparql_queries.add(fileEntry);
      }

    } else
      LOGGER.error("Not a directory");

    this.endpoint = new URL(endpoint);
  }

  public void run() throws HttpException, IOException {

    HttpClient client = new HttpClient();
    client.getHostConfiguration().setHost(endpoint.getHost());

    exit = false;
    for (int index = 0; index < sparql_queries.size(); index++) {
      try {

        FileInputStream fis = new FileInputStream(sparql_queries.get(index));
        String query = IOUtils.toString(fis, "UTF-8");

        String params = "?query=" + URLEncoder.encode(query, "UTF-8");
        params += "&format=" + URLEncoder.encode("application/sparql-results+json", "UTF-8");

        String uri = endpoint.toString() + params;

        LOGGER.info("MethodGet :" + uri);

        GetMethod method = new GetMethod(uri);
        client.executeMethod(method);

        BufferedReader br = new BufferedReader(new InputStreamReader(method
            .getResponseBodyAsStream()));
        String readLine;
        while (((readLine = br.readLine()) != null)) {
          LOGGER.info("  " + readLine);
        }

        method.releaseConnection();

      } catch (UnsupportedEncodingException e) {
        // TODO Auto-generated catch block
        e.printStackTrace();
      }

      // if (exit) break;
    }
  }

  public void stop() {
    exit = true;
  }

}
