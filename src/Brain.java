//
//	File:			Brain.java
//	Author:		Krzysztof Langner
//	Date:			1997/04/28
//
//    Modified by:	Paul Marlow

//    Modified by:      Edgar Acosta
//    Date:             March 4, 2008

import java.util.logging.Level;
import java.util.logging.Logger;
import java.util.regex.Pattern;

import weka.classifiers.Classifier;
import weka.core.Instance;
import weka.core.Instances;

public class Brain extends Thread implements SensorInput {
    private final static Logger LOGGER = Logger.getLogger(Brain.class.getName());

    // ---------------------------------------------------------------------------
    // This constructor:
    // - stores connection to krislet
    // - starts thread for this object
    public Brain(SendCommand krislet, String team, char side, int number, String playMode) {
        m_timeOver = false;
        m_krislet = krislet;
        m_memory = new Memory();
        m_team = team;
        m_side = side;
        // m_number = number;
        m_playMode = playMode;
        start();
    }

    @Override
    public void run() {
        if (runWeka())
            weka();
        else
            originalKrislet();
    }

    public boolean runWeka() {
        return true;
    }

    private void weka() {
        // first put it somewhere on my side
        if (Pattern.matches("^before_kick_off.*", m_playMode))
            m_krislet.move(-Math.random() * 52.5, 34 - Math.random() * 68.0);

        // fetch pre-loaded decision tree information
        Instances trainingData = Krislet.getTrainingData();
        Classifier decisionTree = Krislet.getDecision_tree();
        Instance sampleInstance = trainingData.firstInstance();

        while (!m_timeOver) {
            try {
                long startTime = System.currentTimeMillis();
                // fetch the current perceived environment states
                VisualInfo perceivedEnv = m_memory.getCurrentInfo();
                if (perceivedEnv != null) {
                    // Get string extracted from the perceived environment
                    DataInstance di = new DataInstance(perceivedEnv, this.m_team, this.m_side);
                    String envString = di.toString();

                    // based on the previously captured behavior, and current
                    // environment state, try to come up with a
                    // predicted/suggested action
                    SoccerAction action = predictNextAction(envString, decisionTree, sampleInstance);

                    LOGGER.log(Level.INFO, "Based on " + envString + "; Predicted action: " + String.valueOf(action)
                            + "; Time elapsed = " + (System.currentTimeMillis() - startTime) + " ms");

                    invokeKrisletAction(di, action);
                }
            } catch (Exception e) {
                LOGGER.log(Level.SEVERE, "Could not determine next action", e);

                // do nothing
                instructKrisletToDoNothing();
            }

            // sleep one step to ensure that we will not send
            // two commands in one cycle.
            try {
                Thread.sleep(2 * SoccerParams.simulator_step);
            } catch (Exception e) {
                LOGGER.log(Level.FINE, "Error when waiting for next simulator step", e);
            }
        }
        m_krislet.bye();
    }

    private void invokeKrisletAction(DataInstance di, SoccerAction action) {
        ObjectInfo object;

        if (action == null) {
            instructKrisletToDoNothing();
        } else {
            switch (action) {
            case DASH:
                object = di.getObject("ball");
                if (object != null)
                    m_krislet.dash(10 * object.m_distance);
                else {
                    instructKrisletToDoNothing(); // TODO We were told to
                                                  // dash...but we're not?
                }
                break;
            case KICK:
                // We know where the ball is and we can kick it
                // so look for goal
                if (m_side == 'l')
                    object = di.getObject("goal r");
                else
                    object = di.getObject("goal l");

                if (object != null)
                    m_krislet.kick(100, object.m_direction);
                else {
                    instructKrisletToDoNothing(); // TODO We were told to
                                                  // kick...but we're not?
                }
                break;
            case TURN:
                object = di.getObject("ball");
                if (object != null) {
                    m_krislet.turn(object.m_direction);
                } else
                    m_krislet.turn(40); // TODO Determine default angle
                break;

            default:
                instructKrisletToDoNothing();
                break;
            }
        }
    }

    private void instructKrisletToDoNothing() {
        m_memory.waitForNewInfo();
    }

    private SoccerAction predictNextAction(String currentEnvironment, Classifier decision_tree, Instance sampleInstance)
            throws Exception {
        // build an instance based on current environment
        Instance envIntstance = PerceivedEnvironment.buildWekaInstance(currentEnvironment, sampleInstance);
        // let the decision tree classify this and return the index of the
        // action
        double prediction = decision_tree.classifyInstance(envIntstance);

        if (Instance.missingValue() == prediction)
            return null;
        else {
            // fetch the action based on index (hence ordering is very
            // important)
            return SoccerAction.values()[(int) prediction];
        }
    }

    // ---------------------------------------------------------------------------
    // This is main brain function used to make decision
    // In each cycle we decide which command to issue based on
    // current situation. the rules are:
    //
    // 1. If you don't know where is ball then turn right and wait for new info
    //
    // 2. If ball is too far to kick it then
    // 2.1. If we are directed towards the ball then go to the ball
    // 2.2. else turn to the ball
    //
    // 3. If we dont know where is opponent goal then turn wait
    // and wait for new info
    //
    // 4. Kick ball
    //
    // To ensure that we don't send commands to often after each cycle
    // we waits one simulator steps. (This of course should be done better)

    // *************** Improvements ******************
    // Allways know where the goal is.
    // Move to a place on my side on a kick_off
    // ************************************************
    private void originalKrislet() {
        ObjectInfo object;

        // first put it somewhere on my side
        if (Pattern.matches("^before_kick_off.*", m_playMode))
            m_krislet.move(-Math.random() * 52.5, 34 - Math.random() * 68.0);

        while (!m_timeOver) {
            object = m_memory.getObject("ball");
            if (object == null) {
                // If you don't know where is ball then find it
                m_krislet.turn(40);
                m_memory.waitForNewInfo();
            } else if (object.m_distance > 1.0) {
                // If ball is too far then
                // turn to ball or
                // if we have correct direction then go to ball
                if (object.m_direction != 0)
                    m_krislet.turn(object.m_direction);
                else
                    m_krislet.dash(10 * object.m_distance);
            } else {
                // We know where is ball and we can kick it
                // so look for goal
                if (m_side == 'l')
                    object = m_memory.getObject("goal r");
                else
                    object = m_memory.getObject("goal l");

                if (object == null) {
                    m_krislet.turn(40);
                    m_memory.waitForNewInfo();
                } else
                    m_krislet.kick(100, object.m_direction);
            }

            // sleep one step to ensure that we will not send
            // two commands in one cycle.
            try {
                Thread.sleep(2 * SoccerParams.simulator_step);
            } catch (Exception e) {
            }
        }
        m_krislet.bye();
    }

    // ===========================================================================
    // Here are suporting functions for implement logic

    // ===========================================================================
    // Implementation of SensorInput Interface

    // ---------------------------------------------------------------------------
    // This function sends see information
    public void see(VisualInfo info) {
        m_memory.store(info);
    }

    // ---------------------------------------------------------------------------
    // This function receives hear information from player
    public void hear(int time, int direction, String message) {
    }

    // ---------------------------------------------------------------------------
    // This function receives hear information from referee
    public void hear(int time, String message) {
        if (message.compareTo("time_over") == 0)
            m_timeOver = true;

    }

    // ===========================================================================
    // Private members
    private SendCommand      m_krislet; // robot which is controled by this
                                         // brain
    private Memory           m_memory;  // place where all information is
                                         // stored
    private char             m_side;
    private String           m_team;
    volatile private boolean m_timeOver;
    private String           m_playMode;
}
