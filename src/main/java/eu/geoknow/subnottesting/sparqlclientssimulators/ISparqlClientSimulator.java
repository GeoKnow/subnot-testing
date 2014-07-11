package eu.geoknow.subnottesting.sparqlclientssimulators;

import java.io.IOException;

import org.apache.commons.httpclient.HttpException;

public interface ISparqlClientSimulator {

  public void run() throws HttpException, IOException;

  public void stop();
}
