package gov.healthit.chpl.manager.rules;

import java.io.FileNotFoundException;
import java.io.InputStream;
import java.util.List;
import java.util.Properties;

public abstract class ValidationRule<T> {
    private static final String DEFAULT_PROPERTIES_FILE = "errors.properties";
    //private List<String> messages = new ArrayList<String>();
    private Properties props;

    public ValidationRule() {
        try {
            InputStream in = ValidationRule.class.getClassLoader().getResourceAsStream(DEFAULT_PROPERTIES_FILE);
            if (in == null) {
                props = null;
                throw new FileNotFoundException("Error Properties File not found in class path.");
            } else {
                props = new Properties();
                props.load(in);
                in.close();
            }
        } catch (Exception e) {

        }
    }

    protected String getErrorMessage(String key) {
        return props.getProperty(key);
    }

    //public List<String> getMessages() {
    //    return messages;
    //}

    public abstract List<String> getErrorMessages(T object);
}
