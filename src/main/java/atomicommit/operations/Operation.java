package atomicommit.operations;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.util.HashMap;

public abstract class Operation {

  protected final int key;
  private final Logger logger = LogManager.getLogger();

  public Operation(int k) {
    key = k;
  }

  abstract double perform(HashMap<Integer,Double> table) throws InvalidOperation;

  abstract void rollback(HashMap<Integer,Double> table);

}
