import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.util.logging.Level;
import java.util.logging.Logger;

public class LogConverter {
    private final static Logger LOGGER           = Logger.getLogger(LogConverter.class.getName());

    private String[]            actions          = Property.getInstance().getProperty("actions").split(";");
    private String[]            attributes       = Property.getInstance().getProperty("attributes").split(";");
    private String[]            attributes_types = Property.getInstance().getProperty("attributes_types").split(";");

    /**
     * This method converts the log file to an ARFF file. The log file contains
     * the messages exchanged between player/agent and the server.
     * 
     * @param logFile
     * @param wekaOutputFile
     * @throws IOException
     */
    public void createWekaData(String logFile, String wekaOutputFile) throws IOException {

        BufferedReader reader = new BufferedReader(new FileReader(logFile));
        BufferedWriter writer = new BufferedWriter(new FileWriter(wekaOutputFile));

        try {
            String header = this.getARFFHeader();
            writer.write(header);
        } catch (Exception e) {
            LOGGER.log(Level.SEVERE, "Encountered retrieving ARFF header information", e);
        }

        String message = null;
        DataInstances di = null;

        while ((message = reader.readLine()) != null) {
            if (message.startsWith("(see")) {

                di = new DataInstances(new VisualInfo(message));
            } else if (di != null) {
                for (String ac : this.actions) {
                    if (message.startsWith("(" + ac)) {
                        di.setAction(message);
                        writer.write(di.getARFFmessage());
                        LOGGER.log(Level.FINE, di.getMsg_number() + " - " + di.getARFFmessage());
                        di = null;
                        break;
                    }
                }
            }
        }
        reader.close();
        writer.close();
    }

    /**
     * Based on the attributes and their types as specified in the properties
     * file, this method creates the header required by the ARFF structure.
     * 
     * @return
     * @throws Exception
     */
    private String getARFFHeader() throws Exception {
        if (attributes.length != attributes_types.length)
            throw new Exception("Attributes and their types do not match");

        StringBuilder sb = new StringBuilder();
        sb.append("@RELATION " + Property.getInstance().getProperty("weka_relation_name"));
        sb.append("\n");
        sb.append("\n");

        for (int i = 0; i < attributes.length; i++) {
            sb.append("@ATTRIBUTE " + attributes[i] + " " + attributes_types[i]);
            sb.append("\n");
        }
        sb.append("\n");
        sb.append("@DATA");
        sb.append("\n");

        return sb.toString();
    }
}