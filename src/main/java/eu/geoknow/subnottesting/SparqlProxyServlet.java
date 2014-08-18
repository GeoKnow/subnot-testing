package eu.geoknow.subnottesting;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.MalformedURLException;
import java.net.URL;
import java.net.URLEncoder;
import java.util.Map;

import javax.servlet.ServletConfig;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.apache.commons.httpclient.HttpClient;
import org.apache.commons.httpclient.MultiThreadedHttpConnectionManager;
import org.apache.commons.httpclient.methods.GetMethod;
import org.apache.commons.httpclient.methods.PostMethod;
import org.apache.log4j.Logger;

import eu.geoknow.subnottesting.metrics.Queries;
import eu.geoknow.subnottesting.metrics.Query;

/**
 * This class is a Proxy to measure query response time and to analyze what
 * subscriptions can be affected by a given query
 * 
 * @author alejandragarciarojas
 * 
 */

public class SparqlProxyServlet extends HttpServlet {

    /**
   * 
   */
    private static final long serialVersionUID = 1L;

    private static final Logger LOGGER = Logger
	    .getLogger(SparqlProxyServlet.class);

    private URL url;
    private HttpClient proxy;

    /**
     * The Servlet is initialized with the destination endpoint provided in the
     * web.xml file
     */
    public void init(final ServletConfig config) throws ServletException {

	super.init(config);

	String destUrl = config.getServletContext().getInitParameter(
		"destination-endpoint");

	LOGGER.info("destination-endpoint: " + destUrl);

	try {
	    url = new URL(destUrl);
	} catch (MalformedURLException me) {
	    throw new ServletException(destUrl + " Proxy URL is invalid", me);
	}
	proxy = new HttpClient(new MultiThreadedHttpConnectionManager());
	proxy.getHostConfiguration().setHost(url.getHost());

    }

    @Override
    protected void doPost(HttpServletRequest req, HttpServletResponse resp)
	    throws ServletException, IOException {
	Map<String, String[]> requestParameters = req.getParameterMap();

	String uri = url.toString();
	Query query = null;
	PostMethod proxyMethod = new PostMethod(uri);
	for (String name : requestParameters.keySet()) {
	    for (String value : requestParameters.get(name)) {
		if ("query".equals(name))
		    query = new Query(value);

		// TODO determine what subscriptions can be affected by this
		// query
		// query.subscriptionsImpact
		proxyMethod.addParameter(name, value);
	    }
	}

	if (query != null) {
	    query.setStartTime(System.currentTimeMillis());
	    LOGGER.debug(query.getQuery());
	}
	proxy.executeMethod(proxyMethod);

	if (query != null) {
	    query.setEndTime(System.currentTimeMillis());
	    Queries.getInstance().addQuery(query);
	    LOGGER.debug("duration:"
		    + (query.getEndTime() - query.getStartTime()));
	}

	write(proxyMethod.getResponseBodyAsStream(), resp.getOutputStream());

	proxyMethod.releaseConnection();
    }

    private void write(final InputStream inputStream,
	    final OutputStream outputStream) throws IOException {
	int b;
	while ((b = inputStream.read()) != -1) {
	    outputStream.write(b);
	}
	outputStream.flush();
    }

    @Override
    protected void doGet(HttpServletRequest req, HttpServletResponse resp)
	    throws ServletException, IOException {

	Map<String, String[]> requestParameters = req.getParameterMap();

	Query query = null;

	StringBuilder params = new StringBuilder();
	for (String name : requestParameters.keySet()) {

	    for (String value : requestParameters.get(name)) {

		if (params.length() == 0) {
		    params.append("?");
		} else {
		    params.append("&");
		}

		if ("query".equals(name))
		    query = new Query(value);

		name = URLEncoder.encode(name, "UTF-8");
		value = URLEncoder.encode(value, "UTF-8");

		params.append(String.format("&%s=%s", name, value));
	    }
	}

	String uri = String.format("%s%s", url.toString(), params.toString());
	GetMethod proxyMethod = new GetMethod(uri);

	if (query != null) {
	    LOGGER.debug(query.getQuery());
	    query.setStartTime(System.currentTimeMillis());
	}
	proxy.executeMethod(proxyMethod);

	if (query != null) {
	    query.setEndTime(System.currentTimeMillis());
	    Queries.getInstance().addQuery(query);
	    LOGGER.debug("duration:"
		    + (query.getEndTime() - query.getStartTime()) + " == "
		    + query.getRunTime());
	}

	write(proxyMethod.getResponseBodyAsStream(), resp.getOutputStream());

	proxyMethod.releaseConnection();

    }

}
