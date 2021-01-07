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
    private enum ActionState {
        NONE, TO_BE_CHOSEN, ONGOING, TO_BE_REWARDED
    }

    private static final String m_stateParamAngleToEnemy = "angleToEnemy";
    private static final int m_angleToEnemyBins = 16;
    private static final String m_stateParamDistanceToEnemy = "distanceToEnemy";
    private static final int m_distanceToEnemyBins = 8;
    private static final String m_stateParamGunHeat = "gunHeat";
    private static final int m_gunHeatBins = 2;
    private static final String m_actionFire = "fire";
    private static final String m_actionDoNothing = "nothing";

    private ActionState m_actionState = ActionState.NONE;
    private double m_xpos = 0;
    State m_stateBeforeAction = null;
    Action m_action = null;

    private void init() {
        m_logger = LogManager.getLogger("hunterbase");
        m_qtableFilename = "hunterQtable.bin";
    }

    private void resetEnvironment() {
        m_actions = new ArrayList<>();
        m_actions.add(new Action(0, m_actionFire));
        m_actions.add(new Action(1, m_actionDoNothing));

        final int maxDistance = (int) Math.sqrt(Math.pow(getBattleFieldWidth(), 2) + Math.pow(getBattleFieldHeight(), 2));

        m_currentState = new State(new ArrayList<>(Arrays.asList(
                new Param(m_stateParamAngleToEnemy, 0, 360, m_angleToEnemyBins),
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

        turnHorizontallyRight();
        while (true) {
            setTurnRadarRight(Double.POSITIVE_INFINITY);
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
            }

            execute();
        }
    }

    private void performAction(Action p_action) {
        m_actionState = ActionState.ONGOING;
        m_reward = 0;

        switch (p_action.getName()) {
            case m_actionDoNothing:
                m_actionState = ActionState.TO_BE_REWARDED;
                break;
            case m_actionFire:
                if (getGunHeat() > 0)
                {
                    m_reward -= 1;
                    m_actionState = ActionState.TO_BE_REWARDED;
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
        if (m_actionState != ActionState.NONE) {
            return;
        }

        m_actionState = ActionState.TO_BE_CHOSEN;

        // Simple, but effective radar lock
//        setTurnRadarRight(2.0 * Utils.normalRelativeAngleDegrees(getHeading() + e.getBearing() - getRadarHeading()));

        double enemyDistance = e.getDistance();
        m_currentState.updateParam("distanceToEnemy", enemyDistance);

        double bearing = e.getBearing();
        double absBearing = bearing;
        m_currentState.updateParam("angleToEnemy", absBearing);

//        if (!m_hasScannedEnemy) {
        // Turn gun towards enemy
        setTurnGunRight(Utils.normalRelativeAngleDegrees(getHeading() + e.getBearing() - getGunHeading()));
//        }

//        m_hasScannedEnemy = true;
    }

    public void onBulletHit(BulletHitEvent event) {
        m_reward += 20;
        m_actionState = ActionState.TO_BE_REWARDED;
    }

    public void onBulletMissed(BulletMissedEvent event) {
        m_reward -= 1;
        m_actionState = ActionState.TO_BE_REWARDED;
    }
}
