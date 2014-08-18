package eu.geoknow.subnottesting.sparqlclientssimulators;

import java.io.File;
import java.io.IOException;
import java.net.MalformedURLException;
import java.net.URL;

import org.apache.commons.httpclient.HttpClient;
import org.apache.commons.httpclient.methods.PostMethod;
import org.apache.commons.io.FileUtils;
import org.apache.log4j.Logger;
import org.openrdf.model.Value;
import org.openrdf.query.BindingSet;
import org.openrdf.query.QueryLanguage;
import org.openrdf.query.TupleQuery;
import org.openrdf.query.TupleQueryResult;
import org.openrdf.repository.Repository;
import org.openrdf.repository.RepositoryConnection;
import org.openrdf.repository.http.HTTPRepository;

public class SupplyChainSimulator extends SparqlSimulator {

    private static final Logger LOGGER = Logger
	    .getLogger(SupplyChainSimulator.class);

    private static String hostService;
    private static String frequency = "1.0";
    private static SupplyChainSimulator instance;

    private String namespaces = "@prefix spin: <http://spinrdf.org/sp/> .\n"
	    + "@prefix foaf: <http://xmlns.com/foaf/0.1/> .\n"
	    + "@prefix rsine: <http://lod2.eu/rsine/> .\n"
	    + "@prefix http: <http://www.w3.org/2011/http#> .\n"
	    + "@prefix dcterms: <http://purl.org/dc/terms/>.\n";

    private String notifier = "rsine:notifier [\n"
	    + " a rsine:httpNotifier ;\n"
	    + " http:methodName \"POST\";\n"
	    + " http:absoluteURI <http://localhost:8080/subnot-testing/notify> \n"
	    + " ].";

    // generate a subscription for new order of each supplier replace
    // REPLACE_UR, IREPLACE_URI with corresponding info
    private String received_order_from = "<http://example.org/REPLACE_URI> a rsine:Subscription;\n"
	    + " dcterms:description \"new order to REPLACE_NAME Supplier\";\n"
	    + " rsine:query [\n"
	    + " spin:text \"PREFIX cs:<http://purl.org/vocab/changeset/schema#>\n"
	    + " PREFIX spin:<http://spinrdf.org/sp/>\n"
	    + " PREFIX rdf:<http://www.w3.org/1999/02/22-rdf-syntax-ns#>\n"
	    + " PREFIX skos:<http://www.w3.org/2004/02/skos/core#>\n"
	    + " PREFIX sc: <http://www.xybermotive.com/ontology/> \n"
	    + " SELECT * WHERE {\n"
	    + " ?cs a cs:ChangeSet .\n"
	    + " ?cs cs:createdDate ?csdate .\n"
	    + " ?cs cs:addition ?addition .\n"
	    + " ?addition rdf:subject ?concept .\n"
	    + " ?addition rdf:predicate rdf:type .\n"
	    + " ?addition rdf:object sc:Order }\";\n"
	    + " rsine:condition [\n"
	    + " spin:text \"PREFIX sc: <http://www.xybermotive.com/ontology/> \n"
	    + " ASK {\n"
	    + " ?concept sc:connection ?connection . \n"
	    + " ?connection sc:receiver ?receiver .\n"
	    + " ?receiver sc:name ?name .\n"
	    + " FILTER (regex(?name, \'REPLACE_NAME\'))\n"
	    + " }\";\n"
	    + " rsine:expect \"true\"^^xsd:boolean;\n" + " ];\n ];\n";

    private String received_product = "<http://example.org/REPLACE_URI> a rsine:Subscription;\n"
	    + " dcterms:description \"received product REPLACE_NAME\";\n"
	    + " rsine:query [\n"
	    + " spin:text \"PREFIX cs:<http://purl.org/vocab/changeset/schema#>\n"
	    + " PREFIX spin:<http://spinrdf.org/sp/>\n"
	    + " PREFIX rdf:<http://www.w3.org/1999/02/22-rdf-syntax-ns#>\n"
	    + " PREFIX skos:<http://www.w3.org/2004/02/skos/core#>\n"
	    + " PREFIX sc: <http://www.xybermotive.com/ontology/> \n"
	    + " SELECT * WHERE {\n"
	    + " ?cs a cs:ChangeSet .\n"
	    + " ?cs cs:createdDate ?csdate .\n"
	    + " ?cs cs:addition ?addition .\n"
	    + " ?addition rdf:subject ?concept .\n"
	    + " ?addition rdf:predicate rdf:type .\n"
	    + " ?addition rdf:object sc:Shipping\n"
	    + " }\";\n"
	    + " rsine:condition [\n"
	    + " spin:text \"PREFIX sc: <http://www.xybermotive.com/ontology/> \n"
	    + " ASK {\n"
	    + " ?concept sc:connection ?connection . \n"
	    + " ?connection sc:product ?product .\n"
	    + " ?product sc:name ?name .\n"
	    + " FILTER (regex(?name, \'REPLACE_NAME\'))\n"
	    + " }\";\n"
	    + " rsine:expect \"true\"^^xsd:boolean;\n ];\n];\n";

    private String product_arrived_late = "<http://example.org/REPLACE_URI> a rsine:Subscription;\n"
	    + " dcterms:description \"product arrived late REPLACE_NAME arrived late\";\n"
	    + " rsine:query [\n"
	    + " spin:text \"PREFIX cs:<http://purl.org/vocab/changeset/schema#>\n"
	    + " PREFIX spin:<http://spinrdf.org/sp/>\n"
	    + " PREFIX rdf:<http://www.w3.org/1999/02/22-rdf-syntax-ns#>\n"
	    + " PREFIX skos:<http://www.w3.org/2004/02/skos/core#>\n"
	    + " PREFIX sc: <http://www.xybermotive.com/ontology/> \n"
	    + " SELECT * WHERE {\n"
	    + " ?cs a cs:ChangeSet .\n"
	    + " ?cs cs:createdDate ?csdate .\n"
	    + " ?cs cs:addition ?addition .\n"
	    + " ?addition rdf:subject ?concept .\n"
	    + " ?addition rdf:predicate rdf:type .\n"
	    + " ?addition rdf:object sc:Shipping\n"
	    + " }\";\n"
	    + " rsine:condition [\n"
	    + " spin:text \"PREFIX sc: <http://www.xybermotive.com/ontology/> \n"
	    + " ASK {\n"
	    + " ?concept sc:order ?order .\n"
	    + " ?order sc:dueDate ?dueDate .\n"
	    + " ?concept sc:date ?actualDate .\n"
	    + " ?concept sc:connection ?conn .\n"
	    + " ?conn sc:product ?product . \n"
	    + " ?product sc:name ?name .\n"
	    + " FILTER (regex(?name, \'REPLACE_NAME\'))\n"
	    + " FILTER(?actualDate > ?dueDate)\n"
	    + " }\";\n"
	    + " rsine:expect \"true\"^^xsd:boolean;\n ];\n ];\n";

    private String new_supplier_product = "<http://example.org/REPLACE_URI> a rsine:Subscription;\n"
	    + " dcterms:description \"new supplier of product REPLACE_NAME\";\n"
	    + " rsine:query [\n"
	    + " spin:text \"PREFIX cs:<http://purl.org/vocab/changeset/schema#>\n"
	    + " PREFIX spin:<http://spinrdf.org/sp/>\n"
	    + " PREFIX rdf:<http://www.w3.org/1999/02/22-rdf-syntax-ns#>\n"
	    + " PREFIX skos:<http://www.w3.org/2004/02/skos/core#>\n"
	    + " PREFIX sc: <http://www.xybermotive.com/ontology/> \n"
	    + " SELECT * WHERE {\n"
	    + " ?cs a cs:ChangeSet .\n"
	    + " ?cs cs:createdDate ?csdate .\n"
	    + " ?cs cs:addition ?addition .\n"
	    + " ?addition rdf:subject ?concept .\n"
	    + " ?addition rdf:predicate rdf:type .\n"
	    + " ?addition rdf:object sc:Supplier\n"
	    + " }\";\n"
	    + " rsine:condition [\n"
	    + " spin:text \"PREFIX sc: <http://www.xybermotive.com/ontology/> \n"
	    + " ASK {\n"
	    + " ?concept sc:product ?product .\n"
	    + " ?product sc:name ?name .\n"
	    + " FILTER (regex(?name, \'REPLACE_NAME\'))\n"
	    + " }\";\n"
	    + " rsine:expect \"true\"^^xsd:boolean;\n" + " ];\n ];\n \n";

    public static String getFrequency() {
	return frequency;
    }

    public static void setFrequency(String frequency) {
	SupplyChainSimulator.frequency = frequency;
    }

    public static SupplyChainSimulator getInstance(String hostService)
	    throws MalformedURLException {
	if (instance == null)
	    instance = new SupplyChainSimulator(hostService);
	return instance;
    }

    public SupplyChainSimulator(String hostService)
	    throws MalformedURLException {
	SupplyChainSimulator.hostService = hostService;
    }

    /**
     * TODO: find a better way to do this in the initialization
     * 
     * @param args
     */

    public static void main(String[] args) {
	try {
	    SupplyChainSimulator sim = new SupplyChainSimulator(
		    "http://localhost:9000");
	    sim.run();
	    sim.generateSubsciptionFiles("http://10.0.0.87:8890/sparql",
		    "subnot-test", sim.getClass().getName());
	    sim.stop();
	} catch (MalformedURLException e) {
	    // TODO Auto-generated catch block
	    e.printStackTrace();
	}
    }

    /**
     * Initializes the simulator
     */
    public void run() {

	String uri = SupplyChainSimulator.hostService
		+ "/simulator/run?frequency=" + frequency;

	HttpClient client = new HttpClient();
	PostMethod method = new PostMethod(uri);

	// method.setRequestBody(new NameValuePair[] { new NameValuePair(
	// "frequency", frequency) });
	try {
	    client.executeMethod(method);
	} catch (IOException e) {
	    LOGGER.error(e.getMessage());
	}
	method.releaseConnection();

	LOGGER.info("simulation started");
    }

    /**
     * Stops the simulator
     */
    public void stop() {

	String uri = SupplyChainSimulator.hostService + "/simulator/stop";
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

    /**
     * Generates subscription files based on predefined queries
     * 
     * @param endpoint
     * @param graph
     * @param directory
     */
    public void generateSubsciptionFiles(String endpoint, String graph,
	    String directory) {

	URL url = getClass().getResource(File.separator);
	String dir = url.getPath() + directory;

	Repository myRepository = new HTTPRepository(endpoint, "store");

	try {
	    myRepository.initialize();
	    RepositoryConnection con = myRepository.getConnection();

	    String suppliers_query = "PREFIX sc:<http://www.xybermotive.com/ontology/>\n"
		    + "select distinct ?uri ?name FROM <subnot-test> {\n"
		    + "?uri a sc:Supplier . ?uri sc:name ?name }";

	    TupleQuery tupleQuery = con.prepareTupleQuery(QueryLanguage.SPARQL,
		    suppliers_query);

	    TupleQueryResult result = tupleQuery.evaluate();
	    try {
		while (result.hasNext()) { // iterate over the result
		    BindingSet bindingSet = result.next();
		    Value name = bindingSet.getValue("name");

		    // String uri_base_name =
		    // uri.stringValue().replace("http://www.xybermotive.com/supplier/",
		    // "");

		    String uri = name.stringValue().replace(" ", " ");
		    String filename = dir + File.separator
			    + "received_order_from_" + uri + ".ttl";
		    LOGGER.info("Creating file " + filename);
		    String q = received_order_from.replace("REPLACE_URI",
			    "received_order_from_" + uri).replace(
			    "REPLACE_NAME", name.stringValue());
		    FileUtils.writeStringToFile(new File(filename), namespaces
			    + q + notifier);
		}
	    } finally {
		result.close();
	    }

	    String products_query = "PREFIX sc:<http://www.xybermotive.com/ontology/>\n"
		    + "select distinct ?uri ?name FROM <subnot-test> {\n"
		    + "?uri a sc:Product . ?uri sc:name ?name }";

	    tupleQuery = con.prepareTupleQuery(QueryLanguage.SPARQL,
		    products_query);

	    result = tupleQuery.evaluate();
	    try {
		while (result.hasNext()) { // iterate over the result
		    BindingSet bindingSet = result.next();
		    Value name = bindingSet.getValue("name");

		    // String uri_base_name =
		    // uri.stringValue().replace("http://www.xybermotive.com/product/",
		    // "");

		    String uri = name.stringValue().replace(" ", " ");

		    LOGGER.info("Creating file " + dir + File.separator
			    + "received_product_" + uri);
		    String q = received_product.replace("REPLACE_URI",
			    "received_product_" + uri).replace("REPLACE_NAME",
			    name.stringValue());
		    FileUtils.writeStringToFile(new File(dir + File.separator
			    + "received_shipping_" + uri + ".ttl"), namespaces
			    + q + notifier);

		    LOGGER.info("Creating file " + dir + File.separator
			    + "product_arrived_late_" + uri);
		    q = product_arrived_late.replace("REPLACE_URI",
			    "product_arrived_late_" + uri).replace(
			    "REPLACE_NAME", name.stringValue());
		    FileUtils.writeStringToFile(new File(dir + File.separator
			    + "product_arrived_late_" + uri + ".ttl"),
			    namespaces + q + notifier);

		    LOGGER.info("Creating file " + dir + File.separator
			    + "new_supplier_product_" + uri);
		    q = new_supplier_product.replace("REPLACE_URI",
			    "new_supplier_product_" + uri).replace(
			    "REPLACE_NAME", name.stringValue());
		    FileUtils.writeStringToFile(new File(dir + File.separator
			    + "new_supplier_product_" + uri + ".ttl"),
			    namespaces + q + notifier);

		}
	    } finally {
		result.close();
	    }

	} catch (Exception e) {
	    LOGGER.error("couldnt create queries", e);
	}
    }
}
