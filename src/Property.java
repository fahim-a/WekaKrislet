
import java.io.*;
import java.util.*;
import java.util.Map.Entry;

public class Property {

	private static Property instance = null;
	private HashMap<String, String> properties = null;
	Properties prop;

	protected Property() {
		prop = new Properties();
		InputStream input = null;
		try {

			InputStream in = getClass()
					.getResourceAsStream("config.properties");
			prop.load(in);
			properties = new HashMap<String, String>();
			for (Entry<Object, Object> e : prop.entrySet()) {
				// System.out.println(e.getKey() + " VALUE: " + e.getValue());
				properties.put((String) e.getKey(), (String) e.getValue());
			}
			in.close();
		} catch (IOException ex) {
			ex.printStackTrace();
		} finally {
			if (input != null) {
				try {
					input.close();
				} catch (IOException e) {
					e.printStackTrace();
				}
			}
		}
	}

	public static Property getInstance() {
		if (instance == null) {
			instance = new Property();
		}
		return instance;
	}

	public String getProperty(String propertyName) {
		return properties.get(propertyName);
	}
}