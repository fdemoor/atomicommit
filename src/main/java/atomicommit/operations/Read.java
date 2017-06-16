package atomicommit.operations;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.util.HashMap;

public class Read extends Operation {

  private final Logger logger = LogManager.getLogger();

  public Read(int k) {
    super(k);
  }

  double perform(HashMap<Integer,Double> table) throws InvalidOperation {
    Double x = table.get(key);
    if (x == null) {
      throw new InvalidOperation("Value to read does not exist");
    } else {
      return x;
    }
  }

  void rollback(HashMap<Integer,Double> table) {
    return;
  }

}
