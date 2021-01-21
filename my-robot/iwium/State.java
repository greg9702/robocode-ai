package iwium;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.util.ArrayList;

public class State
{
  private static final Logger logger = LogManager.getLogger("base");

  private final ArrayList<Param> m_params;

  public State(ArrayList<Param> params)
  {
    m_params = new ArrayList<>();
    for (Param p: params) {
      Param paramCopy = new Param(p);
      m_params.add(paramCopy);
    }
    m_params.sort((o1, o2) -> {
      String n1 = o1.getName();
      String n2 = o2.getName();
      return n1.compareTo(n2);
    });
  }

  public State(State s) {
    m_params = new ArrayList<>();
    for (Param p: s.m_params) {
      Param paramCopy = new Param(p);
      m_params.add(paramCopy);
    }
  }

  public void updateParam(String paramName, Double value)
  {
    for (Param p: m_params) {
      if (p.getName().equals(paramName)) {
        p.setNewValue(value);
        return;
      }
    }
  }

  public ArrayList<Param> getParams()
  {
    return m_params;
  }

  public Param getParam(String paramName)
  {
    for (Param p: m_params) {
      if (p.getName().equals(paramName)) {
        return p;
      }
    }
    return null;
  }

  public int getRowId()
  {
    int rowId = 0;
    int totalMultiplicator = 1;
    for (Param p: m_params) {
      try {
        rowId += p.getQuantizedValue() * totalMultiplicator;
      } catch (RobotException e) {
        logger.error("Unable to get param " + p.getName() + ": not set!");
      }

      totalMultiplicator *= p.getNumBuckets();
    }
    return rowId;
  }

  public int getNumStates()
  {
    int totalStates = 1;
    for (Param p: m_params) {
      int states = p.getNumBuckets();
      totalStates *= states;
    }
    return totalStates;
  }

  public String getStringKey()
  {
    StringBuilder key = new StringBuilder();
    for (Param p : m_params) {
      key.append(p.getStringKey());
      key.append(",");
    }
    return key.toString();
  }
};
