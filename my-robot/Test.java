package iwium;

import java.util.*;
import java.io.*;
import javafx.util.Pair;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

// THIS CLASS IS FINE!!!

public class Test
{
  private static final Logger logger = LogManager.getLogger("QLearningRobot");

  private static void testActionClass()
  {
    System.out.println("# Testing Action class");

    System.out.print(" - constructor and name getter");
    Action x = new Action(1, "Test");
    assert x.getName() == "Test";
    System.out.println(" (OK)");

    System.out.println("");
    logger.error("Hello this is a debug message");
    logger.info("Hello this is an info message");
  }

  private static void testParamClass()
  {
    System.out.println("# Testing Param class");

    System.out.print(" - constructor and name getter");
    Param p = new Param("param_0", 0, 10, 5);
    assert p.getName() == "param_0";
    System.out.println(" (OK)");

    System.out.print(" - getting value from empty param");
    try {
      p.getValue();
      assert false;
    } catch (RobotException e) {}
    System.out.println(" (OK)");

    System.out.print(" - setting and getting values");
    try {
      p.setNewValue(new Double(-12.23));
      assert p.getValue() == -12.23;
      assert p.getQuantizedValue() == 0;
    } catch (RobotException e) {
      assert false;
    }
    System.out.println(" (OK)");

    System.out.print(" - quantization");
    p.setNewValue(new Double(3));
    assert p.getQuantizedValue() == 1;
    p.setNewValue(new Double(13));
    assert p.getQuantizedValue() == 4;
    Param p1 = new Param("param_1", -5, 10, 15);
    p1.setNewValue(new Double(-6));
    assert p1.getQuantizedValue() == 0;
    p1.setNewValue(new Double(-5));
    assert p1.getQuantizedValue() == 0;
    p1.setNewValue(new Double(-4));
    assert p1.getQuantizedValue() == 1;
    p1.setNewValue(new Double(0));
    assert p1.getQuantizedValue() == 5;
    p1.setNewValue(new Double(9));
    assert p1.getQuantizedValue() == 13;
    p1.setNewValue(new Double(10));
    assert p1.getQuantizedValue() == 14;
    p1.setNewValue(new Double(11));
    assert p1.getQuantizedValue() == 14;
    System.out.println(" (OK)");

    System.out.print(" - cloning");
    p1.setNewValue(new Double(342.6));
    Param p1_cloned = new Param(p1);
    try {
      p1.setNewValue(new Double(873));
      assert p1_cloned.getValue() == 342.6;
    } catch (RobotException e) {
      assert false;
    }
    System.out.println(" (OK)");

    System.out.println("");
  }

  private static void testStateClass()
  {
    System.out.println("# Testing State class");

    System.out.print(" - array of params is not referenced");
    Param firstParam = new Param("firstParam", 0, 10, 5);
    Param secondParam = new Param("secondParam", 0, 128, 8);
    firstParam.setNewValue(new Double(3));
    secondParam.setNewValue(new Double(20));
    State s;
    s = new State(new ArrayList<>(Arrays.asList(
      firstParam,
      secondParam
    )));
    firstParam.setNewValue(new Double(-7));
    secondParam.setNewValue(new Double(83));
    ArrayList<Param> params = s.getParams();
    try {
      assert params.get(0).getValue() == 3;
      assert params.get(1).getValue() == 20;
    } catch (RobotException e) {
      assert false;
    }
    System.out.println(" (OK)");

    System.out.print(" - copy constructed object has own copy of params");
    State s2 = new State(s);
    firstParam.setNewValue(new Double(65));
    secondParam.setNewValue(new Double(-2));
    ArrayList<Param> s2_params = s2.getParams();
    try {
      assert s2_params.get(0).getValue() == 3;
      assert s2_params.get(1).getValue() == 20;
    } catch (RobotException e) {
      assert false;
    }
    System.out.println(" (OK)");

    System.out.print(" - params can be updated by name");
    s2.updateParam("secondParam", 34.6);
    ArrayList<Param> s2_params_n = s2.getParams();
    try {
      assert s2_params_n.get(0).getValue() == 3;
      assert s2_params_n.get(1).getValue() == 34.6;
    } catch (RobotException e) {
      assert false;
    }
    System.out.println(" (OK)");

    System.out.print(" - can be (de)serialized");
    try {
      ObjectOutputStream objectOutputStream = new ObjectOutputStream(new FileOutputStream("/tmp/xd_des"));
      objectOutputStream.writeObject(s2);
      objectOutputStream.close();
      ObjectInputStream objectInputStream = new ObjectInputStream(new FileInputStream("/tmp/xd_des"));
      State deserializedState = (State)objectInputStream.readObject();
      ArrayList<Param> ds_params = deserializedState.getParams();
      try {
        assert ds_params.get(0).getValue() == 3;
        assert ds_params.get(1).getValue() == 34.6;
      } catch (RobotException e) {
        assert false;
      }
    } catch (Exception e) {
      System.out.println(e);
      assert false;
    }
    System.out.println(" (OK)");

    System.out.println("");
  }

  private static void testQTableClass()
  {
    System.out.println("# Testing QTable class");

    System.out.print(" - constructed keys (from state+action) are the same in hashmap");
    HashMap<String, Double> map = new HashMap<String,Double>();
    Param firstParam = new Param("firstParam", 0, 10, 5);
    Param secondParam = new Param("secondParam", 0, 128, 8);
    firstParam.setNewValue(new Double(3));
    secondParam.setNewValue(new Double(20));
    State s;
    s = new State(new ArrayList<>(Arrays.asList(
      firstParam,
      secondParam
    )));
    Action a = new Action(0, "Test");
    String firstKey = s.getStringKey() + ";" + a.getStringKey();
    map.put(firstKey, 123.543);
    assert map.get(firstKey) == 123.543;
    //
    Param firstParam_x = new Param("firstParam", 0, 10, 5);
    Param secondParam_x = new Param("secondParam", 0, 128, 8);
    firstParam_x.setNewValue(new Double(3));
    secondParam_x.setNewValue(new Double(20));
    State s_x;
    s_x = new State(new ArrayList<>(Arrays.asList(
      firstParam_x,
      secondParam_x
    )));
    Action a_x = new Action(0, "Test");
    String secondKey = s_x.getStringKey() + ";" + a_x.getStringKey();
    assert map.get(secondKey) == 123.543;
    System.out.println(" (OK)");

    // test findBestAction

    // test updateRewards

    // test serialization

    System.out.println("");
  }

  public static void main(String[] args)
  {
    testActionClass();
    testParamClass();
    testStateClass();
    testQTableClass();
    return;
  }
}
