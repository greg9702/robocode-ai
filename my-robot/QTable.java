package iwium;

import robocode.*;

import java.io.*;
import java.util.*;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

public class QTable
{
  private static final Logger logger = LogManager.getLogger("table");

  // values that describe game profit of performing action in given state
  //   rows -> states
  //   cols -> actions
  private double m_values[][];

  // additional array to store visits count of each states/actions
  private int m_valuesVisits[][];
  private int m_uniqueVisits = 0;

  // list of possible actions
  private ArrayList<Action> m_actions;

  private double m_alphaDivisor; // learning rate - log divisor
  private double m_minAlpha; // learning rate - log divisor

  // internal state
  public double m_alpha = 1.0; // learning rate (always start at 1.0)
  public double m_gamma; // discount factor

  private int m_numStates;

  // 100 means range: <-50, 50>
  // 0 means that all values will be filled with 0s.
  private final int initializationRange = 100;

  public QTable(ArrayList<Action> actions, int numStates, double alphaDivisor, double minAlpha, double gamma)
  {
    m_numStates = numStates;
    m_values = new double[numStates][actions.size()];
    m_valuesVisits = new int[numStates][actions.size()];
    m_uniqueVisits = 0;
    m_actions = actions;
    m_alphaDivisor = alphaDivisor;
    m_minAlpha = minAlpha;
    m_gamma = gamma;

    initialize();
  }

  public void initialize() {
    for (int i = 0; i < m_numStates; i++) {
      for (int j = 0; j < m_actions.size(); j++) {
        m_values[i][j] = (Math.random() - 0.5) * initializationRange;
        m_valuesVisits[i][j] = 0;
      }
    }
    logger.error(m_numStates);
    return;
  }

  /**
   * Dumps current instance to file.
   * @param File
   */
  public void save(RobocodeFileOutputStream fout) throws IOException
  {
    PrintStream w = null;

    w = new PrintStream(fout);
    w.println(m_numStates);
    w.println(m_actions.size());
    for (int i = 0; i < m_numStates; i++) {
      for (int j = 0; j < m_actions.size(); j++)
      {
        w.print(m_values[i][j]);
        w.print(" ");
      }
      w.print("\n");
    }
    if (w.checkError()) {
      logger.error("Could not save the data to file!");
    }
    w.close();
  }

  /**
   * Loads QTable values from file.
   * Note: hyperparams are not preserved.
   * @param File
   */
  public void loadValues(File f) throws FileNotFoundException, IOException
  {
    BufferedReader r = new BufferedReader(new FileReader(f));

    int numStates = Integer.parseInt(r.readLine());
    int numActions = Integer.parseInt(r.readLine());
    if (numStates != m_numStates || numActions != m_actions.size()) {
      throw new IOException("Incompatible size of QTable");
    }

    for (int i = 0; i < numStates; i++) {
      String[] actionsVals = r.readLine().split(" ");
      for (int j = 0; j < numActions; j++) {
        m_values[i][j] = Double.parseDouble(actionsVals[j]);
      }
    }

    return;
  }

  /**
   * Updates reward in QTable.
   * @param State s_old
   * @param Action a
   * @param double reward
   * @param State s_new
   */
  public void updateRewards(State s_old, Action a, double reward, State s_new)
  {
    double Q1 = getQ(s_old, a);
    Action bestNewAction = bestAction(s_new);
    double maxQ = getQ(s_new, bestNewAction);

    double updatedQ = Q1 + m_alpha * (reward + m_gamma * maxQ - Q1);
    setQ(s_old, a, updatedQ);

    return;
  }

  double getQ(State state, Action a)
  {
    int stateId = state.getRowId();
    int actionId = m_actions.indexOf(a);
    return m_values[stateId][actionId];
  }

  void setQ(State state, Action a, double Q)
  {
    int stateId = state.getRowId();
    int actionId = m_actions.indexOf(a);
    m_values[stateId][actionId] = Q;

    // save visit
    if (m_valuesVisits[stateId][actionId] == 0) {
      m_uniqueVisits += 1;
    }
    m_valuesVisits[stateId][actionId] += 1;

    return;
  }

  /**
   * Finds best known action for given state.
   * @param State state
   */
  Action bestAction(State state)
  {
    Action bestAction = m_actions.get(0);
    Double bestValue = getQ(state, bestAction);

    for (Action action: m_actions) {
      double value = getQ(state, action);
      if (value > bestValue) {
        bestAction = action;
        bestValue = value;
      }
    }

    return bestAction;
  }

  /**
   * Gets number of already explored states.
   * @return int
   */
  public int getNumberOfExploredStates()
  {
    return m_uniqueVisits;
  }

  /**
   * Updates learning rate.
   * @param long loops
   * @return double
   */
  public double updateRates(long loops)
  {
    double min = m_minAlpha;
    double max = 1.0;
    double value = Math.max(min, Math.min(max, max - Math.log10(loops / m_alphaDivisor)));
    return value;
  }

}  // class QTable
