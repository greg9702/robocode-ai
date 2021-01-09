package iwium;

import org.apache.logging.log4j.LogManager;
import robocode.*;
import robocode.util.Utils;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Random;


/**
 * Hunter - has a fixed movement and learns when to shoot
 */
public class Hunter extends CustomQRobot {


    private static final String m_stateParamAngleToEnemy = "angleToEnemy";
    private static final int m_angleToEnemyBins = 18;
    private static final String m_stateParamDistanceToEnemy = "distanceToEnemy";
    private static final int m_distanceToEnemyBins = 8;
    private static final String m_stateParamGunHeat = "gunHeat";
    private static final int m_gunHeatBins = 2;
    private static final String m_actionFire = "fire";
    private static final String m_actionTurnGunRight = "turnGunRight";
    private static final String m_actionTurnGunLeft = "turnGunLeft";

    private double m_xpos = 0;
    private final double m_angle = 10;
    State m_stateBeforeAction = null;
    Action m_action = null;

    private void init() {
        m_logger = LogManager.getLogger("hunterbase");
        m_qtableFilename = "hunterQtable.bin";
    }

    private void resetEnvironment() {
        m_actions = new ArrayList<>();
        m_actions.add(new Action(0, m_actionFire));
        m_actions.add(new Action(1, m_actionTurnGunRight));
        m_actions.add(new Action(2, m_actionTurnGunLeft));

        final int maxDistance = (int) Math.sqrt(Math.pow(getBattleFieldWidth(), 2) + Math.pow(getBattleFieldHeight(), 2));

        m_currentState = new State(new ArrayList<>(Arrays.asList(
                new Param(m_stateParamAngleToEnemy, -180, 180, m_angleToEnemyBins),
                new Param(m_stateParamDistanceToEnemy, 0, maxDistance, m_distanceToEnemyBins),
                new Param(m_stateParamGunHeat, 0, 1, m_gunHeatBins)
        )));

        initQTable();
    }

    public void run() {
        if (!m_initialized) {
            init();
            m_initialized = true;
        }
        resetEnvironment();

        // Make sure radar and gun are moving independently
        setAdjustGunForRobotTurn(true);
        setAdjustRadarForGunTurn(true);

//        turnHorizontallyRight();
        while (true) {
//            m_logger.info(m_actionState);

            setTurnRadarRight(Double.POSITIVE_INFINITY);
//            setTurnGunRight(Double.POSITIVE_INFINITY);

            if (m_xpos < getBattleFieldWidth() * 0.2) {
                turnHorizontallyRight();
            } else if (m_xpos > getBattleFieldWidth() * 0.8) {
                turnHorizontallyLeft();
            }
            setAhead(Double.POSITIVE_INFINITY);

            switch (m_actionState) {
                case TO_BE_CHOSEN: {
                    m_stateBeforeAction = new State(m_currentState);

                    Random rand = new Random();
                    if (m_epsilon > rand.nextDouble()) {
                        int actionIndex = rand.nextInt(m_actions.size());
                        m_action = m_actions.get(actionIndex);
                    } else {
                        m_action = m_qtable.bestAction(m_currentState);
                    }

                    if (m_action.getName().equals(m_actionFire)) {
//                        try {
//                            m_logger.info("Strzał oddany przy: " + m_currentState.getParam(m_stateParamAngleToEnemy).getValue() + ", czyli: " + m_currentState.getParam(m_stateParamAngleToEnemy).getQuantizedValue());
//                        } catch (RobotException e) {
//                            e.printStackTrace();
//                        }
                    }

                    performAction(m_action);
                    break;
                }
                case TO_BE_REWARDED: {
                    m_cumulativeReward += m_reward;
//                    if (m_action.getName().equals(m_actionTurnGunLeft)) {
//                        try {
//                            m_logger.info("Nagradzamy przesunięcie w lewo!!");
//                            m_logger.info("Kąt przed akcją " + m_stateBeforeAction.getParam(m_stateParamAngleToEnemy).getValue() + ", czyli: " + m_currentState.getParam(m_stateParamAngleToEnemy).getQuantizedValue());
//                            m_logger.info("Kąt po akcji: " + m_currentState.getParam(m_stateParamAngleToEnemy).getValue() + ", czyli: " + m_currentState.getParam(m_stateParamAngleToEnemy).getQuantizedValue());
//                            m_logger.info("A hipotetyczna nagroda: " + m_qtable.getQ(m_currentState, m_action) + "\n");
//                        } catch (RobotException e) {
//                            e.printStackTrace();
//                        }
//                    }
                    m_qtable.updateRewards(m_stateBeforeAction, m_action, m_reward, m_currentState);
                    saveQTable();
                    m_actionState = ActionState.NONE;
                    break;
                }
            }

            execute();
        }
    }

    private void performAction(Action p_action) {
        m_actionState = ActionState.ONGOING;
        m_reward = 0;

        switch (p_action.getName()) {
            case m_actionTurnGunRight:
                turnGunRight(m_angle);
                m_reward -= 1;
                m_actionState = ActionState.WAITING_FOR_STATE_UPDATE;
                break;
            case m_actionTurnGunLeft:
                turnGunLeft(m_angle);
                m_reward -= 1;
                m_actionState = ActionState.WAITING_FOR_STATE_UPDATE;
                break;
            case m_actionFire:
                if (getGunHeat() > 0) {
                    m_reward -= 1;
                    m_actionState = ActionState.WAITING_FOR_STATE_UPDATE;
                }
                fire(1);
        }
    }

    public void onStatus(StatusEvent e) {
        if (m_currentState == null) {
            return;
        }
        RobotStatus s = e.getStatus();
        var gunHeat = s.getGunHeat();

        m_xpos = s.getX();
        m_currentState.updateParam(m_stateParamGunHeat, gunHeat);
    }

    public void onScannedRobot(ScannedRobotEvent e) {
//        m_logger.info("robot scanned");
        if (m_actionState == ActionState.TO_BE_CHOSEN) {
            return;
        } else if (m_actionState == ActionState.WAITING_FOR_STATE_UPDATE) {
            m_actionState = ActionState.TO_BE_REWARDED;
        }
        if (m_actionState == ActionState.NONE) {
            m_actionState = ActionState.TO_BE_CHOSEN;
        }

        // Simple, but effective radar lock
        setTurnRadarRight(2.0 * Utils.normalRelativeAngleDegrees(getHeading() + e.getBearing() - getRadarHeading()));

        double enemyDistance = e.getDistance();
        m_currentState.updateParam(m_stateParamDistanceToEnemy, enemyDistance);

        m_currentState.updateParam(m_stateParamAngleToEnemy, Utils.normalRelativeAngleDegrees(getHeading() + e.getBearing() - getGunHeading()));
    }

    public void onBulletHit(BulletHitEvent event) {
        m_reward += 20;
        m_actionState = ActionState.WAITING_FOR_STATE_UPDATE;
    }

    public void onBulletMissed(BulletMissedEvent event) {
        m_reward -= 2;
        m_actionState = ActionState.WAITING_FOR_STATE_UPDATE;
    }

    public void onHitWall(HitWallEvent e) {
        bounceFromWall(100);
    }
}
