package iwium;

import org.apache.logging.log4j.LogManager;
import robocode.*;
import robocode.util.Utils;

import java.awt.*;
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
    private static final int m_distanceToEnemyBins = 16;
    private static final String m_actionFire = "fire";
    private static final String m_actionTurnRight = "turnRight";
    private static final String m_actionTurnLeft = "turnLeft";
    private static final String m_actionMoveAhead = "moveAhead";

    private double m_xpos = 0;
    private double m_enemyDistance = 0;
    private final double m_angle = 10;
    private final double m_distance = 50;
    State m_stateBeforeAction = null;
    Action m_action = null;

    private void init() {
        m_logger = LogManager.getLogger("hunterbase");
        m_qtableFilename = "hunterQtable.bin";
    }

    private void resetEnvironment() {
        m_actions = new ArrayList<>();
        m_actions.add(new Action(0, m_actionFire));
        m_actions.add(new Action(3, m_actionTurnRight));
        m_actions.add(new Action(4, m_actionTurnLeft));
        m_actions.add(new Action(5, m_actionMoveAhead));

        final int maxDistance = (int) Math.sqrt(Math.pow(getBattleFieldWidth(), 2) + Math.pow(getBattleFieldHeight(), 2));

        m_currentState = new State(new ArrayList<>(Arrays.asList(
                new Param(m_stateParamAngleToEnemy, -180, 180, m_angleToEnemyBins),
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

        setAdjustRadarForGunTurn(true);
        setColors(Color.green, Color.black, Color.red);

        while (true) {
            setTurnRadarRight(Double.POSITIVE_INFINITY);

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

                    performAction(m_action);
                    break;
                }
                case TO_BE_REWARDED: {
                    m_cumulativeReward += m_reward;
                    m_qtable.updateRewards(m_stateBeforeAction, m_action, m_reward, m_currentState);
                    saveQTable();
                    m_actionState = ActionState.NONE;
                    break;
                }
                case ONGOING: {
                    execute();
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
            case m_actionFire:
                if (getGunHeat() > 0) {
                    m_actionState = ActionState.WAITING_FOR_STATE_UPDATE;
                }
                fire(400 / m_enemyDistance);
                break;
            case m_actionTurnRight:
                turnRight(m_angle);
                m_reward -= 1;
                m_actionState = ActionState.WAITING_FOR_STATE_UPDATE;
                break;
            case m_actionTurnLeft:
                turnLeft(m_angle);
                m_actionState = ActionState.WAITING_FOR_STATE_UPDATE;
                m_reward -= 1;
                break;
            case m_actionMoveAhead:
                ahead(m_distance);
                m_actionState = ActionState.WAITING_FOR_STATE_UPDATE;
                m_reward -= 1;
                break;
        }
    }

    public void onStatus(StatusEvent e) {
        if (m_currentState == null) {
            return;
        }
        RobotStatus s = e.getStatus();
        var gunHeat = s.getGunHeat();

        m_xpos = s.getX();
    }

    public void onScannedRobot(ScannedRobotEvent e) {
        if (m_actionState == ActionState.TO_BE_CHOSEN) {
            return;
        } else if (m_actionState == ActionState.WAITING_FOR_STATE_UPDATE) {
            m_actionState = ActionState.TO_BE_REWARDED;
        }
        if (m_actionState == ActionState.NONE) {
            m_actionState = ActionState.TO_BE_CHOSEN;
        }

        m_enemyDistance = e.getDistance();
        m_currentState.updateParam(m_stateParamDistanceToEnemy, m_enemyDistance);

        m_currentState.updateParam(m_stateParamAngleToEnemy, Utils.normalRelativeAngleDegrees(getHeading() + e.getBearing() - getGunHeading()));
    }

    public void onBulletHit(BulletHitEvent event) {
        m_reward += 20;
        m_actionState = ActionState.WAITING_FOR_STATE_UPDATE;
    }

    public void onHitByBullet(HitByBulletEvent event) {
        m_reward -= 5;
    }

    public void onBulletMissed(BulletMissedEvent event) {
        m_reward -= 2;
        m_actionState = ActionState.WAITING_FOR_STATE_UPDATE;
    }

    public void onHitWall(HitWallEvent e) {
        m_reward -= 10;
        bounceFromWall();
    }
}
