package iwium;

import robocode.*;
import robocode.util.*;
import java.awt.Color;
import java.io.*;
import java.util.*;
import java.lang.*;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

// API help : https://robocode.sourceforge.io/docs/robocode/robocode/Robot.html

/**
 * QnRobot - a robot by Andrzej Broński
 */
public class QLearningRobot extends AdvancedRobot
{
  private static final Logger logger = LogManager.getLogger("base");
  private static final Logger loggerRewards = LogManager.getLogger("rewards");
  private static final Logger loggerStates = LogManager.getLogger("states");
  private static final Logger loggerEnergy = LogManager.getLogger("energy");
  private static final Logger loggerEnv = LogManager.getLogger("environment");
  private static final Logger loggerHyperparams = LogManager.getLogger("hyperparams");

  // whether to use fresh QTable instead of loading from file
  final boolean USE_FRESH_QTABLE = false;

  // filename used for QTable dump
  final String QTABLE_FILENAME = "qtable.bin";

  private double m_cumulativeReward;
  private double m_reward;
  private State m_currentState;

  private static QTable m_qtable = null;

  // provided from env
  private static int m_learningRounds;
  private static int m_testingRounds;
  private static double m_alphaDivisor;
  private static double m_minAlpha;
  private static double m_gamma;

  // QLearning environment params
  String m_robotXPosParamName = "m_robotXPosParamName";
  int m_robotXPos_bins = 16;
  String m_robotYPosParamName = "m_robotYPosParamName";
  int m_robotYPos_bins = 12;
  String m_robotHeadingParamName = "m_robotHeadingParamName";
  int m_robotHeadingParamName_bins = 16;
  String m_robotGunHeadingParamName = "m_robotGunHeadingParamName";
  int m_robotGunHeadingParamName_bins = 12;
  String m_absAngleToEnemyParamName = "m_absAngleToEnemyParamName";
  int m_absAngleToEnemy_bins = 12;
  String m_distanceToEnemyParamName = "m_distanceToEnemyParamName";
  int m_distanceToEnemy_bins = 12;

  // temporary store to remember things across many actions
  Double m_lastAbsAngleToEnemy = null;

  // QLearning environment actions
  private static ArrayList<Action> m_actions;
  private static final String m_actionFire2 = "fire2";
  private static final String m_actionFront = "front";
  private static final String m_actionFrontLeft = "frontLeft";
  private static final String m_actionFrontRight = "frontRight";
  private static final String m_actionBack = "back";
  private static final String m_actionBackLeft = "backLeft";
  private static final String m_actionBackRight = "backRight";

  // rewards
  private double m_hitRobotReward = -10;
  private double m_bulletHitReward = 30;
  private double m_hitByBulletReward = -50;
  private double m_bulletMissedReward = 0;
  private double m_hitWallReward = -6;
  private double m_aliveReward = 1;

  private static int m_currentRound = 0;

  // whether first time initialization was completed
  static boolean initialized = false;

  boolean waitingForQAction = false;

  public QLearningRobot()
  {
    logger.debug("QLearningRobot constructor called.");
  }

  /**
   * Initializes all necessary things like QTable.
   * It should be called only once in the whole battle!
   */
  public void init()
  {
    logger.debug("init() called");
    m_actions = new ArrayList<Action>();
    m_actions.add(new Action(0, m_actionFire2));
    m_actions.add(new Action(1, m_actionFront));
    m_actions.add(new Action(2, m_actionFrontLeft));
    m_actions.add(new Action(3, m_actionFrontRight));
    m_actions.add(new Action(4, m_actionBack));
    m_actions.add(new Action(5, m_actionBackRight));
    m_actions.add(new Action(6, m_actionBackRight));

    m_learningRounds = Integer.parseInt(System.getProperty("trainRounds"));
    m_testingRounds = Integer.parseInt(System.getProperty("testRounds"));
    m_alphaDivisor = Double.parseDouble(System.getProperty("alphaDivisor"));
    m_minAlpha = Double.parseDouble(System.getProperty("minAlpha"));
    m_gamma = Double.parseDouble(System.getProperty("gamma"));

    // save ENV configuration
    loggerEnv.info(m_learningRounds);
    loggerEnv.info(m_testingRounds);
  }

  /**
   * We have to use custom reset environment imediatelly after run().
   * Note: in constructor we cannot call a lot of robocode method.
   */
  public void resetEnvironment()
  {
    logger.debug("resetEnvironment() invoked.");
    m_cumulativeReward = 0;

    int maxDistance = (int)Math.sqrt(Math.pow(getBattleFieldWidth(), 2) + Math.pow(getBattleFieldHeight(), 2));
    m_currentState = new State(new ArrayList<>(Arrays.asList(
      new Param(m_robotXPosParamName, 0, getBattleFieldWidth(), m_robotXPos_bins),
      new Param(m_robotYPosParamName, 0, getBattleFieldHeight(), m_robotYPos_bins),
      new Param(m_robotHeadingParamName, 0, 360, m_robotHeadingParamName_bins),
      new Param(m_robotGunHeadingParamName, 0, 360, m_robotGunHeadingParamName_bins),
      new Param(m_absAngleToEnemyParamName, 0, 360, m_absAngleToEnemy_bins),
      new Param(m_distanceToEnemyParamName, 0, maxDistance, m_distanceToEnemy_bins)
    )));

    initQTable();

    // log round params
    loggerHyperparams.info(m_qtable.m_alpha + "|" + getEpsilon() + "|" + m_qtable.m_gamma);

    return;
  }

  /**
   * Initializes QTable, either by loading dump
   * or using fresh instance (depending on settings).
   */
  private void initQTable()
  {
    // Prevent multiple initializations
    if (m_qtable != null) {
      return;
    }

    int numStates = m_currentState.getNumStates();
    m_qtable = new QTable(m_actions, numStates, m_alphaDivisor, m_minAlpha, m_gamma);

    if (USE_FRESH_QTABLE == false) {
      File dumpFile = getDataFile(QTABLE_FILENAME);
      try {
        logger.debug("Loading QTable state.");
        m_qtable.loadValues(dumpFile);
      } catch (Exception e) {
        logger.error("Unable to load QTable: " + e);
        logger.error("Fresh QTable will be used instead.");
      }
    }

    return;
  }

  /**
   * Saves current QTable state into dump file.
   */
  private void saveQTable()
  {
    logger.debug("Saving QTable state.");
    File dumpFile = getDataFile(QTABLE_FILENAME);
    try {
      RobocodeFileOutputStream fstream = new RobocodeFileOutputStream(dumpFile);
      m_qtable.save(fstream);
    } catch (Exception e) {
      logger.error("Unable to save QTable: " + e);
    }
    logger.info("QTable saved!");
  }

  /**
   * run: QLearningRobot's default behavior
   */
  public void run()
  {
    logger.debug("run() invoked.");
    m_currentRound += 1;
    if (initialized == false) {
      init();
      initialized = true;
    }
    resetEnvironment();

    // Make sure radar and gun are moving independently
    setAdjustGunForRobotTurn(true);
    setAdjustRadarForGunTurn(true);

    // Initialization of the robot should be put here
    setColors(Color.green, Color.black, Color.black); // body,gun,radar

    // Run radar scan as fast as possible - this is our first action
    setTurnRadarRight(Double.POSITIVE_INFINITY);
    execute();

    // Robot main loop
    while (true) {

      State stateBeforeAction = new State(m_currentState);
      Action action;
      Random rand = new Random();
      double epsilon = getEpsilon();
      if (epsilon > rand.nextDouble()) {
        // pick random action
        int actionIndex = rand.nextInt(m_actions.size());
        action = m_actions.get(actionIndex);
      } else {
        // pick best action
        action = m_qtable.bestAction(m_currentState);
      }

      // If we already picked action then we can clear dynamic params,
      // so they won't be reused in next steps
      m_currentState.updateParam(m_distanceToEnemyParamName, null);
      m_currentState.updateParam(m_absAngleToEnemyParamName, null);

      // Reset reward and execute
      m_reward = 0;
      performAction(action);

      // TODO consider adding difference between our and enemy
      // energy levels to reward.

      // Update rewards
      m_reward += m_aliveReward; // around 40 times per round
      m_qtable.updateRewards(stateBeforeAction, action, m_reward, m_currentState);
      m_cumulativeReward += m_reward;

      m_qtable.updateRates(m_currentRound);

      waitingForQAction = false;
    }
  }

  /**
   * What to do when our robot scanned enemy.
   */
  public void onScannedRobot(ScannedRobotEvent e)
  {
    // Simple, but effective radar lock
    setTurnRadarRight(2.0 * Utils.normalRelativeAngleDegrees(getHeading() + e.getBearing() - getRadarHeading()));

    double enemyDistance = e.getDistance();
    m_currentState.updateParam(m_distanceToEnemyParamName, enemyDistance);

    double bearing = e.getBearing();
    double absBearing = bearing;
    m_currentState.updateParam(m_absAngleToEnemyParamName, absBearing);
    m_lastAbsAngleToEnemy = absBearing; // tmp store, for future usage

    if (waitingForQAction == false) {
      // Turn gun toward enemy
      setTurnGunRight(Utils.normalRelativeAngleDegrees(getHeading() + e.getBearing() - getGunHeading()));
    }

    waitingForQAction = true;

    return;
  }

  /**
   * onRoundEnded: What to do when round is ended
   */
  public void onRoundEnded(RoundEndedEvent e)
  {
    loggerRewards.debug(m_cumulativeReward);
    loggerStates.debug(m_qtable.getNumberOfExploredStates());
    logger.debug("Round finished.");
    loggerEnergy.debug(getEnergy());
  }

  /**
   * onBattleEnded: What to do when battle is ended
   */
  public void onBattleEnded(BattleEndedEvent e)
  {
    logger.debug("Battle finished.");
    saveQTable();
  }

  /**
   * What to do when our robot collides with enemy.
   */
  public void onHitRobot(HitRobotEvent event) {
    m_reward += m_hitRobotReward;
    return;
  }

  /**
   * What to do when our robot hit enemy.
   */
  public void onBulletHit(BulletHitEvent event) {
    m_reward += m_bulletHitReward;
    return;
  }

  /**
   * What to do when our robot got bullet from enemy.
   */
  public void onHitByBullet(HitByBulletEvent event) {
    m_reward += m_hitByBulletReward;
    return;
  }

  /**
   * What to do when our robot misses.
   */
  public void onBulletMissed(BulletMissedEvent event) {
    m_reward += m_bulletMissedReward;
    return;
  }

  /**
   * What to do when robot hits wall.
   */
  public void onHitWall(HitWallEvent e) {
    m_reward += m_hitWallReward;
    bounceFromWall(100);
    // Rerun radar scanning
    setTurnRadarRight(Double.POSITIVE_INFINITY);
    execute();
    return;
  }

  /**
   * Finds closest wall and runs toward opposite direction.
   * @param int safeDistance
   */
  private void bounceFromWall(int safeDistance)
  {
    double fieldWidth = getBattleFieldWidth();
    double fieldHeight = getBattleFieldHeight();
    double xPos = getX();
    double yPos = getY();
    double currentAngle = getHeading();
    // distances to walls: left, bottom, right, top
    double[] wallDistances = {xPos, yPos, fieldWidth-xPos, fieldHeight-yPos};
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
      logger.error("Unknown wall collision!");
    }
    if (angleDiff > 180) {
      turnLeft(angleDiff  - 180);
    } else {
      turnRight(angleDiff);
    }
    // note: minDistance is probably always equal to 0
    ahead(safeDistance - minDistance);
  }

  /**
   * What to do on every status (tick) update.
   */
  public void onStatus(StatusEvent e)
  {
    if (m_currentState == null) {
      logger.info("Init not called yet. Skipping onStatus.");
      return;
    }
    RobotStatus s = e.getStatus();
    double xPos = s.getX();
    double yPos = s.getY();
    double heading = s.getHeading();
    double gunHeading = s.getGunHeading();
    m_currentState.updateParam(m_robotXPosParamName, xPos);
    m_currentState.updateParam(m_robotYPosParamName, yPos);
    m_currentState.updateParam(m_robotHeadingParamName, heading);
    m_currentState.updateParam(m_robotGunHeadingParamName, gunHeading);
    return;
  }

  /**
   * Performs one of defined actions.
   * @param Action a
   */
  private void performAction(Action action)
  {
    double moveDistance = 100;
    double firePower = 2;
    double rotationDegrees = 60;

    String name = action.getName();
    switch (name) {
      case m_actionFire2:
        Double enemyAngle = m_lastAbsAngleToEnemy;
        // Little help, point gun toward enemy
        if (enemyAngle != null) {
          turnGunRight(enemyAngle);
        }
        // Shoot!
        fire(firePower);
        break;
      case m_actionFront:
        ahead(moveDistance);
        break;
      case m_actionFrontLeft:
        setTurnLeft(rotationDegrees);
        execute(); // start turn, but does not wait until complete
        ahead(moveDistance);
        break;
      case m_actionFrontRight:
        setTurnRight(rotationDegrees);
        execute(); // start turn, but does not wait until complete
        ahead(moveDistance);
        break;
      case m_actionBackLeft:
        setTurnLeft(rotationDegrees);
        execute(); // start turn, but does not wait until complete
        back(moveDistance);
        break;
      case m_actionBackRight:
        setTurnRight(rotationDegrees);
        execute(); // start turn, but does not wait until complete
        back(moveDistance);
        break;
      case m_actionBack:
        back(moveDistance);
        break;
      default:
        logger.error("Unknown action!");
    }
    // TODO consider using turnGun[Left/Right]

    return;
  }

  /**
   * Gets experiment rate.
   * @return double
   */
  private double getEpsilon()
  {
    // A: Fixed value
    /*return 0.2;*/

    // B: Steps decrease
    /*int adaDivisor = 7 * m_learningRounds;
    double min = 0.1;
    double max = 1;
    double value = Math.max(min, Math.min(max, max - Math.log10(m_currentRound / adaDivisor)));
    return value;*/

    // C: Tangens decrease
    double tanArg = (double)m_currentRound / (double)m_learningRounds * 0.785398;
    double value = Math.max(0.0, 1.0 - Math.tan(tanArg));
    return value;

    // C: Firstly explore, then switch to optimal policy
    /*if (getRoundNum() < m_learningRounds) {
      return 1;
    }
    return 0;*/
  }

}  // class QLearningRobot
