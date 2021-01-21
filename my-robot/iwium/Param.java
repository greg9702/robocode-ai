package iwium;

public class Param
{
  private final String m_name;
  private final double m_min;
  private final double m_max;
  private final int m_buckets;
  private Double m_value;

  public Param(String name, double min, double max, int buckets)
  {
    m_name = name;
    m_min = min;
    m_max = max;
    m_buckets = buckets;
    m_value = null;
  }

  public Param(Param p) {
    m_name = p.m_name;
    m_min = p.m_min;
    m_max = p.m_max;
    m_buckets = p.m_buckets;
    m_value = p.m_value;
  }

  public String getName()
  {
    return m_name;
  }

  public double getValue() throws RobotException
  {
    if (m_value == null) {
      throw new RobotException("Unable to get value of empty parameter.");
    }
    return m_value;
  }

  public void setNewValue(Double val)
  {
    m_value = val;
  }

  public int getQuantizedValue() throws RobotException
  {
    if (m_value == null) {
      throw new RobotException("Unable to get value of empty parameter.");
    }

    double value_place_among_buckets = (m_value - m_min) / (m_max - m_min);
    int bin_id = (int)(Math.round((m_buckets - 1) * value_place_among_buckets));

    bin_id = Math.min(m_buckets - 1, Math.max(0, bin_id));

    return bin_id;
  }

  public int getNumBuckets()
  {
    return m_buckets;
  }

  public String getStringKey()
  {
    String key;
    try {
      key = m_name + "," + m_min + "," + m_max + "," + m_buckets + "," + getQuantizedValue();
    } catch (RobotException e) {
      key = m_name + "," + m_min + "," + m_max + "," + m_buckets + "," + "NULL";
    }
    return key;
  }

}
