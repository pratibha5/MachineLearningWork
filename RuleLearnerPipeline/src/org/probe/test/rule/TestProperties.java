package org.probe.test.rule;

import java.io.IOException;
import java.io.InputStream;
import java.util.Properties;

public class TestProperties{  
	 
	   static Properties prop;
	   public static String[] loadProperties(String propFile) {
		   prop = new Properties();
		   String params[] = null;
		   InputStream in = TestProperties.class
				   .getResourceAsStream(propFile);
		   try {
			   prop.load(in);
			   System.out.println("Properties");
			   params = new String[prop.size()];
			   for (int i=1; i<=prop.size(); i++){
				  // System.out.println(prop.get("arg"+i));
				   params[i-1] = (String) prop.get("arg"+i);
			   }
			   
			   in.close();
		   } catch (IOException e) {
			   e.printStackTrace();
		   }
		   return params;
	   }
}