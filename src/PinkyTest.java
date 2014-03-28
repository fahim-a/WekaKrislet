import java.io.IOException;
import java.util.logging.Level;
import java.util.logging.Logger;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import weka.classifiers.trees.J48;
import weka.core.Instance;
import weka.core.Instances;
import weka.core.converters.ConverterUtils.DataSource;
import weka.filters.Filter;
import weka.filters.supervised.attribute.Discretize;

public class PinkyTest {
    private final static Logger LOGGER    = Logger.getLogger(PinkyTest.class.getName());
    private static String[]     sampleEnv = { "?,64.7,?", // turn
            "33.1,64.7,?", // turn
            "33.1,?,?", // turn
            "33.1,?,?", // dash
            "0.9,?,?", // turn
            "0.9,?,?", // turn
            "0.9,52.5,?", // kick
            "1.0,25.3,?", // kick
            "0.8,33.4,?" // kick

                                          };

    public static void main(String[] args) {
        // classifyInstances();
        // createDataFile();
        // testRegex();
    }

    private static void classifyInstances() {
        try {
            // setup the logger
            WekaLogger.setup(PinkyTest.class.getName());

            // read training data from arff file
            DataSource source = new DataSource("lib/weka_test.arff");
            Instances trainingData = source.getDataSet();
            if (trainingData.classIndex() == -1)
                trainingData.setClassIndex(trainingData.numAttributes() - 1);

            Instance sampleInstance = trainingData.firstInstance();

            // build a J48 tree from the training data
            J48 decision_tree = new J48();
            decision_tree.buildClassifier(trainingData);
            LOGGER.log(Level.INFO, String.valueOf(decision_tree));

            // decide on next agent action based on current environment
            // perception and decision tree
            for (String env : sampleEnv) {
                SoccerAction decidedAction = getNextAction(env, decision_tree, sampleInstance);
                LOGGER.log(Level.INFO, env + " -> Suggested action: " + String.valueOf(decidedAction));
            }
        } catch (Exception e) {
            LOGGER.log(Level.SEVERE, "Encountered error", e);
        }
    }

    private static SoccerAction getNextAction(String currentEnvironment, J48 decision_tree, Instance sampleInstance)
            throws Exception {
        Instance envIntstance = PerceivedEnvironment.buildWekaInstance(currentEnvironment, sampleInstance);
        double classResult = decision_tree.classifyInstance(envIntstance);
        return SoccerAction.values()[(int) classResult];
    }

    public static void testRegex() {
        String[] actions = { "(dash 245.0)", "(move -3.1298586595420725 -32.85851748409405)", "(kick 100.0 34.0)",
                "(turn -4.0)", "(turn -2.0)", "(crap 2.0)" };

        Pattern action_msg = Pattern.compile("^\\((kick|dash|turn|turn_neck|move|say|change_view|bye)\\s+.*");
        for (String a : actions) {
            Matcher m = action_msg.matcher(a);
            if (m.find())
                LOGGER.log(Level.INFO, m.group(1));
        }
    }

    public static void createDataFile() {
        try {
            (new ServerLogConverter()).createWekaData(Property.getInstance().getProperty("server_log"), Property
                    .getInstance().getProperty("weka_data_file"));
        } catch (IOException e) {
            LOGGER.log(Level.SEVERE, "Encountered error", e);
        }
    }

    public static void doTestFile() throws Exception {

        // read training data from arff file
        DataSource source = new DataSource(Property.getInstance().getProperty("weka_data_file"));
        Instances trainingData = source.getDataSet();
        if (trainingData.classIndex() == -1)
            trainingData.setClassIndex(trainingData.numAttributes() - 1);

        source = new DataSource(Property.getInstance().getProperty("weka_test_file"));
        Instances test = source.getDataSet();
        if (test.classIndex() == -1)
            test.setClassIndex(trainingData.numAttributes() - 1);

        Instance sampleInstance = trainingData.firstInstance();

        // build a J48 tree from the training data
        J48 decision_tree = new J48();
        Discretize dis = new Discretize();
        dis.setInputFormat(trainingData);
        Filter.useFilter(trainingData, dis);
        decision_tree.buildClassifier(trainingData);

        Instances labeled = new Instances(test);

        java.io.BufferedWriter w = new java.io.BufferedWriter(new java.io.FileWriter(
                "./lib/test_results_getNextAction.arff"));

        w.write("@relation soccer\n\n" +

        "@attribute ball_dis numeric\n" + "@attribute net_dis numeric\n" + "@attribute action {dash,turn,kick}\n\n" +

        "@data\n");

        for (int i = 0; i < test.numInstances(); i++) {
            // method 1
            String inst = test.instance(i).toString();
            w.write(inst.toString().substring(0, inst.length() - 1)
                    + getNextAction(test.instance(i).toString(), decision_tree, sampleInstance).toString()
                            .toLowerCase() + "\n");

            // method 2
            double clsLabel = decision_tree.classifyInstance(test.instance(i));
            labeled.instance(i).setClassValue(clsLabel);
        }
        w.close();

        java.io.BufferedWriter writer = new java.io.BufferedWriter(new java.io.FileWriter("./lib/test_results.arff"));
        writer.write(labeled.toString());
        writer.close();
    }
}