package iwium;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import robocode.AdvancedRobot;
import robocode.RobocodeFileOutputStream;
import robocode.RoundEndedEvent;

import java.io.File;
import java.util.ArrayList;
import java.util.Arrays;

/**
 * CustomRobot - a class that extends the robocode API by actions common to custom QLearning robots
 */
public class CustomQRobot extends AdvancedRobot {
    protected static Logger m_logger;
    private static final Logger m_loggerRewards = LogManager.getLogger("rewards");

    protected enum ActionState {
        NONE, TO_BE_CHOSEN, ONGOING, WAITING_FOR_STATE_UPDATE, TO_BE_REWARDED
    }
    protected ActionState m_actionState = ActionState.NONE;

    protected static String m_qtableFilename;
    protected static boolean m_initialized = false;

    private static final double m_alphaDivisor = 2;
    private static final double m_minAlpha = 0.1;
    protected static double m_epsilon = 0.1;
    private static final double m_gamma = 1;

    protected static QTable m_qtable = null;
    protected State m_currentState;
    protected static ArrayList<Action> m_actions;
    protected double m_cumulativeReward = 0;
    protected double m_reward = 0;

    protected static int m_roundNo = 0;
    private static final int m_trainRounds = Integer.parseInt(System.getProperty("trainRounds"));

    protected void initQTable() {
        var numStates = m_currentState.getNumStates();

        m_qtable = new QTable(m_actions, numStates, m_alphaDivisor, m_minAlpha, m_gamma);
        m_qtable.updateRates(m_roundNo);

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

    protected void moveAhead() {
        ahead(50);
    }

    protected void moveBack() {
        back(20);
    }

    public void onRoundEnded(RoundEndedEvent e) {
        m_loggerRewards.debug(m_cumulativeReward);
        if (m_roundNo++ > m_trainRounds) {
            m_epsilon = 0.0;
        }
    }

    /**
     * Finds closest wall and runs toward opposite direction.
     *
     * @param int safeDistance
     */
    protected void bounceFromWall(int safeDistance) {
        double fieldWidth = getBattleFieldWidth();
        double fieldHeight = getBattleFieldHeight();
        double xPos = getX();
        double yPos = getY();
        double currentAngle = getHeading();
        // distances to walls: left, bottom, right, top
        double[] wallDistances = {xPos, yPos, fieldWidth - xPos, fieldHeight - yPos};
        double minDistance = Arrays.stream(wallDistances).min().getAsDouble();
        double angleDiff = 0;
        // diffs to opposite direction
        if (wallDistances[0] == minDistance) {
            angleDiff = 90 - currentAngle;
        } else if (wallDistances[1] == minDistance) {
            angleDiff = 0 - currentAngle;
        } else if (wallDistances[2] == minDistance) {
            angleDiff = 270 - currentAngle;
        } else if (wallDistances[3] == minDistance) {
            angleDiff = 180 - currentAngle;
        } else {
            m_logger.error("Unknown wall collision!");
        }
        if (angleDiff > 180) {
            turnLeft(angleDiff - 180);
        } else {
            turnRight(angleDiff);
        }
        // note: minDistance is probably always equal to 0
        ahead(safeDistance - minDistance);
    }
}