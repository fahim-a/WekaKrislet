//
//	File:			Brain.java
//	Author:		Krzysztof Langner
//	Date:			1997/04/28
//
//    Modified by:	Paul Marlow

//    Modified by:      Edgar Acosta
//    Date:             March 4, 2008

import java.util.regex.Pattern;

import weka.classifiers.trees.J48;
import weka.core.Instance;
import weka.core.Instances;

class Brain extends Thread implements SensorInput {
	// ---------------------------------------------------------------------------
	// This constructor:
	// - stores connection to krislet
	// - starts thread for this object
	public Brain(SendCommand krislet, String team, char side, int number,
			String playMode) {
		m_timeOver = false;
		m_krislet = krislet;
		m_memory = new Memory();
		// m_team = team;
		m_side = side;
		// m_number = number;
		m_playMode = playMode;
		start();
	}

	@Override
	public void run() {
		// first put it somewhere on my side
		if (Pattern.matches("^before_kick_off.*", m_playMode))
			m_krislet.move(-Math.random() * 52.5, 34 - Math.random() * 68.0);

		while (!m_timeOver) {
			try {
				Instances trainingData = Krislet.getTrainingData();
				J48 decisionTree = Krislet.getDecision_tree();
				Instance sampleInstance = trainingData.firstInstance();

				VisualInfo perceivedEnv = m_memory.getInfo();
				// TODO Get string extracted from the perceived environment
				String envString = "0.4,-88.0,26.8,3.0,2.7,-80.0,?";

				PerceivedEnvironment pe = new PerceivedEnvironment(envString);
				SoccerAction action = getNextAction(pe, decisionTree,
						sampleInstance);
				invokeKrisletAction(action);
			} catch (Exception e) {
				e.printStackTrace();
				invokeKrisletAction(null);
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

	private void invokeKrisletAction(SoccerAction action) {
		ObjectInfo object;

		switch (action) {
		case DASH:
			object = m_memory.getObject("ball");
			m_krislet.dash(10 * object.m_distance);
			break;
		case KICK:
			// We know where is ball and we can kick it
			// so look for goal
			if (m_side == 'l')
				object = m_memory.getObject("goal r");
			else
				object = m_memory.getObject("goal l");
			m_krislet.kick(100, object.m_direction);
			break;
		case TURN:
			m_krislet.turn(40); // TODO Determine proper angle
			break;
		default:
			m_krislet.turn_neck(40);
			m_memory.waitForNewInfo();
			break;
		}
	}

	private SoccerAction getNextAction(PerceivedEnvironment currentEnvironment,
			J48 decision_tree, Instance sampleInstance) throws Exception {
		Instance envIntstance = currentEnvironment
				.buildWekaInstance(sampleInstance);
		double classResult = decision_tree.classifyInstance(envIntstance);
		return SoccerAction.values()[(int) classResult];
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
	public void original_run() {
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
	private SendCommand m_krislet; // robot which is controled by this brain
	private Memory m_memory; // place where all information is stored
	private char m_side;
	volatile private boolean m_timeOver;
	private String m_playMode;
}
