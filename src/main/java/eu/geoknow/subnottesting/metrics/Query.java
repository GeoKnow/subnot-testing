package eu.geoknow.subnottesting.metrics;

/**
 * This class is to measure the query response time
 * 
 * @author alejandragarciarojas
 * 
 */
public class Query {

  private long start_time;
  private long end_time;
  private String sparql_query;

  public Query(String sparql_query) {
    this.sparql_query = sparql_query;
  }

  public String getQuery() {
    return this.sparql_query;
  }

  public long getRunTime() {
    return end_time - start_time;
  }

  public long getStartTime() {
    return start_time;
  }

  public void setStartTime(long start_time) {
    this.start_time = start_time;
  }

  public long getEndTime() {
    return end_time;
  }

  public void setEndTime(long end_time) {
    this.end_time = end_time;
  }

}