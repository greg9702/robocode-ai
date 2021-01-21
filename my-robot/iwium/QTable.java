package iwium;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import robocode.RobocodeFileOutputStream;

import java.io.*;
import java.util.ArrayList;
import java.util.HashMap;

public class QTable
{
  private static final Logger logger = LogManager.getLogger("base");
  private static final Logger loggerTable = LogManager.getLogger("table");

  private final double[][] m_values;

  private final int[][] m_valuesVisits;
  private int m_uniqueVisits;

  private final HashMap<String, Double> m_valuesV2;

  private final ArrayList<Action> m_actions;

  private final double m_alphaDivisor;
  private final double m_minAlpha;

  public double m_alpha = 1.0;
  public double m_gamma;

  private final int m_numStates;

  public QTable(ArrayList<Action> actions, int numStates, double alphaDivisor, double minAlpha, double gamma)
  {
    m_numStates = numStates;
    m_values = new double[numStates][actions.size()];
    m_valuesVisits = new int[numStates][actions.size()];
    m_uniqueVisits = 0;
    m_valuesV2 = new HashMap<>();
    m_actions = actions;
    m_alphaDivisor = alphaDivisor;
    m_minAlpha = minAlpha;
    m_gamma = gamma;

    initialize();
  }

  public void initialize() {
    for (int i = 0; i < m_numStates; i++) {
      for (int j = 0; j < m_actions.size(); j++) {
        int initializationRange = 10;
        m_values[i][j] = (Math.random() - 0.5) * initializationRange;
        m_valuesVisits[i][j] = 0;
      }
    }
  }

  public void save(RobocodeFileOutputStream fout) throws IOException
  {
    for (String key : m_valuesV2.keySet()) {
      loggerTable.debug(key + ": " + m_valuesV2.get(key));
    }

    PrintStream w;
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

  public void loadValues(File f) throws IOException
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
  }

  public void updateRewards(State s_old, Action a, double reward, State s_new)
  {
    double Q1 = getQ(s_old, a);
    Action bestNewAction = bestAction(s_new);
    double maxQ = getQ(s_new, bestNewAction);

    double updatedQ = Q1 + 0.1 * (reward + m_gamma * maxQ - Q1);
    setQ(s_old, a, updatedQ);

    String key = "";
    key += s_old.getStringKey();
    key += ";";
    key += a.getStringKey();
    m_valuesV2.put(key, updatedQ);
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

    if (m_valuesVisits[stateId][actionId] == 0) {
      m_uniqueVisits += 1;
    }
    m_valuesVisits[stateId][actionId] += 1;
  }

  Action bestAction(State state)
  {
    Action bestAction = m_actions.get(0);
    double bestValue = getQ(state, bestAction);

    for (Action action: m_actions) {
      double value = getQ(state, action);
      if (value > bestValue) {
        bestAction = action;
        bestValue = value;
      }
    }

    return bestAction;
  }

  public int getNumberOfExploredStates()
  {
    return m_uniqueVisits;
  }

  public void updateRates(long rounds)
  {
    double max = 1.0;
    m_alpha = Math.max(m_minAlpha, Math.min(max, max - Math.log10((double)rounds / m_alphaDivisor)));
  }

}
