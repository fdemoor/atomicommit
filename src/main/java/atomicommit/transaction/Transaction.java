package atomicommit.transaction;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.util.ArrayList;

public class Transaction {

  private final int myID;
  private final Logger logger = LogManager.getLogger();

  public Transaction(int n) {
    myID = n;
  }

  public int getID() {
    return myID;
  }

  public void commit() {

  }

  public void abort() {

  }

}
