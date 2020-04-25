package iwium;

import robocode.*;

import java.io.*;
import java.util.HashMap; 
import javafx.util.Pair;
import java.io.Serializable;

public class QTable implements Serializable
{
  private HashMap<Pair<State,Action>, Double> rewards;

  public QTable()
  {
    rewards = new HashMap<>(); 
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
}