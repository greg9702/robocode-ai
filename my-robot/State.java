package iwium;

import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.io.Serializable;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

public class State
{
  private static final Logger logger = LogManager.getLogger("base");

  private ArrayList<Param> m_params;

  public State(ArrayList<Param> params)
  {
    m_params = new ArrayList<Param>();
    for (Param p: params) {
      Param paramCopy = new Param(p);
      m_params.add(paramCopy);
    }
    // sort params by name
    Collections.sort(m_params, new Comparator<Param>() {
      @Override
      public int compare(Param o1, Param o2) {
        String n1 = o1.getName();
        String n2 = o2.getName();
        return n1.compareTo(n2);
      }
    });
  }

  /**
   * Copy constructor. Required for storing copy of state.
   * @return Param
   */
  public State(State s) {
    m_params = new ArrayList<Param>();
    for (Param p: s.m_params) {
      Param paramCopy = new Param(p);
      m_params.add(paramCopy);
    }
  }

  /**
   * Updates parameter by name.
   * @param String paramName
   * @param Double value
   * @param bool whther update was successful
   */
  public boolean updateParam(String paramName, Double value)
  {
    for (Param p: m_params) {
      if (p.getName().equals(paramName)) {
        p.setNewValue(value);
        return true;
      }
    }
    return false;
  }

  private void readObject(ObjectInputStream aInputStream) throws ClassNotFoundException, IOException
  {
    m_params = new ArrayList<Param>();
    int num_params = aInputStream.readInt();
    for (int i=0; i<num_params; i++) {
      Param p = (Param)aInputStream.readObject();
      m_params.add(p);
    }
  }

  private void writeObject(ObjectOutputStream aOutputStream) throws IOException
  {
    aOutputStream.writeInt(m_params.size());
    for (Param p : m_params) {
      aOutputStream.writeObject(p);
    }
  }

  /**
   * UNSAFE method! Gets param array.
   * Used in test suite to check references.
   * @return ArrayList<Param>
   */
  public ArrayList<Param> getParams()
  {
    return m_params;
  }

  /**
   * Converts current state to QTable row ID.
   * @return int
   */
  public int getRowId()
  {
    int rowId = 0;
    int totalMultiplicator = 1;
    for (Param p: m_params) {
      try {
        rowId += p.getQuantizedValue() * totalMultiplicator;
        logger.error("adding " + p.getQuantizedValue() + "*" + totalMultiplicator );
        logger.error(p.getName());
      } catch (RobotException e) {
        logger.error("Unable to get param value: not set!");
      }

      totalMultiplicator *= p.getNumBuckets();
    }
    return rowId;
  }

  /**
   * Gets number of total possible states.
   * @return int
   */
  public int getNumStates()
  {
    int totalStates = 1; // we use 1 because we perform multiplication
    for (Param p: m_params) {
      int states = p.getNumBuckets();
      totalStates *= states;
    }
    return totalStates;
  }
};
