package iwium;

import java.lang.Math;

public class Param
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
  public double getValue()
  {
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
}