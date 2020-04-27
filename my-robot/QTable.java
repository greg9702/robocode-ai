package iwium;

import robocode.*;

import java.io.*;
import java.util.*;
import java.io.Serializable;

public class QTable implements Serializable
{
  // values that describe game profit of performing action in given state
  private HashMap<String, Double> m_values;

  // list of possible actions
  private ArrayList<Action> m_actions;

  private double m_alpha = 1.0; // learning rate
  private double m_gamma = 0.9; // discount factor

  public QTable(ArrayList<Action> actions)
  {
    m_values = new HashMap<String,Double>();
    m_actions = actions;
  }

  /**
   * Dumps current instance to file.
   * @param File
   */
  public void save(RobocodeFileOutputStream fout) throws IOException
  {
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

}  // class QTable
