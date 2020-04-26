package iwium;

import robocode.*;
import java.awt.Color;
import java.io.*;
import java.util.*;

// API help : https://robocode.sourceforge.io/docs/robocode/robocode/Robot.html

/**
 * QnRobot - a robot by Andrzej Broński
 */
public class QLearningRobot extends AdvancedRobot
{
  // whether to use fresh QTable instead of loading from file
  final boolean USE_FRESH_QTABLE = false;

  // filename used for QTable dump
  final String QTABLE_FILENAME = "qtable.bin";

  private double m_cumulativeReward;
  private double m_reward;
  private QTable m_qtable;
  private State m_currentState;

  // learning params
  private double m_epsilon = 1.0; // experiment rate

  // QLearning environment params
  String m_robotXPosParamName;
  int m_robotXPos_bins = 8;
  String m_robotYPosParamName;
  int m_robotYPos_bins = 6;
  String m_absAngleToEnemyParamName;
  int m_absAngleToEnemy_bins = 4;
  String m_distanceToEnemyParamName;
  int m_distanceToEnemy_bins = 4;

  // QLearning environment actions
  private ArrayList<Action> m_actions;
  private static final String m_actionFire2 = "fire2";
  private static final String m_actionFrontLeft = "frontLeft";
  private static final String m_actionFrontRight = "frontRight";
  private static final String m_actionBackLeft = "backLeft";
  private static final String m_actionBackRight = "backRight";

  // rewards
  private double m_hitRobotReward = -2;
  private double m_bulletHitReward = 3;
  private double m_hitByBulletReward = -3;
  private double m_bulletMissedReward = 0;
  private double m_hitWallReward = -3.5;

  public QLearningRobot()
  {
    System.out.println("Constructor invoked.");
    m_cumulativeReward = 0;

    int maxDistance = (int)Math.sqrt(Math.pow(getBattleFieldWidth(), 2) + Math.pow(getBattleFieldHeight(), 2));
    m_currentState = new State(new ArrayList<>(Arrays.asList(
      new Param(m_robotXPosParamName, 0, getBattleFieldWidth(), m_robotXPos_bins),
      new Param(m_robotYPosParamName, 0, getBattleFieldHeight(), m_robotYPos_bins),
      new Param(m_absAngleToEnemyParamName, 0, 360, m_absAngleToEnemy_bins),
      new Param(m_distanceToEnemyParamName, 0, maxDistance, m_distanceToEnemy_bins)
    )));

    m_actions = new ArrayList<Action>();
    m_actions.add(new Action(0, m_actionFire2));
    m_actions.add(new Action(1, m_actionFrontLeft));
    m_actions.add(new Action(2, m_actionFrontRight));
    m_actions.add(new Action(3, m_actionBackLeft));
    m_actions.add(new Action(4, m_actionBackRight));
  }

  /**
   * Initializes QTable, either by loading dump
   * or creating fresh instance (depending on settings).
   */
  private void initQTable()
  {
    if (USE_FRESH_QTABLE != false) {
      File dumpFile = getDataFile(QTABLE_FILENAME);
      try {
        m_qtable = QTable.load(dumpFile);
        return;
      } catch (Exception e) {
        System.out.println("[Error] Unable to load QTable:" + e);
        System.out.println("Fallback to fresh QTable instance.");
      }
    }
    m_qtable = new QTable(m_actions);
  }

  /**
   * Saves current QTable state into dump file.
   */
  private void saveQTable()
  {
    File dumpFile = getDataFile(QTABLE_FILENAME);
    try {
      RobocodeFileOutputStream fstream = new RobocodeFileOutputStream(dumpFile);
      m_qtable.save(fstream);
    } catch (Exception e) {
      System.out.println("[Error] Unable to save QTable: " + e);
    }
  }

  /**
   * run: QLearningRobot's default behavior
   */
  public void run()
  {
    System.out.println("run() invoked.");
    initQTable();

    // Initialization of the robot should be put here
    setColors(Color.green, Color.black, Color.black); // body,gun,radar

    // Robot main loop
    while (true) {

      State stateBeforeAction = new State(m_currentState);
      Action action;
      Random rand = new Random();
      if (m_epsilon > rand.nextDouble()) {
        // pick random action
        int actionIndex = rand.nextInt(m_actions.size());
        action = m_actions.get(actionIndex);
      } else {
        // pick best action
        action = m_qtable.findBestAction(m_currentState);
      }

      // Reset reward and execute
      m_reward = 0;
      //turnGunRight(360); // ???
      performAction(action);
      //turnGunRight(360); // ???

      // TODO consider adding difference between our and enemy
      // energy levels to reward.

      // Update rewards
      m_qtable.updateRewards(stateBeforeAction, action, m_reward, m_currentState);
      m_cumulativeReward += m_reward;

      saveQTable();
    }
  }

  /**
   * What to do when our robot scanned enemy.
   */
  public void onScannedRobot(ScannedRobotEvent e)
  {
    double enemyDistance = e.getDistance();
    m_currentState.updateParam(m_distanceToEnemyParamName, enemyDistance);

    double bearing = e.getBearing();
    double absBearing = bearing + 180;
    m_currentState.updateParam(m_absAngleToEnemyParamName, absBearing);

    //fire(1/2/3) // we want AI learn to fire by itself

    return;
  }

  /**
   * onRoundEnded: What to do when round is ended
   */
  public void onRoundEnded(RoundEndedEvent e)
  {
    System.out.println("Cumulative reward: " + m_cumulativeReward + ".");
    System.out.println("Round finished.");
  }

  /**
   * onBattleEnded: What to do when battle is ended
   */
  public void onBattleEnded(BattleEndedEvent e)
  {
    System.out.println("Battle finished.");
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
    bounceFromWall(150);
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
    int wallCase = Arrays.asList(wallDistances).indexOf(minDistance);
    double angleDiff = 0;
    switch (wallCase) {
      case 0: // left
        angleDiff = currentAngle - 270;
        break;
      case 1: // bottom
        angleDiff = currentAngle - 180;
        break;
      case 2: // right
        angleDiff = currentAngle - 90;
        break;
      case 3: // top
        angleDiff = currentAngle - 0;
        break;
      default:
    }
    if (angleDiff >= 0) {
      turnLeft(angleDiff);
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
    RobotStatus s = e.getStatus();
    double xPos = s.getX();
    double yPos = s.getY();
    m_currentState.updateParam(m_robotXPosParamName, xPos);
    m_currentState.updateParam(m_robotYPosParamName, yPos);
    return;
  }

  /**
   * Performs one of defined actions.
   * @param Action a
   */
  private void performAction(Action action)
  {
    double moveDistance = 150;
    double firePower = 2;
    double rotationDegrees = 30;

    String name = action.getName();
    switch (name) {
      case m_actionFire2:
        // TODO consider turning gun toward enemy
        fire(firePower);
        break;
      case m_actionFrontLeft:
        turnLeft(rotationDegrees);
        ahead(moveDistance);
        break;
      case m_actionFrontRight:
        turnRight(rotationDegrees);
        ahead(moveDistance);
        break;
      case m_actionBackLeft:
        turnLeft(rotationDegrees);
        back(moveDistance);
        break;
      case m_actionBackRight:
        turnRight(rotationDegrees);
        back(moveDistance);
        break;
      default:
        System.out.println("Error: Unknown action!");
    }
    // TODO consider using turnGun[Left/Right]

    return;
  }

}  // class QLearningRobot
