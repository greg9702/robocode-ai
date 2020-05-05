package iwium;

import java.io.Serializable;

public class Action implements Serializable
{
  private int m_id;
  private String m_name;

  public Action(int id, String name)
  {
    this.m_id = id;
    this.m_name = name;
  }

  /**
   * Gets param name.
   * @return String
   */
  public String getName()
  {
    String name = m_name;
    return name;
  }

  /**
   * Constructs String that may be used to create hashmap key.
   * @return String
   */
  public String getStringKey()
  {
    String key = m_id + "," + m_name;
    return key;
  }
};
