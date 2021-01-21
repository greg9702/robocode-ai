package iwium;

import org.apache.logging.log4j.LogManager;
import robocode.*;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Random;

/**
 * MiddleRobot - does not shoot at all, learns only how to get to the middle of the board
 */
public class MiddleRobot extends CustomQRobot {
    private static final String m_actionMoveForward = "moveForward";
    private static final String m_actionMoveLeft = "moveLeft";
    private static final String m_actionMoveRight = "moveRight";
    private static final String m_actionMoveBack = "moveBack";

    private void init() {
        m_logger = LogManager.getLogger("middlebase");

        m_qtableFilename = "middleQtable.bin";

        m_actions = new ArrayList<>();
        m_actions.add(new Action(0, m_actionMoveForward));
        m_actions.add(new Action(1, m_actionMoveLeft));
        m_actions.add(new Action(2, m_actionMoveRight));
        m_actions.add(new Action(3, m_actionMoveBack));
    }

    private void resetEnvironment() {
        m_currentState = new State(new ArrayList<>(Arrays.asList(
                new Param("robotXPos", 0, getBattleFieldWidth(), 8),
                new Param("robotYPos", 0, getBattleFieldHeight(), 8),
                new Param("robotHeading", 0, 360, 8)
        )));

        initQTable();
    }

    public void run() {
        if (!m_initialized) {
            init();
            m_initialized = true;
        }

        resetEnvironment();
        while (true) {
            State stateBeforeAction = new State(m_currentState);

            Action action;
            Random rand = new Random();
            double epsilon = m_epsilon;
            if (epsilon > rand.nextDouble()) {
                int actionIndex = rand.nextInt(m_actions.size());
                action = m_actions.get(actionIndex);
            } else {
                action = m_qtable.bestAction(m_currentState);
            }

            m_reward = 0;
            performAction(action);

            waitFor(new MoveCompleteCondition(this));
            waitFor(new TurnCompleteCondition(this));
            waitFor(new GunTurnCompleteCondition(this));

            m_cumulativeReward += m_reward;
            m_qtable.updateRewards(stateBeforeAction, action, m_reward, m_currentState);

            saveQTable();
        }
    }

    private void performAction(Action p_action) {
        String actionName = p_action.getName();

        double moveDistance = 100;
        double rotationDegrees = 90;

        switch (actionName) {
            case m_actionMoveForward:
                ahead(moveDistance);
                break;
            case m_actionMoveBack:
                back(moveDistance);
                break;
            case m_actionMoveLeft:
                turnLeft(rotationDegrees);
                ahead(moveDistance);
                break;
            case m_actionMoveRight:
                turnRight(rotationDegrees);
                ahead(moveDistance);
                break;
        }

        execute();
    }

    public void onStatus(StatusEvent e) {
        if (m_currentState == null) {
            m_logger.info("resetEnvironment() not called yet. Skipping onStatus.");
            return;
        }
        RobotStatus s = e.getStatus();
        double xPos = s.getX();
        double yPos = s.getY();
        double heading = s.getHeading();

        m_currentState.updateParam("robotXPos", xPos);
        m_currentState.updateParam("robotYPos", yPos);
        m_currentState.updateParam("robotHeading", heading);

    }

    public void onHitWall(HitWallEvent e) {
        m_reward -= 10;
    }
}
