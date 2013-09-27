package controllers;

import play.mvc.Controller;
import models.Salesforce;
import samples.QuerySample;
import jp.co.flect.salesforce.SalesforceClient;
import jp.co.flect.salesforce.SObject;
import jp.co.flect.salesforce.query.QueryResult;

public class Application extends Controller {

    public static void index() {
        render();
    }
    
    public static void instance() {
        render();
    }
    
    public static void query(String name) throws Exception {
		SalesforceClient client = Salesforce.createClient();
		QuerySample sample = new QuerySample(client);
		QueryResult<SObject> result = sample.simpleQuery(name);
		render(result);
	}
	
    public static void sobject() {
        render();
    }

}