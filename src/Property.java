import java.io.IOException;
import java.io.InputStream;
import java.util.HashMap;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Properties;
import java.util.logging.Level;
import java.util.logging.Logger;

public class Property {
    private static final Logger LOGGER             = Logger.getLogger(Property.class.getName());
    private static final String PROPERTY_FILE_NAME = "config.properties";

    private static Property     instance           = null;
    private Map<String, String> properties         = null;
    private Properties          prop;

    private Property() {
        prop = new Properties();
        InputStream input = null;
        try {
            InputStream in = getClass().getResourceAsStream(PROPERTY_FILE_NAME);
            prop.load(in);
            properties = new HashMap<String, String>();
            StringBuilder sb = new StringBuilder("Loaded properties file:\n");
            for (Entry<Object, Object> e : prop.entrySet()) {
                sb.append("\t" + e.getKey() + " VALUE: " + e.getValue() + "\n");
                properties.put((String) e.getKey(), (String) e.getValue());
            }
            LOGGER.log(Level.FINE, sb.toString());
            in.close();
        } catch (IOException ex) {
            LOGGER.log(Level.SEVERE, "Encountered error loading properties file", ex);
        } finally {
            if (input != null) {
                try {
                    input.close();
                } catch (IOException e) {
                    LOGGER.log(Level.SEVERE, "Encountered error closing properties file", e);
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