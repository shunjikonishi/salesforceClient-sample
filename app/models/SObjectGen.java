package models;

import play.exceptions.TemplateNotFoundException;
import play.templates.Template;
import play.templates.TemplateLoader;

import java.io.File;
import java.io.FilenameFilter;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.HashMap;
import java.util.Map;
import java.util.HashSet;
import java.util.Set;
import java.util.List;
import java.util.ArrayList;
import java.util.zip.ZipOutputStream;
import java.util.zip.ZipEntry;

import jp.co.flect.salesforce.SObjectDef;
import jp.co.flect.salesforce.FieldDef;
import jp.co.flect.salesforce.RelationDef;
import jp.co.flect.io.FileUtils;

public class SObjectGen {
	
	private static Set<String> ignoreFields = new HashSet<String>();
	
	static {
		ignoreFields.add("Id".toLowerCase());
		ignoreFields.add("Name".toLowerCase());
		ignoreFields.add("CreatedById".toLowerCase());
		ignoreFields.add("CreatedDate".toLowerCase());
		ignoreFields.add("IsDeleted".toLowerCase());
		ignoreFields.add("LastModifiedById".toLowerCase());
		ignoreFields.add("LastModifiedDate".toLowerCase());
		ignoreFields.add("OwnerId".toLowerCase());
		ignoreFields.add("SystemModstamp".toLowerCase());
	}
	
	private String pkg;
	private File outputDir;
	
	public SObjectGen(String pkg, File outputDir) {
		this.pkg = pkg;
		this.outputDir = outputDir;
	}
	
	public void cleanup() {
		FileUtils.deleteRecursive(outputDir);
	}
	
	public File getZip() throws IOException {
		File f = File.createTempFile("tmp", ".zip");
		FileOutputStream fos = new FileOutputStream(f);
		try {
			ZipOutputStream zos = new ZipOutputStream(fos);
			try {
				File[] javaFiles = this.outputDir.listFiles(new FilenameFilter() {
					public boolean accept(File f, String name) {
						return name.endsWith(".java");
					}
				});
				for (File jf : javaFiles) {
					zos.putNextEntry(new ZipEntry(jf.getName()));
					try {
						byte[] data = FileUtils.readFile(jf);
						zos.write(data, 0, data.length);
					} finally {
						zos.closeEntry();
					}
				}
			} finally {
				zos.close();
			}
		} finally {
			fos.close();
		}
		return f;
	}
	
	public void generate(SObjectDef objectDef) throws IOException {
		try {
			Template template = TemplateLoader.load("java/SObject.template");
			Wrapper obj = new Wrapper(objectDef);
			Map<String, Object> map = new HashMap<String, Object>();
			map.put("pkg", this.pkg);
			map.put("obj", obj);
			
			String ret = template.render(map);
			FileUtils.writeFile(new File(this.outputDir, obj.getClassName() + ".java"), ret.getBytes("utf-8"));
		} catch (TemplateNotFoundException e) {
			throw new IllegalStateException(e);
		}
	}
	
	private static class Wrapper {
		
		private SObjectDef objectDef;
		
		public Wrapper(SObjectDef objectDef) {
			this.objectDef = objectDef;
		}
		
		public String getClassName() {
			String name = objectDef.getName();
			if (name.endsWith("__c")) {
				name = name.substring(0, name.length() - 3);
			}
			return name;
		}
		
		public List<FieldDef> getFieldList() {
			List<FieldDef> list = new ArrayList<FieldDef>();
			for (FieldDef f : objectDef.getFieldList()) {
				if (!ignoreFields.contains(f.getName().toLowerCase())) {
					list.add(f);
				}
			}
			return list;
		}
		
		public String getJavaType(FieldDef f) {
			String type = f.getSoapType().getName();
			if (type.equals("string")) return "String";
			if (type.equals("ID")) return "String";
			
			if (type.equals("boolean")) return "boolean";
			
			if (type.equals("date")) return "Date";
			if (type.equals("dateTime")) return "Date";
			if (type.equals("time")) return "Date";
			
			if (type.equals("int")) return "int";
			if (type.equals("double")) return "double";
			
			return "Object";
		}
		
		public String getMethodName(FieldDef f, boolean bGet) {
			String name = f.getName();
			if (name.endsWith("__c")) {
				name = name.substring(0, name.length() - 3);
			}
			String type = getJavaType(f);
			if (type.equals("boolean")) {
				if (name.startsWith("Is")) {
					name = name.substring(2);
				}
				return bGet ? "is" + name : "set" + name;
			} else {
				if (name.equals("Type")) {
					name = "TypeEx";
				}
				if (f.getRelationshipName() != null && 
				    f.getRelationshipName().endsWith("__r") && 
				    f.getRelationshipName().substring(0, f.getRelationshipName().length() - 3).equals(name)) {
					name += "Id";
				}
				return bGet ? "get" + name : "set" + name;
			}
		}
		
		public boolean isSingleRelation(FieldDef f) {
			return f.getRelationshipName() != null && f.getReferenceToName() != null;
		}
		
		public List<RelationDef> getRelationList() {
			List<RelationDef> list = new ArrayList<RelationDef>();
			for (RelationDef r : objectDef.getRelationList()) {
				if (r.getRelationshipName() != null) {
					list.add(r);
				}
			}
			return list;
		}
		
	}
}