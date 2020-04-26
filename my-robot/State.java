package iwium;

import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.io.Serializable;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;

public class State implements Serializable
{
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
   * @param double value
   * @param bool whther update was successful
   */
  public boolean updateParam(String paramName, double value)
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
};
