package eu.geoknow.subnottesting.metrics;

import java.util.ArrayList;
import java.util.List;

/**
 * A class for having collections of queries of a given test and compute the average query response
 * time
 * 
 * @author alejandragarciarojas
 * 
 */
public class Queries {

  private static List<Query> list;
  private static Queries instance = null;

  public static Queries getInstance() {
    if (instance == null) {
      instance = new Queries();
      list = new ArrayList<Query>();
    }
    return instance;
  }

  public List<Query> getQueries() {
    return list;
  }

  public int getNumberOfQueries() {
    return list.size();
  }

  public long getQueryRunTimeAverage() {
    if (list.isEmpty())
      return 0;
    long sum = 0l;
    for (int index = 0; index < list.size(); index++) {
      sum += list.get(index).getRunTime();
    }
    return sum / list.size();
  }

  public Query addQuery(String query) {
    Query q = new Query(query);
    list.add(q);
    return q;
  }

}