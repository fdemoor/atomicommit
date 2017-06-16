package atomicommit.operations;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.util.HashMap;

public class Write extends Operation {

  private final double value;
  private Double former;
  private final Logger logger = LogManager.getLogger();

  public Write(int k, double v) {
    super(k);
    value = v;
    former = null;
  }

  double perform(HashMap<Integer,Double> table) throws InvalidOperation {
    Double x = table.put(key, value);
    former = x;
    return value;
  }

  void rollback(HashMap<Integer,Double> table) {
    if (former == null) {
      table.remove(key);
    } else {
      table.put(key, former);
    }
  }

}
