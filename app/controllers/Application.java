package controllers;

import play.mvc.Controller;
import play.cache.Cache;
import models.Salesforce;
import models.Account;
import jp.co.flect.salesforce.SalesforceClient;
import jp.co.flect.salesforce.SObject;
import jp.co.flect.salesforce.SObjectDef;
import jp.co.flect.salesforce.query.QueryResult;
import jp.co.flect.net.OAuth2;
import jp.co.flect.net.OAuthResponse;
import java.util.List;

import samples.QuerySample;
import samples.CreateSample;

public class Application extends Controller {

	private static final String AUTH_URL = "https://login.salesforce.com/services/oauth2/authorize";
	private static final String TOKEN_URL = "https://login.salesforce.com/services/oauth2/token";
	
	private static OAuth2 createOAuth2(String baseUrl) {
		if (baseUrl.indexOf("localhost") == -1 && baseUrl.startsWith("http://")) {
			baseUrl = "https://" + baseUrl.substring(7);
		}
		String redirectUrl = baseUrl + "/login";
		String authUrl = AUTH_URL;
		String tokenUrl = TOKEN_URL;
System.out.println("Redirect: " + redirectUrl);
		return new OAuth2(
			authUrl,
			tokenUrl,
			System.getenv("SALESFORCE_APPID"),
			System.getenv("SALESFORCE_SECRET"),
			redirectUrl
		);
	}
	
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
	
	public static void oauth() throws Exception {
		String token = (String)Cache.get(session.getId() + "-oauth-token");
		String endpoint = (String)Cache.get(session.getId() + "-oauth-endpoint");
		if (token == null || endpoint == null) {
			OAuth2 oauth = createOAuth2(request.getBase());
			redirect(oauth.getLoginUrl());
		}
		SalesforceClient client = Salesforce.createClient();
		client.setSessionId(token);
		client.setEndpoint(endpoint);
		List<SObjectDef> list = client.describeGlobal().getObjectDefList();
		render(list);
	}
	
	public static void login(String code) throws Exception {
		if (code == null) {
			badRequest();
		}
		OAuth2 oauth = createOAuth2(request.getBase());
		OAuthResponse res = oauth.authenticate(code);
		SalesforceClient client = Salesforce.createClient();
		client.login(res);
		Cache.set(session.getId() + "-oauth-token", client.getSessionId(), "1h");
		Cache.set(session.getId() + "-oauth-endpoint", client.getEndpoint(), "1h");
		oauth();
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