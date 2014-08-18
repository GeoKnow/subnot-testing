package eu.geoknow.subnottesting.metrics;

import java.util.ArrayList;
import java.util.List;

/**
 * A class for having collections of queries of a given test and compute the
 * average query response time
 * 
 * @author alejandragarciarojas
 * 
 */
public class Queries {

    private static List<Query> list;
    private static Queries instance = null;

    public static synchronized Queries getInstance() {
	if (instance == null) {
	    instance = new Queries();
	    list = new ArrayList<Query>();
	}
	return instance;
    }

    public synchronized List<Query> getQueries() {
	return list;
    }

    public int getNumberOfQueries() {
	return list.size();
    }

    public long getQueryRunTimeAverage() {
	if (list.isEmpty())
	    return (long) 0;
	long sum = 0;
	for (int index = 0; index < list.size(); index++) {
	    sum += list.get(index).getRunTime();
	}
	return sum / (long) list.size();
    }

    public void addQuery(Query query) {
	list.add(query);
    }

    public long getQueryMaxDuration() {
	long max = 0;

	for (int index = 0; index < list.size(); index++) {
	    if (list.get(index).getRunTime() > max)
		max = list.get(index).getRunTime();
	}

	return max;
    }

    public long getQueryMinDuration() {

	long min = Long.MAX_VALUE;

	for (int index = 0; index < list.size(); index++) {
	    if (list.get(index).getRunTime() < min)
		min = list.get(index).getRunTime();
	}
	return min;
    }

}