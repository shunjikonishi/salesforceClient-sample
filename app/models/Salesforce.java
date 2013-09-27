package models;

import jp.co.flect.salesforce.SalesforceClient;

public class Salesforce {
	
	private static final String USERNAME = System.getenv("SALESFORCE_USERNAME");
	private static final String PASSWORD = System.getenv("SALESFORCE_PASSWORD");
	private static final String TOKEN    = System.getenv("SALESFORCE_TOKEN");
	
	private static SalesforceClient BASE_CLIENT;
	
	public static synchronized createClient() {
		if (BASE_CLIENT == null) {
		}
		SalesforceClient client = new SalesforceClient(BASE_CLIENT);
	}
}
