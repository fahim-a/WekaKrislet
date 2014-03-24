import java.io.IOException;
import java.io.InputStream;
import java.util.HashMap;
import java.util.Map.Entry;
import java.util.Properties;
import java.util.logging.Level;
import java.util.logging.Logger;

public class Property {
    private final static Logger     LOGGER     = Logger.getLogger(Property.class.getName());

    private static Property         instance   = null;
    private HashMap<String, String> properties = null;
    Properties                      prop;

    protected Property() {
        prop = new Properties();
        InputStream input = null;
        try {
            InputStream in = getClass().getResourceAsStream("config.properties");
            prop.load(in);
            properties = new HashMap<String, String>();
            for (Entry<Object, Object> e : prop.entrySet()) {
                LOGGER.log(Level.FINE, e.getKey() + " VALUE: " + e.getValue());
                properties.put((String) e.getKey(), (String) e.getValue());
            }
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