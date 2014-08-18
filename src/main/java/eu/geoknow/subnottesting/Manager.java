package eu.geoknow.subnottesting;


public class Manager {

    private static boolean testing = false;

    private static Manager instance;

    public Manager getInstance() {
	if (instance == null)
	    instance = new Manager();
	return instance;
    }

    public static boolean isTesting() {
	return testing;
    }

    public static void setTesting(boolean testing) {
	Manager.testing = testing;
    }

}
