//
//	File:			Krislet.java
//	Author:		Krzysztof Langner
//	Date:			1997/04/28
//
//********************************************
//      Updated:               2008/03/01
//      By:               Edgar Acosta
//
//********************************************
import java.io.File;
import java.io.IOException;
import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.InetAddress;
import java.net.SocketException;
import java.util.Arrays;
import java.util.logging.Level;
import java.util.logging.Logger;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import weka.classifiers.Classifier;
import weka.classifiers.trees.J48;
import weka.core.Instances;
import weka.core.converters.ConverterUtils.DataSource;
import weka.filters.Filter;
import weka.filters.supervised.attribute.Discretize;

//***************************************************************************
//
//	This is main object class
//
//***************************************************************************
public class Krislet implements SendCommand {
    private static Instances    trainingData;
    private static Classifier   decision_tree;
    private final static Logger LOGGER          = Logger.getLogger(Krislet.class.getName());

    // ===========================================================================
    // Private members
    // class members
    // Socket to communicate with server
    private DatagramSocket      m_socket;
    // Server address
    private InetAddress         m_host;
    // server port
    private int                 m_port;
    // team name
    private String              m_team;
    // input for sensor information
    private SensorInput         m_brain;
    // controls the MainLoop
    private boolean             m_playing;
    private Pattern             message_pattern = Pattern.compile("^\\((\\w+?)\\s.*");
    private Pattern             hear_pattern    = Pattern.compile("^\\(hear\\s(\\w+?)\\s(\\w+?)\\s(.*)\\).*");
    // private Pattern coach_pattern = Pattern.compile("coach");

    // constants
    // Size of socket buffer
    private static final int    MSG_SIZE        = 4096;

    // ===========================================================================
    // Initialization member functions

    // ---------------------------------------------------------------------------
    // The main appllication function.
    // Command line format:
    //
    // krislet [-parameter value]
    //
    // Parameters:
    //
    // host (default "localhost")
    // The host name can either be a machine name, such as "java.sun.com"
    // or a string representing its IP address, such as "206.26.48.100."
    //
    // port (default 6000)
    // Port number for communication with server
    //
    // team (default Kris)
    // Team name. This name can not contain spaces.
    //
    //
    public static void main(String a[]) throws SocketException, IOException {
        // setup the logger
        WekaLogger.setup(Krislet.class.getName());
        LOGGER.log(Level.INFO, "Krislet starting...");

        String hostName = new String("");
        int port = 6000;
        String team = new String("ML_Agent");

        try {
            // First look for parameters
            for (int c = 0; c < a.length; c += 2) {
                if (a[c].compareTo("-host") == 0) {
                    hostName = a[c + 1];
                } else if (a[c].compareTo("-port") == 0) {
                    port = Integer.parseInt(a[c + 1]);
                } else if (a[c].compareTo("-team") == 0) {
                    team = a[c + 1];
                } else {
                    throw new Exception();
                }
            }
        } catch (Exception e) {
            StringBuilder sb = new StringBuilder("");
            sb.append("USAGE: krislet [-parameter value]\n\n");
            sb.append("    Parameters  value        default\n");
            sb.append("   ------------------------------------\n");
            sb.append("    host        host_name    localhost\n");
            sb.append("    port        port_number  6000\n");
            sb.append("    team        team_name    Kris\n");
            sb.append("\n");
            sb.append("    Example:\n");
            sb.append("      krislet -host www.host.com -port 6000 -team Poland\n");
            sb.append("    or\n");
            sb.append("      krislet -host 193.117.005.223\n");

            LOGGER.log(Level.SEVERE, sb.toString(), e);
            return;
        }

        try {
            // fetch the training log file
            String trainingLogFile = Property.getInstance().getProperty("server_log");
            LOGGER.log(Level.INFO, "Reading training server log file: " + trainingLogFile);

            // ensure it's not empty
            if (trainingLogFile == null || trainingLogFile.isEmpty())
                throw new Exception("Training data file could not be found!");

            // ensure file actually exists
            File f = new File(trainingLogFile);
            if (!f.exists()) {
                LOGGER.log(Level.SEVERE, "The specified trainingLog file does not exist!");
            }

            // fetch where we should output the generated WEKA ARFF file
            String wekaFilePath = Property.getInstance().getProperty("weka_data_file");
            if (wekaFilePath == null || wekaFilePath.isEmpty())
                throw new Exception("Weka ARFF file could not be found!");

            // Pass into parser class that takes the training log data and
            // generates the corresponding WEKA ARFF file
            try {
                long startTime = System.currentTimeMillis();
                ServerLogConverter converter = new ServerLogConverter();
                converter.createWekaData(trainingLogFile, wekaFilePath);
                LOGGER.log(Level.INFO,
                        "Generated Weka ARFF file: " + wekaFilePath + "\nTime elapsed = "
                                + (System.currentTimeMillis() - startTime));
            } catch (IOException e) {
                throw new Exception("Unable to convert training log data file: " + e);
            }

            // read training data from arff file
            DataSource source = new DataSource(wekaFilePath);
            trainingData = source.getDataSet();
            if (trainingData == null)
                throw new Exception("Unable to load ARFF file");
            // by default the last index is the one we are trying to classify
            // (i.e. the soccer action attribute)
            if (trainingData.classIndex() == -1)
                trainingData.setClassIndex(trainingData.numAttributes() - 1);

            try {
                long startTime = System.currentTimeMillis();
                // Build a J48 tree from the training data
                // Use the default WEKA options for now
                J48 tree = new J48();

                // Discretize option
                if (Boolean.parseBoolean(Property.getInstance().getProperty("discretized"))) {
                    // Create a discretize classifier and feed training data into it
                    Discretize dis = new Discretize();
                    dis.setInputFormat(trainingData);
                    trainingData = Filter.useFilter(trainingData, dis);
                    tree.buildClassifier(trainingData);
                    decision_tree = tree;
                    LOGGER.log(Level.INFO, "Discretizing!");
                } else {
                    // train and make predictions on the J48 tree
                    tree.buildClassifier(trainingData);
                    decision_tree = tree;
                }
                LOGGER.log(Level.INFO, "Generated decision tree: " + String.valueOf(decision_tree)
                        + "\nTime elapsed = " + (System.currentTimeMillis() - startTime) + " ms");
            } catch (Exception e) {
                throw new Exception("Unable to generate Weka classifier: " + e);
            }

            Krislet player = new Krislet(InetAddress.getByName(hostName), port, team);

            // enter main loop
            player.mainLoop();

        } catch (Exception e) {
            LOGGER.log(Level.SEVERE, "Could not launch Krislet: ", e);
        }
    }

    public static Instances getTrainingData() {
        return trainingData;
    }

    public static Classifier getDecision_tree() {
        return decision_tree;
    }

    // ---------------------------------------------------------------------------
    // This constructor opens socket for connection with server
    public Krislet(InetAddress host, int port, String team) throws SocketException {
        m_socket = new DatagramSocket();
        m_host = host;
        m_port = port;
        m_team = team;
        m_playing = true;
    }

    // ---------------------------------------------------------------------------
    // This destructor closes socket to server
    public void finalize() {
        m_socket.close();
    }

    // ===========================================================================
    // Protected member functions

    // ---------------------------------------------------------------------------
    // This is main loop for player
    protected void mainLoop() throws IOException {
        byte[] buffer = new byte[MSG_SIZE];
        DatagramPacket packet = new DatagramPacket(buffer, MSG_SIZE);

        // first we need to initialize connection with server
        init();

        m_socket.receive(packet);
        parseInitCommand(new String(buffer));
        m_port = packet.getPort();

        // Now we should be connected to the server
        // and we know side, player number and play mode
        while (m_playing)
            parseSensorInformation(receive());
        finalize();
    }

    // ===========================================================================
    // Implementation of SendCommand Interface

    // ---------------------------------------------------------------------------
    // This function sends move command to the server
    public void move(double x, double y) {
        send("(move " + Double.toString(x) + " " + Double.toString(y) + ")");
    }

    // ---------------------------------------------------------------------------
    // This function sends turn command to the server
    public void turn(double moment) {
        send("(turn " + Double.toString(moment) + ")");
    }

    public void turn_neck(double moment) {
        send("(turn_neck " + Double.toString(moment) + ")");
    }

    // ---------------------------------------------------------------------------
    // This function sends dash command to the server
    public void dash(double power) {
        send("(dash " + Double.toString(power) + ")");
    }

    // ---------------------------------------------------------------------------
    // This function sends kick command to the server
    public void kick(double power, double direction) {
        send("(kick " + Double.toString(power) + " " + Double.toString(direction) + ")");
    }

    // ---------------------------------------------------------------------------
    // This function sends say command to the server
    public void say(String message) {
        send("(say " + message + ")");
    }

    // ---------------------------------------------------------------------------
    // This function sends chage_view command to the server
    public void changeView(String angle, String quality) {
        send("(change_view " + angle + " " + quality + ")");
    }

    // ---------------------------------------------------------------------------
    // This function sends bye command to the server
    public void bye() {
        m_playing = false;
        send("(bye)");
    }

    // ---------------------------------------------------------------------------
    // This function parses initial message from the server
    protected void parseInitCommand(String message) throws IOException {
        Matcher m = Pattern.compile("^\\(init\\s(\\w)\\s(\\d{1,2})\\s(\\w+?)\\).*$").matcher(message);
        if (!m.matches()) {
            throw new IOException(message);
        }

        // initialize player's brain
        m_brain = new Brain(this, m_team, m.group(1).charAt(0), Integer.parseInt(m.group(2)), m.group(3));
    }

    // ===========================================================================
    // Here comes collection of communication function
    // ---------------------------------------------------------------------------
    // This function sends initialization command to the server
    private void init() {
        send("(init " + m_team + " (version 9))");
    }

    // ---------------------------------------------------------------------------
    // This function parses sensor information
    private void parseSensorInformation(String message) throws IOException {
        // First check kind of information
        Matcher m = message_pattern.matcher(message);
        if (!m.matches()) {
            throw new IOException(message);
        }
        if (m.group(1).compareTo("see") == 0) {
            VisualInfo info = new VisualInfo(message);
            info.parse();
            m_brain.see(info);
        } else if (m.group(1).compareTo("hear") == 0)
            parseHear(message);
    }

    // ---------------------------------------------------------------------------
    // This function parses hear information
    private void parseHear(String message) throws IOException {
        // get hear information
        Matcher m = hear_pattern.matcher(message);
        int time;
        String sender;
        String uttered;
        if (!m.matches()) {
            throw new IOException(message);
        }
        time = Integer.parseInt(m.group(1));
        sender = m.group(2);
        uttered = m.group(3);
        if (sender.compareTo("referee") == 0)
            m_brain.hear(time, uttered);
        // else if( coach_pattern.matcher(sender).find())
        // m_brain.hear(time,sender,uttered);
        else if (sender.compareTo("self") != 0)
            m_brain.hear(time, Integer.parseInt(sender), uttered);
    }

    // ---------------------------------------------------------------------------
    // This function sends via socket message to the server
    private void send(String message) {
        byte[] buffer = Arrays.copyOf(message.getBytes(), MSG_SIZE);
        try {
            LOGGER.log(Level.FINEST, "Sending message to server: " + message);
            DatagramPacket packet = new DatagramPacket(buffer, MSG_SIZE, m_host, m_port);
            m_socket.send(packet);
        } catch (IOException e) {
            LOGGER.log(Level.SEVERE, "socket sending error ", e);
        }
    }

    // ---------------------------------------------------------------------------

    // This function waits for new message from server
    private String receive() {
        byte[] buffer = new byte[MSG_SIZE];
        DatagramPacket packet = new DatagramPacket(buffer, MSG_SIZE);
        try {
            m_socket.receive(packet);
        } catch (SocketException e) {
            LOGGER.log(Level.SEVERE, "shutting down...", e);
        } catch (IOException e) {
            LOGGER.log(Level.SEVERE, "socket receiving error ", e);
        }
        return new String(buffer);
    }
}