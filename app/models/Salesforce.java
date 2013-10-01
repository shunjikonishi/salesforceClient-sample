package models;

import jp.co.flect.salesforce.SalesforceClient;
import jp.co.flect.salesforce.Metadata;
import jp.co.flect.soap.InvalidWSDLException;
import jp.co.flect.soap.SoapException;
import jp.co.flect.soap.WSDL;
import jp.co.flect.xmlschema.XMLSchemaException;
import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import org.xml.sax.SAXException;

public class Salesforce {
	
	static {
		//Metadataにこのオブジェクトを登録
		Metadata.registerClass("Account", Account.class);
	}
	
	private static final String USERNAME = System.getenv("SALESFORCE_USERNAME");
	private static final String PASSWORD = System.getenv("SALESFORCE_PASSWORD");
	private static final String TOKEN    = System.getenv("SALESFORCE_TOKEN");
	
	private static long LOGIN_TIME = 0;
	private static SalesforceClient BASE_CLIENT;
	
	public static synchronized WSDL getWSDL() {
		if (BASE_CLIENT == null) {
			try {
				createClient();
			} catch (IOException e) {
				//ignore
				e.printStackTrace();
			} catch (SoapException e) {
				//ignore
				e.printStackTrace();
			}
		}
		return BASE_CLIENT.getWSDL();
	}
	
	public static synchronized SalesforceClient createClient() throws IOException, SoapException {
		if (BASE_CLIENT == null) {
			try {
				BASE_CLIENT = new SalesforceClient(new File("conf/partner.wsdl"));
			} catch (XMLSchemaException e) {
				//not occur
				throw new IllegalStateException(e);
			} catch (InvalidWSDLException e) {
				//not occur
				throw new IllegalStateException(e);
			} catch (SAXException e) {
				//not occur
				throw new IllegalStateException(e);
			}
			LOGIN_TIME = System.currentTimeMillis();
			BASE_CLIENT.login(USERNAME, PASSWORD, TOKEN);
			
			loadMetadata(BASE_CLIENT);
			//ToDo collectMetadata
		}
		long t = System.currentTimeMillis();
		if (t - LOGIN_TIME > BASE_CLIENT.getSessionLifetime()) {
			BASE_CLIENT.login(USERNAME, PASSWORD, TOKEN);
			LOGIN_TIME = t;
		}
		SalesforceClient client = new SalesforceClient(BASE_CLIENT);
		return client;
	}
	
	private static void loadMetadata(SalesforceClient client) throws IOException, SoapException {
		List<String> list = new ArrayList<String>();
		list.add("Account");
		list.add("Contact");
		client.describeSObjects(list);
	}
}
