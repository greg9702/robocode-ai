package iwium;

public class Action
{
  private final int m_id;
  private final String m_name;

  public Action(int id, String name)
  {
    this.m_id = id;
    this.m_name = name;
  }

  public String getName()
  {
    return m_name;
  }

  public String getStringKey()
  {
    return m_id + "," + m_name;
  }

};
