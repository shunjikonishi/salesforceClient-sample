package models;

import jp.co.flect.salesforce.SObject;
import jp.co.flect.salesforce.SObjectDef;
import jp.co.flect.salesforce.Metadata;
import java.util.Date;

/**
 * アプリで使用する個別オブジェクトは必要に応じてサブクラスを作成する
 */
public class Account extends SObject {
	
	public Account(Metadata meta) {
		super(meta);
	}
	public Account(SObjectDef objectDef) {
		super(objectDef);
	}
	
	public String getName() { return getString("Name");}
	public void setName(String s) { set("Name", s);}
	
	public String getFax() { return getString("Fax");}
	public void setFax(String s) { set("Fax", s);}
	
	public String getPhone() { return getString("Phone");}
	public void setPhone(String s) { set("Phone", s);}
	
	public Date getLastActivityDate() { return getDate("LastActivityDate");}
	public void setLastActivityDate(Date d) { set("LastActivityDate", d);}
	
	public Date getCreatedDate() { return getDate("CreatedDate");}
	
	public String getCreatedByName() { 
		SObject obj = getObject("CreatedBy");
		return obj == null ? null : obj.getString("Name");
	}
	
	public String getExId() { return getString("ExID__c");}
	public void setExId(String s) { set("ExID__c", s);}
	
	//ToDo 以下必要なフィールドの定義
}