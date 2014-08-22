package test;

import static org.junit.Assert.*;

import java.io.IOException;
import java.io.InputStream;
import java.util.Properties;

import org.junit.Test;

public class TestProperty {
	   static Properties prop;
	@Test
	public void test() {
		/*prop = new Properties();
		InputStream in = TestProperty.class.getResourceAsStream("/RLParameters.properties");
		try {
			prop.load(in);
			System.out.println("Properties");
			System.out.println(prop.get("arg1"));
			System.out.println(prop.get("arg2"));
			in.close();
		} catch (IOException e) {
			e.printStackTrace();
		}*/
		TestProperties.loadProperties("/RLParameters.properties");
	    /*assertNotNull(p);
	    assertFalse(p.isEmpty());
	    for (final Entry<Object, Object> e : p.entrySet()) {
	        assertEquals(expectedProperties.get(e.getKey()), e.getValue());
	    }*/

	}
}
