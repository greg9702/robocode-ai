package iwium;

import robocode.*;

import java.io.*;
import java.util.HashMap; 
import javafx.util.Pair;
import java.io.Serializable;

public class QTable implements Serializable
{
  private HashMap<Pair<State,Action>, Double> m_rewards;

  public QTable()
  {
    m_rewards = new HashMap<Pair<State,Action>,Double>();
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
   * @param State s
   * @param Action a
   * @param double reward
   */
  public void updateRewards(State s, Action a, double reward)
  {
    Pair key = new Pair(s, a);
    // init key if not exists
    if (m_rewards.containsKey(key) == false) {
      m_rewards.put(key, new Double(0));
    }

    // TODO consider previous value
    m_rewards.put(key, new Double(reward));
    return;
  }
}