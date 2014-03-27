import java.util.Enumeration;
import java.util.logging.Level;
import java.util.logging.Logger;

import weka.core.Attribute;
import weka.core.Instance;

/**
 * Note: Ensure the class attributes are defined in the exact same order in the
 * properties file
 * 
 */
public class PerceivedEnvironment {
    private final static Logger LOGGER = Logger.getLogger(Brain.class.getName());

    public static Instance buildWekaInstance(String envString, Instance sampleInstance) throws Exception {
        if (envString == null || envString.isEmpty())
            throw new IllegalArgumentException("Environment string was not specified!");

        if (sampleInstance == null)
            throw new IllegalArgumentException("A sample instance was not specified!");

        Instance si = new Instance(sampleInstance);
        si.setDataset(sampleInstance.dataset());
        for (int num = 0; num < si.numAttributes(); num++) {
            si.setMissing(num);
        }

        String[] split = envString.split(",");

        if (si.numAttributes() != split.length)
            throw new IllegalArgumentException("Number of environment states does not match the defined Weka Instance");

        @SuppressWarnings("unchecked")
        Enumeration<Attribute> iter = si.enumerateAttributes();

        int i = 0;
        while (iter.hasMoreElements()) {
            Attribute attr = iter.nextElement();

            String s = split[i];
            if (attr.isNumeric()) {
                double numericValue = -1;
                try {
                    numericValue = Double.parseDouble(s);
                } catch (NumberFormatException | NullPointerException e) {
                }

                if (numericValue != -1)
                    si.setValue(i, numericValue);
                else
                    si.setMissing(i);
            } else if (attr.isNominal()) {
                String attrString = attr.toString();
                String parsedString = attrString.substring(attrString.indexOf('{') + 1, attrString.indexOf('}'));
                String[] values = parsedString.split("\\,");

                // iterate over possible values to determine proper index
                int index = -1;
                if (values != null && values.length > 0) {
                    for (int j = 0; j < values.length; j++) {
                        String value = values[j];
                        if (value.equals(s))
                            index = j;
                    }
                }

                if (index != -1)
                    si.setValue(i, index);
                else
                    si.setMissing(i);
            } else {
                LOGGER.log(Level.INFO, "Encountered an unknown attribute type" + attr);
                si.setMissing(i);
            }

            i++;
        }

        return si;
    }
}