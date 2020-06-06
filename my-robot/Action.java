package iwium;

public class Action
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
};
