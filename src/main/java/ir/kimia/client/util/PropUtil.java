package ir.kimia.client.util;

import org.apache.commons.configuration.ConfigurationException;
import org.apache.commons.configuration.PropertiesConfiguration;
import org.apache.log4j.Logger;

/**
 * Utility for reading properties file.
 *
 * @author Amir
 */
public class PropUtil {

    private static Logger log = Logger.getLogger(PropUtil.class.getName());


    private static PropertiesConfiguration propertiesConfiguration;

    public static PropertiesConfiguration getConfig() {
        if(propertiesConfiguration == null) {
            try {
                propertiesConfiguration = new PropertiesConfiguration("config.properties");
            } catch (ConfigurationException e) {
                log.error(e.getMessage());
            }
        }
        return propertiesConfiguration;
    }

    public static String getString(String key) {
        getConfig();
        return propertiesConfiguration.getString(key);
    }

}
