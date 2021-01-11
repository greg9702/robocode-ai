package iwium;

import org.apache.logging.log4j.LogManager;
import robocode.*;
import robocode.util.Utils;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Random;


/**
 * Driver - was trying to learn to avoid bullets
 */
public class Driver extends CustomQRobot {

    private static final String m_stateParamXpos = "Xpos";
    private static final String m_stateParamYpos = "Ypos";
    private static final int m_posBins = 16;

    private static final String m_stateParamDistanceToEnemy = "distanceToEnemy";
    private static final int m_distanceToEnemyBins = 8;

    private static final String m_stateAngle = "Angle";
    private static final int m_angleBins = 16;


    private static final String m_actionTurnRight = "right";
    private static final String m_actionTurnLeft = "left";
    private static final String m_actionAhead = "ahead";
    private static final String m_actionBack = "back";

    private void init() {
        m_logger = LogManager.getLogger("hunterbase");
        m_qtableFilename = "hunterQtable.bin";
    }

    private void resetEnvironment() {
        m_actions = new ArrayList<>();
        m_actions.add(new Action(0, m_actionTurnRight));
        m_actions.add(new Action(1, m_actionTurnLeft));
        m_actions.add(new Action(2, m_actionAhead));
        m_actions.add(new Action(3, m_actionBack));

        final int maxDistance = (int) Math.sqrt(Math.pow(getBattleFieldWidth(), 2) + Math.pow(getBattleFieldHeight(), 2));

        m_currentState = new State(new ArrayList<>(Arrays.asList(
                new Param(m_stateParamXpos, 0, getBattleFieldWidth(), m_posBins),
                new Param(m_stateParamYpos, 0, getBattleFieldHeight(), m_posBins),
                new Param(m_stateAngle, 0, 360, m_angleBins),
                new Param(m_stateParamDistanceToEnemy, 0, maxDistance, m_distanceToEnemyBins)
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
            var stateBeforeAction = new State(m_currentState);

            Random rand = new Random();
            Action action = null;
            if (m_epsilon > rand.nextDouble()) {
                int actionIndex = rand.nextInt(m_actions.size());
                action = m_actions.get(actionIndex);
            } else {
                action = m_qtable.bestAction(m_currentState);
            }
            performAction(action);
            m_cumulativeReward += m_reward;
            m_qtable.updateRewards(stateBeforeAction, action, m_reward, m_currentState);
        }
    }

    private void performAction(Action p_action) {
        m_reward = 0;

        // we reward all actions as +2

        m_reward += 2;

        switch (p_action.getName()) {
            case m_actionAhead:
                moveAhead();
                break;
            case m_actionTurnLeft:
                turnHorizontallyLeft();
                break;
            case m_actionTurnRight:
                turnHorizontallyRight();
                break;
            case m_actionBack:
                moveBack();
                break;
        }

        m_currentState.updateParam(m_stateParamXpos, getX());
        m_currentState.updateParam(m_stateParamYpos, getY());
        m_currentState.updateParam(m_stateAngle, getHeading());
    }

    public void onStatus(StatusEvent e) {
        m_currentState.updateParam(m_stateParamXpos, getX());
        m_currentState.updateParam(m_stateParamYpos, getY());
        m_currentState.updateParam(m_stateAngle, getHeading());
    }

    public void onScannedRobot(ScannedRobotEvent e) {
        double enemyDistance = e.getDistance();
        m_currentState.updateParam(m_stateParamDistanceToEnemy, enemyDistance);
    }

    public void onHitByBullet(HitByBulletEvent event) {
        m_reward -= 3;
    }

    public void onHitWall(HitWallEvent e) {
        m_reward -= 10;
    }
}
