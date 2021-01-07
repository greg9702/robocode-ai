package iwium;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import robocode.AdvancedRobot;
import robocode.RobocodeFileOutputStream;
import robocode.RoundEndedEvent;

import java.io.File;
import java.util.ArrayList;

/**
 * CustomRobot - a class that extends the robocode API by actions common to custom QLearning robots
 */
public class CustomQRobot extends AdvancedRobot {
    protected static Logger m_logger;
    private static final Logger m_loggerRewards = LogManager.getLogger("rewards");

    protected static String m_qtableFilename;
    protected static boolean m_initialized = false;

    private static final double m_alphaDivisor = 2;
    private static final double m_minAlpha = 0.1;
    protected static final double m_epsilon = 0.1;
    private static final double m_gamma = 1;

    protected static QTable m_qtable = null;
    protected State m_currentState;
    protected static ArrayList<Action> m_actions;
    protected double m_cumulativeReward = 0;
    protected double m_reward = 0;

    protected void initQTable() {
        var numStates = m_currentState.getNumStates();

        m_qtable = new QTable(m_actions, numStates, m_alphaDivisor, m_minAlpha, m_gamma);

        File dumpFile = getDataFile(m_qtableFilename);
        try {
            m_qtable.loadValues(dumpFile);
        } catch (Exception e) {
            m_logger.error("Unable to load QTable: " + e);
            m_logger.error("Fresh QTable will be used instead.");
        }
    }

    protected void saveQTable() {
        File dumpFile = getDataFile(m_qtableFilename);
        try {
            RobocodeFileOutputStream fstream = new RobocodeFileOutputStream(dumpFile);
            m_qtable.save(fstream);
        } catch (Exception e) {
            m_logger.error("Unable to save QTable: " + e);
        }
    }

    protected void turnHorizontallyRight() {
        turnLeft(getHeading() - 90);
    }

    protected void turnHorizontallyLeft() {
        turnLeft(getHeading() + 90);
    }

    public void onRoundEnded(RoundEndedEvent e) {
        m_loggerRewards.debug(m_cumulativeReward);
    }
}