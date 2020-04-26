package iwium;

import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.io.Serializable;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;

public class State implements Cloneable
{
  private ArrayList<Param> m_params;

  public State(ArrayList<Param> params)
  {
    m_params = params;
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
    // sort params by name
    Collections.sort(m_params, new Comparator<Param>() {
      @Override
      public int compare(Param o1, Param o2) {
        String n1 = o1.getName();
        String n2 = o2.getName();
        return n1.compareTo(n2);
      }
    });

    aOutputStream.writeInt(m_params.size());
    for (Param p : m_params) {
      aOutputStream.writeObject(p);
    }
  }

  @Override
  protected Object clone() {
    // TODO does params are cloned?
    try {
      return super.clone();
    } catch (CloneNotSupportedException e) {
      System.out.println("Unable to clone!");
      return new Object();
    }
  }
};
