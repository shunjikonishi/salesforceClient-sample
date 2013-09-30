package samples;

import jp.co.flect.salesforce.SalesforceClient;
import jp.co.flect.salesforce.SObject;
import jp.co.flect.salesforce.query.QueryResult;
import jp.co.flect.salesforce.update.SaveResult;
import jp.co.flect.soap.SoapException;
import java.io.IOException;
import java.util.UUID;
import java.util.List;
import java.util.ArrayList;
import models.Account;

public class CreateSample {
	
	private SalesforceClient client;
	
	public CreateSample(SalesforceClient client) {
		this.client = client;
	}
	
	/**
	 * シンプルなCREATEのサンプル
	 */
	public Account simpleCreate(String name) throws IOException, SoapException {
		Account account = (Account)client.newObject("Account");
		account.setName(name);
		SaveResult result = client.create(account);
		if (result.isSuccess()) {
			String query =String.format("select id, name, createdDate, createdBy.Name from account where id= '%s'", result.getId());
			return client.query(query, Account.class).getRecords().get(0);
		} else {
			throw result.getError(0);
		}
	}
	
	/**
	 * シンプルなCREATEのサンプル
	 */
	public String parentAndChild(String name) throws IOException, SoapException {
		String exid = UUID.randomUUID().toString();
		Account account = (Account)client.newObject("Account");
		account.setName(name);
		account.setExId(exid);
		
		List<SObject> list = new ArrayList<SObject>();
		list.add(account);
		for (int i=0; i<5; i++) {
			SObject child = client.newObject("Contact");
			child.set("LastName", name + " Child-" + (i+1));
			child.set("Account", account);
			list.add(child);
		}
		List<SaveResult> results = client.create(list);
		for (SaveResult ret : results) {
			if (!ret.isSuccess()) {
				throw ret.getError(0);
			}
		}
		return results.get(0).getId();
	}
}
