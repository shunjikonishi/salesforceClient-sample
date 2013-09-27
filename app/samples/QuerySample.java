package samples;

import jp.co.flect.salesforce.SalesforceClient;
import jp.co.flect.salesforce.SObject;
import jp.co.flect.salesforce.query.QueryResult;
import jp.co.flect.soap.SoapException;
import java.io.IOException;

public class QuerySample {
	
	private SalesforceClient client;
	
	public QuerySample(SalesforceClient client) {
		this.client = client;
	}
	
	public QueryResult<SObject> simpleQuery(String name) throws IOException, SoapException {
		String query = "select id, name, (select id, name from Contacts), createdBy.name, createdDate from account";
		if (name != null && name.length() > 0) {
			query += " where name like '%" + name + "%'";
		} else {
			query += " limit 5";
		}
		QueryResult<SObject> ret = client.query(query);
		return ret;
	}
}
