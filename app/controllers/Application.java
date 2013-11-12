package controllers;

import play.mvc.Controller;
import models.Salesforce;
import models.Account;
import jp.co.flect.salesforce.SalesforceClient;
import jp.co.flect.salesforce.SObject;
import jp.co.flect.salesforce.SObjectDef;
import jp.co.flect.salesforce.query.QueryResult;

import samples.QuerySample;
import samples.CreateSample;

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
	
    public static void sobject(String id) throws Exception {
		Account account = null;
		if (id != null && id.length() > 0) {
			SalesforceClient client = Salesforce.createClient();
			QuerySample sample = new QuerySample(client);
			QueryResult<Account> result = sample.useCustomClass(id);
			if (result.getAllSize() > 0) {
				account = result.getRecords().get(0);
			}
		}
        render(id, account);
    }
    
    public static void create(String name) throws Exception {
		Account account = null;
		if (name != null && name.length() > 0) {
			SalesforceClient client = Salesforce.createClient();
			CreateSample sample = new CreateSample(client);
			account = sample.simpleCreate(name);
		}
        render(name, account);
    }
    
    public static void createParentAndChild(String name) throws Exception {
		if (name == null || name.length() == 0) {
			badRequest();
		}
		SalesforceClient client = Salesforce.createClient();
		CreateSample sample = new CreateSample(client);
		String id = sample.parentAndChild(name);
		
		SObjectDef objDef = client.getMetadata().getObjectDef("Account");
		if (objDef == null) {
			//最初にdescribeObjectを実行しているか、create等の更新メソッドでAccountを扱っていれば発生しない
			throw new IllegalStateException();
		}
		redirect(objDef.getString("urlDetail").replaceAll("\\{ID\\}", id));
	}
	
	public static void update() {
		render();
	}
	
	public static void upsert() {
		render();
	}
	
	public static void delete() {
		render();
	}
	
	public static void sqllike() {
		render();
	}
	
	public static void importAndExport() {
		render();
	}
	
	public static void sqlsync() {
		render();
	}
	
	public static void sobjectsync() {
		render();
	}
	
	public static void fixture() {
		render();
	}
	
	public static void excelReport() {
		render();
	}
	
	public static void other() {
		render();
	}
	
	public static void todo() {
		render();
	}

}