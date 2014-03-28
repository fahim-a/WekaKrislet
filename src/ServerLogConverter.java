import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.util.logging.Level;
import java.util.logging.Logger;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class ServerLogConverter {
    private final static Logger LOGGER           = Logger.getLogger(ServerLogConverter.class.getName());

    private static Pattern      action_msg       = Pattern
                                                         .compile("^\\((kick|dash|turn|turn_neck|move|say|change_view|bye)\\s+.*");
    private String[]            attributes       = Property.getInstance().getProperty("attributes").split(";");
    private String[]            attributes_types = Property.getInstance().getProperty("attributes_types").split(";");
    private String              team             = Property.getInstance().getProperty("player_team");
    private char                side             = Property.getInstance().getProperty("player_side").charAt(0);

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
        DataInstance di = null;

        while ((message = reader.readLine()) != null) {
            if (message.startsWith("(see")) {
                di = new DataInstance(new VisualInfo(message), team, side);
            } else if (di != null) {
                Matcher m = action_msg.matcher(message);
                if (m.find()) {
                    di.setAction(m.group(1));
                    String arffMessage = di.toString();
                    writer.write(arffMessage);
                    LOGGER.log(Level.FINEST, di.getMsg_number() + " - " + arffMessage);
                    di = null;
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