package iwium;

import java.lang.Math;
import java.io.Serializable;

public class Param implements Serializable
{
  private String m_name;
  private double m_min;
  private double m_max;
  private int m_buckets;
  private Double m_value;

  public Param(String name, double min, double max, int buckets)
  {
    m_name = name;
    m_min = min;
    m_max = max;
    m_buckets = buckets;
    m_value = null;
  }

  /**
   * Copy constructor. Required for storing own copy of parameters.
   * @return Param
   */
  public Param(Param p) {
    m_name = p.m_name;
    m_min = p.m_min;
    m_max = p.m_max;
    m_buckets = p.m_buckets;
    m_value = p.m_value;
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
   * Gets real value of param.
   * @return double
   */
  public double getValue() throws RobotException
  {
    if (m_value == null) {
      throw new RobotException("Unable to get value of empty parameter.");
    }
    Double val = m_value;
    return val;
  }

  /**
   * Sets new param value.
   * @param double
   */
  public void setNewValue(double val)
  {
    m_value = new Double(val);
    return;
  }

  /**
   * Quantizes param value.
   * @return int
   */
  public int getQuantizedValue()
  {
    // if we place buckets on X axis, starting with 0, then we can find where our value is among buckets
    //  eg. 0.5 -> means it is in the center
    double value_place_among_buckets = (m_value - m_min) / (m_max - m_min);
    int bin_id = (int)(Math.round((m_buckets - 1) * value_place_among_buckets));

    // small correction if we get outside
    bin_id = Math.min(m_buckets - 1, Math.max(0, bin_id));

    return bin_id;
  }

  /**
   * Constructs String that may be used to create hashmap key.
   * @return String
   */
  public String getStringKey()
  {
    String key;
    if (m_value == null) {
      key = m_name + "," + m_min + "," + m_max + "," + m_buckets + "," + "NULL";
    } else {
      key = m_name + "," + m_min + "," + m_max + "," + m_buckets + "," + m_value;
    }
    return key;
  }
}
