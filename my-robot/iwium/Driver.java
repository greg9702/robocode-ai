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
    private static final int m_posBins = 4;

    private static final String m_stateParamDistanceToEnemy = "distanceToEnemy";
    private static final int m_distanceToEnemyBins = 4;

    private static final String m_stateParamEnemyEnergyDelta = "enemyEnengy";
    private static final int m_enemyEnergyDelta = 3;

    private static final String m_stateAngle = "Angle";
    private static final String m_stateAngleToEnemy = "AngleToEnenmy";
    private static final int m_angleBins = 4;

    private static final String m_stateRoundsWithoutHit = "RoundsWithoutHit";


    private static final String m_actionTurnRight = "right";
    private static final String m_actionTurnLeft = "left";
    private static final String m_actionAhead = "ahead";
    private static final String m_actionBack = "back";

    private static final String m_actionDoNothing = "nothing";

    private static final String m_stateEnemyBearing = "enemyBearing";
    private static final int m_enenmyBearingBins = 8;


    double m_enemyEnergy;

    private void init() {
        m_logger = LogManager.getLogger("driverbase");
        m_qtableFilename = "driverQtable.bin";
    }

    private void resetEnvironment() {
        m_actions = new ArrayList<>();
        m_actions.add(new Action(1, m_actionDoNothing));
        m_actions.add(new Action(2, m_actionAhead));
        m_actions.add(new Action(3, m_actionBack));
        m_actions.add(new Action(4, m_actionTurnLeft));
        m_actions.add(new Action(5, m_actionTurnRight));

        final int maxDistance = (int) Math.sqrt(Math.pow(getBattleFieldWidth(), 2) + Math.pow(getBattleFieldHeight(), 2));

        m_currentState = new State(new ArrayList<>(Arrays.asList(
                new Param(m_stateParamDistanceToEnemy, 0, maxDistance, m_distanceToEnemyBins),
                new Param(m_stateParamEnemyEnergyDelta, 0, 3, m_enemyEnergyDelta),
                new Param(m_stateEnemyBearing, -180, 180, m_enenmyBearingBins)
//                new Param(m_stateParamYpos, 0, getBattleFieldHeight(), m_posBins)
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

            if (getRoundNum() == 30000) {
                m_epsilon = 0;
            }

            setTurnRadarRight(Double.POSITIVE_INFINITY);

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

//            saveQTable();
        }
    }

    private void performAction(Action p_action) {
        m_reward = 0;

        System.out.println("Performing action" + p_action.getName());

        switch (p_action.getName()) {
            case m_actionAhead:
                moveAhead();
                break;
            case m_actionBack:
                moveBack();
                break;
            case m_actionTurnLeft:
                turnLeft();
                break;
            case m_actionTurnRight:
                turnRight();
                break;
            case m_actionDoNothing:
                break;
        }
    }

    public void onStatus(StatusEvent e) {
//        m_currentState.updateParam(m_stateParamXpos, getX());
//        m_currentState.updateParam(m_stateParamYpos, getY());
    }

    public void onScannedRobot(ScannedRobotEvent e) {
        double enemyDistance = e.getDistance();
        m_currentState.updateParam(m_stateParamDistanceToEnemy, enemyDistance);

        double currentEnergy = e.getEnergy();

        System.out.println("Enenmy energy delta: " + (m_enemyEnergy - currentEnergy));
        m_currentState.updateParam(m_stateParamEnemyEnergyDelta, m_enemyEnergy - currentEnergy);
        m_currentState.updateParam(m_stateEnemyBearing, e.getBearing());

        m_enemyEnergy = currentEnergy;

    }

//    public void onHitWall(HitWallEvent e) {
//        m_reward -= 10;
//    }

    public void onHitByBullet(HitByBulletEvent event) {
        m_reward -= 15;
        double xd = 0;
        m_currentState.updateParam(m_stateRoundsWithoutHit, xd);
    }

}
