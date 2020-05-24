package iwium;

import robocode.*;

import java.io.*;
import java.util.*;
import java.io.Serializable;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

public class QTable implements Serializable
{
  private static final Logger logger = LogManager.getLogger("table");

  // values that describe game profit of performing action in given state
  private HashMap<String, Double> m_values;

  // list of possible actions
  private ArrayList<Action> m_actions;

  private double m_alphaDivisor; // learning rate - log divisor
  private double m_minAlpha; // learning rate - log divisor

  // internal state
  private double m_alpha = 1.0; // learning rate (always start at 1.0)
  private double m_gamma; // discount factor

  public QTable(ArrayList<Action> actions, double alphaDivisor, double minAlpha, double gamma)
  {
    m_values = new HashMap<String,Double>();
    m_actions = actions;
    m_alphaDivisor = alphaDivisor;
    m_minAlpha = minAlpha;
    m_gamma = gamma;
  }

  /**
   * Dumps current instance to file.
   * @param File
   */
  public void save(RobocodeFileOutputStream fout) throws IOException
  {
    logger.debug("Map keys: " + m_values.keySet().size() + ", size: " + m_values.size());
    for (String key : m_values.keySet()) {
      logger.debug(key + ": " + m_values.get(key));
    }
    ObjectOutputStream oos = new ObjectOutputStream(fout);
    oos.writeObject(this);
    oos.close();
  }

  /**
   * Loads serialized QTable from file.
   * @param File
   */
  public static QTable load(File f) throws FileNotFoundException, IOException, ClassNotFoundException
  {
    FileInputStream fi = new FileInputStream(f);
    ObjectInputStream ois = new ObjectInputStream(fi);
    QTable table = (QTable) ois.readObject();
    ois.close();
    return table;
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
    String key = makeHashmapKey(s_old, a);
    // init key if not exists
    if (m_values.containsKey(key) == false) {
      m_values.put(key, new Double(0));
    }

    Action bestAction = findBestAction(s_new);
    String keyNew = makeHashmapKey(s_new, bestAction);
    double new_state_value = m_values.getOrDefault(keyNew, 0.0);

    double current_value = m_values.get(key);
    double new_value = current_value + m_alpha * (reward + m_gamma * new_state_value - current_value);
    m_values.put(key, new Double(new_value));

    return;
  }

  /**
   * Finds best known action for given state.
   * @param State state
   */
  public Action findBestAction(State state)
  {
    Action bestAction = null;
    double bestValue = 0;
    for (Action a: m_actions) {
      String key = makeHashmapKey(state, a);
      double value = m_values.getOrDefault(key, 0.0);
      if (bestAction == null || value > bestValue) {
        bestAction = a;
        bestValue = value;
      }
    }
    return bestAction;
  }

  /**
   * Creates reproducable key for hashmap using objects.
   * @param State s
   * @param Action a
   */
  private String makeHashmapKey(State s, Action a)
  {
    String key = "";
    key += s.getStringKey();
    key += ";";
    key += a.getStringKey();
    return key;
  }

  /**
   * Gets number of already explored states.
   * @return int
   */
  public int getNumberOfExploredStates()
  {
    int exploredStates = m_values.keySet().size();
    return exploredStates;
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
