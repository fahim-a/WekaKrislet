import java.util.logging.Level;
import java.util.logging.Logger;

import weka.classifiers.trees.J48;
import weka.core.Instance;
import weka.core.Instances;
import weka.core.converters.ConverterUtils.DataSource;

public class PinkyTest {
    private final static Logger LOGGER = Logger.getLogger(PinkyTest.class.getName());

    public static void main(String[] args) {
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
            SoccerAction decidedAction = getNextAction(getEnvironment(), decision_tree, sampleInstance);
            LOGGER.log(Level.INFO, "Suggestion action: " + String.valueOf(decidedAction));
        } catch (Exception e) {
            LOGGER.log(Level.SEVERE, "Encountered error", e);
        }
    }

    private static PerceivedEnvironment getEnvironment() {
        // ?,?,66.0,-15.0,60.3,15.0,?,turn
        // return new PerceivedEnvironment(-1, -1, 66.0, -15.0, 60.3,
        // 15.0,null);
        // 0.4,-88.0,26.8,3.0,2.7,-80.0,?,kick
        // return new PerceivedEnvironment(0.4, -88.0, 26.8, 3.0, 2.7, -80.0,
        // null);
        return new PerceivedEnvironment("0.4,-88.0,26.8,3.0,2.7,-80.0,?");
    }

    private static SoccerAction getNextAction(PerceivedEnvironment currentEnvironment, J48 decision_tree,
            Instance sampleInstance) throws Exception {
        Instance envIntstance = currentEnvironment.buildWekaInstance(sampleInstance);
        double classResult = decision_tree.classifyInstance(envIntstance);
        return SoccerAction.values()[(int) classResult];
    }
}