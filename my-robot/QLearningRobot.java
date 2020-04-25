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
  private QTable m_qtable;
  private State m_currentState;

  // QLearning params
  Param m_robotXPos;
  int m_robotXPos_bins = 8;
  Param m_robotYPos;
  int m_robotYPos_bins = 6;
  Param m_absAngleToEnemy;
  int m_absAngleToEnemy_bins = 4;
  Param m_distanceToEnemy;
  int m_distanceToEnemy_bins = 4;

  public QLearningRobot()
  {
    System.out.println("Constructor invoked.");
    m_cumulativeReward = 0;

    m_robotXPos = new Param("robotXPos", 0, getBattleFieldWidth(), m_robotXPos_bins);
    m_robotYPos = new Param("robotYPos", 0, getBattleFieldHeight(), m_robotYPos_bins);
    m_absAngleToEnemy = new Param("absAngleToEnemy", 0, 360, m_absAngleToEnemy_bins);
    int maxDistance = (int)Math.sqrt(Math.pow(getBattleFieldWidth(), 2) + Math.pow(getBattleFieldHeight(), 2));
    m_distanceToEnemy = new Param("absAngleToEnemy", 0, maxDistance, m_distanceToEnemy_bins);
    m_currentState = new State(new ArrayList<>(Arrays.asList(
      m_robotXPos,
      m_robotYPos,
      m_absAngleToEnemy,
      m_distanceToEnemy
    )));
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
    m_qtable = new QTable();
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
    while(true) {
      // Replace the next 4 lines with any behavior you would like
      ahead(100);
      turnGunRight(360);
      back(100);
      turnGunRight(360);

      saveQTable();
    }
  }

  /**
   * onScannedRobot: What to do when you see another robot
   */
  public void onScannedRobot(ScannedRobotEvent e)
  {
    // Replace the next line with any behavior you would like
    fire(1);
  }

  /**
   * onHitByBullet: What to do when you're hit by a bullet
   */
  public void onHitByBullet(HitByBulletEvent e)
  {
    // Replace the next line with any behavior you would like
    back(10);
  }

  /**
   * onHitWall: What to do when you hit a wall
   */
  public void onHitWall(HitWallEvent e)
  {
    // Replace the next line with any behavior you would like
    back(20);
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

}  // class QLearningRobot
