package controllers;

import play.mvc.Controller;
import play.cache.Cache;

import java.io.File;
import java.util.List;
import java.util.Arrays;

import models.Salesforce;
import models.SObjectGen;

import jp.co.flect.io.FileInputStreamWithDelete;
import jp.co.flect.net.OAuth2;
import jp.co.flect.net.OAuthResponse;
import jp.co.flect.salesforce.SalesforceClient;
import jp.co.flect.salesforce.SObjectDef;

public class OAuth extends Controller {
	
	private static final String AUTH_URL = "https://login.salesforce.com/services/oauth2/authorize";
	private static final String TOKEN_URL = "https://login.salesforce.com/services/oauth2/token";
	
	private static final String SALESFORCE_APPID = System.getenv("SALESFORCE_APPID");
	private static final String SALESFORCE_SECRET = System.getenv("SALESFORCE_SECRET");
	
	private static OAuth2 createOAuth2(String baseUrl) {
		if (baseUrl.indexOf("localhost") == -1 && baseUrl.startsWith("http://")) {
			baseUrl = "https://" + baseUrl.substring(7);
		}
		String redirectUrl = baseUrl + "/login";
		return new OAuth2(
			AUTH_URL,
			TOKEN_URL,
			SALESFORCE_APPID,
			SALESFORCE_SECRET,
			redirectUrl
		);
	}
	
	public static void oauth() throws Exception {
		String token = (String)Cache.get(session.getId() + "-oauth-token");
		String endpoint = (String)Cache.get(session.getId() + "-oauth-endpoint");
		if (token == null || endpoint == null) {
			OAuth2 oauth = createOAuth2(request.getBase());
			redirect(oauth.getLoginUrl());
		}
		SalesforceClient client = new SalesforceClient(Salesforce.getWSDL());
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
		SalesforceClient client = new SalesforceClient(Salesforce.getWSDL());
		client.login(res);
		Cache.set(session.getId() + "-oauth-token", client.getSessionId(), "1h");
		Cache.set(session.getId() + "-oauth-endpoint", client.getEndpoint(), "1h");
		oauth();
	}
	
	public static void genJava(String pkg, String[] obj) throws Exception {
		if (pkg == null || obj == null || obj.length == 0) {
			badRequest();
		}
		String token = (String)Cache.get(session.getId() + "-oauth-token");
		String endpoint = (String)Cache.get(session.getId() + "-oauth-endpoint");
		if (token == null || endpoint == null) {
			badRequest();
		}
		SalesforceClient client = new SalesforceClient(Salesforce.getWSDL());
		client.setSessionId(token);
		client.setEndpoint(endpoint);
		List<SObjectDef> list = client.describeSObjects(Arrays.asList(obj));
		
		File outputDir = File.createTempFile("tmp", "");
		outputDir.delete();
		outputDir.mkdirs();
		
		SObjectGen gen = new SObjectGen(pkg, outputDir);
		try {
			for (SObjectDef objectDef : list) {
				gen.generate(objectDef);
			}
			File zip = gen.getZip();
			renderBinary(new FileInputStreamWithDelete(zip), "sobjects.zip", zip.length());
		} finally {
			gen.cleanup();
		}
	}
	
}
