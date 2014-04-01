import java.util.Enumeration;
import java.util.logging.Level;
import java.util.logging.Logger;

import weka.core.Attribute;
import weka.core.Instance;

/**
 * This class consists of a single static method that takes in the current
 * environment state, and builds a Weka compatible {@code Instance}
 * 
 * 
 * @author fahim
 */
public class PerceivedEnvironment {
    private final static Logger LOGGER = Logger.getLogger(Brain.class.getName());

    /**
     * Generates a {@code Instance} given the provided environment state. Note
     * that the classifying index (i.e. the action) is set to be missing! Which
     * makes sense considering it is Weka that will classify this
     * {@code Instance} and fill in the predicted action
     * 
     * @param envString
     *            The current environment string (as denoted by
     *            {@code DataInstance}). The last section of the string (which
     *            denotes the action) is ignored
     * @param sampleInstance
     *            Any random sample {@code Instance} belonging to the generated
     *            ARFF file. We use the sample to merely construct another
     *            {@code Instance} but set all the attirbute fields to be
     *            missing and then fill them in based on {@code envString}
     * @return
     * @throws Exception
     */
    public static Instance buildWekaInstance(String envString, Instance sampleInstance) throws Exception {
        // ensure the environment string is populated
        if (envString == null || envString.isEmpty())
            throw new IllegalArgumentException("Environment string was not specified!");

        // ensure the sample instance we have is indeed valid
        if (sampleInstance == null)
            throw new IllegalArgumentException("A sample instance was not specified!");

        // create a new Instance object and set all the attributes to be
        // initially missing.
        Instance si = new Instance(sampleInstance);
        si.setDataset(sampleInstance.dataset());
        for (int num = 0; num < si.numAttributes(); num++) {
            si.setMissing(num);
        }

        // split the envString on commas, and ensure that it matches the number
        // of attributes in the Instance object
        String[] split = envString.split(",");
        if (si.numAttributes() != split.length)
            throw new IllegalArgumentException("Number of environment states does not match the defined Weka Instance");

        // we need to iterate over the Instance attributes
        @SuppressWarnings("unchecked")
        Enumeration<Attribute> iter = si.enumerateAttributes();

        int i = 0; // index used to keep track of how far we've iterated over
                   // the set
        while (iter.hasMoreElements()) {
            Attribute attr = iter.nextElement();

            // grab the current index attribute
            String s = split[i];

            if (attr.isNumeric()) { // numeric attributes
                double numericValue = -1;
                try {
                    // attempt to parse as Double
                    numericValue = Double.parseDouble(s);
                } catch (NumberFormatException | NullPointerException e) {
                }

                // if parse-able, set value
                if (numericValue != -1)
                    si.setValue(i, numericValue);
                else
                    // mark as missing
                    si.setMissing(i);
            } else if (attr.isNominal()) { // nominal attributes
                // We need to determine the possible set of nominal values.
                // Extract this information from the sample Instance's attribute
                // definition
                String attrString = attr.toString();
                // the String is usually of the form "{value1, value2, ...} so
                // extract the substring inside the braces
                String parsedString = attrString.substring(attrString.indexOf('{') + 1, attrString.indexOf('}'));
                // now split based on commas
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

                // if the value specified is found, set the index in the nominal
                // set
                if (index != -1)
                    si.setValue(i, index);
                else
                    // mark as missing
                    si.setMissing(i);
            } else {
                LOGGER.log(Level.WARNING, "Encountered an unknown attribute type" + attr);
                // default case: treat everything else as missing
                si.setMissing(i);
            }

            i++;
        }

        return si;
    }
}